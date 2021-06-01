package mb.stratego.build.termvisitors;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.TermVisitor;
import org.spoofax.terms.util.TermUtils;

public class CountErrT extends TermVisitor {
    public long count = 0;

    public static long countErrT(IStrategoTerm ast) {
        final CountErrT termSizeTermVisitor = new CountErrT();
        termSizeTermVisitor.visit(ast);
        return termSizeTermVisitor.count;
    }

    @Override public void preVisit(IStrategoTerm term) {
        if(TermUtils.isAppl(term, "ErrT", 0)) {
            count++;
        }
        if(TermUtils.isAppl(term, "Cast", 1) && TermUtils.isApplAt(term, 0, "Fail", 0)) {
            count++;
        }
    }
}
