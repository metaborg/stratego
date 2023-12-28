package renzo.stratego2;

import api.stratego2.Stratego2Program;
import mb.stratego.build.strincr.task.output.CompileOutput;
import org.metaborg.core.MetaborgException;
import org.openjdk.jmh.annotations.*;
import renzo.stratego2.problems.ExecutableProblem;

import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 0)
@Measurement(iterations = 1)
@Fork(value = 1, jvmArgs = {"-Xss16M", "-Xms4G", "-Xmx4G"})
public class ExecutionBenchmarks {

    Stratego2Program program;

    @Param({
            "Benchexpr10",
//            "Benchexpr11",
//            "Benchexpr12",
//            "Benchexpr13",
//            "Benchexpr14",
//            "Benchexpr15",
//            "Benchexpr16",
//            "Benchexpr17",
//
//            "Benchsym10",
//            "Benchsym11",
//            "Benchsym12",
//            "Benchsym13",
//            "Benchsym14",
//            "Benchsym15",
//            "Benchsym16",
//            "Benchsym17",
//            "Benchsym18",
//
//            "Benchtree2",
//            "Benchtree4",
//            "Benchtree6",
//
//            "Bubblesort10",
//            "Bubblesort20",
//            "Bubblesort50",
//            "Bubblesort100",
//            "Bubblesort200",
//
//            "Calls",
//
//            "Factorial4",
//            "Factorial5",
//            "Factorial6",
//            "Factorial7",
//
//            "Fibonacci18",
//            "Fibonacci19",
//            "Fibonacci20",
//            "Fibonacci21",
//
//            "GarbageCollection",
//
//            "Hanoi4",
//            "Hanoi5",
//            "Hanoi6",
//            "Hanoi7",
//            "Hanoi8",
//            "Hanoi9",
//            "Hanoi10",
//            "Hanoi11",
//
//            "Mergesort10",
//            "Mergesort20",
//            "Mergesort30",
//            "Mergesort40",
//
//            "Quicksort10",
//            "Quicksort12",
//            "Quicksort14",
//            "Quicksort16",
//            "Quicksort18",
//            "Quicksort20",
//
//            "Sieve20",
//            "Sieve40",
//            "Sieve60",
//            "Sieve80",
//            "Sieve100",
    })
    ExecutableProblem problem;

    @Benchmark
    public final CompileOutput compileStratego() throws MetaborgException {
        return program.compileStratego();
    }
}
