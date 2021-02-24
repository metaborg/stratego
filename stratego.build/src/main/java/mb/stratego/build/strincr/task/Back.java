package mb.stratego.build.strincr.task;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Inject;

import org.metaborg.util.cmd.Arguments;
import org.spoofax.interpreter.core.Interpreter;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.util.B;
import org.strategoxt.strc.compile_top_level_def_0_0;
import org.strategoxt.strj.strj_sep_comp_0_0;

import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.TaskDef;
import mb.resource.fs.FSPath;
import mb.resource.hierarchical.ResourcePath;
import mb.stratego.build.strincr.IModuleImportService.ModuleIdentifier;
import mb.stratego.build.strincr.ResourcePathConverter;
import mb.stratego.build.strincr.data.ConstructorData;
import mb.stratego.build.strincr.data.ConstructorSignature;
import mb.stratego.build.strincr.data.ConstructorType;
import mb.stratego.build.strincr.data.OverlayData;
import mb.stratego.build.strincr.data.StrategySignature;
import mb.stratego.build.strincr.function.ModulesDefiningOverlays;
import mb.stratego.build.strincr.function.ToConstrData;
import mb.stratego.build.strincr.function.ToGlobalConsInj;
import mb.stratego.build.strincr.function.ToGlobalIndex;
import mb.stratego.build.strincr.function.ToOverlays;
import mb.stratego.build.strincr.function.output.GlobalConsInj;
import mb.stratego.build.strincr.function.output.GlobalIndex;
import mb.stratego.build.strincr.task.input.BackInput;
import mb.stratego.build.strincr.task.input.FrontInput;
import mb.stratego.build.strincr.task.output.BackOutput;
import mb.stratego.build.util.IOAgentTrackerFactory;
import mb.stratego.build.util.PieUtils;
import mb.stratego.build.util.StrIncrContext;
import mb.stratego.build.util.StrategoConstants;
import mb.stratego.build.util.StrategoExecutor;
import mb.stratego.build.util.StrategyStubs;
import mb.stratego.compiler.pack.Packer;

/**
 * Runs per strategy definition. This task desugars the strategy, and then generates Java code for
 * it. Desugaring here does means that dynamic rules are only lifted out at this point, which
 * complicates things slightly. So strategies that contribute to the same dynamic rule are bundled
 * into one larger Back task instead of being compiled separately. Desugaring output of these
 * dynamic rule Back tasks are analysed as well, to be able to generate the DYNAMIC_CALLS strategy
 * correctly.
 * Another artefact of desugaring late is that we don't care to pass constructors to the desugaring.
 * Therefore no congruence strategies are generated. Instead we do this in a separate Back task for
 * all constructors at once. If this turns out to be costly we can split it up to generate per
 * module or even per constructor.
 */
public class Back implements TaskDef<BackInput, BackOutput> {
    public static final String id = "stratego." + Back.class.getSimpleName();

    private final IOAgentTrackerFactory ioAgentTrackerFactory;
    private final StrIncrContext strContext;
    private final StrategyStubs strategyStubs;
    private final ITermFactory tf;
    private final ResourcePathConverter resourcePathConverter;
    private final CheckModule checkModule;
    private final Front front;

    @Inject public Back(IOAgentTrackerFactory ioAgentTrackerFactory, StrIncrContext strContext,
        StrategyStubs strategyStubs, ResourcePathConverter resourcePathConverter,
        CheckModule checkModule, Front front) {
        this.ioAgentTrackerFactory = ioAgentTrackerFactory;
        this.strContext = strContext;
        this.tf = strContext.getFactory();
        this.strategyStubs = strategyStubs;
        this.resourcePathConverter = resourcePathConverter;
        this.checkModule = checkModule;
        this.front = front;
    }

    @Override public BackOutput exec(ExecContext context, BackInput input) throws Exception {
        final Set<StrategySignature> compiledStrategies = new HashSet<>();
        final boolean isBoilerplate = input instanceof BackInput.Boilerplate;
        final IStrategoTerm ctree;
        final ConstructorSignature dr_dummy = new ConstructorSignature("DR_DUMMY", 0, 0);
        final ConstructorSignature dr_undefine = new ConstructorSignature("DR_UNDEFINE", 1, 0);
        final ConstructorSignature anno_cong__ = new ConstructorSignature("Anno_Cong__", 2, 0);
        if(isBoilerplate) {
            final GlobalConsInj globalConsInj =
                PieUtils.requirePartial(context, input.resolveTask, ToGlobalConsInj.INSTANCE);
            final List<ConstructorSignature> constructors =
                new ArrayList<>(globalConsInj.allModuleIdentifiers.size() + 3);
            final List<IStrategoTerm> consInjTerms = new ArrayList<>(
                globalConsInj.allModuleIdentifiers.size()
                    + globalConsInj.nonExternalInjections.size() + 3);
            for(ModuleIdentifier moduleIdentifier : globalConsInj.allModuleIdentifiers) {
                final ArrayList<ConstructorData> constructorData = PieUtils
                    .requirePartial(context, front, new FrontInput(moduleIdentifier, ((BackInput.Boilerplate) input).moduleImportService),
                        ToConstrData.INSTANCE);
                for(ConstructorData constructorDatum : constructorData) {
                    consInjTerms.add(constructorDatum.toTerm(tf));
                    constructors.add(constructorDatum.signature);
                }
            }
            consInjTerms.add(dr_dummy.toTerm(tf));
            consInjTerms.add(dr_undefine.toTerm(tf));
            consInjTerms.add(anno_cong__.toTerm(tf));
            constructors.add(dr_dummy);
            constructors.add(dr_undefine);
            constructors.add(anno_cong__);
            for(Map.Entry<IStrategoTerm, List<IStrategoTerm>> e : globalConsInj.nonExternalInjections
                .entrySet()) {
                final IStrategoTerm from = e.getKey();
                for(IStrategoTerm to : e.getValue()) {
                    consInjTerms.add(tf.makeAppl("ConsDeclInj", tf.makeAppl("FunType",
                        tf.makeList(ConstructorType.typeToConstType(tf, from)),
                        ConstructorType.typeToConstType(tf, to))));
                }
            }
            final Set<StrategySignature> strategies =
                new HashSet<>(globalConsInj.nonExternalStrategies);
            for(ConstructorSignature constructor : constructors) {
                strategies.add(constructor.toCongruenceSig());
            }
            if(((BackInput.Boilerplate) input).dynamicCallsDefined) {
                strategies.add(new StrategySignature("DYNAMIC_CALLS", 0, 0));
            }
            ctree = Packer
                .packBoilerplate(tf, consInjTerms, strategyStubs.declStubs(strategies));
        } else if(input instanceof BackInput.Congruence) {
            // TODO: run congruence task per module or even per constructor?
            final GlobalIndex globalIndex =
                PieUtils.requirePartial(context, input.resolveTask, ToGlobalIndex.INSTANCE);
            final List<ConstructorSignature> constructors =
                new ArrayList<>(globalIndex.nonExternalConstructors.size() + 2);
            constructors.addAll(globalIndex.nonExternalConstructors);
            constructors.add(dr_dummy);
            constructors.add(dr_undefine);

            final List<IStrategoAppl> congruences = new ArrayList<>(constructors.size() + 2);
            for(ConstructorSignature constructor : constructors) {
                if(globalIndex.nonExternalStrategies.contains(constructor.toCongruenceSig())) {
                    context.logger().debug(
                        "Skipping congruence overlapping with existing strategy: " + constructor);
                    continue;
                }
                if(globalIndex.externalConstructors.contains(constructor)) {
                    context.logger().debug(
                        "Skipping congruence of constructor overlapping with external constructor: " + constructor);
                    continue;
                }
                compiledStrategies.add(constructor.toCongruenceSig());
                congruences.add(constructor.congruenceAst(tf));
            }
            congruences.add(ConstructorSignature.annoCongAst(tf));
            compiledStrategies.add(new StrategySignature("Anno_Cong__", 2, 0));

            final BackInput.Congruence congruenceInput = (BackInput.Congruence) input;
            final @Nullable IStrategoAppl dynamicCallsDefinition =
                dynamicCallsDefinition(tf, congruenceInput.dynamicRuleNewGenerated,
                    congruenceInput.dynamicRuleUndefineGenerated);
            if(dynamicCallsDefinition != null) {
                congruences.add(dynamicCallsDefinition);
                compiledStrategies.add(new StrategySignature("DYNAMIC_CALLS", 0, 0));
            }

            ctree = Packer.packStrategy(tf, Collections.emptyList(), congruences);
        } else {
            final List<IStrategoAppl> strategyContributions = new ArrayList<>();
            final Set<ConstructorSignature> usedConstructors = new HashSet<>();
            final BackInput.Normal normalInput = (BackInput.Normal) input;
            normalInput.getStrategyContributions(context, checkModule, strategyContributions,
                usedConstructors);

            final Set<ModuleIdentifier> modulesDefiningOverlay = PieUtils
                .requirePartial(context, input.resolveTask,
                    new ModulesDefiningOverlays<>(usedConstructors));

            final List<IStrategoAppl> overlayContributions = new ArrayList<>();
            for(ModuleIdentifier moduleIdentifier : modulesDefiningOverlay) {
                final List<OverlayData> overlayData = PieUtils.requirePartial(context, front,
                    new FrontInput(moduleIdentifier, normalInput.moduleImportService),
                    new ToOverlays<>(usedConstructors));
                for(OverlayData overlayDatum : overlayData) {
                    overlayContributions.add(overlayDatum.astTerm);
                }
            }

            IStrategoTerm desugaringInput =
                Packer.packStrategy(tf, overlayContributions, strategyContributions);

            final StrategoExecutor.ExecutionResult result = StrategoExecutor
                .runLocallyUniqueStringStrategy(ioAgentTrackerFactory, context.logger(), true,
                    compile_top_level_def_0_0.instance, desugaringInput, strContext);

            if(!result.success) {
                throw new ExecException(
                    "Call to compile-top-level-def failed:\n" + result.exception, null);
            }
            assert result.result != null;

            ctree = result.result;

            //noinspection ConstantConditions
            final Set<StrategySignature> cifiedStrategySignatures =
                CheckModule.extractStrategyDefs(null, 0L, ctree, null).keySet();
            for(StrategySignature cified : cifiedStrategySignatures) {
                final @Nullable StrategySignature uncified = StrategySignature.fromCified(cified.name);
                if(uncified != null) {
                    compiledStrategies.add(uncified);
                }
            }
        }

        // Call Stratego compiler
        // Note that we need --library and turn off fusion with --fusion for separate compilation
        final Arguments arguments = new Arguments().add("-i", "passedExplicitly.ctree")
            .add("-o", resourcePathConverter.toString(input.outputDir))
            //            .add("--verbose", 3)
            .addLine(input.packageName != null ? "-p " + input.packageName : "").add("--library")
            .add("--fusion");
        if(isBoilerplate) {
            arguments.add("--boilerplate");
        } else {
            arguments.add("--single-strategy");
        }

        for(ResourcePath includeDir : input.includeDirs) {
            arguments.add("-I", resourcePathConverter.toString(includeDir));
        }

        if(input.cacheDir != null) {
            arguments.add("--cache-dir", resourcePathConverter.toString(input.cacheDir));
        }

        for(String constant : input.constants) {
            // Needed in boilerplate for generating a strategy (e.g. $C$O$N$S$T$A$N$T_0_0), needed
            //     in single-strategy to turn e.g. prim("CONSTANT") into Build(theconstantvalue),
            //     in the example where you give pass -DCONSTANT=theconstantvalue.
            arguments.add("-D", constant);
        }
        arguments.addAll(input.extraArgs);


        final StrategoExecutor.ExecutionResult result = StrategoExecutor
            .runLocallyUniqueStringStrategy(ioAgentTrackerFactory, context.logger(), true,
                strj_sep_comp_0_0.instance,
                buildInput(ctree, arguments, strj_sep_comp_0_0.instance.getName()), strContext);

        if(!result.success) {
            throw new ExecException("Call to strj-sep-comp failed:\n" + result.exception, null);
        }

        final Set<ResourcePath> resultFiles = new HashSet<>();
        // TODO: have the compilation return a list of files instead of printing to log
        for(String line : result.errLog.split("\\r\\n|[\\r\\n]")) {
            if(line.contains(StrategoConstants.STRJ_INFO_WRITING_FILE)) {
                String fileName = line.substring(
                    line.indexOf(StrategoConstants.STRJ_INFO_WRITING_FILE)
                        + StrategoConstants.STRJ_INFO_WRITING_FILE.length()).trim();
                final File file = new File(fileName);
                context.provide(file);
                resultFiles.add(new FSPath(file.toPath()));
            }
        }

        return new BackOutput(resultFiles, compiledStrategies);
    }

    private @Nullable IStrategoAppl dynamicCallsDefinition(ITermFactory tf,
        Collection<String> dynamicRulesNewGenerated,
        Collection<String> dynamicRulesUndefineGenerated) {
        @Nullable IStrategoAppl body = null;
        final IStrategoAppl id = tf.makeAppl("Id");
        final IStrategoAppl emptyStringLit =
            tf.makeAppl("Anno", tf.makeAppl("Str", tf.makeString("")),
                tf.makeAppl("Op", tf.makeString("Nil"), tf.makeList()));

        /* concrete syntax:
         *   new-[dr-rule-name](|"", "")
         * abstract syntax, desugared and name mangled:
         *   CallT(
         *     "new_[dr-rule-name]_0_2",
         *     [],
         *     [Anno(Str("\"\""), Op("Nil", [])), Anno(Str("\"\""), Op("Nil", []))])
         * strung together with `[call] <+ [other-calls]` or `GuardedLChoice([call], Id(), [other-calls])`
         */
        for(String dynamicRuleName : dynamicRulesNewGenerated) {
            final String drRuleNameNew = Interpreter.cify("new-" + dynamicRuleName) + "_0_2";
            final IStrategoAppl call =
                tf.makeAppl("CallT", tf.makeAppl("SVar", tf.makeString(drRuleNameNew)),
                    tf.makeList(), tf.makeList(emptyStringLit, emptyStringLit));
            if(body == null) {
                body = call;
            } else {
                body = tf.makeAppl("GuardedLChoice", call, id, body);
            }
        }

        /* concrete syntax:
         *   undefine-[dr-rule-name](|"")
         * abstract syntax, desugared and name mangled:
         *   CallT("undefine_[dr-rule-name]_0_1", [], [Anno(Str("\"\""), Op("Nil", []))])
         * strung together with `[call] <+ [other-calls]` or `GuardedLChoice([call], Id(), [other-calls])`
         */
        for(String dynamicRuleName : dynamicRulesUndefineGenerated) {
            final String drRuleNameNew = Interpreter.cify("undefine-" + dynamicRuleName) + "_0_1";
            final IStrategoAppl call =
                tf.makeAppl("CallT", tf.makeAppl("SVar", tf.makeString(drRuleNameNew)),
                    tf.makeList(), tf.makeList(emptyStringLit));
            if(body == null) {
                body = call;
            } else {
                body = tf.makeAppl("GuardedLChoice", call, id, body);
            }
        }
        if(body == null) {
            return null;
        }

        final String dynamicCalls = Interpreter.cify("DYNAMIC_CALLS") + "_0_0";
        return tf
            .makeAppl("SDefT", tf.makeString(dynamicCalls), tf.makeList(), tf.makeList(), body);
    }

    private static IStrategoList buildInput(IStrategoTerm ctree, Arguments arguments, String name) {
        List<String> strings = arguments.asStrings(null);
        final IStrategoTerm[] args = new IStrategoTerm[strings.size() + 2];
        args[0] = B.string(name);
        args[1] = ctree;
        int i = 2;
        for(String string : strings) {
            args[i] = B.string(string);
            i++;
        }

        return B.list(args);
    }

    @Override public String getId() {
        return id;
    }
}
