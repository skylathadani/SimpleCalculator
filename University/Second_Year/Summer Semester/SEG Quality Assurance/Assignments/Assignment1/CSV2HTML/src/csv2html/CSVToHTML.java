package csv2html;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.text.DecimalFormat;

import csvreadwrite.CSVReader;
import utils.EIO;
import utils.FastCat;
import utils.ST;

public class CSVToHTML {

	private static DecimalFormat DF = new DecimalFormat("#,##0");

	/**
	 * Default encoding
	 */
	private static final String DEFAULT_ENCODING = "UTF-8";

	public CSVToHTML() {
	}

	private void generateHeader(final PrintWriter writer, final Charset encoding) {
		writer.print("<!DOCTYPE html>\n");
		writer.print("<html>\n");
		writer.print("<head>\n");
		writer.print("<meta charset=\"" +  encoding.displayName() + "\"> \n");
		writer.print("</head>\n");
		writer.print("<body>\n"); 
		writer.print("<table>\n");
	}
	
	private void generateFooter(PrintWriter writer) {
		writer.print("</table>\n");
		writer.print("</body>\n");
		writer.print("</html>\n");
	}
	
	private  void doconvert(final File fileBeingProcessed, final File outputFile, final Charset encoding,
			final String[] cssClasses, boolean completeHTML, boolean header) throws IOException {
		final CSVReader r = new CSVReader(EIO.getBufferedReader(fileBeingProcessed, 64 * 1024, encoding));
		final PrintWriter w = EIO.getPrintWriter(outputFile, 16 * 1024, encoding);
		if (completeHTML) generateHeader(w, encoding);
		try {
			boolean first = true;
			while (true) {
				final String[] fields = r.getAllFieldsInLine();
				final FastCat sb = new FastCat(fields.length * 5 + 4);
				if (0 < cssClasses.length && cssClasses[0].length() > 0) {
					sb.append("<tr class=\"");
					sb.append(cssClasses[0]);
					sb.append("\">");
				} else {
					sb.append("<tr>");
				}
				for (int i = 0; i < fields.length; i++) {
					final String field = fields[i];
					int j = i + 1;
					if (j < cssClasses.length && cssClasses[j].length() > 0) {
						if (first && header) 
							sb.append("<th class=\"");
						else sb.append("<td class=\"");
						sb.append(cssClasses[j]);
						sb.append("\">");
					} else {
						if (first && header) 
							sb.append("<th>");
						else sb.append("<td>");
					}
					if (ST.isNumeric(field)) {
						final long x = Long.parseLong(field);
						sb.append(DF.format(x));
					} else {
						sb.append(field);
					}
					if (first && header) sb.append("</th>");
					else sb.append("</td>");
				}
				sb.append("</tr>\n");
				first = false;
				w.print(sb.toString());
			}
		} catch (EOFException e) {
			r.close();
		}
		if (completeHTML) generateFooter(w);
		w.close();
	}


	/**
	 * Generates a HTML file containing a HTML table corresponding to a CSV file in input
	 * 
	 * @param infilename CSV input file name
	 * @param outfilename HTML output file name, can be null or empty for default
	 * @param encoding Charset name used for encoding. 
	 * See https://docs.oracle.com/javase/8/docs/api/java/nio/charset/Charset.html. 
	 * Default encoding is used if null or empty 
	 * @param cssClasses array of CSS class names to apply to rows and columns.
	 * @param completeHTML generates a complete HTML file if true
	 * only the table content is generated if false
	 * @param header generates a table heading for the first non commented row if true
	 * @throws IllegalArgumentException, IOException
	 */
	public void csv2html(String infilename, String outfilename, 
			final String encoding, String[] cssClasses, boolean completeHTML, boolean header)
			throws IllegalArgumentException, IOException {
		// check filename
		if (infilename == null || !infilename.endsWith(".csv")) {
			throw new IllegalArgumentException("Wrong Parameter: input filename " + infilename);
		}
		final File inputFile = new File(infilename);
		if (!inputFile.exists() || !inputFile.canRead()) {
			throw new IllegalArgumentException("Wrong Parameter: can not access file " + infilename);
		}
		// check encoding
		Charset charset = null;
		try {
			if (encoding == null || encoding.isEmpty()) 
				charset = Charset.forName(DEFAULT_ENCODING);
			else
				charset = Charset.forName(encoding);
		} catch (IllegalCharsetNameException e) {
			throw new IllegalArgumentException("Wrong Parameter: encoding");
		}
		// check output filename
		File outputFile = null;
		if (outfilename == null || outfilename.isEmpty()) {
			String htmlFilename = EIO.getCanOrAbsPath(inputFile);
			htmlFilename = htmlFilename.substring(0, htmlFilename.length() - 4) + ".html";
			outputFile = new File(htmlFilename);
		} else {
			outputFile = new File(outfilename);
		}
		// ensure cssClasses not null
		if (cssClasses == null)
			cssClasses = new String[0];
		// perform html generation
		doconvert(inputFile, outputFile, charset, cssClasses, completeHTML, header);

	}
}
