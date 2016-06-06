package breakingtherules.dao.xml;

import breakingtherules.dao.DaoConfig;

class XmlDaoConfig extends DaoConfig {

    private static final String HITS_FILE = "fullRepository.xml";

    private static final String RULES_FILE = "repository.xml";

    static final String REPOSITORY = "Repository";
    static final String HIT = "hit";

    static String getHitsFile(final int id) {
	return getRepoRoot(id) + HITS_FILE;
    }

    static String getRulesFile(final int id) {
	return getRepoRoot(id) + RULES_FILE;
    }

}
