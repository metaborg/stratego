package mb.stratego.build.strincr.task;

import java.util.LinkedHashSet;
import java.util.Set;


import org.metaborg.util.cmd.Arguments;

import mb.pie.api.ExecContext;
import mb.pie.api.Interactivity;
import mb.pie.api.STask;
import mb.pie.api.STaskDef;
import mb.pie.api.Supplier;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.stratego.build.strincr.BuiltinLibraryIdentifier;
import mb.stratego.build.strincr.Stratego2LibInfo;
import mb.stratego.build.strincr.data.StrategySignature;
import mb.stratego.build.strincr.function.GetCheckMessages;
import mb.stratego.build.strincr.function.ToCompileGlobalIndex;
import mb.stratego.build.strincr.function.output.CheckOutputMessages;
import mb.stratego.build.strincr.function.output.CompileGlobalIndex;
import mb.stratego.build.strincr.task.input.BackInput;
import mb.stratego.build.strincr.task.input.CLCFInput;
import mb.stratego.build.strincr.task.input.CheckModuleInput;
import mb.stratego.build.strincr.task.input.CompileDynamicRulesInput;
import mb.stratego.build.strincr.task.input.CompileInput;
import mb.stratego.build.strincr.task.output.BackOutput;
import mb.stratego.build.strincr.task.output.CheckModuleOutput;
import mb.stratego.build.strincr.task.output.CompileDynamicRulesOutput;
import mb.stratego.build.strincr.task.output.CompileOutput;

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
    public final CompileDynamicRules compileDynamicRules;
    public final Back back;

    @jakarta.inject.Inject public Compile(Resolve resolve, CopyLibraryClassFiles copyLibraryClassFiles, Check check,
        CompileDynamicRules compileDynamicRules, Back back) {
        this.resolve = resolve;
        this.copyLibraryClassFiles = copyLibraryClassFiles;
        this.check = check;
        this.compileDynamicRules = compileDynamicRules;
        this.back = back;
    }

    @Override public CompileOutput exec(ExecContext context, CompileInput input) {
        final CheckOutputMessages checkOutput =
            context.requireMapping(check, input.checkInput, GetCheckMessages.INSTANCE);
        if(checkOutput.containsErrors) {
            return new CompileOutput.Failure(checkOutput.messages);
        }

        final LinkedHashSet<ResourcePath> resultFiles = new LinkedHashSet<>();

        final CompileGlobalIndex compileGlobalIndex = context.requireMapping(resolve, input.checkInput.resolveInput(),
                ToCompileGlobalIndex.INSTANCE);

        final Arguments extraArgs = new Arguments(input.extraArgs);
        final ResourcePath outputDirWithPackage =
            input.outputDir.appendOrReplaceWithPath(input.packageNames.get(0).replace('.', '/'));
        if(input.createShadowJar) {
            for(Supplier<Stratego2LibInfo> str2library : input.checkInput.importResolutionInfo.str2libraries) {
                context.require(copyLibraryClassFiles, new CLCFInput(str2library, input.str2libReplicateDir));
            }
        }
        for(String importedStr2LibPackageName : compileGlobalIndex.importedStr2LibPackageNames) {
            extraArgs.add("-la", importedStr2LibPackageName);
        }

        final STaskDef<CheckModuleInput, CheckModuleOutput> strategyAnalysisDataTask =
            new STaskDef<>(CheckModule.id);

        boolean usingLegacyStrategoStdLib =
            input.checkInput.importResolutionInfo.linkedLibraries.contains(BuiltinLibraryIdentifier.StrategoLib);

        final CompileDynamicRulesInput compileDRInput =
            new CompileDynamicRulesInput(outputDirWithPackage, input.packageNames, input.cacheDir,
                input.constants, extraArgs, input.checkInput,
                strategyAnalysisDataTask, usingLegacyStrategoStdLib);
        final STask<CompileDynamicRulesOutput> compileDR =
            compileDynamicRules.createSupplier(compileDRInput);
        resultFiles.addAll(context.require(compileDR).resultFiles);

        for(StrategySignature strategySignature : compileGlobalIndex.nonExternalStrategies) {
            final BackInput.Normal normalInput =
                new BackInput.Normal(outputDirWithPackage, input.packageNames, input.cacheDir,
                    input.constants, extraArgs, input.checkInput, strategySignature,
                    strategyAnalysisDataTask, usingLegacyStrategoStdLib);
            final BackOutput output = context.require(back, normalInput);
            assert output != null && !output.depTasksHaveErrorMessages : "Previous code should have already returned on checkOutput.containsErrors";
            resultFiles.addAll(output.resultFiles);
        }
        final BackInput.Boilerplate boilerplateInput =
            new BackInput.Boilerplate(outputDirWithPackage, input.packageNames, input.cacheDir,
                input.constants, extraArgs, input.checkInput, input.library,
                usingLegacyStrategoStdLib, input.libraryName, compileDR);
        final BackOutput boilerplateOutput = context.require(back, boilerplateInput);
        assert boilerplateOutput != null && !boilerplateOutput.depTasksHaveErrorMessages : "Previous code should have already returned on checkOutput.containsErrors";
        resultFiles.addAll(boilerplateOutput.resultFiles);

        final BackInput.Congruence congruenceInput =
            new BackInput.Congruence(outputDirWithPackage, input.packageNames, input.cacheDir,
                input.constants, extraArgs, input.checkInput, usingLegacyStrategoStdLib, compileDR);
        final BackOutput congruenceOutput = context.require(back, congruenceInput);
        assert congruenceOutput != null && !congruenceOutput.depTasksHaveErrorMessages : "Previous code should have already returned on checkOutput.containsErrors";
        resultFiles.addAll(congruenceOutput.resultFiles);

        return new CompileOutput.Success(resultFiles, checkOutput.messages);
    }

    @Override public boolean shouldExecWhenAffected(CompileInput input, Set<?> tags) {
        return tags.isEmpty() || tags.contains(Interactivity.NonInteractive);
    }

    @Override public String getId() {
        return id;
    }
}
