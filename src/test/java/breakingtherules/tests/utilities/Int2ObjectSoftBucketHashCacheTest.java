package breakingtherules.tests.utilities;

import org.junit.Test;

import breakingtherules.utilities.Int2ObjectSoftBucketHashCache;
import breakingtherules.utilities.Object2ObjectSoftBucketHashCache;

@SuppressWarnings("javadoc")
public class Int2ObjectSoftBucketHashCacheTest extends AbstractInt2ObjectCacheTest {

    public void addTest() {

	addTest(new Int2ObjectSoftBucketHashCache<>());
    }

    @Test
    public void addTest0Key() {
	addTest0Key(new Int2ObjectSoftBucketHashCache<>());
    }

    @Test
    public void addTest0KeyAndGetAfter() {
	addTest0KeyAndGetAfter(new Int2ObjectSoftBucketHashCache<>());
    }

    @Test
    public void addTestAndGetAfter() {
	addTestAndGetAfter(new Int2ObjectSoftBucketHashCache<>());
    }

    @Test
    public void clearTest() {
	clearTest(new Int2ObjectSoftBucketHashCache<>());
    }

    @SuppressWarnings("unused")
    @Test
    public void constructorTest() {
	new Int2ObjectSoftBucketHashCache<>();
    }

    @SuppressWarnings("unused")
    @Test
    public void constructorTestInitCapacity() {
	new Object2ObjectSoftBucketHashCache<>(100);
    }

    @SuppressWarnings("unused")
    @Test
    public void constructorTestInitCapacityAndLoadFactor() {
	new Object2ObjectSoftBucketHashCache<>(50, 0.9f);
    }

    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void constructorTestInitCapacityAndNaNLoadFactor() {
	new Object2ObjectSoftBucketHashCache<>(1, Float.NaN);
    }

    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void constructorTestInitCapacityAndNegativeLoadFactor() {
	new Object2ObjectSoftBucketHashCache<>(10_000, -0.1f);
    }

    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void constructorTestNegativeInitCapacity() {
	new Object2ObjectSoftBucketHashCache<>(-20);
    }

    @Test
    public void getOrAddTest() {
	getOrAddTest(new Int2ObjectSoftBucketHashCache<>());
    }

    @Test
    public void getTestNonEmptyCache() {
	getTestNonEmptyCache(new Int2ObjectSoftBucketHashCache<>());
    }

    @Test
    public void rempveTest() {
	removeTest(new Int2ObjectSoftBucketHashCache<>());
    }

    @Test
    public void sizeTestDoubleAddingKey() {
	sizeTestDoubleAddingKey(new Int2ObjectSoftBucketHashCache<>());
    }

    @Test
    public void sizeTestEmptyCache() {
	sizeTestEmptyCache(new Int2ObjectSoftBucketHashCache<>());
    }

    @Test
    public void sizeTestNonEmptyCache() {
	sizeTestNonEmptyCache(new Int2ObjectSoftBucketHashCache<>());
    }

}
