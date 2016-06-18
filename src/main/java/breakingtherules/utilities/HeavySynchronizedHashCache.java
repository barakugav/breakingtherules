package breakingtherules.utilities;

import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

/**
 * The HeavySynchronizedHashCache class is used for heavy computational elements
 * in synchronized cache.
 * <p>
 * The purpose of this cache to support practical use of
 * {@link #getOrAdd(Object, Function)} for heavy computational supplier
 * functions. If there isn't such need you should use basic synchronized caches.
 * <p>
 * The cache is synchronized for all basic methods, {@link #get(Object)},
 * {@link #add(Object, Object)}, {@link #remove(Object)}, {@link #size()} and
 * {@link #clear()}. (same performance as a regular hash table).
 * <p>
 * The main method in this cache, {@link #getOrAdd(Object, Function)}, which is
 * synchronized too with the other methods, contains another synchronization
 * mechanism: If the element exist in the cache, the scenario is identical to
 * {@link #get(Object)}. If the element is not in the cache, the cache prepare
 * for suppling the element from the supplier function, and it's expecting heavy
 * computations - the synchronization between the other methods are released and
 * another synchronization for the specific element is obtained. This mechanism
 * allowing other threads to operate on the cache on other elements (that have
 * different keys) meanwhile the supplier function is operating. If another
 * thread request the same element as currently supplied by one of the suppliers
 * function, he won't supply it again, he will wait until the suppling of the
 * original element will end.
 * <p>
 * This class should be used only in cases that the supplier of the elements is
 * heavy and takes a long time to complete.
 * <p>
 * 
 * @author Barak Ugav
 * @author Yishai Gronich
 *
 * @param <K>
 *            type of the keys in the cache.
 * @param <E>
 *            type of the elements in the cache.
 */
public class HeavySynchronizedHashCache<K, E> implements Cache<K, E> {

    /*
     * Implementation notes.
     * 
     * The HeavySynchronizedHashCache is implemented by a bucket hash table. In
     * each cell in the table there is a bin (linked list of entries) that
     * contains all entries that fell to that cell.
     * 
     * The number of expected elements in each bin, if using the default load
     * factor (0.75) and the hash codes of the keys are random (in theory) is
     * very low. The probability for the length of the bins are as the
     * following: (taken from HashMap documentation)
     * 
     * 0:    0.60653066
     * 1:    0.30326533
     * 2:    0.07581633
     * 3:    0.01263606
     * 4:    0.00157952
     * 5:    0.00015795
     * 6:    0.00001316
     * 7:    0.00000094
     * 8:    0.00000006
     * more: less than 1 in ten million
     * 
     * The synchronized behavior of this cache is obtains by two mechanisms:
     * 
     * 1. There is a main lock for the whole cache, any reading or writing
     * operations on the cache acquire this lock before operating on the table.
     * 
     * 2. Each access to an entry element is synchronized on the entry itself.
     * This synchronization allowing the getOrAdd(Object, Function) to apply the
     * function without blocking the whole cache and later requests to the same
     * key will wait to the supplier function to operate. Allowing slow
     * suppliers for big elements.
     */

    /**
     * The entries table. Holds all elements.
     * <p>
     * The table length MUST be a power of 2. See {@link #mask}.
     */
    private Entry<E>[] table;

    /**
     * Number of elements in the cache.
     */
    private int size;

    /**
     * Cache of the table indexes mask.
     * <p>
     * Used to compute the index in the table of an object by operating
     * bitwise-AND operation on the object hash code and the mask. This
     * operation will give the right index because the length of the table is
     * ALWAYS power of 2, and the mask is ALWAYS the table length minus one.
     * <p>
     * For example:<br>
     * <code>table.length = 0b010000, mask = 0b001111</code><br>
     * This invariant allow fast computation of a key index in the table.
     */
    private int mask;

    /**
     * Cache for number of elements threshold before growing the table. When
     * number of elements is exceeding this number, a grow will be performed.
     * <p>
     * This value is always the table size times {@link #loadFactor}.
     */
    private int growThreshold;

    /**
     * Cache for number of elements threshold before shrinking the table. When
     * number of elements is diminishing this number, a shrink will be
     * performed.
     * <p>
     * This value is always {@link #growThreshold} (which is always the table
     * size times {@link #loadFactor}) divide by 4.
     */
    private int shrinkThreshold;

    /**
     * Load factor of the table. Control the size of the table compare to the
     * number of elements in it.
     * <p>
     * The load factor control when a resize will be performed on the table,
     * relative to the number of elements in it. The table will grow when number
     * of elements in it exceeds the table size times the loadFactor. The table
     * will shrink when number of elements in it is diminishing the table size
     * times the loadFactor divide by 4.
     * <p>
     * If the load factor is high, the table will be dense. If the load factor
     * is low, the table elements will be spread out.
     * <p>
     * If the load factor is high, the cache will use less memory but will be
     * slower. If the load factor is low, the cache will use more memory but
     * will be faster.
     */
    private final float loadFactor;

    /**
     * Lock used to synchronized between all methods.
     */
    private final ReentrantLock lock;

    /**
     * Minimum capacity which the table will not be shrinking less then.
     * <p>
     * MUST be a power of 2.
     */
    private static final int MINIMUM_SHRINK_CAPACITY = 8;

    /**
     * Object used to mask null keys.
     * 
     * @see #maskNull(Object)
     */
    private static final Object NULL = new Object();

    /**
     * Construct new HeavySynchronizedHashCache with default init capacity and
     * default load factor.
     */
    public HeavySynchronizedHashCache() {
	this(Hashs.DEFAULT_INIT_CAPACITY, Hashs.DEFAULT_LOAD_FACTOR);
    }

    /**
     * Construct new HeavySynchronizedHashCache with init capacity parameter and
     * default load factor.
     * 
     * @param initCapacity
     *            the initialize capacity of the cache, can be zero.
     * @throws IllegalArgumentException
     *             if init capacity is negative.
     */
    public HeavySynchronizedHashCache(final int initCapacity) {
	this(initCapacity, Hashs.DEFAULT_LOAD_FACTOR);
    }

    /**
     * Construct new HeavySynchronizedHashCache with init capacity parameter and
     * load factor parameter.
     * 
     * @param initCapacity
     *            the initialize capacity of the cache, can be zero.
     * @param loadFactor
     *            the load factor of the cache, see {@link #loadFactor}.
     * @throws IllegalArgumentException
     *             if init capacity is negative, load factor is negative, 0 or
     *             NaN.
     */
    public HeavySynchronizedHashCache(final int initCapacity, final float loadFactor) {
	if (initCapacity < 0)
	    throw new IllegalArgumentException("initCapacity < 0: " + initCapacity);
	if (loadFactor <= 0 || Float.isNaN(loadFactor))
	    throw new IllegalArgumentException("load factor must be greater then 0 and not NaN: " + loadFactor);

	final int capacity = Hashs.nextPowerOfTwo((int) (initCapacity / loadFactor));
	table = newTable(capacity);
	mask = capacity - 1;
	growThreshold = (int) (capacity * loadFactor);
	shrinkThreshold = growThreshold >> 2;
	this.loadFactor = loadFactor;
	lock = new ReentrantLock();
    }

    /*
     * (non-Javadoc)
     * 
     * @see bugav.util.containers.Cache#get(java.lang.Object)
     */
    @Override
    public E get(final K key) {
	// Compute hash
	final Object k = maskNull(key);
	final int hash = Hashs.hash(k);

	lock.lock();
	try {

	    // Search entry
	    for (Entry<E> p = table[hash & mask]; p != null; p = p.next) {
		if (hash == p.hash && k.equals(p.key)) {
		    lock.unlock();
		    synchronized (p) {
			return p.element;
		    }
		}
	    }
	    // No entry found with same key
	    return null;

	} finally {
	    if (lock.isHeldByCurrentThread()) {
		lock.unlock();
	    }
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see bugav.util.containers.Cache#add(java.lang.Object, java.lang.Object)
     */
    @Override
    public E add(final K key, final E element) {
	// Compute hash
	final Object k = maskNull(key);
	final int hash = Hashs.hash(k);

	lock.lock();
	try {

	    final int index = hash & mask;

	    // Check that such key doesn't already exist
	    final Entry<E> firstEntry = table[index];
	    for (Entry<E> p = firstEntry; p != null; p = p.next) {
		if (hash == p.hash && k.equals(p.key)) {
		    lock.unlock();
		    synchronized (p) {
			return p.element;
		    }
		}
	    }

	    // Insert new entry as first entry in list
	    table[index] = new Entry<>(k, hash, element, firstEntry);
	    grow();
	    return element;

	} finally {
	    if (lock.isHeldByCurrentThread()) {
		lock.unlock();
	    }
	}
    }

    /**
     * Get an element from the cache by it's key or add one if one doesn't
     * exist.
     * <p>
     * This is the main purpose of the HeavySynchronizedHashCache, allowing
     * heavy computations of the supplier without stopping other threads to
     * operate on the cache on other elements (with different keys).
     * <p>
     * This method is similar to {@link Map#computeIfAbsent(Object, Function)}.
     * <p>
     * 
     * @param key
     *            the key of the element.
     * @param supplier
     *            the supplier of the element if one doesn't exist in the cache.
     *            Can performed heavy operations without stopping other threads
     *            to operate on the cache.
     * @return the existing element or the one created from the supplier (if
     *         needed).
     */
    @Override
    public E getOrAdd(final K key, final Function<? super K, ? extends E> supplier) {
	// Compute hash
	final Object k = maskNull(key);
	final int hash = Hashs.hash(k);

	lock.lock();
	try {

	    final int index = hash & mask;

	    // Search entry
	    final Entry<E> firstEntry = table[index];
	    for (Entry<E> p = firstEntry; p != null; p = p.next) {
		if (hash == p.hash && k.equals(p.key)) {
		    lock.unlock();
		    synchronized (p) {
			return p.element;
		    }
		}
	    }

	    // Insert new entry as first entry in list
	    final Entry<E> entry = table[index] = new Entry<>(k, hash, firstEntry);
	    grow();

	    synchronized (entry) {
		lock.unlock();
		final E element = supplier.apply(key);
		entry.element = element;
		return element;
	    }

	} finally {
	    if (lock.isHeldByCurrentThread()) {
		lock.unlock();
	    }
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see bugav.util.containers.Cache#remove(java.lang.Object)
     */
    @Override
    public void remove(final K key) {
	// Compute hash
	final Object k = maskNull(key);
	final int hash = Hashs.hash(k);

	lock.lock();
	try {

	    final int index = hash & mask;

	    Entry<E> p = table[index];
	    Entry<E> prev = null;
	    while (p != null) {
		final Entry<E> next = p.next;
		if (hash == p.hash && k.equals(p.key)) {
		    if (prev == null) {
			table[index] = next;
		    } else {
			prev.next = next;
		    }
		    p.next = null;
		    p.key = null;
		    shrink();
		    break;
		}
		prev = p;
		p = next;
	    }
	} finally {
	    if (lock.isHeldByCurrentThread()) {
		lock.unlock();
	    }
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see bugav.util.containers.Cache#size()
     */
    @Override
    public int size() {
	lock.lock();
	try {
	    return size;
	} finally {
	    if (lock.isHeldByCurrentThread()) {
		lock.unlock();
	    }
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see bugav.util.containers.Cache#clear()
     */
    @Override
    public void clear() {
	lock.lock();
	try {

	    // Clear table
	    final Entry<E>[] tab = table;
	    for (int index = tab.length; index-- != 0;) {
		for (Entry<E> entry = tab[index]; entry != null;) {
		    final Entry<E> next = entry.next;
		    entry.next = null; // Help GC
		    entry.key = null; // Help GC
		    entry = next;
		}
		tab[index] = null;
	    }

	    // Reset size
	    size = 0;

	    // Shrink to minimum capacity
	    if (tab.length > MINIMUM_SHRINK_CAPACITY)
		resize(MINIMUM_SHRINK_CAPACITY); // Update thresholds and mask

	} finally {
	    if (lock.isHeldByCurrentThread()) {
		lock.unlock();
	    }
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	StringBuilder builder = new StringBuilder();
	final String separator = ", ";
	builder.append('[');

	lock.lock();
	try {

	    // Iterate over all elements and append them
	    final Entry<E>[] tab = table;
	    for (int i = tab.length; i-- != 0;) {
		for (Entry<E> entry = tab[i]; entry != null; entry = entry.next) {
		    final E element;
		    synchronized (entry) {
			element = entry.element;
		    }
		    builder.append(element);
		    builder.append(separator);
		}
	    }

	} finally {
	    if (lock.isHeldByCurrentThread()) {
		lock.unlock();
	    }
	}

	if (builder.lastIndexOf(separator) >= 0) {
	    // Had any elements, delete last separator
	    final int length = builder.length();
	    builder.delete(length - 2, length);
	}
	builder.append(']');
	return builder.toString();
    }

    /**
     * Increase size and check if resize is needed
     */
    private void grow() {
	if (++size >= growThreshold)
	    resize(table.length << 1);
    }

    /**
     * Decrease size and check if resize is needed
     */
    private void shrink() {
	if (--size <= shrinkThreshold) {
	    final int currentCapacity = table.length;
	    if (currentCapacity > MINIMUM_SHRINK_CAPACITY)
		resize(currentCapacity >> 1);
	}
    }

    /**
     * Resize the table to a new capacity.
     * 
     * @param newCapacity
     *            new table capacity. MUST be a power of 2.
     */
    private void resize(final int newCapacity) {
	final Entry<E>[] oldTable = table;
	final Entry<E>[] newTable = table = newTable(newCapacity);

	// Update thresholds and mask
	growThreshold = (int) (newCapacity * loadFactor);
	shrinkThreshold = growThreshold >> 2;
	final int newMask = mask = newCapacity - 1;

	// transfer all elements to newTable
	for (int oldIndex = oldTable.length; oldIndex-- != 0;) {
	    for (Entry<E> entry = oldTable[oldIndex]; entry != null;) {

		// Hold next entry before transferring entry. entry.next will be
		// irrelevant for the iteration after transferring current entry
		// to new table.
		final Entry<E> next = entry.next;
		final int newIndex = entry.hash & newMask;
		entry.next = newTable[newIndex];
		newTable[newIndex] = entry;
		entry = next;
	    }
	    oldTable[oldIndex] = null;
	}
    }

    /**
     * Create new table of entries.
     * <p>
     * 
     * @param <E>
     *            type of keys of the table's entries.
     * @param capacity
     *            the desire capacity of the table.
     * @return new table with the specified capacity.
     */
    @SuppressWarnings("unchecked")
    private static <E> Entry<E>[] newTable(final int capacity) {
	return new Entry[capacity];
    }

    /**
     * Mask object by replacing null will non null object.
     * <p>
     * Used to mask null keys.
     * 
     * @param o
     *            masked object
     * @return object quarantined to be not null.
     */
    private static Object maskNull(final Object o) {
	return o == null ? NULL : o;
    }

    /**
     * Inverted version of {@link #maskNull(Object)}.
     * 
     * @param o
     *            masked non null object
     * @return unmasked object, possible null.
     */
    private static Object unmaskNull(final Object o) {
	return o == NULL ? null : o;
    }

    private static class Entry<E> {

	Object key;
	final int hash;
	private E element;
	Entry<E> next;

	Entry(final Object key, final int hash, final Entry<E> next) {
	    this.key = key;
	    this.hash = hash;
	    this.next = next;
	}

	Entry(final Object key, final int hash, final E element, final Entry<E> next) {
	    this.key = key;
	    this.hash = hash;
	    this.element = element;
	    this.next = next;
	}

	@Override
	public String toString() {
	    synchronized (this) {
		return String.valueOf(unmaskNull(key)) + '=' + String.valueOf(element);
	    }
	}

    }

}