package breakingtherules.dao.csv;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import breakingtherules.dao.AbstractCachedHitsDao;
import breakingtherules.firewall.Filter;
import breakingtherules.firewall.Hit;

/**
 * The CSVHitsDao is a basic DAO that only parse hits, line by line from CSV
 * files.
 * <p>
 * 
 * @author Barak Ugav
 * @author Yishai Gronich
 * 
 * @see CSVParser
 */
public class CSVHitsDao extends AbstractCachedHitsDao {

    /**
     * This DAO writes and reads from a CSV DAO. The columnTypes parameter tells
     * the CSVParser the other of the column - what attributes are where
     */
    private List<Integer> m_columnTypes;

    /**
     * Supplier function of hits.
     * <p>
     * 
     * @see #getHitsSupplier()
     */
    private final Function<String, Set<Hit>> m_hitsSupplier = jobName -> {
	try {
	    final Set<Hit> hits = new HashSet<>();
	    CSVParser.parseHits(m_columnTypes, CSVDaoConfig.getHitsFile(jobName),
		    Collections.emptyList(), Filter.ANY_FILTER, hits);
	    return hits;

	} catch (final IOException e) {
	    throw new UncheckedIOException(e);
	} catch (final CSVParseException e) {
	    throw new UncheckedCSVParseException(e);
	}
    };

    /**
     * Construct new CSVHitsDao.
     */
    public CSVHitsDao() {
	m_columnTypes = CSVParser.DEFAULT_COLUMNS_TYPES;
    }

    /**
     * Change the type of each column
     * 
     * @param columnTypes
     *            New column types
     */
    public void setColumnTypes(List<Integer> columnTypes) {
	m_columnTypes = columnTypes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initJob(final String jobName, final Iterable<Hit> hits) throws IOException {
	try {
	    CSVParser.toCSV(m_columnTypes, hits, CSVDaoConfig.getHitsFile(jobName));
	} catch (final CSVParseException e) {
	    throw new IllegalArgumentException(e);
	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Function<String, Set<Hit>> getHitsSupplier() {
	return m_hitsSupplier;
    }

    /**
     * Unchecked version of {@link CSVParseException}.
     * <p>
     * The UncheckedCSVParseException is a wrapper for a checked
     * {@link CSVParseException}.
     * <p>
     * Used when implementing or overriding a method that doesn't throw super
     * class exception of {@link CSVParseException}.
     * <p>
     * This exception is similar to {@link UncheckedIOException}.
     * <p>
     * 
     * @author Barak Ugav
     * @author Yishai Gronich
     *
     */
    protected static class UncheckedCSVParseException extends UncheckedParseException {

	@SuppressWarnings("javadoc")
	private static final long serialVersionUID = 6371272539188428352L;

	/**
	 * Construct new UncheckedCSVParseException without a message.
	 * 
	 * @param cause
	 *            the original checked {@link CSVParseException}.
	 */
	protected UncheckedCSVParseException(final CSVParseException cause) {
	    super(cause);
	}

	/**
	 * Construct new UncheckedCSVParseException with a message.
	 * 
	 * @param message
	 *            the exception's message.
	 * @param cause
	 *            the original checked {@link CSVParseException}.
	 */
	protected UncheckedCSVParseException(final String message, final CSVParseException cause) {
	    super(message, cause);
	}

	/**
	 * Get the {@link CSVParseException} cause of this unchecked exception.
	 * <p>
	 */
	@Override
	public synchronized CSVParseException getCause() {
	    return (CSVParseException) super.getCause();
	}

    }

}
