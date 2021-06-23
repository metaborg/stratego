package api;

import com.google.common.io.ByteStreams;

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

public class Java {
    public static boolean compile(File dest, Iterable<? extends File> sourceFiles,
                                  Iterable<? extends File> classPath, boolean output)
            throws IOException {
        Files.createDirectories(dest.toPath());

        final javax.tools.JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        try (StandardJavaFileManager fileManager = compiler
                .getStandardFileManager(null, Locale.getDefault(), null)) {
            final Iterable<? extends JavaFileObject> compilationUnits =
                    fileManager.getJavaFileObjectsFromFiles(sourceFiles);
            fileManager.setLocation(StandardLocation.CLASS_OUTPUT,
                    Collections.singletonList(dest));
            fileManager.setLocation(StandardLocation.CLASS_PATH, classPath);

            Writer osw;
            if (output) {
                osw = null;
            } else {
                osw = new OutputStreamWriter(ByteStreams.nullOutputStream());
            }

            final javax.tools.JavaCompiler.CompilationTask task =
                    compiler.getTask(osw, fileManager, null, null, null, compilationUnits);
            return task.call();
        }
    }

    public static boolean execute(String classPath, String mainClass, boolean output) throws IOException, InterruptedException {
        final Path java = Paths.get(System.getProperty("java.home")).resolve(Paths.get("bin", "java"));
        final ProcessBuilder processBuilder = new ProcessBuilder(java.toString(), "-cp", classPath, mainClass);
        final Process process = processBuilder.start();

        if (output) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    System.err.println(line);
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

        int result = process.waitFor();
        return result == 0;
    }
}
