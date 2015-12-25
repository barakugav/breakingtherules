package breakingtherules.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import breakingtherules.firewall.Attribute;
import breakingtherules.firewall.Destination;
import breakingtherules.firewall.Filter;
import breakingtherules.firewall.Service;
import breakingtherules.firewall.Source;
import breakingtherules.session.Job;

@RestController
public class FilterController {

    @Autowired
    private Job m_job;

    @RequestMapping(value = "/filter", method = RequestMethod.PUT, params = { "source", "destination", "service" })
    public void setFilter(@RequestParam(value = "source") String source,
	    @RequestParam(value = "destination") String destination, @RequestParam(value = "service") String service)
		    throws IllegalArgumentException {
	List<Attribute> filterAtts = new ArrayList<Attribute>();
	filterAtts.add(new Source(source));
	filterAtts.add(new Destination(destination));
	filterAtts.add(new Service(service));
	Filter newFilter = new Filter(filterAtts);
	m_job.setFilter(newFilter);
    }

    @RequestMapping(value = "/filter", method = RequestMethod.GET)
    public Filter getFilter() {
	Filter filter = m_job.getFilter();
	return filter;
    }

}