package breakingtherules.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * A group of elements, provide minimal interface used only to initialize it
 * with elements and to union it to other groups using the method
 * {@link #transferElementsFrom(UntionGroup)}.
 *
 * @author Barak Ugav
 * @author Yishai Gronich
 *
 * @param <E>
 *            type of elements the group will contain
 */
public class UntionGroup<E> {

    /**
     * Implementation notes.
     *
     * The group is implemented by a one way linked list. When initializing the
     * list each element is stored stored in it's own node.
     *
     * When a union is operated, the union is done in O(1) operations because
     * the nodes are not copied - the union CONSUME the other list.
     */

    /**
     * First node in the list
     */
    private Node<E> first;

    /**
     * Last node in the list
     */
    private Node<E> last;

    /**
     * Construct new empty union group.
     */
    public UntionGroup() {
    }

    /**
     * Construct new group with initialize elements from collection.
     *
     * @param c
     *            the elements source collection.
     * @throws NullPointerException
     *             if the collection is null.
     */
    public UntionGroup(final Collection<? extends E> c) {
	this(c.iterator());
    }

    /**
     * Construct new union group with one element.
     *
     * @param initElement
     *            initialize element.
     */
    public UntionGroup(final E initElement) {
	first = last = new Node<>(initElement);
    }

    /**
     * Construct new group with initialize elements.
     *
     * @param firstInitElement
     *            first initialize element.
     * @param secondInitElement
     *            second initialize element.
     * @param initElements
     *            initialize elements.
     * @throws NullPointerException
     *             if the initialize elements array is null.
     */
    @SafeVarargs
    public UntionGroup(final E firstInitElement, final E secondInitElement, final E... initElements) {
	Node<E> l = first = new Node<>(firstInitElement);
	l = l.next = new Node<>(secondInitElement);
	for (int i = initElements.length; i-- != 0;)
	    l = l.next = new Node<>(initElements[i]);
	last = l;
    }

    /**
     * Construct new group with initialize elements from iterator.
     *
     * @param it
     *            the elements source iterator.
     * @throws NullPointerException
     *             if the iterator is null.
     */
    public UntionGroup(final Iterator<? extends E> it) {
	if (it.hasNext()) {
	    first = last = new Node<>(it.next());
	    while (it.hasNext()) {
		final Node<E> node = new Node<>(it.next());
		last.next = node;
		last = node;
	    }
	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object o) {
	if (o == this)
	    return true;
	if (!(o instanceof UntionGroup))
	    return false;

	final UntionGroup<?> other = (UntionGroup<?>) o;

	for (Node<?> c1 = first, c2 = other.first;; c1 = c1.next, c2 = c2.next) {
	    if (c1 == null || c2 == null)
		return c1 == null && c2 == null;
	    if (!Objects.equals(c1.data, c2.data))
		return false;
	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
	int h = 17;
	for (Node<E> cursor = first; cursor != null; cursor = cursor.next)
	    h = h * 31 + Objects.hashCode(cursor.data);
	return h;
    }

    /**
     * Get all elements of this group to new list.
     *
     * @return new list that contains all elements from this group.
     */
    public List<E> toList() {
	final ArrayList<E> l = new ArrayList<>();
	for (Node<E> node = first; node != null; node = node.next)
	    l.add(node.data);
	return l;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
	final Node<E> f = first, l = last;
	if (f == null)
	    return "[]";

	final StringBuilder builder = new StringBuilder();
	builder.append('[');
	for (Node<E> cursor = f; cursor != l; cursor = cursor.next) {
	    builder.append(cursor.data);
	    builder.append(", ");
	}
	builder.append(l.data);
	builder.append(']');
	return builder.toString();
    }

    /**
     * Transfer all elements from other group to this group. Union.
     * <p>
     * This method <b>CONSUME</b> the other group. After a call to this method,
     * the other group will be empty and will not contain any elements, and all
     * it's elements will be contained in this group.
     *
     * @param other
     *            another union group to consume elements from.
     * @return the united group - this group.
     * @throws NullPointerException
     *             if the other group is null.
     */
    public UntionGroup<E> transferElementsFrom(final UntionGroup<E> other) {
	if (other != this) {
	    if (last == null)
		first = other.first;
	    else
		last.next = other.first;
	    last = other.last;
	    other.first = null;
	    other.last = null;
	}
	return this;
    }

    /**
     * The Node class is a wrapper for an element in the group that store a
     * reference to the next node in the linked list.
     *
     * @author Barak Ugav
     * @author Yishai Gronich
     *
     * @param <E>
     *            type of element the node will wrap
     */
    private static class Node<E> {

	/**
	 * Data of the node
	 */
	private final E data;

	/**
	 * Next node in the list
	 */
	private Node<E> next;

	/**
	 * Construct new node with an element
	 *
	 * @param e
	 *            element of the node
	 */
	private Node(final E e) {
	    data = e;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(final Object o) {
	    if (o == this)
		return true;
	    if (!(o instanceof Node))
		return false;

	    final Node<?> other = (Node<?>) o;
	    return Objects.equals(data, other.data);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
	    return Objects.hashCode(data);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
	    return String.valueOf(data);
	}

    }

}
