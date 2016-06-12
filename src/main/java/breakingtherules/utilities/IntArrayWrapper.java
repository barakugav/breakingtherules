package breakingtherules.utilities;

import java.util.Arrays;

/**
 * A wrapper for an int array.
 * <p>
 * The wrapper supply basic equals, hashCode and toString methods to the int
 * array.
 * 
 * @author Barak Ugav
 * @author Yishai Gronich
 *
 */
public final class IntArrayWrapper {

    /**
     * The wrapped array.
     */
    private final int[] array;

    /**
     * Construct new IntArrayWrapper.
     * 
     * @param array
     *            an array to wrap.
     */
    public IntArrayWrapper(final int[] array) {
	this.array = array;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object o) {
	if (o == this) {
	    return true;
	}
	if (!(o instanceof IntArrayWrapper)) {
	    return false;
	}

	final IntArrayWrapper other = (IntArrayWrapper) o;
	return Arrays.equals(array, other.array);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
	return Arrays.hashCode(array);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	return Arrays.toString(array);
    }

}
