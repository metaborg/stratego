package benchmark.til;

import api.til.TILProgram;
import org.metaborg.core.MetaborgException;
import org.openjdk.jmh.annotations.*;
import benchmark.til.problems.ExecutableTILProblem;

import java.util.concurrent.TimeUnit;

import static benchmark.til.TILBenchmarks.initProgram;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 0)
@Measurement(iterations = 1)
@Fork(value = 1, jvmArgs = {"-Xss16M", "-Xms4G", "-Xmx4G"})
@Timeout(time = 1, timeUnit = TimeUnit.MINUTES)
public class TILExecutionBenchmarks {
    @Param({
            "Add100",
//            "Add200",
//            "Add500",
//            "Add1000",
//            "EBlock",
//            "Factorial4",
//            "Factorial5",
//            "Factorial6",
//            "Factorial7",
//            "Factorial8",
//            "Factorial9",
    })
    public ExecutableTILProblem problem;

    @Param({"3"})
    public int optimisationLevel = -1;

    public TILProgram program;

    @Setup(Level.Trial)
    public void setup() {
        program = initProgram(problem, optimisationLevel);
    }

//    @Benchmark
    public final String run() throws MetaborgException {
        return program.run(problem.input);
    }

    @TearDown(Level.Trial)
    public final void teardown() {
        if (program != null) {
            program.cleanup();
        }
    }
}
