package mb.stratego.build.strincr;

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.SortedMap;

import javax.annotation.Nullable;

import mb.stratego.build.util.IOAgentTrackerFactory;
import mb.stratego.build.util.StrategoConstants;
import org.apache.commons.io.output.NullOutputStream;
import org.metaborg.util.cmd.Arguments;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.util.B;
import org.strategoxt.lang.StrategoExit;
import org.strategoxt.lang.Strategy;
import org.strategoxt.stratego_lib.dr_scope_all_end_0_0;
import org.strategoxt.stratego_lib.dr_scope_all_start_0_0;
import org.strategoxt.strj.strj_sep_comp_0_0;

import javax.inject.Inject;

import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.Logger;
import mb.pie.api.None;
import mb.pie.api.TaskDef;
import mb.stratego.build.termvisitors.TermSize;
import mb.stratego.build.util.IOAgentTracker;
import mb.stratego.build.util.StrIncrContext;
import mb.stratego.build.util.StrategoExecutor;
import mb.stratego.compiler.pack.Packer;

public class Backend implements TaskDef<Backend.Input, None> {
    public static final String id = Backend.class.getCanonicalName();

    public static final class Input implements Serializable {
        final File projectLocation;
        final String strategyName;
        final Collection<IStrategoAppl> strategyContributions;
        final Collection<IStrategoAppl> overlayContributions;
        final SortedMap<String, String> ambStrategyResolution;
        final @Nullable String packageName;
        final File outputPath;
        final @Nullable File cacheDir;
        final List<String> constants;
        final Collection<File> includeDirs;
        final Arguments extraArgs;
        final boolean isBoilerplate;

        Input(File projectLocation, @Nullable String strategyName, Collection<IStrategoAppl> strategyContributions,
            Collection<IStrategoAppl> overlayContributions, SortedMap<String, String> ambStrategyResolution,
            @Nullable String packageName, File outputPath, @Nullable File cacheDir, List<String> constants,
            Collection<File> includeDirs, Arguments extraArgs, boolean isBoilerplate) {
            this.projectLocation = projectLocation;
            this.strategyName = strategyName == null ? "" : strategyName;
            this.strategyContributions = strategyContributions;
            this.overlayContributions = overlayContributions;
            this.ambStrategyResolution = ambStrategyResolution;
            this.packageName = packageName;
            this.outputPath = outputPath;
            this.cacheDir = cacheDir;
            this.constants = constants;
            this.includeDirs = includeDirs;
            this.extraArgs = extraArgs;
            this.isBoilerplate = isBoilerplate;
        }

        @Override
        public String toString() {
            return "StrIncrBack$Input(" + strategyName + ')';
        }

        @Override
        public boolean equals(Object o) {
            if(this == o)
                return true;
            if(!(o instanceof Input))
                return false;
            Input input = (Input) o;
            return isBoilerplate == input.isBoilerplate && projectLocation.equals(input.projectLocation) && strategyName
                .equals(input.strategyName) && strategyContributions.equals(input.strategyContributions)
                && overlayContributions.equals(input.overlayContributions) && ambStrategyResolution
                .equals(input.ambStrategyResolution) && Objects.equals(packageName, input.packageName) && outputPath
                .equals(input.outputPath) && Objects.equals(cacheDir, input.cacheDir) && constants
                .equals(input.constants) && includeDirs.equals(input.includeDirs) && extraArgs.equals(input.extraArgs);
        }

        @Override
        public int hashCode() {
            return Objects
                .hash(projectLocation, strategyName, strategyContributions, overlayContributions, ambStrategyResolution,
                    packageName, outputPath, cacheDir, constants, includeDirs, extraArgs, isBoilerplate);
        }
    }

    private final ITermFactory termFactory;
    private final IOAgentTrackerFactory ioAgentTrackerFactory;
    private final StrIncrContext strContext;

    @Inject public Backend(ITermFactory termFactory, IOAgentTrackerFactory ioAgentTrackerFactory, StrIncrContext strContext) {
        this.termFactory = termFactory;
        this.ioAgentTrackerFactory = ioAgentTrackerFactory;
        this.strContext = strContext;
    }

    @Override public None exec(ExecContext execContext, Input input) throws Exception {
        BuildStats.executedBackTasks++;

        final long startTime = System.nanoTime();
        final IStrategoTerm ctree;
        if(input.isBoilerplate) {
            ctree = Packer.packBoilerplate(termFactory, input.strategyContributions);
        } else {
            ctree = Packer.packStrategy(termFactory, input.overlayContributions, input.strategyContributions,
                input.ambStrategyResolution);
        }
        BuildStats.strategyBackendCTreeSize.put(input.strategyName, TermSize.computeTermSize(ctree));

        // Call Stratego compiler
        // Note that we need --library and turn off fusion with --fusion for separate compilation
        final Arguments arguments = new Arguments().add("-i", "passedExplicitly.ctree").addFile("-o", input.outputPath)
            //            .add("--verbose", 3)
            .addLine(input.packageName != null ? "-p " + input.packageName : "").add("--library").add("--fusion");
        if(input.isBoilerplate) {
            arguments.add("--boilerplate");
        } else {
            arguments.add("--single-strategy");
        }

        for(File includeDir : input.includeDirs) {
            arguments.add("-I", includeDir);
        }

        if(input.cacheDir != null) {
            arguments.addFile("--cache-dir", input.cacheDir);
        }

        for(String constant : input.constants) {
            // Needed in boilerplate for generating a strategy (e.g. $C$O$N$S$T$A$N$T_0_0), needed in single-strategy
            //  to turn e.g. prim("CONSTANT") into Build(theconstantvalue), in the example where you give pass
            //  -DCONSTANT=theconstantvalue.
            arguments.add("-D", constant);
        }
        arguments.addAll(input.extraArgs);


        final StrategoExecutor.ExecutionResult result = runLocallyUniqueStringStrategy(execContext.logger(), true,
            newResourceTracker(new File(System.getProperty("user.dir")), true), strj_sep_comp_0_0.instance,
            buildInput(ctree, arguments, strj_sep_comp_0_0.instance.getName()), strContext);

        if(!result.success) {
            throw new ExecException("Call to strj failed:\n" + result.exception, null);
        }

        for(String line : result.errLog.split("\\r\\n|[\\r\\n]")) {
            if(line.contains(StrategoConstants.STRJ_INFO_WRITING_FILE)) {
                String fileName = line.substring(line.indexOf(StrategoConstants.STRJ_INFO_WRITING_FILE)
                    + StrategoConstants.STRJ_INFO_WRITING_FILE.length()).trim();
                BuildStats.generatedJavaFiles.add(fileName);
                execContext.provide(new File(fileName));
            }
        }
        BuildStats.backTaskTime += System.nanoTime() - startTime;

        return None.instance;
    }

    public static StrategoExecutor.ExecutionResult runLocallyUniqueStringStrategy(Logger logger, boolean silent,
        @Nullable IOAgentTracker tracker, Strategy strategy, IStrategoTerm input, StrIncrContext strContext) {
        strContext.resetUsedStringsInFactory();

        final String name = strategy.getName();

        final ITermFactory factory = strContext.getFactory();
        if(tracker != null) {
            strContext.setIOAgent(tracker.agent());
        }
        dr_scope_all_start_0_0.instance.invoke(strContext, factory.makeTuple());

        final long start = System.nanoTime();
        try {
            // We don't use StackSaver because we do not expect that the strategy invoked here will be more recursive
            //  than the already generous stack limit.
            final IStrategoTerm result = strategy.invoke(strContext, input);
            final long time = System.nanoTime() - start;
            if(!silent && result == null) {
                logger.error("Executing " + name + " failed with normal Stratego failure. ", null);
            }
            final String stdout;
            final String stderr;
            if(tracker != null) {
                stdout = tracker.stdout();
                stderr = tracker.stderr();
            } else {
                stdout = "";
                stderr = "";
            }
            return new StrategoExecutor.ExecutionResult(result != null, stdout, stderr, null, time, result, Arrays.asList(strContext.getTrace()));
        } catch(StrategoExit e) {
            final long time = System.nanoTime() - start;
            if(e.getValue() == 0) {
                final String stdout;
                final String stderr;
                if(tracker != null) {
                    stdout = tracker.stdout();
                    stderr = tracker.stderr();
                } else {
                    stdout = "";
                    stderr = "";
                }
                return new StrategoExecutor.ExecutionResult(true, stdout, stderr, e, time, Arrays.asList(strContext.getTrace()));
            }
            strContext.popOnExit(false);
            if(!silent) {
                logger.error("Executing " + name + " failed: ", e);
            }
            final String stdout;
            final String stderr;
            if(tracker != null) {
                stdout = tracker.stdout();
                stderr = tracker.stderr();
            } else {
                stdout = "";
                stderr = "";
            }
            return new StrategoExecutor.ExecutionResult(false, stdout, stderr, e, time, Arrays.asList(strContext.getTrace()));
        } finally {
            dr_scope_all_end_0_0.instance.invoke(strContext, factory.makeTuple());
        }
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

    public static IStrategoList buildInput(Arguments arguments, String name) {
        final List<String> strings = arguments.asStrings(null);
        final IStrategoTerm[] args = new IStrategoTerm[strings.size() + 1];
        args[0] = B.string(name);
        int i = 1;
        for(String string : strings) {
            args[i] = B.string(string);
            i++;
        }
        return B.list(args);
    }

    private IOAgentTracker newResourceTracker(File baseFile, boolean silent, String... excludePatterns) {
        final IOAgentTracker tracker;
        if(silent) {
            tracker = ioAgentTrackerFactory.create(baseFile, new NullOutputStream(), new NullOutputStream());
        } else {
            tracker = ioAgentTrackerFactory.create(baseFile, excludePatterns);
        }
        return tracker;
    }

    @Override public String getId() {
        return id;
    }

    @Override public Serializable key(Input input) {
        return input.strategyName;
    }
}
