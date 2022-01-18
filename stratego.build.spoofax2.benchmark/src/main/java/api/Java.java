package api;

import com.google.common.io.ByteStreams;
import org.jetbrains.annotations.Nullable;

import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Locale;
import java.util.stream.Collectors;

public final class Java {
    private Java() {
    }

    public static boolean compile(File dest, Iterable<? extends File> sourceFiles,
                                  Iterable<? extends File> classPath, boolean output)
            throws IOException {
        Files.createDirectories(dest.toPath());

        javax.tools.JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        try (StandardJavaFileManager fileManager = compiler
                .getStandardFileManager(null, Locale.getDefault(), null)) {
            Iterable<? extends JavaFileObject> compilationUnits =
                    fileManager.getJavaFileObjectsFromFiles(sourceFiles);
            fileManager.setLocation(StandardLocation.CLASS_OUTPUT,
                    Collections.singletonList(dest));
            fileManager.setLocation(StandardLocation.CLASS_PATH, classPath);

            @Nullable Writer osw;
            if (output) {
                osw = null;
            } else {
                //noinspection UnstableApiUsage
                osw = new OutputStreamWriter(ByteStreams.nullOutputStream());
            }

            javax.tools.JavaCompiler.CompilationTask task =
                    compiler.getTask(osw, fileManager, null, null, null, compilationUnits);
            return task.call();
        }
    }

    public static BufferedReader execute(String classPath, String mainClass, String arg) throws IOException, InterruptedException {
        Path java = Paths.get(System.getProperty("java.home")).resolve(Paths.get("bin", "java"));
        ProcessBuilder processBuilder = new ProcessBuilder(java.toString(), "-cp", classPath, "-ss16M", "-ms2G", "-mx2G", mainClass, arg);
        Process process = processBuilder.start();

        BufferedReader r = new BufferedReader(new InputStreamReader(process.getErrorStream()));

        if (0 != process.waitFor()) {
            throw new RuntimeException("Process did not finish successfully!\nErrors: " + r.lines().collect(Collectors.joining()));
        }

        return r;
    }
}
