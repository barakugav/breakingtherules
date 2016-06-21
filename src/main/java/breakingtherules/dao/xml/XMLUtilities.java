package breakingtherules.dao.xml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * The XMLUtilities class is used by XML DAOs, and include only helper static
 * methods.
 * 
 * @author Barak Ugav
 * @author Yishai Gronich
 * 
 * @see XMLHitsDao
 * @see XMLRulesDao
 */
public class XMLUtilities {

    /**
     * Suppresses default constructor, ensuring non-instantiability.
     */
    private XMLUtilities() {
    }

    /**
     * Read document from a file.
     * <p>
     * 
     * @param fileName
     *            name of the file.
     * @return document with the file data
     * @throws IOException
     *             if reading from file failed
     * @throws SAXException
     *             if failed to parse the file.
     */
    public static Document readFile(final String fileName) throws IOException, SAXException {
	final File repoFile = new File(fileName);
	if (!repoFile.exists()) {
	    throw new FileNotFoundException(fileName);
	}
	final DocumentBuilder builder;
	try {
	    final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    builder = factory.newDocumentBuilder();
	} catch (ParserConfigurationException e) {
	    // Shouldn't happen.
	    throw new InternalError(e);
	}
	final Document fileDocument = builder.parse(repoFile);
	return fileDocument;
    }

    /**
     * Write a document to a file
     * 
     * @param path
     *            string path to file
     * @param doc
     *            document to write
     * @throws IOException
     *             if failed to write to memory
     */
    public static void writeFile(final String path, final Document doc) throws IOException {
	final File file = new File(path);
	final Transformer transformer;
	try {
	    final TransformerFactory factory = TransformerFactory.newInstance();
	    transformer = factory.newTransformer();
	} catch (TransformerConfigurationException e) {
	    // Shouldn't happen.
	    throw new InternalError(e);
	}
	transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
	final DOMSource source = new DOMSource(doc);
	final StreamResult result = new StreamResult(file);
	try {
	    transformer.transform(source, result);
	} catch (final TransformerException e) {
	    throw new IOException(e);
	}
    }

}
