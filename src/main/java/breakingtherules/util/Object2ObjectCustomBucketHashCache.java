package breakingtherules.util;

import java.util.Objects;
import java.util.function.Function;

import breakingtherules.util.Hashs.Strategy;

/**
 * TODO javadoc
 *
 * @author Barak Ugav
 * @author Yishai Gronich
 *
 * @see Strategy
 *
 * @param <K>
 *            type of cache keys.
 * @param <E>
 *            type of the cache elements.
 */
public class Object2ObjectCustomBucketHashCache<K, E> implements Object2ObjectCache<K, E> {

    /*
     * Implementation notes.
     *
     * The Object2ObjectSoftCustomBucketHashCache is implemented by a bucket
     * hash table. In each cell in the table there is a bin (linked list of
     * entries) that contains all entries that fell to that cell.
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
     * The 'soft' behavior of the elements is obtained by the following
     * mechanism: When one of the elements doesn't get held anymore by external
     * strong reference, the entry push itself to a queue (SoftReference support
     * this behavior). The queue of the 'dead' elements get scanned in the
     * beginning of every public method and the 'dead' entry get removed from
     * the table.
     */

    /**
     * The entries table. Holds all elements.
     * <p>
     * The table length MUST be a power of 2. See {@link #mask}.
     */
    private Entry<K, E>[] table;

    /**
     * Flag that indicate if an element with a null key is contained in the
     * cache.
     */
    private boolean containsNull;

    /**
     * The reference to the element with the null key.
     * <p>
     * Always null if {@link #containsNull} is false.
     */
    private E nullElement;

    /**
     * Number of elements in the cache
     */
    private int size;

    /**
     * Cache of the table indexes mask.
     * <p>
     * Used to compute the index in the table of an object by operating
     * bitwise-AND operation on the object hash code and the mask. This
     * operation will give the right index because the length of the table is
     * ALWAYS power of 2, and the mask is ALWAYS the table length minus one. For
     * example:<br>
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
     * The strategy used by this cache.
     */
    private final Strategy<? super K> strategy;

    /**
     * Minimum capacity which the table will not be shrinking less then.
     * <p>
     * MUST be a power of 2.
     */
    private static final int MINIMUM_SHRINK_CAPACITY = 8;

    /**
     * Construct new Object2ObjectSoftCustomBucketHashCache with default init
     * capacity and default load factor.
     * <p>
     *
     * @param strategy
     *            The strategy used by this cache.
     * @throws NullPointerException
     *             if the strategy is null.
     */
    public Object2ObjectCustomBucketHashCache(final Strategy<? super K> strategy) {
	this(strategy, Hashs.DEFAULT_INIT_CAPACITY, Hashs.DEFAULT_LOAD_FACTOR);
    }

    /**
     * Construct new Object2ObjectSoftCustomBucketHashCache with init capacity
     * parameter and default load factor.
     * <p>
     *
     * @param strategy
     *            The strategy used by this cache.
     * @param initCapacity
     *            the initialize capacity of the cache, can be zero
     * @throws IllegalArgumentException
     *             if init capacity is negative.
     * @throws NullPointerException
     *             if the strategy is null.
     */
    public Object2ObjectCustomBucketHashCache(final Strategy<? super K> strategy, final int initCapacity) {
	this(strategy, initCapacity, Hashs.DEFAULT_LOAD_FACTOR);
    }

    /**
     * Construct new Object2ObjectSoftCustomBucketHashCache with init capacity
     * parameter and load factor parameter.
     * <p>
     *
     * @param strategy
     *            The strategy used by this cache.
     * @param initCapacity
     *            the initialize capacity of the cache, can be zero
     * @param loadFactor
     *            the load factor of the cache, see {@link #loadFactor}
     * @throws IllegalArgumentException
     *             if init capacity is negative, load factor is negative, 0 or
     *             NaN.
     * @throws NullPointerException
     *             if the strategy is null.
     */
    public Object2ObjectCustomBucketHashCache(final Strategy<? super K> strategy, final int initCapacity,
	    final float loadFactor) {
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
	containsNull = false;
	this.strategy = Objects.requireNonNull(strategy, "Null strategy");
    }

    /**
     * {@inheritDoc}
     *
     */
    @Override
    public E add(final K key, final E element) {
	if (key == null)
	    return addNull(element);

	final int hash = Hashs.mix(strategy.hashCode(key));
	final int index = hash & mask;

	// Check if an element with the same key is already in cache.
	final Entry<K, E> firstEntry = table[index];
	for (Entry<K, E> p = firstEntry; p != null; p = p.next)
	    if (hash == p.hash && strategy.equals(key, p.key))
		return p.element;

	// Insert new entry as first entry in list
	table[index] = new Entry<>(key, element, hash, firstEntry);
	grow();
	return element;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
	// Clear table
	final Entry<K, E>[] tab = table;
	for (int index = tab.length; index-- != 0;) {
	    for (Entry<K, E> entry = tab[index]; entry != null;) {
		final Entry<K, E> next = entry.next;
		entry.next = null; // help GC
		entry.key = null; // help GC
		entry.element = null; // help GC
		entry = next;
	    }
	    tab[index] = null;
	}

	// Reset size
	size = 0;

	// Shrink to minimum capacity
	if (tab.length > MINIMUM_SHRINK_CAPACITY)
	    resize(MINIMUM_SHRINK_CAPACITY); // Update thresholds and mask
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E get(final K key) {
	if (key == null)
	    return getNull();

	// Search entry
	final int hash = Hashs.mix(strategy.hashCode(key));
	for (Entry<K, E> p = table[hash & mask]; p != null; p = p.next)
	    if (hash == p.hash && strategy.equals(key, p.key))
		return p.element;

	// No entry found with same key
	return null;
    }

    /**
     * This implementation provide a faster alternative over the default
     * implementation.
     * <p>
     *
     * @see Object2ObjectCache#getOrAdd(Object, Function) for full documentation
     *      of the method.
     * @throws NullPointerException
     *             if the element provided by supplier (if needed) is null.
     *             Nulls elements are no allowed in soft cache.
     */
    @Override
    public E getOrAdd(final K key, final Function<? super K, ? extends E> supplier) {
	if (key == null)
	    return getOrAddNull(supplier);

	final int hash = Hashs.mix(strategy.hashCode(key));
	final int index = hash & mask;

	// Search entry
	final Entry<K, E> firstEntry = table[index];
	for (Entry<K, E> p = firstEntry; p != null; p = p.next)
	    if (hash == p.hash && strategy.equals(key, p.key))
		return p.element;

	// Not found, supply element
	final E element = supplier.apply(key);

	// Insert new entry as first entry in list
	table[index] = new Entry<>(key, element, hash, firstEntry);
	grow();
	return element;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(final K key) {
	if (key == null) {
	    removeNull();
	    return;
	}

	final int hash = Hashs.mix(strategy.hashCode(key));
	final int index = hash & mask;

	// Search and remove
	Entry<K, E> p = table[index];
	Entry<K, E> prev = null;
	while (p != null) {
	    final Entry<K, E> next = p.next;
	    if (hash == p.hash && strategy.equals(key, p.key)) {
		if (prev == null)
		    // First element
		    table[index] = next;
		else
		    // Not first element
		    prev.next = next;
		p.next = null; // help GC
		p.key = null; // help GC
		p.element = null; // help GC
		shrink();
		return;
	    }
	    prev = p;
	    p = next;
	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
	return size;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
	final StringBuilder builder = new StringBuilder();
	final String separator = ", ";
	builder.append('[');

	// Iterate over all elements and append them
	final Entry<K, E>[] tab = table;
	for (int i = tab.length; i-- != 0;)
	    for (Entry<K, E> entry = tab[i]; entry != null; entry = entry.next) {
		builder.append(entry.element);
		builder.append(separator);
	    }
	if (containsNull)
	    builder.append(nullElement);
	else if (builder.lastIndexOf(separator) >= 0) {
	    // Had any elements, delete last separator
	    final int length = builder.length();
	    builder.delete(length - 2, length);
	}
	builder.append(']');
	return builder.toString();
    }

    /**
     * Add an element with null key.
     *
     * @param element
     *            the added element.
     * @return the existing element or the added if one doesn't already exist.
     */
    private E addNull(final E element) {
	if (containsNull)
	    return nullElement;
	nullElement = element;
	containsNull = true;
	grow();
	return element;
    }

    /**
     * Get the element with the null key.
     *
     * @return element that it;s key is null or null if one doesn't exist.
     */
    private E getNull() {
	return containsNull ? nullElement : null;
    }

    /**
     * Get or add element with null key.
     *
     * @param supplier
     *            the supplier of the element. Used only if element is null in
     *            the cache.
     * @return the existing element or the supplier element if one doesn't
     *         exist.
     */
    private E getOrAddNull(final Function<? super K, ? extends E> supplier) {
	if (containsNull)
	    return nullElement;
	final E element = supplier.apply(null);
	nullElement = element;
	containsNull = true;
	grow();
	return element;
    }

    /**
     * Increase size and check if resize is needed
     */
    private void grow() {
	if (++size >= growThreshold)
	    resize(table.length << 1);
    }

    /**
     * Remove the element that his key is null.
     */
    private void removeNull() {
	if (containsNull) {
	    containsNull = false;
	    nullElement = null;
	    shrink();
	}
    }

    /**
     * Resize the table to a new capacity.
     *
     * @param newCapacity
     *            new table capacity. MUST be a power of 2.
     */
    private void resize(final int newCapacity) {
	final Entry<K, E>[] oldTable = table;
	final Entry<K, E>[] newTable = table = newTable(newCapacity);

	// Update thresholds and mask
	growThreshold = (int) (newCapacity * loadFactor);
	shrinkThreshold = growThreshold >> 2;
	final int newMask = mask = newCapacity - 1;

	// transfer all elements to newTable
	for (int oldIndex = oldTable.length; oldIndex-- != 0;) {
	    for (Entry<K, E> entry = oldTable[oldIndex]; entry != null;) {

		// Hold next entry before transferring entry. entry.next will be
		// irrelevant for the iteration after transferring current entry
		// to new table.
		final Entry<K, E> next = entry.next;

		// Transfer element to new table
		final int newIndex = entry.hash & newMask;
		entry.next = newTable[newIndex];
		newTable[newIndex] = entry;
		entry = next;
	    }
	    oldTable[oldIndex] = null;
	}
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
     * Create new table of entries.
     * <p>
     *
     * @param <K>
     *            type of keys of the table's entries.
     * @param <E>
     *            type of elements of the table's entries.
     * @param capacity
     *            the desire capacity of the table.
     * @return new table with the specified capacity.
     */
    @SuppressWarnings("unchecked")
    private static <K, E> Entry<K, E>[] newTable(final int capacity) {
	return new Entry[capacity];
    }

    /**
     * Entry of cached element in the {@link Object2ObjectCustomBucketHashCache}
     * .
     * <p>
     * The entries are save as a bin (one way linked list) in each table cell,
     * and last entry at the list {@link #next} field is null.
     * <p>
     *
     * @author Barak Ugav
     * @author Yishai Gronich
     *
     * @param <K>
     *            type of key.
     * @param <E>
     *            type of element.
     */
    private static class Entry<K, E> {

	/**
	 * The entry's key.
	 */
	private K key;

	/**
	 * The entry's element.
	 */
	private E element;

	/**
	 * Cache for the key hash.
	 */
	private final int hash;

	/**
	 * The next entry at the entry linked list, or null if this entry is the
	 * last entry in the list.
	 */
	private Entry<K, E> next;

	/**
	 * Construct new entry
	 *
	 * @param key
	 *            the entry key
	 * @param element
	 *            the entry element
	 * @param hash
	 *            hash of the key
	 * @param next
	 *            next entry.
	 * @throws NullPointerException
	 *             if the element is null.
	 */
	public Entry(final K key, final E element, final int hash, final Entry<K, E> next) {
	    this.key = key;
	    this.element = element;
	    this.hash = hash;
	    this.next = next;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
	    String st = String.valueOf(element);
	    if (next != null)
		st += " -> " + next.toString();
	    return st;
	}

    }

}