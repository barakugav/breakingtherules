package breakingtherules.tests.util;

import org.junit.Test;

import breakingtherules.util.Int2ObjectOpenAddressingHashCache;

@SuppressWarnings("javadoc")
public class Int2ObjectOpenAddressingHashCacheTest extends AbstractInt2ObjectCacheTest {

    public void addTest() {

	addTest(new Int2ObjectOpenAddressingHashCache<>());
    }

    @Test
    public void addTest0Key() {
	addTest0Key(new Int2ObjectOpenAddressingHashCache<>());
    }

    @Test
    public void addTest0KeyAndGetAfter() {
	addTest0KeyAndGetAfter(new Int2ObjectOpenAddressingHashCache<>());
    }

    @Test
    public void addTestAndGetAfter() {
	addTestAndGetAfter(new Int2ObjectOpenAddressingHashCache<>());
    }

    @Test
    public void clearTest() {
	clearTest(new Int2ObjectOpenAddressingHashCache<>());
    }

    @SuppressWarnings("unused")
    @Test
    public void constructorTest() {
	new Int2ObjectOpenAddressingHashCache<>();
    }

    @SuppressWarnings("unused")
    @Test
    public void constructorTestInitCapacity() {
	new Int2ObjectOpenAddressingHashCache<>(100);
    }

    @SuppressWarnings("unused")
    @Test
    public void constructorTestInitCapacityAndLoadFactor() {
	new Int2ObjectOpenAddressingHashCache<>(50, 0.9f);
    }

    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void constructorTestInitCapacityAndNaNLoadFactor() {
	new Int2ObjectOpenAddressingHashCache<>(1, Float.NaN);
    }

    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void constructorTestInitCapacityAndNegativeLoadFactor() {
	new Int2ObjectOpenAddressingHashCache<>(10_000, -0.1f);
    }

    @SuppressWarnings("unused")
    @Test(expected = IllegalArgumentException.class)
    public void constructorTestNegativeInitCapacity() {
	new Int2ObjectOpenAddressingHashCache<>(-20);
    }

    @Test
    public void getOrAddTest() {
	getOrAddTest(new Int2ObjectOpenAddressingHashCache<>());
    }

    @Test
    public void getTestNonEmptyCache() {
	getTestNonEmptyCache(new Int2ObjectOpenAddressingHashCache<>());
    }

    @Test
    public void rempveTest() {
	removeTest(new Int2ObjectOpenAddressingHashCache<>());
    }

    @Test
    public void sizeTestDoubleAddingKey() {
	sizeTestDoubleAddingKey(new Int2ObjectOpenAddressingHashCache<>());
    }

    @Test
    public void sizeTestEmptyCache() {
	sizeTestEmptyCache(new Int2ObjectOpenAddressingHashCache<>());
    }

    @Test
    public void sizeTestNonEmptyCache() {
	sizeTestNonEmptyCache(new Int2ObjectOpenAddressingHashCache<>());
    }

}
