package breakingtherules.utilities;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.IntFunction;

public class Int2ObjectCaches {

    public static <E> Int2ObjectCache<E> synchronizedCache(final Int2ObjectCache<E> cache) {
	return new SynchronizedCache<>(cache);
    }

    public static class SynchronizedCache<E> implements Int2ObjectCache<E> {

	private final Int2ObjectCache<E> cache;
	private final Object sync;

	public SynchronizedCache(final Int2ObjectCache<E> cache) {
	    this.cache = Objects.requireNonNull(cache);
	    sync = this;
	}

	public SynchronizedCache(final Int2ObjectCache<E> cache, final Object sync) {
	    this.cache = Objects.requireNonNull(cache);
	    this.sync = Objects.requireNonNull(sync);
	}

	@Override
	public E get(final int key) {
	    synchronized (sync) {
		return cache.get(key);
	    }
	}

	public E getOrAdd(final int key, final IntFunction<? extends E> supplier) {
	    synchronized (sync) {
		return cache.getOrAdd(key, supplier);
	    }
	}

	@Override
	public E add(final int key, final E element) {
	    synchronized (sync) {
		return cache.add(key, element);
	    }
	}

	@Override
	public void remove(final int key) {
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
