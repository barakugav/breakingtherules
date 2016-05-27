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
import breakingtherules.dao.UtilityDao;
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
 * Implementation of {@link HitsDao} by XML repository
 * 
 * Able to read hits from repository files, save the hits for each repository
 * for next hits request
 */
public class HitsXmlDao implements HitsDao {

    /**
     * Constructor
     */
    public HitsXmlDao() {
    }

    @Override
    public int getHitsNumber(final int jobId, final List<Rule> rules, final Filter filter) throws IOException {
	return getHits(jobId, rules, filter).getSize();
    }

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.dao.HitsDao#getHits(int, java.util.List,
     * breakingtherules.firewall.Filter)
     */
    @Override
    public ListDto<Hit> getHits(final int jobId, final List<Rule> rules, final Filter filter) throws IOException {
	final String path = XmlDaoConfig.getHitsFile(jobId);
	return getHitsByPath(path, rules, filter);
    }

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.dao.HitsDao#getHits(int, java.util.List,
     * breakingtherules.firewall.Filter, int, int)
     */
    @Override
    public ListDto<Hit> getHits(final int jobId, final List<Rule> rules, final Filter filter, final int startIndex,
	    final int endIndex) throws IOException {
	final String path = XmlDaoConfig.getHitsFile(jobId);
	return getHitsByPath(path, rules, filter, startIndex, endIndex);
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
     */
    public ListDto<Hit> getHitsByPath(final String repoPath, final List<Rule> rules, final Filter filter)
	    throws IOException {
	final List<Hit> allHits = loadHits(repoPath);
	final List<Hit> matchedHits = new ArrayList<>();

	for (final Hit hit : allHits) {
	    if (UtilityDao.isMatch(hit, rules, filter)) {
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
     */
    public ListDto<Hit> getHitsByPath(final String repoPath, final List<Rule> rules, final Filter filter,
	    final int startIndex, final int endIndex) throws IOException {
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
	    hits.sort(Hit.IDS_COMPARATOR);
	    for (final Hit hit : hits) {
		final Element elm = doc.createElement(XmlDaoConfig.HIT);
		createElement(elm, hit);
		repoElm.appendChild(elm);
	    }
	    doc.appendChild(repoElm);

	    UtilityXmlDao.writeFile(filePath, doc);
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
     */
    private static List<Hit> loadHits(final String repoPath) throws IOException {
	// Load from file
	final Document repositoryDoc = UtilityXmlDao.readFile(repoPath);

	// Get all hits from repository
	final NodeList hitsList = repositoryDoc.getElementsByTagName(XmlDaoConfig.HIT);

	// Extract all hits that match the filter
	final List<Hit> hits = new ArrayList<>();
	final int length = hitsList.getLength();
	for (int i = 0; i < length; i++) {
	    final Node hitNode = hitsList.item(i);
	    if (hitNode.getNodeType() == Node.ELEMENT_NODE) {
		try {
		    final Element hitElm = (Element) hitNode;
		    final Hit hit = createHit(hitElm);
		    hits.add(hit);

		} catch (final XMLParseException e) {
		    e.printStackTrace();
		}
	    }
	}
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
	final String idStr = hitElm.getAttribute(XmlDaoConfig.ID);
	final String sourceStr = hitElm.getAttribute(Attribute.SOURCE_TYPE.toLowerCase());
	final String destinationStr = hitElm.getAttribute(Attribute.DESTINATION_TYPE.toLowerCase());
	final String serviceStr = hitElm.getAttribute(Attribute.SERVICE_TYPE.toLowerCase());

	if (idStr == null || idStr.isEmpty()) {
	    throw new XMLParseException("Id does not exist");
	} else if (sourceStr == null || sourceStr.isEmpty()) {
	    throw new XMLParseException("Source does not exist");
	} else if (destinationStr == null || destinationStr.isEmpty()) {
	    throw new XMLParseException("Destination does not exist");
	} else if (serviceStr == null || serviceStr.isEmpty()) {
	    throw new XMLParseException("Service does not exist");
	}

	// Convert strings to attributes
	try {
	    final int intID = Integer.parseInt(idStr);
	    final Source sourceObj = new Source(sourceStr);
	    final Destination destinationObj = new Destination(destinationStr);
	    final Service serviceObj = new Service(serviceStr);

	    // Create attributes list
	    final List<Attribute> attributes = new ArrayList<>();
	    attributes.add(sourceObj);
	    attributes.add(destinationObj);
	    attributes.add(serviceObj);
	    return new Hit(intID, attributes);

	} catch (final Exception e) {
	    throw new XMLParseException(e);
	}
    }

    private static void createElement(final Element node, final Hit hit) {
	node.setAttribute(XmlDaoConfig.ID, Integer.toString(hit.getId()));
	for (final Attribute attribute : hit) {
	    node.setAttribute(attribute.getType().toLowerCase(), attribute.toString());
	}
    }

}
