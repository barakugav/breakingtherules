package breakingtherules.service;

import java.util.Objects;

import breakingtherules.dao.HitsDao;

/**
 * TODO - javadoc
 *
 * @author Barak Ugav
 * @author Yishai Gronich
 *
 */
abstract class AbstractSuggestionsAlgorithm implements SuggestionsAlgorithm {

    /**
     * The DAO that the algorithm will use in order to read the job's hits
     */
    protected final HitsDao m_hitsDao;

    /**
     * Initiate the SuggestionsAlgorithm with a DAO it will use
     *
     * @param hitsDao
     *            The DAO that the algorithm will use in order to read the job's
     *            hits
     */
    public AbstractSuggestionsAlgorithm(final HitsDao hitsDao) {
	m_hitsDao = Objects.requireNonNull(hitsDao);
	setPermissiveness(DEFAULT_PERMISSIVENESS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPermissiveness(final double permissiveness) {
	if (!(MIN_PERMISSIVENESS <= permissiveness && permissiveness <= MAX_PERMISSIVENESS))
	    throw new IllegalArgumentException("Permissiveness should be in range [" + MIN_PERMISSIVENESS + ", "
		    + MAX_PERMISSIVENESS + "]: " + permissiveness);
	// Do nothing.
    }

}
