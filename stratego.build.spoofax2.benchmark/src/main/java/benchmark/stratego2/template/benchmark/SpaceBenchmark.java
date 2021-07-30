package benchmark.stratego2.template.benchmark;

import benchmark.exception.SkipException;
import org.apache.commons.io.FileUtils;
import org.metaborg.core.MetaborgException;
import org.openjdk.jmh.annotations.*;

import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

@Measurement(iterations = 1)
@Fork(value = 1, warmups = 0)
@BenchmarkMode(Mode.SingleShotTime)
public abstract class SpaceBenchmark extends OptimisationBenchmark {

    @Setup(Level.Trial)
    public void compile() throws MetaborgException, IOException, SkipException {
        getProgram().compileStratego();
        getProgram().compileJava();
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.SECONDS) // kilobytes
    final public long javaSpace() throws InterruptedException {
        long size = FileUtils.sizeOfDirectory(getProgram().compiler.javaDir);
        Thread.sleep(size);
        return size;
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.SECONDS) // kilobytes
    final public long classesSpace() throws InterruptedException {
        long start = System.currentTimeMillis();
        long size = FileUtils.sizeOfDirectory(getProgram().compiler.classDir);
        long elapsed = System.currentTimeMillis() - start;
        Thread.sleep(size - elapsed);
        return size;
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.SECONDS)
    final public long javaBytes() throws IOException, InterruptedException {
        long bytes = Files.walk(getProgram().compiler.javaDir.toPath()).mapToLong(f -> f.toFile().length()).sum();
        Thread.sleep(bytes);
        return bytes;
    }

    @Benchmark
    @OutputTimeUnit(TimeUnit.SECONDS)
    final public long classBytes() throws IOException, InterruptedException {
        long bytes = Files.walk(getProgram().compiler.classDir.toPath()).mapToLong(f -> f.toFile().length()).sum();
        Thread.sleep(bytes);
        return bytes;
    }

}
