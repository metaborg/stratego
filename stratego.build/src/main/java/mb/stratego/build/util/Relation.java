package mb.stratego.build.util;

import java.util.Map;
import java.util.function.Supplier;

public class Relation {
    public static <K, V> V getOrInitialize(Map<K, V> map, K key, Supplier<V> initialize) {
        map.computeIfAbsent(key, ignore -> initialize.get());
        return map.get(key);
    }
}
