package mb.stratego.build.strincr;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.inject.Inject;

import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;

import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.STask;
import mb.pie.api.stamp.output.OutputStampers;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.PathResourceMatcher;
import mb.resource.hierarchical.match.path.ExtensionsPathMatcher;
import mb.stratego.build.util.LastModified;

public class ModuleImportService implements IModuleImportService {
    private final ResourcePathConverter resourcePathConverter;
    private final ParseStratego parseStratego;

    @Inject public ModuleImportService(ResourcePathConverter resourcePathConverter,
        ParseStratego parseStratego) {
        this.resourcePathConverter = resourcePathConverter;
        this.parseStratego = parseStratego;
    }

    @Override public ImportResolution resolveImport(ExecContext context, IStrategoTerm anImport,
        Collection<STask<?>> strFileGeneratingTasks, Collection<? extends ResourcePath> includeDirs,
        Collection<? extends IModuleImportService.ModuleIdentifier> linkedLibraries)
        throws ExecException, IOException {
        /*
         * Note that we require the sdf task here to force it to generated needed str files. We
         *     then discover those in this method with a directory search.
         */
        for(final STask<?> t : strFileGeneratingTasks) {
            context.require(t, OutputStampers.inconsequential());
        }
        if(!TermUtils.isAppl(anImport)) {
            throw new ExecException("Import term was not a constructor: " + anImport);
        }
        final IStrategoAppl appl = (IStrategoAppl) anImport;
        switch(appl.getName()) {
            case "Import": {
                final String moduleString = TermUtils.toJavaStringAt(appl, 0);
                final @Nullable BuiltinLibraryIdentifier builtinLibraryIdentifier =
                    BuiltinLibraryIdentifier.fromString(moduleString);
                if(builtinLibraryIdentifier != null) {
                    if(!linkedLibraries.contains(builtinLibraryIdentifier)) {
                        return UnresolvedImport.INSTANCE;
                    }
                    return new ResolvedImport(Collections.singleton(builtinLibraryIdentifier));
                }

                final Set<mb.stratego.build.strincr.ModuleIdentifier> result = new HashSet<>();
                boolean foundSomethingToImport = false;
                for(ResourcePath dir : includeDirs) {
                    final ResourcePath strPath = dir.appendOrReplaceWithPath(moduleString + ".str");
                    final HierarchicalResource strResource =
                        context.require(strPath, ResourceStampers.<HierarchicalResource>exists());
                    final ResourcePath rtreePath =
                        dir.appendOrReplaceWithPath(moduleString + ".rtree");
                    final HierarchicalResource rtreeResource = context
                        .require(rtreePath, new ExistsAndRTreeStamper<HierarchicalResource>());
                    if(rtreeResource.exists()) {
                        foundSomethingToImport = true;
                        final boolean isLibrary =
                            ExistsAndRTreeStamper.isLibraryRTree(rtreeResource);
                        result.add(
                            new mb.stratego.build.strincr.ModuleIdentifier(isLibrary, moduleString,
                                rtreeResource));
                    } else if(strResource.exists()) {
                        foundSomethingToImport = true;
                        result.add(
                            new mb.stratego.build.strincr.ModuleIdentifier(false, moduleString,
                                strResource));
                    }
                }
                if(!foundSomethingToImport) {
                    return UnresolvedImport.INSTANCE;
                }
                return new ResolvedImport(result);
            }
            case "ImportWildcard": {
                final String directory = TermUtils.toJavaStringAt(appl, 0);
                final Set<mb.stratego.build.strincr.ModuleIdentifier> result = new HashSet<>();
                boolean foundSomethingToImport = false;
                for(ResourcePath includeDir : includeDirs) {
                    final ResourcePath searchDirectory =
                        includeDir.appendOrReplaceWithPath(directory);
                    context.require(searchDirectory);
                    final HierarchicalResource searchDir =
                        context.getResourceService().getHierarchicalResource(searchDirectory);
                    if(searchDir.exists()) {
                        final List<HierarchicalResource> moduleFiles = searchDir.list(
                            new PathResourceMatcher(new ExtensionsPathMatcher("str", "rtree")))
                            .collect(Collectors.toList());
                        for(HierarchicalResource moduleFile : moduleFiles) {
                            foundSomethingToImport = true;
                            @Nullable final String filename = moduleFile.getLeaf();
                            assert filename != null : "HierarchicalResource::list returned some resources without a path leaf?!";
                            if(filename.endsWith(".str")) {
                                final String moduleString = directory + "/" + filename
                                    .substring(0, filename.length() - ".str".length());
                                result.add(new mb.stratego.build.strincr.ModuleIdentifier(false,
                                    moduleString, moduleFile));
                            } else if(filename.endsWith(".rtree")) {
                                final boolean isLibrary =
                                    ExistsAndRTreeStamper.isLibraryRTree(moduleFile);
                                final String moduleString = directory + "/" + filename
                                    .substring(0, filename.length() - ".rtree".length());
                                result.add(new mb.stratego.build.strincr.ModuleIdentifier(isLibrary,
                                    moduleString, moduleFile));
                            }
                        }
                    }
                }
                if(!foundSomethingToImport) {
                    return UnresolvedImport.INSTANCE;
                }
                return new ResolvedImport(result);
            }
            default:
                throw new ExecException(
                    "Import term was not the expected Import or ImportWildcard: " + appl);
        }
    }

    @Override public LastModified<IStrategoTerm> getModuleAst(ExecContext context,
        IModuleImportService.ModuleIdentifier moduleIdentifier,
        Collection<STask<?>> strFileGeneratingTasks) throws Exception {
        /*
         * Every getModuleAst call depends on the sdf task so there is no hidden dep. To make
         *     sure that getModuleAst only runs when their input _files_ change, we need
         *     getModuleAst to depend on the sdf task with a simple stamper that allows the
         *     execution of the sdf task to be ignored.
         */
        for(final STask<?> t : strFileGeneratingTasks) {
            context.require(t, OutputStampers.inconsequential());
        }
        if(moduleIdentifier instanceof mb.stratego.build.strincr.ModuleIdentifier) {
            final mb.stratego.build.strincr.ModuleIdentifier identifier =
                (mb.stratego.build.strincr.ModuleIdentifier) moduleIdentifier;
            // TODO: existence check required first somewhere (here or in the caller?)
            context.require(identifier.resource);
            try(final InputStream inputStream = new BufferedInputStream(
                identifier.resource.openRead())) {
                final long lastModified =
                    identifier.resource.getLastModifiedTime().getEpochSecond();
                if(moduleIdentifier.isLibrary()) {
                    return new LastModified<>(parseStratego.parseRtree(inputStream), lastModified);
                } else {
                    return new LastModified<>(parseStratego
                        .parse(inputStream, StandardCharsets.UTF_8,
                            resourcePathConverter.toString(identifier.resource.getPath())),
                        lastModified);
                }
            }
        } else {// if(moduleIdentifier instanceof BuiltinLibraryIdentifier) {
            final BuiltinLibraryIdentifier builtinLibraryIdentifier =
                (BuiltinLibraryIdentifier) moduleIdentifier;
            return new LastModified<>(builtinLibraryIdentifier.readLibraryFile(), 0L);
        }
    }
}
