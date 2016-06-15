package breakingtherules.dao.csv;

import breakingtherules.dao.DaoConfig;

/**
 * The CSVDaoConfig used to get configuration information specific for CSV DAO
 * objects
 */
public class CSVDaoConfig extends DaoConfig {

    /**
     * Name of CSV repository file
     */
    private static final String HITS_FILE = "repository.csv";

    /**
     * Get path to repository CSV file by job id
     * 
     * @param name
     *            name of the job
     * @return path to CSV repository file
     */
    public static String getHitsFile(String name) {
	return getRepoRoot(name) + HITS_FILE;
    }

}
