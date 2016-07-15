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
     * The default permissiveness for the algorithm
     */
    public static double DEFUALT_PERMISSIVENESS = 50;

    /**
     * Initiate the SuggestionsAlgorithm with a DAO it will use
     *
     * @param hitsDao
     *            The DAO that the algorithm will use in order to read the job's
     *            hits
     */
    public AbstractSuggestionsAlgorithm(final HitsDao hitsDao) {
	m_hitsDao = Objects.requireNonNull(hitsDao);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPermissiveness(final double permissiveness) {
	if (!(0 <= permissiveness && permissiveness <= 100))
	    throw new IllegalArgumentException("Permissiveness should be in range [0, 100]: " + permissiveness);
	// Do nothing.
    }

}
