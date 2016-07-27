package breakingtherules.util;

import java.util.Map;
import java.util.function.Function;

/**
 * Cache for reusable objects keyed by object keys.
 * <p>
 * Allowing search element by key, insertion by key and remove.
 * <p>
 * This interface is similar to the {@link Map} interface, but is more minimal
 * and have big different in the {@link #add(Object, Object)} method (analogous
 * to {@link Map#put(Object, Object)}): The {@link #add(Object, Object)} method
 * will have no effect if an element with the same key is already in the cache.
 * This behavior is expected because the cache treat elements with the same key
 * as the same element (different from {@link Map} where the mapping between
 * keys to values can change), and the user of the cache should not change the
 * mapping function.
 *
 * @author Barak Ugav
 * @author Yishai Gronich
 *
 * @param <K>
 *            type of key of the cache
 * @param <E>
 *            type of cached elements
 */
public interface Object2ObjectCache<K, E> {

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
    public E add(K key, E element);

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
    public E get(K key);

    /**
     * Remove a cashed element by its key.
     *
     * @param key
     *            the element's key.
     */
    public void remove(K key);

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
     * <li>It can improve performance, instead of calling {@link #get(Object)}
     * and if null is returned call {@link #add(Object, Object)}, the operation
     * combined to one, and can be overridden by a faster implementation.</li>
     * <li>Thread safety, if all the cache methods are synchronized, calling
     * this method ensure that no other threads used the cache between the
     * element search and it's insertion (if needed). If this method wasn't
     * exist, and the alternative is to call {@link #get(Object)} and if
     * returned null call {@link #add(Object, Object)}, another thread could add
     * the desire element between the two calls, duplication the creation effort
     * of the element.</li>
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
    default E getOrAdd(final K key, final Function<? super K, ? extends E> cachingFunction) {
	E elm = get(key);
	if (elm == null) {
	    elm = cachingFunction.apply(key);
	    add(key, elm);
	}
	return elm;
    }

}
