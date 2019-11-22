package mb.stratego.build.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.spoofax.interpreter.terms.IStrategoString;

public class StringSetWithPositions {
    private final HashMap<String, List<IStrategoString>> map;

    public StringSetWithPositions() {
        this.map = new HashMap<>();
    }

    public StringSetWithPositions(StringSetWithPositions toCopy) {
        this.map = new HashMap<>(toCopy.map);
    }

    public Set<String> cloneSet() {
        return new HashSet<>(map.keySet());
    }

    public Set<String> readSet() {
        return Collections.unmodifiableSet(map.keySet());
    }

    public void add(IStrategoString str) {
        Relation.getOrInitialize(map, str.stringValue(), ArrayList::new).add(str);
    }

    public void addAll(Iterable<IStrategoString> iterable) {
        for(IStrategoString s : iterable) {
            add(s);
        }
    }

    private void addAll(String str, List<IStrategoString> strs) {
        Relation.getOrInitialize(map, str, ArrayList::new).addAll(strs);
    }

    public List<IStrategoString> getPositions(String str) {
        return map.get(str);
    }

    public void addAll(StringSetWithPositions other) {
        for(Map.Entry<String, List<IStrategoString>> entry : other.map.entrySet()) {
            addAll(entry.getKey(), entry.getValue());
        }
    }

    public boolean contains(String str) {
        return this.map.containsKey(str);
    }

    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((map == null) ? 0 : map.hashCode());
        return result;
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        StringSetWithPositions other = (StringSetWithPositions) obj;
        if(map == null) {
            if(other.map != null)
                return false;
        } else if(!map.equals(other.map))
            return false;
        return true;
    }

    @Override public String toString() {
        return map.toString();
    }
}
