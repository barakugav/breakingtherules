package breakingtherules.dao.xml;

import breakingtherules.dao.DaoConfig;

public class XmlDaoConfig extends DaoConfig {

    private static final String HITS_FILE = "/repository.xml";

    private static final String RULES_FILE = "/repository.xml";
    
    
    static final String REPOSITORY = "Repository";
    static final String HIT = "hit";
    static final String ID = "id";

    public static String getHitsFile(int id) {
	return getRepoRoot(id) + HITS_FILE;
    }

    public static String getRulesFile(int id) {
	return getRepoRoot(id) + RULES_FILE;
    }

}
