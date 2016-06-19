package breakingtherules.dao.xml;

import breakingtherules.dao.DaoConfig;

/**
 * TODO
 * 
 * @author Barak Ugav
 * @author Yishai Gronich
 *
 */
class XMLDaoConfig {

    private static final String HITS_FILE = "fullRepository.xml";

    private static final String RULES_FILE = "repository.xml";

    static final String REPOSITORY = "Repository";
    static final String HIT = "hit";

    // Suppresses default constructor, ensuring non-instantiability.
    private XMLDaoConfig() {
    }

    static String getHitsFile(final String jobName) {
	return new StringBuilder().append(DaoConfig.getRepoRoot(jobName)).append('/').append(HITS_FILE).toString();
    }

    static String getRulesFile(final String jobName) {
	return new StringBuilder().append(DaoConfig.getRepoRoot(jobName)).append('/').append(RULES_FILE).toString();
    }

}
