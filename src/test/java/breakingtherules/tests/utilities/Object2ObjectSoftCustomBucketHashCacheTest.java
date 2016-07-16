package breakingtherules.tests.utilities;

import org.junit.Test;

import breakingtherules.utilities.Hashs;
import breakingtherules.utilities.Object2ObjectSoftCustomBucketHashCache;

@SuppressWarnings("javadoc")
public class Object2ObjectSoftCustomBucketHashCacheTest extends AbstractObject2ObjectCacheTest {

    @Test
    public void addTest() {

	addTest(new Object2ObjectSoftCustomBucketHashCache<>(Hashs.defaultStrategy()));
    }

    @Test
    public void addTestAndGetAfter() {
	addTestAndGetAfter(new Object2ObjectSoftCustomBucketHashCache<>(Hashs.defaultStrategy()));
    }

    @Test
    public void addTestNullKey() {
	addTestNullKey(new Object2ObjectSoftCustomBucketHashCache<>(Hashs.defaultStrategy()));
    }

    @Test
    public void addTestNullKeyAndGetAfter() {
	addTestNullKeyAndGetAfter(new Object2ObjectSoftCustomBucketHashCache<>(Hashs.defaultStrategy()));
    }

    @Test
    public void clearTest() {
	clearTest(new Object2ObjectSoftCustomBucketHashCache<>(Hashs.defaultStrategy()));
    }

    @SuppressWarnings("unused")
    @Test
    public void constructorTest() {
	new Object2ObjectSoftCustomBucketHashCache<>(Hashs.defaultStrategy());
    }

    @SuppressWarnings("unused")
    @Test
    public void constructorTestInitCapacity() {
	new Object2ObjectSoftCustomBucketHashCache<>(Hashs.defaultStrategy(), 100);
    }

    @SuppressWarnings("unused")
    @Test
    public void constructorTestInitCapacityAndLoadFactor() {
	new Object2ObjectSoftCustomBucketHashCache<>(Hashs.defaultStrategy(), 50, 0.9f);
    }

    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void constructorTestInitCapacityAndNaNLoadFactor() {
	new Object2ObjectSoftCustomBucketHashCache<>(Hashs.defaultStrategy(), 1, Float.NaN);
    }

    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void constructorTestInitCapacityAndNegativeLoadFactor() {
	new Object2ObjectSoftCustomBucketHashCache<>(Hashs.defaultStrategy(), 10_000, -0.1f);
    }

    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void constructorTestNegativeInitCapacity() {
	new Object2ObjectSoftCustomBucketHashCache<>(Hashs.defaultStrategy(), -20);
    }

    @Test
    public void getOrAddTest() {
	getOrAddTest(new Object2ObjectSoftCustomBucketHashCache<>(Hashs.defaultStrategy()));
    }

    @Test
    public void getTestNonEmptyCache() {
	getTestNonEmptyCache(new Object2ObjectSoftCustomBucketHashCache<>(Hashs.defaultStrategy()));
    }

    @Test
    public void rempveTest() {
	removeTest(new Object2ObjectSoftCustomBucketHashCache<>(Hashs.defaultStrategy()));
    }

    @Test
    public void sizeTestDoubleAddingKey() {
	sizeTestDoubleAddingKey(new Object2ObjectSoftCustomBucketHashCache<>(Hashs.defaultStrategy()));
    }

    @Test
    public void sizeTestEmptyCache() {
	sizeTestEmptyCache(new Object2ObjectSoftCustomBucketHashCache<>(Hashs.defaultStrategy()));
    }

    @Test
    public void sizeTestNonEmptyCache() {
	sizeTestNonEmptyCache(new Object2ObjectSoftCustomBucketHashCache<>(Hashs.defaultStrategy()));
    }

}
