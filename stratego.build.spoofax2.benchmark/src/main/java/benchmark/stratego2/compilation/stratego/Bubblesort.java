package benchmark.stratego2.compilation.stratego;

import benchmark.stratego2.problem.BubblesortProblem;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Timeout;

import java.util.concurrent.TimeUnit;

@Timeout(time = 30, timeUnit = TimeUnit.MINUTES)
public class Bubblesort extends StrategoCompilationBenchmark implements BubblesortProblem { }
