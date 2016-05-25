package breakingtherules.tests.services;

import java.util.List;

import breakingtherules.services.CSVScrambler.CSVScramblerRunner;
import breakingtherules.tests.TestBase;
import breakingtherules.utilities.Utility;

public class CSVScramblerTest extends TestBase {

    public static void main(String[] args) {

	String arguments = "-i input.csv -o output.csv -s 0 -d 1 -spo 2 -spr 3";
	List<String> argsList = Utility.breakToWords(arguments, " ");
	args = argsList.toArray(new String[argsList.size()]);

	CSVScramblerRunner runner = new CSVScramblerRunner();
	runner.setArgs(args);
	runner.run();
    }

}
