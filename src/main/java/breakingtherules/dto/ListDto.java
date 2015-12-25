package breakingtherules.dto;

import java.util.List;

/**
 * DTO object that have a list of elements out of a bigger total elements list.
 * This DTO saves the indexes of this elements list out of the total list, and
 * some parameters about the total list
 */
public class ListDto<T> {

    /**
     * Data list to be passed
     */
    private final List<T> m_data;

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
    public ListDto(List<T> data, int startIndex, int endIndex, int total) {
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
    public List<T> getData() {
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
     * @return
     */
    public int getSize() {
	if (m_data == null) {
	    return 0;
	}
	return m_data.size();
    }

    /**
     * Get the size of the total elements list
     * 
     * @return size of the total elements list
     */
    public int getTotal() {
	return m_total;
    }

}