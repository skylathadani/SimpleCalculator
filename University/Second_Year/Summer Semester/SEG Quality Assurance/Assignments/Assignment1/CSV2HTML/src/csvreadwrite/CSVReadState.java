package csvreadwrite;

/**
 * enumeration of the finite state automaton used to read CSV files.
 * <p/>
 * We don't put more logic here since enums are static, and we need separate
 * state for separate CSVReaders.
 *
 * @author Roedy Green, Canadian Mind Products
 * @version 3.4 2010-12-03 add CSVToSRS.
 * @since 2009-03-25
 */
public enum CSVReadState {
	/**
	 * parser: We have just hit a quote, might be doubled or might be last one.
	 */
	AFTER_END_QUOTE,
	/**
	 * parser: We are in the middle of an ordinary field, possible full of blanks.
	 */
	IN_PLAIN_FIELD,
	/**
	 * parser: e are in middle of field surrounded in quotes.
	 */
	IN_QUOTED_FIELD,
	/**
	 * parser : we don't yet know if there is another non-comment field on the line.
	 * In blanks prior to first field.
	 */
	SEEKING_FIELD,
	/**
	 * parser: We are in blanks before a field that we know is there, possibly
	 * empty, because we have seen the comma after the previous field.
	 */
	SEEKING_START,
	/**
	 * parser: We are in blanks after a quoted field looking for the separator
	 */
	SKIPPING_TAIL_AFTER_QUOTE
}
