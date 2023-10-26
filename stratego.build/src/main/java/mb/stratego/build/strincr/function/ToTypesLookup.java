package mb.stratego.build.strincr.function;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import jakarta.annotation.Nullable;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.pie.api.SerializableFunction;
import mb.stratego.build.strincr.data.ConstructorData;
import mb.stratego.build.strincr.data.ConstructorSignature;
import mb.stratego.build.strincr.data.ConstructorType;
import mb.stratego.build.strincr.data.SortSignature;
import mb.stratego.build.strincr.data.StrategyFrontData;
import mb.stratego.build.strincr.data.StrategySignature;
import mb.stratego.build.strincr.data.StrategyType;
import mb.stratego.build.strincr.function.output.TypesLookup;
import mb.stratego.build.strincr.task.output.ModuleData;
import mb.stratego.build.util.Relation;

public class ToTypesLookup implements SerializableFunction<ModuleData, TypesLookup> {
    public final LinkedHashSet<StrategySignature> definedStrategies;
    public final LinkedHashSet<ConstructorSignature> definedOverlays;
    public final LinkedHashSet<StrategySignature> usedStrategies;
    public final LinkedHashSet<String> usedAmbiguousStrategies;
    public final LinkedHashSet<ConstructorSignature> usedConstructors;

    public ToTypesLookup(LinkedHashSet<StrategySignature> definedStrategies,
        LinkedHashSet<ConstructorSignature> definedOverlays, LinkedHashSet<StrategySignature> usedStrategies,
        LinkedHashSet<String> usedAmbiguousStrategies, LinkedHashSet<ConstructorSignature> usedConstructors) {
        this.definedStrategies = definedStrategies;
        this.definedOverlays = definedOverlays;
        this.usedStrategies = usedStrategies;
        this.usedAmbiguousStrategies = usedAmbiguousStrategies;
        this.usedConstructors = usedConstructors;
    }

    @Override public TypesLookup apply(ModuleData moduleData) {
        final LinkedHashMap<StrategySignature, StrategyType> strategyTypes = new LinkedHashMap<>();
        final LinkedHashMap<ConstructorSignature, HashSet<ConstructorType>> constructorTypes =
            new LinkedHashMap<>();
        final HashMap<String, LinkedHashSet<StrategyFrontData>> ambStrategyIndex =
            moduleData.ambStrategyIndex();
        for(StrategySignature definedStrategy : definedStrategies) {
            for(StrategyFrontData strategyFrontData : moduleData.normalStrategyData
                .getOrDefault(definedStrategy, new ArrayList<>(0))) {
                registerStrategyType(strategyTypes, definedStrategy, strategyFrontData);
            }
            for(StrategyFrontData strategyFrontData : moduleData.internalStrategyData
                .getOrDefault(definedStrategy, new ArrayList<>(0))) {
                registerStrategyType(strategyTypes, definedStrategy, strategyFrontData);
            }
            final @Nullable ConstructorSignature usedConstructor = definedStrategy.toConstructorSignature();
            if(usedConstructor != null) {
                for(ConstructorData constructorData : moduleData.constrData
                    .getOrDefault(usedConstructor, new ArrayList<>(0))) {
                    Relation.getOrInitialize(constructorTypes, constructorData.signature, HashSet::new)
                        .add(constructorData.type);
                }
                for(ConstructorData constructorData : moduleData.externalConstrData
                    .getOrDefault(usedConstructor, new ArrayList<>(0))) {
                    Relation.getOrInitialize(constructorTypes, constructorData.signature, HashSet::new)
                        .add(constructorData.type);
                }
                for(ConstructorData overlayData : moduleData.overlayData
                    .getOrDefault(usedConstructor, new ArrayList<>(0))) {
                    Relation.getOrInitialize(constructorTypes, overlayData.signature, HashSet::new)
                        .add(overlayData.type);
                }
            }
        }
        for(StrategySignature usedStrategy : usedStrategies) {
            for(StrategyFrontData strategyFrontData : moduleData.normalStrategyData
                .getOrDefault(usedStrategy, new ArrayList<>(0))) {
                registerStrategyType(strategyTypes, usedStrategy, strategyFrontData);
            }
            for(StrategyFrontData strategyFrontData : moduleData.internalStrategyData
                .getOrDefault(usedStrategy, new ArrayList<>(0))) {
                registerStrategyType(strategyTypes, usedStrategy, strategyFrontData);
            }
            for(StrategyFrontData strategyFrontData : moduleData.externalStrategyData
                .getOrDefault(usedStrategy, new ArrayList<>(0))) {
                registerStrategyType(strategyTypes, usedStrategy, strategyFrontData);
            }
            for(StrategyFrontData strategyFrontData : moduleData.dynamicRuleData
                .getOrDefault(usedStrategy, new ArrayList<>(0))) {
                registerStrategyType(strategyTypes, usedStrategy, strategyFrontData);
            }
        }
        for(String usedAmbiguousStrategy : usedAmbiguousStrategies) {
            for(StrategyFrontData strategyFrontData : ambStrategyIndex
                .getOrDefault(usedAmbiguousStrategy, new LinkedHashSet<>(0))) {
                registerStrategyType(strategyTypes, strategyFrontData.signature, strategyFrontData);
            }
        }
        for(ConstructorSignature usedConstructor : definedOverlays) {
            for(ConstructorData constructorData : moduleData.overlayData
                .getOrDefault(usedConstructor, new ArrayList<>(0))) {
                Relation.getOrInitialize(constructorTypes, constructorData.signature, HashSet::new)
                    .add(constructorData.type);
            }
        }
        for(ConstructorSignature usedConstructor : usedConstructors) {
            for(ConstructorData constructorData : moduleData.constrData
                .getOrDefault(usedConstructor, new ArrayList<>(0))) {
                Relation.getOrInitialize(constructorTypes, constructorData.signature, HashSet::new)
                    .add(constructorData.type);
            }
            for(ConstructorData constructorData : moduleData.externalConstrData
                .getOrDefault(usedConstructor, new ArrayList<>(0))) {
                Relation.getOrInitialize(constructorTypes, constructorData.signature, HashSet::new)
                    .add(constructorData.type);
            }
            for(ConstructorData overlayData : moduleData.overlayData
                .getOrDefault(usedConstructor, new ArrayList<>(0))) {
                Relation.getOrInitialize(constructorTypes, overlayData.signature, HashSet::new)
                    .add(overlayData.type);
            }
        }

        final LinkedHashSet<SortSignature> sorts = new LinkedHashSet<>(moduleData.sortData);
        sorts.addAll(moduleData.externalSortData);

        final LinkedHashMap<IStrategoTerm, ArrayList<IStrategoTerm>> injections =
            new LinkedHashMap<>(moduleData.injections);
        for(Map.Entry<IStrategoTerm, ArrayList<IStrategoTerm>> e : moduleData.externalInjections
            .entrySet()) {
            Relation.getOrInitialize(injections, e.getKey(), ArrayList::new).addAll(e.getValue());
        }
        return new TypesLookup(strategyTypes, constructorTypes, sorts, injections,
            moduleData.imports, moduleData.lastModified);
    }

    public static void registerStrategyType(Map<StrategySignature, StrategyType> strategyTypes,
        StrategySignature usedStrategy, StrategyFrontData strategyFrontData) {
        final @Nullable StrategyType current = strategyTypes.get(usedStrategy);
        if(current == null ||
            current instanceof StrategyType.Standard && !(strategyFrontData.type instanceof StrategyType.Standard)) {
            strategyTypes.put(usedStrategy, strategyFrontData.type);
            return;
        }
        if(!current.equals(strategyFrontData.type)) {
            // TODO: Add check to type checker about multiple type definitions in
            //      different modules
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

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        ToTypesLookup that = (ToTypesLookup) o;

        if(!definedStrategies.equals(that.definedStrategies))
            return false;
        if(!definedOverlays.equals(that.definedOverlays))
            return false;
        if(!usedStrategies.equals(that.usedStrategies))
            return false;
        if(!usedAmbiguousStrategies.equals(that.usedAmbiguousStrategies))
            return false;
        return usedConstructors.equals(that.usedConstructors);
    }

    @Override public int hashCode() {
        int result = definedStrategies.hashCode();
        result = 31 * result + definedOverlays.hashCode();
        result = 31 * result + usedStrategies.hashCode();
        result = 31 * result + usedAmbiguousStrategies.hashCode();
        result = 31 * result + usedConstructors.hashCode();
        return result;
    }
}
