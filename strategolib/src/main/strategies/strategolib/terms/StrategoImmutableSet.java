package strategolib.terms;

import io.usethesource.capsule.Set;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.interpreter.terms.ITermPrinter;
import org.spoofax.interpreter.terms.TermType;
import org.spoofax.terms.StrategoTerm;
import org.spoofax.terms.TermFactory;
import java.io.IOException;
import java.util.*;

public class StrategoImmutableSet extends StrategoTerm implements IStrategoTerm {
    public final Set.Immutable<IStrategoTerm> backingSet;

    public StrategoImmutableSet(IStrategoTerm... terms) {
        this(Set.Immutable.<IStrategoTerm>of().__insertAll(new HashSet<>(Arrays.asList(terms))));
    }

    public StrategoImmutableSet(Set.Immutable<? extends IStrategoTerm> backingSet) {
        super(TermFactory.EMPTY_LIST);
        this.backingSet = (Set.Immutable<IStrategoTerm>) backingSet;
    }

    public StrategoImmutableSet() {
        this(Set.Immutable.of());
    }

    @Override public int getSubtermCount() {
        return 0;
    }

    @Override public IStrategoTerm getSubterm(int index) {
        throw new IndexOutOfBoundsException();
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
        if(!(second instanceof StrategoImmutableSet)) {
            return false;
        }

        Set.Immutable<IStrategoTerm> secondMap = ((StrategoImmutableSet) second).backingSet;
        return backingSet.equals(secondMap);
    }

    @Override protected int hashFunction() {
        return backingSet.hashCode();
    }

    @Override public String toString(int maxDepth) {
        return backingSet.toString();
    }

    @Override public void writeAsString(Appendable output, int maxDepth) throws IOException {
        output.append(toString());
    }

    public IStrategoTerm withWrapper(ITermFactory factory) {
        return factory.makeAppl("ImmutableSet", this);
    }

    @SuppressWarnings("NullableProblems") @Override public Iterator<IStrategoTerm> iterator() {
        return backingSet.iterator();
    }
}
