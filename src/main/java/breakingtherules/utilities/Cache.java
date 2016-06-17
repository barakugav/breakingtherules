package breakingtherules.utilities;

import java.util.function.Function;

/**
 * Cache for reusable objects.
 * <p>
 * Allowing search element by key, insertion by key and remove.
 * 
 * @author Barak Ugav
 * @author Yishai Gronich
 * @param <K>
 *            type of key of the cache
 * @param <E>
 *            type of cached elements
 * 
 */
public interface Cache<K, E> {

    /**
     * Get a cached element by its key.
     * 
     * @param key
     *            the element's key
     * @return the cached element or null if non found.
     */
    public E get(K key);

    /**
     * Add new element to cache
     * 
     * @param key
     *            the element key
     * @param element
     *            the element
     * @return the existing element or the new inserted element
     */
    public E add(K key, E element);

    /**
     * Remove a cashed element by its key.
     * 
     * @param key
     *            the element's key
     */
    public void remove(K key);

    /**
     * Get the number of cached elements in the cache.
     * 
     * @return number of cached elements.
     */
    public int size();

    /**
     * Clear the entire cache from all elements.
     */
    public void clear();

    /**
     * Get an element from the cache or add one if one doesn't exists in the
     * cache.
     * 
     * @param key
     *            the element key
     * @param supplier
     *            a supplier of the element, which will be used only if the
     *            element doesn't exist in the cache.
     * @return the element that exists in the cache or the ones supplied from
     *         the supplier if needed.
     */
    default E getOrAdd(final K key, final Function<? super K, ? extends E> supplier) {
	E elm = get(key);
	if (elm == null) {
	    elm = supplier.apply(key);
	    add(key, elm);
	}
	return elm;
    }

}
