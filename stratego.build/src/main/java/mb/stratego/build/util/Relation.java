package mb.stratego.build.util;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import io.usethesource.capsule.BinaryRelation;

public class Relation {
    public static <K, V> V getOrInitialize(Map<K, V> map, K key, Supplier<V> initialize) {
        return map.computeIfAbsent(key, __ -> initialize.get());
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

    public static <K, V, C extends Collection<V>> void removeAll(java.util.Map<K, C> toRemoveFrom,
        java.util.Map<K, ? extends Collection<V>> toRemove) {
        for(Map.Entry<K, ? extends Collection<V>> e : toRemove.entrySet()) {
            removeAll(toRemoveFrom, e.getKey(), e.getValue());
        }
    }

    public static <K, C extends Collection<V>, V> void removeAll(java.util.Map<K, C> toRemoveFrom,
        K key, Collection<V> valuesToRemove) {
        toRemoveFrom.computeIfPresent(key, (__, vs2) -> {
            vs2.removeAll(valuesToRemove);
            if(vs2.isEmpty()) {
                return null;
            }
            return vs2;
        });
    }

    public static <K, V, C extends Collection<V>> C remove(java.util.Map<K, C> toRemoveFrom,
        K keyToRemove, V valueToRemove) {
        return toRemoveFrom.computeIfPresent(keyToRemove, (__, vs2) -> {
            vs2.remove(valueToRemove);
            if(vs2.isEmpty()) {
                return null;
            }
            return vs2;
        });
    }

    public static <K, V, C extends Collection<V>, R extends java.util.Map<K, C>> R copy(java.util.Map<K, C> toCopy,
        Function<? super java.util.Map<K, C>, R> outerCopy, Function<C, C> innerCopy) {
        final R theCopy = outerCopy.apply(toCopy);
        theCopy.replaceAll((__, c) -> innerCopy.apply(c));
        return theCopy;
    }
}
