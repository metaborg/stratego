package mb.stratego.build.termvisitors;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.TermVisitor;

public class TermSize extends TermVisitor {
    public long size = 0;

    @Override public void preVisit(IStrategoTerm term) {
        size++;
    }
}
