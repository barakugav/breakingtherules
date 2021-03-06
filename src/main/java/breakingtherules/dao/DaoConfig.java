package breakingtherules.dao;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * The DaoConfig used to get configurations information used by DAO objects.
 * <p>
 *
 * @author Barak Ugav
 * @author Yishai Gronich
 *
 * @see HitsDao
 * @see RulesDao
 */
public class DaoConfig {

    /**
     * Repositories root prefix.
     */
    private static final String REPOS_ROOT = "repository";

    /**
     * Suppresses default constructor, ensuring non-instantiability.
     */
    private DaoConfig() {
    }

    /**
     * @return A list of job names, of all the existing repositories in the
     *         system
     */
    public static List<String> getExistingRepositories() {
	final File reposRoot = new File(REPOS_ROOT);
	return Arrays.asList(reposRoot.list());
    }

    /**
     * Get root path to repository by a job name.
     *
     * @param jobName
     *            name of the job.
     * @return string path to repository root.
     */
    public static String getRepoRoot(final String jobName) {
	return new StringBuilder().append(REPOS_ROOT).append('/').append(Objects.requireNonNull(jobName)).toString();
    }

    /**
     * Initiate a repository for a new job with the given name
     *
     * @param jobName
     *            The name of the new job
     * @return Iff the job name is not taken (initiation success)
     * @throws IOException
     *             If the directory write was unsuccessful
     */
    public static boolean initRepository(final String jobName) throws IOException {
	final File directory = new File(getRepoRoot(jobName) + '/');
	if (directory.exists())
	    return false;
	if (directory.mkdir())
	    return true;
	throw new IOException();
    }

}
