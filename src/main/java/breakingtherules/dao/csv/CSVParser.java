package breakingtherules.dao.csv;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import breakingtherules.dao.DaoUtilities;
import breakingtherules.firewall.Attribute;
import breakingtherules.firewall.Destination;
import breakingtherules.firewall.Filter;
import breakingtherules.firewall.Hit;
import breakingtherules.firewall.Rule;
import breakingtherules.firewall.Service;
import breakingtherules.firewall.Source;
import breakingtherules.utilities.Utility;

/**
 * The CSVParser used to read and write CSV file to and from hits.
 * <p>
 * 
 * @author Barak Ugav
 * @author Yishai Gronich
 * 
 */
public class CSVParser {

    /**
     * The column index of source attribute in the CSV file or -1 if doesn't
     * exist.
     */
    private final int sourceIndex;

    /**
     * The column index of destination attribute in the CSV file or -1 if
     * doesn't exist.
     */
    private final int destinationIndex;

    /**
     * The column index of service protocol in the CSV file or -1 if doesn't
     * exist.
     */
    private final int serviceProtocolIndex;

    /**
     * The column index of service port in the CSV file or -1 if doesn't exist.
     */
    private final int servicePortIndex;

    /**
     * The types of each column in the CSV file.
     */
    private final List<Integer> m_columnsTypes;

    /**
     * The symbol's value for source attribute column.
     */
    private static final int SOURCE_VAL = 0;

    /**
     * The symbol's value for destination attribute column.
     */
    private static final int DESTINATION_VAL = 1;

    /**
     * The symbol's value for service protocol column.
     */
    private static final int SERVICE_PROTOCOL_VAL = 2;

    /**
     * The symbol's value for service port column.
     */
    private static final int SERVICE_PORT_VAL = 3;

    /**
     * The symbol for source attribute column.
     */
    public static final Integer SOURCE = Integer.valueOf(SOURCE_VAL);

    /**
     * The symbol for destination attribute column.
     */
    public static final Integer DESTINATION = Integer.valueOf(DESTINATION_VAL);

    /**
     * The symbol for service protocol column.
     */
    public static final Integer SERVICE_PROTOCOL = Integer.valueOf(SERVICE_PROTOCOL_VAL);

    /**
     * The symbol for service port column.
     */
    public static final Integer SERVICE_PORT = Integer.valueOf(SERVICE_PORT_VAL);

    /**
     * Default columns types.
     */
    public static final List<Integer> DEFAULT_COLUMNS_TYPES;

    static {
	final List<Integer> list = new ArrayList<>();
	list.add(SOURCE);
	list.add(DESTINATION);
	list.add(SERVICE_PORT);
	list.add(SERVICE_PROTOCOL);
	DEFAULT_COLUMNS_TYPES = Collections.unmodifiableList(list);
    }

    /**
     * Construct new CSVParser.
     * <p>
     * Initialize the parser with columns order.
     * 
     * @param columnsTypes
     *            type of each column in the CSV file.
     * @throws NullPointerException
     *             if the columns types list is null.
     */
    public CSVParser(final List<Integer> columnsTypes) {
	// Create unmodifiable clone of the input list
	m_columnsTypes = Collections.unmodifiableList(Utility.newArrayList(columnsTypes));

	sourceIndex = columnsTypes.indexOf(SOURCE);
	destinationIndex = columnsTypes.indexOf(DESTINATION);
	serviceProtocolIndex = columnsTypes.indexOf(SERVICE_PROTOCOL);
	servicePortIndex = columnsTypes.indexOf(SERVICE_PORT);
    }

    /**
     * Parse a hit from a line.
     * <p>
     * 
     * @param line
     *            string line.
     * @return hit parsed from the line.
     * @throws CSVParseException
     *             if the line is invalid.
     * @throws NullPointerException
     *             if the line is null.
     */
    public Hit fromCSV(final String line) throws CSVParseException {
	final List<String> words = Utility.breakToWords(line);
	final List<Attribute> attributes = new ArrayList<>();

	try {
	    if (sourceIndex >= 0) {
		final String source = words.get(sourceIndex);
		attributes.add(fromCSVSource(source));
	    }
	    if (destinationIndex >= 0) {
		final String destination = words.get(destinationIndex);
		attributes.add(fromCSVDestination(destination));
	    }
	    if (serviceProtocolIndex >= 0 && servicePortIndex >= 0) {
		final String protocol = words.get(serviceProtocolIndex);
		final String port = words.get(servicePortIndex);
		attributes.add(fromCSVService(port, protocol));
	    }
	} catch (IndexOutOfBoundsException e) {
	    throw new CSVParseException("hit line didn't have enough attributes", e);
	}

	return new Hit(attributes);
    }

    /**
     * Parse hit to CSV string line.
     * <p>
     * 
     * @param hit
     *            parsed hit.
     * @return CSV string line representation of the hit.
     * @throws CSVParseException
     *             if the hit lack one of the attributes, or any other errors
     *             occurs.
     * @throws NullPointerException
     *             if the hit is null.
     */
    public String toCSV(final Hit hit) throws CSVParseException {
	final StringBuilder builder = new StringBuilder();
	for (final int colomnsType : m_columnsTypes) {
	    String colomnValue = "";
	    switch (colomnsType) {
	    case SOURCE_VAL:
		final Source source = (Source) hit.getAttribute(Attribute.SOURCE_TYPE_ID);
		colomnValue = toCSVSource(source);
		break;
	    case DESTINATION_VAL:
		final Destination destination = (Destination) hit.getAttribute(Attribute.DESTINATION_TYPE_ID);
		colomnValue = toCSVDestination(destination);
		break;
	    case SERVICE_PROTOCOL_VAL:
		Service service = (Service) hit.getAttribute(Attribute.SERVICE_TYPE_ID);
		colomnValue = toCSVServiceProtocol(service);
		break;
	    case SERVICE_PORT_VAL:
		service = (Service) hit.getAttribute(Attribute.SERVICE_TYPE_ID);
		colomnValue = toCSVServicePort(service);
		break;
	    }

	    if (!colomnValue.isEmpty()) {
		Utility.addWord(builder, colomnValue, true);
	    }
	}
	return builder.toString();
    }

    /**
     * Create hit list by job id
     * 
     * @param columnsTypes
     *            configuration of columns types
     * @param jobName
     *            name of the job
     * @return list of the hits
     * @throws IOException
     *             if IO errors occurs
     * @throws CSVParseException
     *             if fails to parse file
     */
    public static List<Hit> fromCSV(final List<Integer> columnsTypes, final String jobName)
	    throws IOException, CSVParseException {
	return fromPath(columnsTypes, CSVDaoConfig.getHitsFile(jobName));
    }

    /**
     * Create hit list by file path
     * 
     * @param columnsTypes
     *            configuration of columns types
     * @param filePath
     *            path to file
     * @return list of hits built from the CSV file
     * @throws IOException
     *             if IO errors occurs
     * @throws CSVParseException
     *             if fails to parse file
     */
    public static List<Hit> fromPath(final List<Integer> columnsTypes, final String filePath)
	    throws IOException, CSVParseException {
	return fromCSV(columnsTypes, filePath, new ArrayList<>(), Filter.ANY_FILTER, new ArrayList<>());
    }

    /**
     * Write to a file hits by CSV format
     * 
     * @param columnsTypes
     *            configuration of columns types
     * @param hits
     *            list of the list
     * @param outputPath
     *            path to output file
     * @throws IOException
     *             if IO errors occurs
     * @throws CSVParseException
     *             if fails to parse hits
     */
    public static void toCSV(final List<Integer> columnsTypes, final List<Hit> hits, final String outputPath)
	    throws IOException, CSVParseException {
	final File outputFile = new File(outputPath);
	if (outputFile.exists() && !outputFile.canWrite()) {
	    throw new IOException("File already exist and can't be over written");
	}

	final BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
	try {
	    if (columnsTypes.contains(null)) {
		throw new IllegalArgumentException("Colomns types list can't contains nulls");
	    }
	    if (columnsTypes.contains(SERVICE_PORT) ^ columnsTypes.contains(SERVICE_PROTOCOL)) {
		throw new IllegalArgumentException("Choose service port and service protocol or neither of them");
	    }
	    for (final Hit hit : hits) {
		if (columnsTypes.contains(SOURCE) && hit.getAttribute(Attribute.SOURCE_TYPE_ID) == null) {
		    throw new IllegalArgumentException("Source attribute is missing in one of the hits");
		}
		if (columnsTypes.contains(DESTINATION) && hit.getAttribute(Attribute.DESTINATION_TYPE_ID) == null) {
		    throw new IllegalArgumentException("Destination attribute is missing in one of the hits");
		}
		if (columnsTypes.contains(SOURCE) && hit.getAttribute(Attribute.SOURCE_TYPE_ID) == null) {
		    throw new IllegalArgumentException("Source attribute is missing in one of the hits");
		}
	    }

	    final CSVParser parser = new CSVParser(columnsTypes);
	    for (final Hit hit : hits) {
		if (hit == null) {
		    continue;
		}

		final String line = parser.toCSV(hit);
		if (!line.isEmpty()) {
		    writer.write(line);
		    writer.newLine();
		}
	    }
	} finally {
	    writer.close();
	}
    }

    /**
     * Parse a CSV file and outputs the hits to a list.
     * <p>
     * 
     * @param columnsTypes
     *            the types of the columns in the CSV files.
     * @param fileName
     *            the name of the CSV file.
     * @param rules
     *            the rules to filter the hits by.
     * @param filter
     *            the filter to filter the hits by.
     * @return list with all parsed hits that matched the filter and rules.
     * @throws IOException
     *             if any I/O errors occurs.
     * @throws CSVParseException
     *             if the data in the file is invalid.
     */
    static List<Hit> fromCSV(final List<Integer> columnsTypes, final String fileName, final List<Rule> rules,
	    final Filter filter) throws CSVParseException, IOException {
	return fromCSV(columnsTypes, fileName, rules, filter, new ArrayList<>());
    }

    /**
     * Parse a CSV file and outputs the hits to desire collection destination.
     * <p>
     * 
     * @param <C>
     *            the type of the destination.
     * @param columnsTypes
     *            the types of the columns in the CSV files.
     * @param fileName
     *            the name of the CSV file.
     * @param rules
     *            the rules to filter the hits by.
     * @param filter
     *            the filter to filter the hits by.
     * @param destination
     *            the destination collection, which the hits are inserted to.
     * @return the destination collection.
     * @throws IOException
     *             if any I/O errors occurs.
     * @throws CSVParseException
     *             if the data in the file is invalid.
     */
    static <C extends Collection<? super Hit>> C fromCSV(final List<Integer> columnsTypes, final String fileName,
	    final List<Rule> rules, final Filter filter, final C destination) throws IOException, CSVParseException {
	final File repoFile = new File(fileName);
	if (!repoFile.exists()) {
	    throw new FileNotFoundException("File not found: " + fileName);
	} else if (!repoFile.canRead()) {
	    throw new IOException("File read is not permitted!");
	}

	if (columnsTypes.contains(SERVICE_PORT) ^ columnsTypes.contains(SERVICE_PROTOCOL)) {
	    throw new IllegalArgumentException("Choose service port and service protocol or neither of them");
	}

	final CSVParser parser = new CSVParser(columnsTypes);
	int lineNumber = 1;

	BufferedReader reader = null;
	try {
	    reader = new BufferedReader(new FileReader(repoFile));

	    for (String line; (line = reader.readLine()) != null;) {
		if (line.isEmpty()) {
		    continue;
		}
		Hit hit = parser.fromCSV(line);
		if (DaoUtilities.isMatch(hit, rules, filter)) {
		    destination.add(hit);
		}
		lineNumber++;

		if (lineNumber % 1_000_000 == 0) {
		    System.out.println("Reading hits from CSV: " + lineNumber + " hits have been read so far.");
		}
	    }
	} catch (final CSVParseException e) {
	    throw new CSVParseException("In line " + lineNumber + ": ", e);
	} finally {
	    if (reader != null) {
		reader.close();
	    }
	}
	return destination;
    }

    /**
     * Parse CSV string to source
     * 
     * @param source
     *            source string in CSV format
     * @return Source object
     * @throws CSVParseException
     *             if fails to parse source
     */
    private static Source fromCSVSource(final String source) throws CSVParseException {
	try {
	    return Source.createFromString(source);
	} catch (final IllegalArgumentException e) {
	    throw new CSVParseException("Unable to parse source: ", e);
	}
    }

    /**
     * Parse CSV string to destination
     * 
     * @param destination
     *            destination string in CSV format
     * @return Destination object
     * @throws CSVParseException
     *             if fails to parse destination
     */
    private static Destination fromCSVDestination(final String destination) throws CSVParseException {
	try {
	    return Destination.createFromString(destination);
	} catch (final IllegalArgumentException e) {
	    throw new CSVParseException("Unable to parse destination: ", e);
	}
    }

    /**
     * Parse CSV string to service
     * 
     * @param port
     *            port string in CSV format
     * @param protocol
     *            protocol string in CSV format
     * @return Service object
     * @throws CSVParseException
     *             if fails to parse service
     */
    private static Service fromCSVService(final String port, final String protocol) throws CSVParseException {
	int portNum, protocolInt;
	try {
	    portNum = Integer.parseInt(port);
	} catch (final NumberFormatException e) {
	    throw new CSVParseException("Unable to parse port to integer: ", e);
	}
	try {
	    protocolInt = Integer.parseInt(protocol);
	} catch (final NumberFormatException e) {
	    throw new CSVParseException("Unable to parse protocol code to integer: ", e);
	}

	final String protocolStr = Service.protocolName(protocolInt);
	if (protocolStr == null) {
	    throw new CSVParseException("Unknown protocol number " + protocolInt);
	}

	try {
	    return Service.create(protocolStr, portNum);
	} catch (final IllegalArgumentException e) {
	    throw new CSVParseException("Unable to parse service: ", e);
	}
    }

    /**
     * Convert source object to source string in CSV format
     * 
     * @param source
     *            the source
     * @return CSV source string
     * @throws CSVParseException
     *             if source is null
     */
    private static String toCSVSource(final Source source) throws CSVParseException {
	if (source == null) {
	    throw new CSVParseException("Source doesn't exist");
	}
	return source.getIp().toString();
    }

    /**
     * Convert destination object to destination string in CSV format
     * 
     * @param destination
     *            the destination
     * @return CSV destination string
     * @throws CSVParseException
     *             if destination is null
     */
    private static String toCSVDestination(final Destination destination) throws CSVParseException {
	if (destination == null) {
	    throw new CSVParseException("Destination doesn't exist");
	}
	return destination.getIp().toString();
    }

    /**
     * Convert service object to service protocol string in CSV format
     * 
     * @param service
     *            the service
     * @return CSV service protocol string
     */
    private static String toCSVServiceProtocol(final Service service) {
	return Integer.toString(service.getProtocolCode());
    }

    /**
     * Convert service object to service port string in CSV format
     * 
     * @param service
     *            the service
     * @return CSV service port string
     */
    private static String toCSVServicePort(final Service service) {
	return Integer.toString(service.getPortRangeStart());
    }

}
