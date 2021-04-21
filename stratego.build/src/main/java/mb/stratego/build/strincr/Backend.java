package mb.stratego.build.strincr;

import java.io.File;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;

import mb.resource.ResourceKeyString;
import org.metaborg.util.cmd.Arguments;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.util.B;
import org.strategoxt.strj.strj_sep_comp_0_0;

import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.log.api.Logger;
import mb.pie.api.None;
import mb.pie.api.TaskDef;
import mb.resource.hierarchical.ResourcePath;
import mb.stratego.build.termvisitors.TermSize;
import mb.stratego.build.util.IOAgentTrackerFactory;
import mb.stratego.build.util.StrIncrContext;
import mb.stratego.build.util.StrategoConstants;
import mb.stratego.build.util.StrategoExecutor;
import mb.stratego.compiler.pack.Packer;

public class Backend implements TaskDef<Backend.Input, None> {
    public static final String id = Backend.class.getCanonicalName();

    public static final class Input implements Serializable {
        final ResourcePath projectLocation;
        final String strategyName;
        final Collection<IStrategoTerm> constructors;
        final Collection<IStrategoAppl> strategyContributions;
        final Collection<IStrategoAppl> overlayContributions;
        final @Nullable String packageName;
        final ResourcePath outputPath;
        final @Nullable ResourcePath cacheDir;
        final List<String> constants;
        final Collection<ResourcePath> includeDirs;
        final Arguments extraArgs;
        final boolean isBoilerplate;

        Input(ResourcePath projectLocation, @Nullable String strategyName, Collection<IStrategoTerm> constructors, Collection<IStrategoAppl> strategyContributions,
            Collection<IStrategoAppl> overlayContributions,
            @Nullable String packageName, ResourcePath outputPath, @Nullable ResourcePath cacheDir, List<String> constants, Collection<ResourcePath> includeDirs, Arguments extraArgs,
            boolean isBoilerplate) {
            this.projectLocation = projectLocation;
            this.strategyName = strategyName == null ? "" : strategyName;
            this.constructors = constructors;
            this.strategyContributions = strategyContributions;
            this.overlayContributions = overlayContributions;
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
            if(isBoilerplate) {
                return "StrIncrBack$Input[boilerplate]";
            }
            return "StrIncrBack$Input(" + strategyName + ')';
        }

        @Override public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            final Input input = (Input) o;
            if(isBoilerplate != input.isBoilerplate) return false;
            if(!projectLocation.equals(input.projectLocation)) return false;
            if(!strategyName.equals(input.strategyName)) return false;
            if(!constructors.equals(input.constructors)) return false;
            if(!strategyContributions.equals(input.strategyContributions)) return false;
            if(!overlayContributions.equals(input.overlayContributions)) return false;
            if(packageName != null ? !packageName.equals(input.packageName) : input.packageName != null) return false;
            if(!outputPath.equals(input.outputPath)) return false;
            if(cacheDir != null ? !cacheDir.equals(input.cacheDir) : input.cacheDir != null) return false;
            if(!constants.equals(input.constants)) return false;
            if(!includeDirs.equals(input.includeDirs)) return false;
            return extraArgs.equals(input.extraArgs);
        }

        @Override public int hashCode() {
            int result = projectLocation.hashCode();
            result = 31 * result + strategyName.hashCode();
            result = 31 * result + constructors.hashCode();
            result = 31 * result + strategyContributions.hashCode();
            result = 31 * result + overlayContributions.hashCode();
            result = 31 * result + (packageName != null ? packageName.hashCode() : 0);
            result = 31 * result + outputPath.hashCode();
            result = 31 * result + (cacheDir != null ? cacheDir.hashCode() : 0);
            result = 31 * result + constants.hashCode();
            result = 31 * result + includeDirs.hashCode();
            result = 31 * result + extraArgs.hashCode();
            result = 31 * result + (isBoilerplate ? 1 : 0);
            return result;
        }
    }

    public interface ResourcePathConverter {
        String toString(ResourcePath resourcePath);
    }

    private final ITermFactory termFactory;
    private final IOAgentTrackerFactory ioAgentTrackerFactory;
    private final StrIncrContext strContext;
    private final ResourcePathConverter resourcePathConverter;

    @Inject public Backend(ITermFactory termFactory, IOAgentTrackerFactory ioAgentTrackerFactory, StrIncrContext strContext, ResourcePathConverter resourcePathConverter) {
        this.termFactory = termFactory;
        this.ioAgentTrackerFactory = ioAgentTrackerFactory;
        this.strContext = strContext;
        this.resourcePathConverter = resourcePathConverter;
    }

    @Override public None exec(ExecContext execContext, Input input) throws Exception {
        BuildStats.executedBackTasks++;

        final long startTime = System.nanoTime();
        final IStrategoTerm ctree;
        if(input.isBoilerplate) {
            ctree = Packer.packBoilerplate(termFactory, input.constructors, input.strategyContributions);
        } else {
            ctree = Packer.packStrategy(termFactory, input.overlayContributions, input.strategyContributions);
        }
        BuildStats.strategyBackendCTreeSize.put(input.strategyName, TermSize.computeTermSize(ctree));

        // Call Stratego compiler
        // Note that we need --library and turn off fusion with --fusion for separate compilation
        final Arguments arguments = new Arguments().add("-i", "passedExplicitly.ctree").add("-o", resourcePathConverter.toString(input.outputPath))
            //            .add("--verbose", 3)
            .addLine(input.packageName != null ? "-p " + input.packageName : "").add("--library").add("--fusion");
        if(input.isBoilerplate) {
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
            // Needed in boilerplate for generating a strategy (e.g. $C$O$N$S$T$A$N$T_0_0), needed in single-strategy
            //  to turn e.g. prim("CONSTANT") into Build(theconstantvalue), in the example where you give pass
            //  -DCONSTANT=theconstantvalue.
            arguments.add("-D", constant);
        }
        arguments.addAll(input.extraArgs);


        final StrategoExecutor.ExecutionResult result = StrategoExecutor.runLocallyUniqueStringStrategy(
            ioAgentTrackerFactory, execContext.logger(), true, strj_sep_comp_0_0.instance,
            buildInput(ctree, arguments, strj_sep_comp_0_0.instance.getName()), strContext);

        if(!result.success) {
            throw new ExecException("Call to strj failed:\n" + result.exception, null);
        }

        for(String line : result.errLog.split("\\r\\n|[\\r\\n]")) {
            if(line.contains(StrategoConstants.STRJ_INFO_WRITING_FILE)) {
                String fileName = line.substring(line.indexOf(StrategoConstants.STRJ_INFO_WRITING_FILE)
                    + StrategoConstants.STRJ_INFO_WRITING_FILE.length()).trim();
                BuildStats.generatedJavaFiles.add(fileName);
                execContext.provide(execContext.getResourceService().getHierarchicalResource(ResourceKeyString.parse(fileName)));
            }
        }
        BuildStats.backTaskTime += System.nanoTime() - startTime;

        return None.instance;
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

    @Override public String getId() {
        return id;
    }

    @Override public Serializable key(Input input) {
        return input.strategyName;
    }
}
