package benchmark.til;

import api.til.TILProgram;
import org.metaborg.core.MetaborgException;
import org.openjdk.jmh.annotations.*;
import benchmark.til.problems.ExecutableTILProblem;

import java.util.concurrent.TimeUnit;

import static benchmark.til.TILBenchmarkUtil.initProgram;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 0)
@Measurement(iterations = 1)
@Fork(value = 1, jvmArgs = {"-Xss16M", "-Xms4G", "-Xmx4G"})
@Timeout(time = 1, timeUnit = TimeUnit.MINUTES)
public class TILExecutionBenchmark {
    @Param({
            "Add_100",
//            "Add_200",
//            "Add_500",
//            "Add_1000",
//            "EBlock",
//            "Factorial_4",
//            "Factorial_5",
//            "Factorial_6",
//            "Factorial_7",
//            "Factorial_8",
//            "Factorial_9",
    })
    public ExecutableTILProblem problem;

    @Param({"2"})
    public int optimisationLevel = -1;

    public TILProgram program;

    @Setup(Level.Trial)
    public void setup() {
        program = initProgram(problem, optimisationLevel);
    }

//    @Benchmark
    public final String executeTIL() throws MetaborgException {
        return program.run(problem.input);
    }

    @TearDown(Level.Trial)
    public final void teardown() {
        if (program != null) {
            program.cleanup();
        }
    }
}
