package breakingtherules.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import breakingtherules.dao.HitsDao;
import breakingtherules.firewall.Filter;
import breakingtherules.firewall.Hit;

@RestController
public class HitsController {

    @Autowired
    private HitsDao hitsDao;

    @RequestMapping("/hits")
    public List<Hit> hits() {
	// Empty filter
	Filter f = new Filter();
	List<Hit> hits = hitsDao.getHits(f);
	return hits;
    }
}
