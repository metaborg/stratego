package mb.stratego.build;

import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.Logger;
import mb.pie.api.STask;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.output.InconsequentialOutputStamper;
import mb.stratego.build.util.CommonPaths;

import com.google.inject.Inject;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.context.IContext;
import org.metaborg.core.context.IContextService;
import org.metaborg.core.language.ILanguage;
import org.metaborg.core.language.ILanguageComponent;
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
import org.metaborg.spoofax.core.stratego.IStrategoRuntimeService;
import org.metaborg.spoofax.core.stratego.StrategoRuntimeFacet;
import org.metaborg.spoofax.core.syntax.IParseTableProvider;
import org.metaborg.spoofax.core.syntax.IParserConfig;
import org.metaborg.spoofax.core.syntax.ImploderImplementation;
import org.metaborg.spoofax.core.syntax.JSGLR1FileParseTableProvider;
import org.metaborg.spoofax.core.syntax.JSGLR1I;
import org.metaborg.spoofax.core.syntax.JSGLR2FileParseTableProvider;
import org.metaborg.spoofax.core.syntax.JSGLR2I;
import org.metaborg.spoofax.core.syntax.JSGLRI;
import org.metaborg.spoofax.core.syntax.ParserConfig;
import org.metaborg.spoofax.core.syntax.SyntaxFacet;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.metaborg.spoofax.core.unit.ParseContrib;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.jsglr.client.InvalidParseTableException;
import org.spoofax.jsglr2.parsetable.ParseTableReadException;
import org.spoofax.terms.io.binary.TermReader;
import org.strategoxt.HybridInterpreter;
import org.strategoxt.lang.Context;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class StrIncrFront implements TaskDef<StrIncrFront.Input, StrIncrFront.Output> {
    public static final String id = StrIncrFront.class.getCanonicalName();

    public static final class Input implements Serializable {
        final File projectLocation;
        final String inputFileString;
        final String projectName;
        final Collection<STask> originTasks;

        Input(File projectLocation, String inputFileString, String projectName, Collection<STask> originTasks) {
            this.projectLocation = projectLocation;
            this.inputFileString = inputFileString;
            this.projectName = projectName;
            this.originTasks = originTasks;
        }

        @Override public String toString() {
            return "StrIncrFront$Input(" + inputFileString + ')';
        }

        @Override public boolean equals(Object o) {
            if(this == o)
                return true;
            if(o == null || getClass() != o.getClass())
                return false;

            Input input = (Input) o;

            if(!projectLocation.equals(input.projectLocation))
                return false;
            if(!inputFileString.equals(input.inputFileString))
                return false;
            //noinspection SimplifiableIfStatement
            if(!projectName.equals(input.projectName))
                return false;
            return originTasks.equals(input.originTasks);
        }

        @Override public int hashCode() {
            int result = projectLocation.hashCode();
            result = 31 * result + inputFileString.hashCode();
            result = 31 * result + projectName.hashCode();
            result = 31 * result + originTasks.hashCode();
            return result;
        }
    }

    public static final class Output implements Serializable {
        final String moduleName;
        /**
         * Cified-strategy-name to file with CTree definition of that strategy [static linking]
         */
        final Map<String, File> strategyFiles;
        /**
         * Cified-strategy-names referred to in this module [name checks]
         */
        final Set<String> usedStrategies;
        /**
         * Cified-strategy-names-without-arity referred to in this module in an ambiguous position (strategy argument
         * to other strategy) to cified-strategy-names where the ambiguous call occurs [name checks]
         */
        final Map<String, Set<String>> ambStratUsed;
        /**
         * Cified-strategy-name to constructor_arity names that were used in the body [name checks]
         */
        final Map<String, Set<String>> strategyConstrs;
        /**
         * Overlay_arity names to file with CTree definition of that overlay [static linking / name checks]
         */
        final Map<String, File> overlayFiles;
        /**
         * Imports in this module (normal, library or wildcard) [import tracking / name checks]
         */
        final List<Import> imports;
        /**
         * Constructor_arity names defined in this module [name checks]
         */
        final Set<String> constrs;
        /**
         * Cified-strategy-name of a generated congruence [static linking]
         */
        final Set<String> congrs;
        /**
         * Cified-strategy-names of strategies that need a corresponding strategy in a library
         * because it overrides or extends it. [name checks]
         */
        final Set<String> strategyNeedsExternal;
        /**
         * Overlay_arity names to constructor_arity names used. [static linking / name checks]
         */
        final Map<String, Set<String>> overlayConstrs;
        /**
         * Constructor_arity to file with CTree definition of that congruence [static linking]
         */
        final Map<String, File> congrFiles;

        Output(String moduleName, Map<String, File> strategyFiles, Set<String> usedStrategies,
            Map<String, Set<String>> ambStratUsed, Map<String, Set<String>> strategyConstrs,
            Map<String, File> overlayFiles, List<Import> imports, Set<String> constrs, Set<String> congrs,
            Set<String> strategyNeedsExternal, Map<String, Set<String>> overlayConstrs, Map<String, File> congrFiles) {
            this.moduleName = moduleName;
            this.strategyFiles = strategyFiles;
            this.usedStrategies = usedStrategies;
            this.ambStratUsed = ambStratUsed;
            this.strategyConstrs = strategyConstrs;
            this.overlayFiles = overlayFiles;
            this.imports = imports;
            this.constrs = constrs;
            this.congrs = congrs;
            this.strategyNeedsExternal = strategyNeedsExternal;
            this.overlayConstrs = overlayConstrs;
            this.congrFiles = congrFiles;
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
            if(!ambStratUsed.equals(output.ambStratUsed))
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
            result = 31 * result + ambStratUsed.hashCode();
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
            return new Import(ImportType.library, Library.normalizeBuiltin(libraryName));
        }

        static Import fromTerm(IStrategoTerm importTerm) throws IOException {
            if(!(importTerm instanceof IStrategoAppl)) {
                throw new IOException("Import term was not a constructor: " + importTerm);
            }
            final IStrategoAppl appl = (IStrategoAppl) importTerm;
            switch(appl.getName()) {
                case "Import":
                    String importString = Tools.javaStringAt(appl, 0);
                    if(Library.Builtin.isBuiltinLibrary(importString)) {
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
    private final IStrategoRuntimeService strategoRuntimeService;
    private final ITermFactoryService termFactoryService;
    private final IContextService contextService;
    private final IStrategoCommon strategoCommon;
    private final ISourceTextService sourceTextService;
    private static final Map<String, ParserConfig> parserConfigs = new HashMap<>();

    private static final String COMPILE_STRATEGY_NAME = "compile-module";
    private static final String STRATEGO_LANG_NAME = "Stratego-Sugar";

    @Inject public StrIncrFront(IResourceService resourceService, IProjectService projectService,
        ILanguageIdentifierService languageIdentifierService, IDialectService dialectService,
        ILanguageService languageService, IStrategoRuntimeService strategoRuntimeService,
        ITermFactoryService termFactoryService, IContextService contextService, IStrategoCommon strategoCommon,
        ISourceTextService sourceTextService) {
        this.resourceService = resourceService;
        this.projectService = projectService;
        this.languageIdentifierService = languageIdentifierService;
        this.dialectService = dialectService;
        this.languageService = languageService;
        this.strategoRuntimeService = strategoRuntimeService;
        this.termFactoryService = termFactoryService;
        this.contextService = contextService;
        this.strategoCommon = strategoCommon;
        this.sourceTextService = sourceTextService;
    }


    @Override public Output exec(ExecContext execContext, Input input) throws Exception {
        for(final STask t : input.originTasks) {
            execContext.require(t, InconsequentialOutputStamper.instance);
        }

        final FileObject location = resourceService.resolve(input.projectLocation);
        final FileObject inputFile = resourceService.resolve(input.inputFileString);
        final long startTime = System.nanoTime();
        final IStrategoTerm result =
            runStrategoCompileBuilder(execContext.logger(), inputFile, input.projectName, location);
        execContext.logger().debug(
            "\"FrontEnd task stratego related code took\", " + (System.nanoTime() - startTime) + ", \""
                + input.projectLocation.toPath().relativize(Paths.get(input.inputFileString)) + "\"");

        execContext.require(resourceService.localFile(inputFile));
        // TODO: reinstate support for files from within a jar? Where was this used again?
        //        if(inputURI.getScheme().equals("jar")) {
        //            JarURLConnection c = ((JarURLConnection) inputURI.openConnection());
        //            try(FileSystem fs = FileSystems
        //                .newFileSystem(URI.create("jar:" + c.getJarFileURL().toString()), Collections.emptyMap())) {
        //                execContext.require(fs.getPath(c.getEntryName()));
        //            }
        //        } else {
        //            execContext.require(new File(inputURI));
        //        }

        final String moduleName = Tools.javaStringAt(result, 0);
        final IStrategoList strategyList = Tools.listAt(result, 1);
        final IStrategoList usedStrategyList = Tools.listAt(result, 2);
        final IStrategoList ambStratsUsed = Tools.listAt(result, 3);
        final IStrategoList importsTerm = Tools.listAt(result, 4);
        final IStrategoList constrList = Tools.listAt(result, 5);
        final IStrategoList overlayList = Tools.listAt(result, 6);
        final IStrategoList congrList = Tools.listAt(result, 7);

        final Map<String, File> strategyFiles = new HashMap<>(strategyList.size() * 2);
        final Map<String, Set<String>> strategyConstrs = new HashMap<>(strategyList.size() * 2);
        final Set<String> strategyNeedsExternal = new HashSet<>();
        for(IStrategoTerm strategyTriple : strategyList) {
            String strategy = Tools.javaStringAt(strategyTriple, 0);

            IStrategoList usedConstrTerms = Tools.listAt(strategyTriple, 1);
            Set<String> usedConstrs = new HashSet<>(usedConstrTerms.getSubtermCount());
            for(IStrategoTerm usedConstrTerm : usedConstrTerms) {
                usedConstrs.add(Tools.asJavaString(usedConstrTerm));
            }
            strategyConstrs.put(strategy, usedConstrs);

            IStrategoList annos = Tools.listAt(strategyTriple, 2);
            for(IStrategoTerm anno : annos) {
                if(anno.getSubtermCount() == 0 && anno.getTermType() == IStrategoTerm.APPL) {
                    String annoName = ((IStrategoAppl) anno).getName();
                    if(annoName.equals("Override") || annoName.equals("Extend")) {
                        strategyNeedsExternal.add(strategy);
                        break;
                    }
                }
            }

            final @Nullable File strategyFile = resourceService
                .localPath(CommonPaths.strSepCompStrategyFile(location, input.projectName, moduleName, strategy));
            assert strategyFile
                != null : "Bug in strSepCompStrategyFile or the arguments thereof: returned path is not a file";
            strategyFiles.put(strategy, strategyFile);
            execContext.provide(strategyFile);
        }
        final Map<String, Set<String>> ambStratUsed = new HashMap<>(ambStratsUsed.size() * 2);
        for(IStrategoTerm ambStratUse : ambStratsUsed) {
            final String ambName = Tools.javaStringAt(ambStratUse, 0);
            if(!ambName.endsWith("_0_0")) {
                // Inner strategies that were lifted don't have any arity info in their name and aren't ambiguous uses
                if(!StrIncr.stripArityPattern.matcher(ambName).matches()) {
                    continue;
                }
                throw new ExecException(
                    "Bug in Strategy sep comp frontend: Ambiguous call name '" + ambName + "' didn't end with _0_0.");
            }
            final String useSite = Tools.javaStringAt(ambStratUse, 1);
            StrIncr.getOrInitialize(ambStratUsed, ambName, HashSet::new).add(useSite);
        }
        final Map<String, File> overlayFiles = new HashMap<>(overlayList.size() * 2);
        final Map<String, Set<String>> overlayConstrs = new HashMap<>(overlayList.size() * 2);
        for(IStrategoTerm overlayPair : overlayList) {
            String overlayName = Tools.javaStringAt(overlayPair, 0);
            @Nullable File overlayFile = resourceService
                .localPath(CommonPaths.strSepCompOverlayFile(location, input.projectName, moduleName, overlayName));
            assert overlayFile
                != null : "Bug in strSepCompStrategyFile or the arguments thereof: returned path is not a file";
            overlayFiles.put(overlayName, overlayFile);
            execContext.provide(overlayFile);

            IStrategoList usedConstrTerms = Tools.listAt(overlayPair, 1);
            Set<String> usedConstrs = new HashSet<>(usedConstrTerms.getSubtermCount());
            for(IStrategoTerm usedConstrTerm : usedConstrTerms) {
                usedConstrs.add(Tools.asJavaString(usedConstrTerm));
            }
            overlayConstrs.put(overlayName, usedConstrs);
        }
        final Set<String> constrs = new HashSet<>(constrList.size() * 2);
        for(IStrategoTerm constr : constrList) {
            constrs.add(Tools.asJavaString(constr));
        }
        final Set<String> usedStrategies = new HashSet<>(usedStrategyList.size() * 2);
        for(IStrategoTerm usedStrategy : usedStrategyList) {
            usedStrategies.add(Tools.asJavaString(usedStrategy));
        }
        final Set<String> congrs = new HashSet<>(congrList.size() * 2);
        final Map<String, File> congrFiles = new HashMap<>(congrList.size() * 2);
        for(IStrategoTerm congrPair : congrList) {
            String congrName = Tools.javaStringAt(congrPair, 0);

            IStrategoList usedConstrTerms = Tools.listAt(congrPair, 1);
            Set<String> usedConstrs = new HashSet<>(usedConstrTerms.getSubtermCount());
            for(IStrategoTerm usedConstrTerm : usedConstrTerms) {
                usedConstrs.add(Tools.asJavaString(usedConstrTerm));
            }
            strategyConstrs.put(congrName, usedConstrs);

            congrs.add(congrName + "_0");
            final @Nullable File congrFile = resourceService
                .localPath(CommonPaths.strSepCompStrategyFile(location, input.projectName, moduleName, congrName));
            assert congrFile
                != null : "Bug in strSepCompStrategyFile or the arguments thereof: returned path is not a file";
            congrFiles.put(congrName, congrFile);
            execContext.provide(congrFile);
        }

        final @Nullable File boilerplateFile =
            resourceService.localPath(CommonPaths.strSepCompBoilerplateFile(location, input.projectName, moduleName));
        assert boilerplateFile
            != null : "Bug in strSepCompBoilerplateFile or the arguments thereof: returned path is not a file";
        execContext.provide(boilerplateFile);

        final List<Import> imports = new ArrayList<>(importsTerm.size());
        for(IStrategoTerm importTerm : importsTerm) {
            imports.add(Import.fromTerm(importTerm));
        }

        return new Output(moduleName, strategyFiles, usedStrategies, ambStratUsed, strategyConstrs, overlayFiles,
            imports, constrs, congrs, strategyNeedsExternal, overlayConstrs, congrFiles);
    }

    private IStrategoTerm runStrategoCompileBuilder(Logger logger, FileObject inputFile, String projectName,
        FileObject projectLocation) throws Exception {
        final long startTime = System.nanoTime();
        @Nullable ILanguageImpl strategoDialect = languageIdentifierService.identify(inputFile);
        @Nullable ILanguageImpl strategoLang = dialectService.getBase(strategoDialect);
        IStrategoTerm ast;
        if(strategoLang == null) {
            strategoLang = strategoDialect;
            strategoDialect = null;
        }
        if(strategoLang == null) {
            @Nullable ILanguage stratego = languageService.getLanguage(STRATEGO_LANG_NAME);
            String extension = inputFile.getName().getExtension();
            if(stratego != null && extension.equals("rtree")) {
                strategoLang = stratego.activeImpl();
                // support *.rtree (StrategoSugar AST)
                final ITermFactory factory = termFactoryService.getGeneric();
                ast = new TermReader(factory).parseFromStream(inputFile.getContent().getInputStream());
                if(!(ast instanceof IStrategoAppl && ((IStrategoAppl) ast).getName().equals("Module")
                    && ast.getSubtermCount() == 2)) {
                    throw new ExecException(
                        "Did not find Module/2 in RTree file. Bug in custom library detection? (If file contains Specification/1 with only external definitions, then yes). Found: \n"
                            + ast.toString(2));
                }
            } else {
                throw new ExecException(
                    "Cannot find/load Stratego language. Please add source dependency on org.metaborg:org.metaborg.meta.lang.stratego:${metaborgVersion} in metaborg.yaml");
            }
        } else {
            // parse *.str file
            ast = parse(inputFile, strategoDialect, strategoLang);
        }
        logger.debug("\"Parsing took\", " + (System.nanoTime() - startTime));
        return transform(logger, inputFile, projectName, projectLocation, strategoLang, ast);
    }

    private IStrategoTerm transform(Logger logger, FileObject inputFile, String projectName, FileObject projectLocation,
        @Nullable ILanguageImpl strategoLang, final IStrategoTerm ast) throws Exception {
        final long startTime = System.nanoTime();
        final @Nullable IProject project = projectService.get(projectLocation);
        assert project != null : "Could not find project in location: " + projectLocation;
        if(!contextService.available(strategoLang)) {
            throw new ExecException("Cannot create stratego transformation context");
        }
        final IContext transformContext = contextService.get(inputFile, project, strategoLang);
        final ITermFactory f = termFactoryService.getGeneric();
        final String projectPath = transformContext.project().location().toString();
        final IStrategoTerm inputTerm = f.makeTuple(f.makeString(projectPath), f.makeString(projectName), ast);
        final long beforeStrategoCommonCall = System.nanoTime();
        logger.debug("\"Getting project/context/factory took\", " + (beforeStrategoCommonCall - startTime));
        final HybridInterpreter interpreter =
            strategoRuntimeService.runtime(getComponent(strategoLang), transformContext, false);
        interpreter.getContext().setContextObject(transformContext);
        interpreter.getCompiledContext().setContextObject(transformContext);
        @Nullable IStrategoTerm result = strategoCommon.invoke(interpreter, inputTerm, COMPILE_STRATEGY_NAME);
        logger.debug("\"StrategoCommon#invoke took\", " + (System.nanoTime() - beforeStrategoCommonCall));
        if(result == null) {
            throw new ExecException("Normal Stratego strategy failure during execution of " + COMPILE_STRATEGY_NAME);
        }
        return result;
    }

    private static ILanguageComponent getComponent(@Nullable ILanguageImpl language) throws ExecException {
        if(language != null) {
            for(ILanguageComponent component : language.components()) {
                if(component.facet(StrategoRuntimeFacet.class) == null) {
                    continue;
                }
                return component;
            }
        }
        throw new ExecException("Could not find StrategoRuntime component for Stratego.lang");
    }

    private IStrategoTerm parse(FileObject inputFile, @Nullable ILanguageImpl strategoDialect,
        ILanguageImpl strategoLang) throws ParseException, ExecException {
        final ImploderImplementation imploder;
        final ILanguageImpl langImpl;
        if(strategoDialect != null) {
            langImpl = strategoDialect;
            imploder = ImploderImplementation.stratego;
        } else {
            langImpl = strategoLang;
            imploder = ImploderImplementation.java;
        }
        final ITermFactory termFactory = termFactoryService.get(strategoLang, null, false);

        final IParserConfig config = getParserConfig(findParseTable(langImpl), imploder);
        try {
            final String inputText = sourceTextService.text(inputFile);
            final JSGLRI<?> parser;

            if(imploder == ImploderImplementation.java) {
                parser = new JSGLR2I(config, termFactory, strategoLang, null, inputFile, inputText, false, false);
            } else {
                final Context context = strategoRuntimeService.genericRuntime().getCompiledContext();
                parser = new JSGLR1I(config, termFactory, context, strategoLang, strategoDialect, inputFile, inputText);
            }

            final ParseContrib contrib = parser.parse(null);

            if(!contrib.valid || !contrib.success || contrib.ast == null) {
                throw new ExecException(
                    "Cannot parse stratego file " + inputFile + ": parsing failed with" + (!contrib.valid ? " errors" :
                        (!contrib.success) ? "out errors" : " ast == null"));
            }

            return contrib.ast;
        } catch(IOException | ParseTableReadException | InvalidParseTableException e) {
            throw new ParseException(null, e);
        }
    }

    private IParserConfig getParserConfig(FileObject parseTable, ImploderImplementation imploder)
        throws ParseException {
        if(parserConfigs.containsKey(parseTable.toString())) {
            return parserConfigs.get(parseTable.toString());
        }
        final ITermFactory termFactory =
            termFactoryService.getGeneric().getFactoryWithStorageType(IStrategoTerm.MUTABLE);
        final IParseTableProvider provider;
        if(imploder == ImploderImplementation.java) {
            provider = new JSGLR2FileParseTableProvider(parseTable, termFactory);
        } else {
            provider = new JSGLR1FileParseTableProvider(parseTable, termFactory);
        }
        final ParserConfig config = new ParserConfig("Module", provider, imploder);
        parserConfigs.put(parseTable.toString(), config);
        return config;
    }

    private static FileObject findParseTable(ILanguageImpl lang) throws ParseException {
        final SyntaxFacet facet = Objects.requireNonNull(lang.facet(SyntaxFacet.class));
        @Nullable FileObject parseTable = null;
        if(facet.parseTable == null) {
            try {
                boolean multipleTables = false;
                for(ILanguageComponent component : lang.components()) {
                    if(component.config().sdfEnabled()) {
                        if(component.config().parseTable() != null) {
                            if(multipleTables) {
                                throw new ParseException(null);
                            }

                            parseTable = component.location().resolveFile(component.config().parseTable());
                            multipleTables = true;
                        }
                    }
                }
            } catch(FileSystemException e) {
                throw new ParseException(null, e);
            }
        } else {
            parseTable = facet.parseTable;
        }
        return Objects.requireNonNull(parseTable);
    }

    @Override public String getId() {
        return id;
    }

    @Override public Serializable key(Input input) {
        return input.inputFileString;
    }

}
