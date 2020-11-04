package mb.stratego.build.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.metaborg.util.iterators.CompoundIterator;
import org.spoofax.interpreter.terms.IStrategoString;

public class StringSetWithPositions implements Serializable {
    private final List<Map<String, List<IStrategoString>>> maps;
    private transient boolean latestMutable;

    public StringSetWithPositions() {
        this.maps = new ArrayList<>();
        this.latestMutable = false;
    }

    public StringSetWithPositions(StringSetWithPositions toCopy) {
        this.maps = new ArrayList<>(toCopy.maps);
        this.latestMutable = toCopy.latestMutable = false;
    }

    public Set<String> cloneSet() {
        Set<String> result = new HashSet<>();
        for(Map<String, List<IStrategoString>> map : maps) {
            result.addAll(map.keySet());
        }
        return result;
    }

    public Set<String> readSet() {
        final List<Iterator<String>> keyIterators = new ArrayList<>(maps.size());
        int size = 0;
        for(Map<String, List<IStrategoString>> map : maps) {
            size += map.size();
            keyIterators.add(map.keySet().iterator());
        }
        final int finalSize = size;

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
            public boolean contains(Object o) {
                for(Map<String, List<IStrategoString>> map : maps) {
                    //noinspection SuspiciousMethodCalls
                    if(map.containsKey(o)) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public Iterator<String> iterator() {
                return new CompoundIterator<>(keyIterators);
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

    public void add(IStrategoString str) {
        final Map<String, List<IStrategoString>> map = getMap();
        map.put(str.stringValue(), Collections.singletonList(str));
    }

    private Map<String, List<IStrategoString>> getMap() {
        final Map<String, List<IStrategoString>> map;
        if(!latestMutable) {
            map = new TreeMap<>();
            maps.add(map);
            latestMutable = true;
        } else {
            map = maps.get(maps.size()-1);
        }
        return map;
    }

    public void addAll(Iterable<IStrategoString> iterable) {
        final Map<String, List<IStrategoString>> newMap = getMap();
        for(IStrategoString s : iterable) {
            newMap.put(s.stringValue(), Collections.singletonList(s));
        }
    }

    public List<IStrategoString> getPositions(String str) {
        ArrayList<IStrategoString> result = new ArrayList<>();
        for(Map<String, List<IStrategoString>> map : maps) {
            result.addAll(map.get(str));
        }
        return result;
    }

    public void addAll(StringSetWithPositions other) {
        if(!other.isEmpty()) {
            maps.addAll(other.maps);
        }
    }

    private boolean isEmpty() {
        return maps.isEmpty() || size() == 0;
    }

    public boolean contains(String str) {
        for(Map<String, List<IStrategoString>> map : this.maps) {
            if(map.containsKey(str)) {
                return true;
            }
        }
        return false;
    }

    @Override public int hashCode() {
        return maps.hashCode();
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        StringSetWithPositions other = (StringSetWithPositions) obj;
        return maps.equals(other.maps);
    }

    @Override public String toString() {
        return maps.toString();
    }

    public int size() {
        int size = 0;
        for(Map<String, List<IStrategoString>> map : maps) {
            size += map.size();
        }
        return size;
    }
}
