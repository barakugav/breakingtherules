package breakingtherules.utilities;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

public class SynchronizedHashCache<K, E> implements Cache<K, E> {

    private static final class ElementLock {
    }

    private final ReentrantLock m_mapLock;

    private final Map<K, Pair<ElementLock, E>> m_map;

    public SynchronizedHashCache() {
	m_mapLock = new ReentrantLock();
	m_map = new HashMap<>();
    }

    @Override
    public E get(final K key) {
	m_mapLock.lock();
	try {
	    final Pair<ElementLock, E> pair = m_map.get(key);
	    if (pair == null)
		return null;
	    final ElementLock elementLock = pair.getFirst();
	    m_mapLock.unlock();
	    synchronized (elementLock) {
		return pair.getSecond();
	    }
	} finally {
	    if (m_mapLock.isHeldByCurrentThread()) {
		m_mapLock.unlock();
	    }
	}
    }

    @Override
    public E add(final K key, final E element) {
	try {
	    m_mapLock.lock();
	    Pair<ElementLock, E> pair = m_map.get(key);
	    if (pair != null) {
		final ElementLock elementLock = pair.getFirst();
		m_mapLock.unlock();
		synchronized (elementLock) {
		    return pair.getSecond();
		}
	    } else {
		final ElementLock elementLock = new ElementLock();
		pair = Pair.of(elementLock, null);
		m_map.put(key, pair);
		synchronized (elementLock) {
		    m_mapLock.unlock();
		    pair.setSecond(element);
		    return element;
		}
	    }
	} finally {
	    if (m_mapLock.isHeldByCurrentThread()) {
		m_mapLock.unlock();
	    }
	}
    }

    @Override
    public E getOrAdd(final K key, final Function<? super K, ? extends E> supplier) {
	try {
	    m_mapLock.lock();
	    Pair<ElementLock, E> pair = m_map.get(key);
	    if (pair != null) {
		final ElementLock elementLock = pair.getFirst();
		m_mapLock.unlock();
		synchronized (elementLock) {
		    return pair.getSecond();
		}
	    } else {
		final ElementLock elementLock = new ElementLock();
		pair = Pair.of(elementLock, null);
		m_map.put(key, pair);
		synchronized (elementLock) {
		    m_mapLock.unlock();
		    final E element = supplier.apply(key);
		    pair.setSecond(element);
		    return element;
		}
	    }
	} finally {
	    if (m_mapLock.isHeldByCurrentThread()) {
		m_mapLock.unlock();
	    }
	}
    }

    @Override
    public void remove(final K key) {
	m_mapLock.lock();
	try {
	    m_map.remove(key);
	} finally {
	    if (m_mapLock.isHeldByCurrentThread()) {
		m_mapLock.unlock();
	    }
	}
    }

    @Override
    public int size() {
	m_mapLock.lock();
	try {
	    return m_map.size();
	} finally {
	    if (m_mapLock.isHeldByCurrentThread()) {
		m_mapLock.unlock();
	    }
	}
    }

    @Override
    public void clear() {
	m_mapLock.lock();
	try {
	    m_map.clear();
	} finally {
	    if (m_mapLock.isHeldByCurrentThread()) {
		m_mapLock.unlock();
	    }
	}
    }

}