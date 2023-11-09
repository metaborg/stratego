package strategolib.terms;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.metaborg.util.collection.CapsuleUtil;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermPrinter;
import org.spoofax.interpreter.terms.TermType;
import org.spoofax.terms.StrategoTerm;
import org.spoofax.terms.TermFactory;
import org.strategoxt.lang.Strategy;

public class StrategyRef extends StrategoTerm {
    private static final long serialVersionUID = 1L;

    public final Strategy s;

    public StrategyRef(Strategy s) {
        this.s = s;
    }

    @Override public int getSubtermCount() {
        return 0;
    }

    @Override public IStrategoTerm getSubterm(int index) {
        throw new IndexOutOfBoundsException("Index out of bounds: " + index);
    }

    @Override public IStrategoTerm[] getAllSubterms() {
        return TermFactory.EMPTY_TERM_ARRAY;
    }

    @Override public List<IStrategoTerm> getSubterms() {
        return Collections.emptyList();
    }

    @Deprecated
    @Override public int getTermType() {
        return getType().getValue();
    }

    @Override public TermType getType() {
        return TermType.BLOB;
    }

    @Override public void prettyPrint(ITermPrinter pp) {
        pp.print(toString());
    }

    @Override protected boolean doSlowMatch(IStrategoTerm second) {
        if(!(second instanceof StrategyRef)) {
            return false;
        }
        return this.s == ((StrategyRef) second).s;
    }

    @Override protected int hashFunction() {
        return System.identityHashCode(s);
    }

    @Override public String toString(int maxDepth) {
        return "___StrategyReference___";
    }

    @Override public void writeAsString(Appendable output, int maxDepth) throws IOException {
        output.append(toString());
    }

    @Override public Iterator<IStrategoTerm> iterator() {
        return Collections.emptyIterator();
    }
}
