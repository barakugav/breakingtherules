package breakingtherules.dao.xml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import breakingtherules.dao.HitsDao;
import breakingtherules.dto.ListDto;
import breakingtherules.firewall.Attribute;
import breakingtherules.firewall.Destination;
import breakingtherules.firewall.Filter;
import breakingtherules.firewall.Hit;
import breakingtherules.firewall.Rule;
import breakingtherules.firewall.Service;
import breakingtherules.firewall.Source;

/**
 * Implementation of {@link HitsDao} by XML repository
 * 
 * Able to read hits from repository files, save the hits for each repository
 * for next hits request
 */
public class HitsXmlDao implements HitsDao {

    /**
     * All loaded repositories' hits
     */
    private Hashtable<String, List<Hit>> m_loadedHits;

    /**
     * Constructor
     * 
     * Initialize loaded hits to empty
     */
    public HitsXmlDao() {
	m_loadedHits = new Hashtable<String, List<Hit>>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.dao.HitsDao#getHits(int, java.util.List,
     * breakingtherules.firewall.Filter)
     */
    @Override
    public ListDto<Hit> getHits(int jobId, List<Rule> rules, Filter filter) throws IOException {
	String path = XmlDaoConfig.getHitsFile(jobId);
	return getHitsByPath(path, rules, filter);
    }

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.dao.HitsDao#getHits(int, java.util.List,
     * breakingtherules.firewall.Filter, int, int)
     */
    @Override
    public ListDto<Hit> getHits(int jobId, List<Rule> rules, Filter filter, int startIndex, int endIndex)
	    throws IOException {
	String path = XmlDaoConfig.getHitsFile(jobId);
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
    public ListDto<Hit> getHitsByPath(String repoPath, List<Rule> rules, Filter filter) throws IOException {
	List<Hit> allHits = loadHits(repoPath);
	List<Hit> matchedHits = new ArrayList<Hit>();

	for (Hit hit : allHits) {
	    if (isMatch(rules, filter, hit)) {
		matchedHits.add(hit);
	    }
	}
	return new ListDto<Hit>(matchedHits, 0, matchedHits.size(), matchedHits.size());
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
    public ListDto<Hit> getHitsByPath(String repoPath, List<Rule> rules, Filter filter, int startIndex, int endIndex)
	    throws IOException {
	if (startIndex < 0) {
	    throw new IllegalArgumentException("Start index < 0");
	} else if (startIndex > endIndex) {
	    throw new IllegalArgumentException("Start index > end index");
	}
	// Don't need to check that arguments aren't null, if they do, usual
	// null exception will be thrown later

	// Extract all hits that match the filter
	List<Hit> allHits = getHitsByPath(repoPath, rules, filter).getData();

	int total = allHits.size();
	if (total == 0) // return this empty list
	    return new ListDto<Hit>(allHits, startIndex, endIndex, total);
	;
	if (startIndex >= total) {
	    throw new IndexOutOfBoundsException("Start index " + startIndex + " bigger that total count");
	}
	endIndex = (int) Math.min(endIndex, total);
	List<Hit> subHitsList = allHits.subList(startIndex, endIndex);
	return new ListDto<Hit>(subHitsList, startIndex, endIndex, total);
    }

    public static void writeHits(List<Hit> hits, String repoPath) {

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
    private List<Hit> loadHits(String repoPath) throws IOException {
	// Check if this repository is already loaded
	List<Hit> hits = m_loadedHits.get(repoPath);
	if (hits != null) {
	    return hits;
	}

	// Load from file
	Document repositoryDoc = UtilityXmlDao.readFile(repoPath);

	// Get all hits from repository
	NodeList hitsList = repositoryDoc.getElementsByTagName("hit");

	// Extract all hits that match the filter
	hits = new ArrayList<Hit>();
	for (int i = 0; i < hitsList.getLength(); i++) {
	    Node hitNode = hitsList.item(i);
	    if (hitNode.getNodeType() == Element.ELEMENT_NODE) {
		try {
		    Element hitElm = (Element) hitNode;
		    Hit hit = createHit(hitElm);
		    hits.add(hit);

		} catch (XMLParseException e) {
		    e.printStackTrace();
		    continue;
		}
	    }
	}
	m_loadedHits.put(repoPath, hits);
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
    private static Hit createHit(Element hitElm) {
	// Read attributes from element
	String id = hitElm.getAttribute("id");
	String source = hitElm.getAttribute("source");
	String destination = hitElm.getAttribute("destination");
	String service = hitElm.getAttribute("service");

	if (id == null || id.equals("")) {
	    throw new XMLParseException("Id does not exist");
	} else if (source == null || source.equals("")) {
	    throw new XMLParseException("Source does not exist");
	} else if (destination == null || destination.equals("")) {
	    throw new XMLParseException("Destination does not exist");
	} else if (service == null || service.equals("")) {
	    throw new XMLParseException("Service does not exist");
	}

	// Convert strings to attributes
	int intID = Integer.parseInt(id);
	Source sourceObj = new Source(source);
	Destination destinationObj = new Destination(destination);
	Service serviceObj = new Service(service);

	// Create attributes vector
	Vector<Attribute> attributes = new Vector<Attribute>();
	attributes.add(sourceObj);
	attributes.add(destinationObj);
	attributes.add(serviceObj);

	return new Hit(intID, attributes);
    }

    /**
     * Check if a hit is match to a list of rules and a filter
     * 
     * @param rules
     *            list of rules to check on the hit
     * @param filter
     *            filter to check on the hit
     * @param hit
     *            the hit that being checked
     * @return true if hit match all rules and filter, else - false
     */
    private static boolean isMatch(List<Rule> rules, Filter filter, Hit hit) {
	if (!filter.isMatch(hit)) {
	    return false;
	}
	for (Rule rule : rules) {
	    if (rule.getId() > 1 && rule.isMatch(hit)) {
		return false;
	    }
	}
	return true;
    }

}