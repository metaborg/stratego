package mb.stratego.build.strincr;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import javax.annotation.Nullable;

import org.spoofax.interpreter.library.ssl.StrategoImmutableMap;
import org.spoofax.interpreter.library.ssl.StrategoImmutableSet;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.util.TermUtils;

import io.usethesource.capsule.BinaryRelation;
import io.usethesource.capsule.Set.Transient;
import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.Task;
import mb.pie.api.TaskDef;
import mb.stratego.build.strincr.IModuleImportService.ModuleIdentifier;
import mb.stratego.build.util.PieUtils;
import mb.stratego.build.util.Relation;
import mb.stratego.build.util.StrIncrContext;

public class CheckModule implements TaskDef<CheckModule.Input, CheckModule.Output> {
    public static final String id = CheckModule.class.getCanonicalName();

    public static class Input extends Front.Input {
        public final ModuleIdentifier mainModuleIdentifier;

        public Input(ModuleIdentifier mainModuleIdentifier, ModuleIdentifier moduleIdentifier,
            IModuleImportService moduleImportService) {
            super(moduleIdentifier, moduleImportService);
            this.mainModuleIdentifier = mainModuleIdentifier;
        }

        public Check.Input resolveInput() {
            return new Check.Input(mainModuleIdentifier, moduleImportService);
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o)
                return true;
            if(o == null || getClass() != o.getClass())
                return false;
            if(!super.equals(o))
                return false;

            Input input = (Input) o;

            return mainModuleIdentifier.equals(input.mainModuleIdentifier);
        }

        @Override public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + mainModuleIdentifier.hashCode();
            return result;
        }
    }

    public static class Output implements Serializable {
        public final Map<StrategySignature, Set<StrategyAnalysisData>> strategyDataWithCasts;

        public Output(Map<StrategySignature, Set<StrategyAnalysisData>> strategyDataWithCasts) {
            this.strategyDataWithCasts = strategyDataWithCasts;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o)
                return true;
            if(o == null || getClass() != o.getClass())
                return false;

            Output output = (Output) o;

            return strategyDataWithCasts.equals(output.strategyDataWithCasts);
        }

        @Override public int hashCode() {
            return strategyDataWithCasts.hashCode();
        }

        public static class GetStrategyAnalysisData<T extends Set<StrategyAnalysisData> & Serializable>
            implements Function<Output, T>, Serializable {
            public final StrategySignature strategySignature;

            public GetStrategyAnalysisData(StrategySignature strategySignature) {
                this.strategySignature = strategySignature;
            }

            @SuppressWarnings("unchecked")
            @Override public T apply(Output output) {
                return (T) output.strategyDataWithCasts
                    .getOrDefault(strategySignature, Collections.emptySet());
            }

            @Override public boolean equals(@Nullable Object o) {
                if(this == o)
                    return true;
                if(o == null || getClass() != o.getClass())
                    return false;

                GetStrategyAnalysisData<?> that = (GetStrategyAnalysisData<?>) o;

                return strategySignature.equals(that.strategySignature);
            }

            @Override public int hashCode() {
                return strategySignature.hashCode();
            }
        }
    }

    private final Resolve resolve;
    private final Front front;
    private final Lib lib;
    private final InsertCasts insertCasts;
    private final ITermFactory tf;

    public CheckModule(Resolve resolve, Front front, Lib lib, InsertCasts insertCasts,
        StrIncrContext strIncrContext) {
        this.resolve = resolve;
        this.front = front;
        this.lib = lib;
        this.insertCasts = insertCasts;
        this.tf = strIncrContext.getFactory();
    }

    @Override public Output exec(ExecContext context, Input input) throws Exception {
        final InsertCasts.Input2 input2 =
            new InsertCasts.Input2(input.moduleIdentifier, prepareGTEnvironment(context, input));
        final InsertCasts.Output output = context.require(insertCasts, input2);
        final Map<StrategySignature, Set<StrategyAnalysisData>> strategyDataWithCasts =
            new HashMap<>();
        extractStrategyDefs(input.moduleIdentifier, input2.environment.lastModified,
            output.astWithCasts, strategyDataWithCasts);
        return new Output(strategyDataWithCasts);
    }

    private static void extractStrategyDefs(ModuleIdentifier moduleIdentifier, long lastModified,
        IStrategoTerm ast, Map<StrategySignature, Set<StrategyAnalysisData>> strategyData)
        throws WrongASTException {

        final IStrategoList defs = Front.getDefs(moduleIdentifier, ast);
        for(IStrategoTerm def : defs) {
            if(!TermUtils.isAppl(def) || def.getSubtermCount() != 1) {
                throw new WrongASTException(moduleIdentifier, def);
            }
            switch(TermUtils.toAppl(def).getName()) {
                case "Imports":
                case "Overlays":
                case "Signature":
                    break;
                case "Rules":
                    // fall-through
                case "Strategies":
                    addStrategyData(moduleIdentifier, lastModified, strategyData, def);
                    break;
                default:
                    throw new WrongASTException(moduleIdentifier, def);
            }
        }
    }

    private static void addStrategyData(ModuleIdentifier moduleIdentifier, long lastModified,
        Map<StrategySignature, Set<StrategyAnalysisData>> strategyData, IStrategoTerm strategyDefs)
        throws WrongASTException {
        for(IStrategoTerm strategyDef : strategyDefs) {
            if(!TermUtils.isAppl(strategyDef, "DefHasType", 3)) {
                if(TermUtils.isAppl(strategyDef, "AnnoDef", 2)) {
                    strategyDef = strategyDef.getSubterm(1);
                }
                if(!TermUtils.isAppl(strategyDef)) {
                    throw new WrongASTException(moduleIdentifier, strategyDef);
                }
                final IStrategoAppl strategyDefAppl = TermUtils.toAppl(strategyDef);
                if(!TermUtils.isStringAt(strategyDefAppl, 0)) {
                    throw new WrongASTException(moduleIdentifier, strategyDefAppl);
                }
                final String name = TermUtils.toJavaStringAt(strategyDefAppl, 0);
                final int sArity;
                final int tArity;
                switch(strategyDefAppl.getName()) {
                    case "ExtSDef":
                        // fall-through
                    case "SDef":
                        // fall-through
                    case "RDef": {
                        final IStrategoTerm sargs = strategyDefAppl.getSubterm(1);
                        if(!TermUtils.isList(sargs)) {
                            throw new WrongASTException(moduleIdentifier, sargs);
                        }
                        sArity = sargs.getSubtermCount();
                        tArity = 0;
                        break;
                    }
                    case "SDefNoArgs":
                        // fall-through
                    case "RDefNoArgs":
                        sArity = 0;
                        tArity = 0;
                        break;
                    case "ExtSDefInl":
                        // fall-through
                    case "SDefT":
                        // fall-through
                    case "RDefT":
                        // fall-through
                    case "RDefP": {
                        final IStrategoTerm sargs = strategyDefAppl.getSubterm(1);
                        if(!TermUtils.isList(sargs)) {
                            throw new WrongASTException(moduleIdentifier, sargs);
                        }
                        sArity = sargs.getSubtermCount();
                        final IStrategoTerm targs = strategyDefAppl.getSubterm(2);
                        if(!TermUtils.isList(targs)) {
                            throw new WrongASTException(moduleIdentifier, targs);
                        }
                        tArity = targs.getSubtermCount();
                        break;
                    }
                    default:
                        throw new WrongASTException(moduleIdentifier, strategyDefAppl);
                }
                final StrategySignature strategySignature =
                    new StrategySignature(name, sArity, tArity);
                Relation.getOrInitialize(strategyData, strategySignature, HashSet::new)
                    .add(new StrategyAnalysisData(strategyDefAppl, lastModified));
            }
        }
    }

    private GTEnvironment prepareGTEnvironment(ExecContext context, Input input)
        throws IOException, ExecException {
        final ModuleUsageData moduleUsageData =
            PieUtils.requirePartial(context, front, input, ModuleData.ToModuleUsageData.INSTANCE);

        // Get all defined internal and external strategy names in all modules
        final Set<ModuleIdentifier> allModules = new HashSet<>(PieUtils
            .requirePartial(context, resolve, input.resolveInput(),
                GlobalData.AllModulesIdentifiers.Instance));
        allModules.remove(input.moduleIdentifier);
        // TODO: move combined index of all internal/external strategy names to a separate task,
        //       require that with a filter like we do now with ToAnnoDefs
        final Transient<StrategySignature> internalStrategySigs = Transient.of();
        final Transient<StrategySignature> externalStrategySigs = Transient.of();
        for(ModuleIdentifier moduleIdentifier : allModules) {
            final ModuleAnnoDefs moduleAnnoDefs = PieUtils.requirePartial(context,
                moduleIdentifierToTask(moduleIdentifier, input.moduleImportService),
                new ModuleData.ToAnnoDefs(moduleUsageData.definedStrategies));
            internalStrategySigs.__insertAll(moduleAnnoDefs.internalStrategySigs);
            externalStrategySigs.__insertAll(moduleAnnoDefs.externalStrategySigs);
        }

        // Get the relevant strategy and constructor types and all injections, that are visible
        //     through the import graph
        final io.usethesource.capsule.Map.Transient<StrategySignature, StrategyType> strategyTypes =
            io.usethesource.capsule.Map.Transient.of();
        final BinaryRelation.Transient<ConstructorSignature, ConstructorType> constructorTypes =
            BinaryRelation.Transient.of();
        final BinaryRelation.Transient<IStrategoTerm, IStrategoTerm> injections =
            BinaryRelation.Transient.of();

        final java.util.Set<ModuleIdentifier> seen = new HashSet<>();
        final Deque<ModuleIdentifier> workList = new ArrayDeque<>(Resolve
            .expandImports(context, input.moduleImportService, moduleUsageData.imports,
                moduleUsageData.lastModified, null));
        seen.add(input.moduleIdentifier);
        do {
            final ModuleIdentifier moduleIdentifier = workList.remove();

            final Task<ModuleData> task;
            if(moduleIdentifier.isLibrary()) {
                task = lib.createTask(new Front.Input(moduleIdentifier, input.moduleImportService));
            } else {
                task =
                    front.createTask(new Front.Input(moduleIdentifier, input.moduleImportService));
            }
            final TypesLookup typesLookup = PieUtils.requirePartial(context, task,
                new ModuleData.ToTypesLookup(tf, moduleUsageData.usedStrategies,
                    moduleUsageData.usedAmbiguousStrategies, moduleUsageData.usedConstructors));
            for(Map.Entry<StrategySignature, StrategyType> e : typesLookup.strategyTypes
                .entrySet()) {
                final StrategyType current = strategyTypes.get(e.getKey());
                if(!(e.getValue() instanceof StrategyType.Standard)) {
                    //noinspection StatementWithEmptyBody
                    if(current != null && !(current instanceof StrategyType.Standard)) {
                        // Leave the first one we found...
                        // TODO: Add check to type checker about multiple type definitions in
                        //      different modules
                    } else {
                        strategyTypes.__put(e.getKey(), e.getValue());
                    }
                }
            }
            for(Map.Entry<ConstructorSignature, Set<ConstructorType>> e : typesLookup.constructorTypes
                .entrySet()) {
                for(ConstructorType ty : e.getValue()) {
                    constructorTypes.__put(e.getKey(), ty);
                }
            }
            for(Map.Entry<IStrategoTerm, List<IStrategoTerm>> e : typesLookup.injections
                .entrySet()) {
                for(IStrategoTerm to : e.getValue()) {
                    injections.__put(e.getKey(), to);
                }
            }

            final Set<ModuleIdentifier> expandedImports = Resolve
                .expandImports(context, input.moduleImportService, typesLookup.imports,
                    typesLookup.lastModified, null);
            expandedImports.removeAll(seen);
            workList.addAll(expandedImports);
            seen.addAll(expandedImports);
        } while(!workList.isEmpty());

        final StrategoImmutableSet internalStrategyEnvironment =
            new StrategoImmutableSet(internalStrategySigs.freeze());
        final StrategoImmutableSet externalStrategyEnvironment =
            new StrategoImmutableSet(externalStrategySigs.freeze());
        final StrategoImmutableMap strategyEnvironment =
            new StrategoImmutableMap(strategyTypes.freeze());
        return GTEnvironment
            .from(strategyEnvironment, constructorTypes.freeze(), injections.freeze(),
                internalStrategyEnvironment, externalStrategyEnvironment, moduleUsageData.ast, tf,
                moduleUsageData.lastModified);
    }

    private Task<ModuleData> moduleIdentifierToTask(ModuleIdentifier moduleIdentifier,
        IModuleImportService moduleImportService) {
        if(moduleIdentifier.isLibrary()) {
            return lib.createTask(new Front.Input(moduleIdentifier, moduleImportService));
        } else {
            return front.createTask(new Front.Input(moduleIdentifier, moduleImportService));
        }
    }

    @Override public String getId() {
        return id;
    }
}
