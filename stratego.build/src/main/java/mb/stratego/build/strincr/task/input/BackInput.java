package mb.stratego.build.strincr.task.input;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

import jakarta.annotation.Nullable;

import org.metaborg.util.cmd.Arguments;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.STask;
import mb.pie.api.STaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.stratego.build.strincr.IModuleImportService;
import mb.stratego.build.strincr.data.ConstructorData;
import mb.stratego.build.strincr.data.ConstructorSignature;
import mb.stratego.build.strincr.data.ConstructorType;
import mb.stratego.build.strincr.data.StrategyAnalysisData;
import mb.stratego.build.strincr.data.StrategySignature;
import mb.stratego.build.strincr.function.DynamicCallsDefined;
import mb.stratego.build.strincr.function.GetConstrData;
import mb.stratego.build.strincr.function.GetOverlayData;
import mb.stratego.build.strincr.function.GetStrategiesUsingDynamicRule;
import mb.stratego.build.strincr.function.GetStrategyAnalysisData;
import mb.stratego.build.strincr.function.ModulesDefiningDynamicRule;
import mb.stratego.build.strincr.function.ModulesDefiningOverlays;
import mb.stratego.build.strincr.function.ModulesDefiningStrategy;
import mb.stratego.build.strincr.function.ToCongruenceGlobalIndex;
import mb.stratego.build.strincr.function.ToGlobalConsInj;
import mb.stratego.build.strincr.function.output.CongruenceGlobalIndex;
import mb.stratego.build.strincr.function.output.GlobalConsInj;
import mb.stratego.build.strincr.function.output.OverlayData;
import mb.stratego.build.strincr.task.Back;
import mb.stratego.build.strincr.task.output.BackOutput;
import mb.stratego.build.strincr.task.output.CheckModuleOutput;
import mb.stratego.build.strincr.task.output.CompileDynamicRulesOutput;
import mb.stratego.build.termvisitors.UsedConstrs;
import mb.stratego.build.util.PieUtils;

public abstract class BackInput implements Serializable {
    public final ResourcePath outputDir;
    public final ArrayList<String> packageNames;
    public final @Nullable ResourcePath cacheDir;
    public final ArrayList<String> constants;
    public final Arguments extraArgs;
    public final CheckInput checkInput;
    public final boolean usingLegacyStrategoStdLib;
    protected final int hashCode;

    public BackInput(ResourcePath outputDir, ArrayList<String> packageNames,
        @Nullable ResourcePath cacheDir, ArrayList<String> constants, Arguments extraArgs,
        CheckInput checkInput, boolean usingLegacyStrategoStdLib) {
        this.outputDir = outputDir;
        this.packageNames = packageNames;
        this.cacheDir = cacheDir;
        this.constants = constants;
        this.extraArgs = extraArgs;
        this.checkInput = checkInput;
        this.usingLegacyStrategoStdLib = usingLegacyStrategoStdLib;
        this.hashCode = hashFunction();
    }

    public abstract CTreeBuildResult buildCTree(ExecContext context, Back backTask,
        Collection<StrategySignature> compiledStrategies) throws ExecException;

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        BackInput input = (BackInput) o;

        if(hashCode != input.hashCode)
            return false;
        if(usingLegacyStrategoStdLib != input.usingLegacyStrategoStdLib)
            return false;
        if(!outputDir.equals(input.outputDir))
            return false;
        if(!packageNames.equals(input.packageNames))
            return false;
        if(!Objects.equals(cacheDir, input.cacheDir))
            return false;
        if(!constants.equals(input.constants))
            return false;
        if(!extraArgs.equals(input.extraArgs))
            return false;
        return checkInput.equals(input.checkInput);
    }

    @Override public int hashCode() {
        return hashCode;
    }

    protected int hashFunction() {
        int result = outputDir.hashCode();
        result = 31 * result + packageNames.hashCode();
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

    /**
     * We use this key for Back inputs so a Back task only has a different identity based on things
     * that influence where files are output (to avoid overlapping provider problems from old tasks
     * during bottom-up builds). These influential things are: the kind of Back task it is (Class),
     * the output directory, and for Normal/DynamicRule inputs the strategy they are compiling.
     */
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
                //@formatter:off
                return "BackInput.Key@" + System.identityHashCode(this) + '{'
                    + "aClass=" + aClass
                    + ", outputDir=" + outputDir
                    + '}';
                //@formatter:on
            }
            //@formatter:off
            return "BackInput.Key@" + System.identityHashCode(this) + '{'
                + "aClass=" + aClass
                + ", outputDir=" + outputDir
                + ", anythingElse=" + anythingElse
                + '}';
            //@formatter:on
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

        public Normal(ResourcePath outputDir, ArrayList<String> packageNames,
            @Nullable ResourcePath cacheDir, ArrayList<String> constants, Arguments extraArgs,
            CheckInput checkInput, StrategySignature strategySignature,
            STaskDef<CheckModuleInput, CheckModuleOutput> strategyAnalysisDataTask,
            boolean legacyStrategoStdLib) {
            super(outputDir, packageNames, cacheDir, constants, extraArgs, checkInput,
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

            final ArrayList<IStrategoTerm> overlayContributions = new ArrayList<>();
            for(IModuleImportService.ModuleIdentifier moduleIdentifier : modulesDefiningOverlay) {
                final HashSet<ConstructorSignature> newlyFoundConstructors = new HashSet<>(usedConstructors);
                // Overlays can use other overlays, so this loop is for finding those transitive uses
                while(!newlyFoundConstructors.isEmpty()) {
                    final OverlayData overlayData = PieUtils.requirePartial(
                        context, backTask.front, new FrontInput.Normal(moduleIdentifier,
                            checkInput.importResolutionInfo, checkInput.autoImportStd),
                        new GetOverlayData(new LinkedHashSet<>(newlyFoundConstructors)));
                    usedConstructors.addAll(newlyFoundConstructors);
                    newlyFoundConstructors.clear();
                    overlayContributions.addAll(overlayData.constrAsts);
                    for(ConstructorSignature usedConstructor : overlayData.usedConstructors) {
                        if(!usedConstructors.contains(usedConstructor)) {
                            newlyFoundConstructors.add(usedConstructor);
                        }
                    }
                }
            }

            final IStrategoTerm desugaringInput =
                backTask.generateStratego.packStrategy(overlayContributions, strategyContributions);

            final String projectPath =
                backTask.resourcePathConverter.toString(checkInput.projectPath);
            final IStrategoTerm result =
                backTask.strategoLanguage.desugar(desugaringInput, projectPath);
            final IStrategoTerm desugaredAst = result.getSubterm(0);
            final IStrategoTerm strategySigTerms = result.getSubterm(1);

            for(IStrategoTerm strategySigTerm : strategySigTerms) {
                final @Nullable StrategySignature signature = StrategySignature.fromTuple(strategySigTerm);
                assert signature != null : "";
                compiledStrategies.add(signature);
            }

            return CTreeBuildResult.withResult(desugaredAst);
        }

        /**
         * @return null if the contributions and used constructors were added to the parameters, or
         * the task that actually generates the files for this strategy.
         */
        public @Nullable STask<BackOutput> getStrategyContributions(ExecContext context, Back backTask,
            ArrayList<IStrategoAppl> strategyContributions,
            HashSet<ConstructorSignature> usedConstructors) {
            final StrategySignature strategySignature = this.strategySignature;
            final HashSet<IModuleImportService.ModuleIdentifier> modulesDefiningStrategy = PieUtils
                .requirePartial(context, backTask.resolve, checkInput.resolveInput(),
                    new ModulesDefiningStrategy(strategySignature));

            final TreeSet<StrategySignature> dynamicRules = new TreeSet<>();
            for(IModuleImportService.ModuleIdentifier moduleIdentifier : modulesDefiningStrategy) {
                final Set<StrategyAnalysisData> strategyAnalysisData = PieUtils
                    .requirePartial(context, strategyAnalysisDataTask,
                        checkInput.checkModuleInput(moduleIdentifier),
                        new GetStrategyAnalysisData(strategySignature));
                for(StrategyAnalysisData strategyAnalysisDatum : strategyAnalysisData) {
                    dynamicRules.addAll(strategyAnalysisDatum.definedDynamicRules);
                    strategyContributions.add(strategyAnalysisDatum.analyzedAst);
                    new UsedConstrs(usedConstructors).visit(strategyAnalysisDatum.analyzedAst);
                }
            }
            // Important test: the BackInput.Normal task only outputs files if the strategy was not
            // swept up in a BackInput.DynamicRule task. If it was, then a dynamic rule definition
            // was in one of the strategy contributions, which we detect here.
            if(!dynamicRules.isEmpty()) {
                strategyContributions.clear();
                usedConstructors.clear();
                return backTask.createSupplier(dynamicRuleInput(dynamicRules.first()));
            }
            return null;
        }

        protected DynamicRule dynamicRuleInput(StrategySignature firstSig) {
            return new DynamicRule(outputDir, packageNames, cacheDir, constants, extraArgs,
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
            //@formatter:off
            return "BackInput.Normal@" + System.identityHashCode(this) + '{'
                + "strategySignature=" + strategySignature
                + ", strategyAnalysisDataTask=" + strategyAnalysisDataTask
                + "; outputDir=" + outputDir
                + ", packageName='" + packageNames + '\''
                + (cacheDir == null ? "" : ", cacheDir=" + cacheDir)
                + ", constants=" + constants
                + ", extraArgs=" + extraArgs
                + ", checkInput=" + checkInput
                + ", usingLegacyStrategoStdLib=" + usingLegacyStrategoStdLib
                + '}';
            //@formatter:on
        }

        @Override public Key key() {
            return new Key(this, strategySignature);
        }
    }

    public static class DynamicRule extends Normal {
        public DynamicRule(ResourcePath outputDir, ArrayList<String> packageNames,
            @Nullable ResourcePath cacheDir, ArrayList<String> constants, Arguments extraArgs,
            CheckInput checkInput, StrategySignature strategySignature,
            STaskDef<CheckModuleInput, CheckModuleOutput> strFileGeneratingTasks,
            boolean legacyStrategoStdLib) {
            super(outputDir, packageNames, cacheDir, constants, extraArgs, checkInput,
                strategySignature, strFileGeneratingTasks, legacyStrategoStdLib);
        }

        /**
         * @return null if the contributions and used constructors were added to the parameters, or
         * the task that actually generates the outputs.
         */
        @Override public @Nullable STask<BackOutput> getStrategyContributions(ExecContext context, Back backTask,
            ArrayList<IStrategoAppl> strategyContributions,
            HashSet<ConstructorSignature> usedConstructors) {
            final Queue<StrategySignature> workList = new ArrayDeque<>();
            workList.add(strategySignature);
            final HashSet<StrategySignature> seen = new HashSet<>();
            seen.add(strategySignature);
            final TreeSet<StrategySignature> seenDynamicRules = new TreeSet<>();
            seenDynamicRules.add(strategySignature);
            while(!workList.isEmpty()) {
                StrategySignature dynamicRuleSignature = workList.remove();
                final HashSet<IModuleImportService.ModuleIdentifier> modulesDefiningDynamicRule =
                    PieUtils.requirePartial(context, backTask.check, checkInput,
                        new ModulesDefiningDynamicRule(dynamicRuleSignature));

                for(IModuleImportService.ModuleIdentifier moduleDefiningDynamicRule : modulesDefiningDynamicRule) {
                    if(moduleDefiningDynamicRule.isLibrary()) {
                        continue;
                    }
                    final HashSet<StrategySignature> strategiesUsingDynamicRule = PieUtils
                        .requirePartial(context, strategyAnalysisDataTask,
                            checkInput.checkModuleInput(moduleDefiningDynamicRule),
                            new GetStrategiesUsingDynamicRule(dynamicRuleSignature));
                    for(StrategySignature strategyUsingDynamicRule : strategiesUsingDynamicRule) {
                        if(!seen.contains(strategyUsingDynamicRule)) {
                            seen.add(strategyUsingDynamicRule);
                            final HashSet<IModuleImportService.ModuleIdentifier> modulesDefiningStrategy = PieUtils
                                .requirePartial(context, backTask.resolve, checkInput.resolveInput(),
                                    new ModulesDefiningStrategy(strategyUsingDynamicRule));

                            for(IModuleImportService.ModuleIdentifier moduleDefiningStrategy : modulesDefiningStrategy) {
                                final Set<StrategyAnalysisData> strategyAnalysisData = PieUtils
                                    .requirePartial(context, strategyAnalysisDataTask,
                                        checkInput.checkModuleInput(moduleDefiningStrategy),
                                        new GetStrategyAnalysisData(strategyUsingDynamicRule));
                                for(StrategyAnalysisData strategyAnalysisDatum : strategyAnalysisData) {
                                    for(StrategySignature definedDynamicRule : strategyAnalysisDatum.definedDynamicRules) {
                                        if(!seen.contains(definedDynamicRule)) {
                                            seen.add(definedDynamicRule);
                                            seenDynamicRules.add(definedDynamicRule);
                                            workList.add(definedDynamicRule);
                                        }
                                    }
                                    strategyContributions.add(strategyAnalysisDatum.analyzedAst);
                                    new UsedConstrs(usedConstructors).visit(strategyAnalysisDatum.analyzedAst);
                                }
                            }
                        }
                    }
                }
            }
            // Important test: only the BackInput.DynamicRule task with the "smallest" signature is
            // allowed to compile the set of strategies found through the above process. The Compile
            // tasks is smart and starts with the smallest signature for a dynamic rule, but there
            // may be BackInput.DynamicRule tasks from previous runs around that are no longer
            // valid.
            final StrategySignature firstSig = seenDynamicRules.first();
            final boolean taskIsStillRelevant = firstSig.equals(strategySignature);
            if(taskIsStillRelevant) {
                return null;
            } else {
                strategyContributions.clear();
                usedConstructors.clear();
                return backTask.createSupplier(dynamicRuleInput(firstSig));
            }
        }

        public Set<String> getStrategySignatures(
            Map<StrategySignature, ? extends Set<StrategySignature>> dynamicRules) {
            final Set<String> strategySignatures = new HashSet<>();
            final @Nullable Set<StrategySignature> dynamicRulesOrDefault = dynamicRules.get(strategySignature);
            for(StrategySignature iStrategoTerms : dynamicRulesOrDefault != null ? dynamicRulesOrDefault : Collections.<StrategySignature>emptySet()) {
                String name = iStrategoTerms.cifiedName();
                strategySignatures.add(name);
            }
            return strategySignatures;
        }

        @Override public String toString() {
            //@formatter:off
            return "BackInput.DynamicRule@" + System.identityHashCode(this) + '{'
                + "strategySignature=" + strategySignature
                + ", strategyAnalysisDataTask=" + strategyAnalysisDataTask
                + "; outputDir=" + outputDir
                + ", packageName='" + packageNames + '\''
                + (cacheDir == null ? "" : ", cacheDir=" + cacheDir)
                + ", constants=" + constants
                + ", extraArgs=" + extraArgs
                + ", checkInput=" + checkInput
                + ", usingLegacyStrategoStdLib=" + usingLegacyStrategoStdLib
                + '}';
            //@formatter:on
        }
    }

    public static class Congruence extends BackInput {
        private final STask<CompileDynamicRulesOutput> compileDR;

        public Congruence(ResourcePath outputDir, ArrayList<String> packageNames,
            @Nullable ResourcePath cacheDir, ArrayList<String> constants, Arguments extraArgs,
            CheckInput checkInput, boolean legacyStrategoStdLib,
            STask<CompileDynamicRulesOutput> compileDR) {
            super(outputDir, packageNames, cacheDir, constants, extraArgs, checkInput,
                legacyStrategoStdLib);
            this.compileDR = compileDR;
        }

        @Override public CTreeBuildResult buildCTree(ExecContext context, Back backTask,
            Collection<StrategySignature> compiledStrategies) throws ExecException {
            final CompileDynamicRulesOutput compileDROutput = context.require(compileDR);
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
                // TODO: make sure commented out logger debug messages occur in static checking already
                final StrategySignature congruenceSig = constructor.toCongruenceSig();
                if(globalIndex.nonExternalStrategies.contains(congruenceSig)) {
//                    context.logger().debug(
//                        "Skipping congruence overlapping with existing strategy: " + constructor);
                    continue;
                }
                if(globalIndex.externalConstructors.contains(constructor)) {
//                    context.logger().debug(
//                        "Skipping congruence of constructor overlapping with external constructor: "
//                            + constructor);
                    continue;
                }
                compiledStrategies.add(congruenceSig);
                congruences.add(backTask.strategoLanguage.toCongruenceAst(constructor, projectPath));
            }
            ArrayList<IStrategoTerm> overlayContributions = new ArrayList<>(globalIndex.overlayData.size());
            for(Map.Entry<ConstructorSignature, ArrayList<IStrategoTerm>> e : globalIndex.overlayData.entrySet()) {
                final StrategySignature congruenceSig = e.getKey().toCongruenceSig();
                if(globalIndex.nonExternalStrategies.contains(congruenceSig)) {
//                    context.logger().debug(
//                        "Skipping congruence overlapping with existing strategy: "
//                            + overlayData.signature);
                    continue;
                }
                if(globalIndex.externalConstructors.contains(e.getKey())) {
//                    context.logger().debug(
//                        "Skipping congruence of constructor overlapping with external constructor: "
//                            + overlayData.signature);
                    continue;
                }
                compiledStrategies.add(congruenceSig);
                overlayContributions.addAll(e.getValue());
            }
            congruences.addAll(
                backTask.strategoLanguage.toCongruenceAsts(overlayContributions, projectPath));
            if(usingLegacyStrategoStdLib) {
                congruences.add(backTask.generateStratego.anno_cong__ast);
                compiledStrategies.add(new StrategySignature("Anno_Cong__", 2, 0));
            }

            final @Nullable IStrategoAppl dynamicCallsDefinition = backTask.generateStratego
                .dynamicCallsDefinition(compileDROutput.newGenerated, compileDROutput.undefineGenerated);
            if(dynamicCallsDefinition != null) {
                congruences.add(dynamicCallsDefinition);
                compiledStrategies.add(new StrategySignature("DYNAMIC_CALLS", 0, 0));
            }

            return CTreeBuildResult
                .withResult(backTask.generateStratego.packStrategies(congruences));
        }

        @Override public String toString() {
            //@formatter:off
            return "BackInput.Congruence@" + System.identityHashCode(this) + '{'
                + "compileDR=" + compileDR
                + "; outputDir=" + outputDir
                + ", packageName='" + packageNames + '\''
                + (cacheDir == null ? "" : ", cacheDir=" + cacheDir)
                + ", constants=" + constants
                + ", extraArgs=" + extraArgs
                + ", checkInput=" + checkInput
                + ", usingLegacyStrategoStdLib=" + usingLegacyStrategoStdLib
                + '}';
            //@formatter:on
        }
    }

    public static class Boilerplate extends BackInput {
        public final boolean library;
        public final String libraryName;
        public final STask<CompileDynamicRulesOutput> compileDR;

        public Boilerplate(ResourcePath outputDir, ArrayList<String> packageNames,
            @Nullable ResourcePath cacheDir, ArrayList<String> constants, Arguments extraArgs,
            CheckInput checkInput, boolean library, boolean legacyStrategoStdLib,
            String libraryName, STask<CompileDynamicRulesOutput> compileDR) {
            super(outputDir, packageNames, cacheDir, constants, extraArgs, checkInput,
                legacyStrategoStdLib);
            this.library = library;
            this.libraryName = libraryName;
            this.compileDR = compileDR;
        }

        @Override public boolean equals(Object o) {
            if(this == o)
                return true;
            if(getClass() != o.getClass())
                return false;
            if(!super.equals(o))
                return false;

            Boilerplate that = (Boilerplate) o;

            if(library != that.library)
                return false;
            if(!libraryName.equals(that.libraryName))
                return false;
            return compileDR.equals(that.compileDR);
        }

        @Override public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + (library ? 1 : 0);
            result = 31 * result + libraryName.hashCode();
            result = 31 * result + compileDR.hashCode();
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
            boolean dynamicCallsDefined = PieUtils.requirePartial(context, compileDR,
                DynamicCallsDefined.INSTANCE);
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
            //@formatter:off
            return "BackInput.Boilerplate@" + System.identityHashCode(this) + '{'
                + "library=" + library
                + ", libraryName='" + libraryName + '\''
                + ", compileDR=" + compileDR
                + "; outputDir=" + outputDir
                + ", packageName='" + packageNames + '\''
                + (cacheDir == null ? "" : ", cacheDir=" + cacheDir)
                + ", constants=" + constants
                + ", extraArgs=" + extraArgs
                + ", checkInput=" + checkInput
                + ", usingLegacyStrategoStdLib=" + usingLegacyStrategoStdLib
                + '}';
            //@formatter:on
        }
    }
}
