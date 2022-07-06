package strategolib.terms;

import static org.spoofax.terms.AbstractTermFactory.EMPTY_TERM_ARRAY;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.interpreter.terms.ITermPrinter;
import org.spoofax.interpreter.terms.TermType;
import org.spoofax.interpreter.util.EntryAsPairIterator;
import org.spoofax.terms.StrategoTerm;
import org.spoofax.terms.TermFactory;

import io.usethesource.capsule.BinaryRelation;

public class StrategoImmutableRelation extends StrategoTerm implements IStrategoTerm {
    public final BinaryRelation.Immutable<IStrategoTerm, IStrategoTerm> backingRelation;

    public StrategoImmutableRelation(
        BinaryRelation.Immutable<? extends IStrategoTerm, ? extends IStrategoTerm> backingRelation) {
        super(TermFactory.EMPTY_LIST);
        //noinspection unchecked
        this.backingRelation = (BinaryRelation.Immutable<IStrategoTerm, IStrategoTerm>) backingRelation;
    }

    public StrategoImmutableRelation() {
        this(BinaryRelation.Immutable.of());
    }

    @SuppressWarnings("NullableProblems")
    @Override public Iterator<IStrategoTerm> iterator() {
        return new EntryAsPairIterator(backingRelation.entryIterator());
    }

    @Override public int getSubtermCount() {
        return 0;
    }

    @Override public IStrategoTerm getSubterm(int index) {
        throw new IndexOutOfBoundsException();
    }

    @Override public IStrategoTerm[] getAllSubterms() {
        return EMPTY_TERM_ARRAY;
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
        if(!(second instanceof StrategoImmutableRelation)) {
            return false;
        }

        BinaryRelation.Immutable<IStrategoTerm, IStrategoTerm> secondRelation =
            ((StrategoImmutableRelation) second).backingRelation;
        return backingRelation.equals(secondRelation);
    }

    @Override protected int hashFunction() {
        return backingRelation.hashCode();
    }

    @Override public String toString(int maxDepth) {
        return backingRelation.toString();
    }

    @Override public void writeAsString(Appendable output, int maxDepth) throws IOException {
        output.append(toString());
    }

    public IStrategoTerm withWrapper(ITermFactory factory) {
        return factory.makeAppl("ImmutableRelation", this);
    }

    public static StrategoImmutableRelation union(StrategoImmutableRelation one, StrategoImmutableRelation other) {
        final BinaryRelation.Transient<IStrategoTerm, IStrategoTerm> result = one.backingRelation.asTransient();
        for(Map.Entry<IStrategoTerm, IStrategoTerm> e : other.backingRelation.entrySet()) {
            result.__insert(e.getKey(), e.getValue());
        }
        return new StrategoImmutableRelation(result.freeze());
    }

    public static StrategoImmutableRelation intersect(StrategoImmutableRelation one, StrategoImmutableRelation other) {
        final BinaryRelation.Transient<IStrategoTerm, IStrategoTerm> result = BinaryRelation.Transient.of();
        for(Map.Entry<IStrategoTerm, IStrategoTerm> e : one.backingRelation.entrySet()) {
            if(other.backingRelation.containsEntry(e.getKey(), e.getValue())) {
                result.__insert(e.getKey(), e.getValue());
            }
        }
        return new StrategoImmutableRelation(result.freeze());
    }

    public static StrategoImmutableRelation subtract(StrategoImmutableRelation left, StrategoImmutableRelation right) {
        final BinaryRelation.Transient<IStrategoTerm, IStrategoTerm> result = left.backingRelation.asTransient();
        for(Map.Entry<IStrategoTerm, IStrategoTerm> e : right.backingRelation.entrySet()) {
            result.__remove(e.getKey(), e.getValue());
        }

        return new StrategoImmutableRelation(result.freeze());
    }

    public static StrategoImmutableRelation compose(StrategoImmutableRelation left, StrategoImmutableRelation right) {
        final BinaryRelation.Transient<IStrategoTerm, IStrategoTerm> result = BinaryRelation.Transient.of();
        for(Map.Entry<IStrategoTerm, IStrategoTerm> e : left.backingRelation.entrySet()) {
            for(IStrategoTerm value : right.backingRelation.get(e.getValue())) {
                result.__insert(e.getKey(), value);
            }
        }

        return new StrategoImmutableRelation(result.freeze());
    }

    public static StrategoImmutableRelation reflexiveClosure(StrategoImmutableRelation map) {
        final BinaryRelation.Transient<IStrategoTerm, IStrategoTerm> result = reflClos(map.backingRelation);
        return new StrategoImmutableRelation(result.freeze());
    }

    public static StrategoImmutableRelation transitiveClosure(StrategoImmutableRelation map) {
        final BinaryRelation.Transient<IStrategoTerm, IStrategoTerm> result = transClos(map.backingRelation);
        return new StrategoImmutableRelation(result.freeze());
    }

    public static <T> BinaryRelation.Transient<T, T> reflClos(BinaryRelation.Immutable<T, T> backingRelation) {
        final BinaryRelation.Transient<T, T> result = backingRelation.asTransient();
        reflClos(backingRelation, result);
        return result;
    }

    private static <T> void reflClos(BinaryRelation<T, T> backingRelation,
        final BinaryRelation.Transient<T, T> result) {
        for(Map.Entry<T, T> e : backingRelation.entrySet()) {
            final T key = e.getKey();
            final T value = e.getValue();
            result.__insert(key, key);
            result.__insert(value, value);
        }
    }

    public static <T> BinaryRelation.Transient<T, T> transClos(BinaryRelation.Immutable<T, T> rel) {
        final Deque<Map.Entry<T, T>> worklist =
            new LinkedList<>(rel.entrySet());
        final BinaryRelation.Transient<T, T> result = rel.asTransient();
        transClos(rel, worklist, result);
        return result;
    }

    private static <T> void transClos(BinaryRelation.Immutable<T, T> rel,
        final Deque<Map.Entry<T, T>> worklist, final BinaryRelation.Transient<T, T> result) {
        while(!worklist.isEmpty()) {
            final Map.Entry<T, T> e = worklist.pop();
            for(T post : rel.get(e.getValue())) {
                if(!result.containsEntry(e.getKey(), post)) {
                    result.__insert(e.getKey(), post);
                    worklist.add(new AbstractMap.SimpleImmutableEntry<>(e.getKey(), post));
                }
            }
        }
    }

    public static <T> BinaryRelation.Transient<T, T> reflTransClos(BinaryRelation.Immutable<T, T> rel) {
        final Deque<Map.Entry<T, T>> worklist =
            new LinkedList<>(rel.entrySet());
        final BinaryRelation.Transient<T, T> result = rel.asTransient();
        transClos(rel, worklist, result);
        reflClos(rel, result);
        return result;
    }
}
