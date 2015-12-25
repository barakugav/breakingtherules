package breakingtherules.dao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import breakingtherules.dto.ListDto;
import breakingtherules.firewall.Attribute;
import breakingtherules.firewall.Destination;
import breakingtherules.firewall.Rule;
import breakingtherules.firewall.Service;
import breakingtherules.firewall.Source;

/**
 * Implementation of {@link RulesDao} by XML repository
 */
@Component
public class RulesXmlDao implements RulesDao {

    private static final String REPOS_ROOT = "repository/";

    /**
     * All loaded repositories' rules
     */
    Hashtable<String, List<Rule>> m_loadedRules;

    /**
     * Constructor
     * 
     * Initialize loaded rules to empty
     */
    public RulesXmlDao() {
	m_loadedRules = new Hashtable<String, List<Rule>>();
    }

    /**
     * @see RulesDao#getRules(int)
     */
    @Override
    public ListDto<Rule> getRules(int jobId) throws IOException {
	String path = createPathFromId(jobId);
	return getRulesByPath(path);
    }

    /**
     * @see RulesDao#getRules(int, int, int)
     */
    @Override
    public ListDto<Rule> getRules(int jobId, int startIndex, int endIndex) throws IOException {
	String path = createPathFromId(jobId);
	return getRulesByPath(path, startIndex, endIndex);
    }

    /**
     * Get all rules from repository by repository string path
     * 
     * @param repoPath
     *            string path to repository
     * @return all rules
     * @throws IOException
     *             if failed to read from memory
     */
    public ListDto<Rule> getRulesByPath(String repoPath) throws IOException {
	List<Rule> rules = loadRules(repoPath);
	return new ListDto<Rule>(rules, 0, rules.size(), rules.size());
    }

    /**
     * Get rules from repository in range [startIndex, endIndex] by repository
     * string path
     * 
     * @param repoPath
     *            string path to repository
     * @param startIndex
     *            the end index of the rules list, including this index
     * @param endIndex
     *            the end index of the rules list, including this index
     * @return rules in range [startIndex, endIndex]
     * @throws IOException
     *             if failed to read from memory
     */
    public ListDto<Rule> getRulesByPath(String repoPath, int startIndex, int endIndex) throws IOException {
	if (startIndex < 0) {
	    throw new IllegalArgumentException("Start index < 0");
	} else if (startIndex > endIndex) {
	    throw new IllegalArgumentException("Start index > end index");
	}
	// Don't need to check that arguments aren't null, if they do, usual
	// null exception will be thrown later

	// Extract all rules
	List<Rule> rules = loadRules(repoPath);

	int total = rules.size();
	if (startIndex >= total) {
	    throw new IndexOutOfBoundsException("Start index bigger that total count");
	}
	endIndex = (int) Math.min(endIndex, total);
	List<Rule> subRulesList = rules.subList(startIndex, endIndex);
	return new ListDto<Rule>(subRulesList, startIndex, endIndex, total);
    }

    /**
     * Load all the rules from repository
     * 
     * @param repoPath
     *            string path to repository file
     * @return list of loaded rules from repository
     * @throws IOException
     *             if failed to read from file
     */
    private List<Rule> loadRules(String repoPath) throws IOException {
	// Check if this repository is already loaded
	List<Rule> rules = m_loadedRules.get(repoPath);
	if (rules == null) {
	    // Load from file
	    Document repositoryDoc = UtilityXmlDao.readFile(repoPath);

	    // Get all rules from repository
	    NodeList rulesList = repositoryDoc.getElementsByTagName("rule");

	    // Parse into rule objects
	    rules = new ArrayList<Rule>();
	    for (int i = 0; i < rulesList.getLength(); i++) {
		Node ruleNode = rulesList.item(i);
		if (ruleNode.getNodeType() == Element.ELEMENT_NODE) {
		    Element ruleElm = (Element) ruleNode;
		    Rule rule = createRule(ruleElm);
		    rules.add(rule);
		}
	    }
	    m_loadedRules.put(repoPath, rules);
	}

	return rules;
    }

    /**
     * Create a string path from a job id
     * 
     * @param id
     *            id of the job
     * @return string path to the job's repository
     */
    private static String createPathFromId(int id) {
	return REPOS_ROOT + id + "/repository.xml";
    }

    /**
     * Creates {@link Rule} object from {@link Element} XML object
     * 
     * @param ruleElm
     *            XML element with rule attributes
     * @return rule object with the element attributes
     */
    private static Rule createRule(Element ruleElm) {
	// Read attributes from element
	String id = ruleElm.getAttribute("id");
	String source = ruleElm.getAttribute("source");
	String destination = ruleElm.getAttribute("destination");
	String service = ruleElm.getAttribute("service");

	// Convert strings to attributes
	int newId = Integer.parseInt(id);
	Source newSource = new Source(source);
	Destination newDestination = new Destination(destination);
	Service newService = new Service(service);

	// Create attributes vector
	Vector<Attribute> attributes = new Vector<Attribute>();
	attributes.add(newSource);
	attributes.add(newDestination);
	attributes.add(newService);

	return new Rule(newId, attributes);
    }

}
