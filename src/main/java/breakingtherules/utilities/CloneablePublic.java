package breakingtherules.utilities;

/**
 * The CloneablePublic interface extends the normal <code>Cloneable</code>
 * interface and force the implementor to set the method visibility of
 * <code>clone</code> method to public
 */
public interface CloneablePublic extends Cloneable {

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#clone()
     */
    public CloneablePublic clone();

}
