package breakingtherules.utilities;

import java.util.ArrayList;
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
     * Construct new list with initialize elements
     * 
     * @param initElements
     *            initialize elements
     */
    @SafeVarargs
    public UnionList(final E... initElements) {
	if (initElements.length > 0) {
	    first = last = new Node<>(initElements[0]);
	    for (int i = 1; i < initElements.length; i++) {
		final Node<E> node = new Node<>(initElements[i]);
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
	if (last == null) {
	    first = other.first;
	} else {
	    last.next = other.first;
	}
	last = other.last;
	other.first = null;
	other.last = null;
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
	for (E e : this) {
	    l.add(e);
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
     * 
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
