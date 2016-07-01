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
public class XMLDaoConfig {

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
    static final String REPOSITORY_TAG = "Repository";

    /**
     * Hit tag in XML file.
     */
    static final String HIT_TAG = "hit";

    /**
     * Source tag in XML file.
     */
    static final String SOURCE_TAG = "source";

    /**
     * Destination tag in XML file.
     */
    static final String DESTINATION_TAG = "destination";

    /**
     * Service tag in XML file.
     */
    static final String SERVICE_TAG = "service";

    /**
     * Tag of a rule in XML format.
     */
    static final String RULE_TAG = "rule";

    /**
     * Tag of the original rule in XML format.
     */
    static final String ORIGINAL_RULE_TAG = "original-rule";

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
    public static String getRulesFile(final String jobName) {
	return new StringBuilder().append(DaoConfig.getRepoRoot(jobName)).append('/').append(RULES_FILE).toString();
    }

}
