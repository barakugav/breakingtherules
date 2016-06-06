package breakingtherules.tests.utilities;

import static org.junit.Assert.assertEquals;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.junit.Test;

import breakingtherules.utilities.WeakCache;

public class WeakCacheTest {

    @Test
    public void createTest() {
	new WeakCache<>();
    }

    @Test
    public void createTestInitCapacity() {
	new WeakCache<>(16);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createTestInitCapacityNegative() {
	new WeakCache<>(-1);
    }

    @Test
    public void createTestLoadFactor() {
	new WeakCache<>(16, 0.5f);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createTestLoadFactorNegative() {
	new WeakCache<>(16, -0.5f);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createTestLoadFactorNan() {
	new WeakCache<>(16, Float.NaN);
    }

    @Test
    public void sizeTest() {
	WeakCache<Integer, Object> cache = new WeakCache<>();
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
	WeakCache<Integer, Object> cache = new WeakCache<>();
	Integer one = Integer.valueOf(1);
	Object key = new Object();
	cache.add(one, key);
	assertEquals(key, cache.get(one));
    }

    @Test
    public void addTestNullKey() {
	WeakCache<Integer, Object> cache = new WeakCache<>();
	Object val = new Object();
	cache.add(null, val);
	assertEquals(val, cache.get(null));
    }

    @Test(expected = NullPointerException.class)
    public void addTestNullValue() {
	WeakCache<Integer, Object> cache = new WeakCache<>();
	cache.add(Integer.valueOf(1), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addTestNoUniqeKey() {
	WeakCache<Integer, Object> cache = new WeakCache<>();
	Integer key = Integer.valueOf(1);
	Object val1 = new Object();
	Object val2 = new Object();
	cache.add(key, val1);
	cache.add(key, val2);
    }

    @Test
    public void getTest() {
	WeakCache<Integer, Object> cache = new WeakCache<>();
	for (int i = 0; i < 25; i++) {
	    Integer key = Integer.valueOf(i);
	    Object expected = new Object();
	    cache.add(key, expected);
	    Object actual = cache.get(key);
	    assertEquals(expected, actual);
	}

	cache = new WeakCache<>();
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
	WeakCache<Integer, Object> cache = new WeakCache<>();
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
