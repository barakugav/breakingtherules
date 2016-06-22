package breakingtherules.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import breakingtherules.dao.ParseException;
import breakingtherules.firewall.Attribute;
import breakingtherules.firewall.Destination;
import breakingtherules.firewall.Filter;
import breakingtherules.firewall.Service;
import breakingtherules.firewall.Source;
import breakingtherules.session.Job;
import breakingtherules.session.NoCurrentJobException;

/**
 * Controller that allows setting and getting the current job's filter.
 * <p>
 * 
 * @author Barak Ugav
 * @author Yishai Gronich
 *
 * @see Filter
 * @see Job
 */
@RestController
public class FilterController {

    /**
     * The session job.
     */
    @Autowired
    private Job m_job;

    /**
     * Set the filter to a new one.
     * 
     * @param source
     *            the new source filter.
     * @param destination
     *            the new destination filter.
     * @param service
     *            the new service filter
     * @throws IllegalArgumentException
     *             if the filter's attributes strings are invalid.
     * @throws IOException
     *             if any I/O errors occurs when trying to update the
     *             statistics.
     * @throws ParseException
     *             if any parse errors occurs when trying to update the
     *             statistics.
     * @throws NoCurrentJobException
     *             if the job wasn't set yet.
     */
    @RequestMapping(value = "/filter", method = RequestMethod.PUT, params = { "source", "destination", "service" })
    public void setFilter(@RequestParam(value = "source") final String source,
	    @RequestParam(value = "destination") final String destination,
	    @RequestParam(value = "service") final String service) throws IOException, ParseException {
	final List<Attribute> filterAtts = new ArrayList<>();
	filterAtts.add(Source.valueOf(source));
	filterAtts.add(Destination.valueOf(destination));
	filterAtts.add(Service.valueOf(service));
	final Filter newFilter = new Filter(filterAtts);
	m_job.setFilter(newFilter);
    }

    /**
     * Get the current job's filter.
     * 
     * @return the filter of the current job.
     * @throws NoCurrentJobException
     *             if the job wasn't set yet.
     */
    @RequestMapping(value = "/filter", method = RequestMethod.GET)
    public Filter getFilter() {
	return m_job.getFilter();
    }

}
