package breakingtherules.tests.dao.csv;

import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import breakingtherules.dao.csv.CSVHitsParser;
import breakingtherules.dao.csv.CSVParseException;
import breakingtherules.firewall.Destination;
import breakingtherules.firewall.Hit;
import breakingtherules.firewall.Service;
import breakingtherules.firewall.Source;
import breakingtherules.tests.TestBase;
import breakingtherules.tests.firewall.FirewallTestsUtility;

@SuppressWarnings("javadoc")
public class CSVParserTest extends TestBase {

    private static final Integer DUMMY = Integer.valueOf(0x4000 + rand.nextInt(0x4000));

    private static final String CSV_SUFFIX = ".csv";

    @Test
    @SuppressWarnings("unused")
    public void constructorTestDefault() {
	new CSVHitsParser(CSVHitsParser.DEFAULT_COLUMNS_TYPES);
    }

    @Test(expected = NullPointerException.class)
    @SuppressWarnings("unused")
    public void constructorTestNullType() {
	new CSVHitsParser(Arrays.asList(CSVHitsParser.DESTINATION, null, CSVHitsParser.SERVICE_PORT));
    }

    @Test(expected = NullPointerException.class)
    @SuppressWarnings("unused")
    public void constructorTestNullTypesList() {
	new CSVHitsParser(null);
    }

    @Test
    public void parseHitsTestAllAttributes() throws Exception {
	runTempFileTest(getCurrentMethodName(), CSV_SUFFIX, file -> {
	    try {
		final int numberOfLines = 10_000;

		final List<Hit> expected = new ArrayList<>(numberOfLines);

		try (final BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
		    for (int i = numberOfLines; i-- != 0;) {
			final String source = FirewallTestsUtility.getRandomIP().toString();
			final String destination = FirewallTestsUtility.getRandomIP().toString();
			final String serviceProtocol = Integer.toString(FirewallTestsUtility.getRandomProtocolCode());
			final String servicePort = Integer.toString(FirewallTestsUtility.getRandomPort());
			final String line = source + '\t' + destination + '\t' + serviceProtocol + '\t' + servicePort;

			expected.add(new Hit(
				Arrays.asList(Source.valueOf(source), Destination.valueOf(destination), Service.valueOf(
					Service.parseProtocolCode(serviceProtocol), Service.parsePort(servicePort)))));

			writer.write(line);
			writer.newLine();
		    }
		}

		final List<Integer> columnsTypes = Arrays.asList(CSVHitsParser.SOURCE, CSVHitsParser.DESTINATION,
			CSVHitsParser.SERVICE_PROTOCOL, CSVHitsParser.SERVICE_PORT);
		final Iterable<Hit> actual = CSVHitsParser.parseAllHits(columnsTypes, file.getAbsolutePath());

		assertEqualsAsSets(expected, actual);

	    } catch (final Exception e) {
		e.printStackTrace();
		fail(e.getMessage());
	    }
	});
    }

    @Test
    public void parseHitsTestWithoutDestination() throws Exception {
	runTempFileTest(getCurrentMethodName(), CSV_SUFFIX, file -> {
	    try {
		final int numberOfLines = rand.nextInt(10_000);

		final List<Hit> expected = new ArrayList<>(numberOfLines);

		try (final BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
		    for (int i = numberOfLines; i-- != 0;) {
			final String source = FirewallTestsUtility.getRandomIP().toString();
			final String destination = FirewallTestsUtility.getRandomIP().toString();
			final String serviceProtocol = Integer.toString(FirewallTestsUtility.getRandomProtocolCode());
			final String servicePort = Integer.toString(FirewallTestsUtility.getRandomPort());
			final String line = source + '\t' + destination + '\t' + serviceProtocol + '\t' + servicePort;

			expected.add(new Hit(Arrays.asList(Source.valueOf(source), Service
				.valueOf(Service.parseProtocolCode(serviceProtocol), Service.parsePort(servicePort)))));

			writer.write(line);
			writer.newLine();
		    }
		}

		final List<Integer> columnsTypes = Arrays.asList(CSVHitsParser.SOURCE, DUMMY,
			CSVHitsParser.SERVICE_PROTOCOL, CSVHitsParser.SERVICE_PORT);
		final Iterable<Hit> actual = CSVHitsParser.parseAllHits(columnsTypes, file.getAbsolutePath());

		assertEqualsAsSets(expected, actual);

	    } catch (final Exception e) {
		e.printStackTrace();
		fail(e.getMessage());
	    }
	});
    }

    @Test
    public void parseHitsTestWithoutServicePort() throws Exception {
	runTempFileTest(getCurrentMethodName(), CSV_SUFFIX, file -> {
	    try {
		final int numberOfLines = rand.nextInt(10_000);

		final List<Hit> expected = new ArrayList<>(numberOfLines);

		try (final BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
		    for (int i = numberOfLines; i-- != 0;) {
			final String source = FirewallTestsUtility.getRandomIP().toString();
			final String destination = FirewallTestsUtility.getRandomIP().toString();
			final String serviceProtocol = Integer.toString(FirewallTestsUtility.getRandomProtocolCode());
			final String servicePort = Integer.toString(FirewallTestsUtility.getRandomPort());
			final String line = source + '\t' + destination + '\t' + serviceProtocol + '\t' + servicePort;

			expected.add(new Hit(Arrays.asList(Source.valueOf(source), Destination.valueOf(destination))));

			writer.write(line);
			writer.newLine();
		    }
		}

		final List<Integer> columnsTypes = Arrays.asList(CSVHitsParser.SOURCE, CSVHitsParser.DESTINATION,
			CSVHitsParser.SERVICE_PROTOCOL);
		final Iterable<Hit> actual = CSVHitsParser.parseAllHits(columnsTypes, file.getAbsolutePath());

		assertEquals(expected, actual);

	    } catch (final Exception e) {
		e.printStackTrace();
		fail(e.getMessage());
	    }
	});
    }

    @Test
    public void parseHitsTestWithoutServiveProtocol() throws Exception {
	runTempFileTest(getCurrentMethodName(), CSV_SUFFIX, file -> {
	    try {
		final int numberOfLines = rand.nextInt(10_000);

		final List<Hit> expected = new ArrayList<>(numberOfLines);

		try (final BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
		    for (int i = numberOfLines; i-- != 0;) {
			final String source = FirewallTestsUtility.getRandomIP().toString();
			final String destination = FirewallTestsUtility.getRandomIP().toString();
			final String serviceProtocol = Integer.toString(FirewallTestsUtility.getRandomProtocolCode());
			final String servicePort = Integer.toString(FirewallTestsUtility.getRandomPort());
			final String line = source + '\t' + destination + '\t' + serviceProtocol + '\t' + servicePort;

			expected.add(new Hit(Arrays.asList(Source.valueOf(source), Destination.valueOf(destination))));

			writer.write(line);
			writer.newLine();
		    }
		}

		final List<Integer> columnsTypes = Arrays.asList(CSVHitsParser.SOURCE, CSVHitsParser.DESTINATION, DUMMY,
			CSVHitsParser.SERVICE_PORT);
		final Iterable<Hit> actual = CSVHitsParser.parseAllHits(columnsTypes, file.getAbsolutePath());

		assertEquals(expected, actual);

	    } catch (final Exception e) {
		e.printStackTrace();
		fail(e.getMessage());
	    }
	});
    }

    @Test
    public void parseHitsTestWithoutSource() throws Exception {
	runTempFileTest(getCurrentMethodName(), CSV_SUFFIX, file -> {
	    try {
		final int numberOfLines = rand.nextInt(10_000);

		final List<Hit> expected = new ArrayList<>(numberOfLines);

		try (final BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
		    for (int i = numberOfLines; i-- != 0;) {
			final String source = FirewallTestsUtility.getRandomIP().toString();
			final String destination = FirewallTestsUtility.getRandomIP().toString();
			final String serviceProtocol = Integer.toString(FirewallTestsUtility.getRandomProtocolCode());
			final String servicePort = Integer.toString(FirewallTestsUtility.getRandomPort());
			final String line = source + '\t' + destination + '\t' + serviceProtocol + '\t' + servicePort;

			expected.add(new Hit(Arrays.asList(Destination.valueOf(destination), Service
				.valueOf(Service.parseProtocolCode(serviceProtocol), Service.parsePort(servicePort)))));

			writer.write(line);
			writer.newLine();
		    }
		}

		final List<Integer> columnsTypes = Arrays.asList(DUMMY, CSVHitsParser.DESTINATION,
			CSVHitsParser.SERVICE_PROTOCOL, CSVHitsParser.SERVICE_PORT);
		final Iterable<Hit> actual = CSVHitsParser.parseAllHits(columnsTypes, file.getAbsolutePath());

		assertEqualsAsSets(expected, actual);

	    } catch (final Exception e) {
		e.printStackTrace();
		fail(e.getMessage());
	    }
	});
    }

    @Test
    public void parseHitTestAllAttributes() throws CSVParseException {
	final List<Integer> columnsTypes = Arrays.asList(CSVHitsParser.SOURCE, CSVHitsParser.DESTINATION,
		CSVHitsParser.SERVICE_PROTOCOL, CSVHitsParser.SERVICE_PORT);
	final CSVHitsParser parser = new CSVHitsParser(columnsTypes);
	final int repeat = 25;

	for (int i = repeat; i-- != 0;) {
	    final String source = FirewallTestsUtility.getRandomIP().toString();
	    final String destination = FirewallTestsUtility.getRandomIP().toString();
	    final String serviceProtocol = Integer.toString(FirewallTestsUtility.getRandomProtocolCode());
	    final String servicePort = Integer.toString(FirewallTestsUtility.getRandomPort());
	    final String line = source + '\t' + destination + '\t' + serviceProtocol + '\t' + servicePort;

	    final Hit expected = new Hit(Arrays.asList(Source.valueOf(source), Destination.valueOf(destination),
		    Service.valueOf(Service.parseProtocolCode(serviceProtocol), Service.parsePort(servicePort))));
	    final Hit actual = parser.parseHit(line);

	    assertEquals(expected, actual);
	}
    }

    @Test
    public void parseHitTestWithoutDestination() throws CSVParseException {
	final List<Integer> columnsTypes = Arrays.asList(CSVHitsParser.SOURCE, DUMMY, CSVHitsParser.SERVICE_PROTOCOL,
		CSVHitsParser.SERVICE_PORT);
	final CSVHitsParser parser = new CSVHitsParser(columnsTypes);
	final int repeat = 25;

	for (int i = repeat; i-- != 0;) {
	    final String source = FirewallTestsUtility.getRandomIP().toString();
	    final String destination = FirewallTestsUtility.getRandomIP().toString();
	    final String serviceProtocol = Integer.toString(FirewallTestsUtility.getRandomProtocolCode());
	    final String servicePort = Integer.toString(FirewallTestsUtility.getRandomPort());
	    final String line = source + '\t' + destination + '\t' + serviceProtocol + '\t' + servicePort;

	    final Hit expected = new Hit(Arrays.asList(Source.valueOf(source),
		    Service.valueOf(Service.parseProtocolCode(serviceProtocol), Service.parsePort(servicePort))));
	    final Hit actual = parser.parseHit(line);

	    assertEquals(expected, actual);
	}
    }

    @Test
    public void parseHitTestWithoutServicePort() throws CSVParseException {
	final List<Integer> columnsTypes = Arrays.asList(CSVHitsParser.SOURCE, CSVHitsParser.DESTINATION,
		CSVHitsParser.SERVICE_PROTOCOL);
	final CSVHitsParser parser = new CSVHitsParser(columnsTypes);
	final int repeat = 25;

	for (int i = repeat; i-- != 0;) {
	    final String source = FirewallTestsUtility.getRandomIP().toString();
	    final String destination = FirewallTestsUtility.getRandomIP().toString();
	    final String serviceProtocol = Integer.toString(FirewallTestsUtility.getRandomProtocolCode());
	    final String servicePort = Integer.toString(FirewallTestsUtility.getRandomPort());
	    final String line = source + '\t' + destination + '\t' + serviceProtocol + '\t' + servicePort;

	    final Hit expected = new Hit(Arrays.asList(Source.valueOf(source), Destination.valueOf(destination)));
	    final Hit actual = parser.parseHit(line);

	    assertEquals(expected, actual);
	}
    }

    @Test
    public void parseHitTestWithoutServiceProtocol() throws CSVParseException {
	final List<Integer> columnsTypes = Arrays.asList(CSVHitsParser.SOURCE, CSVHitsParser.DESTINATION, DUMMY,
		CSVHitsParser.SERVICE_PORT);
	final CSVHitsParser parser = new CSVHitsParser(columnsTypes);
	final int repeat = 25;

	for (int i = repeat; i-- != 0;) {
	    final String source = FirewallTestsUtility.getRandomIP().toString();
	    final String destination = FirewallTestsUtility.getRandomIP().toString();
	    final String serviceProtocol = Integer.toString(FirewallTestsUtility.getRandomProtocolCode());
	    final String servicePort = Integer.toString(FirewallTestsUtility.getRandomPort());
	    final String line = source + '\t' + destination + '\t' + serviceProtocol + '\t' + servicePort;

	    final Hit expected = new Hit(Arrays.asList(Source.valueOf(source), Destination.valueOf(destination)));
	    final Hit actual = parser.parseHit(line);

	    assertEquals(expected, actual);
	}
    }

    @Test
    public void parseHitTestWithoutSource() throws CSVParseException {
	final List<Integer> columnsTypes = Arrays.asList(DUMMY, CSVHitsParser.DESTINATION,
		CSVHitsParser.SERVICE_PROTOCOL, CSVHitsParser.SERVICE_PORT);
	final CSVHitsParser parser = new CSVHitsParser(columnsTypes);

	final int repeat = 25;

	for (int i = repeat; i-- != 0;) {
	    final String source = FirewallTestsUtility.getRandomIP().toString();
	    final String destination = FirewallTestsUtility.getRandomIP().toString();
	    final String serviceProtocol = Integer.toString(FirewallTestsUtility.getRandomProtocolCode());
	    final String servicePort = Integer.toString(FirewallTestsUtility.getRandomPort());
	    final String line = source + '\t' + destination + '\t' + serviceProtocol + '\t' + servicePort;

	    final Hit expected = new Hit(Arrays.asList(Destination.valueOf(destination),
		    Service.valueOf(Service.parseProtocolCode(serviceProtocol), Service.parsePort(servicePort))));
	    final Hit actual = parser.parseHit(line);

	    assertEquals(expected, actual);
	}
    }

    @Test
    public void toCSVMultipleHitsTestAllAttributes() throws Exception {
	runTempFileTest(getCurrentMethodName(), CSV_SUFFIX, file -> {
	    try {
		final int numberOfLines = rand.nextInt(10_000);

		final List<String> expected = new ArrayList<>(numberOfLines);
		final List<Hit> hits = new ArrayList<>(numberOfLines);

		for (int i = numberOfLines; i-- != 0;) {
		    final String source = FirewallTestsUtility.getRandomIP().toString();
		    final String destination = FirewallTestsUtility.getRandomIP().toString();
		    final String serviceProtocol = Integer.toString(FirewallTestsUtility.getRandomProtocolCode());
		    final String servicePort = Integer.toString(FirewallTestsUtility.getRandomPort());
		    final String line = source + '\t' + destination + '\t' + serviceProtocol + '\t' + servicePort;
		    expected.add(line);

		    hits.add(new Hit(Arrays.asList(Source.valueOf(source), Destination.valueOf(destination), Service
			    .valueOf(Service.parseProtocolCode(serviceProtocol), Service.parsePort(servicePort)))));
		}

		final List<Integer> columnsTypes = Arrays.asList(CSVHitsParser.SOURCE, CSVHitsParser.DESTINATION,
			CSVHitsParser.SERVICE_PROTOCOL, CSVHitsParser.SERVICE_PORT);
		CSVHitsParser.toCSV(columnsTypes, hits, file.getAbsolutePath());

		final List<String> actual = new ArrayList<>(numberOfLines);
		try (final BufferedReader reader = new BufferedReader(new FileReader(file))) {
		    for (String line; (line = reader.readLine()) != null;)
			actual.add(line);
		}

		assertEquals(expected, actual);

	    } catch (final Exception e) {
		e.printStackTrace();
		fail(e.getMessage());
	    }
	});
    }

    @Test
    public void toCSVMultipleHitsTestWithoutDestination() throws Exception {
	runTempFileTest(getCurrentMethodName(), CSV_SUFFIX, file -> {
	    try {
		final int numberOfLines = rand.nextInt(10_000);

		final List<String> expected = new ArrayList<>(numberOfLines);
		final List<Hit> hits = new ArrayList<>(numberOfLines);

		for (int i = numberOfLines; i-- != 0;) {
		    final String source = FirewallTestsUtility.getRandomIP().toString();
		    final String destination = FirewallTestsUtility.getRandomIP().toString();
		    final String serviceProtocol = Integer.toString(FirewallTestsUtility.getRandomProtocolCode());
		    final String servicePort = Integer.toString(FirewallTestsUtility.getRandomPort());
		    final String line = source + '\t' + serviceProtocol + '\t' + servicePort;
		    expected.add(line);

		    hits.add(new Hit(Arrays.asList(Source.valueOf(source), Destination.valueOf(destination), Service
			    .valueOf(Service.parseProtocolCode(serviceProtocol), Service.parsePort(servicePort)))));
		}

		final List<Integer> columnsTypes = Arrays.asList(CSVHitsParser.SOURCE, CSVHitsParser.SERVICE_PROTOCOL,
			CSVHitsParser.SERVICE_PORT);
		CSVHitsParser.toCSV(columnsTypes, hits, file.getAbsolutePath());

		final List<String> actual = new ArrayList<>(numberOfLines);
		try (final BufferedReader reader = new BufferedReader(new FileReader(file))) {
		    for (String line; (line = reader.readLine()) != null;)
			actual.add(line);
		}

		assertEquals(expected, actual);

	    } catch (final Exception e) {
		e.printStackTrace();
		fail(e.getMessage());
	    }
	});
    }

    @Test
    public void toCSVMultipleHitsTestWithoutServicePort() throws Exception {
	runTempFileTest(getCurrentMethodName(), CSV_SUFFIX, file -> {
	    try {
		final int numberOfLines = rand.nextInt(10_000);

		final List<String> expected = new ArrayList<>(numberOfLines);
		final List<Hit> hits = new ArrayList<>(numberOfLines);

		for (int i = numberOfLines; i-- != 0;) {
		    final String source = FirewallTestsUtility.getRandomIP().toString();
		    final String destination = FirewallTestsUtility.getRandomIP().toString();
		    final String serviceProtocol = Integer.toString(FirewallTestsUtility.getRandomProtocolCode());
		    final String servicePort = Integer.toString(FirewallTestsUtility.getRandomPort());
		    final String line = source + '\t' + destination + '\t' + serviceProtocol;
		    expected.add(line);

		    hits.add(new Hit(Arrays.asList(Source.valueOf(source), Destination.valueOf(destination), Service
			    .valueOf(Service.parseProtocolCode(serviceProtocol), Service.parsePort(servicePort)))));
		}

		final List<Integer> columnsTypes = Arrays.asList(CSVHitsParser.SOURCE, CSVHitsParser.DESTINATION,
			CSVHitsParser.SERVICE_PROTOCOL);
		CSVHitsParser.toCSV(columnsTypes, hits, file.getAbsolutePath());

		final List<String> actual = new ArrayList<>(numberOfLines);
		try (final BufferedReader reader = new BufferedReader(new FileReader(file))) {
		    for (String line; (line = reader.readLine()) != null;)
			actual.add(line);
		}

		assertEquals(expected, actual);

	    } catch (final Exception e) {
		e.printStackTrace();
		fail(e.getMessage());
	    }
	});
    }

    @Test
    public void toCSVMultipleHitsTestWithoutServiceProtocol() throws Exception {
	runTempFileTest(getCurrentMethodName(), CSV_SUFFIX, file -> {
	    try {
		final int numberOfLines = rand.nextInt(10_000);

		final List<String> expected = new ArrayList<>(numberOfLines);
		final List<Hit> hits = new ArrayList<>(numberOfLines);

		for (int i = numberOfLines; i-- != 0;) {
		    final String source = FirewallTestsUtility.getRandomIP().toString();
		    final String destination = FirewallTestsUtility.getRandomIP().toString();
		    final String serviceProtocol = Integer.toString(FirewallTestsUtility.getRandomProtocolCode());
		    final String servicePort = Integer.toString(FirewallTestsUtility.getRandomPort());
		    final String line = source + '\t' + destination + '\t' + servicePort;
		    expected.add(line);

		    hits.add(new Hit(Arrays.asList(Source.valueOf(source), Destination.valueOf(destination), Service
			    .valueOf(Service.parseProtocolCode(serviceProtocol), Service.parsePort(servicePort)))));
		}

		final List<Integer> columnsTypes = Arrays.asList(CSVHitsParser.SOURCE, CSVHitsParser.DESTINATION,
			CSVHitsParser.SERVICE_PORT);
		CSVHitsParser.toCSV(columnsTypes, hits, file.getAbsolutePath());

		final List<String> actual = new ArrayList<>(numberOfLines);
		try (final BufferedReader reader = new BufferedReader(new FileReader(file))) {
		    for (String line; (line = reader.readLine()) != null;)
			actual.add(line);
		}

		assertEquals(expected, actual);

	    } catch (final Exception e) {
		e.printStackTrace();
		fail(e.getMessage());
	    }
	});
    }

    @Test
    public void toCSVMultipleHitsTestWithoutSource() throws Exception {
	runTempFileTest(getCurrentMethodName(), CSV_SUFFIX, file -> {
	    try {
		final int numberOfLines = rand.nextInt(10_000);

		final List<String> expected = new ArrayList<>(numberOfLines);
		final List<Hit> hits = new ArrayList<>(numberOfLines);

		for (int i = numberOfLines; i-- != 0;) {
		    final String source = FirewallTestsUtility.getRandomIP().toString();
		    final String destination = FirewallTestsUtility.getRandomIP().toString();
		    final String serviceProtocol = Integer.toString(FirewallTestsUtility.getRandomProtocolCode());
		    final String servicePort = Integer.toString(FirewallTestsUtility.getRandomPort());
		    final String line = destination + '\t' + serviceProtocol + '\t' + servicePort;
		    expected.add(line);

		    hits.add(new Hit(Arrays.asList(Source.valueOf(source), Destination.valueOf(destination), Service
			    .valueOf(Service.parseProtocolCode(serviceProtocol), Service.parsePort(servicePort)))));
		}

		final List<Integer> columnsTypes = Arrays.asList(CSVHitsParser.DESTINATION,
			CSVHitsParser.SERVICE_PROTOCOL, CSVHitsParser.SERVICE_PORT);
		CSVHitsParser.toCSV(columnsTypes, hits, file.getAbsolutePath());

		final List<String> actual = new ArrayList<>(numberOfLines);
		try (final BufferedReader reader = new BufferedReader(new FileReader(file))) {
		    for (String line; (line = reader.readLine()) != null;)
			actual.add(line);
		}

		assertEquals(expected, actual);

	    } catch (final Exception e) {
		e.printStackTrace();
		fail(e.getMessage());
	    }
	});
    }

    @Test
    public void toCSVSingleHitTestAllAttributes() throws CSVParseException {
	final List<Integer> columnsTypes = Arrays.asList(CSVHitsParser.SOURCE, CSVHitsParser.DESTINATION,
		CSVHitsParser.SERVICE_PROTOCOL, CSVHitsParser.SERVICE_PORT);
	final CSVHitsParser parser = new CSVHitsParser(columnsTypes);
	final int repeat = 25;

	for (int i = repeat; i-- != 0;) {
	    final String source = FirewallTestsUtility.getRandomIP().toString();
	    final String destination = FirewallTestsUtility.getRandomIP().toString();
	    final String serviceProtocol = Integer.toString(FirewallTestsUtility.getRandomProtocolCode());
	    final String servicePort = Integer.toString(FirewallTestsUtility.getRandomPort());
	    final String expected = source + '\t' + destination + '\t' + serviceProtocol + '\t' + servicePort;

	    final Hit hit = new Hit(Arrays.asList(Source.valueOf(source), Destination.valueOf(destination),
		    Service.valueOf(Service.parseProtocolCode(serviceProtocol), Service.parsePort(servicePort))));
	    final String actual = parser.toCSV(hit);

	    assertEquals(expected, actual);
	}
    }

    @Test
    public void toCSVSingleHitTestWithoutDestination() throws CSVParseException {
	final List<Integer> columnsTypes = Arrays.asList(CSVHitsParser.SOURCE, CSVHitsParser.SERVICE_PROTOCOL,
		CSVHitsParser.SERVICE_PORT);
	final CSVHitsParser parser = new CSVHitsParser(columnsTypes);
	final int repeat = 25;

	for (int i = repeat; i-- != 0;) {
	    final String source = FirewallTestsUtility.getRandomIP().toString();
	    final String destination = FirewallTestsUtility.getRandomIP().toString();
	    final String serviceProtocol = Integer.toString(FirewallTestsUtility.getRandomProtocolCode());
	    final String servicePort = Integer.toString(FirewallTestsUtility.getRandomPort());
	    final String expected = source + '\t' + serviceProtocol + '\t' + servicePort;

	    final Hit hit = new Hit(Arrays.asList(Source.valueOf(source), Destination.valueOf(destination),
		    Service.valueOf(Service.parseProtocolCode(serviceProtocol), Service.parsePort(servicePort))));
	    final String actual = parser.toCSV(hit);

	    assertEquals(expected, actual);
	}
    }

    @Test
    public void toCSVSingleHitTestWithoutServicePort() throws CSVParseException {
	final List<Integer> columnsTypes = Arrays.asList(CSVHitsParser.SOURCE, CSVHitsParser.DESTINATION,
		CSVHitsParser.SERVICE_PROTOCOL);
	final CSVHitsParser parser = new CSVHitsParser(columnsTypes);
	final int repeat = 25;

	for (int i = repeat; i-- != 0;) {
	    final String source = FirewallTestsUtility.getRandomIP().toString();
	    final String destination = FirewallTestsUtility.getRandomIP().toString();
	    final String serviceProtocol = Integer.toString(FirewallTestsUtility.getRandomProtocolCode());
	    final String servicePort = Integer.toString(FirewallTestsUtility.getRandomPort());
	    final String expected = source + '\t' + destination + '\t' + serviceProtocol;

	    final Hit hit = new Hit(Arrays.asList(Source.valueOf(source), Destination.valueOf(destination),
		    Service.valueOf(Service.parseProtocolCode(serviceProtocol), Service.parsePort(servicePort))));
	    final String actual = parser.toCSV(hit);

	    assertEquals(expected, actual);
	}
    }

    @Test
    public void toCSVSingleHitTestWithoutServiceProtocol() throws CSVParseException {
	final List<Integer> columnsTypes = Arrays.asList(CSVHitsParser.SOURCE, CSVHitsParser.DESTINATION,
		CSVHitsParser.SERVICE_PORT);
	final CSVHitsParser parser = new CSVHitsParser(columnsTypes);
	final int repeat = 25;

	for (int i = repeat; i-- != 0;) {
	    final String source = FirewallTestsUtility.getRandomIP().toString();
	    final String destination = FirewallTestsUtility.getRandomIP().toString();
	    final String serviceProtocol = Integer.toString(FirewallTestsUtility.getRandomProtocolCode());
	    final String servicePort = Integer.toString(FirewallTestsUtility.getRandomPort());
	    final String expected = source + '\t' + destination + '\t' + servicePort;

	    final Hit hit = new Hit(Arrays.asList(Source.valueOf(source), Destination.valueOf(destination),
		    Service.valueOf(Service.parseProtocolCode(serviceProtocol), Service.parsePort(servicePort))));
	    final String actual = parser.toCSV(hit);

	    assertEquals(expected, actual);
	}
    }

    @Test
    public void toCSVSingleHitTestWithoutSource() throws CSVParseException {
	final List<Integer> columnsTypes = Arrays.asList(CSVHitsParser.DESTINATION, CSVHitsParser.SERVICE_PROTOCOL,
		CSVHitsParser.SERVICE_PORT);
	final CSVHitsParser parser = new CSVHitsParser(columnsTypes);
	final int repeat = 25;

	for (int i = repeat; i-- != 0;) {
	    final String source = FirewallTestsUtility.getRandomIP().toString();
	    final String destination = FirewallTestsUtility.getRandomIP().toString();
	    final String serviceProtocol = Integer.toString(FirewallTestsUtility.getRandomProtocolCode());
	    final String servicePort = Integer.toString(FirewallTestsUtility.getRandomPort());
	    final String expected = destination + '\t' + serviceProtocol + '\t' + servicePort;

	    final Hit hit = new Hit(Arrays.asList(Source.valueOf(source), Destination.valueOf(destination),
		    Service.valueOf(Service.parseProtocolCode(serviceProtocol), Service.parsePort(servicePort))));
	    final String actual = parser.toCSV(hit);

	    assertEquals(expected, actual);
	}
    }

}
