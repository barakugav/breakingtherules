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

/**
 * Implementation of {@link RulesDao} by XML repository
 */
@Component
public class RulesXmlDao implements RulesDao {

    /**
     * Document of this repository
     */
    private Document m_doc;

    /**
     * Constructor
     */
    public RulesXmlDao() {
    }

    public String loadRepository(String path) {
	try {
	    File repoFile = new File(path);

	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder builder = factory.newDocumentBuilder();
	    m_doc = builder.parse(repoFile);
	    return null;

	} catch (IOException e) {
	    e.printStackTrace();
	    return "IO Exception";
	} catch (SAXException e) {
	    e.printStackTrace();
	    return "SAX Exception";
	} catch (ParserConfigurationException e) {
	    e.printStackTrace();
	    return "Parser Configuration Exception";
	}
    }

    public List<Rule> getRules() {
	// Get all rules from repository
	NodeList rulesList = m_doc.getElementsByTagName("rule");

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
