package breakingtherules.dto;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * DTO object that have a list of elements out of a bigger total elements list.
 * <p>
 * This DTO saves the indexes of this elements list out of the total list, and
 * some parameters about the total list.
 * <p>
 * 
 * @author Barak Ugav
 * @author Yishai Gronich
 * 
 * @param <E>
 *            The type list elements.
 */
public class ListDto<E> implements Iterable<E> {

    /**
     * Data list to be passed
     */
    private final List<E> m_data;

    /**
     * The 0-index of the first element, out of all the elements of the total
     * list
     */
    private final int m_startIndex;

    /**
     * The 0-index + 1 of the last element, out of all the elements of the total
     * list
     */
    private final int m_endIndex;

    /**
     * Size of the total list that contains this list
     */
    private final int m_total;

    /**
     * Constructor
     * 
     * @param data
     *            list of the elements
     * @param startIndex
     *            the start index of this elements list out of the total
     *            elements list
     * @param endIndex
     *            the end index of this elements list out of the total elements
     *            list
     * @param total
     *            the size of the total elements list
     */
    public ListDto(final List<E> data, final int startIndex, final int endIndex, final int total) {
	m_total = total;
	m_startIndex = startIndex;
	m_endIndex = endIndex;
	m_data = data;
    }

    /**
     * Get the elements list
     * 
     * @return list of the elements this DTO has
     */
    public List<E> getData() {
	return m_data;
    }

    /**
     * Get the start index of this elements list out of the total elements list
     * 
     * @return start index of this elements list out of the total elements list
     */
    public int getStartIndex() {
	return m_startIndex;
    }

    /**
     * Get the end index of this elements list out of the total elements list
     * 
     * @return end index of this elements list out of the total elements list
     */
    public int getEndIndex() {
	return m_endIndex;
    }

    /**
     * Get the size of the elements list this DTO has
     * 
     * @return Size of the internal list
     */
    public int getSize() {
	return m_data != null ? m_data.size() : 0;
    }

    /**
     * Get the size of the total elements list
     * 
     * @return size of the total elements list
     */
    public int getTotal() {
	return m_total;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<E> iterator() {
	return m_data != null ? m_data.iterator() : Collections.emptyIterator();
    }

}
