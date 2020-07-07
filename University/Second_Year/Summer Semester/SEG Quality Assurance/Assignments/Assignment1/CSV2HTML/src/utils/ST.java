package utils;

import java.util.BitSet;

/**
 * Miscellaneous static methods for dealing with Strings in JDK 1.8+.
 * <p/>
 *
 * @author Roedy Green, Canadian Mind Products
 * @version 3.1 2015-11-02 add last and tail methods
 * @noinspection WeakerAccess
 * @since 2003-05-15
 */
public class ST {
	// declarations

	/**
	 * true if you want extra debugging output and test code
	 */
	private static final boolean DEBUGGING = false;

	/**
	 * used to efficiently generate Strings of spaces of varying length
	 */
	private static final String SOMESPACES = "                                                                      ";

	/**
	 * track which chars in range 0..017F are vowels. Lookup table takes only 48
	 * bytes.
	 */
	private static final BitSet vt = new BitSet(0x0180);

	// /declarations
	static {
		// initialize vt Vowel Table
		vt.set('A');
		vt.set('E');
		vt.set('I');
		vt.set('O');
		vt.set('U');
		vt.set('a');
		vt.set('e');
		vt.set('i');
		vt.set('o');
		vt.set('u');
		vt.set('\u00c0', '\u00c6');
		vt.set('\u00c8', '\u00cf');
		vt.set('\u00d2', '\u00d6');
		vt.set('\u00d8', '\u00dc');
		vt.set('\u00e0', '\u00e6');
		vt.set('\u00e8', '\u00ef');
		vt.set('\u00f2', '\u00f6');
		vt.set('\u00f8', '\u00fc');
		vt.set('\u0100', '\u0105');
		vt.set('\u0112', '\u011b');
		vt.set('\u0128', '\u012f');
		vt.set('\u0130');
		vt.set('\u0132', '\u0133');
		vt.set('\u014c', '\u014f');
		vt.set('\u0150', '\u0153');
		vt.set('\u0168', '\u016f');
		vt.set('\u0170', '\u0173');
	}

	/**
	 * Dummy constructor ST contains only static methods.
	 */
	protected ST() {
	}

	/**
	 * Is this string empty? In Java 1.6 + isEmpty is build in. Sun's version being
	 * an instance method cannot test for null.
	 *
	 * @param s String to be tested for emptiness.
	 *
	 * @return true if the string is null or equal to the "" null string. or just
	 *         blanks
	 * @see Misc#nullToEmpty(String)
	 * @see String#isEmpty()
	 */
	public static boolean isEmpty(String s) {
		return (s == null) || s.trim().length() == 0;
	}// /method

	/**
	 * Count how many times a String occurs on a page.
	 *
	 * @param page    big String to look in.
	 * @param lookFor small String to look for and count instances.
	 *
	 * @return number of times the String appears non-overlapping.
	 */
	public static int countInstances(String page, String lookFor) {
		int count = 0;
		for (int start = 0; (start = page.indexOf(lookFor, start)) >= 0; start += lookFor.length()) {
			count++;
		}
		return count;
	}// /method

	/**
	 * Count how many times a char occurs in a String.
	 *
	 * @param page    big String to look in.
	 * @param lookFor char to lookfor count instances.
	 *
	 * @return number of times the char appears.
	 */
	public static int countInstances(String page, char lookFor) {
		int count = 0;
		for (int i = 0; i < page.length(); i++) {
			if (page.charAt(i) == lookFor) {
				count++;
			}
		}
		return count;
	}// /method

	/**
	 * Convert an integer to a String, with left zeroes.
	 *
	 * @param i   the integer to be converted
	 * @param len the length of the resulting string. Warning. It will chop the
	 *            result on the left if it is too long.
	 *
	 * @return String representation of the int e.g. 007
	 * @see #leftPad
	 */
	public static String toLZ(int i, int len) {
		// Since String is final, we could not add this method there.
		String s = Integer.toString(i);
		if (s.length() > len) {/* return rightmost len chars */
			return s.substring(s.length() - len);
		} else if (s.length() < len)
		// pad on left with zeros
		{
			return "000000000000000000000000000000".substring(0, len - s.length()) + s;
		} else {
			return s;
		}
	}// /method

	/**
	 * is a string numeric, only formed of 0..9? No signs, commas, E
	 *
	 * @param s string
	 *
	 * @return true if numeric, false if has other chars or is empty.
	 */
	public static boolean isNumeric(String s) {
		if (ST.isEmpty(s)) {
			return false;
		}
		for (int i = 0; i < s.length(); i++) {
			final char c = s.charAt(i);
			if ('0' <= c && c <= '9') {
				continue;
			} else {
				return false;
			}
		}
		return true;
	} // method// /method

}
