package csvreadwrite;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;

import utils.BigDate;
import utils.FastCat;
import utils.Misc;

import static csvreadwrite.CSVCharCategory.*;
import static csvreadwrite.CSVReadState.*;
import static java.lang.System.*;

public final class CSVReader
{
/**
 * true if want debugging output and debugging test harness code
 */
private static final boolean DEBUGGING = false;

/**
 * e.g. \n \r\n or \r, whatever system uses to separate lines in a text file. Only used inside multiline fields. The
 * file itself should use Windows format \r \n, though \n by itself will also work.
 */
private static final String lineSeparator = System.getProperty( "line.separator" );

/**
 * what chars start off a comment.
 */
private final String commentChars;

/**
 * true if reader should allow quoted fields to span more than one line. Microsoft Excel sometimes generates files
 * like this.
 */
private final boolean allowMultiLineFields;

/**
 * true means client does not see the comments.  It is as if they were not there.
 */
private final boolean hideComments;

/**
 * true if quoted fields are trimmed of lead/trail blanks.  Usually true.
 */
private final boolean trimQuoted;

/**
 * true if unquoted fields are trimmed of lead/trail blanks. Usually true.
 */
private final boolean trimUnquoted;

/**
 * quote character, usually '\"' '\'' for SOL used to enclose fields containing a separator character.
 */
private final char quoteChar;

/**
 * field separator character, usually ',' in North America, ';' in Europe and sometimes '\t' for tab.
 */
private final char separatorChar;

/**
 * Reader source of the CSV fields to be read. Caller gets to decide on the encoding for the Reader when it creates
 * the Reader to pass to the constructor.
 */
//@Nullable
private BufferedReader r;

/**
 * table to for quick lookup of char category for chars 0..255
 */
private CSVCharCategory[] lookup;

/**
 * state of the parser's finite state automaton for parsing characters on the line
 */
private CSVReadState state = SEEKING_FIELD;

/**
 * The line we are parsing. null means none read yet. Line contains
 * unprocessed chars. Processed ones are removed.
 */
//@Nullable
private String line = null;

/**
 * true if can use fast cached table lookup to categorise chars, later set true when table built.
 */
private boolean useLookup = false;

/**
 * true if last field returned via get was a comment, including a ## label comment..
 */
private boolean wasComment;

/**
 * true if last field returned via get was a ## label comment.
 */
private boolean wasLabelComment;

/**
 * how many fields have been processed on this line so far, not counting comments or EOL
 */
private int fieldCount = 0;

/**
 * How many lines we have read so far. Used in error messages. Hidden comment lines count.
 */
private int lineCount = 0;

/**
 * points to next character to process in the current line.
 */
private int offset;

/**
 * Simplified convenience constructor to read a CSV file , default to comma separator, " for quote,
 * no multiline fields, with trimming.
 *
 * @param r input Reader source of CSV Fields to read.  Should be buffered.
 */
public CSVReader( Reader r )
    {
    this( r /* reader, with implied encoding */,
            ',' /* separator char */,
            '\"'/* quoteChar */,
            "#" /* commentChars */,
            true /* hideComments */,
            true /* trimQuoted */,
            true /* trimUnquoted */,
            false /* allowMultipleLineFields*/ );
    }

/**
 * Detailed constructor to read a CSV file
 *
 * @param r                    input Reader source of CSV Fields to read.  Should be a BufferedReader.
 * @param separatorChar        field separator character, usually ',' in North America, ';' in Europe and sometimes
 *                             '\t' for tab. Note this is a 'char' not a "string".
 * @param quoteChar            char to use to enclose fields containing a separator, usually '\"' . Use (char)0 if
 *                             you don't want a quote character, or any other char that will not appear in the file.
 *                             Note this is a 'char' not a "string".
 * @param commentChars         characters that mark the start of a comment, usually "#", but can be multiple chars.
 *                             Note this is a "string" not a 'char'.
 * @param hideComments         true if clients sees none of the comments.  false if client processes the comments.
 * @param trimQuoted           true if quoted fields are trimmed of lead/trail blanks.  Usually true.
 * @param trimUnquoted         true if unquoted fields are trimmed of lead/trail blanks. Usually true.
 * @param allowMultiLineFields true if reader should allow quoted fields to span more than one line. Microsoft Excel
 */
public CSVReader( final Reader r,
                  final char separatorChar,
                  final char quoteChar,
                  final String commentChars,
                  final boolean hideComments,
                  final boolean trimQuoted,
                  final boolean trimUnquoted,
                  final boolean allowMultiLineFields
)
    {
    /* convert Reader to BufferedReader if necessary */
    this.r = ( r instanceof BufferedReader ) ? ( BufferedReader ) r : new BufferedReader( r );    /* default
    buffer size is 8K */
    this.separatorChar = separatorChar;
    this.quoteChar = quoteChar;
    this.commentChars = commentChars;
    this.hideComments = hideComments;
    this.trimQuoted = trimQuoted;
    this.trimUnquoted = trimUnquoted;
    this.allowMultiLineFields = allowMultiLineFields;
    buildLookup();
    }

/**
 * build table to for quick lookup of char category.
 */
void buildLookup()
    {
    lookup = new CSVCharCategory[ 256 ];
    for ( int c = 0; c < 256; c++ )
        {
        lookup[ c ] = categorise( ( char ) c );
        }
    useLookup = true; // table now safe to use
    }

/**
 * categorise a character for the finite state machine.
 *
 * @param c the character to categorise
 *
 * @return integer representing the character's category.
 */
CSVCharCategory categorise( char c )
    {
    if ( useLookup && c < 256 )
        {
        return lookup[ c ];  // quick lookup of cached values.
        }
    switch ( c )
        {
        case ' ':
        case '\r':
        case '\n':
        case 0xff:
            return WHITESPACE;
        default:
            if ( c == separatorChar )
                {
                return SEPARATOR;
                }
            else if ( c == quoteChar )
                {
                return QUOTE;
                }
            if ( commentChars.indexOf( c ) >= 0 )
                {
                return COMMENT_START;
                }
            /* do our tests in crafted order, hoping for an early return */
            else if ( '!' <= c && c <= '~' )/* includes A-Z \\p{Lower} 0-9 common punctuation */
                {
                return ORDINARY;
                }
            else if ( 0x00 <= c && c <= 0x20 || Character.isWhitespace( c ) )
                {
                return WHITESPACE;
                }
            else
                {
                return ORDINARY;
                }
        }
    }

/**
 * compose an error message to describe what is wrong with the CSV file
 *
 * @param missing what expected char is missing.
 * @param near    offset near where the error is.
 *
 * @return String to describe the problem  to the user
 */
private String complaint( char missing, int near )
    {
    final FastCat sb = new FastCat( 8 );
    sb.append( "Malformed CSV stream. Missing [" );
    sb.append( missing );
    sb.append( "] near offset " );
    sb.append( near );
    sb.append( " after field " );
    sb.append( fieldCount );
    sb.append( " on line " );
    sb.append( lineCount );
    return sb.toString();
    }

/**
 * Make sure a line is available for parsing. Does nothing if there already is one.
 *
 * @throws EOFException if hit the end of the file.
 */
private void getLineIfNeeded() throws IOException
    {
    if ( line == null )
        {
        if ( r == null )
            {
            throw new IllegalArgumentException(
                    "attempt to use a closed CSVReader" );
            }
        line = r.readLine();/* this strips platform specific line ending */
        fieldCount = 0;
        if ( line == null )/*
         * null means EOF, yet another inconsistent Java
         * convention.
         */
            {
            throw new EOFException();
            }
        else
            {
            offset = 0;
            lineCount++;
            }
        }
    }

/**
 * Get field inside quotes.
 *
 * @param field accumulated chars for field.
 *
 * @return String, possibly trimmed.
 */
private String quotedField( final StringBuilder field )
    {
    return trimQuoted ? field.toString().trim() : field.toString();
    }

/**
 * Get field between commas.
 *
 * @param field accumulated chars for field.
 *
 * @return String, possibly trimmed.
 */
private String unquotedField( final StringBuilder field )
    {
    return trimUnquoted ? field.toString().trim() : field.toString();
    }

/**
 * Close the Reader.
 *
 * @throws IOException if problems closing
 */
public void close() throws IOException
    {
    if ( r != null )
        {
        r.close();
        r = null;
        }
    }

/**
 * Read one field from the CSV file. You can also use methods like getInt and getDouble to parse the String for you.
 * You can use getAllFieldsInLine to read the entire line including the EOL.
 *
 * @return String value, even if the field is numeric. Surrounded and embedded double quotes are stripped. possibly
 * "". null means end of line.  Normally you use skiptoNextLine to start the next line rather then using get
 * to read the eol.  Might also be a comment, with lead # stripped.
 * If field was a comment, it is returned with lead # stripped. Check wasComment to see if it was a comment
 * or a data field.
 * @throws EOFException at end of file after all the fields have been read.
 * @throws IOException  Some problem reading the file, possibly malformed data.
 * @noinspection UnusedLabel
 */
public String get() throws IOException
    {
    // we need a StringBuilder since we analyse double " and handle multiline fields.
    // We can't an simply track start and end of string.
    // StringBuilder is better than FastCat for char by char work.
    final StringBuilder field = new StringBuilder( allowMultiLineFields ? 512 : 64 );
    /* don't need to maintain state between fields, just during the processing of a field.
      We need to track offset where to continue processing line between fields, however. */
    this.wasComment = false;
    this.wasLabelComment = false;
    state = SEEKING_FIELD;
    lineLoop:
    while ( true )
        {
        // for a multi-line field we might need to read several lines.
        getLineIfNeeded();
        assert line != null : "program bug: null line being scanned";
        charLoop:
        /* loop for each char in the line to find a field */
        /* guaranteed to leave early by hitting EOL (when not multiline) */
        // pick up from where we left off getting last field.
        for ( int i = offset; i < line.length(); i++ )
            {
            char c = line.charAt( i );
            CSVCharCategory category = categorise( c );
            if ( DEBUGGING )
                {
                // for debugging
                out.println( "char:"
                             + c
                             + " oldState:"
                             + state
                             + " fieldCount: "
                             + fieldCount
                             + " field:["
                             + field.toString() + "]" );
                }
            // nested switch state:char category
            switch ( state )
                {
                // ---------------------------------
                case AFTER_END_QUOTE:
                {
                /*
                 * In situation like this "xxx" which may turn out to be
                 * xxx""xxx" or "xxx", We find out here.
                 */
                switch ( category )
                    {
                    case COMMENT_START:
                        // either skip over comment or process it later
                        offset = hideComments ? line.length() : i;
                        // handle pending field
                        state = SEEKING_FIELD;
                        fieldCount++;
                        return quotedField( field );
                    case ORDINARY:
                        throw new IOException( complaint( separatorChar, i ) );
                    case QUOTE:
                        /* was a double quotechar, e.g. a literal " */
                        field.append( c );
                        state = IN_QUOTED_FIELD;
                        break;
                    case SEPARATOR:
                        /* we are done with field. */
                        offset = i + 1;
                        state = SEEKING_START;
                        fieldCount++;
                        return quotedField( field );
                    case WHITESPACE:
                        /* ignore trailing spaces up to separatorChar */
                        state = SKIPPING_TAIL_AFTER_QUOTE;
                        break;
                    default:
                        throw new IllegalArgumentException( "bug: no case for state " + category.toString() );
                    }
                break;
                }
                // ---------------------------------
                case IN_PLAIN_FIELD:
                {
                /* in middle of ordinary field */
                switch ( category )
                    {
                    case COMMENT_START:
                        // either skip over comment, or handle it later
                        offset = hideComments ? line.length() : i;
                        // handle pending field
                        state = SEEKING_FIELD;
                        fieldCount++;
                        return unquotedField( field );
                    case ORDINARY:
                        field.append( c );
                        break;
                    case QUOTE:
                        throw new IOException( complaint( quoteChar, i ) );
                    case SEPARATOR:
                        /* done */
                        offset = i + 1;
                        state = SEEKING_START;
                        fieldCount++;
                        return unquotedField( field );
                    case WHITESPACE:
                        field.append( ' ' );
                        break;
                    default:
                        throw new IllegalArgumentException( "bug: no case for state " + category.toString() );
                    }
                break;
                }
                // ---------------------------------
                case IN_QUOTED_FIELD:
                {
                /* in middle of field surrounded in quotes */
                switch ( category )
                    {
                    case COMMENT_START:  // inside quotes only " is a special character.
                    case ORDINARY:
                    case SEPARATOR:
                    case WHITESPACE:
                        field.append( c );
                        break;
                    case QUOTE:
                        state = AFTER_END_QUOTE;
                        break;
                    default:
                        throw new IllegalArgumentException( "bug: no case for state " + category.toString() );
                    }
                break;
                }
                // ---------------------------------
                case SEEKING_FIELD:
                {
                /* in blanks before first field, or after a comma*/
                switch ( category )
                    {
                    case COMMENT_START:
                        if ( hideComments )
                            {
                            if ( fieldCount > 0 )
                                { // bypass comment
                                offset = line.length(); /* carry on at eol */
                                state = SEEKING_FIELD;
                                break charLoop;
                                }
                            else
                                {
                                // comment on by itself. ignore the whole thing, including the EOL.
                                line = null;
                                state = SEEKING_FIELD;
                                continue lineLoop;
                                }
                            }
                        else
                            {
                            // entire rest of line is a comment.
                            offset = line.length();/* carry on at eol */
                            state = SEEKING_FIELD;
                            // don't increment fieldCount, this is a comment field.
                            wasComment = true;
                            // strip off lead #
                            final String comment = trimUnquoted ? line.substring( i + 1 ).trim() : line.substring
                                    ( i + 1 );
                            if ( comment.length() > 0 && commentChars.indexOf( comment.charAt( 0 ) ) >= 0 )
                                {
                                wasLabelComment = true;
                                }
                            return comment;
                            }
                    case QUOTE:
                        state = IN_QUOTED_FIELD;
                        break;
                    case SEPARATOR:
                        /* end of empty field */
                        offset = i + 1;
                        state = SEEKING_START;
                        fieldCount++;
                        return "";
                    case ORDINARY:
                        field.append( c );
                        state = IN_PLAIN_FIELD;
                        break;
                    case WHITESPACE:
                        /* ignore */
                        break;
                    default:
                        throw new IllegalArgumentException( "bug: no case for state " + category.toString() );
                    }
                break;
                }
                // ---------------------------------
                case SEEKING_START:
                {
                /* in blanks before field */
                switch ( category )
                    {
                    case COMMENT_START:
                        // either bypass comment or arrange to deal with it later
                        offset = hideComments ? line.length() : i;
                        // handle pending empty field
                        state = SEEKING_FIELD;
                        fieldCount++;
                        return "";
                    case QUOTE:
                        state = IN_QUOTED_FIELD;
                        break;
                    case SEPARATOR:
                        /* end of empty field */
                        offset = i + 1;
                        state = SEEKING_START;
                        fieldCount++;
                        return "";
                    case ORDINARY:
                        field.append( c );
                        state = IN_PLAIN_FIELD;
                        break;
                    case WHITESPACE:
                        /* ignore */
                        break;
                    default:
                        throw new IllegalArgumentException( "bug: no case for state " + category.toString() );
                    }
                break;
                }
                // ---------------------------------
                case SKIPPING_TAIL_AFTER_QUOTE:
                {
                /* in spaces after quoted field, seeking separatorChar */
                switch ( category )
                    {
                    case COMMENT_START:
                        // handle pending field, deal with comment later.
                        offset = i;
                        state = SEEKING_FIELD;
                        fieldCount++;
                        return quotedField( field );
                    case ORDINARY:
                    case QUOTE:
                        throw new IOException( complaint( separatorChar, i )
                        );
                    case SEPARATOR:
                        offset = i + 1;
                        state = SEEKING_START;
                        fieldCount++;
                        return quotedField( field );
                    case WHITESPACE:
                        /* ignore trailing spaces up to separatorChar */
                        break;
                    default:
                        throw new IllegalArgumentException( "bug: no case for state " + category.toString() );
                    }
                break;
                }
                // ---------------------------------
                default:
                    throw new IllegalArgumentException( "bug: no case for state " + state.toString() );
                } // end switch(state)
            } // end charLoop over remaining chars in line.
        // -------------------------------------------------------------------------
        if ( DEBUGGING )
            {
            // for debugging
            out.println( "EOL state:"
                         + state
                         + " fieldCount: "
                         + fieldCount
                         + " field:["
                         + field.toString() + "]" );
            }
        // if not found a field yet, handle the end of line.
        // this code is difficult to encapsulate since following switch code does not always return.
        switch ( state )
            {
            // ---------------------------------
            case AFTER_END_QUOTE:
                /*
                 * In situation like this "xxx" which may turn out to be
                 * xxx""xxx" or "xxx", We find out here.
                 */
                offset = line.length();/* carry on at eol */
                state = SEEKING_FIELD;
                fieldCount++;
                return quotedField( field );
            // ---------------------------------
            case SEEKING_FIELD:
                /* in blanks prior to start of a field  */
                /* null to mark end of line */
                // mark line as done, we need a new line of characters.
                // carry on at start of next line.
                line = null;
                state = SEEKING_FIELD;
                // return null for EOL
                return null;
            // ---------------------------------
            case SEEKING_START:
                /* in blanks after , */
                /* null to mark end of line */
                offset = line.length();/* carry on at eol */
                // handle pending empty field
                state = SEEKING_FIELD;
                fieldCount++;
                return "";
            // ---------------------------------
            case IN_PLAIN_FIELD:
                /* in middle of ordinary field */
                offset = line.length(); /* push EOL back */
                state = SEEKING_FIELD;
                fieldCount++;
                return unquotedField( field );
            // ---------------------------------
            case IN_QUOTED_FIELD:
                /* in middle of field surrounded in quotes */
                if ( allowMultiLineFields )
                    {
                    field.append( lineSeparator );
                    // we are done with that line, but not with
                    // the field.
                    // We don't want to return a null
                    // to mark the end of the line., but we do want another line to process.
                    line = null;
                    // Will read next line and seek the end of
                    // the quoted field with state = IN_QUOTED_FIELD.
                    break;
                    }
                else
                    {
                    // no multiline fields allowed
                    throw new IOException( complaint( quoteChar, line.length() - 1 ) );
                    }
                // ---------------------------------
            case SKIPPING_TAIL_AFTER_QUOTE:
                offset = line.length();/* carry on at eol */
                state = SEEKING_FIELD;
                fieldCount++;
                return quotedField( field );
            // ---------------------------------
            default:
                throw new IllegalArgumentException( "bug: no case for state " + state.toString() );
            } // end switch
        } // end lineLoop
    } // end get

/**
 * Get all fields in the line. This reads only one line, not the whole file.
 * Skips to next line as a side effect, so don't need skipToNextLine.
 * Can find out if last field was a comment with wasComment();
 * If hideComments true in CSVReader Constructor then any comment will not be available.
 *
 * @return Array of strings, one for each field. Possibly empty, but never null.
 * @throws EOFException if run off the end of the file.
 * @throws IOException  if some problem reading the file.
 */
public String[] getAllFieldsInLine() throws IOException
    {
    final ArrayList<String> al = new ArrayList<>( 100 );
    boolean lineHadComment = false;
    do
        {
        String field = get();
        if ( field == null )
            {
            break;
            }
        if ( wasComment )
            {
            if ( hideComments )
                {
                wasComment = false;
                // ignore the comment altogether.
                continue;
                }
            else
                {
                lineHadComment = true;
                }
            }
        al.add( field );
        }
    while ( true );
    if ( lineHadComment )
        {
        wasComment = true;  // need to track specially since get null turns off last wasComment.
        final String comment = al.get( al.size() - 1 );
        if ( comment.length() > 0 && commentChars.indexOf( comment.charAt( 0 ) ) >= 0 )
            {
            wasLabelComment = true;
            }
        }
    return al.toArray( new String[ al.size() ] );
    }

/**
 * Read one boolean field from the CSV file, e.g. (true, yes, 1, +) or (false, no, 0, -).
 *
 * @return boolean, empty field returns false, as does end of line.
 * @throws EOFException           at end of file after all the fields have been read.
 * @throws IOException            Some problem reading the file, possibly malformed data.
 * @throws NumberFormatException, if field does not contain a well-formed int.
 * @noinspection UnusedDeclaration
 */
public boolean getBoolean() throws NumberFormatException, IOException
    {
    final String s = get();
    try
        {
        return Misc.parseBoolean( s, false );
        }
    catch ( NumberFormatException e )
        {
        final FastCat sb = new FastCat( 10 );
        sb.append( "Malformed boolean [", s, "]" );
        sb.append( " near offset ", line.length() - 1 );
        sb.append( " after field ", fieldCount );
        sb.append( " on line ", lineCount, "\n" );
        sb.append( e.getMessage() );
        throw new NumberFormatException( sb.toString() );
        }
    }

/**
 * Read one Character field from the CSV file
 *
 * @return char value, empty field returns 0, as does end of line.
 * @throws EOFException           at end of file after all the fields have been read.
 * @throws IOException            Some problem reading the file, possibly malformed data.
 * @throws NumberFormatException, if field does not contain a well-formed int.
 * @noinspection UnusedDeclaration
 */
public char getChar() throws IOException, NumberFormatException
    {
    // togo: clerify if this expects a single char or an unsigned 16 bit int.
    final String s = get();
    // end of line returns 0
    if ( s == null || s.length() == 0 )
        {
        return 0;
        }
    if ( s.length() == 1 )
        {
        return s.charAt( 0 );
        }
    else
        {
        throw new NumberFormatException(
                "Malformed char ["
                + s
                + "] near offset "
                + ( line.length() - 1 )
                + " after field "
                + fieldCount
                + " on line "
                + lineCount
        );
        }
    }

/**
 * Read one double field from the CSV file.
 *
 * @return double value, empty field returns 0, as does end of line.
 * @throws EOFException           at end of file after all the fields have been read.
 * @throws IOException            Some problem reading the file, possibly malformed data.
 * @throws NumberFormatException, if field does not contain a well-formed double.
 * @noinspection UnusedDeclaration
 */
public double getDouble() throws IOException, NumberFormatException
    {
    final String s = get();
    if ( s == null || s.length() == 0 )
        {
        return 0;
        }
    try
        {
        return Double.parseDouble( s );
        }
    catch ( NumberFormatException e )
        {
        throw new NumberFormatException(
                "Malformed double ["
                + s
                + "] near offset "
                + ( line.length() - 1 )
                + " after field "
                + fieldCount
                + " on line "
                + lineCount
        );
        }
    }

/**
 * Read one float field from the CSV file.
 *
 * @return float value, empty field returns 0, as does end of line.
 * @throws EOFException           at end of file after all the fields have been read.
 * @throws IOException            Some problem reading the file, possibly malformed data.
 * @throws NumberFormatException, if field does not contain a well-formed float.
 * @noinspection UnusedDeclaration
 */
public float getFloat() throws IOException, NumberFormatException
    {
    final String s = get();
    if ( s == null || s.length() == 0 )
        {
        return 0;
        }
    try
        {
        return Float.parseFloat( s );
        }
    catch ( NumberFormatException e )
        {
        throw new NumberFormatException(
                "Malformed float ["
                + s
                + "] near offset "
                + ( line.length() - 1 )
                + " after field "
                + fieldCount
                + " on line "
                + lineCount
        );
        }
    }

/**
 * Read one hex-encoded integer field from the CSV file
 *
 * @return int value, empty field returns 0, as does end of line.
 * @throws EOFException           at end of file after all the fields have been read.
 * @throws IOException            Some problem reading the file, possibly malformed data.
 * @throws NumberFormatException, if field does not contain a well-formed int.
 * @noinspection UnusedDeclaration
 */
public int getHexInt() throws IOException, NumberFormatException
    {
    final String s = get();
    // end of line returns 0
    if ( s == null || s.length() == 0 )
        {
        return 0;
        }
    try
        {
        return Integer.parseInt( s, 16 );
        }
    catch ( NumberFormatException e )
        {
        throw new NumberFormatException(
                "Malformed hex integer ["
                + s
                + "] near offset "
                + ( line.length() - 1 )
                + " after field "
                + fieldCount
                + " on line "
                + lineCount
        );
        }
    }

/**
 * Read one hex-encoded long field from the CSV file
 *
 * @return long value, empty field returns 0, as does end of line.
 * @throws EOFException           at end of file after all the fields have been read.
 * @throws IOException            Some problem reading the file, possibly malformed data.
 * @throws NumberFormatException, if field does not contain a well-formed long.
 * @noinspection UnusedDeclaration
 */
public long getHexLong() throws IOException, NumberFormatException
    {
    final String s = get();
    if ( s == null || s.length() == 0 )
        {
        return 0;
        }
    try
        {
        return Long.parseLong( s, 16 );
        }
    catch ( NumberFormatException e )
        {
        throw new NumberFormatException(
                "Malformed hex long integer ["
                + s
                + "] near offset "
                + ( line.length() - 1 )
                + " after field "
                + fieldCount
                + " on line "
                + lineCount
        );
        }
    }

/**
 * Read one integer field from the CSV file
 *
 * @return int value, empty field returns 0, as does end of line.
 * @throws EOFException           at end of file after all the fields have been read.
 * @throws IOException            Some problem reading the file, possibly malformed data.
 * @throws NumberFormatException, if field does not contain a well-formed int.
 * @noinspection UnusedDeclaration
 */
public int getInt() throws IOException, NumberFormatException
    {
    final String s = get();
    // end of line returns 0
    if ( s == null || s.length() == 0 )
        {
        return 0;
        }
    try
        {
        return Integer.parseInt( s );
        }
    catch ( NumberFormatException e )
        {
        throw new NumberFormatException(
                "Malformed integer ["
                + s
                + "] near offset "
                + ( line.length() - 1 )
                + " after field "
                + fieldCount
                + " on line "
                + lineCount
        );
        }
    }

/**
 * How many lines have been processed so far. Use
 *
 * @return count of how many lines have been read.
 * @see lineCount()
 * @deprecated use lineCount()
 */
public int getLineCount()
    {
    return lineCount;
    }

/**
 * Read one long field from the CSV file
 *
 * @return long value, empty field returns 0, as does end of line.
 * @throws EOFException           at end of file after all the fields have been read.
 * @throws IOException            Some problem reading the file, possibly malformed data.
 * @throws NumberFormatException, if field does not contain a well-formed long.
 * @noinspection UnusedDeclaration
 */
public long getLong() throws IOException, NumberFormatException
    {
    final String s = get();
    if ( s == null || s.length() == 0 )
        {
        return 0;
        }
    try
        {
        return Long.parseLong( s );
        }
    catch ( NumberFormatException e )
        {
        throw new NumberFormatException(
                "Malformed long integer ["
                + s
                + "] near offset "
                + ( line.length() - 1 )
                + " after field "
                + fieldCount
                + " on line "
                + lineCount
        );
        }
    }

/**
 * Read one integer field from the CSV file
 *
 * @return short value, empty field returns 0, as does end of line.
 * @throws EOFException           at end of file after all the fields have been read.
 * @throws IOException            Some problem reading the file, possibly malformed data.
 * @throws NumberFormatException, if field does not contain a well-formed int.
 * @noinspection UnusedDeclaration
 */
public short getShort() throws IOException, NumberFormatException
    {
    final String s = get();
    // end of line returns 0
    if ( s == null || s.length() == 0 )
        {
        return 0;
        }
    try
        {
        return Short.parseShort( s );
        }
    catch ( NumberFormatException e )
        {
        throw new NumberFormatException(
                "Malformed short ["
                + s
                + "] near offset "
                + ( line.length() - 1 )
                + " after field "
                + fieldCount
                + " on line "
                + lineCount
        );
        }
    }

/**
 * Read one Date field from the CSV file, in ISO format yyyy-mm-dd
 *
 * @return yyyy-mm-dd date string, empty field returns "",  end of line. returns null.
 * @throws EOFException           at end of file after all the fields have been read.
 * @throws IOException            Some problem reading the file, possibly malformed data.
 * @throws NumberFormatException, if field does not contain a well-formed date.
 * @noinspection UnusedDeclaration
 */
public String getYYYYMMDD() throws IOException
    {
    final String s = get();
    if ( s == null || s.length() == 0 )
        {
        return s;
        }
    if ( !BigDate.isValid( s ) )
        {
        throw new NumberFormatException(
                "Malformed ISO yyyy-mm-dd date ["
                + s
                + "] near offset "
                + ( line.length() - 1 )
                + " after field "
                + fieldCount
                + " on line "
                + lineCount
        );
        }
    return s;
    }

/**
 * How many lines have been processed so far.
 *
 * @return count of how many lines have been read.
 */
public int lineCount()
    {
    return lineCount;
    }

/**
 * Skip over fields you don't want to process.
 *
 * @param fields How many field you want to bypass reading. The newline counts as one field.
 *
 * @throws EOFException at end of file after all the fields have been read.
 * @throws IOException  Some problem reading the file, possibly malformed data.
 * @noinspection UnusedDeclaration
 */
public void skip( int fields ) throws IOException
    {
    if ( fields <= 0 )
        {
        return;
        }
    for ( int i = 0; i < fields; i++ )
        {
        // throw results away
        get();
        }
    }

/**
 * Skip over remaining fields on this line you don't want to process.
 * Do not call this after using getAllFieldsInLine.
 *
 * @throws EOFException at end of file after all the fields have been read.
 * @throws IOException  Some problem reading the file, possibly malformed data.
 */
public void skipToNextLine() throws IOException
    {
    if ( line == null )
        {
        getLineIfNeeded();
        }
    line = null;
    }

/**
 * Was the last field returned via get a comment (including a label comment)?
 * Also works after getAllFieldsInLine to tell if there was a comment at the end of that line.
 *
 * @return true if last field returned via get was a comment.
 */
public boolean wasComment()
    {
    return this.wasComment;
    }

/**
 * Was the last field returned via get a label ## comment?  Also works after getAllFieldsInLine to tell if there
 * was a comment at the end of that line.
 *
 * @return true if last field returned via get was a ## label  comment.
 */
public boolean wasLabelComment()
    {
    return this.wasLabelComment;
    }
} // end CSVReader class.
