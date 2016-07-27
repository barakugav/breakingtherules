package breakingtherules.tests.util;

import org.junit.Test;

import breakingtherules.util.Object2ObjectHeavySynchronizedBucketHashCache;

@SuppressWarnings("javadoc")
public class Object2ObjectHeavySynchronizedBucketHashCacheTest extends AbstractObject2ObjectCacheTest {

    @Test
    public void addTest() {
	addTest(new Object2ObjectHeavySynchronizedBucketHashCache<>());
    }

    @Test
    public void addTestAndGetAfter() {
	addTestAndGetAfter(new Object2ObjectHeavySynchronizedBucketHashCache<>());
    }

    @Test
    public void addTestNullKey() {
	addTestNullKey(new Object2ObjectHeavySynchronizedBucketHashCache<>());
    }

    @Test
    public void addTestNullKeyAndGetAfter() {
	addTestNullKeyAndGetAfter(new Object2ObjectHeavySynchronizedBucketHashCache<>());
    }

    @Test
    public void clearTest() {
	clearTest(new Object2ObjectHeavySynchronizedBucketHashCache<>());
    }

    @SuppressWarnings("unused")
    @Test
    public void constructorTest() {
	new Object2ObjectHeavySynchronizedBucketHashCache<>();
    }

    @SuppressWarnings("unused")
    @Test
    public void constructorTestInitCapacity() {
	new Object2ObjectHeavySynchronizedBucketHashCache<>(100);
    }

    @SuppressWarnings("unused")
    @Test
    public void constructorTestInitCapacityAndLoadFactor() {
	new Object2ObjectHeavySynchronizedBucketHashCache<>(50, 0.9f);
    }

    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void constructorTestInitCapacityAndNaNLoadFactor() {
	new Object2ObjectHeavySynchronizedBucketHashCache<>(1, Float.NaN);
    }

    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void constructorTestInitCapacityAndNegativeLoadFactor() {
	new Object2ObjectHeavySynchronizedBucketHashCache<>(10_000, -0.1f);
    }

    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void constructorTestNegativeInitCapacity() {
	new Object2ObjectHeavySynchronizedBucketHashCache<>(-20);
    }

    @Test
    public void getOrAddTest() {
	getOrAddTest(new Object2ObjectHeavySynchronizedBucketHashCache<>());
    }

    @Test
    public void getTestNonEmptyCache() {
	getTestNonEmptyCache(new Object2ObjectHeavySynchronizedBucketHashCache<>());
    }

    @Test
    public void rempveTest() {
	removeTest(new Object2ObjectHeavySynchronizedBucketHashCache<>());
    }

    @Test
    public void sizeTestDoubleAddingKey() {
	sizeTestDoubleAddingKey(new Object2ObjectHeavySynchronizedBucketHashCache<>());
    }

    @Test
    public void sizeTestEmptyCache() {
	sizeTestEmptyCache(new Object2ObjectHeavySynchronizedBucketHashCache<>());
    }

    @Test
    public void sizeTestNonEmptyCache() {
	sizeTestNonEmptyCache(new Object2ObjectHeavySynchronizedBucketHashCache<>());
    }

}
