package breakingtherules.dao.xml;

import breakingtherules.dao.DaoConfig;

class XmlDaoConfig extends DaoConfig {

    private static final String HITS_FILE = "fullRepository.xml";

    private static final String RULES_FILE = "repository.xml";

    static final String REPOSITORY = "Repository";
    static final String HIT = "hit";

    static String getHitsFile(final String jobName) {
	return getRepoRoot(jobName) + HITS_FILE;
    }

    static String getRulesFile(final String jobName) {
	return getRepoRoot(jobName) + RULES_FILE;
    }

}
