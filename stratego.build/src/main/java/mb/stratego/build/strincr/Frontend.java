package mb.stratego.build.strincr;

import java.io.BufferedInputStream;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.TermFactory;
import org.spoofax.terms.TermVisitor;
import org.spoofax.terms.attachments.OriginAttachment;
import org.spoofax.terms.io.TAFTermReader;
import org.spoofax.terms.util.B;
import org.spoofax.terms.util.TermUtils;

import javax.inject.Inject;

import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.STask;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.output.InconsequentialOutputStamper;
import mb.resource.ResourceService;
import mb.resource.fs.FSPath;
import mb.resource.fs.FSResource;
import mb.resource.hierarchical.ResourcePath;
import mb.stratego.build.termvisitors.UsedConstrs;
import mb.stratego.build.termvisitors.UsedNames;
import mb.stratego.build.util.CommonPaths;
import mb.stratego.build.util.Relation;
import mb.stratego.build.util.StringSetWithPositions;

public class Frontend implements TaskDef<Frontend.Input, Frontend.Output> {
    public static final String id = Frontend.class.getCanonicalName();

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

        @SuppressWarnings("unused")
        abstract @Nullable FileRemovedOutput fileRemovedOutput();
    }

    public static final class NormalOutput extends Output {
        final String moduleName;
        final IStrategoTerm sugarAST;
        /**
         * Cified-strategy-name to file with CTree definition of that strategy [static linking]
         */
        final Map<String, File> strategyFiles;
        /**
         * Cified-strategy-names referred to in this module [name checks]
         */
        final StringSetWithPositions usedStrategies;
        /**
         * Cified-strategy-names-without-arity referred to in this module in an ambiguous position (strategy argument to
         * other strategy) to cified-strategy-names where the ambiguous call occurs [name checks]
         */
        final Map<String, Set<String>> ambStratUsed;
        /**
         * Cified-strategy-names-without-arity referred to in this module in an ambiguous position (strategy argument to
         * other strategy) to actual AST names where the ambiguous call occurs [name checks]
         */
        public StringSetWithPositions ambStratPositions;
        /**
         * Cified-strategy-name to constructor_arity names that were used in the body [name checks]
         */
        final Map<String, StringSetWithPositions> strategyConstrs;
        /**
         * Overlay_arity names to file with CTree definition of that overlay [static linking / name checks]
         */
        final Map<String, File> overlayFiles;
        /**
         * Imports in this module (normal, library or wildcard) [import tracking / name checks]
         */
        final List<Import> imports;
        /**
         * Cified-strategy-name defined in this module [name checks]
         */
        final StringSetWithPositions strats;
        /**
         * Cified-strategy-name defined in this module annotated with internal [name checks]
         */
        final StringSetWithPositions internalStrats;
        /**
         * Cified-strategy-name defined in this module to be external [name checks]
         */
        final StringSetWithPositions externalStrats;
        /**
         * Constructor_arity names defined in this module [name checks]
         */
        final StringSetWithPositions constrs;
        /**
         * Constructor_arity names defined in this module [name checks]
         */
        final StringSetWithPositions overlays;
        /**
         * Cified-strategy-name of a generated congruence [static linking]
         */
        final StringSetWithPositions congrs;
        /**
         * Cified-strategy-names of strategies that need a corresponding strategy in a library because it overrides or
         * extends it. [name checks]
         */
        final List<IStrategoString> strategyNeedsExternal;
        /**
         * Overlay_arity names to constructor_arity names used. [static linking / name checks]
         */
        final Map<String, StringSetWithPositions> overlayConstrs;
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

        NormalOutput(String moduleName, IStrategoTerm sugarAST, Map<String, File> strategyFiles, StringSetWithPositions usedStrategies,
            Map<String, Set<String>> ambStratUsed, StringSetWithPositions ambStratPositions, Map<String, StringSetWithPositions> strategyConstrs,
            Map<String, File> overlayFiles, List<Import> imports, StringSetWithPositions strats,
            StringSetWithPositions internalStrats, StringSetWithPositions externalStrats, StringSetWithPositions constrs, StringSetWithPositions overlays, StringSetWithPositions congrs,
            List<IStrategoString> strategyNeedsExternal, Map<String, StringSetWithPositions> overlayConstrs, Map<String, File> congrFiles,
            Map<String, Integer> noOfDefinitions, Map<String, IStrategoAppl> strategyASTs,
            Map<String, List<IStrategoAppl>> overlayASTs, Map<String, IStrategoAppl> congrASTs) {
            this.moduleName = moduleName;
            this.sugarAST = sugarAST;
            this.strategyFiles = strategyFiles;
            this.usedStrategies = usedStrategies;
            this.ambStratUsed = ambStratUsed;
            this.ambStratPositions = ambStratPositions;
            this.strategyConstrs = strategyConstrs;
            this.overlayFiles = overlayFiles;
            this.imports = imports;
            this.strats = strats;
            this.internalStrats = internalStrats;
            this.externalStrats = externalStrats;
            this.constrs = constrs;
            this.overlays = overlays;
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
            if(!ambStratPositions.equals(output.ambStratPositions))
                return false;
            if(!strategyConstrs.equals(output.strategyConstrs))
                return false;
            if(!overlayFiles.equals(output.overlayFiles))
                return false;
            if(!imports.equals(output.imports))
                return false;
            if(!strats.equals(output.strats))
                return false;
            if(!internalStrats.equals(output.strats))
                return false;
            if(!constrs.equals(output.constrs))
                return false;
            if(!overlays.equals(output.overlays))
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
            result = 31 * result + ambStratPositions.hashCode();
            result = 31 * result + strategyConstrs.hashCode();
            result = 31 * result + overlayFiles.hashCode();
            result = 31 * result + imports.hashCode();
            result = 31 * result + strats.hashCode();
            result = 31 * result + internalStrats.hashCode();
            result = 31 * result + constrs.hashCode();
            result = 31 * result + overlays.hashCode();
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

    private final ParseStratego parseStratego;
    private final SubFrontend strIncrSubFront;
    static ArrayList<Long> timestamps = new ArrayList<>();

    @Inject public Frontend(ParseStratego parseStratego, SubFrontend strIncrSubFront) {
        this.parseStratego = parseStratego;
        this.strIncrSubFront = strIncrSubFront;
    }

    private static final int LOCAL_DEFS = 0;
    private static final int EXT_DEFS = 1;
    private static final int CONSTRS = 2;
    private static final int OLAYS = 3;
    private static final int CONGRS = 4;
    private static final int DEF_COUNT = 5;


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
        for(STask t : input.originTasks) {
            execContext.require(t, InconsequentialOutputStamper.instance);
        }

        final ResourcePath location = new FSPath(input.projectLocation);
        final FSResource inputFile = new FSResource(input.inputFileString);
        execContext.require(inputFile);
        if(!inputFile.exists()) {
            return FileRemovedOutput.instance;
        }

        timestamps.add(System.nanoTime());

        final long startTime = System.nanoTime();
        final IStrategoTerm ast;
        try(final InputStream inputStream = new BufferedInputStream(inputFile.openRead())) {
            if("rtree".equals(inputFile.getLeafExtension())) {
                ast = parseStratego.parseRtree(inputStream);
            } else {
                ast = parseStratego.parse(inputStream, StandardCharsets.UTF_8, input.inputFileString);
            }
        }

        final SubFrontend.Input frontSplitInput = new SubFrontend.Input(input.inputFileString,
            input.inputFileString, SubFrontend.InputType.Split, ast);
        timestamps.add(System.nanoTime());
        final IStrategoTerm splitTerm = execContext.require(strIncrSubFront, frontSplitInput).result;
        timestamps.add(System.nanoTime());
        final SplitResult splitResult = SplitResult.fromTerm(splitTerm);
        final String moduleName = splitResult.moduleName;
        final List<Import> imports = new ArrayList<>(splitResult.imports.size());
        for(IStrategoTerm importTerm : splitResult.imports) {
            imports.add(Import.fromTerm(importTerm));
        }

        final Map<String, IStrategoAppl> strategyASTs = new HashMap<>();
        final Map<String, File> strategyFiles = new HashMap<>();
        final Map<String, StringSetWithPositions> strategyConstrs = new HashMap<>();
        final List<IStrategoString> strategyNeedsExternal = new ArrayList<>();
        final Map<String, Set<String>> usedAmbStrats = new HashMap<>();
        final StringSetWithPositions ambStratPositions = new StringSetWithPositions();
        final StringSetWithPositions usedStrats = new StringSetWithPositions();
        final StringSetWithPositions definedConstrs = new StringSetWithPositions();
        final StringSetWithPositions definedStrats = new StringSetWithPositions();
        final StringSetWithPositions internalStrats = new StringSetWithPositions();
        final StringSetWithPositions externalStrats = new StringSetWithPositions();
        final StringSetWithPositions definedOverlays = new StringSetWithPositions();
        final Map<String, File> overlayFiles = new HashMap<>();
        final Map<String, StringSetWithPositions> overlayConstrs = new HashMap<>();
        final Map<String, List<IStrategoAppl>> overlayASTs = new HashMap<>();
        final Map<String, IStrategoAppl> congrASTs = new HashMap<>();
        final StringSetWithPositions congrs = new StringSetWithPositions();
        final Map<String, File> congrFiles = new HashMap<>();
        final Map<String, Integer> noOfDefinitions = new HashMap<>();

        for(Map.Entry<String, IStrategoTerm> e : splitResult.strategyDefs.entrySet()) {
            final String strategyName = e.getKey();
            final IStrategoTerm strategyAST = e.getValue();
            final SubFrontend.Input frontTLDInput = new SubFrontend.Input(input.inputFileString, strategyName,
                SubFrontend.InputType.TopLevelDefinition, strategyAST);
            stratFrontEnd(execContext, input.projectName, location, frontTLDInput, moduleName, definedStrats, internalStrats, externalStrats, strategyASTs,
                strategyFiles, strategyConstrs, strategyNeedsExternal, usedAmbStrats, ambStratPositions, usedStrats,
                noOfDefinitions);
        }

        for(Map.Entry<String, IStrategoTerm> e : splitResult.consDefs.entrySet()) {
            final String consName = e.getKey();
            final IStrategoTerm consAST = e.getValue();
            final SubFrontend.Input frontTLDInput = new SubFrontend.Input(input.inputFileString, consName,
                SubFrontend.InputType.TopLevelDefinition, consAST);
            consFrontEnd(execContext, input, location, frontTLDInput, moduleName, strategyConstrs, usedAmbStrats,
                ambStratPositions, usedStrats, definedConstrs, congrASTs, congrs, congrFiles, noOfDefinitions);
        }
        for(Map.Entry<String, IStrategoTerm> e : splitResult.olayDefs.entrySet()) {
            final String olayName = e.getKey();
            final IStrategoTerm olayAST = e.getValue();
            final SubFrontend.Input frontTLDInput = new SubFrontend.Input(input.inputFileString, olayName,
                SubFrontend.InputType.TopLevelDefinition, olayAST);
            overlayFrontEnd(execContext, input, location, frontTLDInput, moduleName, definedStrats, strategyASTs,
                strategyFiles, strategyConstrs, strategyNeedsExternal, usedAmbStrats, ambStratPositions, usedStrats,
                definedOverlays, overlayASTs, noOfDefinitions);
        }
        for(Map.Entry<String, List<IStrategoAppl>> overlayPair : overlayASTs.entrySet()) {
            final String overlayName = overlayPair.getKey();
            final List<IStrategoAppl> overlayASTList = overlayPair.getValue();

            storeOverlay(location, moduleName, overlayName, overlayFiles, input.projectName, execContext.getResourceService());
            final StringSetWithPositions usedConstrs = new StringSetWithPositions();
            collectUsedNames(B.list(overlayASTList), usedConstrs);
            overlayConstrs.put(overlayName, usedConstrs);
        }
        BuildStats.frontTaskTime += System.nanoTime() - startTime;

        timestamps.add(System.nanoTime());

        return new NormalOutput(moduleName, ast, strategyFiles, usedStrats, usedAmbStrats, ambStratPositions,
            strategyConstrs, overlayFiles, imports, definedStrats, internalStrats, externalStrats, definedConstrs, definedOverlays, congrs, strategyNeedsExternal,
            overlayConstrs, congrFiles, noOfDefinitions, strategyASTs, overlayASTs, congrASTs);
    }

    private void overlayFrontEnd(ExecContext execContext, Input input, final ResourcePath location,
        final SubFrontend.Input frontInput, final String moduleName, StringSetWithPositions definedStrats,
        final Map<String, IStrategoAppl> strategyASTs, final Map<String, File> strategyFiles,
        final Map<String, StringSetWithPositions> strategyConstrs, final List<IStrategoString> strategyNeedsExternal,
        final Map<String, Set<String>> usedAmbStrats, final StringSetWithPositions ambStratPositions,
        final StringSetWithPositions usedStrats, StringSetWithPositions definedOverlays, final Map<String, List<IStrategoAppl>> overlayASTs,
        final Map<String, Integer> noOfDefinitions) throws ExecException, InterruptedException {
        timestamps.add(System.nanoTime());
        final IStrategoTerm result = execContext.require(strIncrSubFront, frontInput).result;
        timestamps.add(System.nanoTime());
        final IStrategoList defs3 = TermUtils.toListAt(result, LOCAL_DEFS);
        // EXT_DEFS == empty
        // CONSTRS == DR_UNDEFINE_1, DR_DUMMY_0
        final IStrategoList olays = TermUtils.toListAt(result, OLAYS);
        // CONGRS ~= 1
        final IStrategoList noOfDefs = TermUtils.toListAt(result, DEF_COUNT);

        for(IStrategoTerm overlayPair : olays) {
            final IStrategoString overlayName = TermUtils.toStringAt(overlayPair, 0);
            final IStrategoAppl overlayAST = TermUtils.toApplAt(overlayPair, 1);
            definedOverlays.add(overlayName);

            Relation.getOrInitialize(overlayASTs, overlayName.stringValue(), ArrayList::new).add(overlayAST);
        }

        for(IStrategoTerm defPair : defs3) {
            final IStrategoString strategyName = TermUtils.toStringAt(defPair, 0);
            final IStrategoAppl strategyAST = TermUtils.toApplAt(defPair, 1);
            strategyASTs.put(strategyName.stringValue(), strategyAST);
            definedStrats.add(strategyName);

            storeDef(location, moduleName, strategyName.stringValue(), strategyFiles, input.projectName, execContext.getResourceService());
            final StringSetWithPositions usedConstrs = new StringSetWithPositions();
            collectUsedNames(strategyAST, usedConstrs, usedStrats, usedAmbStrats, ambStratPositions);
            strategyConstrs.put(strategyName.stringValue(), usedConstrs);
            if(needsExternal(annoDefAnnotations(strategyAST))) {
                strategyNeedsExternal.add(strategyName);
            }
        }

        for(IStrategoTerm noOfDef : noOfDefs) {
            final String defName = TermUtils.toJavaStringAt(noOfDef, 0);
            final int no = TermUtils.toJavaIntAt(noOfDef, 1);
            noOfDefinitions.put(defName, no);
        }
    }


    private void consFrontEnd(ExecContext execContext, Input input, final ResourcePath location,
        final SubFrontend.Input frontInput, final String moduleName,
        final Map<String, StringSetWithPositions> strategyConstrs, final Map<String, Set<String>> usedAmbStrats,
        final StringSetWithPositions ambStratPositions, final StringSetWithPositions usedStrats,
        final StringSetWithPositions definedConstrs, final Map<String, IStrategoAppl> congrASTs,
        final StringSetWithPositions congrs, final Map<String, File> congrFiles, final Map<String, Integer> noOfDefinitions)
        throws ExecException, InterruptedException {
        timestamps.add(System.nanoTime());
        final IStrategoTerm result = execContext.require(strIncrSubFront, frontInput).result;
        timestamps.add(System.nanoTime());
        // LOCAL_DEFS == Anno__Cong_____2_0
        // EXT_DEFS == empty
        final IStrategoList constrs = TermUtils.toListAt(result, CONSTRS);
        // OLAYS == empty
        final IStrategoList congs = TermUtils.toListAt(result, CONGRS);
        final IStrategoList noOfDefs = TermUtils.toListAt(result, DEF_COUNT);

        for(IStrategoTerm constr : constrs) {
            definedConstrs.add(TermUtils.toStringAt(constr, 0));
        }

        for(IStrategoTerm congrPair : congs) {
            final IStrategoString congrName = TermUtils.toStringAt(congrPair, 0);
            final IStrategoAppl congrAST = TermUtils.toApplAt(congrPair, 1);
            final String congrNameString = congrName.stringValue();
            final IStrategoString cifiedCongrName = B.string(congrNameString + "_0");
            OriginAttachment.setOrigin(cifiedCongrName, congrName);
            congrs.add(cifiedCongrName);
            congrASTs.put(congrNameString, congrAST);

            storeDef(location, moduleName, congrNameString, congrFiles, input.projectName, execContext.getResourceService());
            final StringSetWithPositions usedConstrs = new StringSetWithPositions();
            collectUsedNames(congrAST, usedConstrs, usedStrats, usedAmbStrats, ambStratPositions);
            strategyConstrs.put(congrNameString, usedConstrs);
        }

        for(IStrategoTerm noOfDef : noOfDefs) {
            final String defName = TermUtils.toJavaStringAt(noOfDef, 0);
            final int no = TermUtils.toJavaIntAt(noOfDef, 1);
            noOfDefinitions.put(defName, no);
        }
    }


    private void stratFrontEnd(ExecContext execContext, String projectName, final ResourcePath location,
        final SubFrontend.Input frontInput, final String moduleName, StringSetWithPositions definedStrats,
        StringSetWithPositions internalStrats, StringSetWithPositions externalStrats, final Map<String, IStrategoAppl> strategyASTs, final Map<String, File> strategyFiles,
        final Map<String, StringSetWithPositions> strategyConstrs, final List<IStrategoString> strategyNeedsExternal,
        final Map<String, Set<String>> usedAmbStrats, final StringSetWithPositions ambStratPositions, final StringSetWithPositions usedStrats,
        final Map<String, Integer> noOfDefinitions)
        throws ExecException, InterruptedException {
        timestamps.add(System.nanoTime());
        final IStrategoTerm result = execContext.require(strIncrSubFront, frontInput).result;
        timestamps.add(System.nanoTime());
        final IStrategoList defs3 = TermUtils.toListAt(result, LOCAL_DEFS);
        final IStrategoList extDefs = TermUtils.toListAt(result, EXT_DEFS);
        // CONSTRS == DR_UNDEFINE_1, DR_DUMMY_0
        // OLAYS == empty
        // CONGRS ~= 1
        final IStrategoList noOfDefs = TermUtils.toListAt(result, DEF_COUNT);

        for(IStrategoTerm defPair : extDefs) {
            final IStrategoString strategyName = TermUtils.toStringAt(defPair, 0);
            externalStrats.add(strategyName);
            // We don't add to strategyASTs so that no backend task is created for this external definition
            // We don't add to definedStrats as the external strategy is defined elsewhere, and defined strategies may not overlap with external ones
//            definedStrats.add(strategyName);
        }
        for(IStrategoTerm defPair : defs3) {
            final IStrategoString strategyName = TermUtils.toStringAt(defPair, 0);
            final IStrategoAppl strategyAST = TermUtils.toApplAt(defPair, 1);
            strategyASTs.put(strategyName.stringValue(), strategyAST);
            definedStrats.add(strategyName);

            storeDef(location, moduleName, strategyName.stringValue(), strategyFiles, projectName, execContext.getResourceService());
            final StringSetWithPositions usedConstrs = new StringSetWithPositions();
            collectUsedNames(strategyAST, usedConstrs, usedStrats, usedAmbStrats, ambStratPositions);
            strategyConstrs.put(strategyName.stringValue(), usedConstrs);
            final Set<String> annoDefAnnotations = annoDefAnnotations(strategyAST);
            if(needsExternal(annoDefAnnotations)) {
                strategyNeedsExternal.add(strategyName);
            }
            if(isInternal(annoDefAnnotations)) {
                internalStrats.add(strategyName);
            }
        }

        for(IStrategoTerm noOfDef : noOfDefs) {
            final String defName = TermUtils.toJavaStringAt(noOfDef, 0);
            final int no = TermUtils.toJavaIntAt(noOfDef, 1);
            noOfDefinitions.put(defName, no);
        }
    }

    /**
     * Collect usedConstructors, usedStrategies, and ambUsedStrategies Combination of extract-used-constructors and
     * extract-used-strategies
     */
    private void collectUsedNames(IStrategoTerm strategyAST, StringSetWithPositions usedConstrs,
        StringSetWithPositions usedStrats, Map<String, Set<String>> usedAmbStrats,
        StringSetWithPositions ambStratPositions) {
        final TermVisitor visitor = new UsedNames(usedConstrs, usedStrats, usedAmbStrats, ambStratPositions);
        visitor.visit(strategyAST);
    }

    private void collectUsedNames(IStrategoTerm overlayASTList, StringSetWithPositions usedConstrs) {
        final TermVisitor visitor = new UsedConstrs(usedConstrs);
        visitor.visit(overlayASTList);
    }

    private Set<String> annoDefAnnotations(IStrategoAppl strategyAST) {
        if(TermUtils.isAppl(strategyAST, "AnnoDef", 2)) {
            IStrategoList annos = TermUtils.toListAt(strategyAST, 0);
            Set<String> annotations = new HashSet<>(annos.size());
            for(IStrategoTerm anno : annos) {
                if(TermUtils.isAppl(anno, null, 0)) {
                    annotations.add(TermUtils.tryGetName(anno).orElse(null));
                }
            }
            return annotations;
        }
        return Collections.emptySet();
    }

    private boolean needsExternal(Set<String> annoDefAnnotations) {
        return annoDefAnnotations.contains("Override") || annoDefAnnotations.contains("Extend");
    }

    private boolean isInternal(Set<String> annoDefAnnotations) {
        return annoDefAnnotations.contains("Internal");
    }

    private void storeDef(ResourcePath location, String moduleName, String strategy, Map<String, File> strategyFiles,
        String projectName, ResourceService resourceService) {
        final @Nullable File strategyFile = resourceService
            .toLocalFile(CommonPaths.strSepCompStrategyFile(location, projectName, moduleName, strategy));
        assert strategyFile != null : "Bug in strSepCompStrategyFile or the arguments thereof: returned path is not a file";

        strategyFiles.put(strategy, strategyFile);
    }

    private void storeOverlay(ResourcePath location, String moduleName, String overlayName,
        Map<String, File> overlayFiles, String projectName, ResourceService resourceService) {
        final @Nullable File overlayFile = resourceService
            .toLocalFile(CommonPaths.strSepCompOverlayFile(location, projectName, moduleName, overlayName));
        assert overlayFile != null : "Bug in strSepCompStrategyFile or the arguments thereof: returned path is not a file";

        overlayFiles.put(overlayName, overlayFile);
    }

    @Override public String getId() {
        return id;
    }

    @Override public Serializable key(Input input) {
        return input.inputFileString;
    }
}
