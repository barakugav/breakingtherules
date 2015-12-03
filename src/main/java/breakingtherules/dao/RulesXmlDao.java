package breakingtherules.dao;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import breakingtherules.firewall.Attribute;
import breakingtherules.firewall.Destination;
import breakingtherules.firewall.Rule;
import breakingtherules.firewall.Service;
import breakingtherules.firewall.Source;
import breakingtherules.session.Job;
import breakingtherules.session.NoCurrentJobException;

/**
 * Implementation of {@link RulesDao} by XML repository
 */
@Component
public class RulesXmlDao implements RulesDao {

    public static Document loadRepository(String path) throws IOException {
	try {
	    File repoFile = new File(path);

	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder builder = factory.newDocumentBuilder();
	    Document repositoryDocument = builder.parse(repoFile);
	    return repositoryDocument;

	} catch (IOException | SAXException | ParserConfigurationException e) {
	    e.printStackTrace();
	    throw new IOException("Unable to load repository: " + e.getMessage());
	}
    }

    public List<Rule> getRules(Job currentJob) throws IOException, NoCurrentJobException {
	String repoPath = currentJob.getRepositoryLocation();
	Document repositoryDoc = loadRepository(repoPath);

	// Get all rules from repository
	NodeList rulesList = repositoryDoc.getElementsByTagName("rule");

	// Parse into rule objects
	List<Rule> matchedRules = new ArrayList<Rule>();
	for (int i = 0; i < rulesList.getLength(); i++) {
	    Node ruleNode = rulesList.item(i);
	    if (ruleNode.getNodeType() == Element.ELEMENT_NODE) {
		Element ruleElm = (Element) ruleNode;

		Rule rule = createRule(ruleElm);
		matchedRules.add(rule);
	    }
	}

	return matchedRules;
    }

    /**
     * Creates {@link Rule} object from {@link Element} XML object
     * 
     * @param ruleElm
     *            XML element with rule attributes
     * @return rule object with the element attributes
     */
    private Rule createRule(Element ruleElm) {
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
