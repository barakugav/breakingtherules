package breakingtherules.dao.csv;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import breakingtherules.firewall.Attribute;
import breakingtherules.firewall.Destination;
import breakingtherules.firewall.Hit;
import breakingtherules.firewall.Service;
import breakingtherules.firewall.Source;
import breakingtherules.utilities.Utility;

/**
 * The CSVParser used to read and write CSV file to and from hits.
 */
public class CSVParser {

    private int m_idCounter;

    private final List<Integer> m_columnsTypes;

    /**
     * IDs for attributes used by this parser
     */
    public static final int SOURCE = 0;
    public static final int DESTINATION = 1;
    public static final int SERVICE_PROTOCOL = 2;
    public static final int SERVICE_PORT = 3;

    /**
     * Default columns types
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

    public CSVParser(final List<Integer> columnsTypes) {
	m_idCounter = 1;
	// Create unmodifiable clone of the input list
	m_columnsTypes = Collections.unmodifiableList(new ArrayList<>(columnsTypes));
    }

    public Hit fromCSV(final String line) throws CSVParseException {
	final List<String> words = Utility.breakToWords(line);
	final List<Attribute> attributes = new ArrayList<>();

	if (m_columnsTypes.contains(SOURCE)) {
	    final String source = words.get(m_columnsTypes.indexOf(SOURCE));
	    attributes.add(fromCSVSource(source));
	}
	if (m_columnsTypes.contains(DESTINATION)) {
	    final String destination = words.get(m_columnsTypes.indexOf(DESTINATION));
	    attributes.add(fromCSVDestination(destination));
	}
	if (m_columnsTypes.contains(SERVICE_PROTOCOL) && m_columnsTypes.contains(SERVICE_PORT)) {
	    final String protocol = words.get(m_columnsTypes.indexOf(SERVICE_PROTOCOL));
	    final String port = words.get(m_columnsTypes.indexOf(SERVICE_PORT));
	    attributes.add(fromCSVService(port, protocol));
	}

	return new Hit(m_idCounter++, attributes);
    }

    public String toCSV(final Hit hit) throws CSVParseException {
	final StringBuilder builder = new StringBuilder();
	for (final int colomnsType : m_columnsTypes) {
	    String colomnValue = "";
	    switch (colomnsType) {
	    case SOURCE:
		final Source source = (Source) hit.getAttribute(Attribute.SOURCE_TYPE_ID);
		colomnValue = toCSVSource(source);
		break;
	    case DESTINATION:
		final Destination destination = (Destination) hit.getAttribute(Attribute.DESTINATION_TYPE_ID);
		colomnValue = toCSVDestination(destination);
		break;
	    case SERVICE_PROTOCOL:
		Service service = (Service) hit.getAttribute(Attribute.SERVICE_TYPE_ID);
		colomnValue = toCSVServiceProtocol(service);
		break;
	    case SERVICE_PORT:
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
     * @param jobId
     *            id of the job
     * @return list of the hits
     * @throws IOException
     *             if IO errors occurs
     * @throws CSVParseException
     *             if fails to parse file
     */
    public static List<Hit> fromCSV(final List<Integer> columnsTypes, final int jobId)
	    throws IOException, CSVParseException {
	return fromCSV(columnsTypes, CSVDaoConfig.getHitsFile(jobId));
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
    public static List<Hit> fromCSV(final List<Integer> columnsTypes, final String filePath)
	    throws IOException, CSVParseException {
	final File repoFile = new File(filePath);
	if (!repoFile.exists()) {
	    throw new FileNotFoundException("File not found: " + filePath);
	} else if (!repoFile.canRead()) {
	    throw new IOException("File read is not permitted!");
	}

	final BufferedReader reader = new BufferedReader(new FileReader(repoFile));
	try {
	    return fromCSV(columnsTypes, new Iterator<String>() {

		private String line = reader.readLine();

		@Override
		public boolean hasNext() {
		    return line != null;
		}

		@Override
		public String next() {
		    String l = line;
		    if (l == null)
			throw new NoSuchElementException();
		    try {
			line = reader.readLine();
		    } catch (final IOException e) {
			throw new UncheckedIOException(e);
		    }
		    return l;
		}
	    });
	} finally {
	    reader.close();
	}
    }

    /**
     * Create it list from string lines iterator
     * 
     * @param columnsTypes
     *            configuration of columns types
     * @param lines
     *            string lines iterator
     * @return hits built from the lines
     * @throws CSVParseException
     *             if fails to parse lines
     */
    public static List<Hit> fromCSV(final List<Integer> columnsTypes, final Iterator<String> lines)
	    throws CSVParseException {
	if (columnsTypes.contains(SERVICE_PORT) ^ columnsTypes.contains(SERVICE_PROTOCOL)) {
	    throw new IllegalArgumentException("Choose service port and service protocol or neither of them");
	}

	final CSVParser parser = new CSVParser(columnsTypes);
	final List<Hit> hits = new ArrayList<>();
	int lineNumber = 1;

	try {
	    while (lines.hasNext()) {
		final String line = lines.next();
		if (line.isEmpty()) {
		    continue;
		}
		hits.add(parser.fromCSV(line));
		lineNumber++;
	    }
	} catch (final CSVParseException e) {
	    throw new CSVParseException("In line " + lineNumber + ": ", e);
	}

	return hits;
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
	    return new Source(source);
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
	    return new Destination(destination);
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
	    return new Service(protocolStr, portNum);
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
