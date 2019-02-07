package mb.stratego.build;

import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.STask;
import mb.pie.api.Task;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.output.InconsequentialOutputStamper;
import mb.stratego.build.util.CommonPaths;

import com.google.inject.Inject;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.context.ContextException;
import org.metaborg.core.context.IContext;
import org.metaborg.core.context.IContextService;
import org.metaborg.core.language.ILanguage;
import org.metaborg.core.language.ILanguageIdentifierService;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.ILanguageService;
import org.metaborg.core.language.dialect.IDialectService;
import org.metaborg.core.project.IProject;
import org.metaborg.core.project.IProjectService;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.core.source.ISourceTextService;
import org.metaborg.core.syntax.ParseException;
import org.metaborg.spoofax.core.stratego.IStrategoCommon;
import org.metaborg.spoofax.core.syntax.ISpoofaxSyntaxService;
import org.metaborg.spoofax.core.syntax.ImploderImplementation;
import org.metaborg.spoofax.core.syntax.SyntaxFacet;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.metaborg.spoofax.core.unit.ISpoofaxInputUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxUnitService;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StrIncrFront implements TaskDef<StrIncrFront.Input, StrIncrFront.Output> {
    public static final class Input implements Serializable {
        final File projectLocation;
        final File inputFile;
        final String projectName;
        final Collection<STask<?>> originTasks;

        Input(File projectLocation, File inputFile, String projectName, Collection<STask<?>> originTasks) {
            this.projectLocation = projectLocation;
            this.inputFile = inputFile;
            this.projectName = projectName;
            this.originTasks = originTasks;
        }

        @Override public String toString() {
            return "StrIncrFront$Input(" + inputFile + ')';
        }

        @Override public boolean equals(Object o) {
            if(this == o)
                return true;
            if(o == null || getClass() != o.getClass())
                return false;

            Input input = (Input) o;

            if(!projectLocation.equals(input.projectLocation))
                return false;
            if(!inputFile.equals(input.inputFile))
                return false;
            //noinspection SimplifiableIfStatement
            if(!projectName.equals(input.projectName))
                return false;
            return originTasks.equals(input.originTasks);
        }

        @Override public int hashCode() {
            int result = projectLocation.hashCode();
            result = 31 * result + inputFile.hashCode();
            result = 31 * result + projectName.hashCode();
            result = 31 * result + originTasks.hashCode();
            return result;
        }
    }

    public static final class Output implements Serializable {
        final String moduleName;
        final Map<String, File> strategyFiles;
        final Map<String, Set<String>> strategyConstrFiles;
        final Map<String, File> overlayFiles;
        final List<Import> imports;

        Output(String moduleName, Map<String, File> strategyFiles, Map<String, Set<String>> strategyConstrFiles,
            Map<String, File> overlayFiles, List<Import> imports) {
            this.moduleName = moduleName;
            this.strategyFiles = strategyFiles;
            this.strategyConstrFiles = strategyConstrFiles;
            this.overlayFiles = overlayFiles;
            this.imports = imports;
        }

        @Override public boolean equals(Object o) {
            if(this == o)
                return true;
            if(o == null || getClass() != o.getClass())
                return false;

            Output output = (Output) o;

            if(!moduleName.equals(output.moduleName))
                return false;
            if(!strategyFiles.equals(output.strategyFiles))
                return false;
            if(!strategyConstrFiles.equals(output.strategyConstrFiles))
                return false;
            //noinspection SimplifiableIfStatement
            if(!overlayFiles.equals(output.overlayFiles))
                return false;
            return imports.equals(output.imports);
        }

        @Override public int hashCode() {
            int result = moduleName.hashCode();
            result = 31 * result + strategyFiles.hashCode();
            result = 31 * result + strategyConstrFiles.hashCode();
            result = 31 * result + overlayFiles.hashCode();
            result = 31 * result + imports.hashCode();
            return result;
        }

        @Override public String toString() {
            return "StrIncrFront$Output(" + moduleName + ')';
        }
    }

    public static final class Import implements Serializable {
        public enum ImportType {
            normal, wildcard
        }

        final ImportType importType;
        final String importString;

        Import(ImportType importType, String importString) {
            this.importType = importType;
            this.importString = importString;
        }

        static Import normal(String importString) {
            return new Import(ImportType.normal, importString);
        }

        static Import wildcard(String importString) {
            return new Import(ImportType.wildcard, importString);
        }

        Set<File> resolveImport(Collection<File> includeDirs) throws IOException {
            Set<File> result = new HashSet<>();
            for(File dir : includeDirs) {
                switch(importType) {
                    case normal: {
                        final Path strPath = dir.toPath().resolve(importString + ".str");
                        final Path rtreePath = dir.toPath().resolve(importString + ".rtree");
                        if(Files.exists(rtreePath)) {
                            result.add(rtreePath.toFile());
                        } else if(Files.exists(strPath)) {
                            result.add(strPath.toFile());
                        }
                        break;
                    }
                    case wildcard: {
                        final Path path = dir.toPath().resolve(importString);
                        if(Files.exists(path)) {
                            final @Nullable File[] strFiles = path.toFile()
                                .listFiles((FilenameFilter) new SuffixFileFilter(Arrays.asList(".str", ".rtree")));
                            if(strFiles == null) {
                                throw new IOException("Reading file list in directory failed for directory: " + path);
                            }
                            result.addAll(Arrays.asList(strFiles));
                        }
                        break;
                    }
                    default:
                        throw new IOException("Missing case for ImportType: " + importType);
                }
            }
            return result;
        }

        static Import fromTerm(IStrategoTerm importTerm) throws IOException {
            if(!(importTerm instanceof IStrategoAppl)) {
                throw new IOException("Import term was not a constructor: " + importTerm);
            }
            final IStrategoAppl appl = (IStrategoAppl) importTerm;
            switch(appl.getName()) {
                case "Import":
                    return normal(Tools.javaStringAt(appl, 0));
                case "ImportWildcard":
                    return wildcard(Tools.javaStringAt(appl, 0));
                default:
                    throw new IOException("Import term was not the expected Import or ImportWildcard: " + appl);
            }
        }

        @Override public boolean equals(Object o) {
            if(this == o)
                return true;
            if(o == null || getClass() != o.getClass())
                return false;

            Import anImport = (Import) o;

            //noinspection SimplifiableIfStatement
            if(importType != anImport.importType)
                return false;
            return importString.equals(anImport.importString);
        }

        @Override public int hashCode() {
            int result = importType.hashCode();
            result = 31 * result + importString.hashCode();
            return result;
        }

        @Override public String toString() {
            return "Import{" + "importType=" + importType + ", importString='" + importString + '\'' + '}';
        }
    }

    private final IResourceService resourceService;
    private final IProjectService projectService;
    private final ILanguageIdentifierService languageIdentifierService;
    private final IDialectService dialectService;
    private final ILanguageService languageService;
    private final ITermFactoryService termFactoryService;
    private final IContextService contextService;
    private final IStrategoCommon strategoCommon;
    private final ISourceTextService sourceTextService;
    private final ISpoofaxUnitService unitService;
    private final ISpoofaxSyntaxService syntaxService;

    private static final String COMPILE_STRATEGY_NAME = "clean-and-compile-module";
    private static final String STRATEGO_LANG_NAME = "Stratego-Sugar";

    @Inject public StrIncrFront(IResourceService resourceService, IProjectService projectService,
        ILanguageIdentifierService languageIdentifierService,
        IDialectService dialectService, ILanguageService languageService, ITermFactoryService termFactoryService,
        IContextService contextService, IStrategoCommon strategoCommon, ISourceTextService sourceTextService,
        ISpoofaxUnitService unitService, ISpoofaxSyntaxService syntaxService) {
        this.resourceService = resourceService;
        this.projectService = projectService;
        this.languageIdentifierService = languageIdentifierService;
        this.dialectService = dialectService;
        this.languageService = languageService;
        this.termFactoryService = termFactoryService;
        this.contextService = contextService;
        this.strategoCommon = strategoCommon;
        this.sourceTextService = sourceTextService;
        this.unitService = unitService;
        this.syntaxService = syntaxService;
    }


    @Override public Output exec(ExecContext execContext, Input input) throws ExecException, InterruptedException {
        for(final STask<?> t : input.originTasks) {
            execContext.require(t, InconsequentialOutputStamper.Companion.getInstance());
        }

        final FileObject location = resourceService.resolve(input.projectLocation);
        final FileObject resource = resourceService.resolve(input.inputFile);
        final IStrategoTerm result;
        try {
            result = runStrategoCompileBuilder(resource, input.projectName, location);
        } catch(IOException e) {
            throw new ExecException(e);
        }

        execContext.require(input.inputFile);



        final String moduleName = Tools.javaStringAt(result, 0);
        final IStrategoList strategyList = Tools.listAt(result, 1);
        final IStrategoList importsTerm = Tools.listAt(result, 2);
        final IStrategoList usedConstrList = Tools.listAt(result, 3);
        final IStrategoList overlayList = Tools.listAt(result, 4);
        assert
            strategyList.size() == usedConstrList.size() :
            "Inconsistent compiler: strategy list size (" + strategyList.size() + ") != used constructors list size ("
                + usedConstrList.size() + ")";

        final Map<String, File> strategyFiles = new HashMap<>();
        final Map<String, Set<String>> strategyConstrFiles = new HashMap<>();
        for(Iterator<IStrategoTerm> strategyIterator = strategyList.iterator(), usedConstrIterator =
            usedConstrList.iterator(); strategyIterator.hasNext(); ) {
            String strategy = Tools.asJavaString(strategyIterator.next());

            IStrategoTerm usedConstrTerms = usedConstrIterator.next();
            Set<String> usedConstrs = new HashSet<>(usedConstrTerms.getSubtermCount());
            for(IStrategoTerm usedConstrTerm : usedConstrTerms) {
                usedConstrs.add(Tools.asJavaString(usedConstrTerm));
            }
            strategyConstrFiles.put(strategy, usedConstrs);

            @Nullable File file = resourceService.localPath(
                CommonPaths.strSepCompStrategyFile(location, input.projectName, moduleName, strategy));
            assert file != null : "Bug in strSepCompStrategyFile or the arguments thereof: returned path is not a file";
            execContext.provide(resourceService.localPath(CommonPaths
                .strSepCompConstrListFile(location, input.projectName, moduleName, strategy)));
            strategyFiles.put(strategy, file);
            execContext.provide(file);
        }
        final Map<String, File> overlayFiles = new HashMap<>();
        for(IStrategoTerm overlayTerm : overlayList) {
            String overlayName = Tools.asJavaString(overlayTerm);
            @Nullable File file = resourceService.localPath(CommonPaths
                .strSepCompOverlayFile(location, input.projectName, moduleName, overlayName));
            assert file != null : "Bug in strSepCompStrategyFile or the arguments thereof: returned path is not a file";
            overlayFiles.put(overlayName, file);
            execContext.provide(file);
        }

        execContext.provide(resourceService
            .localPath(CommonPaths.strSepCompBoilerplateFile(location, input.projectName, moduleName)));

        final List<Import> imports = new ArrayList<>(importsTerm.size());
        for(IStrategoTerm importTerm : importsTerm) {
            try {
                imports.add(Import.fromTerm(importTerm));
            } catch(IOException e) {
                throw new ExecException(e);
            }
        }

        return new Output(moduleName, strategyFiles, strategyConstrFiles, overlayFiles, imports);
    }

    private IStrategoTerm runStrategoCompileBuilder(FileObject resource, String projectName, FileObject projectLocation)
        throws IOException {
        @Nullable ILanguageImpl strategoDialect = languageIdentifierService.identify(resource);
        @Nullable ILanguageImpl strategoLang = dialectService.getBase(strategoDialect);
        final IStrategoTerm ast;
        if(strategoLang == null) {
            strategoLang = strategoDialect;
            strategoDialect = null;
        }
        if(strategoLang == null) {
            @Nullable ILanguage stratego = languageService.getLanguage(STRATEGO_LANG_NAME);
            String extension = resource.getName().getExtension();
            if(stratego != null && extension.equals("rtree")) {
                strategoLang = stratego.activeImpl();
                // support *.rtree (StrategoSugar AST)
                ast = termFactoryService.getGeneric()
                    .parseFromString(readInputStream(resource.getContent().getInputStream()));

            } else {
                throw new IOException(
                    "Cannot find/load Stratego language. Please add source dependency on org.metaborg:org.metaborg.meta.lang.stratego:${metaborgVersion} in metaborg.yaml");
            }
        } else {
            // parse *.str file
            ast = parse(resource, strategoDialect, strategoLang);
        }
        return transform(resource, projectName, projectLocation, strategoLang, ast);
    }

    private IStrategoTerm transform(FileObject resource, String projectName, FileObject projectLocation,
        @Nullable ILanguageImpl strategoLang, final IStrategoTerm ast) throws IOException {
        final @Nullable IProject project = projectService.get(projectLocation);
        assert project != null : "Could not find project in location: " + projectLocation;
        if(!contextService.available(strategoLang)) {
            throw new IOException("Cannot create stratego transformation context");
        }
        final IContext transformContext;
        try {
            transformContext = contextService.get(resource, project, strategoLang);
        } catch(ContextException e) {
            throw new IOException("Cannot create stratego transformation context", e);
        }
        final ITermFactory f = termFactoryService.getGeneric();
        final String projectPath = transformContext.project().location().toString();
        final IStrategoTerm inputTerm = f.makeTuple(f.makeString(projectPath), f.makeString(projectName), ast);
        try {
            @Nullable IStrategoTerm result =
                strategoCommon.invoke(strategoLang, transformContext, inputTerm, COMPILE_STRATEGY_NAME);
            if(result == null) {
                throw new IOException("Normal Stratego strategy failure during execution of " + COMPILE_STRATEGY_NAME);
            }
            return result;
        } catch(MetaborgException e) {
            throw new IOException("Transformation failed", e);
        }
    }

    private IStrategoTerm parse(FileObject resource, @Nullable ILanguageImpl strategoDialect,
        ILanguageImpl strategoLang)
        throws IOException {
        if(strategoDialect != null) {
            final @Nullable SyntaxFacet syntaxFacet = strategoDialect.facet(SyntaxFacet.class);
            assert syntaxFacet != null : "Cannot get Syntax Facet from (non-null) Stratego dialect";
            final @Nullable String dialectName = dialectService.dialectName(strategoDialect);
            assert dialectName != null : "Cannot get dialect name from (non-null) Stratego dialect";
            // Get dialect with stratego imploder setting
            final ILanguageImpl adaptedStrategoDialect =
                dialectService.update(dialectName, syntaxFacet.withImploderSetting(ImploderImplementation.stratego));
            // Update registered dialect back to old one.
            dialectService.update(dialectName, syntaxFacet);
            strategoDialect = adaptedStrategoDialect;
        }

        // PARSE
        final @Nullable IStrategoTerm ast;
        final String text = sourceTextService.text(resource);
        final ISpoofaxInputUnit inputUnit = unitService.inputUnit(resource, text, strategoLang, strategoDialect);
        final ISpoofaxParseUnit parseResult;
        try {
            parseResult = syntaxService.parse(inputUnit);
        } catch(ParseException e) {
            throw new IOException("Cannot parse stratego file " + resource, e);
        }
        ast = parseResult.ast();
        if(!parseResult.valid() || !parseResult.success() || ast == null) {
            throw new IOException("Cannot parse stratego file " + resource);
        }
        return ast;
    }

    /**
     * source: https://stackoverflow.com/a/35446009
     */
    private static String readInputStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while((length = inputStream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString(StandardCharsets.UTF_8.name());
    }

    @Override public String getId() {
        return StrIncrFront.class.getCanonicalName();
    }

    @Override public Serializable key(Input input) {
        return input.inputFile;
    }

    @Override public String desc(Input input) {
        return this.getId() + "(" + input + ")";
    }

    @Override public String desc(Input input, int maxLength) {
        return desc(input);
    }

    @Override public Task<Input, Output> createTask(Input input) {
        return new Task<>(this, input);
    }

    @Override public STask<Input> createSerializableTask(Input input) {
        return new STask<>(this.getId(), input);
    }

}
