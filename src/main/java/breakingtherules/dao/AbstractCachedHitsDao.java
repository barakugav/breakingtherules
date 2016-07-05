package breakingtherules.dao;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import breakingtherules.dto.ListDto;
import breakingtherules.firewall.Filter;
import breakingtherules.firewall.Hit;
import breakingtherules.firewall.Rule;
import breakingtherules.utilities.Cache;
import breakingtherules.utilities.HeavySynchronizedHashCache;
import breakingtherules.utilities.Triple;
import breakingtherules.utilities.Triple.UnmodifiableTriple;
import breakingtherules.utilities.Utility;

/**
 * An implementation of {@link HitsDao} with cache.
 * <p>
 * The DAO caches the hits that are provided by the sub class by the
 * {@link #getHitsSupplier()} abstract method.
 * 
 * @author Barak Ugav
 * @author Yishai Gronich
 *
 */
public abstract class AbstractCachedHitsDao implements HitsDao {

    /**
     * Cache for loaded hits.
     */
    private final Cache<String, List<Hit>> m_cacheHits;

    /**
     * Cache for hits number by filter and rules.
     * <p>
     * The 'number of hits' cache is keyed by the hits jobName, rules (which are
     * stored in a set, because there order doesn't matter) and filter.
     */
    private final Cache<UnmodifiableTriple<String, Set<Rule>, Filter>, Integer> m_totalHitsCache;

    /**
     * Construct new AbstractCachedHitsDao.
     */
    protected AbstractCachedHitsDao() {
	m_cacheHits = new HeavySynchronizedHashCache<>();
	m_totalHitsCache = new HeavySynchronizedHashCache<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final ListDto<Hit> getHitsList(final String jobName, final List<Rule> rules, final Filter filter)
	    throws IOException, ParseException {
	final List<Hit> hits = getHitsInternal(jobName);

	// Filter hits by the rules and filter.
	final List<Hit> filteredHits = new ArrayList<>();
	for (final Hit hit : hits) {
	    if (DaoUtilities.isMatch(hit, rules, filter)) {
		filteredHits.add(hit);
	    }
	}
	final int size = filteredHits.size();

	m_totalHitsCache.add(
		new UnmodifiableTriple<>(jobName, Collections.unmodifiableSet(new HashSet<>(rules)), filter),
		Integer.valueOf(size));
	return new ListDto<>(filteredHits, 0, size, size);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final ListDto<Hit> getHitsList(final String jobName, final List<Rule> rules, final Filter filter,
	    final int startIndex, final int endIndex) throws IOException, ParseException {
	final List<Hit> allHits = getHitsList(jobName, rules, filter).getData();
	final int totalSize = getHitsNumber(jobName, rules, filter);
	if (allHits.isEmpty()) {
	    return new ListDto<>(Collections.emptyList(), 0, 0, totalSize);
	}
	final List<Hit> hits = Utility.subListView(allHits, startIndex, endIndex - startIndex);
	return new ListDto<>(hits, Math.min(startIndex, totalSize - 1), Math.min(endIndex, totalSize), totalSize);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Iterable<Hit> getHits(final String jobName, final List<Rule> rules, final Filter filter)
	    throws ParseException, IOException {

	final List<Hit> hits = getHitsInternal(jobName);

	if (rules.isEmpty() && Filter.ANY_FILTER.equals(filter)) {
	    // No filter is needed.
	    return hits;
	}

	// Filter hits by the rules and filter.
	final List<Hit> filteredHits = new ArrayList<>();
	for (final Hit hit : hits) {
	    if (DaoUtilities.isMatch(hit, rules, filter)) {
		filteredHits.add(hit);
	    }
	}
	return filteredHits;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int getHitsNumber(final String jobName, final List<Rule> rules, final Filter filter)
	    throws IOException, ParseException {
	try {
	    return m_totalHitsCache.getOrAdd(
		    new UnmodifiableTriple<>(jobName, Collections.unmodifiableSet(new HashSet<>(rules)), filter),
		    HITS_NUMBER_SUPPLIER).intValue();

	} catch (final UncheckedIOException e) {
	    throw e.getCause();
	} catch (final UncheckedParseException e) {
	    throw e.getCause();
	}
    }

    /**
     * Get the DAO supplier of the hits.
     * <p>
     * The input to the supplier is the <bold>job name</bold>. And the supplier
     * supply hits associated to the job.
     * <p>
     * The supplier is guaranteed to be used only if the hits are not in the
     * cache.
     * 
     * @return the DAO supplier of the hits by job name.
     */
    protected abstract Function<String, Set<Hit>> getHitsSupplier();

    /**
     * Unchecked version of {@link ParseException}.
     * <p>
     * The UncheckedParseException is a wrapper for a checked
     * {@link ParseException}.
     * <p>
     * Used when implementing or overriding a method that doesn't throw super
     * class exception of {@link ParseException}.
     * <p>
     * This exception is similar to {@link UncheckedIOException}.
     * <p>
     * 
     * @author Barak Ugav
     * @author Yishai Gronich
     *
     */
    protected static class UncheckedParseException extends RuntimeException {

	@SuppressWarnings("javadoc")
	private static final long serialVersionUID = 6371272539188428352L;

	/**
	 * Construct new UncheckedParseException without a message.
	 * 
	 * @param cause
	 *            the original checked {@link ParseException}.
	 */
	protected UncheckedParseException(final ParseException cause) {
	    super(cause);
	}

	/**
	 * Construct new UncheckedParseException with a message.
	 * 
	 * @param message
	 *            the exception's message.
	 * @param cause
	 *            the original checked {@link ParseException}.
	 */
	protected UncheckedParseException(final String message, final ParseException cause) {
	    super(message, cause);
	}

	/**
	 * Get the {@link ParseException} cause of this unchecked exception.
	 * <p>
	 */
	@Override
	public synchronized ParseException getCause() {
	    return (ParseException) super.getCause();
	}

    }

    /**
     * Get all hits, used internally.
     * <p>
     * 
     * @param fileName
     *            the name of the input file.
     * @return all hits, parsed from the file or returned from cache.
     * @throws ParseException
     *             if the data in the file is invalid.
     * @throws IOException
     *             if any I/O errors occurs.
     */
    private List<Hit> getHitsInternal(final String fileName) throws ParseException, IOException {
	try {
	    return m_cacheHits.getOrAdd(fileName, HITS_SUPPLIER);

	} catch (final UncheckedIOException e) {
	    throw e.getCause();
	} catch (final UncheckedParseException e) {
	    throw e.getCause();
	}
    }

    /**
     * Supplier function of the 'hits number'.
     * <p>
     * 
     * @see #getHitsNumber(String, List, Filter)
     */
    private final Function<Triple<String, Set<Rule>, Filter>, Integer> HITS_NUMBER_SUPPLIER = triple -> {
	try {
	    final String jobName = triple.getFirst();
	    final Set<Rule> rules = triple.getSecond();
	    final Filter filter = triple.getThird();
	    final List<Hit> hits = getHitsInternal(jobName);

	    int hitsNumber = 0;
	    if (rules.isEmpty() && filter.equals(Filter.ANY_FILTER)) {
		// No filtering needed
		hitsNumber = hits.size();
	    } else {
		for (final Hit hit : hits) {
		    if (DaoUtilities.isMatch(hit, rules, filter)) {
			hitsNumber++;
		    }
		}
	    }
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
     * 
     * @see #getHitsInternal(String)
     */
    private final Function<String, List<Hit>> HITS_SUPPLIER = fileName -> {
	// Don't let anyone change the cache
	return Collections.unmodifiableList(Utility.newArrayList(getHitsSupplier().apply(fileName)));
    };

}