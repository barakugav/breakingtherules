package breakingtherules.session;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import breakingtherules.dao.DaoConfig;
import breakingtherules.dao.HitsDao;
import breakingtherules.dao.ParseException;
import breakingtherules.dao.csv.CSVDaoConfig;
import breakingtherules.dao.csv.CSVHitsDao;
import breakingtherules.firewall.Filter;
import breakingtherules.firewall.Hit;
import breakingtherules.firewall.Rule;

/**
 * Allows creating a new job from a given CSV hits file
 */
@Component
public class JobCreator {

    /**
     * The Hits DAO that is used to write all of the hits to memory. Might be
     * CSV HitsDao but not necessarily
     */
    @Autowired
    @Qualifier("hitsDao")
    private HitsDao m_hitsDao;

    /**
     * The CSV DAO used to read the hits from the given CSV file
     */
    @Autowired
    @Qualifier("csvHitsDao")
    private CSVHitsDao m_csvHitsDao;

    /**
     * Create a new job, with the given job name and the given hits
     * 
     * @param jobName
     *            The name of the new job. Must be different from existing names
     * @param hitsFile
     *            A CSV file with all of the hits that should be processed in
     *            this job
     * @param columnTypes
     *            The order of the columns in the CSV file
     * @throws IOException
     *             if any I/O errors occurs.
     * @throws ParseException
     *             if failed to parse file.
     * @throws NullPointerException
     *             if the file is null.
     */
    public void createJob(final String jobName, final MultipartFile hitsFile, final List<Integer> columnTypes)
	    throws IOException, ParseException {

	// TODO - treat return value from initRepository.
	DaoConfig.initRepository(jobName);

	final File fileDestination = new File(new File(CSVDaoConfig.getHitsFile(jobName)).getAbsolutePath());
	hitsFile.transferTo(fileDestination);
	m_csvHitsDao.setColumnTypes(columnTypes);
	final Iterable<Hit> hits = m_csvHitsDao.getHits(jobName, Collections.emptyList(), Filter.ANY_FILTER);
	m_hitsDao.initJob(jobName, hits);

	final Job job = new Job();
	job.m_name = jobName;
	job.m_originalRule = new Rule(Filter.ANY_FILTER);
	job.m_rules = new ArrayList<>();
	job.updateRulesFile();
    }
}
