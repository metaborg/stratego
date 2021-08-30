package mb.stratego.build.strincr.task;

import java.util.HashSet;
import java.util.LinkedHashSet;

import javax.inject.Inject;

import org.metaborg.util.cmd.Arguments;

import mb.pie.api.ExecContext;
import mb.pie.api.STaskDef;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.stratego.build.strincr.Stratego2LibInfo;
import mb.stratego.build.strincr.data.StrategySignature;
import mb.stratego.build.strincr.function.GetMessages;
import mb.stratego.build.strincr.function.ToCompileGlobalIndex;
import mb.stratego.build.strincr.function.output.CheckOutputMessages;
import mb.stratego.build.strincr.function.output.CompileGlobalIndex;
import mb.stratego.build.strincr.task.input.BackInput;
import mb.stratego.build.strincr.task.input.CLCFInput;
import mb.stratego.build.strincr.task.input.CheckModuleInput;
import mb.stratego.build.strincr.task.input.CompileInput;
import mb.stratego.build.strincr.task.output.BackOutput;
import mb.stratego.build.strincr.task.output.CheckModuleOutput;
import mb.stratego.build.strincr.task.output.CompileOutput;
import mb.stratego.build.util.PieUtils;

/**
 * The one task to rule them all, this task runs {@link Check}, stops if there are errors, and
 * otherwise continues to run all the Back tasks. It returns a list of the Java files that were
 * written to, as well as non-error messages from Check.
 */
public class Compile implements TaskDef<CompileInput, CompileOutput> {
    public static final String id = "stratego." + Compile.class.getSimpleName();

    public final Resolve resolve;
    public final CopyLibraryClassFiles copyLibraryClassFiles;
    public final Check check;
    public final Back back;

    @Inject public Compile(Resolve resolve, CopyLibraryClassFiles copyLibraryClassFiles, Check check,
        Back back) {
        this.resolve = resolve;
        this.copyLibraryClassFiles = copyLibraryClassFiles;
        this.check = check;
        this.back = back;
    }

    @Override public CompileOutput exec(ExecContext context, CompileInput input) {
        final CheckOutputMessages checkOutput =
            PieUtils.requirePartial(context, check, input.checkInput, GetMessages.INSTANCE);
        if(checkOutput.containsErrors) {
            return new CompileOutput.Failure(checkOutput.messages);
        }

        final LinkedHashSet<ResourcePath> resultFiles = new LinkedHashSet<>();
        final CompileGlobalIndex compileGlobalIndex = PieUtils
            .requirePartial(context, resolve, input.checkInput.resolveInput(),
                ToCompileGlobalIndex.INSTANCE);

        final Arguments extraArgs = new Arguments(input.extraArgs);
        final ResourcePath outputDirWithPackage =
            input.outputDir.appendOrReplaceWithPath(input.packageName.replace('.', '/'));
        for(Supplier<Stratego2LibInfo> str2library : input.checkInput.importResolutionInfo.str2libraries) {
            context.require(copyLibraryClassFiles, new CLCFInput(str2library, input.javaClassDir));
        }
        for(String importedStr2LibPackageName : compileGlobalIndex.importedStr2LibPackageNames) {
            extraArgs.add("-la", importedStr2LibPackageName);
        }

        final HashSet<StrategySignature> compiledThroughDynamicRule = new HashSet<>();

        final HashSet<String> dynamicRuleNewGenerated = new HashSet<>();
        final HashSet<String> dynamicRuleUndefineGenerated = new HashSet<>();

        final STaskDef<CheckModuleInput, CheckModuleOutput> strategyAnalysisDataTask =
            new STaskDef<>(CheckModule.id);

        for(StrategySignature dynamicRule : compileGlobalIndex.dynamicRules) {
            if(compiledThroughDynamicRule.contains(dynamicRule)) {
                continue;
            }
            final BackInput.DynamicRule dynamicRuleInput =
                new BackInput.DynamicRule(outputDirWithPackage, input.packageName, input.cacheDir,
                    input.constants, extraArgs, input.checkInput, dynamicRule,
                    strategyAnalysisDataTask, input.usingLegacyStrategoStdLib);
            final BackOutput output = context.require(back, dynamicRuleInput);
            assert output != null;
            assert !output.depTasksHaveErrorMessages : "Previous code should have already returned on checkOutput.containsErrors";
            resultFiles.addAll(output.resultFiles);
            compiledThroughDynamicRule.addAll(output.compiledStrategies);
        }
        for(StrategySignature dynamicRule : compileGlobalIndex.dynamicRules) {
            final StrategySignature dynamicRuleNew =
                new StrategySignature("new-" + dynamicRule.name, 0, 2);
            if(compiledThroughDynamicRule.contains(dynamicRuleNew)) {
                dynamicRuleNewGenerated.add(dynamicRule.name);
            }
            final StrategySignature dynamicRuleUndefine =
                new StrategySignature("undefine-" + dynamicRule.name, 0, 1);
            if(compiledThroughDynamicRule.contains(dynamicRuleUndefine)) {
                dynamicRuleUndefineGenerated.add(dynamicRule.name);
            }
        }
        for(StrategySignature strategySignature : compileGlobalIndex.nonExternalStrategies) {
            if(compiledThroughDynamicRule.contains(strategySignature)) {
                continue;
            }
            final BackInput.Normal normalInput =
                new BackInput.Normal(outputDirWithPackage, input.packageName, input.cacheDir,
                    input.constants, extraArgs, input.checkInput, strategySignature,
                    strategyAnalysisDataTask, input.usingLegacyStrategoStdLib);
            final BackOutput output = context.require(back, normalInput);
            assert output != null;
            assert !output.depTasksHaveErrorMessages : "Previous code should have already returned on checkOutput.containsErrors";
            resultFiles.addAll(output.resultFiles);
        }
        final boolean dynamicCallsDefined =
            !dynamicRuleNewGenerated.isEmpty() || !dynamicRuleUndefineGenerated.isEmpty();
        final BackInput.Boilerplate boilerplateInput =
            new BackInput.Boilerplate(outputDirWithPackage, input.packageName, input.cacheDir,
                input.constants, extraArgs, input.checkInput, dynamicCallsDefined, input.library,
                input.usingLegacyStrategoStdLib, input.libraryName);
        final BackOutput boilerplateOutput = context.require(back, boilerplateInput);
        assert boilerplateOutput != null;
        assert !boilerplateOutput.depTasksHaveErrorMessages : "Previous code should have already returned on checkOutput.containsErrors";
        resultFiles.addAll(boilerplateOutput.resultFiles);

        final BackInput.Congruence congruenceInput =
            new BackInput.Congruence(outputDirWithPackage, input.packageName, input.cacheDir,
                input.constants, extraArgs, input.checkInput, dynamicRuleNewGenerated,
                dynamicRuleUndefineGenerated, input.usingLegacyStrategoStdLib);
        final BackOutput congruenceOutput = context.require(back, congruenceInput);
        assert congruenceOutput != null;
        assert !congruenceOutput.depTasksHaveErrorMessages : "Previous code should have already returned on checkOutput.containsErrors";
        resultFiles.addAll(congruenceOutput.resultFiles);

        return new CompileOutput.Success(resultFiles, checkOutput.messages);
    }

    @Override public String getId() {
        return id;
    }
}
