package breakingtherules.dao.xml;

import java.io.IOException;
import java.util.Set;

import breakingtherules.dao.AbstractCachedHitsDao;
import breakingtherules.dao.HitsDao;
import breakingtherules.dao.ParseException;
import breakingtherules.firewall.Hit;

/**
 * Implementation of {@link HitsDao} by XML repository.
 * <p>
 * Able to read hits from repository files, save the hits for each repository
 * for next hits request.
 * <p>
 *
 * @author Barak Ugav
 * @author Yishai Gronich
 *
 */
public class XMLHitsDao extends AbstractCachedHitsDao {

    /**
     * {@inheritDoc}
     */
    @Override
    public void initJob(final String jobName, final Iterable<Hit> hits) throws IOException {
	XMLHitsParser.writeHits(hits, XMLDaoConfig.getHitsFile(jobName));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Set<Hit> getHits(final String jobName) throws IOException, ParseException {
	return XMLHitsParser.parseUniqueHits(XMLDaoConfig.getHitsFile(jobName));
    }

}
