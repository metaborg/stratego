package mb.stratego.build.strincr.task.input;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import javax.annotation.Nullable;

import org.metaborg.util.cmd.Arguments;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.strc.compile_top_level_def_0_0;

import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.STaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.stratego.build.strincr.IModuleImportService;
import mb.stratego.build.strincr.data.ConstructorData;
import mb.stratego.build.strincr.data.ConstructorSignature;
import mb.stratego.build.strincr.data.ConstructorType;
import mb.stratego.build.strincr.data.OverlayData;
import mb.stratego.build.strincr.data.StrategyAnalysisData;
import mb.stratego.build.strincr.data.StrategySignature;
import mb.stratego.build.strincr.function.GetDynamicRuleAnalysisData;
import mb.stratego.build.strincr.function.GetStrategyAnalysisData;
import mb.stratego.build.strincr.function.ModulesDefiningDynamicRule;
import mb.stratego.build.strincr.function.ModulesDefiningOverlays;
import mb.stratego.build.strincr.function.ModulesDefiningStrategy;
import mb.stratego.build.strincr.function.ToConstrData;
import mb.stratego.build.strincr.function.ToGlobalConsInj;
import mb.stratego.build.strincr.function.ToGlobalIndex;
import mb.stratego.build.strincr.function.ToOverlays;
import mb.stratego.build.strincr.function.output.GlobalConsInj;
import mb.stratego.build.strincr.function.output.GlobalIndex;
import mb.stratego.build.strincr.task.Back;
import mb.stratego.build.strincr.task.CheckModule;
import mb.stratego.build.strincr.task.output.CheckModuleOutput;
import mb.stratego.build.termvisitors.UsedConstrs;
import mb.stratego.build.util.PieUtils;
import mb.stratego.build.util.StrategoExecutor;
import mb.stratego.compiler.pack.Packer;

public abstract class BackInput implements Serializable {
    public final ResourcePath outputDir;
    public final @Nullable String packageName;
    public final @Nullable ResourcePath cacheDir;
    public final ArrayList<String> constants;
    public final Arguments extraArgs;
    public final CheckInput checkInput;

    public BackInput(ResourcePath outputDir, @Nullable String packageName,
        @Nullable ResourcePath cacheDir, ArrayList<String> constants, Arguments extraArgs,
        CheckInput checkInput) {
        this.outputDir = outputDir;
        this.packageName = packageName;
        this.cacheDir = cacheDir;
        this.constants = constants;
        this.extraArgs = extraArgs;
        this.checkInput = checkInput;
    }

    public abstract IStrategoTerm buildCTree(ExecContext context, Back backTask,
        Collection<StrategySignature> compiledStrategies) throws ExecException;

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        BackInput input = (BackInput) o;

        if(!outputDir.equals(input.outputDir))
            return false;
        if(packageName != null ? !packageName.equals(input.packageName) : input.packageName != null)
            return false;
        if(cacheDir != null ? !cacheDir.equals(input.cacheDir) : input.cacheDir != null)
            return false;
        if(!constants.equals(input.constants))
            return false;
        if(!extraArgs.equals(input.extraArgs))
            return false;
        return checkInput.equals(input.checkInput);
    }

    @Override public int hashCode() {
        int result = outputDir.hashCode();
        result = 31 * result + (packageName != null ? packageName.hashCode() : 0);
        result = 31 * result + (cacheDir != null ? cacheDir.hashCode() : 0);
        result = 31 * result + constants.hashCode();
        result = 31 * result + extraArgs.hashCode();
        result = 31 * result + checkInput.hashCode();
        return result;
    }

    @Override public abstract String toString();

    public static class Normal extends BackInput {
        public final StrategySignature strategySignature;
        public final STaskDef<CheckModuleInput, CheckModuleOutput> strategyAnalysisDataTask;

        public Normal(ResourcePath outputDir, @Nullable String packageName,
            @Nullable ResourcePath cacheDir, ArrayList<String> constants, Arguments extraArgs,
            CheckInput checkInput, StrategySignature strategySignature,
            STaskDef<CheckModuleInput, CheckModuleOutput> strategyAnalysisDataTask) {
            super(outputDir, packageName, cacheDir, constants, extraArgs, checkInput);
            this.strategySignature = strategySignature;
            this.strategyAnalysisDataTask = strategyAnalysisDataTask;
        }

        @Override public IStrategoTerm buildCTree(ExecContext context, Back backTask,
            Collection<StrategySignature> compiledStrategies) throws ExecException {
            final ArrayList<IStrategoAppl> strategyContributions = new ArrayList<>();
            final HashSet<ConstructorSignature> usedConstructors = new HashSet<>();
            getStrategyContributions(context, backTask, strategyContributions, usedConstructors);

            final HashSet<IModuleImportService.ModuleIdentifier> modulesDefiningOverlay = PieUtils
                .requirePartial(context, backTask.resolve, checkInput.resolveInput(),
                    new ModulesDefiningOverlays(usedConstructors));

            final ArrayList<IStrategoAppl> overlayContributions = new ArrayList<>();
            for(IModuleImportService.ModuleIdentifier moduleIdentifier : modulesDefiningOverlay) {
                final ArrayList<OverlayData> overlayData = PieUtils
                    .requirePartial(context, backTask.front,
                        new FrontInput.Normal(moduleIdentifier, checkInput.strFileGeneratingTasks,
                            checkInput.includeDirs, checkInput.linkedLibraries),
                        new ToOverlays(usedConstructors));
                for(OverlayData overlayDatum : overlayData) {
                    overlayContributions.add(overlayDatum.astTerm);
                }
            }

            IStrategoTerm desugaringInput =
                Packer.packStrategy(backTask.tf, overlayContributions, strategyContributions);

            final StrategoExecutor.ExecutionResult result = StrategoExecutor
                .runLocallyUniqueStringStrategy(backTask.ioAgentTrackerFactory, context.logger(),
                    true, compile_top_level_def_0_0.instance, desugaringInput, backTask.strContext);

            if(!result.success) {
                throw new ExecException(
                    "Call to compile-top-level-def failed:\n" + result.exception, null);
            }
            assert result.result != null;

            //noinspection ConstantConditions
            final Set<StrategySignature> cifiedStrategySignatures =
                CheckModule.extractStrategyDefs(null, 0L, result.result, null).keySet();
            for(StrategySignature cified : cifiedStrategySignatures) {
                final @Nullable StrategySignature uncified =
                    StrategySignature.fromCified(cified.name);
                if(uncified != null) {
                    compiledStrategies.add(uncified);
                }
            }

            return result.result;
        }

        public void getStrategyContributions(ExecContext context, Back backTask,
            ArrayList<IStrategoAppl> strategyContributions,
            HashSet<ConstructorSignature> usedConstructors) {
            final StrategySignature strategySignature = this.strategySignature;
            final HashSet<IModuleImportService.ModuleIdentifier> modulesDefiningStrategy = PieUtils
                .requirePartial(context, backTask.resolve, checkInput.resolveInput(),
                    new ModulesDefiningStrategy(strategySignature));

            for(IModuleImportService.ModuleIdentifier moduleIdentifier : modulesDefiningStrategy) {
                if(moduleIdentifier.isLibrary()) {
                    continue;
                }
                final Set<StrategyAnalysisData> strategyAnalysisData = PieUtils
                    .requirePartial(context, strategyAnalysisDataTask, new CheckModuleInput(
                            new FrontInput.Normal(moduleIdentifier, checkInput.strFileGeneratingTasks,
                                checkInput.includeDirs, checkInput.linkedLibraries),
                            checkInput.mainModuleIdentifier),
                        new GetStrategyAnalysisData(strategySignature));
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

            Normal normal = (Normal) o;

            if(!strategySignature.equals(normal.strategySignature))
                return false;
            return strategyAnalysisDataTask == normal.strategyAnalysisDataTask;
        }

        @Override public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + strategySignature.hashCode();
            result = 31 * result + strategyAnalysisDataTask.hashCode();
            return result;
        }

        @Override public String toString() {
            return "Back.NormalInput(" + strategySignature.cifiedName() + ")";
        }
    }

    public static class DynamicRule extends Normal {
        public DynamicRule(ResourcePath outputDir, @Nullable String packageName,
            @Nullable ResourcePath cacheDir, ArrayList<String> constants, Arguments extraArgs,
            CheckInput checkInput, StrategySignature strategySignature,
            STaskDef<CheckModuleInput, CheckModuleOutput> strategoGradualSetting) {
            super(outputDir, packageName, cacheDir, constants, extraArgs, checkInput,
                strategySignature, strategoGradualSetting);
        }

        @Override public void getStrategyContributions(ExecContext context, Back backTask,
            ArrayList<IStrategoAppl> strategyContributions,
            HashSet<ConstructorSignature> usedConstructors) {
            final Queue<StrategySignature> workList = new ArrayDeque<>();
            workList.add(strategySignature);
            final HashSet<StrategySignature> seen = new HashSet<>();
            seen.add(strategySignature);
            while(!workList.isEmpty()) {
                StrategySignature strategySignature = workList.remove();
                final HashSet<IModuleImportService.ModuleIdentifier> modulesDefiningStrategy =
                    PieUtils.requirePartial(context, backTask.check, checkInput,
                        new ModulesDefiningDynamicRule(strategySignature));

                for(IModuleImportService.ModuleIdentifier moduleIdentifier : modulesDefiningStrategy) {
                    if(moduleIdentifier.isLibrary()) {
                        continue;
                    }
                    final HashSet<StrategyAnalysisData> strategyAnalysisData = PieUtils
                        .requirePartial(context, strategyAnalysisDataTask, new CheckModuleInput(
                                new FrontInput.Normal(moduleIdentifier,
                                    checkInput.strFileGeneratingTasks, checkInput.includeDirs,
                                    checkInput.linkedLibraries), checkInput.mainModuleIdentifier),
                            new GetDynamicRuleAnalysisData(strategySignature));
                    for(StrategyAnalysisData strategyAnalysisDatum : strategyAnalysisData) {
                        strategyContributions.add(strategyAnalysisDatum.analyzedAst);
                        new UsedConstrs(usedConstructors, strategyAnalysisDatum.lastModified)
                            .visit(strategyAnalysisDatum.analyzedAst);
                        for(StrategySignature definedDynamicRule : strategyAnalysisDatum.definedDynamicRules) {
                            if(!seen.contains(definedDynamicRule)) {
                                workList.add(definedDynamicRule);
                                seen.add(definedDynamicRule);
                            }
                        }
                    }
                }
            }
        }

        @Override public String toString() {
            return "Back.DynamicRuleInput(" + strategySignature.cifiedName() + ")";
        }
    }

    public static class Congruence extends BackInput {
        public final HashSet<String> dynamicRuleNewGenerated;
        public final HashSet<String> dynamicRuleUndefineGenerated;

        public Congruence(ResourcePath outputDir, @Nullable String packageName,
            @Nullable ResourcePath cacheDir, ArrayList<String> constants, Arguments extraArgs,
            CheckInput checkInput, HashSet<String> dynamicRuleNewGenerated,
            HashSet<String> dynamicRuleUndefineGenerated) {
            super(outputDir, packageName, cacheDir, constants, extraArgs, checkInput);
            this.dynamicRuleNewGenerated = dynamicRuleNewGenerated;
            this.dynamicRuleUndefineGenerated = dynamicRuleUndefineGenerated;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o)
                return true;
            if(o == null || getClass() != o.getClass())
                return false;
            if(!super.equals(o))
                return false;

            Congruence that = (Congruence) o;

            if(!dynamicRuleNewGenerated.equals(that.dynamicRuleNewGenerated))
                return false;
            return dynamicRuleUndefineGenerated.equals(that.dynamicRuleUndefineGenerated);
        }

        @Override public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + dynamicRuleNewGenerated.hashCode();
            result = 31 * result + dynamicRuleUndefineGenerated.hashCode();
            return result;
        }

        @Override public IStrategoTerm buildCTree(ExecContext context, Back backTask,
            Collection<StrategySignature> compiledStrategies) {
            // TODO: run congruence task per module or even per constructor?
            final GlobalIndex globalIndex = PieUtils
                .requirePartial(context, backTask.resolve, checkInput.resolveInput(),
                    ToGlobalIndex.INSTANCE);
            final ArrayList<ConstructorSignature> constructors =
                new ArrayList<>(globalIndex.nonExternalConstructors.size() + 2);
            constructors.addAll(globalIndex.nonExternalConstructors);
            constructors.add(backTask.generateStratego.dr_dummy);
            constructors.add(backTask.generateStratego.dr_undefine);

            final ArrayList<IStrategoAppl> congruences = new ArrayList<>(constructors.size() + 2);
            for(ConstructorSignature constructor : constructors) {
                if(globalIndex.nonExternalStrategies.contains(constructor.toCongruenceSig())) {
                    context.logger().debug(
                        "Skipping congruence overlapping with existing strategy: " + constructor);
                    continue;
                }
                if(globalIndex.externalConstructors.contains(constructor)) {
                    context.logger().debug(
                        "Skipping congruence of constructor overlapping with external constructor: "
                            + constructor);
                    continue;
                }
                compiledStrategies.add(constructor.toCongruenceSig());
                congruences.add(constructor.congruenceAst(backTask.tf));
            }
            congruences.add(ConstructorSignature.annoCongAst(backTask.tf));
            compiledStrategies.add(new StrategySignature("Anno_Cong__", 2, 0));

            final @Nullable IStrategoAppl dynamicCallsDefinition = backTask.generateStratego
                .dynamicCallsDefinition(dynamicRuleNewGenerated, dynamicRuleUndefineGenerated);
            if(dynamicCallsDefinition != null) {
                congruences.add(dynamicCallsDefinition);
                compiledStrategies.add(new StrategySignature("DYNAMIC_CALLS", 0, 0));
            }

            return Packer.packStrategy(backTask.tf, new ArrayList<>(0), congruences);
        }

        @Override public String toString() {
            return "Back.CongruenceInput";
        }
    }

    public static class Boilerplate extends BackInput {
        public final boolean dynamicCallsDefined;

        public Boilerplate(ResourcePath outputDir, @Nullable String packageName,
            @Nullable ResourcePath cacheDir, ArrayList<String> constants, Arguments extraArgs,
            CheckInput checkInput, boolean dynamicCallsDefined) {
            super(outputDir, packageName, cacheDir, constants, extraArgs, checkInput);
            this.dynamicCallsDefined = dynamicCallsDefined;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o)
                return true;
            if(o == null || getClass() != o.getClass())
                return false;
            if(!super.equals(o))
                return false;

            Boilerplate that = (Boilerplate) o;

            return dynamicCallsDefined == that.dynamicCallsDefined;
        }

        @Override public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + (dynamicCallsDefined ? 1 : 0);
            return result;
        }

        @Override public IStrategoTerm buildCTree(ExecContext context, Back backTask,
            Collection<StrategySignature> compiledStrategies) {
            final GlobalConsInj globalConsInj = PieUtils
                .requirePartial(context, backTask.resolve, checkInput.resolveInput(),
                    ToGlobalConsInj.INSTANCE);
            final ArrayList<ConstructorSignature> constructors =
                new ArrayList<>(globalConsInj.allModuleIdentifiers.size() + 3);
            final ArrayList<IStrategoTerm> consInjTerms = new ArrayList<>(
                globalConsInj.allModuleIdentifiers.size() + globalConsInj.nonExternalInjections
                    .size() + 3);
            for(IModuleImportService.ModuleIdentifier moduleIdentifier : globalConsInj.allModuleIdentifiers) {
                final ArrayList<ConstructorData> constructorData = PieUtils
                    .requirePartial(context, backTask.front,
                        new FrontInput.Normal(moduleIdentifier, checkInput.strFileGeneratingTasks,
                            checkInput.includeDirs, checkInput.linkedLibraries),
                        ToConstrData.INSTANCE);
                for(ConstructorData constructorDatum : constructorData) {
                    consInjTerms.add(constructorDatum.toTerm(backTask.tf));
                    constructors.add(constructorDatum.signature);
                }
            }
            consInjTerms.add(backTask.generateStratego.dr_dummyTerm);
            consInjTerms.add(backTask.generateStratego.dr_undefineTerm);
            consInjTerms.add(backTask.generateStratego.anno_cong__Term);
            constructors.add(backTask.generateStratego.dr_dummy);
            constructors.add(backTask.generateStratego.dr_undefine);
            constructors.add(backTask.generateStratego.anno_cong__);
            for(Map.Entry<IStrategoTerm, ArrayList<IStrategoTerm>> e : globalConsInj.nonExternalInjections
                .entrySet()) {
                final IStrategoTerm from = e.getKey();
                for(IStrategoTerm to : e.getValue()) {
                    consInjTerms.add(backTask.tf.makeAppl("ConsDeclInj", backTask.tf.makeAppl("FunType",
                        backTask.tf.makeList(ConstructorType.typeToConstType(backTask.tf, from)),
                        ConstructorType.typeToConstType(backTask.tf, to))));
                }
            }
            final HashSet<StrategySignature> strategies =
                new HashSet<>(globalConsInj.nonExternalStrategies);
            for(ConstructorSignature constructor : constructors) {
                strategies.add(constructor.toCongruenceSig());
            }
            if(dynamicCallsDefined) {
                strategies.add(new StrategySignature("DYNAMIC_CALLS", 0, 0));
            }
            return Packer.packBoilerplate(backTask.tf, consInjTerms,
                backTask.generateStratego.declStubs(strategies));
        }

        @Override public String toString() {
            return "Back.BoilerplateInput";
        }
    }
}