package mb.stratego.build.strincr;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import jakarta.annotation.Nullable;

import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;

import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.STask;
import mb.pie.api.Supplier;
import mb.pie.api.stamp.output.OutputStampers;
import mb.pie.api.stamp.resource.ExistsResourceStamper;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.ReadableResource;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.PathResourceMatcher;
import mb.resource.hierarchical.match.path.ExtensionsPathMatcher;
import mb.stratego.build.strincr.data.StrategySignature;
import mb.stratego.build.util.ExistsAndRTreeStamper;
import mb.stratego.build.util.LastModified;
import mb.stratego.build.util.Relation;

public class ModuleImportService implements IModuleImportService {
    private final ResourcePathConverter resourcePathConverter;
    private final StrategoLanguage strategoLanguage;

    @jakarta.inject.Inject public ModuleImportService(ResourcePathConverter resourcePathConverter,
        StrategoLanguage strategoLanguage) {
        this.resourcePathConverter = resourcePathConverter;
        this.strategoLanguage = strategoLanguage;
    }

    private enum SomethingToImport {
        Str2Lib,
        RTree,
        Str2,
        Str,
        None
    }

    private static class Import implements Serializable {
        final SomethingToImport forSorting;
        final mb.stratego.build.strincr.ModuleIdentifier moduleIdentifier;

        private Import(SomethingToImport forSorting, boolean legacyStratego, boolean isLibrary,
            String moduleString, ResourcePath strPath) {
            this.forSorting = forSorting;
            this.moduleIdentifier = new mb.stratego.build.strincr.ModuleIdentifier(legacyStratego, isLibrary,
                moduleString, strPath);
        }

        @Override public boolean equals(Object o) {
            if(this == o)
                return true;
            if(o == null || getClass() != o.getClass())
                return false;

            Import anImport = (Import) o;

            if(forSorting != anImport.forSorting)
                return false;
            return moduleIdentifier.equals(anImport.moduleIdentifier);
        }

        @Override public int hashCode() {
            int result = forSorting.hashCode();
            result = 31 * result + moduleIdentifier.hashCode();
            return result;
        }

        static class Comparator implements java.util.Comparator<Import> {
            @Override public int compare(Import o1, Import o2) {
                final int typeComparison = o1.forSorting.compareTo(o2.forSorting);
                if(typeComparison != 0) {
                    return typeComparison;
                }
                return o1.moduleIdentifier.compareTo(o2.moduleIdentifier);
            }
        }
    }

    @Override public ImportResolution resolveImport(ExecContext context, IStrategoTerm anImport,
        ImportResolutionInfo importResolutionInfo) throws ExecException, IOException {
        /*
         * Note that we require the sdf task here to force it to generated needed str files. We
         *     then discover those in this method with a directory search.
         */
        for(final STask<?> t : importResolutionInfo.strFileGeneratingTasks) {
            context.require(t, OutputStampers.inconsequential());
        }
        List<ResourcePath> includeDirs = new ArrayList<>(importResolutionInfo.includeDirs);
        for(Supplier<Stratego2LibInfo> str2lib : importResolutionInfo.str2libraries) {
            includeDirs.add(context.require(str2lib).str2libFile.getParent());
        }
        if(!TermUtils.isAppl(anImport)) {
            throw new ExecException("Import term was not a constructor: " + anImport);
        }
        final IStrategoAppl appl = (IStrategoAppl) anImport;
        switch(appl.getName()) {
            case "Import": {
                String moduleString = TermUtils.toJavaStringAt(appl, 0);
                final @Nullable BuiltinLibraryIdentifier builtinLibraryIdentifier =
                    BuiltinLibraryIdentifier.fromString(moduleString);
                if(builtinLibraryIdentifier != null) {
                    if(!importResolutionInfo.linkedLibraries.contains(builtinLibraryIdentifier)) {
                        // HACK: work around bootstrapping issues with meta-languages
                        if(builtinLibraryIdentifier.equals(BuiltinLibraryIdentifier.StrategoGpp)) {
                            moduleString = "gpp";
                        } else {
                            return UnresolvedImport.INSTANCE;
                        }
                    }
                    return new ResolvedImport(Collections.singleton(builtinLibraryIdentifier));
                }

                final Set<Import> imports = new TreeSet<>(new Import.Comparator());
                if(!moduleString.contains("/")) {
                    for(ResourcePath dir : includeDirs) {
                        final ResourcePath str2libPath =
                            dir.appendOrReplaceWithPath(moduleString + ".str2lib");
                        final HierarchicalResource str2libResource = context
                            .require(str2libPath, ResourceStampers.<HierarchicalResource>exists());
                        if(str2libResource.exists()) {
                            imports.add(new Import(SomethingToImport.Str2Lib, false, true, moduleString,
                                str2libPath));
                        }
                    }
                }
                if(importResolutionInfo.supportRTree) {
                    for(ResourcePath dir : includeDirs) {
                        final ResourcePath rtreePath =
                            dir.appendOrReplaceWithPath(moduleString + ".rtree");
                        final HierarchicalResource rtreeResource = context
                            .require(rtreePath, new ExistsAndRTreeStamper<HierarchicalResource>());
                        if(rtreeResource.exists()) {
                            final boolean isLibrary =
                                ExistsAndRTreeStamper.isLibraryRTree(rtreeResource);
                            imports.add(
                                new Import(SomethingToImport.RTree, true, isLibrary, moduleString,
                                    rtreePath));
                        }
                    }
                }
                for(ResourcePath dir : includeDirs) {
                    final ResourcePath str2Path =
                        dir.appendOrReplaceWithPath(moduleString + ".str2");
                    final HierarchicalResource str2Resource = context
                        .require(str2Path, ResourceStampers.<HierarchicalResource>exists());
                    if(str2Resource.exists()) {
                        imports.add(new Import(SomethingToImport.Str2, false, false, moduleString,
                            str2Path));
                    }
                }
                if(importResolutionInfo.supportStr1) {
                    for(ResourcePath dir : includeDirs) {
                        final ResourcePath strPath =
                            dir.appendOrReplaceWithPath(moduleString + ".str");
                        final HierarchicalResource strResource = context.require(strPath,
                            ResourceStampers.<HierarchicalResource>exists());
                        if(strResource.exists()) {
                            imports.add(
                                new Import(SomethingToImport.Str, true, false, moduleString, strPath));
                        }
                    }
                }
                if(imports.isEmpty()) {
                    return UnresolvedImport.INSTANCE;
                }
                final HashSet<ModuleIdentifier> result = new HashSet<>();
                if(imports.size() == 1) {
                    result.add(imports.iterator().next().moduleIdentifier);
                } else {
                    SomethingToImport foundSomethingToImport = SomethingToImport.None;
                    for(Import anImport1 : imports) {
                        if(foundSomethingToImport.compareTo(anImport1.forSorting) >= 0) {
                            foundSomethingToImport = anImport1.forSorting;
                            result.add(anImport1.moduleIdentifier);
                        } else {
                            break;
                        }
                    }
                }
                return new ResolvedImport(result);
            }
            case "ImportWildcard": {
                final List<String> extensions = new ArrayList<>(3);
                if(importResolutionInfo.supportRTree) {
                    extensions.add("rtree");
                }
                extensions.add("str2");
                if(importResolutionInfo.supportStr1) {
                    extensions.add("str");
                }
                final PathResourceMatcher pathResourceMatcher = new PathResourceMatcher(new ExtensionsPathMatcher(extensions));
                final String directory = TermUtils.toJavaStringAt(appl, 0);
                final Map<String, TreeSet<Import>> foundModules = new HashMap<>();
                for(ResourcePath includeDir : importResolutionInfo.includeDirs) {
                    final HierarchicalResource searchDir = context.require(includeDir.appendOrReplaceWithPath(directory));
                    if(searchDir.exists()) {
                        // N.B. deliberate choice not to resolve to str2lib files here, those should be imported by name.
                        searchDir.listForEach(pathResourceMatcher, moduleFile -> {
                            @Nullable final String filename = moduleFile.getLeaf();
                            @Nullable final String ext = moduleFile.getLeafExtension();
                            assert filename != null : "HierarchicalResource::list returned some resources without a path leaf?!";
                            assert ext != null : "HierarchicalResource::list returned some resources without an extension?!";
                            boolean legacyStratego;
                            boolean isLibrary = false;
                            String moduleString;
                            SomethingToImport somethingToImport;
                            switch(ext) {
                                case "rtree":
                                    isLibrary = ExistsAndRTreeStamper.isLibraryRTree(moduleFile);
                                    moduleString = directory + "/" + filename
                                        .substring(0, filename.length() - ".rtree".length());
                                    legacyStratego = true;
                                    somethingToImport = SomethingToImport.RTree;
                                    break;
                                case "str2":
                                    moduleString = directory + "/" + filename
                                        .substring(0, filename.length() - ".str2".length());
                                    legacyStratego = false;
                                    somethingToImport = SomethingToImport.Str2;
                                    break;
                                case "str":
                                    moduleString = directory + "/" + filename
                                        .substring(0, filename.length() - ".str".length());
                                    legacyStratego = true;
                                    somethingToImport = SomethingToImport.Str;
                                    break;
                                default:
                                    assert false : "HierarchicalResource::list returned some resources an extension that it shouldn't search for?!";
                                    return;
                            }
                            Relation.getOrInitialize(foundModules, moduleString, () -> new TreeSet<>(new Import.Comparator()))
                                .add(new Import(somethingToImport, legacyStratego, isLibrary,
                                    moduleString, moduleFile.getPath()));
                        });
                    }
                }
                if(foundModules.isEmpty()) {
                    return UnresolvedImport.INSTANCE;
                }
                // Have rtree files for a module shadow str2/str files, have str2 files shadow str files
                final Set<mb.stratego.build.strincr.ModuleIdentifier> result = new HashSet<>();
                for(Set<Import> modules : foundModules.values()) {
                    if(modules.size() == 1) {
                        result.add(modules.iterator().next().moduleIdentifier);
                    } else {
                        SomethingToImport foundSomethingToImport = SomethingToImport.None;
                        for(Import module : modules) {
                            if(foundSomethingToImport.compareTo(module.forSorting) >= 0) {
                                foundSomethingToImport = module.forSorting;
                                result.add(module.moduleIdentifier);
                            } else {
                                break;
                            }
                        }
                    }
                }
                return new ResolvedImport(result);
            }
            default:
                throw new ExecException(
                    "Import term was not the expected Import or ImportWildcard: " + appl);
        }
    }

    @Override public LastModified<IStrategoTerm> getModuleAst(ExecContext context,
        ModuleIdentifier moduleIdentifier,
        ImportResolutionInfo importResolutionInfo) throws Exception {
        /*
         * Every getModuleAst call depends on the sdf task so there is no hidden dep. To make
         *     sure that getModuleAst only runs when their input _files_ change, we need
         *     getModuleAst to depend on the sdf task with a simple stamper that allows the
         *     execution of the sdf task to be ignored.
         */
        for(final STask<?> t : importResolutionInfo.strFileGeneratingTasks) {
            context.require(t, OutputStampers.inconsequential());
        }
        if(moduleIdentifier instanceof mb.stratego.build.strincr.ModuleIdentifier) {
            final mb.stratego.build.strincr.ModuleIdentifier identifier =
                (mb.stratego.build.strincr.ModuleIdentifier) moduleIdentifier;
                if(moduleIdentifier.isLibrary() && moduleIdentifier.legacyStratego()) {
                    final HierarchicalResource resource = context.require(identifier.path);
                    try(final InputStream inputStream = new BufferedInputStream(resource.openRead())) {
                        final long lastModified = resource.getLastModifiedTime().getEpochSecond();
                        return new LastModified<>(strategoLanguage.parseRtree(inputStream), lastModified);
                    }
                } else if(moduleIdentifier.isLibrary() && !moduleIdentifier.legacyStratego()) {
                    for(Supplier<Stratego2LibInfo> str2library : importResolutionInfo.str2libraries) {
                        if(str2library instanceof STask) {
                            context.require((STask<Stratego2LibInfo>) str2library, OutputStampers.inconsequential());
                        } else {
                            context.require(str2library);
                        }
                    }
                    final HierarchicalResource resource = context.require(identifier.path);
                    try(final InputStream inputStream = new BufferedInputStream(resource.openRead())) {
                        final long lastModified = resource.getLastModifiedTime().getEpochSecond();
                        return new LastModified<>(strategoLanguage.parseStr2Lib(inputStream), lastModified);
                    }
                } else {
                    final HierarchicalResource resource = context.require(identifier.path);
                    try(final InputStream inputStream = new BufferedInputStream(resource.openRead())) {
                        final long lastModified = resource.getLastModifiedTime().getEpochSecond();
                        IStrategoTerm ast = strategoLanguage
                            .parse(context, inputStream,
                                StandardCharsets.UTF_8, resourcePathConverter.toString(identifier.path));
                        ast = strategoLanguage.postparseDesugar(ast);
                        return new LastModified<>(ast, lastModified);
                    }
                }
        } else {// if(moduleIdentifier instanceof BuiltinLibraryIdentifier) {
            final BuiltinLibraryIdentifier builtinLibraryIdentifier =
                (BuiltinLibraryIdentifier) moduleIdentifier;
            return new LastModified<>(builtinLibraryIdentifier.readLibraryFile(), 0L);
        }
    }

    @Override public @Nullable String fileName(ModuleIdentifier moduleIdentifier) {
        if(moduleIdentifier instanceof mb.stratego.build.strincr.ModuleIdentifier) {
            final mb.stratego.build.strincr.ModuleIdentifier identifier =
                (mb.stratego.build.strincr.ModuleIdentifier) moduleIdentifier;
            return resourcePathConverter.toString(identifier.path);
        } else {// if(moduleIdentifier instanceof BuiltinLibraryIdentifier) {
            return null;
        }
    }

    @Override public boolean externalStrategyExists(ExecContext context, StrategySignature strategySignature,
        ImportResolutionInfo importResolutionInfo) {
        try {
            return context.require(
                importResolutionInfo.resolveExternals.appendAsRelativePath(
                    dollarsForCapitals(strategySignature.cifiedName()) + ".java"), ResourceStampers.<ReadableResource>exists()).exists();
        } catch(IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private String dollarsForCapitals(String cified) {
        return cified.replaceAll("\\p{Lu}", "\\$$0");
    }
}
