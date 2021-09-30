import api.Compiler;
import benchmark.exception.SkipException;
import benchmark.stratego2.compilation.stratego.*;
import benchmark.stratego2.template.benchmark.compilation.CompilationBenchmark;
import joptsimple.internal.Strings;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.metaborg.core.MetaborgException;
import org.openjdk.jmh.annotations.Param;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class SpaceBenchmarks {
    static char delim = ',';

    public static void main(String... args) throws IOException, InstantiationException, IllegalAccessException {
        String resultsFileName = "results-compilespace.csv";
        File resultsFile = FileUtils.getFile(resultsFileName);

        System.out.println("Writing results to " + resultsFile.getAbsolutePath());

        BufferedWriter fileWriter = new BufferedWriter(new FileWriter(resultsFile));

        fileWriter.write(Strings.join(new String[]{"Benchmark", "Param: problemSize", "Param: optimisationLevel", "Score"}, Character.toString(delim)));
        fileWriter.newLine();
        fileWriter.flush();

        int[] optimisationLevels = {2, 3, 4};

        Collection<Class<? extends CompilationBenchmark>> problems = new LinkedList<>(Arrays.asList(
                Benchexpr.class,
                Bubblesort.class,
                Calls.class,
                Factorial.class,
                Fibonacci.class,
                GarbageCollection.class,
                Hanoi.class,
                Mergesort.class,
                Quicksort.class,
                Sieve.class));

        for (int optimisationLevel : optimisationLevels) {
            for (Class<? extends CompilationBenchmark> problemClass : problems) {
                for (int problemSize : Arrays.stream(
                        FieldUtils.getField(problemClass, "problemSize", true)
                                .getAnnotation(Param.class)
                                .value())
                        .mapToInt(Integer::valueOf).toArray()) {
                    System.out.printf("Problem %s (%d); -O %d%n", problemClass.getSimpleName(), problemSize, optimisationLevel);

                    CompilationBenchmark benchmark = problemClass.newInstance();
                    try {
                        benchmark.setMetaborgVersion("2.6.0-SNAPSHOT");
                        benchmark.setOptimisationLevel(optimisationLevel);
                        benchmark.setProblemSize(problemSize);
                        benchmark.setSharedConstructors("on");

                        benchmark.setup();
                        benchmark.removeCompilationResults();

                        Map<String, Long> bms = new HashMap<>();
                        bms.put("Java space", Compiler.javaFiles(benchmark.getProgram().compileStratego()).stream().mapToLong(FileUtils::sizeOf).sum());
                        bms.put("Class space", FileUtils.sizeOfDirectory(benchmark.getProgram().compileJava()));

                        for (Map.Entry<String, Long> bm : bms.entrySet()) {
                            fileWriter.write(problemClass.getName() + "." + bm.getKey());

                            fileWriter.write(delim);
                            fileWriter.write(Integer.toString(problemSize));

                            fileWriter.write(delim);
                            fileWriter.write(Integer.toString(optimisationLevel));

                            fileWriter.write(delim);
                            fileWriter.write(Long.toString(bm.getValue()));

                            fileWriter.newLine();
                            fileWriter.flush();
                        }
                    } catch (MetaborgException | SkipException | IOException e) {
                        e.printStackTrace();
                    } finally {
                        benchmark.removeCompilationResults();
                    }

                }
            }
        }

        fileWriter.close();
    }

}
