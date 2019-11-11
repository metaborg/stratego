package mb.stratego.build;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.ILanguage;
import org.metaborg.core.language.ILanguageIdentifierService;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.ILanguageService;
import org.metaborg.core.language.dialect.IDialectService;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.core.source.ISourceTextService;
import org.metaborg.core.syntax.ParseException;
import org.metaborg.spoofax.core.SpoofaxConstants;
import org.metaborg.spoofax.core.syntax.ISpoofaxSyntaxService;
import org.metaborg.spoofax.core.syntax.ImploderImplementation;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.metaborg.spoofax.core.unit.ISpoofaxInputUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxUnitService;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.TermFactory;
import org.spoofax.terms.TermVisitor;
import org.spoofax.terms.io.TAFTermReader;
import org.spoofax.terms.io.binary.TermReader;
import org.strategoxt.lang.Context;

import com.google.inject.Inject;

import mb.flowspec.terms.StrategoArrayList;
import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.STask;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.output.InconsequentialOutputStamper;
import mb.stratego.build.util.CommonPaths;

public class StrIncrFront implements TaskDef<StrIncrFront.Input, StrIncrFront.Output> {
    public static final String id = StrIncrFront.class.getCanonicalName();

    public static final class Input implements Serializable {
        final File projectLocation;
        final String inputFileString;
        final String projectName;
        final Collection<STask> originTasks;
        transient Context context;

        Input(File projectLocation, String inputFileString, String projectName, Collection<STask> originTasks, Context context) {
            this.projectLocation = projectLocation;
            this.inputFileString = inputFileString;
            this.projectName = projectName;
            this.originTasks = originTasks;
            this.context = context;
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
            // noinspection SimplifiableIfStatement
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

    public static abstract class Output implements Serializable {
        abstract @Nullable NormalOutput normalOutput();

        abstract @Nullable FileRemovedOutput fileRemovedOutput();
    }

    public static final class NormalOutput extends Output {
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
         * Cified-strategy-names-without-arity referred to in this module in an ambiguous position (strategy argument to
         * other strategy) to cified-strategy-names where the ambiguous call occurs [name checks]
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
         * Cified-strategy-names of strategies that need a corresponding strategy in a library because it overrides or
         * extends it. [name checks]
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
        /**
         * Cified-strategy-name to no. of separate definitions found in the file before merging [statistics]
         */
        final Map<String, Integer> noOfDefinitions;
        /**
         * Cified-strategy-name to CTree definition of that strategy [in-memory optimization]
         */
        transient Map<String, IStrategoAppl> strategyASTs;
        /**
         * Overlay_arity names to CTree definition of that strategy [in-memory optimization]
         */
        transient Map<String, List<IStrategoAppl>> overlayASTs;
        /**
         * Constructor_arity names to CTree definition of that strategy [in-memory optimization]
         */
        transient Map<String, IStrategoAppl> congrASTs;

        NormalOutput(String moduleName, Map<String, File> strategyFiles, Set<String> usedStrategies,
            Map<String, Set<String>> ambStratUsed, Map<String, Set<String>> strategyConstrs,
            Map<String, File> overlayFiles, List<Import> imports, Set<String> constrs, Set<String> congrs,
            Set<String> strategyNeedsExternal, Map<String, Set<String>> overlayConstrs, Map<String, File> congrFiles,
            Map<String, Integer> noOfDefinitions, Map<String, IStrategoAppl> strategyASTs,
            Map<String, List<IStrategoAppl>> overlayASTs, Map<String, IStrategoAppl> congrASTs) {
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
            this.noOfDefinitions = noOfDefinitions;
            this.strategyASTs = strategyASTs;
            this.overlayASTs = overlayASTs;
            this.congrASTs = congrASTs;
        }

        @Override public String toString() {
            return "StrIncrFront$Output(" + moduleName + ')';
        }

        private void writeObject(ObjectOutputStream out) throws IOException {
            writeASTs();
            out.defaultWriteObject();
        }

        private void writeASTs() throws IOException {
            for(Map.Entry<String, File> entry : strategyFiles.entrySet()) {
                final IStrategoAppl strategyAST = strategyASTs.get(entry.getKey());
                final File strategyFile = entry.getValue();
                ensureEmptyFileExists(strategyFile);

                try(final OutputStream outputStream = new FileOutputStream(strategyFile)) {
                    // N.B. unparseToFile(IStrategoTerm, OutputStream) buffers, so we don't
                    new TAFTermReader(null).unparseToFile(strategyAST, outputStream);
                }
            }
            for(Map.Entry<String, File> entry : congrFiles.entrySet()) {
                final IStrategoAppl congrAST = congrASTs.get(entry.getKey());
                final File congrFile = entry.getValue();
                ensureEmptyFileExists(congrFile);

                try(final OutputStream outputStream = new FileOutputStream(congrFile)) {
                    // N.B. unparseToFile(IStrategoTerm, OutputStream) buffers, so we don't
                    new TAFTermReader(null).unparseToFile(congrAST, outputStream);
                }
            }
            for(Map.Entry<String, File> entry : overlayFiles.entrySet()) {
                final List<IStrategoAppl> overlayASTList = overlayASTs.get(entry.getKey());
                final File overlayFile = entry.getValue();
                ensureEmptyFileExists(overlayFile);

                try(final Writer writer = new BufferedWriter(new FileWriter(overlayFile))) {
                    String sep = "";
                    for(IStrategoAppl overlayAST : overlayASTList) {
                        writer.write(sep);
                        // N.B. unparseToFile(IStrategoTerm, Writer) doesn't buffer, so we can reuse our own buffer
                        new TAFTermReader(null).unparseToFile(overlayAST, writer);
                        sep = "\n";
                    }
                }
            }
        }

        private static void ensureEmptyFileExists(File file) throws IOException {
            if(file.exists()) {
                Files.write(file.toPath(), new byte[0], StandardOpenOption.TRUNCATE_EXISTING);
            } else {
                final File parentFile = file.getParentFile();
                if(!parentFile.exists()) {
                    Files.createDirectories(parentFile.toPath());
                }
                boolean created = file.createNewFile();
                assert created;
            }
        }

        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            in.defaultReadObject();
            readASTs();
        }

        private void readASTs() throws IOException {
            strategyASTs = new HashMap<>(strategyFiles.size() * 2);
            final TAFTermReader tafTermReader = new TAFTermReader(new TermFactory());
            for(Map.Entry<String, File> entry : strategyFiles.entrySet()) {
                try(final InputStream is = new FileInputStream(entry.getValue())) {
                    strategyASTs.put(entry.getKey(), (IStrategoAppl) tafTermReader.parseFromStream(is));
                }
            }
            congrASTs = new HashMap<>(congrFiles.size() * 2);
            for(Map.Entry<String, File> entry : congrFiles.entrySet()) {
                try(final InputStream is = new FileInputStream(entry.getValue())) {
                    congrASTs.put(entry.getKey(), (IStrategoAppl) tafTermReader.parseFromStream(is));
                }
            }
            overlayASTs = new HashMap<>(overlayFiles.size() * 2);
            for(Map.Entry<String, File> entry : overlayFiles.entrySet()) {
                try(final BufferedReader br = new BufferedReader(new FileReader(entry.getValue()))) {
                    List<IStrategoAppl> overlayASTList = new ArrayList<>();
                    for(String line; (line = br.readLine()) != null;) {
                        overlayASTList.add((IStrategoAppl) tafTermReader.parseFromString(line));
                    }
                    overlayASTs.put(entry.getKey(), overlayASTList);
                }
            }
        }

        @Override NormalOutput normalOutput() {
            return this;
        }

        @Override FileRemovedOutput fileRemovedOutput() {
            return null;
        }

        @Override public boolean equals(Object o) {
            if(this == o)
                return true;
            if(o == null || getClass() != o.getClass())
                return false;

            NormalOutput output = (NormalOutput) o;

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
            if(!imports.equals(output.imports))
                return false;
            if(!constrs.equals(output.constrs))
                return false;
            if(!congrs.equals(output.congrs))
                return false;
            if(!strategyNeedsExternal.equals(output.strategyNeedsExternal))
                return false;
            if(!overlayConstrs.equals(output.overlayConstrs))
                return false;
            if(!congrFiles.equals(output.congrFiles))
                return false;
            if(!strategyASTs.equals(output.strategyASTs))
                return false;
            // noinspection SimplifiableIfStatement
            if(!overlayASTs.equals(output.overlayASTs))
                return false;
            return congrASTs.equals(output.congrASTs);
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
            result = 31 * result + congrs.hashCode();
            result = 31 * result + strategyNeedsExternal.hashCode();
            result = 31 * result + overlayConstrs.hashCode();
            result = 31 * result + congrFiles.hashCode();
            result = 31 * result + strategyASTs.hashCode();
            result = 31 * result + overlayASTs.hashCode();
            result = 31 * result + congrASTs.hashCode();
            return result;
        }
    }

    public static final class FileRemovedOutput extends Output {
        public static final FileRemovedOutput instance = new FileRemovedOutput();

        private FileRemovedOutput() {
        }

        @Nullable @Override NormalOutput normalOutput() {
            return null;
        }

        @Override FileRemovedOutput fileRemovedOutput() {
            return this;
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

            // noinspection SimplifiableIfStatement
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
    private final ILanguageIdentifierService languageIdentifierService;
    private final IDialectService dialectService;
    private final ILanguageService languageService;
    private final ITermFactoryService termFactoryService;
    private final ISourceTextService sourceTextService;
    private final ISpoofaxUnitService unitService;
    private final ISpoofaxSyntaxService syntaxService;
    private final StrIncrSubFront strIncrThenFront;

    private ILanguageImpl strategoLang;

    @Inject public StrIncrFront(IResourceService resourceService,
        ILanguageIdentifierService languageIdentifierService, IDialectService dialectService,
        ILanguageService languageService, ITermFactoryService termFactoryService, ISourceTextService sourceTextService,
        ISpoofaxUnitService unitService, ISpoofaxSyntaxService syntaxService, StrIncrSubFront strIncrThenFront) {
        this.resourceService = resourceService;
        this.languageIdentifierService = languageIdentifierService;
        this.dialectService = dialectService;
        this.languageService = languageService;
        this.termFactoryService = termFactoryService;
        this.sourceTextService = sourceTextService;
        this.unitService = unitService;
        this.syntaxService = syntaxService;
        this.strIncrThenFront = strIncrThenFront;
    }


    @Override public Output exec(ExecContext execContext, Input input) throws Exception {
        BuildStats.executedFrontTasks++;
        /*
         * Note that we require the sdf tasks here because we may be reading a Stratego file that was generated by one
         * of those tasks and that dependency is not allowed to be hidden from the build system. To make sure that
         * front-end tasks only run when their input _files_ change, we need the front-end to depend on the sdf tasks
         * with a simple stamper that allows the execution of the sdf task to be ignored. The execution of the sdf task
         * is forced in the main task StrIncr before it starts frontend tasks and searches for Stratego files through
         * imports.
         */
        for(final STask t : input.originTasks) {
            execContext.require(t, InconsequentialOutputStamper.instance);
        }

        final FileObject location = resourceService.resolve(input.projectLocation);
        final FileObject inputFile = resourceService.resolve(input.inputFileString);

        execContext.require(resourceService.localFile(inputFile));
        if(!inputFile.exists()) {
            return FileRemovedOutput.instance;
        }

        // TODO: reinstate support for files from within a jar? Where was this used again?
        // if(inputURI.getScheme().equals("jar")) {
        // JarURLConnection c = ((JarURLConnection) inputURI.openConnection());
        // try(FileSystem fs = FileSystems
        // .newFileSystem(URI.create("jar:" + c.getJarFileURL().toString()), Collections.emptyMap())) {
        // execContext.require(fs.getPath(c.getEntryName()));
        // }
        // } else {
        // execContext.require(new File(inputURI));
        // }

        final long startTime = System.nanoTime();
        final IStrategoTerm ast = parseFile(inputFile, input.projectName, location);

        StrIncrSubFront.Input frontInput = new StrIncrSubFront.Input(input.projectLocation, input.inputFileString, input.inputFileString,
            input.context, StrIncrSubFront.InputType.Split, ast);
        final SplitResult splitResult = SplitResult.fromTerm(execContext.require(strIncrThenFront, frontInput).result);
        final String moduleName = splitResult.moduleName;
        final List<Import> imports = new ArrayList<>(splitResult.imports.size());
        for(IStrategoTerm importTerm : splitResult.imports) {
            imports.add(Import.fromTerm(importTerm));
        }

        final Map<String, IStrategoAppl> strategyASTs = new HashMap<>();
        final Map<String, File> strategyFiles = new HashMap<>();
        final Map<String, Set<String>> strategyConstrs = new HashMap<>();
        final Set<String> strategyNeedsExternal = new HashSet<>();
        final Map<String, Set<String>> usedAmbStrats = new HashMap<>();
        final Set<String> usedStrats = new HashSet<>();
        final Set<String> definedConstrs = new HashSet<>();
        final Map<String, File> overlayFiles = new HashMap<>();
        final Map<String, Set<String>> overlayConstrs = new HashMap<>();
        final Map<String, List<IStrategoAppl>> overlayASTs = new HashMap<>();
        final Map<String, IStrategoAppl> congrASTs = new HashMap<>();
        final Set<String> congrs = new HashSet<>();
        final Map<String, File> congrFiles = new HashMap<>();
        final Map<String, Integer> noOfDefinitions = new HashMap<>();

        for(Map.Entry<String, IStrategoTerm> e : splitResult.strategyDefs.entrySet()) {
            String strategyName = e.getKey();
            IStrategoTerm strategyAST = e.getValue();
            frontInput = new StrIncrSubFront.Input(input.projectLocation, input.inputFileString, strategyName,
                input.context, StrIncrSubFront.InputType.TopLevelDefinition, strategyAST);
            stratFrontEnd(execContext, input.projectName, location, frontInput, moduleName, strategyASTs, strategyFiles,
                strategyConstrs, strategyNeedsExternal, usedAmbStrats, usedStrats, noOfDefinitions);
        }
        for(Map.Entry<String, IStrategoTerm> e : splitResult.consDefs.entrySet()) {
            String consName = e.getKey();
            IStrategoTerm consAST = e.getValue();
            frontInput = new StrIncrSubFront.Input(input.projectLocation, input.inputFileString, consName,
                input.context, StrIncrSubFront.InputType.TopLevelDefinition, consAST);
            consFrontEnd(execContext, input, location, frontInput, moduleName, strategyConstrs, usedAmbStrats,
                usedStrats, definedConstrs, congrASTs, congrs, congrFiles, noOfDefinitions);
        }
        for(Map.Entry<String, IStrategoTerm> e : splitResult.olayDefs.entrySet()) {
            String olayName = e.getKey();
            IStrategoTerm olayAST = e.getValue();
            frontInput = new StrIncrSubFront.Input(input.projectLocation, input.inputFileString, olayName,
                input.context, StrIncrSubFront.InputType.TopLevelDefinition, olayAST);
            overlayFrontEnd(execContext, input, location, frontInput, moduleName, strategyASTs, strategyFiles,
                strategyConstrs, strategyNeedsExternal, usedAmbStrats, usedStrats, overlayASTs, noOfDefinitions);
        }
        for(Map.Entry<String, List<IStrategoAppl>> overlayPair : overlayASTs.entrySet()) {
            final String overlayName = overlayPair.getKey();
            final List<IStrategoAppl> overlayASTList = overlayPair.getValue();

            storeOverlay(location, moduleName, overlayName, overlayFiles, input.projectName);
            final HashSet<String> usedConstrs = new HashSet<>();
            collectUsedNames(StrategoArrayList.fromList(overlayASTList), usedConstrs);
            overlayConstrs.put(overlayName, usedConstrs);
        }
        BuildStats.frontTaskTime += System.nanoTime() - startTime;

        return new NormalOutput(moduleName, strategyFiles, usedStrats, usedAmbStrats, strategyConstrs, overlayFiles,
            imports, definedConstrs, congrs, strategyNeedsExternal, overlayConstrs, congrFiles, noOfDefinitions,
            strategyASTs, overlayASTs, congrASTs);
    }


    private void overlayFrontEnd(ExecContext execContext, Input input, final FileObject location,
        final StrIncrSubFront.Input frontInput, final String moduleName, final Map<String, IStrategoAppl> strategyASTs,
        final Map<String, File> strategyFiles, final Map<String, Set<String>> strategyConstrs,
        final Set<String> strategyNeedsExternal, final Map<String, Set<String>> usedAmbStrats,
        final Set<String> usedStrats, final Map<String, List<IStrategoAppl>> overlayASTs,
        final Map<String, Integer> noOfDefinitions) throws ExecException, InterruptedException, IOException {
        final IStrategoTerm result = execContext.require(strIncrThenFront, frontInput).result;
        final IStrategoList defs3 = Tools.listAt(result, 0);
        // 1 == DR_UNDEFINE_1, DR_DUMMY_0
        final IStrategoList olays = Tools.listAt(result, 2);
        // 3 ~= 1
        final IStrategoList noOfDefs = Tools.listAt(result, 4);

        for(IStrategoTerm overlayPair : olays) {
            final String overlayName = Tools.javaStringAt(overlayPair, 0);
            final IStrategoAppl overlayAST = Tools.applAt(overlayPair, 1);

            StrIncr.getOrInitialize(overlayASTs, overlayName, ArrayList::new).add(overlayAST);
        }

        for(IStrategoTerm defPair : defs3) {
            final String strategyName = Tools.javaStringAt(defPair, 0);
            final IStrategoAppl strategyAST = Tools.applAt(defPair, 1);
            strategyASTs.put(strategyName, strategyAST);

            storeDef(location, moduleName, strategyName, strategyFiles, input.projectName);
            final HashSet<String> usedConstrs = new HashSet<>();
            collectUsedNames(strategyAST, usedConstrs, usedStrats, usedAmbStrats);
            strategyConstrs.put(strategyName, usedConstrs);
            if(needsExternal(strategyAST)) {
                strategyNeedsExternal.add(strategyName);
            }
        }

        for(IStrategoTerm noOfDef : noOfDefs) {
            final String defName = Tools.javaStringAt(noOfDef, 0);
            final int no = Tools.javaIntAt(noOfDef, 1);
            noOfDefinitions.put(defName, no);
        }
    }


    private void consFrontEnd(ExecContext execContext, Input input, final FileObject location,
        final StrIncrSubFront.Input frontInput, final String moduleName, final Map<String, Set<String>> strategyConstrs,
        final Map<String, Set<String>> usedAmbStrats, final Set<String> usedStrats, final Set<String> definedConstrs,
        final Map<String, IStrategoAppl> congrASTs, final Set<String> congrs, final Map<String, File> congrFiles,
        final Map<String, Integer> noOfDefinitions) throws ExecException, InterruptedException, IOException {
        final IStrategoTerm result = execContext.require(strIncrThenFront, frontInput).result;
        // 0 == Anno__Cong_____2_0
        final IStrategoList constrs = Tools.listAt(result, 1);
        // 2 == empty
        final IStrategoList congs = Tools.listAt(result, 3);
        final IStrategoList noOfDefs = Tools.listAt(result, 4);

        for(IStrategoTerm constr : constrs) {
            definedConstrs.add(Tools.javaStringAt(constr, 0));
        }

        for(IStrategoTerm congrPair : congs) {
            final String congrName = Tools.javaStringAt(congrPair, 0);
            final IStrategoAppl congrAST = Tools.applAt(congrPair, 1);
            congrs.add(congrName + "_0");
            congrASTs.put(congrName, congrAST);

            storeDef(location, moduleName, congrName, congrFiles, input.projectName);
            final HashSet<String> usedConstrs = new HashSet<>();
            collectUsedNames(congrAST, usedConstrs, usedStrats, usedAmbStrats);
            strategyConstrs.put(congrName, usedConstrs);
        }

        for(IStrategoTerm noOfDef : noOfDefs) {
            final String defName = Tools.javaStringAt(noOfDef, 0);
            final int no = Tools.javaIntAt(noOfDef, 1);
            noOfDefinitions.put(defName, no);
        }
    }


    private void stratFrontEnd(ExecContext execContext, String projectName, final FileObject location,
        final StrIncrSubFront.Input frontInput, final String moduleName, final Map<String, IStrategoAppl> strategyASTs,
        final Map<String, File> strategyFiles, final Map<String, Set<String>> strategyConstrs,
        final Set<String> strategyNeedsExternal, final Map<String, Set<String>> usedAmbStrats,
        final Set<String> usedStrats, final Map<String, Integer> noOfDefinitions)
        throws ExecException, InterruptedException, IOException {
        final IStrategoTerm result = execContext.require(strIncrThenFront, frontInput).result;
        final IStrategoList defs3 = Tools.listAt(result, 0);
        // 1 == DR_UNDEFINE_1, DR_DUMMY_0
        // 2 == empty
        // 3 ~= 1
        final IStrategoList noOfDefs = Tools.listAt(result, 4);

        for(IStrategoTerm defPair : defs3) {
            final String strategyName = Tools.javaStringAt(defPair, 0);
            final IStrategoAppl strategyAST = Tools.applAt(defPair, 1);
            strategyASTs.put(strategyName, strategyAST);

            storeDef(location, moduleName, strategyName, strategyFiles, projectName);
            final HashSet<String> usedConstrs = new HashSet<>();
            collectUsedNames(strategyAST, usedConstrs, usedStrats, usedAmbStrats);
            strategyConstrs.put(strategyName, usedConstrs);
            if(needsExternal(strategyAST)) {
                strategyNeedsExternal.add(strategyName);
            }
        }
        final @Nullable File boilerplateFile = resourceService
            .localPath(CommonPaths.strSepCompBoilerplateFile(location, projectName, moduleName));
        assert boilerplateFile != null : "Bug in strSepCompBoilerplateFile or the arguments thereof: returned path is not a file";

        for(IStrategoTerm noOfDef : noOfDefs) {
            final String defName = Tools.javaStringAt(noOfDef, 0);
            final int no = Tools.javaIntAt(noOfDef, 1);
            noOfDefinitions.put(defName, no);
        }
    }

    /**
     * Collect usedConstructors, usedStrategies, and ambUsedStrategies Combination of extract-used-constructors and
     * extract-used-strategies
     */
    private void collectUsedNames(IStrategoTerm strategyAST, Set<String> usedConstrs, Set<String> usedStrats,
        Map<String, Set<String>> usedAmbStrats) {
        final TermVisitor visitor = new CollectUsedNamesTermVisitor(usedConstrs, usedStrats, usedAmbStrats);
        visitor.visit(strategyAST);
    }

    private void collectUsedNames(IStrategoTerm overlayASTList, Set<String> usedConstrs) {
        final TermVisitor visitor = new CollectUsedConstrsTermVisitor(usedConstrs);
        visitor.visit(overlayASTList);
    }

    private boolean needsExternal(IStrategoAppl strategyAST) {
        if(Tools.hasConstructor(strategyAST, "AnnoDef", 2)) {
            IStrategoList annos = Tools.listAt(strategyAST, 0);
            for(IStrategoTerm anno : annos) {
                if(anno.getSubtermCount() == 0 && anno.getTermType() == IStrategoTerm.APPL) {
                    String annoName = Tools.constructorName(anno);
                    if(annoName.equals("Override") || annoName.equals("Extend")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void storeDef(FileObject location, String moduleName, String strategy, Map<String, File> strategyFiles,
        String projectName) throws IOException {
        final @Nullable File strategyFile =
            resourceService.localPath(CommonPaths.strSepCompStrategyFile(location, projectName, moduleName, strategy));
        assert strategyFile != null : "Bug in strSepCompStrategyFile or the arguments thereof: returned path is not a file";

        strategyFiles.put(strategy, strategyFile);
    }

    private void storeOverlay(FileObject location, String moduleName, String overlayName,
        Map<String, File> overlayFiles, String projectName) throws IOException {
        final @Nullable File overlayFile = resourceService
            .localPath(CommonPaths.strSepCompOverlayFile(location, projectName, moduleName, overlayName));
        assert overlayFile != null : "Bug in strSepCompStrategyFile or the arguments thereof: returned path is not a file";

        overlayFiles.put(overlayName, overlayFile);
    }

    private IStrategoTerm parseFile(FileObject inputFile, String projectName, FileObject projectLocation)
        throws Exception {
        ILanguageImpl strategoDialect = languageIdentifierService.identify(inputFile);
        if(strategoLang == null) {
            strategoLang = dialectService.getBase(strategoDialect);
            strIncrThenFront.strategoLang = strategoLang;
        }
        if(strategoLang == null) {
            strategoLang = strategoDialect;
            strIncrThenFront.strategoLang = strategoLang;
            strategoDialect = null;
        }
        IStrategoTerm ast;
        if(strategoLang == null) {
            @Nullable ILanguage stratego = languageService.getLanguage(SpoofaxConstants.LANG_STRATEGO_NAME);
            String extension = inputFile.getName().getExtension();
            if(stratego != null && extension.equals("rtree")) {
                strategoLang = stratego.activeImpl();
                strIncrThenFront.strategoLang = strategoLang;
                // support *.rtree (StrategoSugar AST)
                final ITermFactory factory = termFactoryService.getGeneric();
                ast = new TermReader(factory).parseFromStream(inputFile.getContent().getInputStream());
                if(!(ast instanceof IStrategoAppl && ((IStrategoAppl) ast).getName().equals("Module")
                    && ast.getSubtermCount() == 2)) {
                    if(!(ast instanceof IStrategoAppl && ((IStrategoAppl) ast).getName().equals("Specification")
                        && ast.getSubtermCount() == 1)) {
                        throw new ExecException("Did not find Module/2 in RTree file. Found: \n" + ast.toString(2));
                    } else {
                        throw new ExecException("Bug in custom library detection. Please file a bug report and "
                            + "turn off Stratego separate compilation for now as a work-around. ");
                    }
                }
            } else {
                if(stratego == null || stratego.activeImpl() == null) {
                    throw new ExecException("Cannot find/load Stratego language. Please add a source dependency "
                        + "'org.metaborg:org.metaborg.meta.lang.stratego:${metaborgVersion}' in your metaborg.yaml file. ");
                } else {
                    throw new ExecException("Cannot find the right Stratego dialect for " + inputFile
                        + ". Make sure the .tbl file of the dialect was made available via a -I commandline flag. ");
                }
            }
        } else {
            // parse *.str file
            ast = parse(inputFile, strategoDialect, strategoLang);
        }
        if(ast instanceof IStrategoAppl && ((IStrategoAppl) ast).getName().equals("Module")
            && ast.getSubtermCount() == 2) {
            final TermSizeTermVisitor termSizeTermVisitor = new TermSizeTermVisitor();
            termSizeTermVisitor.visit(ast);
            final String moduleName = Tools.javaStringAt(ast, 0);
            BuildStats.moduleFrontendCTreeSize.put(moduleName, termSizeTermVisitor.size);
        }

        return ast;
    }

    private IStrategoTerm parse(FileObject inputFile, @Nullable ILanguageImpl strategoDialect,
        ILanguageImpl strategoLang) throws ParseException, ExecException {
        final @Nullable ImploderImplementation overrideImploder =
            strategoDialect == null ? null : ImploderImplementation.stratego;

        final @Nullable IStrategoTerm ast;
        final String text;
        try {
            text = sourceTextService.text(inputFile);
        } catch(IOException e) {
            throw new ParseException(null, e);
        }
        final ISpoofaxInputUnit inputUnit = unitService.inputUnit(inputFile, text, strategoLang, strategoDialect);
        final ISpoofaxParseUnit parseResult = syntaxService.parse(inputUnit, overrideImploder);
        ast = parseResult.ast();
        if(!parseResult.success() || ast == null) {
            throw new ExecException("Cannot parse stratego file " + inputFile + ": " + parseResult.messages());
        }
        return ast;
    }

    @Override public String getId() {
        return id;
    }

    @Override public Serializable key(Input input) {
        return input.inputFileString;
    }
}
