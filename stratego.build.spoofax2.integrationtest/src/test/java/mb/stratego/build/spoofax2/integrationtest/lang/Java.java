package mb.stratego.build.spoofax2.integrationtest.lang;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.StringJoiner;

import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.StrategoException;
import org.strategoxt.lang.StrategoExit;

public class Java {
    public static boolean compile(Path dest, Iterable<? extends File> sourceFiles,
        Iterable<? extends File> classPath)
        throws IOException {
        final javax.tools.JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        try(StandardJavaFileManager fileManager = compiler
            .getStandardFileManager(null, Locale.getDefault(), null)) {
            final Iterable<? extends JavaFileObject> compilationUnits =
                fileManager.getJavaFileObjectsFromFiles(sourceFiles);
            fileManager.setLocation(StandardLocation.CLASS_OUTPUT,
                Collections.singletonList(dest.toFile()));
            fileManager.setLocation(StandardLocation.CLASS_PATH, classPath);
            final StringJoiner classPathJoiner = new StringJoiner(":");
            for(File file : classPath) {
                classPathJoiner.add(file.toString());
            }
            final javax.tools.JavaCompiler.CompilationTask task = compiler
                .getTask(null, fileManager, null,
                    Arrays.asList("-classpath", classPathJoiner.toString()), null,
                    compilationUnits);
            return task.call();
        }
    }

    public static boolean execute(Collection<Path> classPaths, String mainClassName)
        throws IOException, ClassNotFoundException, InstantiationException,
        IllegalAccessException, NoSuchMethodException {
        final Path java = Paths.get(System.getProperty("java.home")).resolve("bin/java");
        final URL[] urls = new URL[classPaths.size()];
        int i = 0;
        for(Path classPath : classPaths) {
            urls[i] = classPath.toUri().toURL();
            i++;
        }
        final ClassLoader classLoader = new URLClassLoader(urls);
        final Class<?> mainClass = classLoader.loadClass(mainClassName);
        final Object mainClassObj = mainClass.newInstance();
        final Method mainNoExit = mainClass.getMethod("mainNoExit", String[].class);
        final Object result;
        try {
            result = mainNoExit.invoke(mainClassObj, new Object[] { new String[0] });
        } catch(InvocationTargetException e) {
            Throwable cause = e.getCause();
            if(cause instanceof StrategoExit) {
                return ((StrategoExit) cause).getValue() == 0;
            } else {
                throw (StrategoException) cause;
            }
        }
        return result != null;
    }
}
