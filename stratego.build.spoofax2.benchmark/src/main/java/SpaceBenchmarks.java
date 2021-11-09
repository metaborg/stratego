import api.stratego2.Stratego2Compiler;
import benchmark.stratego2.compilation.stratego.*;
import joptsimple.internal.Strings;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.metaborg.core.MetaborgException;
import org.openjdk.jmh.annotations.Param;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

final class SpaceBenchmarks {
    private static final char delim = ',';

    private SpaceBenchmarks() {
    }

    public static void main(String... args) throws IOException, InstantiationException, IllegalAccessException {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd-hhmmss");
        String resultsFileName = String.format("%s_results-compilespace.csv", format.format(new Date()));
        File resultsFile = FileUtils.getFile(resultsFileName);

        System.out.println("Writing results to " + resultsFile.getAbsolutePath());

        BufferedWriter fileWriter = new BufferedWriter(new FileWriter(resultsFile));

        fileWriter.write(Strings.join(new String[]{"Benchmark", "Param: problemSize", "Param: optimisationLevel", "Param: switchImplementation", /*"Param: switchImplementationOrder",*/ "Score"}, Character.toString(delim)));
        fileWriter.newLine();
        fileWriter.flush();

        int[] optimisationLevels = {2, 3, 4};
        String[] switchImplementations = {"", "elseif", "nested-switch", "hash-switch"};
//        String[] switchImplementationOrders = {"", "arity-name"};

        Collection<Class<? extends StrategoCompilationBenchmark>> problems = new LinkedList<>(Arrays.asList(
                Benchexpr.class,
                Benchsym.class,
                Benchtree.class,
                Bubblesort.class,
                Calls.class,
                Factorial.class,
                Fibonacci.class,
                GarbageCollection.class,
                Hanoi.class,
                Mergesort.class,
                Quicksort.class,
                Sieve.class));

        for (Class<? extends StrategoCompilationBenchmark> problemClass : problems) {
            for (int problemSize : Arrays.stream(
                            FieldUtils.getField(problemClass, "problemSize", true)
                                    .getAnnotation(Param.class)
                                    .value())
                    .mapToInt(Integer::valueOf).toArray()) {
                for (int optimisationLevel : optimisationLevels) {
                    for (String switchImplementation : switchImplementations) {
//                        for (String switchImplementationOrder : switchImplementationOrders){
                            System.out.printf("%s (%d); -O %d; switch: %s%n", problemClass.getSimpleName(), problemSize, optimisationLevel, switchImplementation);

                            StrategoCompilationBenchmark benchmark = problemClass.newInstance();
                            try {
                                benchmark.setMetaborgVersion("2.6.0-SNAPSHOT");
                                benchmark.setOptimisationLevel(optimisationLevel);
                                benchmark.setProblemSize(problemSize);
                                benchmark.setSharedConstructors("on");
                                benchmark.setSwitchImplementation(switchImplementation);
//                                benchmark.setSwitchImplementationOrder(switchImplementationOrder);

                                benchmark.setup();

                                Map<String, Long> bms = new HashMap<>();
                                bms.put("Java space", Stratego2Compiler.javaFiles(benchmark.getProgram().compileStratego()).stream().mapToLong(FileUtils::sizeOf).sum());
                                bms.put("Class space", FileUtils.sizeOfDirectory(benchmark.getProgram().compileJava()));

                                for (Map.Entry<String, Long> bm : bms.entrySet()) {
                                    fileWriter.write(problemClass.getName() + "." + bm.getKey());

                                    fileWriter.write(delim);
                                    fileWriter.write(Integer.toString(problemSize));

                                    fileWriter.write(delim);
                                    fileWriter.write(Integer.toString(optimisationLevel));

                                    fileWriter.write(delim);
                                    fileWriter.write(switchImplementation);

//                                    fileWriter.write(delim);
//                                    fileWriter.write(switchImplementationOrder);

                                    fileWriter.write(delim);
                                    fileWriter.write(Long.toString(bm.getValue()));

                                    fileWriter.newLine();
                                    fileWriter.flush();
                                }
                            } catch (IOException | MetaborgException e) {
                                e.printStackTrace();
                            } finally {
                                benchmark.teardown();
                            }

                        }
                    }
//                }
            }
        }

        fileWriter.close();
    }

}
