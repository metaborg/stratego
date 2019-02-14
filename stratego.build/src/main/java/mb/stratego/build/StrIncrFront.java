package mb.stratego.build;

import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.STask;
import mb.pie.api.Task;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.output.InconsequentialOutputStamper;
import mb.stratego.build.util.CommonPaths;

import com.google.inject.Inject;
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
import org.spoofax.terms.io.binary.TermReader;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StrIncrFront implements TaskDef<StrIncrFront.Input, StrIncrFront.Output> {
    public static final String id = StrIncrFront.class.getCanonicalName();

    public static final class Input implements Serializable {
        final File projectLocation;
        final URL inputFile;
        final String projectName;
        final Collection<STask<?>> originTasks;

        Input(File projectLocation, URL inputFile, String projectName, Collection<STask<?>> originTasks) {
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
        /**
         * Strategy-name to file with CTree definition of that strategy
         */
        final Map<String, File> strategyFiles;
        /**
         * Cified-strategy-names defined in this module
         */
        final Set<String> strategies;
        /**
         * Cified-strategy-names referred to in this module
         */
        final Set<String> usedStrategies;
        /**
         * Strategy-name to constructor_arity names that were used in the body
         */
        final Map<String, Set<String>> strategyConstrs;
        /**
         * Overlay_arity names to file with CTree definition of that overlay
         */
        final Map<String, File> overlayFiles;
        /**
         * Imports in this module (normal, library or wildcard)
         */
        final List<Import> imports;
        /**
         * Constructor_arity names used in this module
         */
        final Set<String> constrs;

        Output(String moduleName, Map<String, File> strategyFiles, Set<String> strategies, Set<String> usedStrategies,
            Map<String, Set<String>> strategyConstrs, Map<String, File> overlayFiles, List<Import> imports,
            Set<String> constrs) {
            this.moduleName = moduleName;
            this.strategyFiles = strategyFiles;
            this.strategies = strategies;
            this.usedStrategies = usedStrategies;
            this.strategyConstrs = strategyConstrs;
            this.overlayFiles = overlayFiles;
            this.imports = imports;
            this.constrs = constrs;
        }

        @Override public String toString() {
            return "StrIncrFront$Output(" + moduleName + ')';
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
            if(!usedStrategies.equals(output.usedStrategies))
                return false;
            if(!strategyConstrs.equals(output.strategyConstrs))
                return false;
            if(!overlayFiles.equals(output.overlayFiles))
                return false;
            //noinspection SimplifiableIfStatement
            if(!imports.equals(output.imports))
                return false;
            return constrs.equals(output.constrs);
        }

        @Override public int hashCode() {
            int result = moduleName.hashCode();
            result = 31 * result + strategyFiles.hashCode();
            result = 31 * result + usedStrategies.hashCode();
            result = 31 * result + strategyConstrs.hashCode();
            result = 31 * result + overlayFiles.hashCode();
            result = 31 * result + imports.hashCode();
            result = 31 * result + constrs.hashCode();
            return result;
        }
    }

    public static final class Import implements Serializable {
        public enum ImportType {
            normal, wildcard, library
        }

        final ImportType type;
        final String path;

        Import(ImportType type, String path) {
            this.type = type;
            this.path = path;
        }

        static Import normal(String importString) {
            return new Import(ImportType.normal, importString);
        }

        static Import wildcard(String importString) {
            return new Import(ImportType.wildcard, importString);
        }

        static Import library(String libraryName) {
            assert StrIncrFrontLib.builtinLibraries.contains(libraryName) || StrIncrFrontLib.builtinLibraries
                .contains("lib" + libraryName);
            return new Import(ImportType.library, libraryName);
        }

        static Import fromTerm(IStrategoTerm importTerm) throws IOException {
            if(!(importTerm instanceof IStrategoAppl)) {
                throw new IOException("Import term was not a constructor: " + importTerm);
            }
            final IStrategoAppl appl = (IStrategoAppl) importTerm;
            switch(appl.getName()) {
                case "Import":
                    String importString = Tools.javaStringAt(appl, 0);
                    if(StrIncrFrontLib.builtinLibraries.contains(importString)) {
                        return library(importString);
                    } else {
                        return normal(importString);
                    }
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
            if(type != anImport.type)
                return false;
            return path.equals(anImport.path);
        }

        @Override public int hashCode() {
            int result = type.hashCode();
            result = 31 * result + path.hashCode();
            return result;
        }

        @Override public String toString() {
            return "Import(" + type + ", '" + path + '\'' + ')';
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
        ILanguageIdentifierService languageIdentifierService, IDialectService dialectService,
        ILanguageService languageService, ITermFactoryService termFactoryService, IContextService contextService,
        IStrategoCommon strategoCommon, ISourceTextService sourceTextService, ISpoofaxUnitService unitService,
        ISpoofaxSyntaxService syntaxService) {
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
        URI inputURI;
        try {
            inputURI = input.inputFile.toURI();
        } catch(URISyntaxException e) {
            throw new ExecException(e);
        }
        final FileObject resource = resourceService.resolve(inputURI);
        final IStrategoTerm result;
        try {
            result = runStrategoCompileBuilder(resource, input.projectName, location);
        } catch(IOException e) {
            throw new ExecException(e);
        }

        if(input.inputFile.getProtocol().equals("jar")) {
            JarURLConnection c;
            try {
                c = ((JarURLConnection) input.inputFile.openConnection());
            } catch(IOException e) {
                throw new ExecException(e);
            }
            try(FileSystem fs = FileSystems
                .newFileSystem(URI.create("jar:" + c.getJarFileURL().toString()), Collections.emptyMap())) {
                execContext.require(fs.getPath(c.getEntryName()));
            } catch(IOException e) {
                throw new ExecException(e);
            }
        } else {
            execContext.require(new File(inputURI));
        }

        final String moduleName = Tools.javaStringAt(result, 0);
        final IStrategoList strategyList = Tools.listAt(result, 1);
        final IStrategoList cifiedStratNameList = Tools.listAt(result, 2);
        final IStrategoList usedStrategyList = Tools.listAt(result, 3);
        final IStrategoList importsTerm = Tools.listAt(result, 4);
        final IStrategoList constrList = Tools.listAt(result, 5);
        final IStrategoList usedConstrList = Tools.listAt(result, 6);
        final IStrategoList overlayList = Tools.listAt(result, 7);
        assert
            strategyList.size() == usedConstrList.size() :
            "Inconsistent compiler: strategy list size (" + strategyList.size() + ") != used constructors list size ("
                + usedConstrList.size() + ")";

        final Map<String, File> strategyFiles = new HashMap<>();
        final Map<String, Set<String>> strategyConstrs = new HashMap<>();
        for(Iterator<IStrategoTerm> strategyIterator = strategyList.iterator(), usedConstrIterator =
            usedConstrList.iterator(); strategyIterator.hasNext(); ) {
            String strategy = Tools.asJavaString(strategyIterator.next());

            IStrategoTerm usedConstrTerms = usedConstrIterator.next();
            Set<String> usedConstrs = new HashSet<>(usedConstrTerms.getSubtermCount());
            for(IStrategoTerm usedConstrTerm : usedConstrTerms) {
                usedConstrs.add(Tools.asJavaString(usedConstrTerm));
            }
            strategyConstrs.put(strategy, usedConstrs);

            @Nullable File file = resourceService
                .localPath(CommonPaths.strSepCompStrategyFile(location, input.projectName, moduleName, strategy));
            assert file != null : "Bug in strSepCompStrategyFile or the arguments thereof: returned path is not a file";
            execContext.provide(resourceService
                .localPath(CommonPaths.strSepCompConstrListFile(location, input.projectName, moduleName, strategy)));
            strategyFiles.put(strategy, file);
            execContext.provide(file);
        }
        final Set<String> strategies = new HashSet<>();
        for(IStrategoTerm cifiedStratName : cifiedStratNameList) {
            strategies.add(Tools.asJavaString(cifiedStratName));
        }
        final Map<String, File> overlayFiles = new HashMap<>();
        for(IStrategoTerm overlayTerm : overlayList) {
            String overlayName = Tools.asJavaString(overlayTerm);
            @Nullable File file = resourceService
                .localPath(CommonPaths.strSepCompOverlayFile(location, input.projectName, moduleName, overlayName));
            assert file != null : "Bug in strSepCompStrategyFile or the arguments thereof: returned path is not a file";
            overlayFiles.put(overlayName, file);
            execContext.provide(file);
        }
        final Set<String> constrs = new HashSet<>(constrList.size() * 2);
        for(IStrategoTerm constr : constrList) {
            constrs.add(Tools.asJavaString(constr));
        }
        final Set<String> usedStrategies = new HashSet<>(usedStrategyList.size() * 2);
        for(IStrategoTerm usedStrategy : usedStrategyList) {
            usedStrategies.add(Tools.asJavaString(usedStrategy));
        }

        execContext.provide(
            resourceService.localPath(CommonPaths.strSepCompBoilerplateFile(location, input.projectName, moduleName)));

        final List<Import> imports = new ArrayList<>(importsTerm.size());
        for(IStrategoTerm importTerm : importsTerm) {
            try {
                imports.add(Import.fromTerm(importTerm));
            } catch(IOException e) {
                throw new ExecException(e);
            }
        }

        return new Output(moduleName, strategyFiles, strategies, usedStrategies, strategyConstrs, overlayFiles, imports,
            constrs);
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
                ast = new TermReader(termFactoryService.getGeneric())
                    .parseFromStream(resource.getContent().getInputStream());

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
        final @Nullable IProject project = projectService.get(projectLocation.resolveFile("metaborg.yaml"));
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
        final IStrategoTerm inputTerm;
        inputTerm = f.makeTuple(f.makeString(projectPath), f.makeString(projectName), ast);
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
        ILanguageImpl strategoLang) throws IOException {
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

    @Override public String getId() {
        return id;
    }

    @Override public Serializable key(Input input) {
        return input.inputFile;
    }

    @Override public String desc(Input input) {
        return TaskDef.DefaultImpls.desc(this, input);
    }

    @Override public String desc(Input input, int maxLength) {
        return TaskDef.DefaultImpls.desc(this, input, maxLength);
    }

    @Override public Task<Input, StrIncrFront.Output> createTask(Input input) {
        return TaskDef.DefaultImpls.createTask(this, input);
    }

    @Override public STask<Input> createSerializableTask(Input input) {
        return TaskDef.DefaultImpls.createSerializableTask(this, input);
    }

}
