package breakingtherules.dao;

public class DaoConfig {

    private static final String REPOS_ROOT = "repository/";

    public static String getRepoRoot(int id) {
	return REPOS_ROOT + id + "/";
    }

}
