package breakingtherules.tests.util;

import org.junit.Test;

import breakingtherules.util.Hashs;
import breakingtherules.util.Object2ObjectCustomBucketHashCache;

@SuppressWarnings("javadoc")
public class Object2ObjectCustomBucketHashCacheTest extends AbstractObject2ObjectCacheTest {

    @Test
    public void addTest() {
	addTest(new Object2ObjectCustomBucketHashCache<>(Hashs.defaultStrategy()));
    }

    @Test
    public void addTestAndGetAfter() {
	addTestAndGetAfter(new Object2ObjectCustomBucketHashCache<>(Hashs.defaultStrategy()));
    }

    @Test
    public void addTestNullKey() {
	addTestNullKey(new Object2ObjectCustomBucketHashCache<>(Hashs.defaultStrategy()));
    }

    @Test
    public void addTestNullKeyAndGetAfter() {
	addTestNullKeyAndGetAfter(new Object2ObjectCustomBucketHashCache<>(Hashs.defaultStrategy()));
    }

    @Test
    public void clearTest() {
	clearTest(new Object2ObjectCustomBucketHashCache<>(Hashs.defaultStrategy()));
    }

    @SuppressWarnings("unused")
    @Test
    public void constructorTest() {
	new Object2ObjectCustomBucketHashCache<>(Hashs.defaultStrategy());
    }

    @SuppressWarnings("unused")
    @Test
    public void constructorTestInitCapacity() {
	new Object2ObjectCustomBucketHashCache<>(Hashs.defaultStrategy(), 100);
    }

    @SuppressWarnings("unused")
    @Test
    public void constructorTestInitCapacityAndLoadFactor() {
	new Object2ObjectCustomBucketHashCache<>(Hashs.defaultStrategy(), 50, 0.9f);
    }

    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void constructorTestInitCapacityAndNaNLoadFactor() {
	new Object2ObjectCustomBucketHashCache<>(Hashs.defaultStrategy(), 1, Float.NaN);
    }

    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void constructorTestInitCapacityAndNegativeLoadFactor() {
	new Object2ObjectCustomBucketHashCache<>(Hashs.defaultStrategy(), 10_000, -0.1f);
    }

    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void constructorTestNegativeInitCapacity() {
	new Object2ObjectCustomBucketHashCache<>(Hashs.defaultStrategy(), -20);
    }

    @Test
    public void getOrAddTest() {
	getOrAddTest(new Object2ObjectCustomBucketHashCache<>(Hashs.defaultStrategy()));
    }

    @Test
    public void getTestNonEmptyCache() {
	getTestNonEmptyCache(new Object2ObjectCustomBucketHashCache<>(Hashs.defaultStrategy()));
    }

    @Test
    public void rempveTest() {
	removeTest(new Object2ObjectCustomBucketHashCache<>(Hashs.defaultStrategy()));
    }

    @Test
    public void sizeTestDoubleAddingKey() {
	sizeTestDoubleAddingKey(new Object2ObjectCustomBucketHashCache<>(Hashs.defaultStrategy()));
    }

    @Test
    public void sizeTestEmptyCache() {
	sizeTestEmptyCache(new Object2ObjectCustomBucketHashCache<>(Hashs.defaultStrategy()));
    }

    @Test
    public void sizeTestNonEmptyCache() {
	sizeTestNonEmptyCache(new Object2ObjectCustomBucketHashCache<>(Hashs.defaultStrategy()));
    }

}
