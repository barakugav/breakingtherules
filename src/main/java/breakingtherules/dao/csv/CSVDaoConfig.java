package breakingtherules.dao.csv;

import breakingtherules.dao.DaoConfig;

/**
 * The CSVDaoConfig used to get configuration information specific for CSV DAO
 * objects.
 * <p>
 * 
 * @author Barak Ugav
 * @author Yishai Gronich
 * 
 */
public class CSVDaoConfig {

    /**
     * Name of CSV repository file
     */
    private static final String HITS_FILE = "repository.csv";

    // Suppresses default constructor, ensuring non-instantiability.
    private CSVDaoConfig() {
    }

    /**
     * Get path to repository CSV file by job id
     * 
     * @param jobName
     *            name of the job
     * @return path to CSV repository file
     */
    public static String getHitsFile(final String jobName) {
	return new StringBuilder().append(DaoConfig.getRepoRoot(jobName)).append('/').append(HITS_FILE).toString();
    }

}
