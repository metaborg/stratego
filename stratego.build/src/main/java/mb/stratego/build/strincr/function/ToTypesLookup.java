package mb.stratego.build.strincr.function;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import javax.annotation.Nullable;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import mb.stratego.build.strincr.data.ConstructorData;
import mb.stratego.build.strincr.data.ConstructorSignature;
import mb.stratego.build.strincr.data.ConstructorSignatureMatcher;
import mb.stratego.build.strincr.data.ConstructorType;
import mb.stratego.build.strincr.data.OverlayData;
import mb.stratego.build.strincr.data.StrategyFrontData;
import mb.stratego.build.strincr.data.StrategySignature;
import mb.stratego.build.strincr.data.StrategyType;
import mb.stratego.build.strincr.function.output.TypesLookup;
import mb.stratego.build.strincr.task.output.ModuleData;
import mb.stratego.build.util.Relation;

public class ToTypesLookup implements Function<ModuleData, TypesLookup>, Serializable {
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
        final HashMap<StrategySignature, StrategyType> strategyTypes = new HashMap<>();
        final HashMap<ConstructorSignature, HashSet<ConstructorType>> constructorTypes =
            new HashMap<>();
        final HashMap<String, HashSet<StrategyFrontData>> ambStrategyIndex =
            moduleData.ambStrategyIndex();
        for(StrategySignature usedStrategy : usedStrategies) {
            for(StrategyFrontData strategyFrontData : moduleData.normalStrategyData
                .getOrDefault(usedStrategy, new HashSet<>(0))) {
                registerStrategyType(strategyTypes, usedStrategy, strategyFrontData.getType(tf));
            }
            for(StrategyFrontData strategyFrontData : moduleData.internalStrategyData
                .getOrDefault(usedStrategy, new HashSet<>(0))) {
                registerStrategyType(strategyTypes, usedStrategy, strategyFrontData.getType(tf));
            }
            for(StrategyFrontData strategyFrontData : moduleData.externalStrategyData
                .getOrDefault(usedStrategy, new HashSet<>(0))) {
                registerStrategyType(strategyTypes, usedStrategy, strategyFrontData.getType(tf));
            }
            for(StrategyFrontData strategyFrontData : moduleData.dynamicRuleData
                .getOrDefault(usedStrategy, new HashSet<>(0))) {
                registerStrategyType(strategyTypes, usedStrategy, strategyFrontData.getType(tf));
            }
        }
        for(String usedStrategy : usedAmbiguousStrategies) {
            for(StrategyFrontData strategyFrontData : ambStrategyIndex
                .getOrDefault(usedStrategy, new HashSet<>(0))) {
                final StrategyType type = strategyFrontData.type != null ? strategyFrontData.type :
                    strategyFrontData.signature.standardType(tf);
                strategyTypes.put(strategyFrontData.signature, type);
            }
        }
        for(ConstructorSignature usedConstructor : usedConstructors) {
            for(ConstructorData constructorData : moduleData.constrData
                .getOrDefault(new ConstructorSignatureMatcher(usedConstructor),
                    new ArrayList<>(0))) {
                Relation.getOrInitialize(constructorTypes, constructorData.signature, HashSet::new)
                    .add(constructorData.type);
            }
            for(ConstructorData constructorData : moduleData.externalConstrData
                .getOrDefault(new ConstructorSignatureMatcher(usedConstructor),
                    new ArrayList<>(0))) {
                Relation.getOrInitialize(constructorTypes, constructorData.signature, HashSet::new)
                    .add(constructorData.type);
            }
            for(OverlayData overlayData : moduleData.overlayData
                .getOrDefault(new ConstructorSignatureMatcher(usedConstructor),
                    new ArrayList<>(0))) {
                Relation.getOrInitialize(constructorTypes, overlayData.signature, HashSet::new)
                    .add(overlayData.type);
            }
        }


        final HashMap<IStrategoTerm, ArrayList<IStrategoTerm>> injections =
            new HashMap<>(moduleData.injections);
        for(Map.Entry<IStrategoTerm, ArrayList<IStrategoTerm>> e : moduleData.externalInjections
            .entrySet()) {
            Relation.getOrInitialize(injections, e.getKey(), ArrayList::new).addAll(e.getValue());
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
