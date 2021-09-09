package mb.stratego.build.strincr.task.input;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.Nullable;

import org.metaborg.util.cmd.Arguments;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.STask;
import mb.pie.api.STaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.stratego.build.strincr.IModuleImportService;
import mb.stratego.build.strincr.data.ConstructorData;
import mb.stratego.build.strincr.data.ConstructorSignature;
import mb.stratego.build.strincr.data.ConstructorType;
import mb.stratego.build.strincr.data.OverlayData;
import mb.stratego.build.strincr.data.StrategyAnalysisData;
import mb.stratego.build.strincr.data.StrategySignature;
import mb.stratego.build.strincr.function.GetConstrData;
import mb.stratego.build.strincr.function.GetDynamicRuleAnalysisData;
import mb.stratego.build.strincr.function.GetOverlayData;
import mb.stratego.build.strincr.function.GetStrategyAnalysisData;
import mb.stratego.build.strincr.function.ModulesDefiningDynamicRule;
import mb.stratego.build.strincr.function.ModulesDefiningOverlays;
import mb.stratego.build.strincr.function.ModulesDefiningStrategy;
import mb.stratego.build.strincr.function.ToCongruenceGlobalIndex;
import mb.stratego.build.strincr.function.ToGlobalConsInj;
import mb.stratego.build.strincr.function.output.CongruenceGlobalIndex;
import mb.stratego.build.strincr.function.output.GlobalConsInj;
import mb.stratego.build.strincr.task.Back;
import mb.stratego.build.strincr.task.CheckModule;
import mb.stratego.build.strincr.task.output.BackOutput;
import mb.stratego.build.strincr.task.output.CheckModuleOutput;
import mb.stratego.build.termvisitors.UsedConstrs;
import mb.stratego.build.util.PieUtils;

public abstract class BackInput implements Serializable {
    public final ResourcePath outputDir;
    public final String packageName;
    public final @Nullable ResourcePath cacheDir;
    public final ArrayList<String> constants;
    public final Arguments extraArgs;
    public final CheckInput checkInput;
    public final boolean usingLegacyStrategoStdLib;

    public BackInput(ResourcePath outputDir, String packageName,
        @Nullable ResourcePath cacheDir, ArrayList<String> constants, Arguments extraArgs,
        CheckInput checkInput, boolean usingLegacyStrategoStdLib) {
        this.outputDir = outputDir;
        this.packageName = packageName;
        this.cacheDir = cacheDir;
        this.constants = constants;
        this.extraArgs = extraArgs;
        this.checkInput = checkInput;
        this.usingLegacyStrategoStdLib = usingLegacyStrategoStdLib;
    }

    public abstract CTreeBuildResult buildCTree(ExecContext context, Back backTask,
        Collection<StrategySignature> compiledStrategies) throws ExecException;

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        BackInput input = (BackInput) o;

        if(!outputDir.equals(input.outputDir))
            return false;
        if(!packageName.equals(input.packageName))
            return false;
        if(!Objects.equals(cacheDir, input.cacheDir))
            return false;
        if(!constants.equals(input.constants))
            return false;
        if(!extraArgs.equals(input.extraArgs))
            return false;
        if(!checkInput.equals(input.checkInput))
            return false;
        return usingLegacyStrategoStdLib == input.usingLegacyStrategoStdLib;
    }

    @Override public int hashCode() {
        int result = outputDir.hashCode();
        result = 31 * result + packageName.hashCode();
        result = 31 * result + (cacheDir != null ? cacheDir.hashCode() : 0);
        result = 31 * result + constants.hashCode();
        result = 31 * result + extraArgs.hashCode();
        result = 31 * result + checkInput.hashCode();
        result = 31 * result + (usingLegacyStrategoStdLib ? 1 : 0);
        return result;
    }

    @Override public abstract String toString();

    public Key key() {
        return new Key(this);
    }

    public static class Key implements Serializable {
        public final Class<? extends BackInput> aClass;
        public final ResourcePath outputDir;
        public final @Nullable Serializable anythingElse;

        public Key(BackInput backInput, Serializable anythingElse) {
            this.aClass = backInput.getClass();
            this.outputDir = backInput.outputDir;
            this.anythingElse = anythingElse;
        }

        public Key(BackInput backInput) {
            this.aClass = backInput.getClass();
            this.outputDir = backInput.outputDir;
            this.anythingElse = null;
        }

        @Override public boolean equals(Object o) {
            if(this == o)
                return true;
            if(o == null || getClass() != o.getClass())
                return false;

            Key key = (Key) o;

            if(!aClass.equals(key.aClass))
                return false;
            if(!outputDir.equals(key.outputDir))
                return false;
            return Objects.equals(anythingElse, key.anythingElse);
        }

        @Override public int hashCode() {
            int result = aClass.hashCode();
            result = 31 * result + outputDir.hashCode();
            result = 31 * result + (anythingElse != null ? anythingElse.hashCode() : 0);
            return result;
        }

        @Override public String toString() {
            if(anythingElse == null) {
                return "BackInput." + aClass.getSimpleName() + "(" + outputDir + ")";
            } else {
                return "BackInput." + aClass.getSimpleName() + "(" + outputDir + ", " + anythingElse
                    + ")";
            }
        }
    }

    public static abstract class CTreeBuildResult {
        public @Nullable IStrategoTerm result() {
            return null;
        }

        public @Nullable STask<BackOutput> generatingTask() {
            return null;
        }

        public static CTreeBuildResult withResult(IStrategoTerm result) {
            return new CTreeBuildResult() {
                public IStrategoTerm result() {
                    return result;
                }
            };
        }

        public static CTreeBuildResult withGeneratingTask(STask<BackOutput> generatingTask) {
            return new CTreeBuildResult() {
                public STask<BackOutput> generatingTask() {
                    return generatingTask;
                }
            };
        }
    }

    public static class Normal extends BackInput {
        public final StrategySignature strategySignature;
        public final STaskDef<CheckModuleInput, CheckModuleOutput> strategyAnalysisDataTask;

        public Normal(ResourcePath outputDir, String packageName,
            @Nullable ResourcePath cacheDir, ArrayList<String> constants, Arguments extraArgs,
            CheckInput checkInput, StrategySignature strategySignature,
            STaskDef<CheckModuleInput, CheckModuleOutput> strategyAnalysisDataTask,
            boolean legacyStrategoStdLib) {
            super(outputDir, packageName, cacheDir, constants, extraArgs, checkInput,
                legacyStrategoStdLib);
            this.strategySignature = strategySignature;
            this.strategyAnalysisDataTask = strategyAnalysisDataTask;
        }

        @Override public CTreeBuildResult buildCTree(ExecContext context, Back backTask,
            Collection<StrategySignature> compiledStrategies) throws ExecException {
            final ArrayList<IStrategoAppl> strategyContributions = new ArrayList<>();
            final HashSet<ConstructorSignature> usedConstructors = new HashSet<>();
            final @Nullable STask<BackOutput> generatingTask =
                getStrategyContributions(context, backTask, strategyContributions, usedConstructors);
            if(generatingTask != null) {
                return CTreeBuildResult.withGeneratingTask(generatingTask);
            }

            final HashSet<IModuleImportService.ModuleIdentifier> modulesDefiningOverlay = PieUtils
                .requirePartial(context, backTask.resolve, checkInput.resolveInput(),
                    new ModulesDefiningOverlays(usedConstructors));

            final ArrayList<IStrategoAppl> overlayContributions = new ArrayList<>();
            for(IModuleImportService.ModuleIdentifier moduleIdentifier : modulesDefiningOverlay) {
                final HashSet<ConstructorSignature> newlyFoundConstructors = new HashSet<>(usedConstructors);
                // Overlays can use other overlays, so this loop is for finding those transitive uses
                while(!newlyFoundConstructors.isEmpty()) {
                    final ArrayList<OverlayData> overlayData = PieUtils
                        .requirePartial(context, backTask.front,
                            new FrontInput.Normal(moduleIdentifier, checkInput.importResolutionInfo,
                                checkInput.autoImportStd),
                            new GetOverlayData(newlyFoundConstructors));
                    usedConstructors.addAll(newlyFoundConstructors);
                    newlyFoundConstructors.clear();
                    for(OverlayData overlayDatum : overlayData) {
                        overlayContributions.add(overlayDatum.astTerm);
                        for(ConstructorSignature usedConstructor : overlayDatum.usedConstructors) {
                            if(!usedConstructors.contains(usedConstructor)) {
                                newlyFoundConstructors.add(usedConstructor);
                            }
                        }
                    }
                }
            }

            IStrategoTerm desugaringInput =
                backTask.generateStratego
                    .packStrategy(overlayContributions, strategyContributions);

            final String projectPath =
                backTask.resourcePathConverter.toString(checkInput.projectPath);
            final IStrategoTerm result =
                backTask.strategoLanguage.desugar(desugaringInput, projectPath);

            final Set<StrategySignature> cifiedStrategySignatures =
                CheckModule.extractStrategyDefs(null, result, null).keySet();
            for(StrategySignature cified : cifiedStrategySignatures) {
                final @Nullable StrategySignature uncified =
                    StrategySignature.fromCified(cified.name);
                if(uncified != null) {
                    // Hack to work around lossy property of cified names where both "--" and "_"
                    //   are translated to "__", and the reverse always translates to "_".
                    // TODO: adapt desugar stratego code to explicitly give back compiled strategies
                    //   names are cified.
                    if(uncified.name.contains("_")) {
                        compiledStrategies.add(
                            new StrategySignature(uncified.name.replace("_", "--"),
                                uncified.noStrategyArgs, uncified.noTermArgs));
                    }
                    compiledStrategies.add(uncified);
                }
            }

            return CTreeBuildResult.withResult(result);
        }

        /**
         * @return null if the contributions and used constructors were added to the parameters, or
         *      the task that actually generates the outputs.
         */
        public @Nullable STask<BackOutput> getStrategyContributions(ExecContext context, Back backTask,
            ArrayList<IStrategoAppl> strategyContributions,
            HashSet<ConstructorSignature> usedConstructors) {
            final StrategySignature strategySignature = this.strategySignature;
            final HashSet<IModuleImportService.ModuleIdentifier> modulesDefiningStrategy = PieUtils
                .requirePartial(context, backTask.resolve, checkInput.resolveInput(),
                    new ModulesDefiningStrategy(strategySignature));

            for(IModuleImportService.ModuleIdentifier moduleIdentifier : modulesDefiningStrategy) {
                final Set<StrategyAnalysisData> strategyAnalysisData = PieUtils
                    .requirePartial(context, strategyAnalysisDataTask,
                        checkInput.checkModuleInput(moduleIdentifier),
                        new GetStrategyAnalysisData(strategySignature));
                for(StrategyAnalysisData strategyAnalysisDatum : strategyAnalysisData) {
                    if(!strategyAnalysisDatum.definedDynamicRules.isEmpty()) {
                        strategyContributions.clear();
                        usedConstructors.clear();
                        return backTask.createSupplier(
                            dynamicRuleInput(strategyAnalysisDatum.definedDynamicRules.first()));
                    }
                    strategyContributions.add(strategyAnalysisDatum.analyzedAst);
                    new UsedConstrs(usedConstructors).visit(strategyAnalysisDatum.analyzedAst);
                }
            }
            return null;
        }

        protected DynamicRule dynamicRuleInput(StrategySignature firstSig) {
            return new DynamicRule(outputDir, packageName, cacheDir, constants, extraArgs,
                checkInput, firstSig, strategyAnalysisDataTask, usingLegacyStrategoStdLib);
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
            return strategyAnalysisDataTask.equals(normal.strategyAnalysisDataTask);
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

        @Override public Key key() {
            return new Key(this, strategySignature);
        }
    }

    public static class DynamicRule extends Normal {
        public DynamicRule(ResourcePath outputDir, String packageName,
            @Nullable ResourcePath cacheDir, ArrayList<String> constants, Arguments extraArgs,
            CheckInput checkInput, StrategySignature strategySignature,
            STaskDef<CheckModuleInput, CheckModuleOutput> strFileGeneratingTasks,
            boolean legacyStrategoStdLib) {
            super(outputDir, packageName, cacheDir, constants, extraArgs, checkInput,
                strategySignature, strFileGeneratingTasks, legacyStrategoStdLib);
        }

        /**
         * @return null if the contributions and used constructors were added to the parameters, or
         *      the task that actually generates the outputs.
         */
        @Override public @Nullable STask<BackOutput> getStrategyContributions(ExecContext context, Back backTask,
            ArrayList<IStrategoAppl> strategyContributions,
            HashSet<ConstructorSignature> usedConstructors) {
            final Queue<StrategySignature> workList = new ArrayDeque<>();
            workList.add(strategySignature);
            final TreeSet<StrategySignature> seen = new TreeSet<>();
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
                        .requirePartial(context, strategyAnalysisDataTask,
                            checkInput.checkModuleInput(moduleIdentifier),
                            new GetDynamicRuleAnalysisData(strategySignature));
                    for(StrategyAnalysisData strategyAnalysisDatum : strategyAnalysisData) {
                        strategyContributions.add(strategyAnalysisDatum.analyzedAst);
                        new UsedConstrs(usedConstructors).visit(strategyAnalysisDatum.analyzedAst);
                        for(StrategySignature definedDynamicRule : strategyAnalysisDatum.definedDynamicRules) {
                            if(!seen.contains(definedDynamicRule)) {
                                workList.add(definedDynamicRule);
                                seen.add(definedDynamicRule);
                            }
                        }
                    }
                }
            }
            // Important test: only the BackInput.DynamicRule task with the "smallest" signature is
            // allowed to compile the set of strategies found through the above process. The Compile
            // tasks is smart and starts with the smallest signature for a dynamic rule, but there
            // maybe be BackInput.DynamicRule tasks from previous runs around that are no longer
            // valid.
            final StrategySignature firstSig = seen.first();
            final boolean taskIsStillRelevant = firstSig.equals(strategySignature);
            if(taskIsStillRelevant) {
                return null;
            } else {
                strategyContributions.clear();
                usedConstructors.clear();
                return backTask.createSupplier(dynamicRuleInput(firstSig));
            }
        }

        public Set<String> getStrategySignatures(ITermFactory tf) {
            final Set<String> strategySignatures = new HashSet<>();
            for(StrategySignature iStrategoTerms : strategySignature.dynamicRuleSignatures(tf)
                .keySet()) {
                String name = iStrategoTerms.cifiedName();
                strategySignatures.add(name);
            }
            return strategySignatures;
        }

        @Override public String toString() {
            return "Back.DynamicRuleInput(" + strategySignature.cifiedName() + ")";
        }
    }

    public static class Congruence extends BackInput {
        public final HashSet<String> dynamicRuleNewGenerated;
        public final HashSet<String> dynamicRuleUndefineGenerated;

        public Congruence(ResourcePath outputDir, String packageName,
            @Nullable ResourcePath cacheDir, ArrayList<String> constants, Arguments extraArgs,
            CheckInput checkInput, HashSet<String> dynamicRuleNewGenerated,
            HashSet<String> dynamicRuleUndefineGenerated, boolean legacyStrategoStdLib) {
            super(outputDir, packageName, cacheDir, constants, extraArgs, checkInput,
                legacyStrategoStdLib);
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

        @Override public CTreeBuildResult buildCTree(ExecContext context, Back backTask,
            Collection<StrategySignature> compiledStrategies) throws ExecException {
            final CongruenceGlobalIndex globalIndex = PieUtils
                .requirePartial(context, backTask.resolve, checkInput.resolveInput(),
                    ToCongruenceGlobalIndex.INSTANCE);
            final ArrayList<ConstructorSignature> constructors =
                new ArrayList<>(globalIndex.nonExternalConstructors.size() + 2);
            constructors.addAll(globalIndex.nonExternalConstructors);
            if(usingLegacyStrategoStdLib) {
                constructors.add(backTask.generateStratego.dr_dummy);
                constructors.add(backTask.generateStratego.dr_undefine);
            }

            final String projectPath =
                backTask.resourcePathConverter.toString(checkInput.projectPath);

            final ArrayList<IStrategoAppl> congruences = new ArrayList<>(constructors.size() + 2);
            for(ConstructorSignature constructor : constructors) {
                final StrategySignature congruenceSig = constructor.toCongruenceSig();
                if(globalIndex.nonExternalStrategies.contains(congruenceSig)) {
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
                compiledStrategies.add(congruenceSig);
                congruences.add(backTask.strategoLanguage.toCongruenceAst(constructor, projectPath));
            }
            ArrayList<IStrategoAppl> overlayContributions = new ArrayList<>(globalIndex.overlayData.size());
            for(OverlayData overlayData : globalIndex.overlayData) {
                final StrategySignature congruenceSig = overlayData.signature.toCongruenceSig();
                if(globalIndex.nonExternalStrategies.contains(congruenceSig)) {
                    context.logger().debug(
                        "Skipping congruence overlapping with existing strategy: "
                            + overlayData.signature);
                    continue;
                }
                if(globalIndex.externalConstructors.contains(overlayData.signature)) {
                    context.logger().debug(
                        "Skipping congruence of constructor overlapping with external constructor: "
                            + overlayData.signature);
                    continue;
                }
                compiledStrategies.add(congruenceSig);
                overlayContributions.add(overlayData.astTerm);
            }
            congruences.addAll(backTask.strategoLanguage.toCongruenceAsts(overlayContributions, projectPath));
            if(usingLegacyStrategoStdLib) {
                congruences.add(backTask.generateStratego.anno_cong__ast);
                compiledStrategies.add(new StrategySignature("Anno_Cong__", 2, 0));
            }

            final @Nullable IStrategoAppl dynamicCallsDefinition = backTask.generateStratego
                .dynamicCallsDefinition(dynamicRuleNewGenerated, dynamicRuleUndefineGenerated);
            if(dynamicCallsDefinition != null) {
                congruences.add(dynamicCallsDefinition);
                compiledStrategies.add(new StrategySignature("DYNAMIC_CALLS", 0, 0));
            }

            return CTreeBuildResult
                .withResult(backTask.generateStratego.packStrategies(congruences));
        }

        @Override public String toString() {
            return "Back.CongruenceInput";
        }
    }

    public static class Boilerplate extends BackInput {
        public final boolean dynamicCallsDefined;
        public final boolean library;
        public final String libraryName;

        public Boilerplate(ResourcePath outputDir, String packageName,
            @Nullable ResourcePath cacheDir, ArrayList<String> constants, Arguments extraArgs,
            CheckInput checkInput, boolean dynamicCallsDefined, boolean library,
            boolean legacyStrategoStdLib, String libraryName) {
            super(outputDir, packageName, cacheDir, constants, extraArgs, checkInput,
                legacyStrategoStdLib);
            this.dynamicCallsDefined = dynamicCallsDefined;
            this.library = library;
            this.libraryName = libraryName;
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

        @Override public CTreeBuildResult buildCTree(ExecContext context, Back backTask,
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
                        new FrontInput.Normal(moduleIdentifier, checkInput.importResolutionInfo,
                            checkInput.autoImportStd), GetConstrData.INSTANCE);
                for(ConstructorData constructorDatum : constructorData) {
                    consInjTerms.add(constructorDatum.toTerm(backTask.tf));
                    constructors.add(constructorDatum.signature);
                }
            }
            if(usingLegacyStrategoStdLib) {
                consInjTerms.add(backTask.generateStratego.dr_dummyTerm);
                consInjTerms.add(backTask.generateStratego.dr_undefineTerm);
                consInjTerms.add(backTask.generateStratego.anno_cong__Term);
                constructors.add(backTask.generateStratego.dr_dummy);
                constructors.add(backTask.generateStratego.dr_undefine);
                constructors.add(backTask.generateStratego.anno_cong__);
            }
            for(Map.Entry<IStrategoTerm, ArrayList<IStrategoTerm>> e : globalConsInj.nonExternalInjections
                .entrySet()) {
                final IStrategoTerm from = e.getKey();
                for(IStrategoTerm to : e.getValue()) {
                    consInjTerms.add(backTask.tf.makeAppl("OpDeclInj", backTask.tf.makeAppl("FunType",
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
            return CTreeBuildResult.withResult(backTask.generateStratego
                .packBoilerplate(consInjTerms, backTask.generateStratego.declStubs(strategies)));
        }

        public ResourcePath str2LibFile() {
            return outputDir.appendAsRelativePath(libraryName + ".str2lib");
        }

        @Override public String toString() {
            return "Back.BoilerplateInput";
        }
    }
}
