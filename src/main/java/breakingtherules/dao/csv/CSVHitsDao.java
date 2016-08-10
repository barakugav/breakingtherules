package breakingtherules.dao.csv;

import java.io.IOException;
import java.util.List;

import breakingtherules.dao.AbstractCachedHitsDao;
import breakingtherules.dao.ParseException;
import breakingtherules.firewall.Hit;

/**
 * The CSVHitsDao is a basic DAO that only parse hits, line by line from CSV
 * files.
 * <p>
 *
 * @author Barak Ugav
 * @author Yishai Gronich
 *
 * @see CSVHitsParser
 */
public class CSVHitsDao extends AbstractCachedHitsDao {

    /**
     * This DAO writes and reads from a CSV DAO. The columnTypes parameter tells
     * the CSVParser the other of the column - what attributes are where
     */
    private List<Integer> m_columnTypes;

    /**
     * Construct new CSVHitsDao.
     */
    public CSVHitsDao() {
	m_columnTypes = CSVHitsParser.DEFAULT_COLUMNS_TYPES;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initJob(final String jobName, final Iterable<Hit> hits) throws IOException {
	try {
	    CSVHitsParser.toCSV(m_columnTypes, hits, CSVDaoConfig.getHitsFile(jobName));
	} catch (final CSVParseException e) {
	    throw new IllegalArgumentException(e);
	}
    }

    /**
     * Change the type of each column
     *
     * @param columnTypes
     *            New column types
     */
    public void setColumnTypes(final List<Integer> columnTypes) {
	m_columnTypes = columnTypes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Iterable<Hit> getHits(final String jobName) throws IOException, ParseException {
	return CSVHitsParser.parseUniqueHits(m_columnTypes, CSVDaoConfig.getHitsFile(jobName));
    }

}
