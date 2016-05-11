package breakingtherules.utilities;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class UnionList<E> implements Iterable<E> {

    private Node<E> first;
    private Node<E> last;

    @SafeVarargs
    public UnionList(E... initElements) {
	if (initElements.length > 0) {
	    first = last = new Node<>(initElements[0]);
	    for (int i = 1; i < initElements.length; i++) {
		Node<E> node = new Node<>(initElements[i]);
		last.next = node;
		last = node;
	    }
	}
    }

    public void addLast(E e) {
	Node<E> node = new Node<>(e);
	if (last == null) {
	    first = node;
	} else {
	    last.next = node;
	}
	last = node;
    }

    public UnionList<E> unionTo(UnionList<E> other) {
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

    @Override
    public Iterator<E> iterator() {
	return new LinkedListIterator<>(first);
    }

    public List<E> toList() {
	List<E> l = new ArrayList<>();
	for (E e : this)
	    l.add(e);
	return l;
    }

    @Override
    public String toString() {
	if (first == null)
	    return "[]";

	StringBuilder builder = new StringBuilder('[');
	final String spacer = ", ";
	for (Node<E> cursor = first; cursor != last; cursor = cursor.next) {
	    builder.append(cursor.data);
	    builder.append(spacer);
	}
	builder.append(last.data);
	builder.append(']');
	return builder.toString();
    }

    private static class Node<E> {

	private E data;
	private Node<E> next;

	private Node(E e) {
	    data = e;
	}

    }

    private static class LinkedListIterator<E> implements Iterator<E> {

	private Node<E> cursor;

	private LinkedListIterator(Node<E> begin) {
	    cursor = begin;
	}

	@Override
	public boolean hasNext() {
	    return cursor != null;
	}

	@Override
	public E next() {
	    if (cursor == null)
		throw new NoSuchElementException();
	    E data = cursor.data;
	    cursor = cursor.next;
	    return data;
	}

    }

}
