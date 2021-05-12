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
import org.spoofax.terms.StrategoTerm;

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
import mb.stratego.build.strincr.task.output.CheckModuleOutput;
import mb.stratego.build.termvisitors.UsedConstrs;
import mb.stratego.build.util.PieUtils;
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
            return anythingElse != null ? anythingElse.equals(key.anythingElse) :
                key.anythingElse == null;
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
                final HashSet<ConstructorSignature> newlyFoundConstructors = new HashSet<>(usedConstructors);
                // Overlays can use other overlays, so this loop is for finding those transitive uses
                while(!newlyFoundConstructors.isEmpty()) {
                    final ArrayList<OverlayData> overlayData = PieUtils
                        .requirePartial(context, backTask.front,
                            new FrontInput.Normal(moduleIdentifier, checkInput.strFileGeneratingTasks,
                                checkInput.includeDirs, checkInput.linkedLibraries, checkInput.autoImportStd),
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
                Packer.packStrategy(backTask.tf, overlayContributions, strategyContributions);

            final String projectPath =
                backTask.resourcePathConverter.toString(checkInput.projectPath);
            final IStrategoTerm result =
                backTask.strategoLanguage.desugar(desugaringInput, projectPath);

            //noinspection ConstantConditions
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

            return result;
        }

        public void getStrategyContributions(ExecContext context, Back backTask,
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
                    strategyContributions.add(strategyAnalysisDatum.analyzedAst);
                    new UsedConstrs(usedConstructors)
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
        public DynamicRule(ResourcePath outputDir, @Nullable String packageName,
            @Nullable ResourcePath cacheDir, ArrayList<String> constants, Arguments extraArgs,
            CheckInput checkInput, StrategySignature strategySignature,
            STaskDef<CheckModuleInput, CheckModuleOutput> strFileGeneratingTasks) {
            super(outputDir, packageName, cacheDir, constants, extraArgs, checkInput,
                strategySignature, strFileGeneratingTasks);
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
                        .requirePartial(context, strategyAnalysisDataTask,
                            checkInput.checkModuleInput(moduleIdentifier),
                            new GetDynamicRuleAnalysisData(strategySignature));
                    for(StrategyAnalysisData strategyAnalysisDatum : strategyAnalysisData) {
                        strategyContributions.add(strategyAnalysisDatum.analyzedAst);
                        new UsedConstrs(usedConstructors)
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
            Collection<StrategySignature> compiledStrategies) throws ExecException {
            final CongruenceGlobalIndex globalIndex = PieUtils
                .requirePartial(context, backTask.resolve, checkInput.resolveInput(),
                    ToCongruenceGlobalIndex.INSTANCE);
            final ArrayList<ConstructorSignature> constructors =
                new ArrayList<>(globalIndex.nonExternalConstructors.size() + 2);
            constructors.addAll(globalIndex.nonExternalConstructors);
            constructors.add(backTask.generateStratego.dr_dummy);
            constructors.add(backTask.generateStratego.dr_undefine);

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
                congruences.add(backTask.strategoLanguage.toCongruenceAst(overlayData.astTerm, projectPath));
            }
            congruences.add(backTask.generateStratego.anno_cong__ast);
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
        public final boolean library;

        public Boilerplate(ResourcePath outputDir, @Nullable String packageName,
            @Nullable ResourcePath cacheDir, ArrayList<String> constants, Arguments extraArgs,
            CheckInput checkInput, boolean dynamicCallsDefined, boolean library) {
            super(outputDir, packageName, cacheDir, constants, extraArgs, checkInput);
            this.dynamicCallsDefined = dynamicCallsDefined;
            this.library = library;
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
                            checkInput.includeDirs, checkInput.linkedLibraries,
                            checkInput.autoImportStd), GetConstrData.INSTANCE);
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
