package mb.stratego.build;

import mb.flowspec.terms.B;
import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.Logger;
import mb.pie.api.None;
import mb.pie.api.STask;
import mb.pie.api.TaskDef;
import mb.stratego.build.util.ResourceAgentTracker;
import mb.stratego.build.util.StrategoExecutor;
import mb.stratego.compiler.pack.Packer;

import com.google.inject.Inject;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.spoofax.core.SpoofaxConstants;
import org.metaborg.spoofax.core.stratego.ResourceAgent;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.metaborg.util.cmd.Arguments;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.StackSaver;
import org.strategoxt.lang.StrategoExit;
import org.strategoxt.lang.Strategy;
import org.strategoxt.stratego_lib.dr_scope_all_end_0_0;
import org.strategoxt.stratego_lib.dr_scope_all_start_0_0;
import org.strategoxt.strj.strj_sep_comp_0_0;
import javax.annotation.Nullable;
import java.io.File;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.SortedMap;

import static org.strategoxt.lang.Term.NO_STRATEGIES;
import static org.strategoxt.lang.Term.NO_TERMS;

public class StrIncrBack implements TaskDef<StrIncrBack.Input, None> {
    public static final String id = StrIncrBack.class.getCanonicalName();

    public static final class Input implements Serializable {
        final Collection<STask> frontEndTasks;
        final File projectLocation;
        final @Nullable String strategyName;
        final File strategyDir;
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

        Input(Collection<STask> frontEndTasks, File projectLocation, @Nullable String strategyName, File strategyDir,
            Collection<IStrategoAppl> strategyContributions, Collection<IStrategoAppl> overlayContributions,
            SortedMap<String, String> ambStrategyResolution, @Nullable String packageName, File outputPath,
            @Nullable File cacheDir, List<String> constants, Collection<File> includeDirs, Arguments extraArgs,
            boolean isBoilerplate) {
            this.frontEndTasks = frontEndTasks;
            this.projectLocation = projectLocation;
            this.strategyName = strategyName;
            this.strategyDir = strategyDir;
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

        @Override public String toString() {
            return "StrIncrBack$Input(" + strategyName + ')';
        }

        @Override public boolean equals(Object o) {
            if(this == o)
                return true;
            if(o == null || getClass() != o.getClass())
                return false;

            Input input = (Input) o;

            if(isBoilerplate != input.isBoilerplate)
                return false;
            if(!frontEndTasks.equals(input.frontEndTasks))
                return false;
            if(!projectLocation.equals(input.projectLocation))
                return false;
            if(strategyName != null ? !strategyName.equals(input.strategyName) : input.strategyName != null)
                return false;
            if(!strategyDir.equals(input.strategyDir))
                return false;
            if(!strategyContributions.equals(input.strategyContributions))
                return false;
            if(!overlayContributions.equals(input.overlayContributions))
                return false;
            if(!ambStrategyResolution.equals(input.ambStrategyResolution))
                return false;
            if(packageName != null ? !packageName.equals(input.packageName) : input.packageName != null)
                return false;
            if(!outputPath.equals(input.outputPath))
                return false;
            if(cacheDir != null ? !cacheDir.equals(input.cacheDir) : input.cacheDir != null)
                return false;
            if(!constants.equals(input.constants))
                return false;
            //noinspection SimplifiableIfStatement
            if(!includeDirs.equals(input.includeDirs))
                return false;
            return extraArgs.equals(input.extraArgs);
        }

        @Override public int hashCode() {
            int result = frontEndTasks.hashCode();
            result = 31 * result + projectLocation.hashCode();
            result = 31 * result + (strategyName != null ? strategyName.hashCode() : 0);
            result = 31 * result + strategyDir.hashCode();
            result = 31 * result + strategyContributions.hashCode();
            result = 31 * result + overlayContributions.hashCode();
            result = 31 * result + ambStrategyResolution.hashCode();
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

    private final IResourceService resourceService;
    private final ITermFactoryService termFactoryService;

    @Inject public StrIncrBack(IResourceService resourceService, ITermFactoryService termFactoryService) {
        this.resourceService = resourceService;
        this.termFactoryService = termFactoryService;
    }

    @Override public None exec(ExecContext execContext, Input input) throws Exception {
        for(STask t : input.frontEndTasks) {
            execContext.require(t);
        }

        final ITermFactory factory = termFactoryService.getGeneric();

        final long startTime = System.nanoTime();
        final IStrategoTerm ctree;
        if(input.isBoilerplate) {
            ctree = Packer.packBoilerplate(factory, input.strategyContributions);
        } else {
            ctree = Packer.packStrategy(factory, input.overlayContributions, input.strategyContributions,
                input.ambStrategyResolution);
        }
//        execContext.logger().debug(
//            "\"BackEnd task packing took\", " + (System.nanoTime() - startTime) + ", \"" + input.projectLocation
//                .toPath().relativize(Paths.get(input.strategyDir.toString(), "packed$.ctree")) + "\"");

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


        final long strategoStartTime = System.nanoTime();
        final StrategoExecutor.ExecutionResult result = runStrj(execContext.logger(), ctree, arguments, true);
//        execContext.logger().debug(
//            "\"BackEnd task stratego code took\", " + (System.nanoTime() - strategoStartTime) + ", \"" + input.projectLocation
//                .toPath().relativize(Paths.get(input.strategyDir.toString(), "packed$.ctree")) + "\"");

        if(!result.success) {
            throw new ExecException("Call to strj failed", result.exception);
        }

        for(String line : result.errLog.split(System.lineSeparator())) {
            if(line.startsWith(SpoofaxConstants.STRJ_INFO_WRITING_FILE)) {
                String fileName = line.substring(SpoofaxConstants.STRJ_INFO_WRITING_FILE.length()).trim();
                execContext.provide(new File(fileName));
            }
        }
        execContext.logger().debug(
            "\"Full BackEnd task took\", " + (System.nanoTime() - strategoStartTime) + ", \"" + input.projectLocation
                .toPath().relativize(Paths.get(input.strategyDir.toString(), "packed$.ctree")) + "\"");

        return None.instance;
    }

    private StrategoExecutor.ExecutionResult runStrj(Logger logger, IStrategoTerm ctree, Arguments arguments, boolean silent) {
        final ResourceAgentTracker tracker = newResourceTracker(new File(System.getProperty("user.dir")), silent);
        final Context context = org.strategoxt.strj.strj.init();
        final Strategy strategy = strj_sep_comp_0_0.instance;
        final String name = "strj-sep-comp";

        final ITermFactory factory = context.getFactory();
        try {
            if(!silent) {
                logger.info("Execute " + name + " " + arguments);
            }
            context.setIOAgent(tracker.agent());
            dr_scope_all_start_0_0.instance.invoke(context, factory.makeTuple());
            IStrategoList input = buildInput(ctree, arguments, name);

            // Launch with a clean operand stack when launched from SSL_java_call, Ant, etc.
            if(new Exception().getStackTrace().length > 20) {
                new StackSaver(strategy).invokeStackFriendly(context, input, NO_STRATEGIES, NO_TERMS);
            } else {
                strategy.invoke(context, input);
            }
            return new StrategoExecutor.ExecutionResult(true, tracker.stdout(), tracker.stderr(), null);
        } catch(StrategoExit e) {
            if(e.getValue() == 0) {
                return new StrategoExecutor.ExecutionResult(true, tracker.stdout(), tracker.stderr(), e);
            }
            context.popOnExit(false);
            if(!silent) {
                logger.error("Executing " + name + " failed: ", e);
            }
            return new StrategoExecutor.ExecutionResult(false, tracker.stdout(), tracker.stderr(), e);
        } finally {
            dr_scope_all_end_0_0.instance.invoke(context, factory.makeTuple());
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

    private ResourceAgentTracker newResourceTracker(File baseFile, boolean silent, String... excludePatterns) {
        final FileObject base = resourceService.resolve(baseFile);
        final ResourceAgentTracker tracker;
        if(silent) {
            tracker = new ResourceAgentTracker(resourceService, base, new NullOutputStream(), new NullOutputStream());
        } else {
            tracker = new ResourceAgentTracker(resourceService, base, excludePatterns);
        }
        final ResourceAgent agent = tracker.agent();
        agent.setAbsoluteWorkingDir(base);
        agent.setAbsoluteDefinitionDir(base);
        return tracker;
    }

    @Override public String getId() {
        return id;
    }

    @Override public Serializable key(Input input) {
        return input.strategyDir;
    }
}
