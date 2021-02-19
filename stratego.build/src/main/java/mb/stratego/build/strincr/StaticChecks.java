package mb.stratego.build.strincr;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import javax.annotation.Nullable;
import javax.inject.Inject;

import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.AbstractTermFactory;
import org.spoofax.terms.StrategoString;

import com.google.common.collect.Sets;

import io.usethesource.capsule.BinaryRelation;
import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.stratego.build.strincr.message.Message;
import mb.stratego.build.termvisitors.SugarAnalysis;
import mb.stratego.build.util.Algorithms;
import mb.stratego.build.util.Relation;
import mb.stratego.build.util.StrIncrContext;
import mb.stratego.build.util.StrategoGradualSetting;
import mb.stratego.build.util.StrategyEnvironment;
import mb.stratego.build.util.StringSetWithPositions;

import static mb.stratego.build.strincr.StrIncrAnalysis.reportOverlappingStrategies;

public class StaticChecks {
    public static final class Output implements Serializable {
        // Cified-strategy-name (where the call occurs) to cified-strategy-name (amb call) to cified-strategy-name (amb
        // call resolves to)
        public final Map<String, SortedMap<String, String>> ambStratResolution;

        Output(Map<String, SortedMap<String, String>> ambStratResolution) {
            this.ambStratResolution = ambStratResolution;
        }

        @Override
        public String toString() {
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
            }
            return "  (none)";
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ambStratResolution.hashCode();
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if(this == obj)
                return true;
            if(obj == null)
                return false;
            if(getClass() != obj.getClass())
                return false;
            Output other = (Output) obj;
            return ambStratResolution.equals(other.ambStratResolution);
        }
    }

    public static final class Data implements Serializable {
        // Module-path to strategy "signatures" (name + arity) to types (FunTType)
        public final Map<String, Map<StrategySignature, IStrategoTerm>> libraryStrategies = new HashMap<>();
        // Module-path to constructor "signatures" (name + arity) to types (ConstrType)
        public final Map<String, Map<ConstructorSignature, IStrategoTerm>> libraryConstructors = new HashMap<>();
        // Module-path to injections (pairs of types)
        public final Map<String, Set<IStrategoTerm>> libraryInjections = new HashMap<>();

        // Module-path to sugar ast
        public final Map<String, IStrategoTerm> sugarASTs = new HashMap<>();
        // Module-path to module-path
        public final Map<String, Set<String>> imports = new HashMap<>();
        // Module-path to cified-strategy-names used (to AST names of actual use)
        public final Map<String, StrategyEnvironment> usedStrategies = new HashMap<>();
        // Module-path to cified-strategy-name used in ambiguous call position to cified-strategy-names where the calls
        // occur
        public final Map<String, Map<String, Set<String>>> usedAmbStrategies = new HashMap<>();
        // Module-path to cified-strategy-name used in ambiguous call position (to AST names of actual use)
        public final Map<String, StringSetWithPositions> ambStratPositions = new HashMap<>();
        // Module-path to constructor_arity names used (to AST names of actual use)
        public final Map<String, StringSetWithPositions> usedConstructors = new HashMap<>();
        // Module-path to cified-strategy-names defined here (to AST names of actual definitions)
        public final Map<String, StrategyEnvironment> definedStrategies = new HashMap<>();
        // Module-path to cified-strategy-names defined here as congruences
        public final Map<String, StrategyEnvironment> definedCongruences = new HashMap<>();
        // Module-path to external cified-strategy-names that will be imported in Java
        public final Map<String, StrategyEnvironment> externalStrategies = new HashMap<>();
        // External cified-strategy-names that will be imported in Java
        public final StrategyEnvironment libraryExternalStrategies = new StrategyEnvironment();
        // Internal cified-strategy-names that will be imported in Java
        public final Map<String, Set<String>> internalStrategies = new HashMap<>();
        // External constructors that will be imported in Java
        public final StringSetWithPositions externalConstructors = new StringSetWithPositions();
        // Module-path to constructor_arity names defined there (to AST names of actual definitions)
        public final Map<String, StringSetWithPositions> definedConstructors = new HashMap<>();
        // Cified-strategy-names that need a corresponding name in a library because it overrides
        // or extends it. (tostrategy definition AST names)
        public final StrategyEnvironment strategyNeedsExternal = new StrategyEnvironment();
        // Constructor_arity overlay name. (to overlay definition AST names)
        public final StringSetWithPositions overlayDefs = new StringSetWithPositions();
        // Constructor strictness for each strategy
        public final Map<String, Boolean> strictnessLevel = new HashMap<>();

        @Override public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Data data = (Data) o;
            if(!libraryStrategies.equals(data.libraryStrategies)) return false;
            if(!libraryConstructors.equals(data.libraryConstructors)) return false;
            if(!libraryInjections.equals(data.libraryInjections)) return false;
            if(!sugarASTs.equals(data.sugarASTs)) return false;
            if(!imports.equals(data.imports)) return false;
            if(!usedStrategies.equals(data.usedStrategies)) return false;
            if(!usedAmbStrategies.equals(data.usedAmbStrategies)) return false;
            if(!ambStratPositions.equals(data.ambStratPositions)) return false;
            if(!usedConstructors.equals(data.usedConstructors)) return false;
            if(!definedStrategies.equals(data.definedStrategies)) return false;
            if(!definedCongruences.equals(data.definedCongruences)) return false;
            if(!externalStrategies.equals(data.externalStrategies)) return false;
            if(!libraryExternalStrategies.equals(data.libraryExternalStrategies)) return false;
            if(!internalStrategies.equals(data.internalStrategies)) return false;
            if(!externalConstructors.equals(data.externalConstructors)) return false;
            if(!definedConstructors.equals(data.definedConstructors)) return false;
            if(!strategyNeedsExternal.equals(data.strategyNeedsExternal)) return false;
            if(!overlayDefs.equals(data.overlayDefs)) return false;
            return strictnessLevel.equals(data.strictnessLevel);
        }

        @Override public int hashCode() {
            int result = libraryStrategies.hashCode();
            result = 31 * result + libraryConstructors.hashCode();
            result = 31 * result + libraryInjections.hashCode();
            result = 31 * result + sugarASTs.hashCode();
            result = 31 * result + imports.hashCode();
            result = 31 * result + usedStrategies.hashCode();
            result = 31 * result + usedAmbStrategies.hashCode();
            result = 31 * result + ambStratPositions.hashCode();
            result = 31 * result + usedConstructors.hashCode();
            result = 31 * result + definedStrategies.hashCode();
            result = 31 * result + definedCongruences.hashCode();
            result = 31 * result + externalStrategies.hashCode();
            result = 31 * result + libraryExternalStrategies.hashCode();
            result = 31 * result + internalStrategies.hashCode();
            result = 31 * result + externalConstructors.hashCode();
            result = 31 * result + definedConstructors.hashCode();
            result = 31 * result + strategyNeedsExternal.hashCode();
            result = 31 * result + overlayDefs.hashCode();
            result = 31 * result + strictnessLevel.hashCode();
            return result;
        }

        public void registerConstructorDefinitions(String modulePath, Map<ConstructorSignature, IStrategoTerm> constrs) {
            libraryConstructors.put(modulePath, constrs);
            final StringSetWithPositions set = new StringSetWithPositions();
            for(ConstructorSignature constr : constrs.keySet()) {
                set.add(new StrategoString(constr.cifiedName(), AbstractTermFactory.EMPTY_LIST));
            }
            registerConstructorDefinitions(modulePath, set, new StringSetWithPositions());
        }

        public void registerConstructorDefinitions(String modulePath, StringSetWithPositions constrs,
            StringSetWithPositions overlays) {
            final StringSetWithPositions visConstrs = new StringSetWithPositions(constrs);
            visConstrs.addAll(overlays);
            definedConstructors.put(modulePath, visConstrs);
        }
    }

    // remove once strategoxt is bootstrapped and has an updated baseline. This stuff was added to the standard library and can be removed from the compiler once the baseline is updated.
    static final HashSet<String> ALWAYS_DEFINED =
        new HashSet<>(Arrays.asList("DR__DUMMY_0_0", "Anno__Cong_____2_0", "DR__UNDEFINE_1_0"));

    protected final Frontend strIncrFront;
    protected final InsertCasts strIncrInsertCasts;
    private final StrIncrContext strContext;

    @Inject
    public StaticChecks(Frontend strIncrFront, InsertCasts strIncrInsertCasts, StrIncrContext strContext) {
        this.strIncrFront = strIncrFront;
        this.strIncrInsertCasts = strIncrInsertCasts;
        this.strContext = strContext;
    }

    public void insertCasts(ExecContext execContext, String mainFileModulePath, StrIncrAnalysis.Output output,
        StrategoGradualSetting strGradualSetting) throws ExecException {
        final Data staticData = output.staticData;
        final StrategyEnvironment allExternals = new StrategyEnvironment(staticData.libraryExternalStrategies);
        for(StrategyEnvironment s : staticData.externalStrategies.values()) {
            allExternals.addAll(s);
        }
        final Set<String> definedConstructors = new HashSet<>();
        for(StringSetWithPositions sswp : staticData.definedConstructors.values()) {
            definedConstructors.addAll(sswp.readSet());
        }
        final SugarAnalysis sugarAnalysis = new SugarAnalysis(output.messages, definedConstructors);

        // Module-path to cified_strategy names to FunTType visible in the module (def or import of def)
        final Map<String, Map<StrategySignature, IStrategoTerm>> stratEnvInclImports = new HashMap<>();
        // Module-path to constructor_arity names to ConstrType visible in the module (def or import of def)
        final Map<String, BinaryRelation.Immutable<ConstructorSignature, IStrategoTerm>> constrEnvInclImports =
            new HashMap<>();
        // Module-path to Sort type to Sort type in the module (def or import of def)
        final Map<String, BinaryRelation.Immutable<IStrategoTerm, IStrategoTerm>> injEnvInclImports = new HashMap<>();
        final Deque<Set<String>> sccs = Algorithms.topoSCCs(Collections.singleton(mainFileModulePath),
            k -> staticData.imports.getOrDefault(k, Collections.emptySet()));
        for(Set<String> scc : sccs) {
            // Gather up environment for SCC (typically just 1 module)
            Map<StrategySignature, IStrategoTerm> sccStrategyEnv = new HashMap<>();
            BinaryRelation.Transient<ConstructorSignature, IStrategoTerm> sccConstructorEnvT =
                BinaryRelation.Transient.of();
            BinaryRelation.Transient<IStrategoTerm, IStrategoTerm> sccInjEnvT = BinaryRelation.Transient.of();
            final ITermFactory tf = strContext.getFactory();
            prepareSCCEnv(output, staticData, stratEnvInclImports, constrEnvInclImports, injEnvInclImports, scc,
                sccStrategyEnv, sccConstructorEnvT, sccInjEnvT, tf);
            BinaryRelation.Immutable<ConstructorSignature, IStrategoTerm> sccConstructorEnv =
                sccConstructorEnvT.freeze();
            BinaryRelation.Immutable<IStrategoTerm, IStrategoTerm> sccInjEnv = sccInjEnvT.freeze();
            // Do the actual work
            for(String moduleName : scc) {
                // Incrementally build the transitive closure while traversing the SCCs
                constrEnvInclImports.put(moduleName, sccConstructorEnv);
                stratEnvInclImports.put(moduleName, sccStrategyEnv);
                injEnvInclImports.put(moduleName, sccInjEnv);

                if(Library.Builtin.isBuiltinLibrary(moduleName)) {
                    continue;
                }
                // CHECK for constant congruences & overlap between local variables and nullary constructors
                if(!staticData.sugarASTs.containsKey(moduleName)) {
                    execContext.logger().debug("Sugar ASTs available for: " + staticData.sugarASTs.keySet());
                    throw new ExecException("Cannot find sugar AST for " + moduleName);
                }
                sugarAnalysis.visit(moduleName, staticData.sugarASTs.get(moduleName));
                // CHECK for externals that are also locally defined
                overlapWithExternals(staticData, output.messages, moduleName, allExternals);
                // Insert casts (mutates splitResult.strategyDefs)
                final SplitResult splitResult = output.splitModules.get(moduleName);
                switch(strGradualSetting) {
                    case DYNAMIC:
                        insertCasts(moduleName, execContext, output.messages, sccStrategyEnv, tf, sccConstructorEnv,
                            sccInjEnv, splitResult, true);
                        break;
                    case STATIC:
                        insertCasts(moduleName, execContext, output.messages, sccStrategyEnv, tf, sccConstructorEnv,
                            sccInjEnv, splitResult, false);
                        break;
                    case NONE:
                        break;
                }
                for(List<IStrategoTerm> consDefs : splitResult.consDefs.values()) {
                    output.backendData.consDefs.addAll(consDefs);
                }

                long shuffleStartTime;

                final Frontend.Input frontInput =
                    new Frontend.Input(splitResult.inputFileString, splitResult);
                final @Nullable Frontend.NormalOutput frontOutput =
                    execContext.require(strIncrFront, frontInput).normalOutput();
                // Shuffle information
                if(frontOutput == null) {
                    execContext.logger().debug("File deletion detected: " + splitResult.inputFileString);
                    continue;
                }
                execContext.logger().debug("File parsed: " + splitResult.inputFileString);

                for(Map.Entry<String, Integer> strategyNoOfDefs : frontOutput.noOfDefinitions.entrySet()) {
                    Relation
                        .getOrInitialize(BuildStats.modulesDefiningStrategy, strategyNoOfDefs.getKey(), ArrayList::new)
                        .add(strategyNoOfDefs.getValue());
                }
                shuffleStartTime = System.nanoTime();

                // combining output for check
                for(StringSetWithPositions usedConstrs : frontOutput.strategyConstrs.values()) {
                    Relation.getOrInitialize(staticData.usedConstructors, moduleName, StringSetWithPositions::new)
                        .addAll(usedConstrs);
                }
                staticData.usedStrategies.put(moduleName, frontOutput.usedStrategies);
                staticData.usedAmbStrategies.put(moduleName, frontOutput.ambStratUsed);
                staticData.ambStratPositions.put(moduleName, frontOutput.ambStratPositions);
                staticData.definedStrategies.put(moduleName, frontOutput.strats);
                staticData.internalStrategies.put(moduleName, frontOutput.internalStrats);
                reportOverlappingStrategies(staticData.libraryExternalStrategies, frontOutput.externalStrats,
                    execContext.logger());
                staticData.externalStrategies.put(moduleName, frontOutput.externalStrats);
                staticData.definedCongruences.put(moduleName, frontOutput.congrs);
                staticData.registerConstructorDefinitions(moduleName, frontOutput.constrs, frontOutput.overlays);

                staticData.strategyNeedsExternal.addAll(frontOutput.strategyNeedsExternal);


                // shuffling output for backend
                for(Map.Entry<String, IStrategoAppl> gen : frontOutput.strategyASTs.entrySet()) {
                    String strategyName = gen.getKey();
                    // ensure the strategy is a key in the strategyFiles map
                    Relation.getOrInitialize(output.backendData.strategyASTs, strategyName, ArrayList::new)
                        .add(gen.getValue());
                    final StringSetWithPositions constructorSignatures =
                        frontOutput.strategyConstrs.get(strategyName);
                    Relation.getOrInitialize(output.backendData.strategyConstrs, strategyName, HashSet::new)
                        .addAll(constructorSignatures.readSet());
                }
                for(Map.Entry<String, IStrategoAppl> gen : frontOutput.congrASTs.entrySet()) {
                    final String congrName = gen.getKey();
                    output.backendData.congrASTs.put(congrName, gen.getValue());
                    final StringSetWithPositions constructorSignatures = frontOutput.strategyConstrs.get(congrName);
                    Relation.getOrInitialize(output.backendData.strategyConstrs, congrName, HashSet::new)
                        .addAll(constructorSignatures.readSet());
                }
                for(Map.Entry<String, List<IStrategoAppl>> gen : frontOutput.overlayASTs.entrySet()) {
                    final String overlayName = gen.getKey();

                    Relation.getOrInitialize(output.backendData.overlayASTs, overlayName, ArrayList::new)
                        .addAll(gen.getValue());
                }
                for(Map.Entry<String, StringSetWithPositions> gen : frontOutput.overlayConstrs.entrySet()) {
                    final String overlayName = gen.getKey();
                    final StringSetWithPositions constructorSignatures = gen.getValue();
                    Relation.getOrInitialize(output.backendData.overlayConstrs, overlayName, HashSet::new)
                        .addAll(constructorSignatures.readSet());
                }

                BuildStats.shuffleTime += System.nanoTime() - shuffleStartTime;
            }
        }

        sccs.clear();

        cyclicOverlays(mainFileModulePath, output.staticData, output.backendData.overlayConstrs, output.messages);

        // Run old static checks while we move those from here to the type system implementation
        strategyNeedsExternal(mainFileModulePath, staticData, output.messages, allExternals);
    }

    public void insertCasts(String moduleName, ExecContext execContext, List<Message<?>> outputMessages,
        Map<StrategySignature, IStrategoTerm> sccStrategyEnv, ITermFactory tf,
        BinaryRelation.Immutable<ConstructorSignature, IStrategoTerm> sccConstructorEnv,
        BinaryRelation.Immutable<IStrategoTerm, IStrategoTerm> sccInjEnv, SplitResult splitResult, boolean keepCasts) {
        final InsertCasts.Input.Builder builder =
            new InsertCasts.Input.Builder(moduleName, sccStrategyEnv, sccConstructorEnv, sccInjEnv, tf);
        for(Map.Entry<StrategySignature, IStrategoTerm> e : splitResult.strategyDefs.entrySet()) {
            final InsertCasts.Input insertCastsInput = builder.build(e.getValue());
            final InsertCasts.Output result = execContext.require(strIncrInsertCasts, insertCastsInput);
            if(keepCasts) {
                e.setValue(result.astWithCasts);
            }
            outputMessages.addAll(result.messages);
        }
    }

    public static void prepareSCCEnv(StrIncrAnalysis.Output output, Data staticData,
        Map<String, Map<StrategySignature, IStrategoTerm>> stratEnvInclImports,
        Map<String, BinaryRelation.Immutable<ConstructorSignature, IStrategoTerm>> constrEnvInclImports,
        Map<String, BinaryRelation.Immutable<IStrategoTerm, IStrategoTerm>> injEnvInclImports, Set<String> scc,
        Map<StrategySignature, IStrategoTerm> sccStrategyEnv,
        BinaryRelation.Transient<ConstructorSignature, IStrategoTerm> sccConstructorEnvT,
        BinaryRelation.Transient<IStrategoTerm, IStrategoTerm> sccInjEnvT, ITermFactory tf) {
        for(String moduleName : scc) {
            if(Library.Builtin.isBuiltinLibrary(moduleName)) {
                final Map<StrategySignature, IStrategoTerm> definedStrats =
                    staticData.libraryStrategies.get(moduleName);
                for(Map.Entry<StrategySignature, IStrategoTerm> e : definedStrats.entrySet()) {
                    sccStrategyEnv.put(e.getKey(), e.getValue());
                }

                final Map<ConstructorSignature, IStrategoTerm> definedConstrs =
                    staticData.libraryConstructors.get(moduleName);
                for(Map.Entry<ConstructorSignature, IStrategoTerm> e : definedConstrs.entrySet()) {
                    sccConstructorEnvT.__insert(e.getKey(), e.getValue());
                }

                final Set<IStrategoTerm> definedInjs =
                    staticData.libraryInjections.get(moduleName);
                for(IStrategoTerm inj : definedInjs) {
                    sccInjEnvT.__insert(inj.getSubterm(0), inj.getSubterm(1));
                }
                continue;
            }
            final SplitResult splitResult = output.splitModules.get(moduleName);
            if(splitResult == null) {
                throw new NullPointerException("Cannot find splitResult for module " + moduleName);
            }

            final Map<StrategySignature, IStrategoTerm> moduleEnv = new HashMap<>(2 * splitResult.defTypes.size());
            // untyped definitions
            for(StrategySignature sig : splitResult.strategyDefs.keySet()) {
                moduleEnv.put(sig, sig.standardType(tf).toTerm(tf));
            }
            // typed definitions
            moduleEnv.putAll(splitResult.defTypes);
            // generated strategies for dynamic rules
            for(StrategySignature dynRuleSig : splitResult.dynRuleSigs) {
                for(Map.Entry<StrategySignature, StrategyType> e : dynRuleSig
                    .dynamicRuleSignatures(tf).entrySet()) {
                    moduleEnv.put(e.getKey(), e.getValue().toTerm(tf));
                }
            }
            // congruences
            for(Map.Entry<ConstructorSignature, IStrategoTerm> e : splitResult.consTypes.entrySet()) {
//                moduleEnv.put(e.getKey().toCongruenceSig(), e.getValue());
                sccConstructorEnvT.__insert(e.getKey(), e.getValue());
            }
            Relation.putAll(sccInjEnvT, splitResult.injections);
            sccStrategyEnv.putAll(moduleEnv);
            for(String mod : staticData.imports.getOrDefault(moduleName, Collections.emptySet())) {
                Relation
                    .putAll(sccConstructorEnvT, constrEnvInclImports.getOrDefault(mod, BinaryRelation.Immutable.of()));
                Relation.putAll(sccInjEnvT, injEnvInclImports.getOrDefault(mod, BinaryRelation.Immutable.of()));
                final Map<StrategySignature, IStrategoTerm> importEnv =
                    stratEnvInclImports.getOrDefault(mod, new HashMap<>());
                for(Map.Entry<StrategySignature, IStrategoTerm> e : importEnv.entrySet()) {
                    final StrategySignature key = e.getKey();
                    final IStrategoTerm value = e.getValue();
                    if(sccStrategyEnv.containsKey(key)) {
                        final IStrategoTerm oldValue = sccStrategyEnv.get(key);
                        if(oldValue.equals(value) || key.standardType(tf).equals(value)) {
                            continue;
                        }
                    }
                    sccStrategyEnv.put(key, value);
                }
            }
        }
    }

    /**
     * CHECK for overlap with external strategies (error condition)
     * TODO: move this into the type checker (insertCasts)
     */
    private static void overlapWithExternals(Data staticData, List<Message<?>> outputMessages, String moduleName,
        StrategyEnvironment allExternals) {
        final StrategyEnvironment definedStrategies =
            staticData.definedStrategies.getOrDefault(moduleName, new StrategyEnvironment());
        final Set<String> internalStrategies =
            staticData.internalStrategies.getOrDefault(moduleName, Collections.emptySet());
        final Set<String> strategiesOverlapWithExternal = Sets.difference(
            Sets.difference(Sets.intersection(definedStrategies.readSet(), allExternals.readSet()), ALWAYS_DEFINED),
            internalStrategies);
        for(String name : strategiesOverlapWithExternal) {
            if(!staticData.strategyNeedsExternal.contains(name)) {
                for(IStrategoString strategyDef : definedStrategies.getPositions(name)) {
                    outputMessages.add(Message.externalStrategyOverlap(moduleName, strategyDef));
                }
            }
        }
    }

    /**
     * CHECK that overlays do not cyclically use each other (error condition) (New check, old compiler looped)
     * TODO: move this into the type checker (insertCasts) -- is that possible to do in the type checker?
     */
    private static void cyclicOverlays(String mainFileModulePath, Data staticData,
        Map<String, Set<String>> overlayConstrs, List<Message<?>> outputMessages) {
        final Deque<Set<String>> overlaySccs =
            Algorithms.topoSCCs(overlayConstrs.keySet(), k -> overlayConstrs.getOrDefault(k, Collections.emptySet()));
        overlaySccs.removeIf(s -> {
            String overlayName = s.iterator().next();
            return s.size() == 1 && !overlayConstrs.getOrDefault(overlayName, Collections.emptySet())
                .contains(overlayName);
        });
        for(Set<String> overlayScc : overlaySccs) {
            for(String name : overlayScc) {
                for(IStrategoString overlayName : staticData.overlayDefs.getPositions(name)) {
                    outputMessages.add(Message.cyclicOverlay(mainFileModulePath, overlayName, overlayScc));
                }
            }
        }
    }

    /**
     * CHECK that extending and/or overriding strategies have an external strategy to extend and/or override (New check,
     * old compiler generated Java code that would fail to compile)
     * TODO: move this into the type checker (insertCasts)
     */
    private static void strategyNeedsExternal(String mainFileModulePath, Data staticData,
        List<Message<?>> outputMessages, StrategyEnvironment allExternals) {
        Set<String> strategyNeedsExternalNonOverlap =
            Sets.difference(staticData.strategyNeedsExternal.readSet(), allExternals.readSet());
        for(String name : strategyNeedsExternalNonOverlap) {
            for(IStrategoString definitionName : staticData.strategyNeedsExternal.getPositions(name)) {
                outputMessages.add(Message.externalStrategyNotFound(mainFileModulePath, definitionName));
            }
        }
    }

}
