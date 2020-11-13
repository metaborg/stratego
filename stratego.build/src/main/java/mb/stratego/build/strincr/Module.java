package mb.stratego.build.strincr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.ReadableResource;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.PathResourceMatcher;
import mb.resource.hierarchical.match.path.ExtensionsPathMatcher;
import mb.stratego.build.strincr.message.Message;

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
     * Create source module with a normalized, absolute path to the module file. This
     * should give us a unique string to use to identify the module file within this pipeline.
     */
    public static Module source(ResourcePath path) {
        return new Module(path.getNormalized().asString(), Type.source);
    }

    static Set<Module> resolveWildcards(String modulePath, Collection<Import> imports, Collection<ResourcePath> includeDirs,
        List<Message<?>> outputMessages, ExecContext execContext) throws ExecException, IOException {
        final Set<Module> result = new HashSet<>(imports.size() * 2);
        for(Import anImport : imports) {
            switch(anImport.type) {
                case normal: {
                    boolean foundSomethingToImport = false;
                    for(ResourcePath dir : includeDirs) {
                        final ResourcePath strPath = dir.appendOrReplaceWithPath(anImport.path + ".str");
                        final HierarchicalResource strResource = execContext.require(strPath, ResourceStampers.<HierarchicalResource>exists()); // Only checking if it exists.
                        final ResourcePath rtreePath = dir.appendOrReplaceWithPath(anImport.path + ".rtree");
                        // Checking if exists, but also checking the first 4 bytes. OPTO: create stamper that only compares first 4 bytes.
                        final HierarchicalResource rtreeResource = execContext.require(rtreePath);
                        if(rtreeResource.exists()) {
                            foundSomethingToImport = true;
                            if(isLibraryRTree(rtreeResource)) {
                                result.add(Module.library(rtreePath.asString()));
                            } else {
                                result.add(Module.source(rtreePath));
                            }
                        } else if(strResource.exists()) {
                            foundSomethingToImport = true;
                            result.add(Module.source(strPath));
                        }
                    }
                    if(!foundSomethingToImport) {
                        outputMessages.add(Message.unresolvedImport(modulePath, anImport.pathTerm));
                    }
                    break;
                }
                case wildcard: {
                    boolean foundSomethingToImport = false;
                    for(ResourcePath includeDir : includeDirs) {
                        final ResourcePath path = includeDir.appendOrReplaceWithPath(anImport.path);
                        execContext.require(path);
                        final HierarchicalResource dir = execContext.getResourceService().getHierarchicalResource(path);
                        if(dir.exists()) {
                            final List<HierarchicalResource> strFiles = dir
                                .list(new PathResourceMatcher(new ExtensionsPathMatcher("str", "rtree")))
                                .collect(Collectors.toList());
                            for(HierarchicalResource strFile : strFiles) {
                                foundSomethingToImport = true;
                                result.add(Module.source(strFile.getPath()));
                            }
                        }
                    }
                    if(!foundSomethingToImport) {
                        outputMessages.add(Message.unresolvedWildcardImport(modulePath, anImport.pathTerm));
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
     * @param rtreeFile Path to the file
     * @return if file starts with Specification/1
     * @throws IOException on file system trouble
     */
    private static boolean isLibraryRTree(ReadableResource rtreeFile) throws IOException {
        char[] chars = new char[4];
        try(BufferedReader r = new BufferedReader(new InputStreamReader(rtreeFile.openRead()))) {
            return r.read(chars) != -1 && Arrays.equals(chars, "Spec".toCharArray());
        }
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
