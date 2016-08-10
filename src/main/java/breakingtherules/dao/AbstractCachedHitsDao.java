package breakingtherules.dao;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import breakingtherules.dto.ListDto;
import breakingtherules.firewall.Filter;
import breakingtherules.firewall.Hit;
import breakingtherules.firewall.Rule;
import breakingtherules.util.Object2ObjectCache;
import breakingtherules.util.Object2ObjectHeavySynchronizedBucketHashCache;
import breakingtherules.util.Triple;
import breakingtherules.util.Triple.UnmodifiableTriple;
import breakingtherules.util.Utility;

/**
 * An implementation of {@link HitsDao} with cache.
 * <p>
 * The DAO caches the hits that are provided by the sub class by the
 * {@link #getHits(String)} abstract method.
 *
 * @author Barak Ugav
 * @author Yishai Gronich
 *
 */
public abstract class AbstractCachedHitsDao implements HitsDao {

    /**
     * Cache for loaded hits.
     */
    private final Object2ObjectCache<String, List<Hit>> m_hitsCache;

    /**
     * Cache for hits number by filter and rules.
     * <p>
     * The 'number of hits' cache is keyed by the hits jobName, rules (which are
     * stored in a set, because there order doesn't matter) and filter.
     */
    private final Object2ObjectCache<UnmodifiableTriple<String, Set<Rule>, Filter>, Integer> m_totalHitsCache;

    /**
     * Supplier function of the 'hits number'.
     * <p>
     * Used by {@link Object2ObjectCache#getOrAdd(Object, Function)
     * cache.getOrAdd(key, Function)}.
     *
     * @see #getHitsNumber(String, Iterable, Filter)
     */
    private final Function<Triple<String, Set<Rule>, Filter>, Integer> m_hitsNumberSupplier = triple -> {
	try {
	    final String jobName = triple.getFirst();
	    final Set<Rule> rules = triple.getSecond();
	    final Filter filter = triple.getThird();
	    final List<Hit> hits = getHitsInternal(jobName);

	    int hitsNumber = 0;
	    if (rules.isEmpty() && filter.equals(Filter.ANY_FILTER))
		// No filtering needed
		hitsNumber = hits.size();
	    else
		for (final Hit hit : hits)
		    if (DaoUtils.isMatch(hit, rules, filter))
			hitsNumber++;
	    return Integer.valueOf(hitsNumber);

	} catch (final IOException e) {
	    throw new UncheckedIOException(e);
	} catch (final ParseException e) {
	    throw new UncheckedParseException(e);
	}
    };

    /**
     * Supplier function of hits.
     * <p>
     * Used by {@link Object2ObjectCache#getOrAdd(Object, Function)
     * cache.getOrAdd(key, Function)}.
     *
     * @see #getHitsInternal(String)
     */
    private final Function<String, List<Hit>> m_hitsSupplier = jobName -> {
	try {
	    // Don't let anyone change the cache.
	    // We are using array list here for faster iterations.

	    final Iterable<Hit> hits = getHits(jobName);
	    return Collections.unmodifiableList(hits instanceof List ? (List<Hit>) hits : Utility.newArrayList(hits));
	} catch (final IOException e) {
	    throw new UncheckedIOException(e);
	} catch (final ParseException e) {
	    throw new UncheckedParseException(e);
	}
    };

    /**
     * Construct new AbstractCachedHitsDao.
     */
    protected AbstractCachedHitsDao() {
	m_hitsCache = new Object2ObjectHeavySynchronizedBucketHashCache<>();
	m_totalHitsCache = new Object2ObjectHeavySynchronizedBucketHashCache<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Iterable<Hit> getHits(final String jobName, final Iterable<Rule> rules, final Filter filter)
	    throws ParseException, IOException {
	final List<Hit> hits = getHitsInternal(jobName);

	// Filter hits by the rules and filter.
	final List<Hit> filteredHits = new ArrayList<>();
	for (final Hit hit : hits)
	    if (DaoUtils.isMatch(hit, rules, filter))
		filteredHits.add(hit);
	return filteredHits;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final ListDto<Hit> getHitsList(final String jobName, final Iterable<Rule> rules, final Filter filter)
	    throws IOException, ParseException {
	final List<Hit> hits = getHitsInternal(jobName);

	// Filter hits by the rules and filter.
	final List<Hit> filteredHits = new ArrayList<>();
	for (final Hit hit : hits)
	    if (DaoUtils.isMatch(hit, rules, filter))
		filteredHits.add(hit);
	final int size = filteredHits.size();

	m_totalHitsCache.add(
		new UnmodifiableTriple<>(jobName, Collections.unmodifiableSet(Utility.newHashSet(rules)), filter),
		Integer.valueOf(size));
	return new ListDto<>(filteredHits, 0, size, size);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final ListDto<Hit> getHitsList(final String jobName, final Iterable<Rule> rules, final Filter filter,
	    final int startIndex, final int endIndex) throws IOException, ParseException {
	final List<Hit> allHits = getHitsList(jobName, rules, filter).getData();
	final int totalSize = getHitsNumber(jobName, rules, filter);
	if (allHits.isEmpty())
	    return new ListDto<>(Collections.emptyList(), 0, 0, totalSize);
	final List<Hit> hits = Utility.subListView(allHits, startIndex, endIndex - startIndex);
	return new ListDto<>(hits, Math.min(startIndex, totalSize - 1), Math.min(endIndex, totalSize), totalSize);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int getHitsNumber(final String jobName, final Iterable<Rule> rules, final Filter filter)
	    throws IOException, ParseException {
	try {
	    return m_totalHitsCache.getOrAdd(
		    new UnmodifiableTriple<>(jobName, Collections.unmodifiableSet(Utility.newHashSet(rules)), filter),
		    m_hitsNumberSupplier).intValue();

	} catch (final UncheckedIOException e) {
	    throw e.getCause();
	} catch (final UncheckedParseException e) {
	    throw e.getCause();
	}
    }

    /**
     * Get all (unique) hits that match the job.
     * <p>
     *
     * @param jobName
     *            name of the job.
     * @return iterable of all (unique) hits that are related to the job,
     * @throws IOException
     *             if any I/O errors occurs.
     * @throws ParseException
     *             if any parse errors occurs.
     */
    protected abstract Iterable<Hit> getHits(String jobName) throws IOException, ParseException;

    /**
     * Get all hits, used internally.
     * <p>
     *
     * @param jobName
     *            name of the job.
     * @return all hits, maybe from cache. (unmodifiable list)
     * @throws ParseException
     *             if the data in the file is invalid.
     * @throws IOException
     *             if any I/O errors occurs.
     */
    private List<Hit> getHitsInternal(final String jobName) throws ParseException, IOException {
	try {
	    return m_hitsCache.getOrAdd(jobName, m_hitsSupplier);

	} catch (final UncheckedIOException e) {
	    throw e.getCause();
	} catch (final UncheckedParseException e) {
	    throw e.getCause();
	}
    }

}