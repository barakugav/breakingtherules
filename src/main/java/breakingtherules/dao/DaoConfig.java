package breakingtherules.dao;

/**
 * The DaoConfig used to get configurations information used by DAO objects
 */
public class DaoConfig {

    /**
     * Repositories root prefix
     */
    private static final String REPOS_ROOT = "repository/";

    /**
     * Get root path to repository by a job id
     * 
     * @param name
     *            name of the job
     * @return string path to repository root
     */
    public static String getRepoRoot(final String name) {
	return REPOS_ROOT + name + "/";
    }

}
