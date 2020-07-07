package csv2html;

import static java.lang.System.err;
import java.io.IOException;

import org.junit.Test;

public class testCsv2html {

	@Test
	//test case 0
	public void test0() {
		String input = "test.csv";
		String output = "result0.html";
		String[] cssClasses = new String[]{"class1","class2","class3", "class4", "class5", "class6", "class7"};
		CSVToHTML converter = new CSVToHTML();
		try {
			converter.csv2html(input, output, "ASCII", cssClasses, true, true);
		} catch ( Exception e )
        {
        err.println();
        e.printStackTrace( err );
        err.println( "Failed to export " + input );
        err.println();
        }
	}
	
	@Test
	//test case 1
	public void test1() {
		String input = "test.csv";
		String output = "result1.html";
		String[] cssClasses = new String[]{"class1","class2","class3", "class4", "class5", "class6", "class7"};
		CSVToHTML converter = new CSVToHTML();
		try {
			converter.csv2html(input, output, null, cssClasses, false, false);
		} catch ( Exception e )
        {
        err.println();
        e.printStackTrace( err );
        err.println( "Failed to export " + input );
        err.println();
        }
	}
	
	
	@Test
	//test case 2
	public void test2() {
		String input = "test";
		String output = "result2.html";
		String[] cssClasses = new String[]{"class1","class2","class3", "class4", "class5", "class6", "class7"};
		CSVToHTML converter = new CSVToHTML();
		try {
			converter.csv2html(input, output, "ASCII", cssClasses, false, true);
		} catch ( Exception e )
        {
        err.println();
        e.printStackTrace( err );
        err.println( "Failed to export " + input );
        err.println();
        }
	}
	
	
	@Test
	//test case 3
	public void test3() {
		String input = "badpath.csv";
		String output = "result.html";
		String[] cssClasses = new String[]{"class1","class2","class3", "class4", "class5", "class6", "class7"};
		CSVToHTML converter = new CSVToHTML();
		try {
			converter.csv2html(input, output, null, cssClasses, true, false);
		} catch ( Exception e )
        {
        err.println();
        e.printStackTrace( err );
        err.println( "Failed to export " + input );
        err.println();
        }
	}
	
	@Test
	//test case 4
	public void test4() {
		String input = "test.csv";
		String output = "result4";
		String[] cssClasses = new String[]{"class1","class2","class3", "class4", "class5", "class6", "class7"};
		CSVToHTML converter = new CSVToHTML();
		try {
			converter.csv2html(input, output,null, cssClasses, true, true);
		} catch ( Exception e )
        {
        err.println();
        e.printStackTrace( err );
        err.println( "Failed to export " + input );
        err.println();
        }
	}
	
	@Test
	//test case 5
	public void test5() {
		String input = "test.csv";
		String output = "/bad/bad/badpath.html";
		String[] cssClasses = new String[]{"class1","class2","class3", "class4", "class5", "class6", "class7"};
		CSVToHTML converter = new CSVToHTML();
		try {
			converter.csv2html(input, output, "ASCII", cssClasses, false, false);
		} catch ( Exception e )
        {
        err.println();
        e.printStackTrace( err );
        err.println( "Failed to export " + input );
        err.println();
        }
	}
	
	@Test
	//test case 6
	public void test6() {
		String input = "test.csv";
		String output = "result6.html";
		String[] cssClasses = new String[]{"class1","class2","class3", "class4", "class5", "class6", "class7"};
		CSVToHTML converter = new CSVToHTML();
		try {
			converter.csv2html(input, output, null, cssClasses, true, true);
		} catch ( Exception e )
        {
        err.println();
        e.printStackTrace( err );
        err.println( "Failed to export " + input );
        err.println();
        }
	}
	
	@Test
	//test case 7
	public void test7() {
		String input = "test.csv";
		String output = "result7.html";
		String[] cssClasses = new String[]{"c"};
		CSVToHTML converter = new CSVToHTML();
		try {
			converter.csv2html(input, output, null, cssClasses, false, true);
		} catch ( Exception e )
        {
        err.println();
        e.printStackTrace( err );
        err.println( "Failed to export " + input );
        err.println();
        }
	}
	
	@Test
	//test case 8
	public void test8() {
		String input = "test.csv";
		String output = "result8.html";
		String[] cssClasses = null;
		CSVToHTML converter = new CSVToHTML();
		try {
			converter.csv2html(input, output, "ASCII", cssClasses, false, false);
		} catch ( Exception e )
        {
        err.println();
        e.printStackTrace( err );
        err.println( "Failed to export " + input );
        err.println();
        }
	}
	
	@Test
	//test case 9
	public void test9() {
		String input = "test.csv";
		String output = "result9.html";
		String[] cssClasses = new String[]{"class1","class2","class3", "class4", "class5", "class6", "class7"};
		CSVToHTML converter = new CSVToHTML();
		try {
			converter.csv2html(input, output, "ASCII", cssClasses, false, false);
		} catch ( Exception e )
        {
        err.println();
        e.printStackTrace( err );
        err.println( "Failed to export " + input );
        err.println();
        }
	}
	
	@Test
	//test case 10
	public void test10() {
		String input = "test.csv";
		String output = "result10.html";
		String[] cssClasses = new String[]{"class1","class2","class3", "class4", "class5", "class6", "class7"};
		CSVToHTML converter = new CSVToHTML();
		try {
			converter.csv2html(input, output, null, cssClasses, true, true);
		} catch ( Exception e )
        {
        err.println();
        e.printStackTrace( err );
        err.println( "Failed to export " + input );
        err.println();
        }
	}
	
	
	@Test
	//test case 11
	public void test11() {
		String input = "test.csv";
		String output = "result11.html";
		String[] cssClasses = new String[]{"class1","class2","class3", "class4", "class5", "class6", "class7"};
		CSVToHTML converter = new CSVToHTML();
		try {
			converter.csv2html(input, output, null, cssClasses, false, false);
		} catch ( Exception e )
        {
        err.println();
        e.printStackTrace( err );
        err.println( "Failed to export " + input );
        err.println();
        }
	}
	
	@Test
	//test case 12
	public void test12() {
		String input = "test.csv";
		String output = "result12.html";
		String[] cssClasses = new String[]{"class1","class2","class3", "class4", "class5", "class6", "class7"};
		CSVToHTML converter = new CSVToHTML();
		try {
			converter.csv2html(input, output, "ASCII", cssClasses, true, false);
		} catch ( Exception e )
        {
        err.println();
        e.printStackTrace( err );
        err.println( "Failed to export " + input );
        err.println();
        }
	}
	
	@Test
	//test case 13
	public void test13() {
		String input = "test.csv";
		String output = "result13.html";
		String[] cssClasses = new String[]{"class1","class2","class3", "class4", "class5", "class6", "class7"};
		CSVToHTML converter = new CSVToHTML();
		try {
			converter.csv2html(input, output, null, cssClasses, false, true);
		} catch ( Exception e )
        {
        err.println();
        e.printStackTrace( err );
        err.println( "Failed to export " + input );
        err.println();
        }
	}
	
	@Test
	//test case 14
	public void test14() {
		String input = "test.csv";
		String output = "result14.html";
		String[] cssClasses = new String[]{"class1","class2","class3", "class4", "class5", "class6", "class7"};
		CSVToHTML converter = new CSVToHTML();
		try {
			converter.csv2html(input, output, "bad", cssClasses, true, false);
		} catch ( Exception e )
        {
        err.println();
        e.printStackTrace( err );
        err.println( "Failed to export " + input );
        err.println();
        }
	}
}
