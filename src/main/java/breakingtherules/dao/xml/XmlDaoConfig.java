package breakingtherules.dao.xml;

import breakingtherules.dao.DaoConfig;

/**
 * 
 * @author Barak Ugav
 * @author Yishai Gronich
 *
 */
class XmlDaoConfig extends DaoConfig {

    private static final String HITS_FILE = "fullRepository.xml";

    private static final String RULES_FILE = "repository.xml";

    static final String REPOSITORY = "Repository";
    static final String HIT = "hit";

    static String getHitsFile(final String jobName) {
	return new StringBuilder().append(getRepoRoot(jobName)).append('/').append(HITS_FILE).toString();
    }

    static String getRulesFile(final String jobName) {
	return new StringBuilder().append(getRepoRoot(jobName)).append('/').append(RULES_FILE).toString();
    }

}
