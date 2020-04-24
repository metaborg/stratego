package mb.stratego.build.termvisitors;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.TermVisitor;

public class TermSize extends TermVisitor {
    public long size = 0;

    public static long computeTermSize(IStrategoTerm ctree) {
        final TermSize termSizeTermVisitor = new TermSize();
        termSizeTermVisitor.visit(ctree);
        return termSizeTermVisitor.size;
    }

    @Override public void preVisit(IStrategoTerm term) {
        size++;
    }
}
