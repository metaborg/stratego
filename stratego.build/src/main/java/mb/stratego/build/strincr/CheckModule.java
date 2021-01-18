package mb.stratego.build.strincr;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.spoofax.interpreter.library.ssl.StrategoImmutableMap;
import org.spoofax.interpreter.library.ssl.StrategoImmutableSet;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import io.usethesource.capsule.BinaryRelation;
import io.usethesource.capsule.Set.Transient;
import mb.pie.api.ExecContext;
import mb.pie.api.Task;
import mb.pie.api.TaskDef;
import mb.stratego.build.strincr.IModuleImportService.ModuleIdentifier;
import mb.stratego.build.util.PieUtils;
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
    }

    public static class Output implements Serializable {
        public final Map<StrategySignature, StrategyAnalysisData> strategyDataWithCasts;

        public Output(Map<StrategySignature, StrategyAnalysisData> strategyDataWithCasts) {
            this.strategyDataWithCasts = strategyDataWithCasts;
        }
    }

    private final Resolve resolve;
    private final Front front;
    private final Lib lib;
    private final ITermFactory tf;

    public CheckModule(Resolve resolve, Front front, Lib lib, StrIncrContext strIncrContext) {
        this.resolve = resolve;
        this.front = front;
        this.lib = lib;
        this.tf = strIncrContext.getFactory();
    }

    @Override public Output exec(ExecContext context, Input input) throws Exception {
        // Checks:
        //     - Gradual type check.
        //         - Provide relevant externals (overlapping with definitions in module), for checks
        //             of overlap between normal and external, and override/extend and external.
        //         - Provide overlays for desugaring
        //         - In stratego: provide externals checks
        return null;
    }

    private GTEnvironment prepareGTEnvironment(ExecContext context, Input input)
        throws IOException {
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
        return GTEnvironment.from(strategyEnvironment, constructorTypes.freeze(), injections.freeze(),
            internalStrategyEnvironment, externalStrategyEnvironment, moduleUsageData.ast, tf);
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
