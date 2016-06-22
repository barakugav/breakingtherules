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
 * @see CSVHitsDao
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
    private final int[] m_columnsTypes;

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
     *             if the columns types list is null, or one the elements in it
     *             is null.
     */
    public CSVParser(final List<Integer> columnsTypes) {
	// Create unmodifiable clone of the input list
	m_columnsTypes = new int[columnsTypes.size()];
	for (int i = m_columnsTypes.length; i-- != 0;) {
	    m_columnsTypes[i] = columnsTypes.get(i).intValue();
	}

	sourceIndex = columnsTypes.indexOf(SOURCE);
	destinationIndex = columnsTypes.indexOf(DESTINATION);
	serviceProtocolIndex = columnsTypes.indexOf(SERVICE_PROTOCOL);
	servicePortIndex = columnsTypes.indexOf(SERVICE_PORT);
    }

    /**
     * Parse a hit from a string line.
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
    public Hit parseHit(final String line) throws CSVParseException {
	final String[] words = Utility.breakToWords(line, Utility.TAB, Utility.SPACE);
	final List<Attribute> attributes = new ArrayList<>(Attribute.TYPES_COUNT);

	try {
	    if (sourceIndex >= 0) {
		final String source = words[sourceIndex];
		attributes.add(parseSource(source));
	    }
	    if (destinationIndex >= 0) {
		final String destination = words[destinationIndex];
		attributes.add(parseDestination(destination));
	    }
	    if (serviceProtocolIndex >= 0 && servicePortIndex >= 0) {
		final String protocol = words[serviceProtocolIndex];
		final String port = words[servicePortIndex];
		attributes.add(parseService(port, protocol));
	    }
	} catch (ArrayIndexOutOfBoundsException e) {
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
	    String colomnValue;
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
	    default:
		colomnValue = null;
	    }

	    if (colomnValue != null && !colomnValue.isEmpty()) {
		Utility.addWord(builder, colomnValue, true);
	    }
	}
	return builder.toString();
    }

    /**
     * Write to a file hits by CSV format
     * 
     * @param columnsTypes
     *            configuration of columns types.
     * @param hits
     *            list of the list.
     * @param outputPath
     *            path to output file.
     * @throws IOException
     *             if IO errors occurs.
     * @throws CSVParseException
     *             if fails to parse hits.
     * @throws NullPointerException
     *             if the columns types list is null, or one of the elements in
     *             it is null.
     */
    public static void toCSV(final List<Integer> columnsTypes, final Iterable<Hit> hits, final String outputPath)
	    throws IOException, CSVParseException {
	final File outputFile = new File(outputPath);
	if (outputFile.exists() && !outputFile.canWrite()) {
	    throw new IOException("File already exist and can't be over written");
	}

	try (final BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
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
		if (hit != null) {
		    final String line = parser.toCSV(hit);
		    if (!line.isEmpty()) {
			writer.write(line);
			writer.newLine();
		    }
		}
	    }
	}
    }

    /**
     * Parse all hits that are in a file.
     * 
     * @param columnsTypes
     *            configuration of columns types
     * @param fileName
     *            name of the file.
     * @return list of hits built from the CSV file
     * @throws IOException
     *             if IO errors occurs
     * @throws CSVParseException
     *             if fails to parse file
     */
    public static List<Hit> parseHits(final List<Integer> columnsTypes, final String fileName)
	    throws IOException, CSVParseException {
	final List<Hit> hits = new ArrayList<>();
	parseHits(columnsTypes, fileName, Collections.emptyList(), Filter.ANY_FILTER, hits);
	return hits;
    }

    /**
     * Parse CSV file of hits and output the hits to a destination collection.
     * <p>
     * All hits from file will be parsed. If they will match all rules and
     * filter, they will be added to the destination collection through the
     * {@link Collection#add(Object)} method.
     * 
     * @param columnsTypes
     *            configuration of columns types.
     * @param fileName
     *            name of the CSV file.
     * @param rules
     *            list of rules to filter by them.
     * @param filter
     *            filter to filter by it.
     * @param destination
     *            the destination collection.
     * @throws CSVParseException
     *             if the data in the file is invalid.
     * @throws IOException
     *             if any I/O errors occurs.
     */
    static void parseHits(final List<Integer> columnsTypes, final String fileName, final List<Rule> rules,
	    final Filter filter, final Collection<? super Hit> destination) throws CSVParseException, IOException {
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
	int lineNumber = 0;

	try (final BufferedReader reader = new BufferedReader(new FileReader(repoFile))) {
	    for (String line; (line = reader.readLine()) != null;) {
		lineNumber++;
		if (line.isEmpty()) {
		    continue;
		}
		final Hit hit = parser.parseHit(line);
		if (DaoUtilities.isMatch(hit, rules, filter)) {
		    destination.add(hit);
		}

		if (lineNumber % 1_000_000 == 0) {
		    System.out.println("Reading hits from CSV: " + lineNumber + " hits have been read so far.");
		}
	    }
	} catch (final CSVParseException e) {
	    throw new CSVParseException("In line " + lineNumber + ": ", e);
	}
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
    private static Source parseSource(final String source) throws CSVParseException {
	try {
	    return Source.valueOf(source);
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
    private static Destination parseDestination(final String destination) throws CSVParseException {
	try {
	    return Destination.valueOf(destination);
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
    private static Service parseService(final String port, final String protocol) throws CSVParseException {
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

	try {
	    return Service.valueOf(protocolInt, portNum);
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
     * @throws CSVParseException
     *             if the service is null.
     */
    private static String toCSVServiceProtocol(final Service service) throws CSVParseException {
	if (service == null) {
	    throw new CSVParseException("Service doesn't exist");
	}
	return Integer.toString(service.getProtocolCode());
    }

    /**
     * Convert service object to service port string in CSV format
     * 
     * @param service
     *            the service
     * @return CSV service port string
     * 
     * @throws CSVParseException
     *             if the service is null.
     */
    private static String toCSVServicePort(final Service service) throws CSVParseException {
	if (service == null) {
	    throw new CSVParseException("Service doesn't exist");
	}
	return Integer.toString(service.getPortRangeStart());
    }

}
