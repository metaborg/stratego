package mb.stratego.build.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nullable;

import org.metaborg.util.iterators.CompoundIterator;
import org.spoofax.interpreter.terms.IStrategoString;

import mb.stratego.build.strincr.SplitResult;

/**
 * This is a collection of cified strategy names. It can be searched for a strategy name by prefix, by
 * {@link SplitResult.StrategySignature}, or by cified strategy as a string. The prefix is the fastest. Each entry
 * contains the {@link SplitResult.StrategySignature} and a list of IStrategoString occurrences of that name.
 *
 * Adding another StrategyEnvironment to this one results in a slower lookup of prefixes make the cost of the add operation cheaper (in time and more importantly in memory).
 */
public class StrategyEnvironment implements Serializable {
    private final Set<Map<String, List<Entry>>> maps;
    private transient @Nullable Map<String, List<Entry>> latestMutable = null;

    public StrategyEnvironment(StrategyEnvironment env) {
        this.maps = Collections.newSetFromMap(new IdentityHashMap<>());
        addAll(env);
        env.latestMutable = null;
    }

    public StrategyEnvironment() {
        this.maps = Collections.newSetFromMap(new IdentityHashMap<>());
    }

    public void addAll(StrategyEnvironment other) {
        this.maps.addAll(other.maps);
    }

    public @Nullable Entry get(SplitResult.StrategySignature sig) {
        @Nullable Entry result = null;
        for(Map<String, List<Entry>> map : maps) {
            final @Nullable List<Entry> entries = map.get(sig.name);
            if(entries != null) {
                for(Entry entry : entries) {
                    if(entry.strategySig.equals(sig)) {
                        if(result == null) {
                            result = new Entry(entry);
                            break; // only the inner loop
                        } else {
                            result.merge(entry);
                        }
                    }
                }
            }
        }
        return result;
    }

    public List<Entry> getByPrefix(String namePart) {
        final List<Entry> result = new ArrayList<>();
        for(Map<String, List<Entry>> map : maps) {
            final @Nullable List<Entry> entries = map.get(namePart);
            if(entries != null) {
                result.addAll(entries);
            }
        }
        return result;
    }

    public @Nullable Entry get(String cifiedName) {
        return get(Objects.requireNonNull(SplitResult.StrategySignature.fromCified(cifiedName)));
    }

    public List<IStrategoString> getPositions(String cifiedName) {
        final @Nullable Entry entry = get(Objects.requireNonNull(SplitResult.StrategySignature.fromCified(cifiedName)));
        return entry != null ? entry.occurrences : Collections.emptyList();
    }

    public boolean isEmpty() {
        return this.maps.isEmpty() || size() == 0;
    }

    private int size() {
        int size = 0;
        for(Map<String, List<Entry>> map : maps) {
            size += map.size();
        }
        return size;
    }

    public boolean contains(String usedAmbStrategy) {
        final @Nullable SplitResult.StrategySignature sig =
            SplitResult.StrategySignature.fromCified(usedAmbStrategy);
        if(sig != null) {
            for(Map<String, List<Entry>> map : maps) {
                final @Nullable List<Entry> entries = map.get(sig.name);
                if(entries != null) {
                    for(Entry entry : entries) {
                        if(entry.strategySig.equals(sig)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public void add(IStrategoString str) {
        final SplitResult.StrategySignature sig =
            Objects.requireNonNull(SplitResult.StrategySignature.fromCified(str.stringValue()));
        final Map<String, List<Entry>> map = getMap();
        final List<Entry> entries = map.getOrDefault(sig.name, new ArrayList<>(1));
        for(Entry entry : entries) {
            if(entry.strategySig.equals(sig)) {
                entry.occurrences.add(str);
                return;
            }
        }
        final List<IStrategoString> strs = new ArrayList<>(1);
        strs.add(str);
        entries.add(new Entry(sig, strs));
        map.put(sig.name, entries);
    }

    private Map<String, List<Entry>> getMap() {
        if(latestMutable == null) {
            final Map<String, List<Entry>> map;
            map = new HashMap<>();
            maps.add(map);
            latestMutable = map;
        }
        return latestMutable;
    }

    public void addAll(List<IStrategoString> strategyNeedsExternal) {
        for(IStrategoString string : strategyNeedsExternal) {
            add(string);
        }
    }

    public Set<String> readSet() {
        final Iterator<String> namesIterator = strategyNameIterator();
        final int finalSize = this.size();

        return new Set<String>() {
            @Override
            public int size() {
                return finalSize;
            }

            @Override
            public boolean isEmpty() {
                return finalSize == 0;
            }

            @Override
            public boolean contains(@Nullable Object o) {
                if(o != null && o instanceof String) {
                    return StrategyEnvironment.this.contains((String) o);
                }
                return false;
            }

            @Override
            public Iterator<String> iterator() {
                return namesIterator;
            }

            @Override
            public Object[] toArray() {
                throw new UnsupportedOperationException();
            }

            @Override
            public <T> T[] toArray(T[] a) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean add(String s) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean remove(Object o) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean containsAll(Collection<?> c) {
                for(Object o : c) {
                    if(!contains(o)) {
                        return false;
                    }
                }
                return true;
            }

            @Override
            public boolean addAll(Collection<? extends String> c) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean retainAll(Collection<?> c) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean removeAll(Collection<?> c) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void clear() {
                throw new UnsupportedOperationException();
            }
        };
    }

    public Iterator<String> strategyNameIterator() {
        return new Iterator<String>() {
            final Iterator<Map<String, List<Entry>>> mapIterator = StrategyEnvironment.this.maps.iterator();
            Iterator<Entry> entryIterator = Collections.emptyIterator();

            @Override
            public boolean hasNext() {
                initEntryIterator();
                return entryIterator.hasNext();
            }

            @Override
            public String next() {
                initEntryIterator();
                return entryIterator.next().strategySig.cifiedName();
            }

            private void initEntryIterator() {
                if(!entryIterator.hasNext() && mapIterator.hasNext()) {
                    List<Iterator<Entry>> list = new ArrayList<>();
                    for(List<Entry> entries : mapIterator.next().values()) {
                        Iterator<Entry> iterator = entries.iterator();
                        list.add(iterator);
                    }
                    entryIterator = new CompoundIterator<>(list);
                }
            }
        };
    }

    public static final class Entry implements Serializable {
        public final SplitResult.StrategySignature strategySig;
        public final List<IStrategoString> occurrences;

        public Entry(SplitResult.StrategySignature strategyName, List<IStrategoString> occurrences) {
            this.strategySig = strategyName;
            this.occurrences = occurrences;
        }

        public Entry(Entry entry) {
            this.strategySig = entry.strategySig;
            this.occurrences = new ArrayList<>(entry.occurrences);
        }

        public void merge(Entry entry) {
            occurrences.addAll(entry.occurrences);
        }

        @Override
        public boolean equals(Object o) {
            if(this == o)
                return true;
            if(getClass() != o.getClass())
                return false;
            Entry entry = (Entry) o;
            return strategySig.equals(entry.strategySig) && occurrences.equals(entry.occurrences);
        }

        @Override
        public int hashCode() {
            return Objects.hash(strategySig, occurrences);
        }
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final StrategyEnvironment that = (StrategyEnvironment) o;
        if(!maps.equals(that.maps)) return false;
        return Objects.equals(latestMutable, that.latestMutable);
    }

    @Override public int hashCode() {
        int result = maps.hashCode();
        result = 31 * result + (latestMutable != null ? latestMutable.hashCode() : 0);
        return result;
    }
}
