package breakingtherules.tests.utilities;

import static org.junit.Assert.assertEquals;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.junit.Test;

import breakingtherules.utilities.WeakHashCache;

public class WeakHashCacheTest {

    @Test
    public void createTest() {
	new WeakHashCache<>();
    }

    @Test
    public void createTestInitCapacity() {
	new WeakHashCache<>(16);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createTestInitCapacityNegative() {
	new WeakHashCache<>(-1);
    }

    @Test
    public void createTestLoadFactor() {
	new WeakHashCache<>(16, 0.5f);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createTestLoadFactorNegative() {
	new WeakHashCache<>(16, -0.5f);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createTestLoadFactorNan() {
	new WeakHashCache<>(16, Float.NaN);
    }

    @Test
    public void sizeTest() {
	WeakHashCache<Integer, Object> cache = new WeakHashCache<>();
	assertEquals(0, cache.size());

	Integer key1 = Integer.valueOf(1);
	Object val1 = new Object();
	cache.add(key1, val1);
	assertEquals(1, cache.size());

	Integer key2 = Integer.valueOf(2);
	Object val2 = new Object();
	cache.add(key2, val2);
	assertEquals(2, cache.size());
    }

    @Test
    public void addTest() {
	WeakHashCache<Integer, Object> cache = new WeakHashCache<>();
	Integer one = Integer.valueOf(1);
	Object key = new Object();
	cache.add(one, key);
	assertEquals(key, cache.get(one));
    }

    @Test
    public void addTestNullKey() {
	WeakHashCache<Integer, Object> cache = new WeakHashCache<>();
	Object val = new Object();
	cache.add(null, val);
	assertEquals(val, cache.get(null));
    }

    @Test(expected = NullPointerException.class)
    public void addTestNullValue() {
	WeakHashCache<Integer, Object> cache = new WeakHashCache<>();
	cache.add(Integer.valueOf(1), null);
    }

    @Test
    public void addTestNoUniqeKey() {
	WeakHashCache<Integer, Object> cache = new WeakHashCache<>();
	Integer key = Integer.valueOf(1);
	Object val1 = new Object();
	Object val2 = new Object();
	cache.add(key, val1);
	assertEquals(val1, cache.add(key, val2));
    }

    @Test
    public void getTest() {
	WeakHashCache<Integer, Object> cache = new WeakHashCache<>();
	for (int i = 0; i < 25; i++) {
	    Integer key = Integer.valueOf(i);
	    Object expected = new Object();
	    cache.add(key, expected);
	    Object actual = cache.get(key);
	    assertEquals(expected, actual);
	}

	cache = new WeakHashCache<>();
	Map<Integer, Object> m = new HashMap<>();
	Random rand = new Random();
	for (int i = 0; i < 500; i++) {
	    m.put(Integer.valueOf(rand.nextInt()), new Object());
	}
	for (Entry<Integer, Object> entry : m.entrySet()) {
	    cache.add(entry.getKey(), entry.getValue());
	}
	for (Entry<Integer, Object> entry : m.entrySet()) {
	    Object cachedObject = cache.get(entry.getKey());
	    assertEquals(entry.getValue(), cachedObject);
	}
    }

    @Test
    public void cleanCacheTest() {
	final String POSSIBLE_GC_NOT_WORKING_MSSG = "Cache didn't clean, "
		+ "or GC didn't act (This test is inconsistent and can't be "
		+ "changed as far as we know) try running this test alone.";
	WeakHashCache<Integer, Object> cache = new WeakHashCache<>();
	Integer key1 = Integer.valueOf(0);
	Integer key2 = Integer.valueOf(1);
	Integer key3 = Integer.valueOf(2);
	Object val1 = new Object();
	Object val2 = new Object();
	Object val3 = new Object();
	cache.add(key1, val1);
	cache.add(key2, val2);
	cache.add(key3, val3);
	assertEquals(3, cache.size());
	assertEquals(val1, cache.get(key1));
	assertEquals(val2, cache.get(key2));
	assertEquals(val3, cache.get(key3));
	val1 = null;
	forceGC();
	assertEquals(POSSIBLE_GC_NOT_WORKING_MSSG, 2, cache.size());
	assertEquals(null, cache.get(key1));
	assertEquals(val2, cache.get(key2));
	assertEquals(val3, cache.get(key3));
	val3 = null;
	forceGC();
	assertEquals(POSSIBLE_GC_NOT_WORKING_MSSG, 1, cache.size());
	assertEquals(null, cache.get(key1));
	assertEquals(val2, cache.get(key2));
	assertEquals(null, cache.get(key3));
	val2 = null;
	forceGC();
	assertEquals(POSSIBLE_GC_NOT_WORKING_MSSG, 0, cache.size());
	assertEquals(null, cache.get(key1));
	assertEquals(null, cache.get(key2));
	assertEquals(null, cache.get(key3));
    }

    private static void forceGC() {
	for (int i = 250; i-- != 0;) {
	    Object o = new Object();
	    WeakReference<Object> ref = new WeakReference<>(o);
	    o = null;
	    while (ref.get() != null) {
		System.gc();
	    }
	}
    }

}
