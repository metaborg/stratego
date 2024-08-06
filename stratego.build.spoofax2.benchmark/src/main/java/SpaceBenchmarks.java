import api.stratego2.Stratego2Compiler;
import api.stratego2.Stratego2Program;
import benchmark.stratego2.problems.ExecutableStr2Problem;
import joptsimple.internal.Strings;
import org.apache.commons.io.FileUtils;
import org.metaborg.core.MetaborgException;
import org.metaborg.util.cmd.Arguments;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

import static benchmark.stratego2.problems.ExecutableStr2Problem.*;

final class SpaceBenchmarks {
    private static final char delim = ',';

    private SpaceBenchmarks() {
    }

    public static void main(String... args) throws IOException {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd-hhmmss");
        String resultsFileName = String.format("%s_results-compilespace.csv", format.format(new Date()));
        File resultsFile = FileUtils.getFile(resultsFileName);

        System.out.println("Writing results to " + resultsFile.getAbsolutePath());

        BufferedWriter fileWriter = new BufferedWriter(new FileWriter(resultsFile));

        fileWriter.write(Strings.join(new String[]{"Benchmark", "Param: problemSize", "Param: optimisationLevel", "Score"}, Character.toString(delim)));
        fileWriter.newLine();
        fileWriter.flush();

        int[] optimisationLevels = {2, 3, 4};

        Collection<ExecutableStr2Problem> problems = new LinkedList<>(Arrays.asList(
                Benchexpr_10,
                Benchsym_10,
                Benchtree_10,
                Bubblesort_10,
                Calls,
                Factorial_4,
                Fibonacci_18,
                GarbageCollection,
                Hanoi_4,
                Mergesort_10,
                Quicksort_10,
                Sieve_20));

        for (ExecutableStr2Problem problem : problems) {
            for (int optimisationLevel : optimisationLevels) {
                System.out.printf("%s (%s); -O %d%n", problem.name, problem.input, optimisationLevel);
                Stratego2Program program = null;
                try {
                    Path sourcePath = Paths.get("src", "main", "resources", "stratego2", problem.name + ".str2");
                    String MetaborgVersion = "2.6.0-SNAPSHOT";
                    Arguments str2Args = new Arguments();
                    str2Args.add("-O", optimisationLevel);
                    str2Args.add("-sc", "on");
                    program = new Stratego2Program(sourcePath, str2Args, MetaborgVersion);

                    Map<String, Long> bms = new HashMap<>();
                    bms.put("Java space", Stratego2Compiler.javaFiles(program.compileStratego()).stream().mapToLong(FileUtils::sizeOf).sum());
                    bms.put("Class space", FileUtils.sizeOfDirectory(program.compileJava()));

                    for (Map.Entry<String, Long> bm : bms.entrySet()) {
                        fileWriter.write(problem.name + "." + bm.getKey());

                        fileWriter.write(delim);
                        fileWriter.write("");

                        fileWriter.write(delim);
                        fileWriter.write(Integer.toString(optimisationLevel));

                        fileWriter.write(delim);
                        fileWriter.write(Long.toString(bm.getValue()));

                        fileWriter.newLine();
                        fileWriter.flush();
                    }
                } catch (IOException | MetaborgException e) {
                    e.printStackTrace();
                } finally {
                    if (program != null) {
                        program.cleanup();
                    }
                }

            }
        }

        fileWriter.close();
    }

}
