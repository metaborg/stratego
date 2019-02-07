package mb.stratego.build;

import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.None;
import mb.pie.api.STask;
import mb.pie.api.Task;
import mb.pie.api.TaskDef;
import mb.pie.api.fs.stamp.FileSystemStampers;
import mb.pie.api.stamp.output.InconsequentialOutputStamper;
import mb.stratego.build.util.ResourceAgentTracker;
import mb.stratego.build.util.StrategoExecutor;
import mb.stratego.compiler.pack.Packer;

import com.google.inject.Inject;
import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.spoofax.core.SpoofaxConstants;
import org.metaborg.spoofax.core.stratego.ResourceAgent;
import org.metaborg.util.cmd.Arguments;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

public class StrIncrBack implements TaskDef<StrIncrBack.Input, None> {
    public static final class Input implements Serializable {
        final Collection<STask<?>> frontEndTasks;
        final File projectLocation;
        final @Nullable String strategyName;
        final File strategyDir;
        final Collection<File> strategyContributions;
        final Collection<File> constructorsUsed;
        final @Nullable String packageName;
        final File outputPath;
        final @Nullable File cacheDir;
        final Arguments extraArgs;
        final boolean isBoilerplate;

        Input(Collection<STask<?>> frontEndTasks, File projectLocation, @Nullable String strategyName, File strategyDir,
            Collection<File> strategyContributions, Collection<File> constructorsUsed, @Nullable String packageName,
            File outputPath, @Nullable File cacheDir, Arguments extraArgs, boolean isBoilerplate) {
            this.frontEndTasks = frontEndTasks;
            this.projectLocation = projectLocation;
            this.strategyName = strategyName;
            this.strategyDir = strategyDir;
            this.strategyContributions = strategyContributions;
            this.constructorsUsed = constructorsUsed;
            this.packageName = packageName;
            this.outputPath = outputPath;
            this.cacheDir = cacheDir;
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
            if(!constructorsUsed.equals(input.constructorsUsed))
                return false;
            if(packageName != null ? !packageName.equals(input.packageName) : input.packageName != null)
                return false;
            if(!outputPath.equals(input.outputPath))
                return false;
            //noinspection SimplifiableIfStatement
            if(cacheDir != null ? !cacheDir.equals(input.cacheDir) : input.cacheDir != null)
                return false;
            return extraArgs.equals(input.extraArgs);
        }

        @Override public int hashCode() {
            int result = frontEndTasks.hashCode();
            result = 31 * result + projectLocation.hashCode();
            result = 31 * result + (strategyName != null ? strategyName.hashCode() : 0);
            result = 31 * result + strategyDir.hashCode();
            result = 31 * result + strategyContributions.hashCode();
            result = 31 * result + constructorsUsed.hashCode();
            result = 31 * result + (packageName != null ? packageName.hashCode() : 0);
            result = 31 * result + outputPath.hashCode();
            result = 31 * result + (cacheDir != null ? cacheDir.hashCode() : 0);
            result = 31 * result + extraArgs.hashCode();
            result = 31 * result + (isBoilerplate ? 1 : 0);
            return result;
        }
    }

    private static final ILogger logger = LoggerUtils.logger(StrIncrBack.class);

    private final IResourceService resourceService;

    @Inject public StrIncrBack(IResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @Override public None exec(ExecContext execContext, Input input) throws ExecException, InterruptedException {
        for(STask<?> t : input.frontEndTasks) {
            execContext.require(t, InconsequentialOutputStamper.Companion.getInstance());
        }

        long startTime = System.nanoTime();

        final List<Path> contributionPaths = new ArrayList<>(input.strategyContributions.size());
        for(File strategyContrib : input.strategyContributions) {
            execContext.require(strategyContrib, FileSystemStampers.INSTANCE.getHash());
            contributionPaths.add(strategyContrib.toPath());
        }

        final List<Path> overlayPaths = new ArrayList<>(input.constructorsUsed.size());
        for(File overlayFile : input.constructorsUsed) {
            execContext.require(overlayFile, FileSystemStampers.INSTANCE.getHash());
            if(overlayFile.exists()) {
                overlayPaths.add(overlayFile.toPath());
            }
        }

        logger.debug("Hashchecks took: {} ns", System.nanoTime() - startTime);

        // Pack the directory into a single strategy
        final Path packedFile = Paths.get(input.strategyDir.toString(), "packed$.ctree");
        try {
            if(input.isBoilerplate) {
                Packer.packBoilerplate(contributionPaths, packedFile);
            } else {
                Packer.packStrategy(overlayPaths, contributionPaths, packedFile);
            }
        } catch(IOException e) {
            throw new ExecException(e);
        }

        // Call Stratego compiler
        // Note that we need --library and turn off fusion with --fusion for separate compilation
        final Arguments arguments = new Arguments().addFile("-i", packedFile.toFile()).addFile("-o", input.outputPath)
            .addLine(input.packageName != null ? "-p " + input.packageName : "").add("--library").add("--fusion");
        if(input.isBoilerplate) {
            arguments.add("--boilerplate");
        } else {
            arguments.add("--single-strategy");
        }

        if(input.cacheDir != null) {
            arguments.addFile("--cache-dir", input.cacheDir);
        }
        arguments.addAll(input.extraArgs);

        final ResourceAgentTracker tracker =
            newResourceTracker(input.projectLocation, Pattern.quote("[ strj | info ]") + ".*",
                Pattern.quote("[ strj | error ] Compilation failed") + ".*",
                Pattern.quote("[ strj | warning ] Nullary constructor") + ".*",
                Pattern.quote("[ strj | warning ] No Stratego files found in directory") + ".*",
                Pattern.quote("[ strj | warning ] Found more than one matching subdirectory found for") + ".*",
                Pattern.quote(SpoofaxConstants.STRJ_INFO_WRITING_FILE) + ".*",
                Pattern.quote("* warning (escaping-var-id):") + ".*",
                Pattern.quote("          [\"") + ".*" + Pattern.quote("\"]"));

        final StrategoExecutor.ExecutionResult result =
            new StrategoExecutor().withStrjContext().withStrategy(org.strategoxt.strj.main_0_0.instance)
                .withTracker(tracker).withName("strj").setSilent(true).executeCLI(arguments);

        Arrays.stream(result.errLog.split(System.lineSeparator()))
            .filter(line -> line.startsWith(SpoofaxConstants.STRJ_INFO_WRITING_FILE)).forEach(line -> {
            String fileName = line.substring(SpoofaxConstants.STRJ_INFO_WRITING_FILE.length());
            execContext.provide(new File(fileName));
        });

        long buildDuration = System.nanoTime() - startTime;
        logger.debug("Backend task took: {} ns", buildDuration);
        return None.getInstance();
    }

    private ResourceAgentTracker newResourceTracker(File baseFile, String... excludePatterns) {
        final FileObject base = resourceService.resolve(baseFile);
        final ResourceAgentTracker tracker = new ResourceAgentTracker(resourceService, base, excludePatterns);
        final ResourceAgent agent = tracker.agent();
        agent.setAbsoluteWorkingDir(base);
        agent.setAbsoluteDefinitionDir(base);
        return tracker;
    }

    @Override public String getId() {
        return StrIncrBack.class.getCanonicalName();
    }

    @Override public Serializable key(Input input) {
        return input.strategyDir;
    }

    @Override public String desc(Input input) {
        return this.getId() + "(" + input + ")";
    }

    @Override public String desc(Input input, int maxLength) {
        return desc(input);
    }

    @Override public Task<Input, None> createTask(Input input) {
        return new Task<>(this, input);
    }

    @Override public STask<Input> createSerializableTask(Input input) {
        return new STask<>(this.getId(), input);
    }
}
