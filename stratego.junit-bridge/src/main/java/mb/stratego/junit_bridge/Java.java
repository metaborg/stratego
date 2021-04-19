package mb.stratego.junit_bridge;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

public class Java {
    public static boolean compile(Path dest, Iterable<? extends File> sourceFiles,
        Iterable<? extends File> classPath)
        throws IOException {
        final javax.tools.JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        try(StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null)) {
            final Iterable<? extends JavaFileObject> compilationUnits =
                fileManager.getJavaFileObjectsFromFiles(sourceFiles);
            fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Collections.singletonList(dest.toFile()));
            fileManager.setLocation(StandardLocation.CLASS_PATH, classPath);
            final javax.tools.JavaCompiler.CompilationTask task =
                compiler.getTask(null, fileManager, null, null, null, compilationUnits);
            return task.call();
        }
    }

    public static boolean execute(String classPath, String mainClass) throws IOException, InterruptedException {
        final Path java = Paths.get(System.getProperty("java.home")).resolve("bin/java");
        final ProcessBuilder processBuilder = new ProcessBuilder(java.toString(), "-cp", classPath, mainClass);
        //        processBuilder.redirectErrorStream(true);
        final Process process = processBuilder.start();
        try(BufferedReader br = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            String line;
            while((line = br.readLine()) != null) {
                System.err.println(line);
            }
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
        final int result = process.waitFor();
        return result == 0;
    }
}
