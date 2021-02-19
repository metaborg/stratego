package mb.stratego.build.spoofax2;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.ClosedByInterruptException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.config.JSGLRVersion;
import org.metaborg.core.language.ILanguage;
import org.metaborg.core.language.ILanguageIdentifierService;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.ILanguageService;
import org.metaborg.core.language.IdentifiedResource;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.core.syntax.ParseException;
import org.metaborg.spoofax.core.SpoofaxConstants;
import org.metaborg.spoofax.core.syntax.ISpoofaxSyntaxService;
import org.metaborg.spoofax.core.unit.ISpoofaxInputUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxUnitService;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.io.binary.TermReader;
import org.spoofax.terms.util.TermUtils;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.STask;
import mb.pie.api.stamp.output.OutputStampers;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.PathResourceMatcher;
import mb.resource.hierarchical.match.path.ExtensionsPathMatcher;
import mb.stratego.build.strincr.IModuleImportService;
import mb.stratego.build.strincr.ResourcePathConverter;
import mb.stratego.build.termvisitors.DisambiguateAsAnno;
import mb.stratego.build.util.LastModified;
import mb.stratego.build.util.StrIncrContext;

public class ModuleImportService implements IModuleImportService {
    private final IResourceService resourceService;
    private final ILanguageIdentifierService languageIdentifierService;
    private final ILanguageService languageService;
    private final ITermFactory termFactory;
    private final ISpoofaxUnitService unitService;
    private final ISpoofaxSyntaxService syntaxService;
    private final StrIncrContext strContext;
    private final ResourcePathConverter resourcePathConverter;

    private final @Nullable LastModified<IStrategoTerm> ast;
    private final @Nullable String moduleName;
    private final Collection<STask<?>> strFileGeneratingTasks;
    private final Collection<ResourcePath> includeDirs;

    @AssistedInject public ModuleImportService(IResourceService resourceService,
        ILanguageIdentifierService languageIdentifierService, ILanguageService languageService,
        ITermFactory termFactory, ISpoofaxUnitService unitService,
        ISpoofaxSyntaxService syntaxService, StrIncrContext strContext,
        ResourcePathConverter resourcePathConverter,
        @Assisted Collection<STask<?>> strFileGeneratingTasks,
        @Assisted Collection<ResourcePath> includeDirs, @Assisted @Nullable String moduleName,
        @Assisted @Nullable LastModified<IStrategoTerm> ast) {
        this.resourceService = resourceService;
        this.languageIdentifierService = languageIdentifierService;
        this.languageService = languageService;
        this.termFactory = termFactory;
        this.unitService = unitService;
        this.syntaxService = syntaxService;
        this.strContext = strContext;
        this.resourcePathConverter = resourcePathConverter;

        this.strFileGeneratingTasks = strFileGeneratingTasks;
        this.includeDirs = includeDirs;
        this.moduleName = moduleName;
        if(moduleName != null && ast == null) {
            throw new IllegalArgumentException(
                "moduleName and ast should be both null or both not null");
        }
        this.ast = ast;
    }

    @AssistedInject public ModuleImportService(IResourceService resourceService,
        ILanguageIdentifierService languageIdentifierService, ILanguageService languageService,
        ITermFactory termFactory, ISpoofaxUnitService unitService,
        ISpoofaxSyntaxService syntaxService, StrIncrContext strContext,
        ResourcePathConverter resourcePathConverter,
        @Assisted Collection<STask<?>> strFileGeneratingTasks,
        @Assisted Collection<ResourcePath> includeDirs) {
        this(resourceService, languageIdentifierService, languageService, termFactory, unitService,
            syntaxService, strContext, resourcePathConverter, strFileGeneratingTasks, includeDirs,
            null, null);
    }

    @Override public ImportResolution resolveImport(ExecContext context, IStrategoTerm anImport)
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
                    return new ResolvedImport(Collections.singleton(builtinLibraryIdentifier));
                }

                final Set<mb.stratego.build.spoofax2.ModuleIdentifier> result = new HashSet<>();
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
                            new mb.stratego.build.spoofax2.ModuleIdentifier(isLibrary, moduleString,
                                rtreeResource));
                    } else if(strResource.exists()) {
                        foundSomethingToImport = true;
                        result.add(
                            new mb.stratego.build.spoofax2.ModuleIdentifier(false, moduleString,
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
                final Set<mb.stratego.build.spoofax2.ModuleIdentifier> result = new HashSet<>();
                boolean foundSomethingToImport = false;
                for(ResourcePath includeDir : includeDirs) {
                    final ResourcePath searchDirectory = includeDir.appendOrReplaceWithPath(directory);
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
                                result.add(new mb.stratego.build.spoofax2.ModuleIdentifier(false,
                                    moduleString, moduleFile));
                            } else if(filename.endsWith(".rtree")) {
                                final boolean isLibrary =
                                    ExistsAndRTreeStamper.isLibraryRTree(moduleFile);
                                final String moduleString = directory + "/" + filename
                                    .substring(0, filename.length() - ".rtree".length());
                                result.add(new mb.stratego.build.spoofax2.ModuleIdentifier(isLibrary,
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
        IModuleImportService.ModuleIdentifier moduleIdentifier) throws Exception {
        /*
         * Every getModuleAst call depends on the sdf task so there is no hidden dep. To make
         *     sure that getModuleAst only runs when their input _files_ change, we need
         *     getModuleAst to depend on the sdf task with a simple stamper that allows the
         *     execution of the sdf task to be ignored.
         */
        for(final STask<?> t : strFileGeneratingTasks) {
            context.require(t, OutputStampers.inconsequential());
        }
        if(moduleIdentifier instanceof mb.stratego.build.spoofax2.ModuleIdentifier) {
            final mb.stratego.build.spoofax2.ModuleIdentifier identifier =
                (mb.stratego.build.spoofax2.ModuleIdentifier) moduleIdentifier;
            // TODO: existence check required first somewhere (here or in the caller?)
            context.require(identifier.resource);
            if(containsChangesNotReflectedInResource(identifier)) {
                assert ast != null;
                context.logger().debug("File open in editor: " + identifier.resource);
                return ast;
            } else {
                try(final InputStream inputStream = new BufferedInputStream(
                    identifier.resource.openRead())) {
                    final long lastModified =
                        identifier.resource.getLastModifiedTime().getEpochSecond();
                    if(moduleIdentifier.isLibrary()) {
                        return new LastModified<>(parseRtree(inputStream), lastModified);
                    } else {
                        return new LastModified<>(parse(inputStream,
                            resourcePathConverter.toString(identifier.resource.getPath())),
                            lastModified);
                    }
                }
            }
        } else {// if(moduleIdentifier instanceof BuiltinLibraryIdentifier) {
            final BuiltinLibraryIdentifier builtinLibraryIdentifier =
                (BuiltinLibraryIdentifier) moduleIdentifier;
            return new LastModified<>(builtinLibraryIdentifier.readLibraryFile(), 0L);
        }
    }

    @Override
    public boolean containsChangesNotReflectedInResource(ModuleIdentifier moduleIdentifier) {
        return moduleName != null && (moduleIdentifier.moduleString().endsWith(moduleName)
            || moduleName.endsWith(moduleIdentifier.moduleString()));
    }

    private IStrategoTerm parse(InputStream inputStream, @Nullable String path)
        throws ExecException, IOException, ParseException {
        final @Nullable FileObject inputFile;
        if(path != null) {
            inputFile = resourceService.resolve(path);
        } else {
            inputFile = null;
        }

        @Nullable ILanguageImpl strategoLangImpl = null;
        @Nullable ILanguageImpl strategoDialect = null;
        if(inputFile != null) {
            final @Nullable IdentifiedResource identified =
                languageIdentifierService.identifyToResource(inputFile);
            if(identified != null) {
                strategoLangImpl = identified.language;
                strategoDialect = identified.dialect;
            }
        }
        if(strategoLangImpl == null) {
            final @Nullable ILanguage strategoLang =
                languageService.getLanguage(SpoofaxConstants.LANG_STRATEGO_NAME);
            if(strategoLang != null) {
                strategoLangImpl = strategoLang.activeImpl();
            }
        }
        if(strategoLangImpl == null) {
            throw new ExecException(
                "Cannot find/load Stratego language. Please add a source dependency "
                    + "'org.metaborg:org.metaborg.meta.lang.stratego:${metaborgVersion}' in your metaborg.yaml file. ");
        }

        @Nullable IStrategoTerm ast;
        final String text;
        try {
            text = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        } catch(ClosedByInterruptException e) {
            throw new ExecException("Interrupted while reading file", e);
        }
        final ISpoofaxInputUnit inputUnit =
            unitService.inputUnit(inputFile, text, strategoLangImpl, strategoDialect);
        final ISpoofaxParseUnit parseResult = syntaxService.parse(inputUnit, JSGLRVersion.v2);
        ast = parseResult.ast();
        if(!parseResult.success() || ast == null) {
            throw new ExecException(
                "Cannot parse stratego file " + inputFile + ": " + parseResult.messages());
        }

        // Remove ambiguity that occurs in old table from sdf2table when using JSGLR2 parser
        ast = new DisambiguateAsAnno(strContext).visit(ast);

        return ast;
    }

    private IStrategoTerm parseRtree(InputStream inputStream) throws ExecException, IOException {
        final IStrategoTerm ast = new TermReader(termFactory).parseFromStream(inputStream);
        if(!(TermUtils.isAppl(ast) && ((IStrategoAppl) ast).getName().equals("Module")
            && ast.getSubtermCount() == 2)) {
            if(!(TermUtils.isAppl(ast) && ((IStrategoAppl) ast).getName().equals("Specification")
                && ast.getSubtermCount() == 1)) {
                throw new ExecException(
                    "Did not find Module/2 in RTree file. Found: \n" + ast.toString(2));
            } else {
                throw new ExecException(
                    "Bug in custom library detection. Please file a bug report and "
                        + "turn off Stratego separate compilation for now as a work-around. ");
            }
        }
        return ast;
    }

    @Override public boolean equals(@Nullable Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        ModuleImportService that = (ModuleImportService) o;

        if(ast != null ? !ast.equals(that.ast) : that.ast != null)
            return false;
        if(moduleName != null ? !moduleName.equals(that.moduleName) : that.moduleName != null)
            return false;
        if(!strFileGeneratingTasks.equals(that.strFileGeneratingTasks))
            return false;
        return includeDirs.equals(that.includeDirs);
    }

    @Override public int hashCode() {
        int result = ast != null ? ast.hashCode() : 0;
        result = 31 * result + (moduleName != null ? moduleName.hashCode() : 0);
        result = 31 * result + strFileGeneratingTasks.hashCode();
        result = 31 * result + includeDirs.hashCode();
        return result;
    }

    @Override public String toString() {
        if(moduleName == null) {
            return "ModuleImportService()";
        } else {
            return "ModuleImportService(" + moduleName + ")";
        }
    }
}
