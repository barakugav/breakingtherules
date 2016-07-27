package breakingtherules.tests.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import breakingtherules.tests.TestBase;
import breakingtherules.util.Int2ObjectCache;

@SuppressWarnings("javadoc")
class AbstractInt2ObjectCacheTest extends TestBase {

    void addTest(final Int2ObjectCache<Integer> cache) {
	checkCache(cache);
	final int key = 2;
	final Integer expected = Integer.valueOf(5);
	final Integer actual = cache.add(key, expected);
	assertEquals(expected, actual);
    }

    void addTest0Key(final Int2ObjectCache<Integer> cache) {
	checkCache(cache);
	final int key = 0;
	final Integer expected = Integer.valueOf(5);
	final Integer actual = cache.add(key, expected);
	assertEquals(expected, actual);
    }

    void addTest0KeyAndGetAfter(final Int2ObjectCache<Integer> cache) {
	checkCache(cache);
	final int key = 0;
	final Integer expected = Integer.valueOf(5);
	cache.add(key, expected);
	final Integer actual = cache.get(key);
	assertEquals(expected, actual);
    }

    void addTestAndGetAfter(final Int2ObjectCache<Integer> cache) {
	checkCache(cache);
	final int key = 2;
	final Integer expected = Integer.valueOf(5);
	cache.add(key, expected);
	final Integer actual = cache.get(key);
	assertEquals(expected, actual);
    }

    void clearTest(final Int2ObjectCache<Integer> cache) {
	checkCache(cache);
	final int SIZE = 25;
	final List<Integer[]> pairs = randPairs(SIZE);
	for (final Integer[] pair : pairs)
	    cache.add(pair[0].intValue(), pair[1]);
	assertNotEquals(0, cache.size());
	cache.clear();
	assertEquals(0, cache.size());
    }

    void getOrAddTest(final Int2ObjectCache<Integer> cache) {
	checkCache(cache);
	final int SIZE = 25;
	final List<Integer[]> allPairs = randPairs(SIZE);
	final List<Integer[]> existingPairs = new ArrayList<>(allPairs.subList(0, SIZE / 2));
	final List<Integer[]> nonexistingPairs = new ArrayList<>(allPairs.subList(SIZE / 2, SIZE));
	for (final Integer[] pair : existingPairs)
	    cache.add(pair[0].intValue(), pair[1]);
	assertEquals(existingPairs.size(), cache.size());
	for (final Integer[] pair : existingPairs)
	    assertEquals(pair[1], cache.get(pair[0].intValue()));

	// Try to call 'getOrAdd' on keys that are already in the cache. Should
	// do nothing.
	for (final Integer[] pair : existingPairs) {
	    // Should be ignored by 'getOrAdd' because there already element
	    // with that key.
	    final Integer differentValue = pair[1].intValue() == 0 ? Integer.valueOf(1) : Integer.valueOf(0);
	    cache.getOrAdd(pair[0].intValue(), ignoredkey -> differentValue);
	}
	assertEquals(existingPairs.size(), cache.size());
	for (final Integer[] pair : existingPairs)
	    assertEquals(pair[1], cache.get(pair[0].intValue()));

	// Call 'getOrAdd' on non existing pairs.
	for (final Integer[] pair : nonexistingPairs)
	    cache.getOrAdd(pair[0].intValue(), ignoredkey -> pair[1]);
	assertEquals(SIZE, cache.size());
	for (final Integer[] pair : allPairs)
	    assertEquals(pair[1], cache.get(pair[0].intValue()));
    }

    void getTestEmptyCache(final Int2ObjectCache<Integer> cache) {
	checkCache(cache);
	final List<Integer> keys = randUniqueList(25);
	for (final Integer key : keys)
	    assertEquals(null, cache.get(key.intValue()));
    }

    void getTestNonEmptyCache(final Int2ObjectCache<Integer> cache) {
	checkCache(cache);
	final int SIZE = 25;
	final List<Integer[]> pairs = randPairs(SIZE);
	for (final Integer[] pair : pairs)
	    cache.add(pair[0].intValue(), pair[1]);
	for (final Integer[] pair : pairs)
	    assertEquals(pair[1], cache.get(pair[0].intValue()));
    }

    void removeTest(final Int2ObjectCache<Integer> cache) {
	checkCache(cache);
	final int SIZE = 25;
	final List<Integer[]> pairs = randPairs(SIZE);
	for (final Integer[] pair : pairs)
	    cache.add(pair[0].intValue(), pair[1]);
	for (final Integer[] pair : pairs)
	    assertEquals(pair[1], cache.get(pair[0].intValue()));
	assertEquals(pairs.size(), cache.size());
	while (!pairs.isEmpty()) {
	    cache.remove(pairs.get(0)[0].intValue());
	    pairs.remove(0);
	    for (final Integer[] pair : pairs)
		assertEquals(pair[1], cache.get(pair[0].intValue()));
	    assertEquals(pairs.size(), cache.size());
	}
    }

    void sizeTestDoubleAddingKey(final Int2ObjectCache<Integer> cache) {
	checkCache(cache);
	final int SIZE = 25;
	final List<Integer[]> pairs = randPairs(SIZE);
	for (int i = 0; i < SIZE; i++) {
	    final Integer[] pair = pairs.get(i);
	    assertEquals(i, cache.size());
	    cache.add(pair[0].intValue(), pair[1]);
	}

	// Add all again
	for (final Integer[] pair : pairs) {
	    cache.add(pair[0].intValue(), pair[1]);
	    assertEquals(SIZE, cache.size());
	}
    }

    void sizeTestEmptyCache(final Int2ObjectCache<Integer> cache) {
	checkCache(cache);
	assertEquals(0, cache.size());
	cache.get(8);
	cache.get(-543);
	assertEquals(0, cache.size());
    }

    void sizeTestNonEmptyCache(final Int2ObjectCache<Integer> cache) {
	checkCache(cache);
	final int SIZE = 25;
	final List<Integer[]> pairs = randPairs(SIZE);
	for (int i = 0; i < SIZE; i++) {
	    final Integer[] pair = pairs.get(i);
	    assertEquals(i, cache.size());
	    cache.add(pair[0].intValue(), pair[1]);
	}
	assertEquals(SIZE, cache.size());
    }

    static List<Integer> randList(int s) {
	final List<Integer> l = new ArrayList<>(s);
	while (s-- != 0)
	    l.add(Integer.valueOf(rand.nextInt()));
	return l;
    }

    static List<Integer[]> randPairs(final int s) {
	final Set<Integer> keys = randSet(s);
	final List<Integer[]> pairs = new ArrayList<>(s);
	for (final Integer key : keys)
	    pairs.add(new Integer[] { key, Integer.valueOf(rand.nextInt()) });
	return pairs;
    }

    static List<Integer> randUniqueList(final int s) {
	return new ArrayList<>(randSet(s));
    }

    private static void checkCache(final Int2ObjectCache<?> cache) {
	if (cache == null)
	    throw new InternalError("Tested cache is null.");
	if (cache.size() != 0)
	    throw new InternalError("Tested cache is not empty. (size=" + cache.size() + ")");
    }

    private static Set<Integer> randSet(final int s) {
	final Set<Integer> set = new HashSet<>(s);
	while (set.size() < s)
	    set.add(Integer.valueOf(rand.nextInt()));
	return set;
    }

}
