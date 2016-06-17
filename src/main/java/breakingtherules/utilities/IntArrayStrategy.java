package breakingtherules.utilities;

import breakingtherules.utilities.Hashs.Strategy;

/**
 * A strategy for int arrays.
 * <p>
 * This strategy implements the strategy methods of the int arrays as simply as
 * possible.
 * <p>
 * This class is singleton.
 * <p>
 * 
 * @author Barak Ugav
 * @author Yishai Gronich
 *
 */
public final class IntArrayStrategy implements Strategy<int[]> {

    /**
     * The single instance of this class.
     */
    public static final IntArrayStrategy INSTANCE = new IntArrayStrategy();

    private IntArrayStrategy() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.utilities.Hashs.Strategy#equals(java.lang.Object,
     * java.lang.Object)
     */
    @Override
    public boolean equals(int[] a, int[] b) {
	// non null arrays
	if (a.length != b.length)
	    return false;
	for (int i = a.length; i-- != 0;)
	    if (a[i] != b[i])
		return false;
	return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.utilities.Hashs.Strategy#hashCode(java.lang.Object)
     */
    @Override
    public int hashCode(int[] k) {
	// non null array
	int h = 17;
	for (int i = k.length; i-- != 0;)
	    h = h * 31 + k[i];
	return h;
    }

}
