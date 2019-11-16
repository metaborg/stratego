package mb.stratego.build.strincr;

import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;

import org.apache.commons.io.filefilter.SuffixFileFilter;
import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public final class Module implements Serializable {
    public enum Type {
        library, source
    }

    public final String path;
    public final Type type;

    private Module(String path, Type type) {
        this.path = path;
        this.type = type;
    }

    static Module library(String path) {
        return new Module(path, Type.library);
    }

    /**
     * Create source module with a normalized, relative path from the projectLocation to the module file. This
     * should give us a unique string to use to identify the module file within this pipeline.
     */
    public static Module source(Path projectLocationPath, Path path) {
        return new Module(projectLocationPath.relativize(path.toAbsolutePath().normalize()).toString(),
            Type.source);
    }

    static Set<Module> resolveWildcards(ExecContext execContext, Collection<Frontend.Import> imports,
        Collection<File> includeDirs, Path projectLocation) throws ExecException, IOException {
        final Set<Module> result = new HashSet<>(imports.size() * 2);
        for(Frontend.Import anImport : imports) {
            switch(anImport.type) {
                case normal: {
                    boolean foundSomethingToImport = false;
                    for(File dir : includeDirs) {
                        final Path strPath = dir.toPath().resolve(anImport.path + ".str");
                        final Path rtreePath = dir.toPath().resolve(anImport.path + ".rtree");
                        if(Files.exists(rtreePath)) {
                            foundSomethingToImport = true;
                            if(isLibraryRTree(rtreePath)) {
                                result.add(Module.library(rtreePath.toString()));
                            } else {
                                result.add(Module.source(projectLocation, rtreePath));
                            }
                        } else if(Files.exists(strPath)) {
                            foundSomethingToImport = true;
                            result.add(Module.source(projectLocation, strPath));
                        }
                    }
                    if(!foundSomethingToImport) {
                        execContext.logger()
                            .warn("Could not find any module corresponding to import " + anImport.path, null);
                    }
                    break;
                }
                case wildcard: {
                    boolean foundSomethingToImport = false;
                    for(File dir : includeDirs) {
                        final Path path = dir.toPath().resolve(anImport.path);
                        execContext.require(path);
                        if(Files.exists(path)) {
                            final @Nullable File[] strFiles = path.toFile()
                                .listFiles((FilenameFilter) new SuffixFileFilter(Arrays.asList(".str", ".rtree")));
                            if(strFiles == null) {
                                throw new ExecException(
                                    "Reading file list in directory failed for directory: " + path);
                            }
                            for(File strFile : strFiles) {
                                foundSomethingToImport = true;
                                Path p = strFile.toPath();
                                result.add(Module.source(projectLocation, p));
                            }
                        }
                    }
                    if(!foundSomethingToImport) {
                        execContext.logger()
                            .warn("Could not find any module corresponding to import " + anImport.path + "/-",
                                null);
                    }
                    break;
                }
                case library: {
                    result.add(Module.library(anImport.path));
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Check if file starts with Specification/1 instead of Module/2
     *
     * @param rtreePath Path to the file
     * @return if file starts with Specification/1
     * @throws IOException on file system trouble
     */
    private static boolean isLibraryRTree(Path rtreePath) throws IOException {
        char[] chars = new char[4];
        BufferedReader r = Files.newBufferedReader(rtreePath);
        return r.read(chars) != -1 && Arrays.equals(chars, "Spec".toCharArray());
    }

    String resolveFrom(Path projectLocation) throws MalformedURLException {
        return projectLocation.resolve(path).normalize().toAbsolutePath().toString();
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        Module module = (Module) o;

        //noinspection SimplifiableIfStatement
        if(!path.equals(module.path))
            return false;
        return type == module.type;
    }

    @Override public int hashCode() {
        int result = path.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }

    @Override public String toString() {
        return "Module(" + path + ", " + type + ')';
    }
}
