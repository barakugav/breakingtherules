package breakingtherules.utilities;

import java.util.Objects;
import java.util.function.Supplier;

public class Caches {

    private Caches() {
    }

    @SuppressWarnings("unchecked")
    public static <K, E> Cache<K, E> emptyCache() {
	return EmptyCache.INSTANCE;
    }

    public static <K, E> Cache<K, E> unmodifiableCache(final Cache<K, E> cache) {
	return new UnmodifiableCache<>(cache);
    }

    public static <K, E> Cache<K, E> synchronizedCache(final Cache<K, E> cache) {
	return new SynchronizedCache<>(cache);
    }

    public static <K, E> Cache<K, E> synchronizedCache(final Cache<K, E> cache, final Object sync) {
	return new SynchronizedCache<>(cache, sync);
    }

    public static class EmptyCache<K, E> implements Cache<K, E> {

	@SuppressWarnings("rawtypes")
	private static final EmptyCache INSTANCE = new EmptyCache<>();

	private EmptyCache() {
	}

	@Override
	public E get(final Object key) {
	    return null;
	}

	@Override
	public E add(final K key, final E element) {
	    throw new UnsupportedOperationException();
	}

	@Override
	public void remove(final K key) {
	}

	@Override
	public int size() {
	    return 0;
	}

	@Override
	public void clear() {
	}

	@Override
	public boolean equals(final Object o) {
	    return o instanceof Cache && ((Cache<?, ?>) o).size() == 0;
	}

	@Override
	public int hashCode() {
	    return 0;
	}

	@Override
	public String toString() {
	    return "[]";
	}

    }

    public static class UnmodifiableCache<K, E> implements Cache<K, E> {

	private final Cache<K, E> cache;

	public UnmodifiableCache(final Cache<K, E> cache) {
	    this.cache = Objects.requireNonNull(cache);
	}

	@Override
	public E get(final K key) {
	    return cache.get(key);
	}

	@Override
	public E add(final K key, final E element) {
	    throw new UnsupportedOperationException();
	}

	@Override
	public void remove(final K key) {
	    throw new UnsupportedOperationException();
	}

	@Override
	public int size() {
	    return cache.size();
	}

	@Override
	public void clear() {
	    throw new UnsupportedOperationException();
	}

	@Override
	public boolean equals(final Object o) {
	    return o == this || cache.equals(o);
	}

	@Override
	public int hashCode() {
	    return cache.hashCode();
	}

	@Override
	public String toString() {
	    return cache.toString();
	}

    }

    public static class SynchronizedCache<K, E> implements Cache<K, E> {

	private final Cache<K, E> cache;
	private final Object sync;

	public SynchronizedCache(final Cache<K, E> cache) {
	    this.cache = Objects.requireNonNull(cache);
	    sync = this;
	}

	public SynchronizedCache(final Cache<K, E> cache, final Object sync) {
	    this.cache = Objects.requireNonNull(cache);
	    this.sync = Objects.requireNonNull(sync);
	}

	@Override
	public E get(final K key) {
	    synchronized (sync) {
		return cache.get(key);
	    }
	}

	@Override
	public E getOrAdd(final K key, final Supplier<E> supplier) {
	    synchronized (sync) {
		return cache.getOrAdd(key, supplier);
	    }
	}

	@Override
	public E add(final K key, final E element) {
	    synchronized (sync) {
		return cache.add(key, element);
	    }
	}

	@Override
	public void remove(final K key) {
	    synchronized (sync) {
		cache.remove(key);
	    }
	}

	@Override
	public int size() {
	    synchronized (sync) {
		return cache.size();
	    }
	}

	@Override
	public void clear() {
	    synchronized (sync) {
		cache.clear();
	    }
	}

	@Override
	public boolean equals(final Object o) {
	    synchronized (sync) {
		return o == this || cache.equals(o);
	    }
	}

	@Override
	public int hashCode() {
	    synchronized (sync) {
		return cache.hashCode();
	    }
	}

	@Override
	public String toString() {
	    synchronized (sync) {
		return cache.toString();
	    }
	}

    }

}
