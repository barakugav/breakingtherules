package breakingtherules.services.algorithm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import breakingtherules.dao.HitsDao;
import breakingtherules.dao.ParseException;
import breakingtherules.firewall.Attribute;
import breakingtherules.firewall.Destination;
import breakingtherules.firewall.Filter;
import breakingtherules.firewall.Hit;
import breakingtherules.firewall.IP;
import breakingtherules.firewall.IPAttribute;
import breakingtherules.firewall.Rule;
import breakingtherules.firewall.Source;
import breakingtherules.utilities.UnionList;
import breakingtherules.utilities.Utility;

/**
 * Algorithm to get suggestions for rules. Based on information theory.
 * <p>
 * This algorithm is intended only for IP type attributes - i.e. source and
 * destination. This means that for other attributes, this algorithm backs up to
 * a different algorithm, i.e. {@link SimpleAlgorithm}.
 * <p>
 * This algorithm uses dynamic programming. It is based on a recursive rule to
 * decide if a certain node in the IP tree is worth separating, or if it is best
 * united (suggested on its own). The recursive rule also gives the node a
 * certain score, and the lower the score - the better the node, because we were
 * able to express the same amount of information with less bits (information
 * theory).
 * <p>
 * The recursive rule is:
 * 
 * <pre>
 * f(x) = min { |x|&middot(log<sub>2</sub>(S<sub>x</sub>) - log<sub>2</sub>(P(x))) + K,
 * f(x.right) + f(x.left) }
 * </pre>
 * 
 * Where:
 * <ul>
 * <li>|x| is the number of hits under subnetwork x.</li>
 * <li>S<sub>x</sub> is the size of the subnetwork x (which is a power of 2).
 * </li>
 * <li>P(x) is the probability of the subnetwork x, i.e. the percentage of hits
 * that are in it out of all hits.</li>
 * <li>K is the rule weight, a constant that allows the user to choose the
 * permissiveness of the rules they would like.</li>
 * </ul>
 * <p>
 * 
 * @author Barak Ugav
 * @author Yishai Gronich
 * 
 * @see IP
 * @see Hit
 * @see Suggestion
 */
public class InformationAlgorithm implements SuggestionsAlgorithm {

    /**
     * Allows configuration, if the user wants more general rules (high
     * ruleWeight) or more specific rules (low ruleWeight)
     */
    private double m_ruleWeight;

    /**
     * Flag of parallel computation. If true, the algorithm will run parallel
     */
    private boolean m_parallel;

    /**
     * Number of current used threads by all information algorithms. Used to
     * optimize parallel.
     */
    private static AtomicInteger numberOfUsedThreads;

    /**
     * Max used threads.
     */
    private int m_maxThreads;

    /**
     * Threshold of number of IPs to parallel operations.
     */
    private int m_parallelThreshold;

    /**
     * Used when the attribute type if not source or destination
     */
    private final SimpleAlgorithm m_simpleAlgorithm;

    /**
     * Max time to which the algorithm will abort after starting all threads if
     * they haven't finished yet.
     */
    private static final int ALGORITHM_TIMEOUT = 5; // minutes

    /**
     * Default value for the ruleWeight parameter
     */
    private static final double DEFAULT_RULE_WIEGHT = 500;

    /**
     * If true, the information will operate on default. Else, the
     * {@link #activateParallel()} will be needed.
     * <p>
     * 
     * @see #m_parallel
     */
    private static final boolean DEFAULT_PARALLEL = false;

    /**
     * The default max number of threads used by the algorithm.
     * <p>
     * 
     * @see #m_maxThreads
     */
    private static final int DEFAULT_MAX_THREADS = Integer.MAX_VALUE;

    /**
     * The default number of IPs threshold for parallel.
     * <p>
     * 
     * @see #m_parallelThreshold
     */
    private static final int DEFAULT_PARALLEL_THRESHOLD = 0x10000;

    /**
     * Number of times the algorithm will try to join the threads. If each try
     * the algorithm, got interrupted, and the number of tries exceeded this
     * number, a non parallel computation will be performed instead.
     */
    private static final int NUMBER_OF_REPEATED_INTERRUPTED_ATTEMPTS = 10;

    static {
	configCheck();
	numberOfUsedThreads = new AtomicInteger(0);
    }

    /**
     * Construct new Information algorithm with default rule weight
     */
    public InformationAlgorithm() {
	m_ruleWeight = DEFAULT_RULE_WIEGHT;
	m_parallel = DEFAULT_PARALLEL;
	m_maxThreads = DEFAULT_MAX_THREADS;
	m_parallelThreshold = DEFAULT_PARALLEL_THRESHOLD;
	m_simpleAlgorithm = new SimpleAlgorithm();
    }

    /**
     * Set the rule weight of this algorithm to new one
     * 
     * @param weight
     *            new rule weight value
     * @throws IllegalArgumentException
     *             if weight is NaN or negative
     */
    public void setRuleWeight(final double weight) {
	if (Double.isNaN(weight))
	    throw new IllegalArgumentException("Rule weight can't be NaN");
	if (weight < 0)
	    throw new IllegalArgumentException("Rule weight can't be negative: " + weight);
	m_ruleWeight = weight;
    }

    /**
     * Active the parallel for this algorithm.
     * <p>
     * The setting for parallel are as previously used or the default if they
     * didn't got set.
     */
    public void activateParallel() {
	m_parallel = true;
    }

    /**
     * Active the parallel for this algorithm and set the number of max threads
     * used by the algorithm.
     * <p>
     * Other setting of parallel are as previously used or the default if they
     * didn't got set.
     * 
     * @param maxThreads
     *            number of max threads will be used by this algorithm.
     * @throws IllegalArgumentException
     *             if {@code maxThreads} is not positive.
     */
    public void activateParallel(final int maxThreads) {
	if (maxThreads <= 0)
	    throw new IllegalArgumentException("maxThreads <= 0: " + maxThreads);
	m_parallel = true;
	m_maxThreads = maxThreads;
    }

    /**
     * Active the parallel for this algorithm, set the number of max threads
     * used by the algorithm and set the threshold for parallel.
     * 
     * @param maxThreads
     *            number of max threads will be used by this algorithm.
     * @param parallelThreshold
     *            number of IPs threshold for parallel.
     * @throws IllegalArgumentException
     *             if {@code maxThreads} is not positive or
     *             {@code parallelThreshold} is not positive.
     */
    public void activateParallel(final int maxThreads, final int parallelThreshold) {
	if (maxThreads <= 0)
	    throw new IllegalArgumentException("maxThreads <= 0: " + maxThreads);
	if (parallelThreshold <= 0)
	    throw new IllegalArgumentException("parallelThreshold <= 0: " + parallelThreshold);
	m_parallel = true;
	m_maxThreads = maxThreads;
	m_parallelThreshold = parallelThreshold;
    }

    /**
     * Deactivate parallel.
     * <p>
     * <bold>Does not</bold> erase the other parallel setting.
     */
    public void deactivateParallel() {
	m_parallel = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Suggestion> getSuggestions(final HitsDao dao, final String jobName, final List<Rule> rules,
	    final Filter filter, final int amount, final String attType) throws IOException, ParseException {
	final int attTypeId = Attribute.typeStrToTypeId(attType);
	if (attTypeId == Attribute.UNKOWN_ATTRIBUTE_ID) {
	    throw new IllegalArgumentException("Unkown attribute: " + attType);
	}
	final Iterable<Hit> hits = dao.getHits(jobName, rules, filter);
	final InformationAlgorithmRunner runner = new InformationAlgorithmRunner(hits, amount, attTypeId);
	runner.run();
	return runner.m_result;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Operate the request with multithreaded if the parallel option was turn
     * on.
     */
    @Override
    public List<Suggestion>[] getSuggestions(final HitsDao dao, final String jobName, final List<Rule> rules,
	    final Filter filter, final int amount, final String[] attTypes) throws IOException, ParseException {

	final InformationAlgorithmRunner[] runners = new InformationAlgorithmRunner[attTypes.length];
	final Iterable<Hit> hits = dao.getHits(jobName, rules, filter);
	for (int i = 0; i < attTypes.length; i++) {
	    final String attType = attTypes[i];
	    final int attTypeId = Attribute.typeStrToTypeId(attType);
	    if (attTypeId == Attribute.UNKOWN_ATTRIBUTE_ID) {
		throw new IllegalArgumentException("Unknown attribute: " + attType);
	    }
	    final InformationAlgorithmRunner runner = new InformationAlgorithmRunner(hits, amount, attTypeId);
	    runners[i] = runner;
	}

	boolean parallel = runners.length > 1 && m_parallel;
	if (parallel) {
	    // Parallel
	    final int availableProcessors = Runtime.getRuntime().availableProcessors();
	    final int requiredProcessors = attTypes.length;
	    final int avaiableThreads = Math.min(m_maxThreads, Math.min(availableProcessors, requiredProcessors));

	    boolean wasInterrupted;
	    int numberOfInteraptedAttempts = 0;

	    // Try to create, start and join to all threads. If got
	    // interrupted too many times - abort parallel and use
	    // single thread.
	    do {
		wasInterrupted = false;
		try {
		    if (attTypes.length < avaiableThreads) {
			final Thread[] threads = new Thread[attTypes.length];
			for (int i = 0; i < attTypes.length; i++) {
			    threads[i] = new Thread(runners[i], "InformationAlgorithm" + attTypes[i]);
			}
			for (final Thread thread : threads) {
			    thread.start();
			}
			for (final Thread thread : threads) {
			    thread.join();
			}
		    } else {
			final ExecutorService executor = Executors.newFixedThreadPool(avaiableThreads);
			for (int i = 0; i < attTypes.length; i++) {
			    executor.execute(runners[i]);
			}
			executor.shutdown();
			executor.awaitTermination(ALGORITHM_TIMEOUT, TimeUnit.MINUTES);
			if (!executor.isTerminated()) {
			    throw new RuntimeException("Timed out.");
			}
		    }

		} catch (InterruptedException e) {
		    wasInterrupted = true;
		    numberOfInteraptedAttempts++;
		    System.err.println("Interapted durring an Inforamation algorithm, " + "will try again no more then "
			    + (NUMBER_OF_REPEATED_INTERRUPTED_ATTEMPTS - numberOfInteraptedAttempts) + " times.");
		    e.printStackTrace(System.err);
		}
	    } while (wasInterrupted && numberOfInteraptedAttempts < NUMBER_OF_REPEATED_INTERRUPTED_ATTEMPTS);

	    if (wasInterrupted) {
		// Got interrupted too many times, abandon parallel, use
		// regular single thread
		parallel = false;
	    }
	}

	// Check the parallel flag again and don't used the else statement
	// because maybe the flag was change throughout the if statement.
	if (!parallel) {
	    // No parallel
	    for (final InformationAlgorithmRunner runner : runners) {
		runner.run();
	    }
	}

	// Extract all results
	@SuppressWarnings("unchecked")
	final List<Suggestion>[] suggestions = new List[attTypes.length];
	for (int i = 0; i < runners.length; i++) {
	    suggestions[i] = runners[i].m_result;
	}
	return suggestions;
    }

    /**
     * The main runnable of the algorithm. Compute suggestion for one attribute
     * type.
     * 
     * @author Barak Ugav
     * @author Yishai Gronich
     *
     */
    private class InformationAlgorithmRunner implements Runnable {

	/**
	 * Input hits.
	 */
	private final Iterable<Hit> m_hits;

	/**
	 * Id of desire suggestions's type.
	 */
	private final int m_attTypeId;

	/**
	 * Number of desire suggestions.
	 */
	private final int m_amount;

	/**
	 * The result buffer.
	 */
	private List<Suggestion> m_result;

	/**
	 * Construct new InformationAlgorithmRunner.
	 * 
	 * @param hits
	 *            input hits.
	 * @param amount
	 *            number of desire suggestions.
	 * @param attTypeId
	 *            type id of desire suggestions.
	 */
	InformationAlgorithmRunner(final Iterable<Hit> hits, final int amount, final int attTypeId) {
	    m_hits = hits;
	    m_attTypeId = attTypeId;
	    m_amount = amount;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
	    if (m_attTypeId != Attribute.DESTINATION_TYPE_ID && m_attTypeId != Attribute.SOURCE_TYPE_ID) {
		m_result = m_simpleAlgorithm.getSuggestions(m_hits, m_amount, m_attTypeId);
		return;
	    }

	    switch (m_attTypeId) {
	    case Attribute.DESTINATION_TYPE_ID:
		m_result = getSuggestionsDestination();
		break;
	    case Attribute.SOURCE_TYPE_ID:
		m_result = getSuggestionsSource();
		break;
	    default:
		throw new InternalError(
			"Attribute type wasn't destination not source after checking it was one of them.");
	    }
	}

	/**
	 * Get suggestions for hits about destination attribute
	 * 
	 * @return list of destination suggestion
	 * @throws NullPointerException
	 *             if hits are null, or one of the hits are null
	 * @throws IllegalArgumentException
	 *             if one of the hits doesn't contains destination attribute
	 */
	private List<Suggestion> getSuggestionsDestination() {
	    // Calculate suggestions
	    List<IPNode> subnets = getIPSuggestions();

	    // Sort suggestions from small to big, so reverse list after sort
	    subnets.sort(IPNode.SUBNET_SUGGESTIONS_SIZE_COMPARATOR);
	    Collections.reverse(subnets);
	    subnets = Utility.subList(subnets, 0, m_amount);

	    final List<Suggestion> suggestions = new ArrayList<>(subnets.size());
	    for (final IPNode subnet : subnets) {
		suggestions.add(new Suggestion(Destination.valueOf(subnet.m_ip), subnet.m_size, subnet.getScore()));
	    }

	    return suggestions;
	}

	/**
	 * Get suggestions for hits about source attribute
	 * 
	 * @return list of source suggestion
	 * @throws NullPointerException
	 *             if hits are null, or one of the hits are null
	 * @throws IllegalArgumentException
	 *             if one of the hits doesn't contains the source attribute
	 */
	private List<Suggestion> getSuggestionsSource() {
	    // Calculate suggestions

	    List<IPNode> subnets = getIPSuggestions();

	    // Sort suggestions from small to big, so reverse list after sort
	    subnets.sort(IPNode.SUBNET_SUGGESTIONS_SIZE_COMPARATOR);
	    Collections.reverse(subnets);
	    subnets = Utility.subList(subnets, 0, m_amount);

	    final List<Suggestion> suggestions = new ArrayList<>(subnets.size());
	    for (final IPNode subnet : subnets) {
		suggestions.add(new Suggestion(Source.valueOf(subnet.m_ip), subnet.m_size, subnet.getScore()));
	    }
	    return suggestions;
	}

	/**
	 * Get suggestion for hits for IP attribute.
	 * 
	 * @return list of suggestions for IPs.
	 * @throws NullPointerException
	 *             if hits are null, or one of the hits are null
	 * @throws IllegalArgumentException
	 *             if one of the hits doesn't contains destination attribute
	 */
	private List<IPNode> getIPSuggestions() {
	    // Creates lowest layer nodes from hits.
	    IPNode[] nodes = toIPNodes();

	    if (nodes.length == 0) {
		return new ArrayList<>();
	    }
	    if (nodes.length == 1) {
		List<IPNode> res = new ArrayList<>();
		res.add(nodes[0]);
		return res;
	    }
	    // The total number of hits, used to calculate probability (constant
	    // value)
	    int totalSize = 0;
	    for (final IPNode node : nodes) {
		totalSize += node.m_size;
	    }

	    // the current IP layer the algorithm is working on
	    IPNode[] currentLayer = nodes;
	    nodes = null; // Free memory

	    // Sort the nodes by their IPs, ensuring the assumption that if for
	    // a node there is a brother, it will be next to it. This assumption
	    // will stay for next layers too.
	    Arrays.parallelSort(currentLayer, IPNode.IPS_COMPARATOR);

	    // Run until there are only one element in the list (all nodes are
	    // sub children of the node)
	    int currentLayerSize = currentLayer.length;
	    while (currentLayerSize > 1) {

		// The next IP layer that is currently constructed
		IPNode[] nextLayer = null;

		final int numberOfJobs;
		boolean parallel = m_parallel;
		if (parallel) {
		    synchronized (numberOfUsedThreads) {

			final int availableProcessors = Math.max(1,
				Runtime.getRuntime().availableProcessors() - numberOfUsedThreads.get());
			final int requiredProcessors = Math.max(1, 1 + (currentLayerSize / m_parallelThreshold));
			numberOfJobs = Math.min(m_maxThreads, Math.min(availableProcessors, requiredProcessors));
			if (parallel = m_parallel && numberOfJobs != 1)
			    numberOfUsedThreads.addAndGet(numberOfJobs);
		    }
		} else {
		    numberOfJobs = 1;
		}

		if (parallel) {
		    List<InformationAlgorithmLayerRunner> runners = null;
		    boolean wasInterrupted;
		    int numberOfInteraptedAttempts = 0;

		    // Try to create, start and join to all threads. If got
		    // interrupted too many times - abort parallel and use
		    // single thread.
		    do {
			wasInterrupted = false;
			try {
			    // List of all threads
			    final List<Thread> threads = new ArrayList<>(numberOfJobs);
			    runners = new ArrayList<>(numberOfJobs);
			    final int nodesPerThread = currentLayerSize / numberOfJobs;
			    int fromIndex, toIndex = 0;

			    // Create all threads
			    for (int threadNumber = 1; threadNumber <= numberOfJobs; threadNumber++) {
				// continue from where the last thread stopped
				fromIndex = toIndex;
				if (threadNumber == numberOfJobs) {
				    // Last thread creation, run up to the end
				    // of the current layer
				    toIndex = currentLayerSize;

				} else {
				    // Middle thread, run on at least
				    // nodesPerThread
				    toIndex = fromIndex + nodesPerThread;

				    // Include additional IPNode if it's the
				    // current last brother, else they will not
				    // be connected
				    if (currentLayer[toIndex - 1].m_ip.isBrother(currentLayer[toIndex].m_ip)) {
					toIndex++;
				    }
				}
				final InformationAlgorithmLayerRunner runner = new InformationAlgorithmLayerRunner(
					currentLayer, fromIndex, toIndex, totalSize, m_ruleWeight);
				runners.add(runner);
				threads.add(new Thread(runner, "InformationAlgorithmThread" + threadNumber));
			    }

			    // Start all threads
			    for (final Thread runnerThread : threads) {
				runnerThread.start();
			    }

			    // Wait to all threads
			    for (final Thread runnerThread : threads) {
				runnerThread.join();
			    }

			} catch (InterruptedException e) {
			    wasInterrupted = true;
			    numberOfInteraptedAttempts++;
			    System.err.println(
				    "Interapted durring an Inforamation algorithm, " + "will try again no more then "
					    + (NUMBER_OF_REPEATED_INTERRUPTED_ATTEMPTS - numberOfInteraptedAttempts)
					    + " times.");
			    e.printStackTrace(System.err);
			}
		    } while (wasInterrupted && numberOfInteraptedAttempts < NUMBER_OF_REPEATED_INTERRUPTED_ATTEMPTS);

		    if (wasInterrupted) {
			// Got interrupted too many times, abandon parallel, use
			// regular single thread
			parallel = false;

		    } else {
			// Combine all results from all threads results to the
			// new layer
			int nextLayerSize = 0;
			for (final InformationAlgorithmLayerRunner runner : runners) {
			    nextLayerSize += runner.m_nextLayerSize;
			}
			nextLayer = new IPNode[nextLayerSize];
			int offSet = 0;
			for (final InformationAlgorithmLayerRunner runner : runners) {
			    System.arraycopy(runner.m_nextLayer, 0, nextLayer, offSet, runner.m_nextLayerSize);
			    offSet += runner.m_nextLayerSize;
			}
			currentLayerSize = nextLayerSize;
		    }

		    synchronized (numberOfUsedThreads) {
			numberOfUsedThreads.addAndGet(-numberOfJobs);
		    }
		}

		// Check parallel flag again and don't use the else block
		// because maybe the flag got changed during the parallel block
		if (!parallel) {
		    // No parallel, run normal on single thread
		    final InformationAlgorithmLayerRunner runner = new InformationAlgorithmLayerRunner(currentLayer, 0,
			    currentLayerSize, totalSize, m_ruleWeight);
		    synchronized (numberOfUsedThreads) {
			numberOfUsedThreads.incrementAndGet();
		    }
		    runner.run();
		    synchronized (numberOfUsedThreads) {
			numberOfUsedThreads.decrementAndGet();
		    }
		    nextLayer = runner.m_nextLayer;
		    currentLayerSize = runner.m_nextLayerSize;
		}

		// Current layer is finished, move to next layer
		currentLayer = nextLayer;
	    }

	    // Only one element in layer, it is the parent node of all others
	    IPNode root = currentLayer[0];
	    return root.m_bestSubnets.toArrayList();
	}

	/**
	 * Create list of IPNodes from the iterable of hits
	 * 
	 * @return list of IPNodes constructed from the hits
	 * @throws NullPointerException
	 *             if hits are null, or one of the hits are null
	 * @throws IllegalArgumentException
	 *             if one of the hits doesn't contains the desire attribute
	 */
	private IPNode[] toIPNodes() {
	    final Map<IP, IPNode> uniqueIPNodes = new HashMap<>();
	    for (final Hit hit : m_hits) {
		final IPAttribute att = (IPAttribute) hit.getAttribute(m_attTypeId);
		if (att == null) {
		    throw new IllegalArgumentException("One of the hits doesn't have the desire attribute");
		}
		final IP ip = att.getIp();
		final IPNode existingNode = uniqueIPNodes.get(ip);
		if (existingNode == null) {
		    final IPNode newNode = new IPNode(ip);
		    newNode.m_compressSize = m_ruleWeight;
		    newNode.m_size = 1;
		    uniqueIPNodes.put(ip, newNode);
		} else {
		    existingNode.m_size++;
		}
	    }
	    for (final IPNode node : uniqueIPNodes.values()) {
		node.m_bestSubnets = new UnionList<>(node);
	    }
	    return uniqueIPNodes.values().toArray(new IPNode[uniqueIPNodes.size()]);
	}

    }

    /**
     * The secondary runnable used by {@link InformationAlgorithmRunner the main
     * runnable}.
     * <p>
     * This runnable operate on a single IPNodes layer and construct the next
     * layer from it. And for each IPNode calculate if it should be treaded as
     * one union subnetwork or as multiple smaller networks (in both cases,
     * covering all the input IPs).
     * 
     * @author Barak Ugav
     * @author Yishai Gronich
     *
     */
    private static class InformationAlgorithmLayerRunner implements Runnable {

	/**
	 * Current IPNodes layer.
	 */
	private final IPNode[] m_currentLayer;

	/**
	 * The relevant begin interval index in the current layer.
	 */
	private final int m_fromIndex;

	/**
	 * The relevant end interval index in the current layer.
	 */
	private final int m_toIndex;

	/**
	 * The next constructed layer. This buffer is relevant only after the
	 * runner was run.
	 */
	private final IPNode[] m_nextLayer;

	/**
	 * After the runner was run, this value is the size of the next
	 * constructed layer.
	 */
	private int m_nextLayerSize;

	/**
	 * The total number of IPs in the current layer.
	 * <p>
	 * Used to calculate probability of each subnetwork of the total IPs.
	 */
	private final int m_totalSize;

	/**
	 * The rules weight used by this runner.
	 * <p>
	 * 
	 * @see InformationAlgorithm#m_ruleWeight
	 */
	private final double m_ruleWeight;

	/**
	 * Construct new InformationAlgorithmLayerRunner.
	 * 
	 * @param currentLayer
	 *            the current layer.
	 * @param fromIndex
	 *            the relevant begin interval index in the current layer.
	 * @param toIndex
	 *            the relevant end interval index in the current layer.
	 * @param totalSize
	 *            the total number of IPs.
	 * @param ruleWeight
	 *            the weight the runner should give to a new rule (see
	 *            {@link InformationAlgorithm#m_ruleWeight}).
	 */
	public InformationAlgorithmLayerRunner(final IPNode[] currentLayer, final int fromIndex, final int toIndex,
		final int totalSize, final double ruleWeight) {
	    m_currentLayer = currentLayer;
	    m_fromIndex = fromIndex;
	    m_toIndex = toIndex;
	    m_nextLayer = new IPNode[toIndex - fromIndex];
	    m_totalSize = totalSize;
	    m_ruleWeight = ruleWeight;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
	    // Run over all elements, for each element construct his parent
	    // element for the next layer by checking if his brother exist and
	    // if so - merge them, else construct the parent base only on the
	    // one current node. Run over all elements except the last one
	    // (length = size -1) so we don't get out of bounds when searching
	    // for brother (always in [curenntIndex + 1])
	    final int fence = m_toIndex - 1;
	    int currentLayarIndex = m_fromIndex;
	    m_nextLayerSize = 0;
	    while (currentLayarIndex < fence) {

		// The current node
		final IPNode current = m_currentLayer[currentLayarIndex];

		// The next node, relevant only if brother of nodeA
		final IPNode brother = m_currentLayer[currentLayarIndex + 1];

		// The IP of the current node
		final IP ip = current.m_ip;

		// The IP of the constructed parent
		final IP parentIp = ip.getParent();

		// The parent node of the current node (possible of brother
		// candidate too, if they are acutely brothers), currently
		// constructed
		final IPNode parent = new IPNode(parentIp);

		final double totalSizeLog = Utility.log2(m_totalSize);

		// If nodeA and nodeB are brothers:
		if (ip.isBrother(brother.m_ip)) {

		    // The number of hits in the constructed parent node
		    final int size = parent.m_size = current.m_size + brother.m_size;

		    // The optimal compress size if the parent node chosen as a
		    // union single subnetwork:
		    //
		    // union = size * (log(subnetwork size) +
		    // log(1/probability)) + ruleWeight
		    // = size * (log(subnetwork size) - log(probability)) +
		    // ruleWeight
		    // = size * (log(subnetwork size) - log(size / totalSize)) +
		    // ruleWeight
		    // = size * (log(subnetwork size) - (log(size) -
		    // log(totalSize))) + ruleWeight
		    // = size * (log(subnetwork size) - log(size) +
		    // log(totalSize)) + ruleWeight
		    final double union = size * (parentIp.getSubnetBitsNum() - Utility.log2(size) + totalSizeLog)
			    + m_ruleWeight;

		    // The optimal compress size if the parent node chosen as
		    // separated small subnetworks:
		    //
		    // separated = (brother1 optimal) + (brother2 optimal)
		    final double separated = current.m_compressSize + brother.m_compressSize;

		    // Choose optimal (minimum) choice between union subnetwork
		    // or separated small subnetworks. using <= prefer less
		    // subnetworks
		    if (union <= separated) {
			// Using union subnetwork
			parent.m_compressSize = union;

			// Subnetwork is the parent subnetwork
			parent.m_bestSubnets = new UnionList<>(parent);
		    } else {
			// Using separated small subnetworks
			parent.m_compressSize = separated;

			// Union the two subnetworks from both child nodes
			parent.m_bestSubnets = current.m_bestSubnets.transferElementsFrom(brother.m_bestSubnets);
		    }

		    // Used the current node and next node, increase index by 2
		    currentLayarIndex++;
		} else {
		    // Current node and the candidate brother are not brothers.
		    // Copy all values from current node to parent node
		    parent.m_size = current.m_size;
		    parent.m_compressSize = current.m_compressSize;
		    parent.m_bestSubnets = current.m_bestSubnets;
		}

		// Add the finished parent node to next layer list
		m_nextLayer[m_nextLayerSize++] = parent;

		currentLayarIndex++;
	    }

	    // Check last element in list - if it was brother of one before last
	    // meaning no more action is require, else - create his parent node
	    // and copy his properties (no brother for sure)
	    final IPNode beforeLast = m_currentLayer[m_toIndex - 2];
	    final IPNode last = m_currentLayer[m_toIndex - 1];
	    final IP ip = last.m_ip;
	    if (!beforeLast.m_ip.isBrother(ip)) {
		final IPNode parent = new IPNode(ip.getParent());
		parent.m_size = last.m_size;
		parent.m_compressSize = last.m_compressSize;
		parent.m_bestSubnets = last.m_bestSubnets;
		m_nextLayer[m_nextLayerSize++] = parent;
	    }
	}
    }

    /**
     * Configuration check. Used to check the static final fields of this
     * algorithm.
     * 
     * @throws InternalError
     *             if one of the fields is not legal
     */
    @SuppressWarnings("unused")
    private static void configCheck() {
	if (Double.isNaN(DEFAULT_RULE_WIEGHT) || 0 >= DEFAULT_RULE_WIEGHT) {
	    throw new InternalError("DEFAULT_RULE_WIEGHT(" + DEFAULT_RULE_WIEGHT + ") should be > 0");
	}
	if (DEFAULT_MAX_THREADS <= 0) {
	    throw new InternalError("DEFAULT_MAX_THREADS(" + DEFAULT_MAX_THREADS + ") should be > 0");
	}
	if (DEFAULT_PARALLEL_THRESHOLD <= 0) {
	    throw new InternalError("DEFAULT_PARALLEL_THRESHOLD(" + DEFAULT_PARALLEL_THRESHOLD + ") should be > 0");
	}
	if (NUMBER_OF_REPEATED_INTERRUPTED_ATTEMPTS <= 0) {
	    throw new InternalError("NUMBER_OF_REPEATED_INTERRUPTED_ATTEMPTS(" + NUMBER_OF_REPEATED_INTERRUPTED_ATTEMPTS
		    + ") should be > 0");
	}
    }

    /**
     * The IPNode class is node in the Information IPs tree.
     * <p>
     * The node contains the following fields:
     * <ul>
     * <li>ip: IP of the node (if the node is leaf in the tree) or IP of the
     * subnetwork of the node (if the node is a inner node in the tree).</li>
     * <li>size: number of different hits under the ip subnetwork (exactly
     * equals hits count as one, but different hits, even if they have the same
     * IP, count as two).</li>
     * <li>compressSize: value of compression size by the InformationAlgorithm.
     * If this value is high, the node is a strong node.</li>
     * <li>bestSubnets: list of best subnetworks that contained in the IP
     * subnetwork of the node.</li>
     * </ul>
     */
    private static final class IPNode {

	/**
	 * The IP of the node.
	 */
	final IP m_ip;

	/**
	 * The size of the node - the number hits that are covered by it's
	 * subnetworks.
	 */
	int m_size;

	/**
	 * The compress size of the IPNodes, described in more details in the
	 * main documentation of {@link InformationAlgorithm}.
	 */
	double m_compressSize;

	/**
	 * The best subnetworks that this IPNode suggests.
	 */
	UnionList<IPNode> m_bestSubnets;

	/**
	 * Comparator of IPNodes, comparing them by their IPs.
	 */
	static final Comparator<IPNode> IPS_COMPARATOR = (final IPNode o1, final IPNode o2) -> {
	    return o1.m_ip.compareTo(o2.m_ip);
	};

	/**
	 * Comparator of suggestions, comparing them by their sizes.
	 */
	static final Comparator<IPNode> SUBNET_SUGGESTIONS_SIZE_COMPARATOR = (final IPNode s1, final IPNode s2) -> {
	    return s1.m_size - s2.m_size;
	};

	/**
	 * Construct new IPNode of IP.
	 * 
	 * @param ip
	 *            the IP.
	 */
	IPNode(final IP ip) {
	    m_ip = ip;
	}

	/**
	 * Get the score of this IPNode. Treaded as suggestion for IP
	 * subnetwork.
	 * 
	 * @return the score of the IPNode.
	 */
	double getScore() {
	    return 1 / m_compressSize;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(final Object o) {
	    if (o == this) {
		return true;
	    } else if (!(o instanceof IPNode)) {
		return false;
	    }

	    IPNode other = (IPNode) o;
	    return m_ip.equals(other.m_ip);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
	    return m_ip.hashCode();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
	    final StringBuilder builder = new StringBuilder();
	    builder.append(m_ip);
	    builder.append(" size=");
	    builder.append(m_size);
	    builder.append(" compressSize=");
	    builder.append(m_compressSize);
	    builder.append(" nets=");
	    if (m_bestSubnets == null) {
		builder.append("null");
	    } else {
		builder.append('[');
		final Iterator<IPNode> it = m_bestSubnets.iterator();
		final String spacer = ", ";

		if (it.hasNext()) { // Have at least one elements
		    do {
			final IPNode node = it.next();
			builder.append(node.m_ip);
			if (!it.hasNext())
			    break;
			builder.append(spacer);
		    } while (true);
		}
		builder.append(']');
	    }
	    return builder.toString();
	}

    }

}
