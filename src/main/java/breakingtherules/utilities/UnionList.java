package breakingtherules.utilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * The UnionList is a basic linked list that provide minimal interface used only
 * to initialize it with elements and to union it to other lists using the
 * method {@link #transferElementsFrom(UnionList)}.
 * 
 * @author Barak Ugav
 * @author Yishai Gronich
 * @param <E>
 *            type of elements the list will contain
 */
public class UnionList<E> implements Iterable<E> {

    /**
     * Reference to first node in the list
     */
    private Node<E> first;

    /**
     * Reference to last node in the list
     */
    private Node<E> last;

    /**
     * Construct new empty union list.
     */
    public UnionList() {
    }

    /**
     * Construct new union list with one element.
     * 
     * @param initElement
     *            initialize element.
     */
    public UnionList(final E initElement) {
	first = last = new Node<>(initElement);
    }

    /**
     * Construct new list with initialize elements.
     * 
     * @param firstInitElement
     *            first initialize element.
     * @param secondInitElement
     *            second initialize element.
     * @param initElements
     *            initialize elements
     * @throws NullPointerException
     *             if the initialize elements array is null.
     */
    @SafeVarargs
    public UnionList(final E firstInitElement, final E secondInitElement, final E... initElements) {
	Node<E> l = first = new Node<>(firstInitElement);
	l = (l.next = new Node<>(secondInitElement));
	for (int i = initElements.length; i-- != 0;) {
	    l = (l.next = new Node<>(initElements[i]));
	}
	last = l;
    }

    /**
     * Construct new list with initialize elements from collection.
     * 
     * @param c
     *            the elements source collection.
     * @throws NullPointerException
     *             if the collection is null.
     */
    public UnionList(final Collection<? extends E> c) {
	this(c.iterator());
    }

    /**
     * Construct new list with initialize elements from iterator.
     * 
     * @param it
     *            the elements source iterator.
     * @throws NullPointerException
     *             if the iterator is null.
     */
    public UnionList(final Iterator<? extends E> it) {
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
     * Transfer all elements from other list to this list. Union.
     * <p>
     * This method <b>CONSUME</b> the other list. After a call to this method,
     * the other list will be empty and will not contain any elements, and all
     * it's elements will be contained in this list.
     * 
     * @param other
     *            another union list to consume elements from
     * @return the united list - this list
     * @throws NullPointerException
     *             if the other list is null
     */
    public UnionList<E> transferElementsFrom(final UnionList<E> other) {
	if (other != this) {
	    if (last == null) {
		first = other.first;
	    } else {
		last.next = other.first;
	    }
	    last = other.last;
	    other.first = null;
	    other.last = null;
	}
	return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<E> iterator() {
	return new LinkedListIterator<>(first);
    }

    /**
     * Get all elements of this list to new array list.
     * 
     * @return new array list that contains all elements from this list
     */
    public ArrayList<E> toArrayList() {
	final ArrayList<E> l = new ArrayList<>();
	for (Node<E> node = first; node != null; node = node.next) {
	    l.add(node.data);
	}
	return l;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	final Node<E> f = first, l = last;
	if (f == null) {
	    return "[]";
	}

	final StringBuilder builder = new StringBuilder('[');
	final String spacer = ", ";
	for (Node<E> cursor = f; cursor != l; cursor = cursor.next) {
	    builder.append(cursor.data);
	    builder.append(spacer);
	}
	builder.append(l.data);
	builder.append(']');
	return builder.toString();
    }

    /**
     * The Node class is a wrapper for an element in the union list that store a
     * reference to the next node in the list.
     * 
     * @author Barak Ugav
     * @author Yishai Gronich
     * @param <E>
     *            type of element the node will wrap
     */
    private static class Node<E> {

	/**
	 * Data of the node
	 */
	private final E data;

	/**
	 * Reference to next node in the list
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

    }

    /**
     * The LinkedListIterator is an iterator used to iterate over linked list.
     * <p>
     * The iterator store a cursor to one of the nodes and advance only forward
     * by jumping from node to another.
     * 
     * @author Barak Ugav
     * @author Yishai Gronich
     * @param <E>
     *            type of elements the nodes contained
     */
    private static class LinkedListIterator<E> implements Iterator<E> {

	/**
	 * Current cursor of the iterator
	 */
	private Node<E> cursor;

	/**
	 * Construct new iterator that will iterating over nodes from a node
	 * forward.
	 * 
	 * @param begin
	 *            beginning node to iterate from
	 */
	private LinkedListIterator(final Node<E> begin) {
	    cursor = begin;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext() {
	    return cursor != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Iterator#next()
	 */
	@Override
	public E next() {
	    final Node<E> c = cursor;
	    if (c == null)
		throw new NoSuchElementException();
	    final E data = c.data;
	    cursor = c.next;
	    return data;
	}

    }

}
