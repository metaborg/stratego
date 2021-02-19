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
    public final Map<ConstructorSignature, List<ConstructorData>> externalConstrData;
    public final Map<IStrategoTerm, List<IStrategoTerm>> injections;
    public final Map<IStrategoTerm, List<IStrategoTerm>> externalInjections;
    public final Map<StrategySignature, Set<StrategyFrontData>> normalStrategyData;
    public final Map<StrategySignature, Set<StrategyFrontData>> internalStrategyData;
    public final Map<StrategySignature, Set<StrategyFrontData>> externalStrategyData;
    public final Map<StrategySignature, Set<StrategyFrontData>> dynamicRuleData;
    public final Map<ConstructorSignature, List<OverlayData>> overlayData;
    public final Set<ConstructorSignature> usedConstructors;
    public final Set<StrategySignature> usedStrategies;
    public final Set<StrategySignature> dynamicRules;
    public final Set<String> usedAmbiguousStrategies;
    public final long lastModified;
    private transient @Nullable Map<String, Set<StrategyFrontData>> ambStrategyIndex = null;

    public ModuleData(ModuleIdentifier moduleIdentifier, IStrategoTerm ast,
        List<IStrategoTerm> imports, Map<ConstructorSignature, List<ConstructorData>> constrData,
        Map<ConstructorSignature, List<ConstructorData>> externalConstrData,
        Map<IStrategoTerm, List<IStrategoTerm>> injections,
        Map<IStrategoTerm, List<IStrategoTerm>> externalInjections,
        Map<StrategySignature, Set<StrategyFrontData>> normalStrategyData,
        Map<StrategySignature, Set<StrategyFrontData>> internalStrategyData,
        Map<StrategySignature, Set<StrategyFrontData>> externalStrategyData,
        Map<StrategySignature, Set<StrategyFrontData>> dynamicRuleData,
        Map<ConstructorSignature, List<OverlayData>> overlayData,
        Set<ConstructorSignature> usedConstructors, Set<StrategySignature> usedStrategies,
        Set<StrategySignature> dynamicRules, Set<String> usedAmbiguousStrategies,
        long lastModified) {
        this.moduleIdentifier = moduleIdentifier;
        this.ast = ast;
        this.imports = imports;
        this.constrData = constrData;
        this.externalConstrData = externalConstrData;
        this.injections = injections;
        this.externalInjections = externalInjections;
        this.normalStrategyData = normalStrategyData;
        this.internalStrategyData = internalStrategyData;
        this.externalStrategyData = externalStrategyData;
        this.dynamicRuleData = dynamicRuleData;
        this.overlayData = overlayData;
        this.usedConstructors = usedConstructors;
        this.usedStrategies = usedStrategies;
        this.dynamicRules = dynamicRules;
        this.usedAmbiguousStrategies = usedAmbiguousStrategies;
        this.lastModified = lastModified;
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        ModuleData that = (ModuleData) o;

        if(lastModified != that.lastModified)
            return false;
        if(!moduleIdentifier.equals(that.moduleIdentifier))
            return false;
        if(!ast.equals(that.ast))
            return false;
        if(!imports.equals(that.imports))
            return false;
        if(!constrData.equals(that.constrData))
            return false;
        if(!externalConstrData.equals(that.externalConstrData))
            return false;
        if(!injections.equals(that.injections))
            return false;
        if(!externalInjections.equals(that.externalInjections))
            return false;
        if(!normalStrategyData.equals(that.normalStrategyData))
            return false;
        if(!internalStrategyData.equals(that.internalStrategyData))
            return false;
        if(!externalStrategyData.equals(that.externalStrategyData))
            return false;
        if(!dynamicRuleData.equals(that.dynamicRuleData))
            return false;
        if(!overlayData.equals(that.overlayData))
            return false;
        if(!usedConstructors.equals(that.usedConstructors))
            return false;
        if(!usedStrategies.equals(that.usedStrategies))
            return false;
        if(!dynamicRules.equals(that.dynamicRules))
            return false;
        return usedAmbiguousStrategies.equals(that.usedAmbiguousStrategies);
    }

    @Override public int hashCode() {
        int result = moduleIdentifier.hashCode();
        result = 31 * result + ast.hashCode();
        result = 31 * result + imports.hashCode();
        result = 31 * result + constrData.hashCode();
        result = 31 * result + externalConstrData.hashCode();
        result = 31 * result + injections.hashCode();
        result = 31 * result + externalInjections.hashCode();
        result = 31 * result + normalStrategyData.hashCode();
        result = 31 * result + internalStrategyData.hashCode();
        result = 31 * result + externalStrategyData.hashCode();
        result = 31 * result + dynamicRuleData.hashCode();
        result = 31 * result + overlayData.hashCode();
        result = 31 * result + usedConstructors.hashCode();
        result = 31 * result + usedStrategies.hashCode();
        result = 31 * result + dynamicRules.hashCode();
        result = 31 * result + usedAmbiguousStrategies.hashCode();
        result = 31 * result + (int) (lastModified ^ lastModified >>> 32);
        return result;
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
                    moduleData.overlayData.get(new ConstructorSignatureMatcher(usedConstructor));
                if(overlayData != null) {
                    result.addAll(overlayData);
                }
            }
            return (T) result;
        }
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
            for(Map.Entry<StrategySignature, Set<StrategyFrontData>> e : dynamicRuleData
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
                new HashSet<>(moduleData.constrData.keySet()),
                new HashSet<>(moduleData.externalConstrData.keySet()),
                new HashSet<>(moduleData.normalStrategyData.keySet()),
                new HashSet<>(moduleData.internalStrategyData.keySet()),
                new HashSet<>(moduleData.externalStrategyData.keySet()), moduleData.dynamicRules,
                moduleData.overlayData, moduleData.lastModified);
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
                    registerStrategyType(strategyTypes, usedStrategy,
                        strategyFrontData.getType(tf));
                }
                for(StrategyFrontData strategyFrontData : moduleData.internalStrategyData
                    .getOrDefault(usedStrategy, Collections.emptySet())) {
                    registerStrategyType(strategyTypes, usedStrategy,
                        strategyFrontData.getType(tf));
                }
                for(StrategyFrontData strategyFrontData : moduleData.externalStrategyData
                    .getOrDefault(usedStrategy, Collections.emptySet())) {
                    registerStrategyType(strategyTypes, usedStrategy,
                        strategyFrontData.getType(tf));
                }
                for(StrategyFrontData strategyFrontData : moduleData.dynamicRuleData
                    .getOrDefault(usedStrategy, Collections.emptySet())) {
                    registerStrategyType(strategyTypes, usedStrategy,
                        strategyFrontData.getType(tf));
                }
            }
            for(String usedStrategy : usedAmbiguousStrategies) {
                for(StrategyFrontData strategyFrontData : ambStrategyIndex
                    .getOrDefault(usedStrategy, Collections.emptySet())) {
                    final StrategyType type =
                        strategyFrontData.type != null ? strategyFrontData.type :
                            strategyFrontData.signature.standardType(tf);
                    strategyTypes.put(strategyFrontData.signature, type);
                }
            }
            for(ConstructorSignature usedConstructor : usedConstructors) {
                for(ConstructorData constructorData : moduleData.constrData
                    .getOrDefault(new ConstructorSignatureMatcher(usedConstructor),
                        Collections.emptyList())) {
                    Relation
                        .getOrInitialize(constructorTypes, constructorData.signature, HashSet::new)
                        .add(constructorData.type);
                }
                for(ConstructorData constructorData : moduleData.externalConstrData
                    .getOrDefault(new ConstructorSignatureMatcher(usedConstructor),
                        Collections.emptyList())) {
                    Relation
                        .getOrInitialize(constructorTypes, constructorData.signature, HashSet::new)
                        .add(constructorData.type);
                }
                for(OverlayData overlayData : moduleData.overlayData
                    .getOrDefault(new ConstructorSignatureMatcher(usedConstructor),
                        Collections.emptyList())) {
                    Relation.getOrInitialize(constructorTypes, overlayData.signature, HashSet::new)
                        .add(overlayData.type);
                }
            }


            final Map<IStrategoTerm, List<IStrategoTerm>> injections =
                new HashMap<>(moduleData.injections);
            for(Map.Entry<IStrategoTerm, List<IStrategoTerm>> e : moduleData.externalInjections
                .entrySet()) {
                Relation.getOrInitialize(injections, e.getKey(), ArrayList::new)
                    .addAll(e.getValue());
            }
            return new TypesLookup(strategyTypes, constructorTypes, injections, moduleData.imports,
                moduleData.lastModified);
        }

        public static void registerStrategyType(Map<StrategySignature, StrategyType> strategyTypes,
            StrategySignature usedStrategy, StrategyType strategyType) {
            final @Nullable StrategyType current = strategyTypes.get(usedStrategy);
            if(current == null || !(strategyType instanceof StrategyType.Standard)) {
                if(current != null && !(current instanceof StrategyType.Standard)) {
                    //noinspection StatementWithEmptyBody
                    if(!current.equals(strategyType)) {
                        // TODO: Add check to type checker about multiple type definitions in
                        //      different modules
                    }
                    // Leave the first one we found...
                } else {
                    strategyTypes.put(usedStrategy, strategyType);
                }
            }
        }

        public static void registerStrategyType(
            io.usethesource.capsule.Map.Transient<StrategySignature, StrategyType> strategyTypes,
            StrategySignature usedStrategy, StrategyType strategyType) {
            final @Nullable StrategyType current = strategyTypes.get(usedStrategy);
            if(current == null || !(strategyType instanceof StrategyType.Standard)) {
                if(current != null && !(current instanceof StrategyType.Standard)) {
                    //noinspection StatementWithEmptyBody
                    if(!current.equals(strategyType)) {
                        // TODO: Add check to type checker about multiple type definitions in
                        //      different modules
                    }
                    // Leave the first one we found...
                } else {
                    strategyTypes.__put(usedStrategy, strategyType);
                }
            }
        }
    }
}
