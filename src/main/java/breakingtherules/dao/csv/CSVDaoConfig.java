package breakingtherules.dao.csv;

import breakingtherules.dao.DaoConfig;

public class CSVDaoConfig extends DaoConfig {

    private static final String HITS_FILE = "repository.csv";

    public static String getHitsFile(int id) {
	return getRepoRoot(id) + HITS_FILE;
    }

}
