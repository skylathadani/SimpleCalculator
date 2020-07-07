package utils;

import static java.lang.System.*;

/**
 * Simple convenience methods used often by CMP utilities.
 *
 * @author Roedy Green, Canadian Mind Products
 * @version 1.9 2016-05-21 move deleteAndRename to com.mindprod.hunkio.HunkIO
 * @since 2003-05-15
 */
public final class Misc {
	// declarations

	/**
	 * true if you want extra debugging output and test code
	 */
	private static final boolean DEBUGGING = false;
	// /declarations

	/**
	 * Misc contains only static methods.
	 */
	private Misc() {
	}
	
    /**
     * parse a string to produce a boolean, often from a SET variable or a boolean macro parameter o9 a csv file.
     *
     * @param s            one of "yes", "true", "Y", "T", "1" ,"+" for true
     *                     or null, "",  "no", "false", "N", "F", "0", "-" for false, case-insensitive.
     * @param defaultValue if the value is null, return this value
     *
     * @return whether the string represents true or false.
     * @throws java.lang.NumberFormatException if s is not an int.
     */
    public static boolean parseBoolean( final String s, final boolean defaultValue )
        {
        if ( ST.isEmpty( s ) )
            {
            return defaultValue;
            }
        else
            {
            final String cleaned = s.trim().toLowerCase();
            switch ( cleaned )
                {
                case "yes":
                case "true":
                case "y":
                case "t":
                case "1":
                case "+":
                    return true;
                case "no":
                case "false":
                case "n":
                case "f":
                case "0":
                case "-":
                    return false;
                default:
                    throw new NumberFormatException( "Boolean value " + cleaned + " must be one of true(\"yes\", \"true\", \"Y\", \"T\", \"1\", \"+\") "
                                                     + "or false:(\"no\", \"false\", \"N\", \"F\", \"0\", \"-\", \"\", null)" );
                }
            }
        }// /method
}
