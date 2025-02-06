package mb.stratego.build.strincr.message;

import org.spoofax.interpreter.terms.IStrategoTerm;

public class UnresolvedStrategy extends Message {
    private final String strategyName;
    public final int strategyArity;
    public final int termArity;

    public UnresolvedStrategy(IStrategoTerm locationTerm, String strategyName, int strategyArity, int termArity,
        MessageSeverity severity, long lastModified) {
        super(locationTerm, severity, lastModified);
        this.strategyName = strategyName;
        this.strategyArity = strategyArity;
        this.termArity = termArity;
    }

    @Override
    public String getMessage() {
        return "Unresolved strategy " + strategyName + " with arity " + strategyArity + "/" + termArity + ".";
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;
        if(!super.equals(o))
            return false;

        UnresolvedStrategy that = (UnresolvedStrategy) o;

        if(strategyArity != that.strategyArity)
            return false;
        return termArity == that.termArity;
    }

    @Override public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + strategyArity;
        result = 31 * result + termArity;
        return result;
    }
}
