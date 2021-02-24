package mb.stratego.build.strincr.task.input;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import org.metaborg.util.cmd.Arguments;
import org.spoofax.interpreter.terms.IStrategoAppl;

import mb.pie.api.ExecContext;
import mb.pie.api.STask;
import mb.resource.hierarchical.ResourcePath;
import mb.stratego.build.strincr.IModuleImportService.ModuleIdentifier;
import mb.stratego.build.strincr.data.ConstructorSignature;
import mb.stratego.build.strincr.data.StrategyAnalysisData;
import mb.stratego.build.strincr.data.StrategySignature;
import mb.stratego.build.strincr.function.GetDynamicRuleAnalysisData;
import mb.stratego.build.strincr.function.GetStrategyAnalysisData;
import mb.stratego.build.strincr.function.ModulesDefiningDynamicRule;
import mb.stratego.build.strincr.function.ModulesDefiningStrategy;
import mb.stratego.build.strincr.task.CheckModule;
import mb.stratego.build.strincr.task.output.CheckOutput;
import mb.stratego.build.strincr.task.output.GlobalData;
import mb.stratego.build.termvisitors.UsedConstrs;
import mb.stratego.build.util.PieUtils;

public abstract class BackInput implements Serializable {
    public final ResourcePath outputDir;
    public final @Nullable String packageName;
    public final @Nullable ResourcePath cacheDir;
    public final List<String> constants;
    public final Collection<ResourcePath> includeDirs;
    public final Arguments extraArgs;
    public final STask<GlobalData> resolveTask;

    public BackInput(ResourcePath outputDir, @Nullable String packageName,
        @Nullable ResourcePath cacheDir, List<String> constants,
        Collection<ResourcePath> includeDirs, Arguments extraArgs, STask<GlobalData> resolveTask) {
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

        BackInput input = (BackInput) o;

        if(!outputDir.equals(input.outputDir))
            return false;
        if(packageName != null ? !packageName.equals(input.packageName) : input.packageName != null)
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

    public static class Normal extends BackInput {
        public final StrategySignature strategySignature;
        public final ModuleIdentifier mainModuleIdentifier;
        public final Collection<STask<?>> strFileGeneratingTasks;

        public Normal(StrategySignature strategySignature, ResourcePath outputDir,
            @Nullable String packageName, @Nullable ResourcePath cacheDir, List<String> constants,
            Collection<ResourcePath> includeDirs, Arguments extraArgs,
            STask<GlobalData> resolveTask, ModuleIdentifier mainModuleIdentifier,
            Collection<STask<?>> strFileGeneratingTasks) {
            super(outputDir, packageName, cacheDir, constants, includeDirs, extraArgs, resolveTask);
            this.strategySignature = strategySignature;
            this.mainModuleIdentifier = mainModuleIdentifier;
            this.strFileGeneratingTasks = strFileGeneratingTasks;
        }

        public void getStrategyContributions(ExecContext context, CheckModule checkModule,
            List<IStrategoAppl> strategyContributions, Set<ConstructorSignature> usedConstructors) {
            final StrategySignature strategySignature = this.strategySignature;
            final Set<ModuleIdentifier> modulesDefiningStrategy = PieUtils
                .requirePartial(context, this.resolveTask,
                    new ModulesDefiningStrategy<>(strategySignature));

            for(ModuleIdentifier moduleIdentifier : modulesDefiningStrategy) {
                if(moduleIdentifier.isLibrary()) {
                    continue;
                }
                final Set<StrategyAnalysisData> strategyAnalysisData = PieUtils
                    .requirePartial(context, checkModule,
                        new CheckModuleInput.Normal(this.mainModuleIdentifier, moduleIdentifier,
                            strFileGeneratingTasks, includeDirs),
                        new GetStrategyAnalysisData<>(strategySignature));
                for(StrategyAnalysisData strategyAnalysisDatum : strategyAnalysisData) {
                    strategyContributions.add(strategyAnalysisDatum.analyzedAst);
                    new UsedConstrs(usedConstructors, strategyAnalysisDatum.lastModified)
                        .visit(strategyAnalysisDatum.analyzedAst);
                }
            }
        }

        @Override public String toString() {
            return "Back.NormalInput(" + strategySignature.cifiedName() + ")";
        }
    }

    public static class DynamicRule extends Normal {
        public final STask<CheckOutput> checkTask;

        public DynamicRule(StrategySignature strategySignature, ResourcePath outputDir,
            @Nullable String packageName, @Nullable ResourcePath cacheDir, List<String> constants,
            Collection<ResourcePath> includeDirs, Arguments extraArgs,
            STask<GlobalData> resolveTask, ModuleIdentifier mainModuleIdentifier,
            Collection<STask<?>> strFileGeneratingTasks,
            STask<CheckOutput> checkTask) {
            super(strategySignature, outputDir, packageName, cacheDir, constants, includeDirs,
                extraArgs, resolveTask, mainModuleIdentifier, strFileGeneratingTasks);
            this.checkTask = checkTask;
        }

        @Override public void getStrategyContributions(ExecContext context, CheckModule checkModule,
            List<IStrategoAppl> strategyContributions, Set<ConstructorSignature> usedConstructors) {
            final Deque<StrategySignature> workList = new ArrayDeque<>();
            workList.add(strategySignature);
            final Set<StrategySignature> seen = new HashSet<>();
            seen.add(strategySignature);
            while(!workList.isEmpty()) {
                StrategySignature strategySignature = workList.remove();
                final Set<ModuleIdentifier> modulesDefiningStrategy = PieUtils
                    .requirePartial(context, this.checkTask,
                        new ModulesDefiningDynamicRule<>(strategySignature));

                for(ModuleIdentifier moduleIdentifier : modulesDefiningStrategy) {
                    if(moduleIdentifier.isLibrary()) {
                        continue;
                    }
                    final Set<StrategyAnalysisData> strategyAnalysisData = PieUtils
                        .requirePartial(context, checkModule,
                            new CheckModuleInput.Normal(this.mainModuleIdentifier, moduleIdentifier,
                                strFileGeneratingTasks, includeDirs),
                            new GetDynamicRuleAnalysisData<>(strategySignature));
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

        @Override public boolean equals(Object o) {
            if(this == o)
                return true;
            if(o == null || getClass() != o.getClass())
                return false;
            if(!super.equals(o))
                return false;

            DynamicRule that = (DynamicRule) o;

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

    public static class Congruence extends BackInput {
        public final Set<String> dynamicRuleNewGenerated;
        public final Set<String> dynamicRuleUndefineGenerated;

        public Congruence(STask<GlobalData> resolveTask, ResourcePath outputDir,
            @Nullable String packageName, @Nullable ResourcePath cacheDir, List<String> constants,
            Collection<ResourcePath> includeDirs, Arguments extraArgs,
            Set<String> dynamicRuleNewGenerated, Set<String> dynamicRuleUndefineGenerated) {
            super(outputDir, packageName, cacheDir, constants, includeDirs, extraArgs, resolveTask);
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

        @Override public String toString() {
            return "Back.CongruenceInput";
        }
    }

    public static class Boilerplate extends BackInput {
        public final boolean dynamicCallsDefined;
        public final Collection<STask<?>> strFileGeneratingTasks;

        public Boilerplate(STask<GlobalData> resolveTask, ResourcePath outputDir,
            @Nullable String packageName, @Nullable ResourcePath cacheDir, List<String> constants,
            Collection<ResourcePath> includeDirs, Arguments extraArgs, boolean dynamicCallsDefined,
            Collection<STask<?>> strFileGeneratingTasks) {
            super(outputDir, packageName, cacheDir, constants, includeDirs, extraArgs, resolveTask);
            this.dynamicCallsDefined = dynamicCallsDefined;
            this.strFileGeneratingTasks = strFileGeneratingTasks;
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

        @Override public String toString() {
            return "Back.BoilerplateInput";
        }
    }
}
