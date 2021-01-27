package mb.stratego.build.strincr;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import javax.annotation.Nullable;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import mb.stratego.build.strincr.IModuleImportService.ModuleIdentifier;
import mb.stratego.build.util.Relation;
import mb.stratego.build.util.WithLastModified;

/**
 * The AST of a module and some of it's data pre-extracted.
 */
public class ModuleData implements Serializable, WithLastModified {
    public final ModuleIdentifier moduleIdentifier;
    public final IStrategoTerm ast;
    public final List<IStrategoTerm> imports;
    public final Map<ConstructorSignature, List<ConstructorData>> constrData;
    public final Map<IStrategoTerm, List<IStrategoTerm>> injections;
    public final Map<StrategySignature, Set<StrategyFrontData>> normalStrategyData;
    public final Map<StrategySignature, Set<StrategyFrontData>> internalStrategyData;
    public final Map<StrategySignature, Set<StrategyFrontData>> externalStrategyData;
    public final Map<ConstructorSignature, List<OverlayData>> overlayData;
    public final Set<ConstructorSignature> usedConstructors;
    public final Set<StrategySignature> usedStrategies;
    public final Set<String> usedAmbiguousStrategies;
    public final long lastModified;
    private transient @Nullable Set<StrategySignature> allStrategies = null;
    private transient @Nullable Map<String, Set<StrategyFrontData>> ambStrategyIndex = null;

    public ModuleData(ModuleIdentifier moduleIdentifier, IStrategoTerm ast,
        List<IStrategoTerm> imports, Map<ConstructorSignature, List<ConstructorData>> constrData,
        Map<IStrategoTerm, List<IStrategoTerm>> injections,
        Map<StrategySignature, Set<StrategyFrontData>> normalStrategyData,
        Map<StrategySignature, Set<StrategyFrontData>> internalStrategyData,
        Map<StrategySignature, Set<StrategyFrontData>> externalStrategyData,
        Map<ConstructorSignature, List<OverlayData>> overlayData,
        Set<ConstructorSignature> usedConstructors, Set<StrategySignature> usedStrategies,
        Set<String> usedAmbiguousStrategies, long lastModified) {
        this.moduleIdentifier = moduleIdentifier;
        this.ast = ast;
        this.imports = imports;
        this.constrData = constrData;
        this.injections = injections;
        this.normalStrategyData = normalStrategyData;
        this.internalStrategyData = internalStrategyData;
        this.externalStrategyData = externalStrategyData;
        this.overlayData = overlayData;
        this.usedConstructors = usedConstructors;
        this.usedStrategies = usedStrategies;
        this.usedAmbiguousStrategies = usedAmbiguousStrategies;
        this.lastModified = lastModified;
    }

    public static class ToOverlays<T extends List<OverlayData> & Serializable>
        implements Function<ModuleData, T>, Serializable {
        private final Set<ConstructorSignature> usedConstructors;

        public ToOverlays(Set<ConstructorSignature> usedConstructors) {
            this.usedConstructors = usedConstructors;
        }

        @SuppressWarnings("unchecked") @Override public T apply(ModuleData moduleData) {
            final List<OverlayData> result = new ArrayList<>();
            for(ConstructorSignature usedConstructor : usedConstructors) {
                final @Nullable List<OverlayData> overlayData =
                    moduleData.overlayData.get(usedConstructor);
                if(overlayData != null) {
                    result.addAll(overlayData);
                }
            }
            return (T) result;
        }
    }

    public Set<StrategySignature> allStrategies() {
        if(allStrategies == null) {
            allStrategies = new HashSet<>();
            allStrategies.addAll(normalStrategyData.keySet());
            allStrategies.addAll(internalStrategyData.keySet());
            allStrategies.addAll(externalStrategyData.keySet());
        }
        return allStrategies;
    }

    public Map<String, Set<StrategyFrontData>> ambStrategyIndex() {
        if(ambStrategyIndex == null) {
            ambStrategyIndex = new HashMap<>();
            for(Map.Entry<StrategySignature, Set<StrategyFrontData>> e : normalStrategyData
                .entrySet()) {
                Relation.getOrInitialize(ambStrategyIndex, e.getKey().name, HashSet::new)
                    .addAll(e.getValue());
            }
            for(Map.Entry<StrategySignature, Set<StrategyFrontData>> e : internalStrategyData
                .entrySet()) {
                Relation.getOrInitialize(ambStrategyIndex, e.getKey().name, HashSet::new)
                    .addAll(e.getValue());
            }
            for(Map.Entry<StrategySignature, Set<StrategyFrontData>> e : externalStrategyData
                .entrySet()) {
                Relation.getOrInitialize(ambStrategyIndex, e.getKey().name, HashSet::new)
                    .addAll(e.getValue());
            }
        }
        return ambStrategyIndex;
    }

    public static class ToModuleIndex implements Function<ModuleData, ModuleIndex>, Serializable {
        public static final ModuleData.ToModuleIndex INSTANCE = new ModuleData.ToModuleIndex();

        private ToModuleIndex() {
        }

        @Override public ModuleIndex apply(ModuleData moduleData) {
            return new ModuleIndex(moduleData.imports,
                new HashSet<>(moduleData.constrData.keySet()), moduleData.allStrategies(),
                moduleData.overlayData, moduleData.lastModified);
        }
    }

    public static class ToModuleUsageData
        implements Function<ModuleData, ModuleUsageData>, Serializable {
        public static final ModuleData.ToModuleUsageData INSTANCE =
            new ModuleData.ToModuleUsageData();

        private ToModuleUsageData() {
        }

        @Override public ModuleUsageData apply(ModuleData moduleData) {
            return new ModuleUsageData(moduleData.ast, moduleData.imports,
                moduleData.allStrategies(), moduleData.usedConstructors, moduleData.usedStrategies,
                moduleData.usedAmbiguousStrategies, moduleData.lastModified);
        }
    }

    public static class ToAnnoDefs implements Function<ModuleData, ModuleAnnoDefs>, Serializable {
        public final Set<StrategySignature> filter;

        public ToAnnoDefs(Set<StrategySignature> filter) {
            this.filter = filter;
        }

        @Override public ModuleAnnoDefs apply(ModuleData moduleData) {
            // Assumption: filter.size() > internalStrategyData.size() + externalStrategyData.size()
            final Set<StrategySignature> internalStrategyData = new HashSet<>();
            for(StrategySignature strategySignature : moduleData.internalStrategyData.keySet()) {
                if(filter.contains(strategySignature)) {
                    internalStrategyData.add(strategySignature);
                }
            }
            final Set<StrategySignature> externalStrategyData = new HashSet<>();
            for(StrategySignature strategySignature : moduleData.externalStrategyData.keySet()) {
                if(filter.contains(strategySignature)) {
                    externalStrategyData.add(strategySignature);
                }
            }
            return new ModuleAnnoDefs(internalStrategyData, externalStrategyData);
        }
    }

    @Override public long lastModified() {
        return lastModified;
    }

    public static class ToTypesLookup implements Function<ModuleData, TypesLookup>, Serializable {
        public final ITermFactory tf;
        public final Set<StrategySignature> usedStrategies;
        public final Set<String> usedAmbiguousStrategies;
        public final Set<ConstructorSignature> usedConstructors;

        public ToTypesLookup(ITermFactory tf, Set<StrategySignature> usedStrategies,
            Set<String> usedAmbiguousStrategies, Set<ConstructorSignature> usedConstructors) {
            this.tf = tf;
            this.usedStrategies = usedStrategies;
            this.usedAmbiguousStrategies = usedAmbiguousStrategies;
            this.usedConstructors = usedConstructors;
        }

        @Override public TypesLookup apply(ModuleData moduleData) {
            final Map<StrategySignature, StrategyType> strategyTypes = new HashMap<>();
            final Map<ConstructorSignature, Set<ConstructorType>> constructorTypes =
                new HashMap<>();
            final Map<String, Set<StrategyFrontData>> ambStrategyIndex =
                moduleData.ambStrategyIndex();
            for(StrategySignature usedStrategy : usedStrategies) {
                for(StrategyFrontData strategyFrontData : moduleData.normalStrategyData
                    .getOrDefault(usedStrategy, Collections.emptySet())) {
                    if(strategyFrontData.type != null) {
                        strategyTypes.put(strategyFrontData.signature, strategyFrontData.type);
                    } else {
                        strategyTypes.put(strategyFrontData.signature,
                            strategyFrontData.signature.standardType(tf));
                    }
                }
                for(StrategyFrontData strategyFrontData : moduleData.internalStrategyData
                    .getOrDefault(usedStrategy, Collections.emptySet())) {
                    if(strategyFrontData.type != null) {
                        strategyTypes.put(strategyFrontData.signature, strategyFrontData.type);
                    } else {
                        strategyTypes.put(strategyFrontData.signature,
                            strategyFrontData.signature.standardType(tf));
                    }
                }
                for(StrategyFrontData strategyFrontData : moduleData.externalStrategyData
                    .getOrDefault(usedStrategy, Collections.emptySet())) {
                    if(strategyFrontData.type != null) {
                        strategyTypes.put(strategyFrontData.signature, strategyFrontData.type);
                    } else {
                        strategyTypes.put(strategyFrontData.signature,
                            strategyFrontData.signature.standardType(tf));
                    }
                }
            }
            for(String usedStrategy : usedAmbiguousStrategies) {
                for(StrategyFrontData strategyFrontData : ambStrategyIndex
                    .getOrDefault(usedStrategy, Collections.emptySet())) {
                    if(strategyFrontData.type != null) {
                        strategyTypes.put(strategyFrontData.signature, strategyFrontData.type);
                    } else {
                        strategyTypes.put(strategyFrontData.signature,
                            strategyFrontData.signature.standardType(tf));
                    }
                }
            }
            for(ConstructorSignature usedConstructor : usedConstructors) {
                for(ConstructorData constructorData : moduleData.constrData
                    .getOrDefault(usedConstructor, Collections.emptyList())) {
                    Relation
                        .getOrInitialize(constructorTypes, constructorData.signature, HashSet::new)
                        .add(constructorData.type);
                }
            }

            return new TypesLookup(strategyTypes, constructorTypes, moduleData.injections,
                moduleData.imports, moduleData.lastModified);
        }
    }
}
