package mb.stratego.build.strincr;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Inject;

import org.metaborg.util.cmd.Arguments;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.util.B;
import org.strategoxt.strj.strj_sep_comp_0_0;

import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.Task;
import mb.pie.api.TaskDef;
import mb.resource.ResourceKeyString;
import mb.resource.hierarchical.ResourcePath;
import mb.stratego.build.strincr.IModuleImportService.ModuleIdentifier;
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
        public final String packageName;
        public final @Nullable ResourcePath cacheDir;
        public final List<String> constants;
        public final Collection<ResourcePath> includeDirs;
        public final Arguments extraArgs;
        public final Task<GlobalData> resolveTask;

        public Input(ResourcePath outputDir, String packageName, @Nullable ResourcePath cacheDir,
            List<String> constants, Collection<ResourcePath> includeDirs, Arguments extraArgs,
            Task<GlobalData> resolveTask) {
            this.outputDir = outputDir;
            this.packageName = packageName;
            this.cacheDir = cacheDir;
            this.constants = constants;
            this.includeDirs = includeDirs;
            this.extraArgs = extraArgs;
            this.resolveTask = resolveTask;
        }
    }

    public static class NormalInput extends Input {
        public final StrategySignature strategySignature;

        public NormalInput(StrategySignature strategySignature, ResourcePath outputDir,
            String packageName, @Nullable ResourcePath cacheDir, List<String> constants,
            Collection<ResourcePath> includeDirs, Arguments extraArgs,
            Task<GlobalData> resolveTask) {
            super(outputDir, packageName, cacheDir, constants, includeDirs, extraArgs, resolveTask);
            this.strategySignature = strategySignature;
        }
    }

    public static class BoilerplateInput extends Input {
        public BoilerplateInput(Task<GlobalData> resolveTask, ResourcePath outputDir,
            String packageName, @Nullable ResourcePath cacheDir, List<String> constants,
            Collection<ResourcePath> includeDirs, Arguments extraArgs) {
            super(outputDir, packageName, cacheDir, constants, includeDirs, extraArgs, resolveTask);
        }
    }

    public static class Output implements Serializable {
        public final List<ResourcePath> resultFiles;

        public Output(List<ResourcePath> resultFiles) {
            this.resultFiles = resultFiles;
        }
    }

    private final IOAgentTrackerFactory ioAgentTrackerFactory;
    private final StrIncrContext strContext;
    private final ITermFactory termFactory;
    private final ResourcePathConverter resourcePathConverter;

    @Inject public Back(IOAgentTrackerFactory ioAgentTrackerFactory, StrIncrContext strContext,
        ResourcePathConverter resourcePathConverter) {
        this.ioAgentTrackerFactory = ioAgentTrackerFactory;
        this.strContext = strContext;
        this.termFactory = strContext.getFactory();
        this.resourcePathConverter = resourcePathConverter;
    }

    @Override public Output exec(ExecContext context, Input input) throws Exception {
        List<ResourcePath> resultFiles = new ArrayList<>();

        final boolean isBoilerplate = input instanceof BoilerplateInput;
        final IStrategoTerm ctree;
        if(isBoilerplate) {
            final GlobalIndex globalIndex =
                PieUtils.requirePartial(context, input.resolveTask, GlobalData::toGlobalIndex);
            ctree = Packer.packBoilerplate(termFactory, globalIndex.constructors,
                StrategyStubs.declStubs(globalIndex.strategies));
        } else {
            final List<IStrategoAppl> strategyContributions = new ArrayList<>();

            final StrategySignature strategySignature = ((NormalInput) input).strategySignature;
            final Set<ModuleIdentifier> modulesDefiningStrategy = PieUtils
                .requirePartial(context, input.resolveTask,
                    gd -> gd.modulesDefiningStrategy(strategySignature));

            // TODO: get all strategy definitions based on the signature, from the tasks that did the analysis/cast insertion for the modulesDefiningStrategy
            // TODO: move overlay stuff back to Check, remove from pack and the Stratego backend code
            ctree = Packer
                .packStrategy(termFactory, Collections.emptyList(), strategyContributions);
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
            throw new ExecException("Call to strj failed:\n" + result.exception, null);
        }

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

        return new Output(resultFiles);
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
