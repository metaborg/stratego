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
        return "Overlapping left-hand-sides for dynamic rule " + locationTerm + ": " + lhs1 + " in "
            + def1 + " overlaps with " + lhs2 + " in " + def2 + ".";
    }
}
