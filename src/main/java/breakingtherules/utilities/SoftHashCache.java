package breakingtherules.utilities;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.function.Function;

/**
 * Cache that provides search of cached element by key and adding element by
 * key. Elements are stored by soft reference.
 * <p>
 * When there is some memory demand and no more strong references to an element,
 * it will be automatically removed from the cache, so cached elements are
 * mostly elements that are held by other references too.
 * <p>
 * This class has the same performance as hash map and it depends strongly on
 * the keys {@link Object#hashCode()} method.
 * <p>
 * When using this class, it's essential that the keys are not the same objects
 * as the elements - if this is the case, the cache itself will hold strong
 * reference to the keys, to the elements as well(as they are the same) and
 * therefore the elements will never be cleaned from cache and memory by the GC.
 * <p>
 * Null elements are not allowed, because there will be no way to determine when
 * to remove them from cache.
 * <p>
 * The cache will resize itself (grow and shrink) according to the number of
 * elements in it and the {@link #loadFactor}.
 * 
 * @author Barak Ugav
 * @author Yishai Gronich
 * 
 * @see SoftReference
 * @see WeakHashCache
 * 
 * @param <K>
 *            type of key of the cache
 * @param <E>
 *            type of cached elements
 */
public class SoftHashCache<K, E> implements Cache<K, E> {

    /*
     * Implementation notes.
     * 
     * The SoftHashCache is implemented by a bucket hash table. In each cell in
     * the table there is a bin (linked list of entries) that contains all
     * entries that fell to that cell.
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
     * strong reference and there is a demand for memory (as described in the
     * SoftReference documentations), the entry push itself to a queue
     * (SoftReference support this behavior). The queue of the 'dead' elements
     * get scanned in the beginning of every public method and the 'dead' entry
     * get removed from the table.
     */

    /**
     * The entries table. Holds all elements.
     * <p>
     * The table length MUST be a power of 2. See {@link #mask}.
     */
    private Entry[] table;

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
     * Queue used to determine which elements was queued and needed to be
     * removed from the table.
     */
    private final ReferenceQueue<Object> queue;

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
     * Construct new SoftHashCache with default init capacity and default load
     * factor.
     */
    public SoftHashCache() {
	this(Hashs.DEFAULT_INIT_CAPACITY, Hashs.DEFAULT_LOAD_FACTOR);
    }

    /**
     * Construct new SoftHashCache with init capacity parameter and default load
     * factor.
     * 
     * @param initCapacity
     *            the initialize capacity of the cache, can be zero
     * @throws IllegalArgumentException
     *             if init capacity is negative
     */
    public SoftHashCache(final int initCapacity) {
	this(initCapacity, Hashs.DEFAULT_LOAD_FACTOR);
    }

    /**
     * Construct new SoftHashCache with init capacity parameter and load factor
     * parameter.
     * 
     * @param initCapacity
     *            the initialize capacity of the cache, can be zero
     * @param loadFactor
     *            the load factor of the cache, see {@link #loadFactor}
     * @throws IllegalArgumentException
     *             if init capacity is negative, load factor is negative, 0 or
     *             NaN.
     */
    public SoftHashCache(final int initCapacity, final float loadFactor) {
	if (initCapacity < 0)
	    throw new IllegalArgumentException("initCapacity < 0: " + initCapacity);
	if (loadFactor <= 0 || Float.isNaN(loadFactor))
	    throw new IllegalArgumentException("load factor must be greater then 0 and not NaN: " + loadFactor);

	final int capacity = Hashs.nextPowerOfTwo((int) (initCapacity / loadFactor));
	table = new Entry[capacity];
	mask = capacity - 1;
	growThreshold = (int) (capacity * loadFactor);
	shrinkThreshold = growThreshold >> 2;
	this.loadFactor = loadFactor;
	queue = new ReferenceQueue<>();
    }

    /**
     * Get a cached element by it's key.
     * 
     * @param key
     *            the element's key
     * @return the cached element or null if non found.
     */
    @Override
    @SuppressWarnings("unchecked")
    public E get(final K key) {
	// Compute hash
	final Object k = maskNull(key);
	final int hash = Hashs.hash(k);

	// Clean cache, delayed as possible, so GC have more time to act.
	cleanCache();

	// Search entry
	for (Entry p = table[hash & mask]; p != null; p = p.next)
	    if (hash == p.hash && k.equals(p.key))
		/*
		 * Entry found, no need to check if it's element is a dead
		 * reference because two reasons. First of all, if there are
		 * only one entry with the specified key in the map, if the
		 * element is dead it will return null (which is equivalent to
		 * it not contained in the cache). Secondly, if there is more
		 * then one entry with the specified key, which can happen if
		 * only one of the entries contains alive element (see add(K, E)
		 * implementation), if there is an entry with an alive element
		 * between those entries, it will always be the first one
		 * because the insertion of the entries is always to the
		 * beginning of the linked list (when transferring entries to
		 * new table in resize the order is reversed. But then dead
		 * reference are eliminated, so for each key is guaranteed that
		 * there is at most one entry that match it).
		 */
		return (E) p.get();

	// No entry found with same key
	return null;
    }

    /**
     * Add new element to cache
     * 
     * @param key
     *            the element key
     * @param element
     *            the element
     * @throws NullPointerException
     *             if element is null. Null elements are not allowed in soft
     *             cache because the cache will not be able to determinate when
     *             to remove them, see {@link SoftReference}.
     * @throws IllegalArgumentException
     *             if an element with the same key is already in the cache.
     */
    @Override
    public E add(final K key, final E element) {
	if (element == null)
	    throw new NullPointerException("Nulls elements are not allowed in soft cache");

	// Compute hash
	final Object k = maskNull(key);
	final int hash = Hashs.hash(k);

	// Clean cache, delayed as possible, so GC have more time to act.
	cleanCache();

	// Compute index in table, MUST happen after clearCache() because shrink
	// may be caused and may change the mask.
	final int index = hash & mask;

	// Check that such key doesn't already exist
	final Entry firstEntry = table[index];
	for (Entry p = firstEntry; p != null; p = p.next) {
	    if (hash == p.hash && k.equals(p.key)) {
		@SuppressWarnings("unchecked")
		final E existing = (E) p.get();
		if (p.get() != null)
		    return existing;
		/*
		 * If the program reached this part of the code, that mean that
		 * p's elements is a dead reference and we can remove it. We
		 * choose not to do it here because that will require the
		 * previous entry (so another entry iterator is required). This
		 * scenario is not very likely because cleanCache() was called
		 * already in this method. We prefer to leave the dead reference
		 * as is and remove it in the next cleaning and not adding
		 * another iterator here for performance (this method may be
		 * called a lot).
		 */
	    }
	}

	// Insert new entry as first entry in list
	table[index] = new Entry(k, element, queue, hash, firstEntry);
	grow();
	return element;
    }

    @Override
    public E getOrAdd(final K key, final Function<? super K, ? extends E> supplier) {
	// Compute hash
	final Object k = maskNull(key);
	final int hash = Hashs.hash(k);
	final int index = hash & mask;

	// Clean cache, delayed as possible, so GC have more time to act.
	cleanCache();

	// Search entry
	final Entry firstEntry = table[index];
	for (Entry p = firstEntry; p != null; p = p.next) {
	    if (hash == p.hash && k.equals(p.key)) {
		@SuppressWarnings("unchecked")
		final E elm = (E) p.get();
		if (elm != null)
		    return elm;
	    }
	}

	// Not found, insert new entry as first entry in list
	final E element = supplier.apply(key);
	table[index] = new Entry(k, element, queue, hash, firstEntry);
	grow();
	return element;
    }

    /**
     * Remove a cashed element by it's key.
     * 
     * @param key
     *            the element's key
     */
    @Override
    public void remove(final K key) {
	// Compute hash
	final Object k = maskNull(key);
	final int hash = Hashs.hash(k);

	// Clean cache, delayed as possible, so GC have more time to act.
	cleanCache();

	// Compute index in table, MUST happen after clearCache() because shrink
	// may be caused and may change the mask.
	final int index = hash & mask;

	// Check if element is first in his list
	Entry p = table[index];
	if (p == null)
	    return;
	if (hash == p.hash && k.equals(p.key)) {
	    // Element is first in this list, remove and shrink
	    table[index] = p.next;
	    p.next = null; // Help GC
	    p.key = null; // Help GC
	    shrink();
	    return;
	}

	// Element is not first in his list, search it
	Entry prev;
	while ((p = (prev = p).next) != null) {
	    if (hash == p.hash && k.equals(p.key)) {
		// Element found, remove and shrink
		prev.next = p.next;
		p.next = null; // Help GC
		p.key = null; // Help GC
		shrink();
		return;
	    }
	}
    }

    /**
     * Get the number of cached elements.
     * <p>
     * Used mostly for testing.
     * 
     * @return number of cached elements.
     */
    @Override
    public int size() {
	cleanCache();
	return size;
    }

    /**
     * Clear the entire cache from all elements.
     */
    @Override
    public void clear() {
	// Clear dead elements queue
	while (queue.poll() != null) {
	}

	// Clear table
	final Entry[] tab = table;
	for (int index = tab.length; index-- != 0;) {
	    for (Entry entry = tab[index]; entry != null;) {
		final Entry next = entry.next;
		entry.clear();
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

	// Clear dead elements queue if some already added
	while (queue.poll() != null) {
	}
    }

    /**
     * Clean the cache from dead references.
     * <p>
     * This method doesn't HAVE to be called by the cache user to keep the cache
     * clean. If the user doesn't call this method, no errors will occurs
     * because of it. This method is frequency called by the internal
     * implementation of the cache, but it is visible to the user so he can free
     * memory if he knows a lot of the elements are already dead reference.
     */
    public void cleanCache() {
	// Poll from dead entries queue until it's empty. Remove each entry of
	// dead element from the table.
	for (Entry entry; (entry = (Entry) queue.poll()) != null;) {
	    synchronized (queue) {
		final int index = entry.hash & mask;
		Entry p = table[index];
		/*
		 * Search the entry in it's list. If found, remove it, if not,
		 * do nothing. The scenario that the entry is not found means
		 * that it was already removed, this can happen if the dead
		 * element's entry was detected already during other operation,
		 * for example, during resize.
		 */
		if (p == entry) {
		    // Entry is the first one in it's list, remove and shrink.
		    table[index] = p.next;
		    shrink();

		} else if (p == null) {
		    // Already removed.
		    continue;

		} else {
		    // The entry is not the first entry in it's list, search it
		    Entry prev;
		    while ((p = (prev = p).next) != null) {
			if (p == entry) {
			    // Entry found, remove and shrink
			    prev.next = p.next;
			    shrink();
			    break;
			}
		    }
		    // Entry was not found, already removed.
		}

		entry.key = null; // Help GC
		entry.next = null; // Help GC
	    }
	}
    }

    /**
     * Get a string representation of all elements in cache.
     * 
     * @return String representation of all elements in cache.
     */
    @Override
    public String toString() {
	StringBuilder builder = new StringBuilder();
	final String separator = ", ";
	builder.append('[');

	// Clean cache, delayed as possible, so GC have more time to act.
	cleanCache();

	// Iterate over all elements and append them
	final Entry[] tab = table;
	for (int i = tab.length; i-- != 0;) {
	    for (Entry entry = tab[i]; entry != null; entry = entry.next) {
		final Object element = entry.get();
		if (element == null)
		    // If element is already dead reference, ignore it
		    continue;
		builder.append(element);
		builder.append(separator);
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
	final Entry[] oldTable = table;
	final Entry[] newTable = table = new Entry[newCapacity];

	// Update thresholds and mask
	growThreshold = (int) (newCapacity * loadFactor);
	shrinkThreshold = growThreshold >> 2;
	final int newMask = mask = newCapacity - 1;

	// transfer all elements to newTable
	for (int oldIndex = oldTable.length; oldIndex-- != 0;) {
	    for (Entry entry = oldTable[oldIndex]; entry != null;) {

		// Hold next entry before transferring entry. entry.next will be
		// irrelevant for the iteration after transferring current entry
		// to new table.
		final Entry next = entry.next;

		if (entry.get() == null) {
		    // If we encounter dead reference, don't transfer it to new
		    // table - use the opportunity to remove it.
		    entry.clear();
		    entry.next = null; // Help GC
		    entry.key = null; // Help GC
		    size--;

		} else {
		    // Element is still alive, transfer him to new table
		    final int newIndex = entry.hash & newMask;
		    entry.next = newTable[newIndex];
		    newTable[newIndex] = entry;
		}
		entry = next;
	    }
	    oldTable[oldIndex] = null;
	}

	/*
	 * If enough dead elements were detected through the transferring, a
	 * shrink may be required. This scenario is rare but it's possible.
	 */
	if (size <= shrinkThreshold && newCapacity > MINIMUM_SHRINK_CAPACITY) {
	    /*
	     * In rare case shrinking will be required more that one time,
	     * shrink to final capacity in advance. This scenario can happen
	     * when the cache was initialized with big capacity, small number of
	     * elements was inserted and then a shrink was performed.
	     */
	    int numberOfShrinks = 1;
	    int newShrinkedCapacity = newCapacity >> 1;
	    int newShrinkThreshold = (int) (newShrinkedCapacity * loadFactor);
	    while (size <= newShrinkThreshold && newShrinkedCapacity > MINIMUM_SHRINK_CAPACITY) {
		newShrinkedCapacity >>= 1;
		newShrinkThreshold = (int) (newShrinkedCapacity * loadFactor);
		numberOfShrinks++;
	    }
	    resize(newCapacity >> numberOfShrinks);
	}
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
     * Entry of cached element in the {@link SoftHashCache}.
     * <p>
     * The entries are stored as a bin (one way linked list) in each table cell,
     * and last entry at the list {@link #next} field is null.
     * <p>
     * The key is saved as a field and the element itself is saved via the super
     * class {@link SoftReference}. When there is demand for memory and no more
     * strong references to the element the {@link SoftHashCache} will remove
     * the entry from the table.
     *
     */
    private static class Entry extends SoftReference<Object> {

	/**
	 * The entry key.
	 */
	private Object key;

	/**
	 * Cache for the key hash.
	 */
	private final int hash;

	/**
	 * The next entry at the entry linked list, or null if this entry is the
	 * last entry in the list.
	 */
	private Entry next;

	/**
	 * Construct new entry
	 * 
	 * @param key
	 *            the entry key
	 * @param element
	 *            the entry element
	 * @param queue
	 *            the ReferenceQueue which this entry should add itself when
	 *            the element become dead reference
	 * @param hash
	 *            hash of the key
	 * @param next
	 *            next entry
	 */
	public Entry(final Object key, final Object element, final ReferenceQueue<Object> queue, final int hash,
		final Entry next) {
	    super(element, queue);
	    this.key = key;
	    this.hash = hash;
	    this.next = next;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 * 
	 * This method is not called at all. Used to debug.
	 */
	@Override
	public String toString() {
	    String st = String.valueOf(get());
	    if (next != null)
		st += " -> " + next.toString();
	    return st;
	}

    }

}
