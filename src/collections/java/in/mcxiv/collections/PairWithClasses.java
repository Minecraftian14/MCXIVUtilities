package in.mcxiv.collections;

public class PairWithClasses<A, B> {
    private A a;
    private Class<A> a_clazz;
    private B b;
    private Class<B> b_clazz;

    public PairWithClasses() {
        this(null, null);
    }

    public PairWithClasses(A a, B b) {
        setA(a);
        setB(b);
    }

    @SuppressWarnings("unchecked")
    public void setA(A a) {
        this.a = a;
        this.a_clazz = (Class<A>) a.getClass();
    }

    @SuppressWarnings("unchecked")
    public void setB(B b) {
        this.b = b;
        this.b_clazz = (Class<B>) b.getClass();
    }
}
