package breakingtherules.utilities;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.IntFunction;

/**
 *
 * @author Barak Ugav
 * @author Yishai Gronich
 *
 * @param <E>
 *            type of cached elements in the cache.
 */
public class Int2ObjectOpenAddressingHashCache<E> implements Int2ObjectCache<E> {

    /*
     * The Int2ObjectOpenAddressingHashCache is implemented by a hash table of
     * closed hashing (all keys and elements are in the main tables) managed by
     * open addressing.
     *
     * The hash table is implemented by two arrays - one for the keys and one
     * for the elements. Each key in index 'x' in the keys array is associated
     * with the element in 'elements[x]'.
     *
     * The hash table is of close hashing, meaning all keys and elements are
     * store in the main arrays (different that traditional bucket hash tables,
     * where the keys and elements are store in a linked lists pointed from the
     * main tables cells). When using closed hashing, one must use open
     * addressing strategy - the implementation uses a simple one: each key (and
     * is associated element) is stored in the first free slot following it's
     * initialized slot (calculated from the key's hash modulo the table size).
     * When inserting new key (and element), one need to search the first free
     * slot, and it's done by iterating over the keys array until finding an
     * empty slot. When removing a key, one need to 'shift' the existing keys to
     * their correct slot.
     */

    /**
     * The array of the keys. Contains all current keys.
     * <p>
     * The size of this array is always {@link #n}.
     * <p>
     * In each cell, the stored key is never 0. If it's 0 meaning the cell is
     * unused.
     */
    private int[] keys;

    /**
     * The array of the elements. Contains all current elements.
     * <p>
     * The size of this array is always {@link #n} + 1 (for the element that his
     * mapping key is 0).
     */
    private E[] elements;

    /**
     * The number of elements in the cache.
     */
    private int size;

    /**
     * Flag that indicates if the cache contains a mapping from the key 0 to any
     * element.
     */
    private boolean containsKey0;

    /**
     * The size of the table.
     */
    private int n;

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
     * This value is always {@link #n} times {@link #loadFactor}.
     */
    private int growThreshold;

    /**
     * Cache for number of elements threshold before shrinking the table. When
     * number of elements is diminishing this number, a shrink will be
     * performed.
     * <p>
     * This value is always {@link #growThreshold} (which is always {@link #n}
     * times {@link #loadFactor}) divide by 4.
     */
    private int shrinkThreshold;

    /**
     * Load factor of the table. Control the size of the table compare to the
     * number of elements in it.
     * <p>
     * MUST be in range (0, 1], so there are always at least one slot without an
     * entry in it - so searchers will now when to stop.
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
     * Minimum capacity which the table will not be shrinking less then.
     * <p>
     * MUST be a power of 2.
     */
    private static final int MINIMUM_SHRINK_CAPACITY = 8;

    /**
     * Construct new Int2ObjectOpenAddressingHashCache.
     * <p>
     * Uses {@link Hashs#DEFAULT_INIT_CAPACITY} and
     * {@link Hashs#DEFAULT_LOAD_FACTOR}.
     */
    public Int2ObjectOpenAddressingHashCache() {
	this(Hashs.DEFAULT_INIT_CAPACITY, Hashs.DEFAULT_LOAD_FACTOR);
    }

    /**
     * Construct new Int2ObjectOpenAddressingHashCache.
     * <p>
     * Uses {@link Hashs#DEFAULT_LOAD_FACTOR}.
     *
     * @param initCapacity
     *            the initialize capacity of the cache.
     * @throws IllegalArgumentException
     *             if the init capacity is negative.
     */
    public Int2ObjectOpenAddressingHashCache(final int initCapacity) {
	this(initCapacity, Hashs.DEFAULT_LOAD_FACTOR);
    }

    /**
     * Construct new Int2ObjectOpenAddressingHashCache.
     *
     * @param initCapacity
     *            the initialize capacity of the cache.
     * @param loadFactor
     *            the load factor used by the cache. Should be in range (0, 1].
     *            See {@link #loadFactor}.
     * @throws IllegalArgumentException
     *             if the init capacity is negative, or the load factor is not
     *             in the range (0, 1] (or NaN).
     */
    @SuppressWarnings("unchecked")
    public Int2ObjectOpenAddressingHashCache(final int initCapacity, final float loadFactor) {
	if (initCapacity < 0)
	    throw new IllegalArgumentException("initCapacity < 0: " + initCapacity);
	if (loadFactor <= 0 || loadFactor > 1 || Float.isNaN(loadFactor))
	    throw new IllegalArgumentException("load factor must be in range (0, 1] and not NaN: " + loadFactor);

	n = Hashs.nextPowerOfTwo((int) (initCapacity / loadFactor));
	mask = n - 1;
	growThreshold = (int) (n * loadFactor);
	shrinkThreshold = growThreshold >> 2;
	this.loadFactor = loadFactor;

	keys = new int[n];
	elements = (E[]) new Object[n + 1];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E add(final int key, final E element) {
	final int[] keysTab = this.keys;
	final int m = mask;

	if (key == 0) {
	    if (containsKey0)
		return elements[n];
	    containsKey0 = true;
	    elements[n] = element;
	    if (++size >= growThreshold)
		resize(n << 1);
	    return element;
	}
	int curr, pos;
	if ((curr = keysTab[pos = Hashs.mix(key) & m]) != 0)
	    do
		if (curr == key)
		    return elements[pos];
	    while ((curr = keysTab[pos = pos + 1 & m]) != 0);

	// Not found, insert in the first free slot that was found.
	keysTab[pos] = key;
	elements[pos] = element;

	// Grow if one is needed
	if (++size >= growThreshold)
	    resize(n << 1);
	return element;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public void clear() {
	if (size == 0)
	    return; // Do nothing

	size = 0;
	containsKey0 = false;

	if (n > MINIMUM_SHRINK_CAPACITY) {
	    // Shrink if the array is bigger then minimum capacity.
	    keys = new int[MINIMUM_SHRINK_CAPACITY];
	    elements = (E[]) new Object[MINIMUM_SHRINK_CAPACITY];
	    n = MINIMUM_SHRINK_CAPACITY;
	    mask = MINIMUM_SHRINK_CAPACITY - 1;
	    growThreshold = (int) (MINIMUM_SHRINK_CAPACITY / loadFactor);
	    shrinkThreshold = growThreshold >> 2;

	} else {
	    Arrays.fill(keys, 0);
	    Arrays.fill(elements, null);
	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object o) {
	if (o == this)
	    return true;
	if (!(o instanceof Int2ObjectCache))
	    return false;

	final Int2ObjectCache<?> other = (Int2ObjectCache<?>) o;
	if (size != other.size())
	    return false;
	final E[] elementsTab = elements;
	if (containsKey0 && !Objects.equals(elementsTab[n], other.get(0)))
	    return false;
	final int[] keysTab = keys;
	for (int j = containsKey0 ? size - 1 : size, i = n; j-- != 0;) {
	    do { // Spin until any entry is found.
	    } while (keysTab[--i] == 0);
	    if (!Objects.equals(elementsTab[i], other.get(keysTab[i])))
		return false;
	}
	return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E get(final int key) {
	if (key == 0)
	    return containsKey0 ? elements[n] : null;

	final int[] keysTab = this.keys;
	final int m = mask;
	int pos, curr;
	if ((curr = keysTab[pos = Hashs.mix(key) & m]) != 0)
	    do
		if (key == curr)
		    return elements[pos];
	    while ((curr = keysTab[pos = pos + 1 & m]) != 0);

	// Not found
	return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public E getOrAdd(final int key, final IntFunction<? extends E> cachingFunction) {
	if (key == 0) {
	    if (containsKey0)
		return elements[n];

	    // Not found, add.
	    final E element = elements[n] = cachingFunction.apply(0);
	    containsKey0 = true;
	    if (++size >= growThreshold)
		resize(n << 1);
	    return element;
	}

	final int[] keysTab = this.keys;
	final int m = mask;
	int pos, curr;
	if ((curr = keysTab[pos = Hashs.mix(key) & m]) != 0)
	    do
		if (key == curr)
		    return elements[pos];
	    while ((curr = keysTab[pos = pos + 1 & m]) != 0);

	// Not found, add.
	final E element = cachingFunction.apply(key);
	keysTab[pos] = key;
	elements[pos] = element;

	// Grow if one is needed.
	if (++size >= growThreshold)
	    resize(n << 1);
	return element;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
	int h = 0;
	final E[] elementsTab = elements;
	final int[] keysTab = keys;
	if (containsKey0)
	    h ^= Objects.hashCode(elementsTab[n]);
	for (int j = containsKey0 ? size - 1 : size, i = n; j-- != 0;) {
	    do { // Spin until any entry is found.
	    } while (keysTab[--i] == 0);
	    h ^= keysTab[i];
	    h ^= Objects.hashCode(elementsTab[i]);
	}
	return h;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(final int key) {
	if (key == 0) {
	    if (containsKey0) {
		containsKey0 = false;
		elements[n] = null;

		// Shrink if one is needed
		if (--size < shrinkThreshold && n > MINIMUM_SHRINK_CAPACITY)
		    resize(n >> 1);
	    }
	    return;
	}

	final int m = mask;
	int curr, pos;
	final int[] keyTabs = keys;
	if ((curr = keyTabs[pos = Hashs.mix(key) & m]) != 0)
	    do
		if (key == curr) {
		    final E[] elementsTab = elements;
		    elementsTab[pos] = null;

		    // Shift keys after remove
		    shiftLoop: for (int last, slot;;) {
			for (pos = (last = pos) + 1 & m;; pos = pos + 1 & m) {
			    if ((curr = keyTabs[pos]) == 0) {
				keyTabs[last] = 0;
				elementsTab[last] = null;
				break shiftLoop;
			    }
			    slot = Hashs.mix(curr) & m;
			    if (last <= pos ? last >= slot || slot > pos : last >= slot && slot > pos)
				break;
			}
			keyTabs[last] = curr;
			elementsTab[last] = elementsTab[pos];
		    }

		    // Shrink if one is needed
		    if (--size < shrinkThreshold && n > MINIMUM_SHRINK_CAPACITY)
			resize(n >> 1);
		    return;
		}
	    while ((curr = keyTabs[pos = pos + 1 & m]) != 0);

	// Not found.
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
	builder.append('{');

	final E[] elementsTab = elements;
	final int[] keysTab = keys;
	if (containsKey0)
	    builder.append(0).append(" => ").append(elementsTab[n]).append(", ");
	for (int j = containsKey0 ? size - 1 : size, i = n; j-- != 0;) {
	    do { // Spin until any entry is found.
	    } while (keysTab[--i] == 0);
	    builder.append(keysTab[i]).append(" => ").append(elementsTab[i]).append(", ");
	}

	// Remove last delimiter if exists
	if (builder.lastIndexOf(", ") >= 0)
	    builder.delete(builder.length() - ", ".length(), builder.length());

	builder.append('}');
	return builder.toString();
    }

    /**
     * Resize the table to a new capacity.
     *
     * @param newCapacity
     *            new table capacity. MUST be a power of 2.
     */
    private void resize(final int newCapacity) {
	final int[] oldKeys = keys;
	final E[] oldElements = elements;
	final int newKeys[] = keys = new int[newCapacity];
	@SuppressWarnings("unchecked")
	final E[] newElements = elements = (E[]) new Object[newCapacity + 1];

	final int m = mask = newCapacity - 1;
	for (int newIndex, j = containsKey0 ? size - 1 : size, oldIndex = n; j-- != 0;) {
	    do { // Spin until any entry is found in the old array.
	    } while (oldKeys[--oldIndex] == 0);

	    if (newKeys[newIndex = Hashs.mix(oldKeys[oldIndex]) & m] != 0)
		do { // Spin until the first free slot is found in the new
		     // array.
		} while (newKeys[newIndex = newIndex + 1 & m] != 0);

	    newKeys[newIndex] = oldKeys[oldIndex];
	    newElements[newIndex] = oldElements[oldIndex];
	}
	newElements[newCapacity] = oldElements[n];
	n = newCapacity;
	growThreshold = (int) (newCapacity * loadFactor);
	shrinkThreshold = growThreshold >> 2;
    }

}
