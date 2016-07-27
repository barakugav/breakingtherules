package breakingtherules.util;

import java.util.Map;
import java.util.function.Function;
import java.util.function.IntFunction;

/**
 * Specific int keyed to object elements cache.
 *
 * @author Barak Ugav
 * @author Yishai Gronich
 *
 * @param <E>
 *            type of cached elements.
 */
public interface Int2ObjectCache<E> {

    /**
     * Add new element to cache.
     * <p>
     * This method is different from {@link Map#put(Object, Object)}, if an
     * element with the same key as added is already in the cache, this
     * operation has no effect.
     *
     * @param key
     *            the element key
     * @param element
     *            the element
     * @return the existing element or the new inserted element
     */
    public E add(int key, E element);

    /**
     * Clear the entire cache from all elements.
     */
    public void clear();

    /**
     * Get a cached element by its key.
     *
     * @param key
     *            the element's key
     * @return the cached element or null if non found.
     */
    public E get(int key);

    /**
     * Remove a cashed element by its key.
     *
     * @param key
     *            the element's key.
     */
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
     * Get an element from the cache by it's key or add one if one doesn't
     * exist.
     * <p>
     * This method should be used for two reasons:
     * <ol>
     * <li>It can improve performance, instead of calling {@link #get(int)} and
     * if null is returned call {@link #add(int, Object)}, the operation
     * combined to one, and can be overridden by a faster implementation.</li>
     * <li>Thread safety, if all the cache methods are synchronized, calling
     * this method ensure that no other threads used the cache between the
     * element search and it's insertion (if needed). If this method wasn't
     * exist, and the alternative is to call {@link #get(int)} and if returned
     * null call {@link #add(int, Object)}, another thread could add the desire
     * element between the two calls, duplication the creation effort of the
     * element.</li>
     * </ol>
     * <p>
     * This method is similar to {@link Map#computeIfAbsent(Object, Function)}.
     * <p>
     *
     * @param key
     *            the key of the element.
     * @param cachingFunction
     *            the supplier of the element if one doesn't exist in the cache.
     * @return the existing element or the one created from the supplier (if
     *         needed).
     * @throws NullPointerException
     *             if the supplier is needed and it's null or the supplied
     *             element is null (null elements are not allowed in weak
     *             cache).
     */
    default E getOrAdd(final int key, final IntFunction<? extends E> cachingFunction) {
	E elm = get(key);
	if (elm == null) {
	    elm = cachingFunction.apply(key);
	    add(key, elm);
	}
	return elm;
    }

}
