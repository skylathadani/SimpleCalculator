package csvreadwrite;

/**
 * enumeration of the classes of characters processed by the CSV finite state
 * automaton.
 *
 * @author Roedy Green, Canadian Mind Products
 * @version 1.0 2009-03-24 initial version
 * @since 2009-03-24
 */
public enum CSVCharCategory {
	COMMENT_START, ORDINARY, QUOTE, SEPARATOR, WHITESPACE
}
