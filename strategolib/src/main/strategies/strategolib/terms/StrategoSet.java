package strategolib.terms;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermPrinter;
import org.spoofax.interpreter.terms.TermType;
import org.spoofax.terms.AbstractSimpleTerm;
import org.spoofax.terms.TermFactory;

public class StrategoSet extends AbstractSimpleTerm implements IStrategoTerm, Serializable {

    private static final long serialVersionUID = -4514696890481283123L;
    private int counter;
    private final Map<IStrategoTerm, Integer> map;

    public StrategoSet(int initialSize, int maxLoad) {
        map = new HashMap<IStrategoTerm, Integer>(initialSize, 1.0f * maxLoad / 100);
        counter = 0;
    }

    public int put(IStrategoTerm value) {
        int idx = counter++;
        map.put(value, idx);
        return idx;
    }

    public int getIndex(IStrategoTerm t) {
        Integer r = map.get(t);
        return r == null ? -1 : r;
    }

    public IStrategoTerm getElem(int index) {
        for(Map.Entry<IStrategoTerm, Integer> e : map.entrySet()) {
            if(e.getValue().equals(index)) {
                return e.getKey();
            }
        }
        return null;
    }

    public boolean containsValue(IStrategoTerm t) {
        return map.containsKey(t);
    }

    public Collection<Integer> values() {
        return map.values();
    }

    public boolean remove(IStrategoTerm t) {
        return map.remove(t) != null;
    }

    public void clear() {
        counter = 0;
        map.clear();
    }

    public Collection<IStrategoTerm> keySet() {
        // FIXME this could easily be more efficient
        class X implements Comparable<X> {
            IStrategoTerm t;
            Integer i;

            X(IStrategoTerm t, Integer i) {
                this.t = t;
                this.i = i;
            }

            @Override public int compareTo(X o) {
                return i.compareTo(o.i);
            }
        }
        List<X> xs = new ArrayList<X>();
        for(Entry<IStrategoTerm, Integer> e : map.entrySet()) {
            xs.add(new X(e.getKey(), e.getValue()));
        }
        Collections.sort(xs);
        List<IStrategoTerm> r = new ArrayList<IStrategoTerm>();
        for(X x : xs)
            r.add(x.t);
        return r;
    }

    public IStrategoTerm any() {
        return map.keySet().iterator().next();
    }

    public boolean containsKey(IStrategoTerm t) {
        return map.containsKey(t);
    }

    public int size() {
        return map.size();
    }

    public Set<Entry<IStrategoTerm, Integer>> entrySet() {
        return map.entrySet();
    }

    @Override public List<IStrategoTerm> getSubterms() {
        return Collections.emptyList();
    }

    @Override public IStrategoList getAnnotations() {
        return TermFactory.EMPTY_LIST;
    }

    @Override public IStrategoTerm getSubterm(int index) {
        throw new UnsupportedOperationException();
    }

    @Override public IStrategoTerm[] getAllSubterms() {
        return TermFactory.EMPTY_TERM_ARRAY;
    }

    @Override public int getSubtermCount() {
        return 0;
    }

    @Deprecated @Override public int getTermType() {
        return getType().getValue();
    }

    @Override public TermType getType() {
        return TermType.BLOB;
    }

    @Override public boolean match(IStrategoTerm second) {
        return second == this;
    }

    @Override public int hashCode() {
        return System.identityHashCode(this);
    }

    @Override public void prettyPrint(ITermPrinter pp) {
        pp.print(toString());
    }

    @Override public String toString() {
        return String.valueOf(hashCode());
    }

    @Override public String toString(int maxDepth) {
        return toString();
    }

    @Override public void writeAsString(Appendable output, int maxDepth) throws IOException {
        output.append(toString());
    }

    @Override public boolean isList() {
        return false;
    }

    @Override public Iterator<IStrategoTerm> iterator() {
        return map.keySet().iterator();
    }

}
