package breakingtherules.utilities;

public class Triplet<A, B, C> {

    public A first;
    public B second;
    public C third;
    
    public Triplet(A a, B b, C c) {
	first = a;
	second = b;
	third = c;
    }
    
    @Override
    public boolean equals(Object o) {
	if (o == null) return false;
	if (!(o instanceof Triplet<?, ?, ?>)) {
	    return false;
	}
	Triplet<?, ?, ?> other = (Triplet<?, ?, ?>) o;
	return first == other.first && second == other.second && third == other.third;
    }
    
    @Override
    public int hashCode() {
	int code = 0;
	code += first.hashCode();
	code <<= 10;
	code += second.hashCode();
	code <<= 10;
	code += third.hashCode();
	return code;
    }
}
