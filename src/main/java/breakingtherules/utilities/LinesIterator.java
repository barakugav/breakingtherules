package breakingtherules.utilities;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * The LinesIterator is used to iterate over lines of a file without loading all
 * of them to the memory
 */
public class LinesIterator implements Iterator<String>, Closeable {

    /**
     * Reader is used by this iterator
     */
    private BufferedReader reader;

    /**
     * Current line
     */
    private String line;

    /**
     * Number of current line
     */
    private int lineNumber;

    /**
     * Constructor with file path
     * 
     * @param path
     *            path to input file
     * @throws FileNotFoundException
     *             if file wan't found
     */
    public LinesIterator(final String path) throws FileNotFoundException {
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
	reader = new BufferedReader(new FileReader(file));
	line = null;
	lineNumber = 0;
    }

    /**
     * Checks if this iterator has more lines
     * 
     * @return true if this iterator can read more lines
     * @throws UncheckedIOException
     *             if I/O errors occurs
     */
    @Override
    public boolean hasNext() throws UncheckedIOException {
	try {
	    openCheck();
	    return line != null || (line = reader.readLine()) != null;
	} catch (final IOException e) {
	    throw new UncheckedIOException(e);
	}
    }

    /**
     * Get the next line in this iterator
     * 
     * @return next line in the iterator
     * @throws UncheckedIOException
     *             if I/O errors occurs
     */
    @Override
    public String next() throws UncheckedIOException {
	try {
	    if (!hasNext())
		throw new NoSuchElementException();
	    final String nextLine = this.line;
	    line = reader.readLine();
	    lineNumber++;
	    return nextLine;
	} catch (final IOException e) {
	    throw new UncheckedIOException(e);
	}
    }

    /**
     * Get current line number
     * 
     * @return current line number
     */
    public int lineNumber() {
	return lineNumber;
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
	if (reader == null) {
	    throw new IOException("The connection is already closed");
	}
    }

}