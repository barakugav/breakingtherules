package breakingtherules.utilities;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * The LinesIterator is used to iterate over line of a file without loading all
 * of them to the memory
 */
public class LinesIterator implements Closeable {

    /**
     * Reader is used by this iterator
     */
    private BufferedReader reader;

    /**
     * Current line
     */
    private String line;

    /**
     * Constructor with file
     * 
     * @param path
     *            path to input file
     * @throws FileNotFoundException
     *             if file wan't found
     */
    public LinesIterator(String path) throws FileNotFoundException {
	this(new File(path));
    }

    /**
     * Constructor with file
     * 
     * @param file
     *            input file
     * @throws FileNotFoundException
     *             if fail wan't found
     */
    public LinesIterator(File file) throws FileNotFoundException {
	Objects.requireNonNull(file);
	reader = new BufferedReader(new FileReader(file));
	line = null;
    }

    /**
     * Checks if this iterator has more lines
     * 
     * @return true if this iterator can read more lines
     * @throws IOException
     *             if I/O errors occurs
     */
    public boolean hasNext() throws IOException {
	openCheck();
	return line != null || (line = reader.readLine()) != null;
    }

    /**
     * Get the next line in this iterator
     * 
     * @return next line in the iterator
     * @throws IOException
     *             if I/O errors occurs
     */
    public String next() throws IOException {
	openCheck();
	if (!hasNext())
	    throw new NoSuchElementException();
	String line = this.line;
	this.line = reader.readLine();
	return line;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.io.Closeable#close()
     */
    @Override
    public void close() throws IOException {
	if (reader != null) {
	    reader.close();
	    reader = null;
	}
    }

    /**
     * Checks if this iterator is opened
     * 
     * @throws IOException
     *             if this iterator is closed
     */
    private void openCheck() throws IOException {
	if (reader == null)
	    throw new IOException("The connection is already closed");
    }

}
