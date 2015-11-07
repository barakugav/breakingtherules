package controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dao.Filter;
import dao.Hit;
import dao.HitsDao;

@RestController
public class HitsController {

    @Autowired
    private HitsDao hitsDao;
    
    @RequestMapping("/hits")
    public List<Hit> hits() {
	Filter f = new Filter();
	List<Hit> hits = hitsDao.getHits(f);
	return hits;
    }
}
