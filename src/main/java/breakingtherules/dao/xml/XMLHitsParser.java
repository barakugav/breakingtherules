package breakingtherules.dao.xml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import breakingtherules.firewall.Destination;
import breakingtherules.firewall.Hit;
import breakingtherules.firewall.IP;
import breakingtherules.firewall.Service;
import breakingtherules.firewall.Source;

/**
 * Parser that parses hits from {@link Element}.
 * <p>
 * Caches can be set to reduce object creations and memory use.
 * <p>
 *
 * @author Barak Ugav
 * @author Yishai Gronich
 *
 * @see Hit
 * @see XMLHitsDao
 */
public class XMLHitsParser extends AbstractXMLAttributesContainerParser {

    /**
     * Construct new parser.
     */
    XMLHitsParser() {
    }

    /**
     * Creates {@link Hit} object from {@link Element} XML object
     *
     * @param hitElm
     *            XML element with hit attributes
     * @return hit object with the element attributes
     * @throws XMLParseException
     *             if failed to parse element to hit
     */
    private Hit parseHit(final Element hitElm) throws XMLParseException {
	try {
	    return new Hit(parseAttributesContainer(hitElm));
	} catch (final Exception e) {
	    throw new XMLParseException(e);
	}
    }

    /**
     * Get all hits from file.
     *
     * @param fileName
     *            name of the file.
     * @return all hits parsed from file.
     * @throws IOException
     *             if failed to read from memory.
     * @throws XMLParseException
     *             if any XML parse error occurs in the data.
     */
    public static List<Hit> parseAllHits(final String fileName) throws IOException, XMLParseException {
	final ArrayList<Hit> hits = new ArrayList<>();
	parseHits(fileName, hits);
	return hits;
    }

    /**
     * Get all unique hits from file.
     *
     * @param fileName
     *            name of the file.
     * @return all unique hits parsed from file.
     * @throws IOException
     *             if failed to read from memory.
     * @throws XMLParseException
     *             if any XML parse error occurs in the data.
     */
    public static Set<Hit> parseUniqueHits(final String fileName) throws XMLParseException, IOException {
	final Set<Hit> hits = new HashSet<>();
	parseHits(fileName, hits);
	return hits;
    }

    /**
     * Write a list of hits to file in XML format
     *
     * @param hits
     *            list of the hits
     * @param fileName
     *            output file path
     * @throws IOException
     *             if fails to write to file
     */
    public static void writeHits(final Iterable<Hit> hits, final String fileName) throws IOException {
	try {
	    final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    final DocumentBuilder builder = factory.newDocumentBuilder();
	    final Document doc = builder.newDocument();

	    final Element repoElm = doc.createElement(XMLDaoConfig.REPOSITORY_TAG);

	    for (final Hit hit : hits)
		repoElm.appendChild(fillElement(doc.createElement(XMLDaoConfig.HIT_TAG), hit));
	    doc.appendChild(repoElm);

	    XMLUtils.writeFile(fileName, doc);
	} catch (final ParserConfigurationException e) {
	    throw new IOException(e);
	}
    }

    /**
     * Parse all hits from file to a destination collection.
     *
     * @param fileName
     *            the input file name.
     * @param destination
     *            the destination collection of all hits parsed from the file
     *            and matched all rules and the filter.
     * @throws XMLParseException
     *             if the data in the file is invalid.
     * @throws IOException
     *             if any I/O errors occurs.
     */
    private static void parseHits(final String fileName, final Collection<? super Hit> destination)
	    throws XMLParseException, IOException {
	// Load from file
	final Document repositoryDoc;
	try {
	    repositoryDoc = XMLUtils.readFile(fileName);
	} catch (final SAXException e) {
	    throw new XMLParseException(e);
	}

	// Get all hits from repository
	final NodeList hitsList = repositoryDoc.getElementsByTagName(XMLDaoConfig.HIT_TAG);

	final XMLHitsParser parser = new XMLHitsParser();
	final IP.Cache ipsCache = new IP.Cache();
	parser.setSourceCache(new Source.Cache(ipsCache));
	parser.setDestinationCache(new Destination.Cache(ipsCache));
	parser.setServiceCache(new Service.Cache());

	// Extract all hits that match the filter
	final int length = hitsList.getLength();
	for (int i = 0; i < length; i++) {
	    final Node hitNode = hitsList.item(i);
	    if (hitNode.getNodeType() == Node.ELEMENT_NODE) {
		final Element hitElm = (Element) hitNode;
		final Hit hit = parser.parseHit(hitElm);
		destination.add(hit);
	    }
	}
    }

}
