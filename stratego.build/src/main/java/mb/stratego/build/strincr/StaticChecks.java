package mb.stratego.build.strincr;

import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.spoofax.interpreter.terms.IStrategoString;

import com.google.common.collect.Sets;

import mb.pie.api.ExecException;
import mb.pie.api.Logger;
import mb.stratego.build.util.Algorithms;
import mb.stratego.build.util.StringSetWithPositions;

public class StaticChecks {
    public static final class Output {
        // Cified-strategy-name (where the call occurs) to cified-strategy-name (amb call) to cified-strategy-name (amb
        // call resolves to)
        public final Map<String, SortedMap<String, String>> ambStratResolution;

        Output(Map<String, SortedMap<String, String>> ambStratResolution) {
            this.ambStratResolution = ambStratResolution;
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

        @Override public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((ambStratResolution == null) ? 0 : ambStratResolution.hashCode());
            return result;
        }

        @Override public boolean equals(Object obj) {
            if(this == obj)
                return true;
            if(obj == null)
                return false;
            if(getClass() != obj.getClass())
                return false;
            Output other = (Output) obj;
            if(ambStratResolution == null) {
                if(other.ambStratResolution != null)
                    return false;
            } else if(!ambStratResolution.equals(other.ambStratResolution))
                return false;
            return true;
        }
    }

    public static final class Data {
        // Module-path to module-path
        public final Map<String, Set<String>> imports = new HashMap<>();
        // Module-path to cified-strategy-names used (to AST names of actual use)
        public final Map<String, StringSetWithPositions> usedStrategies = new HashMap<>();
        // Module-path to cified-strategy-name used in ambiguous call position to cified-strategy-names where the calls
        // occur
        public final Map<String, Map<String, Set<String>>> usedAmbStrategies = new HashMap<>();
        // Module-path to cified-strategy-name used in ambiguous call position (to AST names of actual use)
        public final Map<String, StringSetWithPositions> ambStratPositions = new HashMap<>();
        // Module-path to constructor_arity names used (to AST names of actual use)
        public final Map<String, StringSetWithPositions> usedConstructors = new HashMap<>();
        // Module-path to cified-strategy-names defined here (to AST names of actual definitions)
        public final Map<String, StringSetWithPositions> definedStrategies = new HashMap<>();
        // Module-path to cified-strategy-names defined here as congruences
        public final Map<String, Set<String>> definedCongruences = new HashMap<>();
        // External cified-strategy-names that will be imported in Java
        public final StringSetWithPositions externalStrategies = new StringSetWithPositions();
        // External constructors that will be imported in Java
        public final StringSetWithPositions externalConstructors = new StringSetWithPositions();
        // Module-path to constructor_arity names defined there (to AST names of actual definitions)
        public final Map<String, StringSetWithPositions> definedConstructors = new HashMap<>();
        // Cified-strategy-names that need a corresponding name in a library because it overrides or extends it. (to
        // strategy definition AST names)
        public final StringSetWithPositions strategyNeedsExternal = new StringSetWithPositions();
        // Constructor_arity overlay name. (to overlay definition AST names)
        public final StringSetWithPositions overlayDefs = new StringSetWithPositions();

        @Override public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((definedCongruences == null) ? 0 : definedCongruences.hashCode());
            result = prime * result + ((definedConstructors == null) ? 0 : definedConstructors.hashCode());
            result = prime * result + ((definedStrategies == null) ? 0 : definedStrategies.hashCode());
            result = prime * result + ((externalConstructors == null) ? 0 : externalConstructors.hashCode());
            result = prime * result + ((externalStrategies == null) ? 0 : externalStrategies.hashCode());
            result = prime * result + ((imports == null) ? 0 : imports.hashCode());
            result = prime * result + ((strategyNeedsExternal == null) ? 0 : strategyNeedsExternal.hashCode());
            result = prime * result + ((usedAmbStrategies == null) ? 0 : usedAmbStrategies.hashCode());
            result = prime * result + ((usedConstructors == null) ? 0 : usedConstructors.hashCode());
            result = prime * result + ((usedStrategies == null) ? 0 : usedStrategies.hashCode());
            return result;
        }

        @Override public boolean equals(Object obj) {
            if(this == obj)
                return true;
            if(obj == null)
                return false;
            if(getClass() != obj.getClass())
                return false;
            Data other = (Data) obj;
            if(definedCongruences == null) {
                if(other.definedCongruences != null)
                    return false;
            } else if(!definedCongruences.equals(other.definedCongruences))
                return false;
            if(definedConstructors == null) {
                if(other.definedConstructors != null)
                    return false;
            } else if(!definedConstructors.equals(other.definedConstructors))
                return false;
            if(definedStrategies == null) {
                if(other.definedStrategies != null)
                    return false;
            } else if(!definedStrategies.equals(other.definedStrategies))
                return false;
            if(externalConstructors == null) {
                if(other.externalConstructors != null)
                    return false;
            } else if(!externalConstructors.equals(other.externalConstructors))
                return false;
            if(externalStrategies == null) {
                if(other.externalStrategies != null)
                    return false;
            } else if(!externalStrategies.equals(other.externalStrategies))
                return false;
            if(imports == null) {
                if(other.imports != null)
                    return false;
            } else if(!imports.equals(other.imports))
                return false;
            if(strategyNeedsExternal == null) {
                if(other.strategyNeedsExternal != null)
                    return false;
            } else if(!strategyNeedsExternal.equals(other.strategyNeedsExternal))
                return false;
            if(usedAmbStrategies == null) {
                if(other.usedAmbStrategies != null)
                    return false;
            } else if(!usedAmbStrategies.equals(other.usedAmbStrategies))
                return false;
            if(usedConstructors == null) {
                if(other.usedConstructors != null)
                    return false;
            } else if(!usedConstructors.equals(other.usedConstructors))
                return false;
            if(usedStrategies == null) {
                if(other.usedStrategies != null)
                    return false;
            } else if(!usedStrategies.equals(other.usedStrategies))
                return false;
            return true;
        }

        public void registerStrategyDefinitions(Module module, StringSetWithPositions strategies) {
            definedStrategies.put(module.path, strategies);
        }

        public void registerCongruenceDefinitions(Module module, Set<String> strategies) {
            definedCongruences.put(module.path, strategies);
        }

        public void registerConstructorDefinitions(Module module, StringSetWithPositions constrs, StringSetWithPositions overlays) {
            StringSetWithPositions visConstrs = new StringSetWithPositions(constrs);
            visConstrs.addAll(overlays);
            definedConstructors.put(module.path, visConstrs);
        }
    }

    static final HashSet<String> ALWAYS_DEFINED =
        new HashSet<>(Arrays.asList("DR__DUMMY_0_0", "Anno__Cong_____2_0", "DR__UNDEFINE_1_0"));
    public static final Pattern stripArityPattern = Pattern.compile("([A-Za-z$_][A-Za-z0-9_$]*)_(\\d+)_(\\d+)");

    public static Output check(Logger logger, String mainFileModulePath, Data staticData,
        Map<String, Set<String>> overlayConstrs, List<Message> outputMessages) throws ExecException {
        // Cified-strategy-name (where the call occurs) to cified-strategy-name (amb call) to cified-strategy-name (amb
        // call resolves to)
        final Map<String, SortedMap<String, String>> ambStratResolution = new HashMap<>();
        // Module-path to visible when imported (transitive closure of strategy definitions)
        final Map<String, Set<String>> visibleStrategies = new HashMap<>(2 * (staticData.definedStrategies.size() + staticData.definedCongruences.size()));
        for(Map.Entry<String, StringSetWithPositions> entry : staticData.definedStrategies.entrySet()) {
            visibleStrategies.put(entry.getKey(), entry.getValue().cloneSet());
        }
        for(Map.Entry<String, Set<String>> entry : staticData.definedCongruences.entrySet()) {
            getOrInitialize(visibleStrategies, entry.getKey(), HashSet::new).addAll(entry.getValue());
        }
        // Module-path to constructor_arity names visible when imported (transitive closure of constructor definitions)
        final Map<String, Set<String>> visibleConstructors = new HashMap<>(2 * staticData.definedConstructors.size());
        for(Map.Entry<String, StringSetWithPositions> entry : staticData.definedConstructors.entrySet()) {
            visibleConstructors.put(entry.getKey(), entry.getValue().cloneSet());
        }

        // CHECK that extending and/or overriding strategies have an external strategy to extend and/or override
        Set<String> strategyNeedsExternalNonOverlap =
            Sets.difference(staticData.strategyNeedsExternal.readSet(), staticData.externalStrategies.readSet());
        for(String name : strategyNeedsExternalNonOverlap) {
            for(IStrategoString definitionName : staticData.strategyNeedsExternal.getPositions(name)) {
                outputMessages.add(Message.externalStrategyNotFound(mainFileModulePath, definitionName));
            }
        }

        // CHECK that overlays do not cyclically use each other
        final Deque<Set<String>> overlaySccs =
            Algorithms.topoSCCs(overlayConstrs.keySet(), k -> overlayConstrs.getOrDefault(k, Collections.emptySet()));
        overlaySccs.removeIf(s -> {
            String overlayName = s.iterator().next();
            return s.size() == 1
                && !(overlayConstrs.getOrDefault(overlayName, Collections.emptySet()).contains(overlayName));
        });
        for(Set<String> overlayScc : overlaySccs) {
            for(String name : overlayScc) {
                for(IStrategoString overlayName : staticData.overlayDefs.getPositions(name)) {
                    outputMessages.add(Message.cyclicOverlay(mainFileModulePath, overlayName, overlayScc));
                }
            }
        }

        // CHECK that names can be resolved
        final Deque<Set<String>> sccs = Algorithms.topoSCCs(Collections.singleton(mainFileModulePath),
            k -> staticData.imports.getOrDefault(k, Collections.emptySet()));
        for(Iterator<Set<String>> iterator = sccs.descendingIterator(); iterator.hasNext();) {
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
                final StringSetWithPositions usedConstructors =
                    staticData.usedConstructors.getOrDefault(moduleName, new StringSetWithPositions());
                Set<String> unresolvedConstructors = Sets.difference(usedConstructors.readSet(), theVisibleConstructors);
                for(String name : unresolvedConstructors) {
                    for(IStrategoString constructorUse : usedConstructors.getPositions(name)) {
                        outputMessages.add(Message.constructorNotFound(moduleName, constructorUse));
                    }
                }
                final StringSetWithPositions usedStrategies =
                    staticData.usedStrategies.getOrDefault(moduleName, new StringSetWithPositions());
                Set<String> unresolvedStrategies = Sets.difference(usedStrategies.readSet(), theVisibleStrategies);
                for(String name : unresolvedStrategies) {
                    for(IStrategoString strategyUse : usedStrategies.getPositions(name)) {
                        outputMessages.add(Message.strategyNotFound(moduleName, strategyUse));
                    }
                }
                final StringSetWithPositions definedStrategies =
                    staticData.definedStrategies.getOrDefault(moduleName, new StringSetWithPositions());
                Set<String> strategiesOverlapWithExternal = Sets
                    .difference(Sets.intersection(definedStrategies.readSet(), staticData.externalStrategies.readSet()), ALWAYS_DEFINED);
                for(String name : strategiesOverlapWithExternal) {
                    for(IStrategoString strategyDef : definedStrategies.getPositions(name)) {
                        outputMessages.add(Message.externalStrategyOverlap(moduleName, strategyDef));
                    }
                }
                Map<String, Set<String>> theUsedAmbStrategies =
                    new HashMap<>(staticData.usedAmbStrategies.getOrDefault(moduleName, Collections.emptyMap()));
                StringSetWithPositions ambStratPositions =
                    staticData.ambStratPositions.getOrDefault(moduleName, new StringSetWithPositions());
                // By default a _0_0 strategy is used in the ambiguous call situation if one is defined.
                theUsedAmbStrategies.keySet().removeIf(theVisibleStrategies::contains);
                if(!theUsedAmbStrategies.isEmpty()) {
                    Map<String, Set<String>> differentArityDefinitions = new HashMap<>(2 * theVisibleStrategies.size());
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
                                for(IStrategoString ambStrategyPosition : ambStratPositions.getPositions(usedAmbStrategy)) {
                                    outputMessages.add(Message.strategyNotFound(moduleName, ambStrategyPosition));
                                }
                                break;
                            case 1:
                                final String resolvedDef = defs.iterator().next();
                                for(String useSite : entry.getValue()) {
                                    getOrInitialize(ambStratResolution, useSite, TreeMap::new).put(usedAmbStrategy,
                                        resolvedDef);
                                }
                                break;
                            default:
                                for(IStrategoString ambStratPosition : ambStratPositions.getPositions(usedAmbStrategy)) {
                                    outputMessages.add(Message.ambiguousStrategyCall(moduleName, ambStratPosition, defs));
                                }
                        }
                    }
                }
            }
        }
        return new Output(ambStratResolution);
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
