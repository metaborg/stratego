package mb.stratego.build;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.TermVisitor;

public class TermSizeTermVisitor extends TermVisitor {
    long size = 0;

    @Override public void preVisit(IStrategoTerm term) {
        size++;
    }
}
