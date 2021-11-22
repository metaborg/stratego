package benchmark.generic;

import benchmark.exception.InvalidConfigurationException;
import benchmark.exception.SkipException;
import org.apache.commons.lang3.NotImplementedException;
import org.metaborg.core.MetaborgException;
import org.metaborg.util.cmd.Arguments;
import org.openjdk.jmh.annotations.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("JmhInspections")
@State(Scope.Thread)
@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(value = 3, jvmArgs = {"-Xss16M", "-Xms4G", "-Xmx4G"})
public abstract class BaseBenchmark<P extends Program<?>> implements Problem {

    protected Path sourcePath;

    protected P program;
    protected Arguments args;

//    @Param({"2.6.0-SNAPSHOT"})
    public String metaborgVersion = "2.6.0-SNAPSHOT";

    @Param({"2"})
    public int optimisationLevel;

    @Param({""})
    public String switchImplementation;

//    @Param({""})
//    public String switchImplementationOrder;

    @Param({"on"})
    public String sharedConstructors;

    @Param({"-1"})
    public int problemSize;

//    @Param({"on", "off"})
//    public String fusion;

    public final void setMetaborgVersion(String metaborgVersion) {
        this.metaborgVersion = metaborgVersion;
    }

    public final void setOptimisationLevel(int optimisationLevel) {
        this.optimisationLevel = optimisationLevel;
    }

    public final void setSharedConstructors(String sharedConstructors) {
        this.sharedConstructors = sharedConstructors;
    }

    public final void setSwitchImplementation(String switchImplementation) {
        this.switchImplementation = switchImplementation;
    }

//    public void setSwitchImplementationOrder(String switchImplementationOrder) {
//        this.switchImplementationOrder = switchImplementationOrder;
//    }

    public final void setProblemSize(int problemSize) {
        this.problemSize = problemSize;
    }

    /**
     * @throws SkipException
     * @throws InvalidConfigurationException
     */
    @Setup(Level.Trial)
    public void setup() {
        args = new Arguments();
        if (4 == optimisationLevel) {
            if (Objects.equals(switchImplementation, "")) {
                throw new InvalidConfigurationException("No switch implementation set on -O 4");
            } /*else if ((Objects.equals(switchImplementation, "nested-switch") && Objects.equals(switchImplementationOrder, ""))
                    || (Objects.equals(switchImplementation, "hash-switch") && !Objects.equals(switchImplementationOrder, ""))
                    || (Objects.equals(switchImplementation, "elseif") && !Objects.equals(switchImplementationOrder, ""))
            ) {
                throw new InvalidConfigurationException("Invalid combination of switch implementation and switch implementation order.");
            }*/

            args.add("--pmc:switchv", switchImplementation);

//            if (Objects.equals(switchImplementation, "nested-switch")) {
//                args.add("--pmc:switchv-order", switchImplementationOrder);
//            }
        } else {
            if (!Objects.equals(switchImplementation, "") /*|| !Objects.equals(switchImplementationOrder, "")*/)
                throw new InvalidConfigurationException("Switch implementation set, but not on -O 4");
        }

        sourcePath = Paths.get("src", "main", "resources", languageSubFolder(), sourceFileName());

        args.add("-O", optimisationLevel);
        args.add("-sc", sharedConstructors);
//        args.add("--fusion", fusion);
//        args.add("--statistics", 1);
//        args.add("--verbose", 3);

        try {
            instantiateProgram();
        } catch (MetaborgException | IOException e) {
            handleException(e);
        }
    }

    @TearDown(Level.Trial)
    public void teardown() {
        getProgram().cleanup();
    }

    protected abstract void instantiateProgram() throws MetaborgException, IOException;

    /**
     * @param e
     * @throws SkipException
     */
    protected final void handleException(Exception e) {
        if (e instanceof FileNotFoundException) {
            throw new SkipException(String.format("Benchmark problem file %s does not exist! Skipping.", sourcePath), e);
        } else if (e instanceof IOException) {
            throw new SkipException("Exception while creating temporary intermediate directory! Skipping.", e);
        } else if (e instanceof MetaborgException) {
            throw new SkipException("Exception in build system! Skipping.", e);
        }
    }

    public final P getProgram() {
        return program;
    }

    protected final String sourceFileName() {
        return String.format(problemFileNamePattern(), problemSize);
    }

    protected abstract String languageSubFolder();

}
