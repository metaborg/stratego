package mb.stratego.build.strincr;

import java.io.File;
import java.io.Serializable;
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
import mb.pie.api.STask;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKeyString;
import mb.resource.hierarchical.ResourcePath;
import mb.stratego.build.strincr.IModuleImportService.ModuleIdentifier;
import mb.stratego.build.termvisitors.UsedConstrs;
import mb.stratego.build.util.CommonPaths;
import mb.stratego.build.util.IOAgentTrackerFactory;
import mb.stratego.build.util.PieUtils;
import mb.stratego.build.util.StrIncrContext;
import mb.stratego.build.util.StrategoConstants;
import mb.stratego.build.util.StrategoExecutor;
import mb.stratego.compiler.pack.Packer;

public class Back implements TaskDef<Back.Input, Back.Output> {
    public static final String id = Back.class.getCanonicalName();

    public static abstract class Input implements Serializable {
        public final ResourcePath outputDir;
        public final @Nullable String packageName;
        public final @Nullable ResourcePath cacheDir;
        public final List<String> constants;
        public final Collection<ResourcePath> includeDirs;
        public final Arguments extraArgs;
        public final STask<GlobalData> resolveTask;

        public Input(ResourcePath outputDir, @Nullable String packageName,
            @Nullable ResourcePath cacheDir, List<String> constants,
            Collection<ResourcePath> includeDirs, Arguments extraArgs,
            STask<GlobalData> resolveTask) {
            this.outputDir = outputDir;
            this.packageName = packageName;
            this.cacheDir = cacheDir;
            this.constants = constants;
            this.includeDirs = includeDirs;
            this.extraArgs = extraArgs;
            this.resolveTask = resolveTask;
        }

        @Override public boolean equals(Object o) {
            if(this == o)
                return true;
            if(o == null || getClass() != o.getClass())
                return false;

            Input input = (Input) o;

            if(!outputDir.equals(input.outputDir))
                return false;
            if(packageName != null ? !packageName.equals(input.packageName) :
                input.packageName != null)
                return false;
            if(cacheDir != null ? !cacheDir.equals(input.cacheDir) : input.cacheDir != null)
                return false;
            if(!constants.equals(input.constants))
                return false;
            if(!includeDirs.equals(input.includeDirs))
                return false;
            if(!extraArgs.equals(input.extraArgs))
                return false;
            return resolveTask.equals(input.resolveTask);
        }

        @Override public int hashCode() {
            int result = outputDir.hashCode();
            result = 31 * result + (packageName != null ? packageName.hashCode() : 0);
            result = 31 * result + (cacheDir != null ? cacheDir.hashCode() : 0);
            result = 31 * result + constants.hashCode();
            result = 31 * result + includeDirs.hashCode();
            result = 31 * result + extraArgs.hashCode();
            result = 31 * result + resolveTask.hashCode();
            return result;
        }

        @Override public abstract String toString();
    }

    public static class NormalInput extends Input {
        public final StrategySignature strategySignature;
        public final ModuleIdentifier mainModuleIdentifier;
        public final IModuleImportService moduleImportService;

        public NormalInput(StrategySignature strategySignature, ResourcePath outputDir,
            @Nullable String packageName, @Nullable ResourcePath cacheDir, List<String> constants,
            Collection<ResourcePath> includeDirs, Arguments extraArgs,
            STask<GlobalData> resolveTask, ModuleIdentifier mainModuleIdentifier,
            IModuleImportService moduleImportService) {
            super(outputDir, packageName, cacheDir, constants, includeDirs, extraArgs, resolveTask);
            this.strategySignature = strategySignature;
            this.mainModuleIdentifier = mainModuleIdentifier;
            this.moduleImportService = moduleImportService;
        }

        public void getStrategyContributions(ExecContext context, CheckModule checkModule,
            List<IStrategoAppl> strategyContributions, Set<ConstructorSignature> usedConstructors) {
            final StrategySignature strategySignature = this.strategySignature;
            final Set<ModuleIdentifier> modulesDefiningStrategy = PieUtils
                .requirePartial(context, this.resolveTask,
                    new GlobalData.ModulesDefiningStrategy<>(strategySignature));

            for(ModuleIdentifier moduleIdentifier : modulesDefiningStrategy) {
                if(moduleIdentifier.isLibrary()) {
                    continue;
                }
                final Set<StrategyAnalysisData> strategyAnalysisData = PieUtils
                    .requirePartial(context, checkModule,
                        new CheckModule.Input(this.mainModuleIdentifier, moduleIdentifier,
                            this.moduleImportService),
                        new CheckModule.GetStrategyAnalysisData<>(strategySignature));
                for(StrategyAnalysisData strategyAnalysisDatum : strategyAnalysisData) {
                    strategyContributions.add(strategyAnalysisDatum.analyzedAst);
                    new UsedConstrs(usedConstructors, strategyAnalysisDatum.lastModified)
                        .visit(strategyAnalysisDatum.analyzedAst);
                }
            }
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o)
                return true;
            if(o == null || getClass() != o.getClass())
                return false;
            if(!super.equals(o))
                return false;

            NormalInput that = (NormalInput) o;

            if(!strategySignature.equals(that.strategySignature))
                return false;
            if(!mainModuleIdentifier.equals(that.mainModuleIdentifier))
                return false;
            return moduleImportService.equals(that.moduleImportService);
        }

        @Override public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + strategySignature.hashCode();
            result = 31 * result + mainModuleIdentifier.hashCode();
            result = 31 * result + moduleImportService.hashCode();
            return result;
        }

        @Override public String toString() {
            return "Back.NormalInput(" + strategySignature.cifiedName() + ")";
        }
    }

    public static class DynamicRuleInput extends NormalInput {
        public final STask<Check.Output> checkTask;

        public DynamicRuleInput(StrategySignature strategySignature, ResourcePath outputDir,
            @Nullable String packageName, @Nullable ResourcePath cacheDir, List<String> constants,
            Collection<ResourcePath> includeDirs, Arguments extraArgs,
            STask<GlobalData> resolveTask, ModuleIdentifier mainModuleIdentifier,
            IModuleImportService moduleImportService, STask<Check.Output> checkTask) {
            super(strategySignature, outputDir, packageName, cacheDir, constants, includeDirs,
                extraArgs, resolveTask, mainModuleIdentifier, moduleImportService);
            this.checkTask = checkTask;
        }

        @Override public void getStrategyContributions(ExecContext context, CheckModule checkModule,
            List<IStrategoAppl> strategyContributions, Set<ConstructorSignature> usedConstructors) {
            final StrategySignature strategySignature = this.strategySignature;
            final Set<ModuleIdentifier> modulesDefiningStrategy = PieUtils
                .requirePartial(context, this.checkTask,
                    new Check.ModulesDefiningDynamicRule<>(strategySignature));

            for(ModuleIdentifier moduleIdentifier : modulesDefiningStrategy) {
                if(moduleIdentifier.isLibrary()) {
                    continue;
                }
                final Set<StrategyAnalysisData> strategyAnalysisData = PieUtils
                    .requirePartial(context, checkModule,
                        new CheckModule.Input(this.mainModuleIdentifier, moduleIdentifier,
                            this.moduleImportService),
                        new CheckModule.GetDynamicRuleAnalysisData<>(strategySignature));
                for(StrategyAnalysisData strategyAnalysisDatum : strategyAnalysisData) {
                    strategyContributions.add(strategyAnalysisDatum.analyzedAst);
                    new UsedConstrs(usedConstructors, strategyAnalysisDatum.lastModified)
                        .visit(strategyAnalysisDatum.analyzedAst);
                }
            }
        }

        @Override public boolean equals(Object o) {
            if(this == o)
                return true;
            if(o == null || getClass() != o.getClass())
                return false;
            if(!super.equals(o))
                return false;

            DynamicRuleInput that = (DynamicRuleInput) o;

            return checkTask.equals(that.checkTask);
        }

        @Override public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + checkTask.hashCode();
            return result;
        }

        @Override public String toString() {
            return "Back.DynamicRuleInput(" + strategySignature.cifiedName() + ")";
        }
    }

    public static class BoilerplateInput extends Input {
        public BoilerplateInput(STask<GlobalData> resolveTask, ResourcePath outputDir,
            @Nullable String packageName, @Nullable ResourcePath cacheDir, List<String> constants,
            Collection<ResourcePath> includeDirs, Arguments extraArgs) {
            super(outputDir, packageName, cacheDir, constants, includeDirs, extraArgs, resolveTask);
        }

        @Override public String toString() {
            return "Back.BoilerplateInput";
        }
    }

    public static class CongruenceInput extends Input {
        public CongruenceInput(STask<GlobalData> resolveTask, ResourcePath outputDir,
            @Nullable String packageName, @Nullable ResourcePath cacheDir, List<String> constants,
            Collection<ResourcePath> includeDirs, Arguments extraArgs) {
            super(outputDir, packageName, cacheDir, constants, includeDirs, extraArgs, resolveTask);
        }

        @Override public String toString() {
            return "Back.CongruenceInput";
        }
    }

    public static class Output implements Serializable {
        public final Set<ResourcePath> resultFiles;
        public final Collection<? extends StrategySignature> compiledStrategies;

        public Output(Set<ResourcePath> resultFiles,
            Collection<? extends StrategySignature> compiledStrategies) {
            this.resultFiles = resultFiles;
            this.compiledStrategies = compiledStrategies;
        }

        @Override public boolean equals(Object o) {
            if(this == o)
                return true;
            if(o == null || getClass() != o.getClass())
                return false;

            Output output = (Output) o;

            if(!resultFiles.equals(output.resultFiles))
                return false;
            return compiledStrategies.equals(output.compiledStrategies);
        }

        @Override public int hashCode() {
            int result = resultFiles.hashCode();
            result = 31 * result + compiledStrategies.hashCode();
            return result;
        }

        @Override public String toString() {
            return "Back.Output(" + resultFiles + ")";
        }
    }

    private final IOAgentTrackerFactory ioAgentTrackerFactory;
    private final StrIncrContext strContext;
    private final StrategyStubs strategyStubs;
    private final ITermFactory termFactory;
    private final ResourcePathConverter resourcePathConverter;
    private final CheckModule checkModule;
    private final Front front;

    @Inject public Back(IOAgentTrackerFactory ioAgentTrackerFactory, StrIncrContext strContext,
        StrategyStubs strategyStubs, ResourcePathConverter resourcePathConverter,
        CheckModule checkModule, Front front) {
        this.ioAgentTrackerFactory = ioAgentTrackerFactory;
        this.strContext = strContext;
        this.termFactory = strContext.getFactory();
        this.strategyStubs = strategyStubs;
        this.resourcePathConverter = resourcePathConverter;
        this.checkModule = checkModule;
        this.front = front;
    }

    @Override public Output exec(ExecContext context, Input input) throws Exception {
        final Set<StrategySignature> compiledStrategies = new HashSet<>();
        final boolean isBoilerplate = input instanceof BoilerplateInput;
        final IStrategoTerm ctree;
        if(isBoilerplate) {
            final GlobalIndex globalIndex = PieUtils
                .requirePartial(context, input.resolveTask, GlobalData.ToGlobalIndex.INSTANCE);
            final Set<ConstructorSignature> constructors = new HashSet<>(globalIndex.constructors);
            addDrConstructors(constructors);
            constructors.add(new ConstructorSignature("Anno_Cong__", 2, 0));
            final Set<StrategySignature> strategies =
                new HashSet<>(globalIndex.nonExternalStrategies);
            for(ConstructorSignature constructor : constructors) {
                strategies.add(constructor.toCongruenceSig());
            }
            ctree = Packer
                .packBoilerplate(termFactory, constructors, strategyStubs.declStubs(strategies));
        } else if(input instanceof CongruenceInput) {
            // TODO: run congruence task per module or even per constructor?
            final GlobalIndex globalIndex = PieUtils
                .requirePartial(context, input.resolveTask, GlobalData.ToGlobalIndex.INSTANCE);
            final Set<ConstructorSignature> constructors = new HashSet<>(globalIndex.constructors);
            addDrConstructors(constructors);
            final List<IStrategoAppl> congruences = new ArrayList<>();
            for(ConstructorSignature constructor : constructors) {
                if(globalIndex.nonExternalStrategies.contains(constructor.toCongruenceSig())) {
                    context.logger().debug(
                        "Skipping congruence overlapping with existing strategy: " + constructor);
                    continue;
                }
                compiledStrategies.add(constructor.toCongruenceSig());
                congruences.add(constructor.congruenceAst(termFactory));
            }
            congruences.add(ConstructorSignature.annoCongAst(termFactory));
            if(!globalIndex.dynamicRules.isEmpty()) {
                congruences.add(dynamicCallsDefinition(termFactory, globalIndex.dynamicRules));
            }
            compiledStrategies.add(new StrategySignature("Anno_Cong__", 2, 0));
            ctree = Packer.packStrategy(termFactory, Collections.emptyList(), congruences);
        } else {
            final List<IStrategoAppl> strategyContributions = new ArrayList<>();
            final Set<ConstructorSignature> usedConstructors = new HashSet<>();
            final NormalInput normalInput = (NormalInput) input;
            normalInput.getStrategyContributions(context, checkModule, strategyContributions,
                usedConstructors);

            final Set<ModuleIdentifier> modulesDefiningOverlay = PieUtils
                .requirePartial(context, input.resolveTask,
                    new GlobalData.ModulesDefiningOverlays<>(usedConstructors));

            final List<IStrategoAppl> overlayContributions = new ArrayList<>();
            for(ModuleIdentifier moduleIdentifier : modulesDefiningOverlay) {
                final List<OverlayData> overlayData = PieUtils.requirePartial(context, front,
                    new Front.Input(moduleIdentifier, normalInput.moduleImportService),
                    new ModuleData.ToOverlays<>(usedConstructors));
                for(OverlayData overlayDatum : overlayData) {
                    overlayContributions.add(overlayDatum.astTerm);
                }
            }

            IStrategoTerm desugaringInput =
                Packer.packStrategy(termFactory, overlayContributions, strategyContributions);

            final StrategoExecutor.ExecutionResult result = StrategoExecutor
                .runLocallyUniqueStringStrategy(ioAgentTrackerFactory, context.logger(), true,
                    compile_top_level_def_0_0.instance, desugaringInput, strContext);

            if(!result.success) {
                throw new ExecException(
                    "Call to compile-top-level-def failed:\n" + result.exception, null);
            }
            assert result.result != null;

            ctree = result.result;

            final Map<StrategySignature, Set<StrategyAnalysisData>> strategySignatureSetMap =
                CheckModule.extractStrategyDefs(null, 0L, ctree, Collections.emptyMap());
            compiledStrategies.addAll(strategySignatureSetMap.keySet());
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
                resultFiles.add(
                    context.getResourceService().getResourcePath(ResourceKeyString.of(fileName)));
                context.provide(new File(fileName));
            }
        }

        return new Output(resultFiles, compiledStrategies);
    }

    private IStrategoAppl dynamicCallsDefinition(ITermFactory tf,
        Collection<StrategySignature> dynamicRules) {
        /* concrete syntax:
         *   DYNAMIC_CALLS = new-[dr-rule-name](|"", "")
         * abstract syntax, desugared and name mangled:
         *   SDefT("$D$Y$N$A$M$I$C__$C$A$L$L$S_0_0", [], [], CallT("new_[dr-rule-name]_0_2", [], [NoAnnoList(Str("\"\"")), NoAnnoList(Str("\"\""))]))
         * strung together with `[call] <+ [other-calls]` or `GuardedLChoice([call], Id(), [other-calls])`
         */
        @Nullable IStrategoAppl body = null;
        final IStrategoAppl id = tf.makeAppl("Id");
        final IStrategoAppl emptyStringLit =
            tf.makeAppl("NoAnnoList", tf.makeAppl("Str", tf.makeString("\"\"")));

        for(StrategySignature dynamicRule : dynamicRules) {
            final String drRuleNameNew =
                CommonPaths.capitalsForDollars(Interpreter.cify("new-" + dynamicRule.name))
                    + "_0_2";
            final IStrategoAppl call =
                tf.makeAppl("CallT", tf.makeAppl("SVar", tf.makeString(drRuleNameNew)),
                    tf.makeList(), tf.makeList(emptyStringLit, emptyStringLit));
            if(body == null) {
                body = call;
            } else {
                body = tf.makeAppl("GuardedLChoice", call, id, body);
            }
        }

        final String dynamicCalls =
            CommonPaths.capitalsForDollars(Interpreter.cify("DYNAMIC_CALLS")) + "_0_0";
        return tf
            .makeAppl("SDefT", tf.makeString(dynamicCalls), tf.makeList(), tf.makeList(), body);
    }

    private static void addDrConstructors(Set<ConstructorSignature> constructors) {
        constructors.add(new ConstructorSignature("DR_DUMMY", 0, 0));
        constructors.add(new ConstructorSignature("DR_UNDEFINE", 1, 0));
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
