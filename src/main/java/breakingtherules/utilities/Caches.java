package breakingtherules.utilities;

import java.util.Objects;
import java.util.function.Function;

/**
 * Utilities class for caches.
 * 
 * @author Barak Ugav
 * @author Yishai Gronich
 * 
 * @see Cache
 */
public class Caches {

    /**
     * Suppresses default constructor, ensuring non-instantiability.
     */
    private Caches() {
    }

    /**
     * Get an empty cache object.
     * 
     * @param <K>
     *            the type of keys used in the cache.
     * @param <E>
     *            the type of element of the cache.
     * @return an empty unmodifiable cache.
     */
    @SuppressWarnings("unchecked")
    public static <K, E> Cache<K, E> emptyCache() {
	return EmptyCache.INSTANCE;
    }

    /**
     * Get an unmodifiable cache view on existing cache.
     * 
     * @param <K>
     *            the type of the keys used in the cache.
     * @param <E>
     *            the type of elements if the cache.
     * @param cache
     *            existing cache.
     * @return unmodifiable cache view on the existing cache.
     * @throws NullPointerException
     *             if the cache is null.
     */
    public static <K, E> Cache<K, E> unmodifiableCache(final Cache<K, E> cache) {
	return new UnmodifiableCache<>(cache);
    }

    /**
     * Get a synchronized cache view on existing cache.
     * 
     * @param <K>
     *            the type of the keys used in the cache.
     * @param <E>
     *            the type of elements if the cache.
     * @param cache
     *            existing cache.
     * @return synchronized cache view on the existing cache.
     * @throws NullPointerException
     *             if the cache is null.
     */
    public static <K, E> Cache<K, E> synchronizedCache(final Cache<K, E> cache) {
	return new SynchronizedCache<>(cache);
    }

    /**
     * Get a synchronized cache view on existing cache.
     * 
     * @param <K>
     *            the type of the keys used in the cache.
     * @param <E>
     *            the type of elements if the cache.
     * @param cache
     *            existing cache.
     * @param sync
     *            object the cache will synchronize on.
     * @return synchronized cache view on the existing cache.
     * @throws NullPointerException
     *             if the cache is null.
     */
    public static <K, E> Cache<K, E> synchronizedCache(final Cache<K, E> cache, final Object sync) {
	return new SynchronizedCache<>(cache, sync);
    }

    /**
     * An empty unmodifiable cache.
     * 
     * @author Barak Ugav
     * @author Yishai Gronich
     * 
     * @param <K>
     *            type of key of the cache
     * @param <E>
     *            type of cached elements
     */
    public static class EmptyCache<K, E> implements Cache<K, E> {

	/**
	 * Single instance of this class.
	 */
	@SuppressWarnings("rawtypes")
	private static final EmptyCache INSTANCE = new EmptyCache<>();

	/**
	 * Construct new empty cache.
	 */
	private EmptyCache() {
	}

	/**
	 * @return null
	 */
	@Override
	public E get(final Object key) {
	    return null;
	}

	/**
	 * @throws UnsupportedOperationException
	 *             (always)
	 */
	@Override
	public E add(final K key, final E element) {
	    throw new UnsupportedOperationException();
	}

	/**
	 * Does nothing
	 */
	@Override
	public void remove(final K key) {
	    // Do nothing
	}

	/**
	 * @return 0
	 */
	@Override
	public int size() {
	    return 0;
	}

	/**
	 * Does nothing
	 */
	@Override
	public void clear() {
	    // Do nothing
	}

	/**
	 * @return true only if the compared object is cache with size 0.
	 */
	@Override
	public boolean equals(final Object o) {
	    return o instanceof Cache && ((Cache<?, ?>) o).size() == 0;
	}

	/**
	 * @return 0
	 */
	@Override
	public int hashCode() {
	    return 0;
	}

	/**
	 * @return '[]'
	 */
	@Override
	public String toString() {
	    return "[]";
	}

    }

    /**
     * An unmodifiable cache view on other cache.
     * 
     * @author Barak Ugav
     * @author Yishai Gronich
     * 
     * @param <K>
     *            type of key of the cache
     * @param <E>
     *            type of cached elements
     */
    public static class UnmodifiableCache<K, E> implements Cache<K, E> {

	/**
	 * The backing cache.
	 */
	private final Cache<K, E> cache;

	/**
	 * Construct new UnmodifiableCache view to existing cache.
	 * 
	 * @param cache
	 *            the existing cache.
	 * @throws NullPointerException
	 *             if the cache is null.
	 */
	public UnmodifiableCache(final Cache<K, E> cache) {
	    this.cache = Objects.requireNonNull(cache);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public E get(final K key) {
	    return cache.get(key);
	}

	/**
	 * @throws UnsupportedOperationException
	 *             (always)
	 */
	@Override
	public E add(final K key, final E element) {
	    throw new UnsupportedOperationException();
	}

	/**
	 * @throws UnsupportedOperationException
	 *             (always)
	 */
	@Override
	public void remove(final K key) {
	    throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int size() {
	    return cache.size();
	}

	/**
	 * @throws UnsupportedOperationException
	 *             (always)
	 */
	@Override
	public void clear() {
	    throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(final Object o) {
	    return o == this || cache.equals(o);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
	    return cache.hashCode();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
	    return cache.toString();
	}

    }

    /**
     * A synchronized cache view on other cache.
     * 
     * @author Barak Ugav
     * @author Yishai Gronich
     * 
     * @param <K>
     *            type of key of the cache
     * @param <E>
     *            type of cached elements
     */
    public static class SynchronizedCache<K, E> implements Cache<K, E> {

	/**
	 * The backing cache.
	 */
	private final Cache<K, E> cache;

	/**
	 * The object which this cache is synchronizing on.
	 */
	private final Object sync;

	/**
	 * Construct new synchronized cache view on existing cache.
	 * <p>
	 * The cache will synchronize on this SynchronizedCache object.
	 * 
	 * @param cache
	 *            the existing cache.
	 * @throws NullPointerException
	 *             if the cache is null.
	 */
	public SynchronizedCache(final Cache<K, E> cache) {
	    this.cache = Objects.requireNonNull(cache);
	    sync = this;
	}

	/**
	 * Construct new synchronized cache view on existing cache.
	 * <p>
	 * The cache will synchronize on the given object.
	 * 
	 * @param cache
	 *            the existing cache.
	 * @param sync
	 *            an object to synchronized on.
	 * @throws NullPointerException
	 *             if the cache is null or the sync object is null.
	 */
	public SynchronizedCache(final Cache<K, E> cache, final Object sync) {
	    this.cache = Objects.requireNonNull(cache);
	    this.sync = Objects.requireNonNull(sync);
	}

	/**
	 * Synchronized version of {@link Cache#get(Object)}.
	 */
	@Override
	public E get(final K key) {
	    synchronized (sync) {
		return cache.get(key);
	    }
	}

	/**
	 * Synchronized version of {@link Cache#getOrAdd(Object, Function)}.
	 */
	@Override
	public E getOrAdd(final K key, final Function<? super K, ? extends E> supplier) {
	    synchronized (sync) {
		return cache.getOrAdd(key, supplier);
	    }
	}

	/**
	 * Synchronized version of {@link Cache#add(Object, Object)}.
	 */
	@Override
	public E add(final K key, final E element) {
	    synchronized (sync) {
		return cache.add(key, element);
	    }
	}

	/**
	 * Synchronized version of {@link Cache#remove(Object)}.
	 */
	@Override
	public void remove(final K key) {
	    synchronized (sync) {
		cache.remove(key);
	    }
	}

	/**
	 * Synchronized version of {@link Cache#size()}.
	 */
	@Override
	public int size() {
	    synchronized (sync) {
		return cache.size();
	    }
	}

	/**
	 * Synchronized version of {@link Cache#clear()}.
	 */
	@Override
	public void clear() {
	    synchronized (sync) {
		cache.clear();
	    }
	}

	/**
	 * Synchronized version of {@link Cache#equals(Object)}.
	 */
	@Override
	public boolean equals(final Object o) {
	    if (o == this)
		return true;
	    synchronized (sync) {
		return cache.equals(o);
	    }
	}

	/**
	 * Synchronized version of {@link Cache#hashCode()}.
	 */
	@Override
	public int hashCode() {
	    synchronized (sync) {
		return cache.hashCode();
	    }
	}

	/**
	 * Synchronized version of {@link Cache#toString()}.
	 */
	@Override
	public String toString() {
	    synchronized (sync) {
		return cache.toString();
	    }
	}

    }

}
