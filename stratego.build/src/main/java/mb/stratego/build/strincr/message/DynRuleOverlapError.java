package mb.stratego.build.strincr.message;

import org.spoofax.interpreter.terms.IStrategoTerm;

public class DynRuleOverlapError extends Message {
    private final String lhs1;
    private final String def1;
    private final String lhs2;
    private final String def2;

    public DynRuleOverlapError(IStrategoTerm locationTerm, String lhs1, String def1, String lhs2,
        String def2, MessageSeverity severity, long lastModified) {
        super(locationTerm, severity, lastModified);
        this.lhs1 = lhs1;
        this.def1 = def1;
        this.lhs2 = lhs2;
        this.def2 = def2;
    }

    @Override public String getMessage() {
        return "Overlapping left-hand-sides for dynamic rule " + locationTermString + ": " + lhs1 + " in "
            + def1 + " overlaps with " + lhs2 + " in " + def2 + ".";
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;
        if(!super.equals(o))
            return false;

        DynRuleOverlapError that = (DynRuleOverlapError) o;

        if(!lhs1.equals(that.lhs1))
            return false;
        if(!def1.equals(that.def1))
            return false;
        if(!lhs2.equals(that.lhs2))
            return false;
        return def2.equals(that.def2);
    }

    @Override public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + lhs1.hashCode();
        result = 31 * result + def1.hashCode();
        result = 31 * result + lhs2.hashCode();
        result = 31 * result + def2.hashCode();
        return result;
    }
}
