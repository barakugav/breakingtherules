package breakingtherules.dao;

public class Source extends Attribute {

    private IP m_ip;
    
    public Source(String ip) {
	this(IP.fromString(ip));
    }
    
    public Source(IP ip) {
	super(AttType.Source);
	m_ip = ip;
    }

    @Override
    public boolean contain(Attribute other) {
	if (other == null)
	    return false;
	if (!(other instanceof Source))
	    return false;
	
	Source o = (Source) other;
	return m_ip.contain(o.m_ip);
    }

}
