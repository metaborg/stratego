package mb.stratego.build.strincr;

import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Sets;

import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.stratego.build.util.Algorithms;

public class StaticChecks {
    public static final class Output {
        // Cified-strategy-name (where the call occurs) to cified-strategy-name (amb call) to cified-strategy-name (amb call resolves to)
        final Map<String, SortedMap<String, String>> ambStratResolution;
        final boolean staticNameCheck;

        Output(Map<String, SortedMap<String, String>> ambStratResolution, boolean staticNameCheck) {
            this.ambStratResolution = ambStratResolution;
            this.staticNameCheck = staticNameCheck;
        }

        @Override public String toString() {
            if(!ambStratResolution.isEmpty()) {
                final StringBuilder b = new StringBuilder();
                for(Map.Entry<String, SortedMap<String, String>> stringSortedMapEntry : ambStratResolution.entrySet()) {
                    b.append("  In strategy ").append(stringSortedMapEntry.getKey()).append(":\n");
                    for(Map.Entry<String, String> stringStringEntry : stringSortedMapEntry.getValue().entrySet()) {
                        b.append("    ").append(stringStringEntry.getKey()).append(" -> ")
                            .append(stringStringEntry.getValue()).append("\n");
                    }
                }
                return b.toString();
            } else {
                return "  (none)";
            }
        }
    }

    public static final class Data {
        // Module-path to module-path
        public final Map<String, Set<String>> imports = new HashMap<>();
        // Module-path to cified-strategy-names used
        public final Map<String, Set<String>> usedStrategies = new HashMap<>();
        // Module-path to cified-strategy-name used in ambiguous call position to cified-strategy-names where the calls occur
        public final Map<String, Map<String, Set<String>>> usedAmbStrategies = new HashMap<>();
        // Module-path to constructor_arity names used
        public final Map<String, Set<String>> usedConstructors = new HashMap<>();
        // Module-path to cified-strategy-names defined here
        public final Map<String, Set<String>> definedStrategies = new HashMap<>();
        // Module-path to cified-strategy-names defined here as congruences
        public final Map<String, Set<String>> definedCongruences = new HashMap<>();
        // External cified-strategy-names that will be imported in Java
        public final Set<String> externalStrategies = new HashSet<>();
        // Module-path to constructor_arity names defined there
        public final Map<String, Set<String>> definedConstructors = new HashMap<>();
        // Cified-strategy-names that need a corresponding name in a library because it overrides or extends it.
        public final Set<String> strategyNeedsExternal = new HashSet<>();
    }

    static final HashSet<String> ALWAYS_DEFINED =
        new HashSet<>(Arrays.asList("DR__DUMMY_0_0", "Anno__Cong_____2_0", "DR__UNDEFINE_1_0"));
    public static final Pattern stripArityPattern = Pattern.compile("([A-Za-z$_][A-Za-z0-9_$]*)_(\\d+)_(\\d+)");

    public static Output check(ExecContext execContext, String mainFileModulePath,
        Data staticData, Map<String, Set<String>> overlayConstrs) throws ExecException {
        boolean staticNameCheck = true;
        // Cified-strategy-name (where the call occurs) to cified-strategy-name (amb call) to cified-strategy-name (amb call resolves to)
        final Map<String, SortedMap<String, String>> ambStratResolution = new HashMap<>();
        // Module-path to  visible when imported (transitive closure of strategy definitions)
        final Map<String, Set<String>> visibleStrategies = new HashMap<>(staticData.definedStrategies);
        for(Map.Entry<String, Set<String>> entry : visibleStrategies.entrySet()) {
            entry.setValue(new HashSet<>(entry.getValue()));
        }
        for(Map.Entry<String, Set<String>> entry : staticData.definedCongruences.entrySet()) {
            getOrInitialize(visibleStrategies, entry.getKey(), HashSet::new).addAll(entry.getValue());
        }
        // Module-path to constructor_arity names visible when imported (transitive closure of constructor definitions)
        final Map<String, Set<String>> visibleConstructors = new HashMap<>(staticData.definedConstructors);
        for(Map.Entry<String, Set<String>> entry : visibleConstructors.entrySet()) {
            entry.setValue(new HashSet<>(entry.getValue()));
        }

        // CHECK that extending and/or overriding strategies have an external strategy to extend and/or override
        Set<String> strategyNeedsExternalNonOverlap = Sets.difference(staticData.strategyNeedsExternal, staticData.externalStrategies);
        if(!strategyNeedsExternalNonOverlap.isEmpty()) {
            staticNameCheck = false;
            execContext.logger()
                .error("Cannot find external strategies for override/extend " + strategyNeedsExternalNonOverlap, null);
        }

        // CHECK that overlays do not cyclically use each other
        final Deque<Set<String>> overlaySccs =
            Algorithms.topoSCCs(overlayConstrs.keySet(), k -> overlayConstrs.getOrDefault(k, Collections.emptySet()));
        overlaySccs.removeIf(s -> {
            String overlayName = s.iterator().next();
            return s.size() == 1 && !(overlayConstrs.getOrDefault(overlayName, Collections.emptySet())
                .contains(overlayName));
        });
        if(!overlaySccs.isEmpty()) {
            staticNameCheck = false;
            for(Set<String> overlayScc : overlaySccs) {
                execContext.logger().error("Overlays have a cyclic dependency " + overlayScc, null);
            }
        }

        // CHECK that names can be resolved
        final Deque<Set<String>> sccs = Algorithms
            .topoSCCs(Collections.singleton(mainFileModulePath), k -> staticData.imports.getOrDefault(k, Collections.emptySet()));
        for(Iterator<Set<String>> iterator = sccs.descendingIterator(); iterator.hasNext(); ) {
            Set<String> scc = iterator.next();
            Set<String> theVisibleStrategies = new HashSet<>();
            Set<String> theVisibleConstructors = new HashSet<>();
            for(String moduleName : scc) {
                theVisibleConstructors.addAll(visibleConstructors.getOrDefault(moduleName, Collections.emptySet()));
                theVisibleStrategies.addAll(visibleStrategies.getOrDefault(moduleName, Collections.emptySet()));
                for(String mod : staticData.imports.getOrDefault(moduleName, Collections.emptySet())) {
                    theVisibleConstructors.addAll(visibleConstructors.getOrDefault(mod, Collections.emptySet()));
                    theVisibleStrategies.addAll(visibleStrategies.getOrDefault(mod, Collections.emptySet()));
                }
            }
            for(String moduleName : scc) {
                if(Library.Builtin.isBuiltinLibrary(moduleName)) {
                    continue;
                }
                visibleConstructors.put(moduleName, theVisibleConstructors);
                visibleStrategies.put(moduleName, theVisibleStrategies);
                Set<String> unresolvedConstructors =
                    Sets.difference(staticData.usedConstructors.getOrDefault(moduleName, Collections.emptySet()),
                        theVisibleConstructors);
                if(!unresolvedConstructors.isEmpty()) {
                    staticNameCheck = false;
                    execContext.logger()
                        .error("In module " + moduleName + ": Cannot find constructors " + unresolvedConstructors,
                            null);
                }
                Set<String> unresolvedStrategies =
                    Sets.difference(staticData.usedStrategies.getOrDefault(moduleName, Collections.emptySet()),
                        theVisibleStrategies);
                if(!unresolvedStrategies.isEmpty()) {
                    staticNameCheck = false;
                    execContext.logger()
                        .error("In module " + moduleName + ": Cannot find strategies " + unresolvedStrategies, null);
                }
                Set<String> strategiesOverlapWithExternal = Sets.difference(
                    Sets.intersection(staticData.definedStrategies.getOrDefault(moduleName, Collections.emptySet()),
                        staticData.externalStrategies), ALWAYS_DEFINED);
                if(!strategiesOverlapWithExternal.isEmpty()) {
                    staticNameCheck = false;
                    execContext.logger().error("In module " + moduleName + ": Illegal overlap with external strategies "
                        + strategiesOverlapWithExternal, null);
                }
                Map<String, Set<String>> theUsedAmbStrategies =
                    new HashMap<>(staticData.usedAmbStrategies.getOrDefault(moduleName, Collections.emptyMap()));
                // By default a _0_0 strategy is used in the ambiguous call situation if one is defined.
                theUsedAmbStrategies.keySet().removeIf(theVisibleStrategies::contains);
                if(!theUsedAmbStrategies.isEmpty()) {
                    Map<String, Set<String>> differentArityDefinitions = new HashMap<>(theVisibleStrategies.size());
                    for(String theVisibleStrategy : theVisibleStrategies) {
                        String ambCallVersion = stripArity(theVisibleStrategy) + "_0_0";
                        getOrInitialize(differentArityDefinitions, ambCallVersion, HashSet::new)
                            .add(theVisibleStrategy);
                    }
                    for(Map.Entry<String, Set<String>> entry : theUsedAmbStrategies.entrySet()) {
                        final String usedAmbStrategy = entry.getKey();
                        final Set<String> defs =
                            differentArityDefinitions.getOrDefault(usedAmbStrategy, Collections.emptySet());
                        switch(defs.size()) {
                            case 0:
                                execContext.logger().error(
                                    "In module " + moduleName + ": Cannot find strategy " + usedAmbStrategy
                                        + " in ambiguous call position", null);
                                break;
                            case 1:
                                final String resolvedDef = defs.iterator().next();
                                for(String useSite : entry.getValue()) {
                                    getOrInitialize(ambStratResolution, useSite, TreeMap::new)
                                        .put(usedAmbStrategy, resolvedDef);
                                }
                                break;
                            default:
                                execContext.logger().error(
                                    "In module " + moduleName + ": Call to strategy " + usedAmbStrategy
                                        + " is ambiguous, multiple arities possible. ", null);
                        }
                    }
                }
            }
        }
        return new Output(ambStratResolution, staticNameCheck);
    }

    private static String stripArity(String s) throws ExecException {
        if(s.substring(s.length() - 4, s.length()).matches("_\\d_\\d")) {
            return s.substring(0, s.length() - 4);
        }
        if(s.substring(s.length() - 5, s.length()).matches("_\\d+_\\d+")) {
            return s.substring(0, s.length() - 5);
        }
        Matcher m = stripArityPattern.matcher(s);
        if(!m.matches()) {
            throw new ExecException(
                "Frontend returned stratego strategy name that does not conform to cified name: '" + s + "'");
        }
        return m.group(1);
    }

    static <K, V> V getOrInitialize(Map<K, V> map, K key, Supplier<V> initialize) {
        map.computeIfAbsent(key, ignore -> initialize.get());
        return map.get(key);
    }

}
