package breakingtherules.services.algorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import breakingtherules.dao.HitsDao;
import breakingtherules.dao.UniqueHit;
import breakingtherules.firewall.Attribute;
import breakingtherules.firewall.Destination;
import breakingtherules.firewall.Filter;
import breakingtherules.firewall.IP;
import breakingtherules.firewall.IPAttribute;
import breakingtherules.firewall.Rule;
import breakingtherules.firewall.Source;
import breakingtherules.utilities.UnionList;
import breakingtherules.utilities.Utility;

/**
 * Algorithm to get suggestions for rules. Based in information theory.
 * 
 * This algorithm is intended only for IP type attributes - i.e. Source and
 * Destination. This means that for other attributes, this algorithm backs up to
 * a different algorithm, i.e. SimpleAlgorithm.
 * 
 * This algorithm uses dynamic programming. It is based on a recursive rule to
 * decide if a certain node in the IP tree is worth separating, or if it is best
 * united (suggested on its own). The recursive rule also gives the node a
 * certain score, and the lower the score - the better the node, because we were
 * able to express the same amount of information with less bits (information
 * theory).
 * 
 * The recursive rule is: f(x) = min { |x| ( log( Sx ) - log ( P(x) ) )
 * f(x.right) + f(x.left) + K } Where: |x| is the number of hits under subnet x
 * Sx is the size of the subnet x (which is a power of 2) P(x) is the
 * probability of the subnet x, i.e. the percentage of hits that are in it out
 * of all hits K is a constant that allows the user to choose the permissiveness
 * of the rules they would like.
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

    private static final Object numberOfUsedThreadsLock;
    private static int numberOfUsedThreads;

    private int m_maxThreads;

    private int m_parallelThreshold;

    /**
     * Used when the attribute type if not Source or Destination
     */
    private final SimpleAlgorithm m_simpleAlgorithm;

    private static final int ALGORITHM_TIMEOUT = 5; // minutes

    /**
     * Default value for the ruleWeight parameter
     */
    private static final double DEFAULT_RULE_WIEGHT = 500;

    private static final double UNIQUE_LIST_FACTOR = 0.25;

    private static final boolean DEFAULT_PARALLEL = false;

    private static final int DEFAULT_MAX_THREADS = Integer.MAX_VALUE;

    private static final int DEFAULT_PARALLEL_THRESHOLD = 0x10000;

    private static final int NUMBER_OF_REPEATED_INTERRUPTED_ATTEMPTS = 10;

    /**
     * Comparator of suggestions, comparing them by their sizes.
     */
    private static final Comparator<SubnetSuggestion> SUBNET_SUGGESTIONS_SIZE_COMPARATOR = new Comparator<SubnetSuggestion>() {

	@Override
	public int compare(final SubnetSuggestion s1, final SubnetSuggestion s2) {
	    return s1.uniqueHitsCount - s2.uniqueHitsCount;
	}
    };

    /**
     * Comparator of IPNodes, comparing them by their IPs.
     */
    private static final Comparator<IPNode> IP_COMPARATOR = new Comparator<IPNode>() {

	@Override
	public int compare(final IPNode o1, final IPNode o2) {
	    return o1.ip.compareTo(o2.ip);
	}
    };

    static {
	configCheck();
	numberOfUsedThreads = 0;
	numberOfUsedThreadsLock = new Object();
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

    public void activateParallel() {
	m_parallel = true;
    }

    public void activateParallel(final int maxThreads) {
	if (maxThreads <= 0)
	    throw new IllegalArgumentException("maxThreads <= 0: " + maxThreads);
	m_parallel = true;
	m_maxThreads = maxThreads;
    }

    public void activateParallel(final int maxThreads, final int parallelThreshold) {
	if (maxThreads <= 0)
	    throw new IllegalArgumentException("maxThreads <= 0: " + maxThreads);
	if (parallelThreshold <= 0)
	    throw new IllegalArgumentException("parallelThreshold <= 0: " + parallelThreshold);
	m_parallel = true;
	m_maxThreads = maxThreads;
	m_parallelThreshold = parallelThreshold;
    }

    public void deactivateParallel() {
	m_parallel = false;
    }

    @Override
    public List<Suggestion> getSuggestions(final HitsDao dao, final String jobName, final List<Rule> rules,
	    final Filter filter, final int amount, final String attType) throws Exception {
	final int attTypeId = Attribute.typeStrToTypeId(attType);
	if (attTypeId == Attribute.UNKOWN_ATTRIBUTE_ID) {
	    throw new IllegalArgumentException("Unkown attribute: " + attType);
	}
	Set<UniqueHit> hits = dao.getUnique(jobName, rules, filter);
	final InformationAlgoRunner runner = new InformationAlgoRunner(hits, amount, attTypeId);
	runner.run();
	return runner.result;
    }

    @Override
    public List<Suggestion>[] getSuggestions(final HitsDao dao, final String jobName, final List<Rule> rules,
	    final Filter filter, final int amount, final String[] attTypes) throws Exception {

	InformationAlgoRunner[] runners = new InformationAlgoRunner[attTypes.length];
	Set<UniqueHit> hits = dao.getUnique(jobName, rules, filter);
	for (int i = 0; i < attTypes.length; i++) {
	    String attType = attTypes[i];
	    final int attTypeId = Attribute.typeStrToTypeId(attType);
	    if (attTypeId == Attribute.UNKOWN_ATTRIBUTE_ID) {
		throw new IllegalArgumentException("Unknown attribute: " + attType);
	    }
	    final InformationAlgoRunner runner = new InformationAlgoRunner(hits, amount, attTypeId);
	    runners[i] = runner;
	}

	if (runners.length > 1 && m_parallel) {
	    // Parallel
	    final int availableProcessors = Runtime.getRuntime().availableProcessors();
	    final int requiredProcessors = attTypes.length;
	    final int avaiableThreads = Math.min(m_maxThreads, Math.min(availableProcessors, requiredProcessors));

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
		ExecutorService executor = Executors.newFixedThreadPool(avaiableThreads);
		for (int i = 0; i < attTypes.length; i++) {
		    executor.execute(runners[i]);
		}
		executor.shutdown();
		executor.awaitTermination(ALGORITHM_TIMEOUT, TimeUnit.MINUTES);
		if (!executor.isTerminated()) {
		    throw new RuntimeException("Timed out.");
		}
	    }
	} else {
	    // No parallel
	    for (final InformationAlgoRunner runner : runners) {
		runner.run();
	    }
	}

	// Extract all results
	@SuppressWarnings("unchecked")
	final List<Suggestion>[] suggestions = new List[attTypes.length];
	for (int i = 0; i < runners.length; i++) {
	    suggestions[i] = runners[i].getResults();
	}
	return suggestions;
    }

    private class InformationAlgoRunner implements SuggestionsAlgorithmRunner {

	private final Set<UniqueHit> hits;
	private final int attTypeId;
	private final int amount;
	private List<Suggestion> result;

	private InformationAlgoRunner(final Set<UniqueHit> hits, final int amount, final int attTypeId) {
	    this.hits = hits;
	    this.attTypeId = attTypeId;
	    this.amount = amount;
	}

	@Override
	public void run() {
	    if (attTypeId != Attribute.DESTINATION_TYPE_ID && attTypeId != Attribute.SOURCE_TYPE_ID) {
		result = m_simpleAlgorithm.getSuggestions(hits, amount, attTypeId);
		return;
	    }

	    switch (attTypeId) {
	    case Attribute.DESTINATION_TYPE_ID:
		result = getSuggestionsDestination();
		break;
	    case Attribute.SOURCE_TYPE_ID:
		result = getSuggestionsSource();
		break;
	    default:
		throw new InternalError(
			"Attribute type wasn't destination not source after checking it was one of them.");
	    }
	}

	@Override
	public List<Suggestion> getResults() {
	    return result;
	}

	/**
	 * Get suggestions for hits about destination attribute
	 * 
	 * @param hits
	 *            iterable object of hits - the input for the suggestions
	 * @return list of destination suggestion
	 * @throws NullPointerException
	 *             if hits are null, or one of the hits are null
	 * @throws IllegalArgumentException
	 *             if one of the hits doesn't contains destination attribute
	 */
	private List<Suggestion> getSuggestionsDestination() {
	    // Calculate suggestions
	    List<SubnetSuggestion> subnets = getIPSuggestions();

	    // Sort suggestions from small to big, so reverse list after sort
	    subnets.sort(SUBNET_SUGGESTIONS_SIZE_COMPARATOR);
	    Collections.reverse(subnets);
	    subnets = Utility.subList(subnets, 0, amount);

	    final List<Suggestion> suggestions = new ArrayList<>(subnets.size());
	    for (final SubnetSuggestion subnet : subnets) {
		suggestions.add(new Suggestion(Destination.create(subnet.ip), subnet.uniqueHitsCount, subnet.score));
	    }

	    return suggestions;
	}

	/**
	 * Get suggestions for hits about source attribute
	 * 
	 * @param hits
	 *            iterable object of hits - the input for the suggestions
	 * @return list of source suggestion
	 * @throws NullPointerException
	 *             if hits are null, or one of the hits are null
	 * @throws IllegalArgumentException
	 *             if one of the hits doesn't contains the source attribute
	 */
	private List<Suggestion> getSuggestionsSource() {
	    // Calculate suggestions

	    List<SubnetSuggestion> subnets = getIPSuggestions();

	    // Sort suggestions from small to big, so reverse list after sort
	    subnets.sort(SUBNET_SUGGESTIONS_SIZE_COMPARATOR);
	    Collections.reverse(subnets);
	    subnets = Utility.subList(subnets, 0, amount);

	    final List<Suggestion> suggestions = new ArrayList<>(subnets.size());
	    for (final SubnetSuggestion subnet : subnets) {
		suggestions.add(new Suggestion(Source.create(subnet.ip), subnet.totalHitsCount, subnet.score));
	    }
	    return suggestions;
	}

	private List<SubnetSuggestion> getIPSuggestions() {
	    // Creates lowest layer nodes from hits
	    IPNode[] nodes = toIPNodes(hits, attTypeId);

	    if (nodes.length == 0) {
		return new ArrayList<>();
	    }
	    if (nodes.length == 1) {
		List<SubnetSuggestion> res = new ArrayList<>();
		res.add(nodes[0].toSuggestion());
		return res;
	    }
	    // The total number of hits, used to calculate probability (constant
	    // value)
	    int totalSize = 0;
	    for (IPNode node : nodes) {
		totalSize += node.size;
	    }

	    // the current IP layer the algorithm is working on
	    IPNode[] currentLayer = nodes;
	    nodes = null; // Free memory

	    // Sort the IPs, ensuring the assumption that if for a node there is
	    // a brother, it will be next to it. This assumption will stay for
	    // next layers too.
	    Arrays.parallelSort(currentLayer, IP_COMPARATOR);

	    // Run until there are only one element in the list (all nodes are
	    // sub children of the node)
	    int currentLayerSize = currentLayer.length;
	    while (currentLayerSize > 1) {

		// The next IP layer that is currently constructed
		IPNode[] nextLayer = null;

		final int numberOfJobs;
		boolean parallel;
		synchronized (numberOfUsedThreadsLock) {
		    final int availableProcessors = Math.max(1,
			    Runtime.getRuntime().availableProcessors() - numberOfUsedThreads);
		    final int requiredProcessors = Math.max(1, 1 + (currentLayerSize / m_parallelThreshold));
		    numberOfJobs = Math.min(m_maxThreads, Math.min(availableProcessors, requiredProcessors));
		    if (parallel = m_parallel && numberOfJobs != 1)
			numberOfUsedThreads += numberOfJobs;
		}

		if (parallel) {
		    List<InformationAlgoLayerRunner> runners = null;
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
				    if (currentLayer[toIndex - 1].ip.isBrother(currentLayer[toIndex].ip)) {
					toIndex++;
				    }
				}
				final InformationAlgoLayerRunner runner = new InformationAlgoLayerRunner(currentLayer,
					fromIndex, toIndex, totalSize, m_ruleWeight);
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
			for (final InformationAlgoLayerRunner runner : runners) {
			    nextLayerSize += runner.nextLayerSize;
			}
			nextLayer = new IPNode[nextLayerSize];
			int offSet = 0;
			for (final InformationAlgoLayerRunner runner : runners) {
			    System.arraycopy(runner.nextLayer, 0, nextLayer, offSet, runner.nextLayerSize);
			    offSet += runner.nextLayerSize;
			}
			currentLayerSize = nextLayerSize;
		    }

		    synchronized (numberOfUsedThreadsLock) {
			numberOfUsedThreads -= numberOfJobs;
		    }
		}

		// Check parallel flag again and don't use the else block
		// because maybe the flag got changed during the parallel block
		if (!parallel) {
		    // No parallel, run normal on single thread
		    final InformationAlgoLayerRunner runner = new InformationAlgoLayerRunner(currentLayer, 0,
			    currentLayerSize, totalSize, m_ruleWeight);
		    synchronized (numberOfUsedThreadsLock) {
			numberOfUsedThreads++;
		    }
		    runner.run();
		    synchronized (numberOfUsedThreadsLock) {
			numberOfUsedThreads--;
		    }
		    nextLayer = runner.nextLayer;
		    currentLayerSize = runner.nextLayerSize;
		}

		// Current layer is finished, move to next layer
		currentLayer = nextLayer;
	    }

	    // Only one element in layer, it is the parent node of all others
	    IPNode root = currentLayer[0];
	    return root.bestSubnets.toArrayList();
	}

    }

    private static class InformationAlgoLayerRunner implements Runnable {

	private final IPNode[] currentLayer;
	private final int fromIndex;
	private final int toIndex;
	private final IPNode[] nextLayer;
	private int nextLayerSize;
	private final int totalSize;
	private final double ruleWeight;

	public InformationAlgoLayerRunner(final IPNode[] currentLayer, final int fromIndex, final int toIndex,
		final int totalSize, final double ruleWeight) {
	    this.currentLayer = currentLayer;
	    this.fromIndex = fromIndex;
	    this.toIndex = toIndex;
	    this.nextLayer = new IPNode[toIndex - fromIndex];
	    this.totalSize = totalSize;
	    this.ruleWeight = ruleWeight;
	}

	@Override
	public void run() {
	    // Run over all elements, for each element construct his parent
	    // element for the next layer by checking if his brother exist and
	    // if so - merge them, else construct the parent base only on the
	    // one current node. Run over all elements except the last one
	    // (length = size -1) so we don't get out of bounds when searching
	    // for brother (always in [curenntIndex + 1])
	    final int fence = toIndex - 1;
	    int index = fromIndex;
	    nextLayerSize = 0;
	    while (index < fence) {

		// The current node
		IPNode current = currentLayer[index];

		// The next node, relevant only if brother of nodeA
		IPNode brother = currentLayer[index + 1];

		// The IP of the current node
		IP ip = current.ip;

		// The IP of the constructed parent
		IP parentIp = ip.getParent();

		// The parent node of the current node (possible of brother
		// candidate too, if they are acutely brothers), currently
		// constructed
		IPNode parent = new IPNode(parentIp);

		// If nodeA and nodeB are brothers:
		if (ip.isBrother(brother.ip)) {

		    // The number of hits in the constructed parent node
		    int size = parent.size = current.size + brother.size;
		    parent.totalHitsCount = current.totalHitsCount + brother.totalHitsCount;

		    // Probability of parent node out of the total hits:
		    // size / totalSize
		    double probability = size / (double) totalSize;

		    // The optimal compress size if the parent node chosen as a
		    // union single subnetwork:
		    // union = size * (log(subnetwork size) +
		    // log(1/probability)) + ruleWeight
		    double union = size * (parentIp.getSubnetBitsNum() - Utility.log2(probability)) + ruleWeight;

		    // The optimal compress size if the parent node chosen as
		    // separated small subnetworks:
		    // separated = (nodeA optimal) + (nodeB optimal)
		    double separated = current.compressSize + brother.compressSize;

		    // Choose optimal (minimum) choice between union subnetwork
		    // or separated small subnetworks. using <= prefer less
		    // subnetworks
		    if (union <= separated) {
			// Using union subnetwork
			parent.compressSize = union;

			// Subnetwork is the parent subnetwork
			parent.bestSubnets = new UnionList<>(parent.toSuggestion());
		    } else {
			// Using separated small subnetworks
			parent.compressSize = separated;

			// Union the two subnetworks from both child nodes
			parent.bestSubnets = current.bestSubnets.transferElementsFrom(brother.bestSubnets);
		    }

		    // Used the current node and next node, increase index by 2
		    index++;
		} else {
		    // Current node and the candidate brother are not brothers.
		    // Copy all values from current node to parent node
		    parent.size = current.size;
		    parent.totalHitsCount = current.totalHitsCount;
		    parent.compressSize = current.compressSize;
		    parent.bestSubnets = current.bestSubnets;
		}

		// Add the finished parent node to next layer list
		nextLayer[nextLayerSize++] = parent;

		index++;
	    }

	    // Check last element in list - if it was brother of one before last
	    // meaning no more action is require, else - create his parent node
	    // and copy his properties (no brother for sure)
	    IPNode beforeLast = currentLayer[toIndex - 2];
	    IPNode last = currentLayer[toIndex - 1];
	    IP ip = last.ip;
	    if (!beforeLast.ip.isBrother(ip)) {
		IPNode parent = new IPNode(ip.getParent());
		parent.size = last.size;
		parent.totalHitsCount = last.totalHitsCount;
		parent.compressSize = last.compressSize;
		parent.bestSubnets = last.bestSubnets;
		nextLayer[nextLayerSize++] = parent;
	    }
	}
    }

    /**
     * Create list of IPNodes from iterable of hits
     * 
     * @param hits
     *            iterable object of hits
     * @param ipAttTypeId
     *            id of the IP attribute
     * @return list of IPNodes constructed from the hits
     * @throws NullPointerException
     *             if hits are null, or one of the hits are null
     * @throws IllegalArgumentException
     *             if one of the hits doesn't contains the desire attribute
     */
    private IPNode[] toIPNodes(final Set<UniqueHit> hits, final int ipAttTypeId) {
	final ArrayList<IPNode> allNodes = new ArrayList<>(hits.size());

	for (final Iterator<UniqueHit> it = hits.iterator(); it.hasNext();) {
	    final UniqueHit hit = it.next();
	    final IPAttribute att = (IPAttribute) hit.getAttribute(ipAttTypeId);
	    if (att == null) {
		throw new IllegalArgumentException("One of the hits doesn't have the desire attribute");
	    }
	    final IPNode ipNode = new IPNode(att.getIp());
	    ipNode.compressSize = m_ruleWeight;
	    ipNode.size = 1;
	    ipNode.totalHitsCount = hit.getAmount();
	    ipNode.bestSubnets = new UnionList<>(ipNode.toSuggestion());
	    allNodes.add(ipNode);
	}
	allNodes.sort(IP_COMPARATOR);

	// Init list with approximate size depends on nodes list size
	final ArrayList<IPNode> uniqueNodes = new ArrayList<>((int) (allNodes.size() * UNIQUE_LIST_FACTOR));
	final Iterator<IPNode> it = allNodes.iterator();
	if (it.hasNext()) {
	    IPNode lastNode = it.next();
	    uniqueNodes.add(lastNode);

	    while (it.hasNext()) {
		final IPNode node = it.next();
		if (lastNode.ip.equals(node.ip)) {
		    lastNode.size++;
		    lastNode.totalHitsCount += node.totalHitsCount;
		} else {
		    uniqueNodes.add(node);
		    lastNode = node;
		}
		lastNode.bestSubnets = new UnionList<>(lastNode.toSuggestion());
	    }
	}
	return uniqueNodes.toArray(new IPNode[0]);
    }

    /**
     * Configuration check. Used to check the static final fields of this
     * algorithm.
     * 
     * @throws InternalError
     *             if one of the fiels is not legal
     */
    @SuppressWarnings("unused")
    private static void configCheck() {
	if (Double.isNaN(DEFAULT_RULE_WIEGHT) || 0 >= DEFAULT_RULE_WIEGHT) {
	    throw new InternalError("DEFAULT_RULE_WIEGHT(" + DEFAULT_RULE_WIEGHT + ") should be > 0");
	}
	if (Double.isNaN(UNIQUE_LIST_FACTOR) || UNIQUE_LIST_FACTOR <= 0 || UNIQUE_LIST_FACTOR > 1) {
	    throw new InternalError("UNIQUE_LIST_FACTOR(" + UNIQUE_LIST_FACTOR + ") should be in range (0, 1]");
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
    private static class IPNode {

	private final IP ip;

	private int size;

	private int totalHitsCount;

	private double compressSize;

	private UnionList<SubnetSuggestion> bestSubnets;

	public IPNode(final IP ip) {
	    this.ip = ip;
	}

	@Override
	public boolean equals(final Object o) {
	    if (o == this) {
		return true;
	    } else if (!(o instanceof IPNode)) {
		return false;
	    }

	    IPNode other = (IPNode) o;
	    return ip.equals(other.ip);
	}

	@Override
	public int hashCode() {
	    return ip.hashCode();
	}

	@Override
	public String toString() {
	    final StringBuilder builder = new StringBuilder();
	    builder.append(ip);
	    builder.append(" size=");
	    builder.append(size);
	    builder.append(" compressSize=");
	    builder.append(compressSize);
	    builder.append(" nets=");
	    if (bestSubnets == null) {
		builder.append("null");
	    } else {
		builder.append('[');
		final Iterator<SubnetSuggestion> it = bestSubnets.iterator();
		final String spacer = ", ";

		if (it.hasNext()) { // Have at least one elements
		    do {
			final SubnetSuggestion node = it.next();
			builder.append(node.ip);
			if (!it.hasNext())
			    break;
			builder.append(spacer);
		    } while (true);
		}
		builder.append(']');
	    }
	    return builder.toString();
	}

	private SubnetSuggestion toSuggestion() {
	    final SubnetSuggestion suggestion = new SubnetSuggestion();
	    suggestion.ip = ip;
	    suggestion.uniqueHitsCount = size;
	    suggestion.totalHitsCount = totalHitsCount;
	    suggestion.score = 1 / compressSize;
	    return suggestion;
	}

    }

    private static class SubnetSuggestion {

	private IP ip;

	private int uniqueHitsCount;

	private int totalHitsCount;

	private double score;

	@Override
	public String toString() {
	    return ip.toString() + " size=" + uniqueHitsCount + " score=" + score;
	}
    }

}
