package breakingtherules.tests.service;

import breakingtherules.service.CSVScrambler.CSVScramblerRunner;
import breakingtherules.tests.TestBase;
import breakingtherules.util.Utility;

@SuppressWarnings("javadoc")
public class CSVScramblerTest extends TestBase {

    public static void main(String[] args) {
	final String arguments = "-i input.csv -o output.csv -s 0 -d 1 -spo 2 -spr 3";
	args = Utility.breakToWords(arguments, ' ');

	final CSVScramblerRunner runner = new CSVScramblerRunner();
	runner.setArgs(args);
	runner.run();
    }

}
