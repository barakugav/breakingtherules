package breakingtherules.dao;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import breakingtherules.dto.ListDto;
import breakingtherules.firewall.Rule;
import breakingtherules.util.Object2ObjectCache;
import breakingtherules.util.Object2ObjectHeavySynchronizedBucketHashCache;
import breakingtherules.util.Utility;

/**
 * An implementation of {@link RulesDao} with cache.
 * <p>
 * The DAO caches the rules that are provided by the sub class by the
 * {@link #getRulesInternal(String)} and
 * {@link #getOriginalRuleInternal(String)} abstract methods.
 *
 * @author Barak Ugav
 * @author Yishai Gronich
 *
 */
public abstract class AbstractCachedRulesDao implements RulesDao {

    /**
     * Cache of loaded rules.
     * <p>
     * Keyed by the job name.
     */
    private final Object2ObjectCache<String, List<Rule>> m_rulesCache;

    /**
     * Cache of loaded original rules.
     * <p>
     * Keyed by the job name.
     */
    private final Object2ObjectCache<String, Rule> m_originalRulesCache;

    /**
     * Supplier function of rules by job name.
     * <p>
     * Used by {@link Object2ObjectCache#getOrAdd(Object, Function)
     * cache.getOrAdd(key, Function)}.
     *
     * @see #getRules(String)
     */
    private final Function<String, List<Rule>> m_rulesSupplier = jonName -> {
	try {
	    // Don't let anyone change the cache.
	    return Collections.unmodifiableList(getRulesInternal(jonName));
	} catch (final IOException e) {
	    throw new UncheckedIOException(e);
	} catch (final ParseException e) {
	    throw new UncheckedParseException(e);
	}
    };

    /**
     * Supplier function of the original rule of a job.
     * <p>
     * Used by {@link Object2ObjectCache#getOrAdd(Object, Function)
     * cache.getOrAdd(key, Function)}.
     *
     * @see #getOriginalRule(String)
     */
    private final Function<String, Rule> m_originalRuleSupplier = jonName -> {
	try {
	    return getOriginalRuleInternal(jonName);
	} catch (final IOException e) {
	    throw new UncheckedIOException(e);
	} catch (final ParseException e) {
	    throw new UncheckedParseException(e);
	}
    };

    /**
     * Construct new AbstractCachedHitsDao.
     */
    protected AbstractCachedRulesDao() {
	m_rulesCache = new Object2ObjectHeavySynchronizedBucketHashCache<>();
	m_originalRulesCache = new Object2ObjectHeavySynchronizedBucketHashCache<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Rule getOriginalRule(final String jobName) throws IOException, ParseException {
	try {
	    return m_originalRulesCache.getOrAdd(jobName, m_originalRuleSupplier);
	} catch (final UncheckedIOException e) {
	    throw e.getCause();
	} catch (final UncheckedParseException e) {
	    throw e.getCause();
	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ListDto<Rule> getRules(final String jobName) throws IOException, ParseException {
	final List<Rule> rules;
	try {
	    rules = m_rulesCache.getOrAdd(jobName, m_rulesSupplier);
	} catch (final UncheckedIOException e) {
	    throw e.getCause();
	} catch (final UncheckedParseException e) {
	    throw e.getCause();
	}
	final int size = rules.size();
	return new ListDto<>(rules, 0, size, size);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ListDto<Rule> getRules(final String jobName, final int startIndex, final int endIndex)
	    throws IOException, ParseException {
	if (startIndex < 0)
	    throw new IllegalArgumentException("Start index < 0");
	else if (startIndex > endIndex)
	    throw new IllegalArgumentException("Start index > end index");

	final List<Rule> rules;
	try {
	    rules = m_rulesCache.getOrAdd(jobName, m_rulesSupplier);
	} catch (final UncheckedIOException e) {
	    throw e.getCause();
	} catch (final UncheckedParseException e) {
	    throw e.getCause();
	}

	final int total = rules.size();
	if (startIndex >= total)
	    throw new IndexOutOfBoundsException("Start index bigger that total count");
	final List<Rule> subRulesList = Utility.subList(rules, startIndex, endIndex - startIndex);
	return new ListDto<>(subRulesList, startIndex, endIndex, total);
    }

    /**
     * Get the original rule of a job.
     * <p>
     * Used internally (and from subclasses).
     *
     * @param jobName
     *            the job name.
     * @return the original rule of the job.
     * @throws IOException
     *             if any I/O errors occurs.
     * @throws ParseException
     *             if any parse errors occurs.
     */
    protected abstract Rule getOriginalRuleInternal(final String jobName) throws IOException, ParseException;

    /**
     * Get all rules of a job.
     * <p>
     * Used internally (and from subclasses).
     *
     * @param jobName
     *            the job name.
     * @return all the rules of the job.
     * @throws IOException
     *             if any I/O errors occurs.
     * @throws ParseException
     *             if any parse errors occurs.
     */
    protected abstract List<Rule> getRulesInternal(final String jobName) throws IOException, ParseException;

}
