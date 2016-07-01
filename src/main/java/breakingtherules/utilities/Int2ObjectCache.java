package breakingtherules.utilities;

import java.util.function.IntFunction;

public interface Int2ObjectCache<E> {

    public E get(int key);

    public E add(int key, E element);

    public void remove(int key);

    /**
     * Get the number of cached elements in the cache.
     * <p>
     * Used mostly for testing.
     * 
     * @return number of cached elements.
     */
    public int size();

    /**
     * Clear the entire cache from all elements.
     */
    public void clear();

    default E getOrAdd(final int key, final IntFunction<? extends E> supplier) {
	E elm = get(key);
	if (elm == null) {
	    elm = supplier.apply(key);
	    add(key, elm);
	}
	return elm;
    }

}
