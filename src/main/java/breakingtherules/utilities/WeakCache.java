package breakingtherules.utilities;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

public class WeakCache<K, V> {

    /**
     * The entries table. Holds all elements.
     */
    private Entry[] table;

    /**
     * Number of elements in the cache
     */
    private int size;

    /**
     * Cache for number of elements threshold. When number of elements is
     * exceeding this number, a resize will be performed.
     */
    private int threshold;

    /**
     * Cache of the table mask
     */
    private int mask;

    /**
     * Factor of the table size. The table will be resized after exceeding it.
     */
    private final float loadFactor;

    /**
     * Queue used to determine which elements was queued and needed to be
     * removed from the table.
     */
    private final ReferenceQueue<Object> queue;

    /** 2<sup>32</sup> &middot; &phi;, &phi; = (&#x221A;5 &minus; 1)/2. */
    private static final int INT_PHI = 0x9E3779B9;

    public WeakCache() {
	this(16, .75f);
    }

    public WeakCache(final int initCapacity) {
	this(initCapacity, .75f);
    }

    public WeakCache(final int initCapacity, final float loadFactor) {
	if (initCapacity < 0)
	    throw new IllegalArgumentException("init capacity < 0");
	if (loadFactor <= 0 || Float.isNaN(loadFactor))
	    throw new IllegalArgumentException("load factor must be greater then 0 and not NaN");
	final int capacity = nextPowerOfTwo((int) (initCapacity / loadFactor));
	table = new Entry[capacity];
	mask = capacity - 1;
	threshold = (int) (capacity * loadFactor);
	this.loadFactor = loadFactor;
	queue = new ReferenceQueue<>();
    }

    @SuppressWarnings("unchecked")
    public V get(final K key) {
	if (key == null)
	    return null;
	final int hash = hash(key);
	cleanCache();
	for (Entry p = table[hash & mask]; p != null; p = p.next)
	    if (hash == p.hash && key.equals(p.key))
		return (V) p.get();
	return null;
    }

    public void put(final K key, final V value) {
	if (key == null || value == null)
	    throw new NullPointerException("Nulls are not allowed in weak cache");
	final int hash = hash(key);
	final Entry e = new Entry(key, value, queue, hash);
	final int i = hash & mask;
	cleanCache();
	Entry p = table[i];
	if (p == null) {
	    // No entries in table cell, put entry as first
	    table[i] = e;
	} else {
	    // Some entries are in the table cell. First check for key
	    // duplication, then put entry at the end of the entries list
	    Entry prev = null;
	    for (; p != null; p = (prev = p).next)
		if (hash == p.hash && key.equals(p.key))
		    if (p.get() != null)
			throw new IllegalArgumentException("key is already in the cache");
	    prev.next = e;
	}

	// Increase size and grow if needed
	if (++size >= threshold)
	    grow();

    }

    public void cleanCache() {
	for (Entry e; (e = (Entry) queue.poll()) != null;) {
	    synchronized (queue) {
		final int i = e.hash & mask;
		Entry p = table[i];
		if (p == e) {
		    // e is first entry
		    table[i] = p.next;

		    // Decrease size and shrink if needed
		    size--;
		} else {
		    // e is not the first entry
		    Entry prev = p;
		    while (p != null) {
			p = p.next;
			final Entry next = p.next;
			if (p == e) {
			    prev.next = next;
			    e.clear();
			    e.key = null; // Help GC

			    // Decrease size and shrink if needed
			    size--;
			    break;
			}
			prev = p;
		    }
		}
	    }
	}
    }

    public int size() {
	cleanCache();
	return size;
    }

    private void grow() {
	// resize
	final int newCapacity = table.length << 1;
	final Entry[] newTable = new Entry[newCapacity];
	mask = newCapacity - 1;

	// transfer
	for (int oldIndex = 0; oldIndex < table.length; oldIndex++) {
	    Entry entry = table[oldIndex];
	    table[oldIndex] = null;
	    while (entry != null) {
		final Entry next = entry.next;
		final int newIndex = entry.hash & mask;
		if (entry.get() == null) {
		    entry.next = null; // Help GC
		    entry.key = null; // Help GC
		    size--;
		} else {
		    entry.next = newTable[newIndex];
		    newTable[newIndex] = entry;
		}
		entry = next;
	    }
	}

	table = newTable;
	threshold = (int) (newCapacity * loadFactor);
    }

    @Override
    public String toString() {
	cleanCache();

	StringBuilder builder = new StringBuilder();
	builder.append("size=");
	builder.append(size);
	builder.append(' ');
	builder.append('[');
	final String delimiter = ", ";
	final Entry[] t = table;
	for (int i = 0; i < t.length; i++) {
	    for (Entry e = t[i]; e != null; e = e.next) {
		final Object v = e.get();
		if (v == null)
		    continue;
		builder.append(v);
		builder.append(delimiter);
	    }
	}
	final int length = builder.length();
	if (length > 1) {
	    // Had any elements, delete last delimiter
	    builder.delete(length - 2, length);
	}
	builder.append(']');
	return builder.toString();
    }

    private static int hash(final Object key) {
	final int h = key.hashCode() * INT_PHI;
	return h ^ (h >>> 16);
    }

    private static int nextPowerOfTwo(int x) {
	if (x == 0)
	    return 1;
	x--;
	x |= x >> 1;
	x |= x >> 2;
	x |= x >> 4;
	x |= x >> 8;
	return (x | x >> 16) + 1;
    }

    private static class Entry extends WeakReference<Object> {

	private Object key;
	private Entry next;
	private final int hash;

	public Entry(final Object key, final Object value, final ReferenceQueue<Object> queue, final int hash) {
	    super(value, queue);
	    this.key = key;
	    this.hash = hash;
	}

	@Override
	public String toString() {
	    String st = String.valueOf(get());
	    if (next != null)
		st += " -> " + next.toString();
	    return st;
	}

    }

}
