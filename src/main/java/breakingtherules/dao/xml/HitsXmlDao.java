package breakingtherules.dao.xml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import breakingtherules.dao.HitsDao;
import breakingtherules.dao.DaoUtilities;
import breakingtherules.dto.ListDto;
import breakingtherules.firewall.Attribute;
import breakingtherules.firewall.Destination;
import breakingtherules.firewall.Filter;
import breakingtherules.firewall.Hit;
import breakingtherules.firewall.Rule;
import breakingtherules.firewall.Service;
import breakingtherules.firewall.Source;
import breakingtherules.utilities.Utility;

/**
 * Implementation of {@link HitsDao} by XML repository.
 * <p>
 * Able to read hits from repository files, save the hits for each repository
 * for next hits request.
 * <p>
 * 
 * @author Barak Ugav
 * @author Yishai Gronich
 * 
 */
public class HitsXmlDao implements HitsDao {

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.dao.HitsDao#getHitsNumber(java.lang.String,
     * java.util.List, breakingtherules.firewall.Filter)
     */
    @Override
    public int getHitsNumber(final String jobName, final List<Rule> rules, final Filter filter)
	    throws IOException, XMLParseException {
	return getHits(jobName, rules, filter).getSize();
    }

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.dao.HitsDao#getHits(java.lang.String,
     * java.util.List, breakingtherules.firewall.Filter)
     */
    @Override
    public ListDto<Hit> getHits(final String jobName, final List<Rule> rules, final Filter filter)
	    throws IOException, XMLParseException {
	final String path = XmlDaoConfig.getHitsFile(jobName);
	return getHitsByPath(path, rules, filter);
    }

    /**
     * Get hits from repository that match all rules and filter by repository
     * string path
     * 
     * @param repoPath
     *            string path to repository
     * @param rules
     *            list of the current rules, act like additional filter
     * @param filter
     *            filter of the hits
     * @return all hits that match all rules and filter
     * @throws IOException
     *             if failed to read from memory
     * @throws XMLParseException
     *             if any XML parse error occurs in the data.
     */
    public ListDto<Hit> getHitsByPath(final String repoPath, final List<Rule> rules, final Filter filter)
	    throws IOException, XMLParseException {
	final List<Hit> allHits = loadHits(repoPath);
	final List<Hit> matchedHits = new ArrayList<>();

	for (final Hit hit : allHits) {
	    if (DaoUtilities.isMatch(hit, rules, filter)) {
		matchedHits.add(hit);
	    }
	}
	final int size = matchedHits.size();
	return new ListDto<>(matchedHits, 0, size, size);
    }

    /**
     * Get all hits from repository that match all rules and filter by
     * repository string path
     * 
     * @param repoPath
     *            string path to repository
     * @param startIndex
     *            the start index of the hits list, including this index
     * @param endIndex
     *            the end index of the hits list, including this index
     * @param rules
     *            list of the current rules, act like additional filter
     * @param filter
     *            filter of the hits
     * @return all hits that match all rules and filter in range [startIndex,
     *         endIndex]
     * @throws IOException
     *             if failed to read from memory
     * @throws XMLParseException
     *             if any XML parse error occurs in the data.
     */
    public ListDto<Hit> getHitsByPath(final String repoPath, final List<Rule> rules, final Filter filter,
	    final int startIndex, final int endIndex) throws IOException, XMLParseException {
	if (startIndex < 0) {
	    throw new IllegalArgumentException("Start index < 0");
	} else if (startIndex > endIndex) {
	    throw new IllegalArgumentException("Start index > end index");
	}
	// Don't need to check that arguments aren't null, if they do, usual
	// null exception will be thrown later

	// Extract all hits that match the filter
	final List<Hit> allHits = getHitsByPath(repoPath, rules, filter).getData();

	int total = allHits.size();
	if (total == 0) {// return this empty list
	    return new ListDto<>(allHits, startIndex, endIndex, total);
	}
	if (startIndex >= total) {
	    throw new IndexOutOfBoundsException("Start index " + startIndex + " bigger that total count");
	}
	final List<Hit> subHitsList = Utility.subList(allHits, startIndex, endIndex - startIndex);
	return new ListDto<>(subHitsList, startIndex, endIndex, total);
    }

    /**
     * Write a list of hits to file in XML format
     * 
     * @param hits
     *            list of the hits
     * @param filePath
     *            output file path
     * @throws IOException
     *             if fails to write to file
     */
    public static void toXml(final List<Hit> hits, final String filePath) throws IOException {
	try {
	    final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    final DocumentBuilder builder = factory.newDocumentBuilder();
	    final Document doc = builder.newDocument();

	    final Element repoElm = doc.createElement(XmlDaoConfig.REPOSITORY);
	    for (final Hit hit : hits) {
		final Element elm = createElement(doc, hit);
		repoElm.appendChild(elm);
	    }
	    doc.appendChild(repoElm);

	    XMLDaoUtilities.writeFile(filePath, doc);
	} catch (final ParserConfigurationException e) {
	    throw new IOException(e);
	}
    }

    /**
     * Load all the hits from repository
     * 
     * @param repoPath
     *            string path to repository file
     * @return list of loaded hits from repository
     * @throws IOException
     *             if failed to read from file
     * @throws XMLParseException
     *             if any XML parse error occurs in the data.
     */
    private static List<Hit> loadHits(final String repoPath) throws IOException, XMLParseException {
	// Load from file
	final Document repositoryDoc = XMLDaoUtilities.readFile(repoPath);

	// Get all hits from repository
	final NodeList hitsList = repositoryDoc.getElementsByTagName(XmlDaoConfig.HIT);

	// Extract all hits that match the filter
	final int length = hitsList.getLength();
	final ArrayList<Hit> hits = new ArrayList<>(length);
	for (int i = 0; i < length; i++) {
	    final Node hitNode = hitsList.item(i);
	    if (hitNode.getNodeType() == Node.ELEMENT_NODE) {
		final Element hitElm = (Element) hitNode;
		final Hit hit = createHit(hitElm);
		hits.add(hit);
	    }
	}
	hits.trimToSize();
	return hits;
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
    private static Hit createHit(final Element hitElm) throws XMLParseException {
	// Read attributes from element
	final String sourceStr = hitElm.getAttribute(Attribute.SOURCE_TYPE.toLowerCase());
	final String destinationStr = hitElm.getAttribute(Attribute.DESTINATION_TYPE.toLowerCase());
	final String serviceStr = hitElm.getAttribute(Attribute.SERVICE_TYPE.toLowerCase());

	if (sourceStr == null || sourceStr.isEmpty()) {
	    throw new XMLParseException("Source does not exist");
	} else if (destinationStr == null || destinationStr.isEmpty()) {
	    throw new XMLParseException("Destination does not exist");
	} else if (serviceStr == null || serviceStr.isEmpty()) {
	    throw new XMLParseException("Service does not exist");
	}

	// Convert strings to attributes
	final List<Attribute> attributes = new ArrayList<>();
	try {
	    attributes.add(Source.createFromString(sourceStr));
	    attributes.add(Destination.createFromString(destinationStr));
	    attributes.add(Service.createFromString(serviceStr));

	} catch (final Exception e) {
	    throw new XMLParseException(e);
	}
	return new Hit(attributes);
    }

    /**
     * Create new XML element of hit.
     * <p>
     * 
     * @param doc
     *            the parent document.
     * @param hit
     *            the parsed hit.
     * @return XML element that represent the hit.
     */
    private static Element createElement(final Document doc, final Hit hit) {
	final Element elm = doc.createElement(XmlDaoConfig.HIT);
	for (final Attribute attribute : hit) {
	    elm.setAttribute(attribute.getType().toLowerCase(), attribute.toString());
	}
	return elm;
    }

}
