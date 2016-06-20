package breakingtherules.dao.xml;

import breakingtherules.dao.DaoConfig;

/**
 * The XMLDaoConfig is a configuration class containing only constants and
 * static method.
 * <p>
 * The DAO classes uses this configuration class to parse XML files.
 * <p>
 * 
 * @author Barak Ugav
 * @author Yishai Gronich
 *
 * @see XMLHitsDao
 * @see XMLRulesDao
 */
class XMLDaoConfig {

    /**
     * Name of the hits file in each repository.
     */
    private static final String HITS_FILE = "fullRepository.xml";

    /**
     * Name if the rules file in each repository.
     */
    private static final String RULES_FILE = "repository.xml";

    /**
     * Repository tag in XML file.
     */
    static final String TAG_REPOSITORY = "Repository";

    /**
     * Hit tag in XML file.
     */
    static final String TAG_HIT = "hit";

    /**
     * Source tag in XML file.
     */
    static final String TAG_SOURCE = "source";

    /**
     * Destination tag in XML file.
     */
    static final String TAG_DESTINATION = "destination";

    /**
     * Service tag in XML file.
     */
    static final String TAG_SERVICE = "service";

    /**
     * Suppresses default constructor, ensuring non-instantiability.
     */
    private XMLDaoConfig() {
    }

    /**
     * Get the name of XML hits file by job name.
     * 
     * @param jobName
     *            name of the job.
     * @return name of the XML hits file associated with the job.
     */
    static String getHitsFile(final String jobName) {
	return new StringBuilder().append(DaoConfig.getRepoRoot(jobName)).append('/').append(HITS_FILE).toString();
    }

    /**
     * Get the name of XML rules file by job name.
     * 
     * @param jobName
     *            name of the job.
     * @return name of the XML rules file associated with the job.
     */
    static String getRulesFile(final String jobName) {
	return new StringBuilder().append(DaoConfig.getRepoRoot(jobName)).append('/').append(RULES_FILE).toString();
    }

}
