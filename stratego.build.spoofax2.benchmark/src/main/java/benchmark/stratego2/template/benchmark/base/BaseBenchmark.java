package benchmark.stratego2.template.benchmark.base;

import api.Stratego2Program;
import benchmark.exception.InvalidConfigurationException;
import benchmark.exception.SkipException;
import benchmark.stratego2.template.problem.Problem;
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
@Fork(value = 3, jvmArgs = {"-Xss16M", "-Xms4G", "-Xmx4G"})
@OutputTimeUnit(TimeUnit.SECONDS)
public abstract class BaseBenchmark implements Problem {

    protected Path sourcePath;

    protected Stratego2Program program;
    protected final Arguments args = new Arguments();

    @Param({"2.6.0-SNAPSHOT"})
    public String metaborgVersion;

    @Param({"2"})
    public int optimisationLevel;

    @Param({""})
    public String switchImplementation;

    @Param({""})
    public String switchImplementationOrder;

    @Param({"on"})
    public String sharedConstructors;

    @Param({"-1"})
    public int problemSize;

//    @Param({"on", "off"})
//    public String fusion;

    public BaseBenchmark() {}

    public BaseBenchmark(String metaborgVersion, int optimisationLevel, int problemSize, String sharedConstructors) {
        this.metaborgVersion = metaborgVersion;
        this.optimisationLevel = optimisationLevel;
        this.sharedConstructors = sharedConstructors;
        this.problemSize = problemSize;
    }

    public final void setMetaborgVersion(String metaborgVersion) {
        this.metaborgVersion = metaborgVersion;
    }

    public final void setOptimisationLevel(int optimisationLevel) {
        this.optimisationLevel = optimisationLevel;
    }

    public final void setSharedConstructors(String sharedConstructors) {
        this.sharedConstructors = sharedConstructors;
    }

    public void setSwitchImplementation(String switchImplementation) {
        this.switchImplementation = switchImplementation;
    }

    public void setSwitchImplementationOrder(String switchImplementationOrder) {
        this.switchImplementationOrder = switchImplementationOrder;
    }

    public final void setProblemSize(int problemSize) {
        this.problemSize = problemSize;
    }

    @Setup(Level.Trial)
    public void setup() throws SkipException, InvalidConfigurationException {
        if (optimisationLevel == 4) {
            if (Objects.equals(switchImplementation, "")) {
                throw new InvalidConfigurationException("No switch implementation set on -O 4");
            } else if ((Objects.equals(switchImplementation, "nested-switch") && Objects.equals(switchImplementationOrder, ""))
                    || (Objects.equals(switchImplementation, "hash-switch") && !Objects.equals(switchImplementationOrder, ""))
                    || (Objects.equals(switchImplementation, "elseif") && !Objects.equals(switchImplementationOrder, ""))
            ) {
                throw new InvalidConfigurationException("Invalid combination of switch implementation and switch implementation order.");
            }

            args.add("--pmc:switchv", switchImplementation);

            if (Objects.equals(switchImplementation, "nested-switch")) {
                args.add("--pmc:switchv-order", switchImplementationOrder);
            }
        } else {
            if (!Objects.equals(switchImplementation, "") || !Objects.equals(switchImplementationOrder, ""))
                throw new InvalidConfigurationException("Switch implementation set, but not on -O 4");
        }

        sourcePath = Paths.get("src", "main", "resources", sourceFileName());

        args.add("-O", optimisationLevel);
        args.add("-sc", sharedConstructors);
//        args.add("--fusion", fusion);
//        args.add("--statistics", 1);
//        args.add("--verbose", 3);

        try {
            program = new Stratego2Program(sourcePath, args, metaborgVersion);
        } catch (FileNotFoundException e) {
            throw new SkipException(String.format("Benchmark problem file %s does not exist! Skipping.", sourcePath), e);
        } catch (IOException e) {
            throw new SkipException("Exception while creating temporary intermediate directory! Skipping.", e);
        } catch (MetaborgException e) {
            throw new SkipException("Exception in build system! Skipping.", e);
        }
    }

    @TearDown(Level.Trial)
    public void teardown() {
        getProgram().cleanup();
    }

    public Stratego2Program getProgram() {
        return program;
    }

    public String sourceFileName() {
        return String.format(problemFileNamePattern(), problemSize);
    }

}
