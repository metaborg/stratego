package strategolib.terms;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.interpreter.terms.ITermPrinter;
import org.spoofax.interpreter.terms.TermType;
import org.spoofax.interpreter.util.EntryAsPairIterator;
import org.spoofax.terms.StrategoTerm;
import org.spoofax.terms.TermFactory;

import io.usethesource.capsule.Map;

public class StrategoImmutableMap extends StrategoTerm implements IStrategoTerm {
    public final Map.Immutable<IStrategoTerm, IStrategoTerm> backingMap;

    public StrategoImmutableMap(Map.Immutable<? extends IStrategoTerm, ? extends IStrategoTerm> backingMap) {
        super(TermFactory.EMPTY_LIST);
        //noinspection unchecked
        this.backingMap = (Map.Immutable<IStrategoTerm, IStrategoTerm>) backingMap;
    }

    public StrategoImmutableMap() {
        this(Map.Immutable.of());
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
        if(!(second instanceof StrategoImmutableMap)) {
            return false;
        }

        Map.Immutable<IStrategoTerm, IStrategoTerm> secondMap = ((StrategoImmutableMap) second).backingMap;
        return backingMap.equals(secondMap);
    }

    @Override protected int hashFunction() {
        return backingMap.hashCode();
    }

    @Override public String toString(int maxDepth) {
        return backingMap.toString();
    }

    @Override public void writeAsString(Appendable output, int maxDepth) throws IOException {
        output.append(toString());
    }

    public IStrategoTerm withWrapper(ITermFactory factory) {
        return factory.makeAppl("ImmutableMap", this);
    }

    public static StrategoImmutableMap fromMap(Map.Immutable<? extends IStrategoTerm, ? extends IStrategoTerm> map) {
        return new StrategoImmutableMap(map);
    }

    public static StrategoImmutableMap fromMap(java.util.Map<? extends IStrategoTerm, ? extends IStrategoTerm> map) {
        final Map.Transient<IStrategoTerm, IStrategoTerm> mapT = Map.Transient.of();
        mapT.__putAll(map);
        return new StrategoImmutableMap(mapT.freeze());
    }

    @SuppressWarnings("NullableProblems")
    @Override public Iterator<IStrategoTerm> iterator() {
        return new EntryAsPairIterator(backingMap.entryIterator());
    }
}
