package mb.stratego.build.util;

import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

import io.usethesource.capsule.BinaryRelation;

public class Relation {
    public static <K, V> V getOrInitialize(Map<K, V> map, K key, Supplier<V> initialize) {
        map.computeIfAbsent(key, ignore -> initialize.get());
        return map.get(key);
    }

    public static <K, V, C extends Collection<V>> void putAll(java.util.Map<K, C> toAddTo,
            java.util.Map<K, ? extends Collection<V>> toAdd, Supplier<C> initialize) {
        for(Map.Entry<K, ? extends Collection<V>> e : toAdd.entrySet()) {
            Relation.getOrInitialize(toAddTo, e.getKey(), initialize).addAll(e.getValue());
        }
    }

    public static <K, V> void putAll(BinaryRelation.Transient<K, V> toAddTo, BinaryRelation<K, V> toAdd) {
        for(Map.Entry<K, V> kvEntry : toAdd.entrySet()) {
            toAddTo.__insert(kvEntry.getKey(), kvEntry.getValue());
        }
    }
}
