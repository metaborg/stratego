package mb.stratego.build.strincr;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Inject;

import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.TermVisitor;
import org.spoofax.terms.attachments.OriginAttachment;
import org.spoofax.terms.util.B;
import org.spoofax.terms.util.TermUtils;

import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.TaskDef;
import mb.resource.ResourceService;
import mb.resource.fs.FSPath;
import mb.resource.hierarchical.ResourcePath;
import mb.stratego.build.strincr.SplitResult.ConstructorSignature;
import mb.stratego.build.strincr.SplitResult.StrategySignature;
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
        final SplitResult splitResult;

        Input(File projectLocation, String inputFileString, String projectName, SplitResult splitResult) {
            this.projectLocation = projectLocation;
            this.inputFileString = inputFileString;
            this.projectName = projectName;
            this.splitResult = splitResult;
        }

        @Override public String toString() {
            return "Frontend$Input(" + inputFileString + ')';
        }

        @Override
        public boolean equals(Object o) {
            if(this == o)
                return true;
            if(!(o instanceof Input))
                return false;
            Input input = (Input) o;
            return projectLocation.equals(input.projectLocation) && inputFileString.equals(input.inputFileString)
                && projectName.equals(input.projectName) && splitResult.equals(input.splitResult);
        }

        @Override
        public int hashCode() {
            return Objects.hash(projectLocation, inputFileString, projectName, splitResult);
        }
    }

    public static abstract class Output implements Serializable {
        abstract @Nullable NormalOutput normalOutput();

        @SuppressWarnings("unused")
        abstract @Nullable FileRemovedOutput fileRemovedOutput();

        @Override
        public abstract int hashCode();

        @Override
        public abstract boolean equals(Object obj);
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
        final StringSetWithPositions usedStrategies;
        /**
         * Cified-strategy-names-without-arity referred to in this module in an ambiguous position (strategy argument to
         *  other strategy) to cified-strategy-names where the ambiguous call occurs [name checks]
         */
        final Map<String, Set<String>> ambStratUsed;
        /**
         * Cified-strategy-names-without-arity referred to in this module in an ambiguous position (strategy argument to
         *  other strategy) to actual AST names where the ambiguous call occurs [name checks]
         */
        final StringSetWithPositions ambStratPositions;
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
         *  extends it. [name checks]
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
         * Cified-strategy-name to CTree definition of that strategy [compilation]
         */
        final Map<String, IStrategoAppl> strategyASTs;
        /**
         * Overlay_arity names to CTree definition of that strategy [compilation]
         */
        final Map<String, List<IStrategoAppl>> overlayASTs;
        /**
         * Constructor_arity names to CTree definition of that strategy [compilation]
         */
        final Map<String, IStrategoAppl> congrASTs;

        NormalOutput(String moduleName, Map<String, File> strategyFiles, StringSetWithPositions usedStrategies,
            Map<String, Set<String>> ambStratUsed, StringSetWithPositions ambStratPositions,
            Map<String, StringSetWithPositions> strategyConstrs, Map<String, File> overlayFiles, List<Import> imports,
            StringSetWithPositions strats, StringSetWithPositions internalStrats, StringSetWithPositions externalStrats,
            StringSetWithPositions constrs, StringSetWithPositions overlays, StringSetWithPositions congrs,
            List<IStrategoString> strategyNeedsExternal, Map<String, StringSetWithPositions> overlayConstrs,
            Map<String, File> congrFiles, Map<String, Integer> noOfDefinitions, Map<String, IStrategoAppl> strategyASTs,
            Map<String, List<IStrategoAppl>> overlayASTs, Map<String, IStrategoAppl> congrASTs) {
            this.moduleName = moduleName;
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

        @Override NormalOutput normalOutput() {
            return this;
        }

        @Override FileRemovedOutput fileRemovedOutput() {
            return null;
        }

        @Override
        public boolean equals(Object o) {
            if(this == o)
                return true;
            if(!(o instanceof NormalOutput))
                return false;
            NormalOutput that = (NormalOutput) o;
            return moduleName.equals(that.moduleName) && strategyFiles.equals(that.strategyFiles) && usedStrategies
                .equals(that.usedStrategies) && ambStratUsed.equals(that.ambStratUsed) && ambStratPositions
                .equals(that.ambStratPositions) && strategyConstrs.equals(that.strategyConstrs) && overlayFiles
                .equals(that.overlayFiles) && imports.equals(that.imports) && strats.equals(that.strats)
                && internalStrats.equals(that.internalStrats) && externalStrats.equals(that.externalStrats) && constrs
                .equals(that.constrs) && overlays.equals(that.overlays) && congrs.equals(that.congrs)
                && strategyNeedsExternal.equals(that.strategyNeedsExternal) && overlayConstrs
                .equals(that.overlayConstrs) && congrFiles.equals(that.congrFiles) && noOfDefinitions
                .equals(that.noOfDefinitions);
        }

        @Override
        public int hashCode() {
            return Objects
                .hash(moduleName, strategyFiles, usedStrategies, ambStratUsed, ambStratPositions, strategyConstrs,
                    overlayFiles, imports, strats, internalStrats, externalStrats, constrs, overlays, congrs,
                    strategyNeedsExternal, overlayConstrs, congrFiles, noOfDefinitions);
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

        @Override
        public int hashCode() {
            return System.identityHashCode(this);
        }

        @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
        @Override
        public boolean equals(Object obj) {
            return obj == instance;
        }
    }

    private final ITermFactory tf;
    private final SubFrontend strIncrSubFront;

    @Inject public Frontend(ITermFactory tf, ParseStratego parseStratego, SubFrontend strIncrSubFront) {
        this.tf = tf;
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
        final long startTime = System.nanoTime();
        final ResourcePath location = new FSPath(input.projectLocation);
        final String moduleName = input.splitResult.moduleName;
        final List<Import> imports = new ArrayList<>(input.splitResult.imports.size());
        for(IStrategoTerm importTerm : input.splitResult.imports) {
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

        for(Map.Entry<StrategySignature, IStrategoTerm> e : input.splitResult.strategyDefs.entrySet()) {
            final String strategyName = e.getKey().cifiedName();
            final IStrategoTerm strategyAST = e.getValue();
            final SubFrontend.Input frontTLDInput =
                SubFrontend.Input.topLevelDefinition(input.inputFileString, strategyName, strategyAST);
            stratFrontEnd(execContext, input.projectName, location, frontTLDInput, moduleName, definedStrats,
                internalStrats, externalStrats, strategyASTs, strategyFiles, strategyConstrs, strategyNeedsExternal,
                usedAmbStrats, ambStratPositions, usedStrats, noOfDefinitions);
        }

        for(Map.Entry<ConstructorSignature, List<IStrategoTerm>> e : input.splitResult.consDefs.entrySet()) {
            final String consName = e.getKey().cifiedName();
            final List<IStrategoTerm> consASTs = e.getValue();
            final IStrategoTerm consAST = tf.makeList(tf.makeAppl("Signature", tf.makeList(tf.makeAppl("Constructors", tf.makeList(consASTs)))));
            final SubFrontend.Input frontTLDInput =
                SubFrontend.Input.topLevelDefinition(input.inputFileString, consName, consAST);
            consFrontEnd(execContext, input, location, frontTLDInput, moduleName, strategyConstrs, usedAmbStrats,
                ambStratPositions, usedStrats, definedConstrs, congrASTs, congrs, congrFiles, noOfDefinitions);
        }
        for(Map.Entry<ConstructorSignature, IStrategoTerm> e : input.splitResult.olayDefs.entrySet()) {
            final String olayName = e.getKey().cifiedName();
            final IStrategoTerm olayAST = e.getValue();
            final SubFrontend.Input frontTLDInput =
                SubFrontend.Input.topLevelDefinition(input.inputFileString, olayName, olayAST);
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


        return new NormalOutput(moduleName, strategyFiles, usedStrats, usedAmbStrats, ambStratPositions,
            strategyConstrs, overlayFiles, imports, definedStrats, internalStrats, externalStrats, definedConstrs,
            definedOverlays, congrs, strategyNeedsExternal, overlayConstrs, congrFiles, noOfDefinitions, strategyASTs,
            overlayASTs, congrASTs);
    }

    private void overlayFrontEnd(ExecContext execContext, Input input, final ResourcePath location,
        final SubFrontend.Input frontInput, final String moduleName, StringSetWithPositions definedStrats,
        final Map<String, IStrategoAppl> strategyASTs, final Map<String, File> strategyFiles,
        final Map<String, StringSetWithPositions> strategyConstrs, final List<IStrategoString> strategyNeedsExternal,
        final Map<String, Set<String>> usedAmbStrats, final StringSetWithPositions ambStratPositions,
        final StringSetWithPositions usedStrats, StringSetWithPositions definedOverlays, final Map<String, List<IStrategoAppl>> overlayASTs,
        final Map<String, Integer> noOfDefinitions) throws ExecException, InterruptedException {
        final IStrategoTerm result = execContext.require(strIncrSubFront, frontInput).result;
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
        final IStrategoTerm result = execContext.require(strIncrSubFront, frontInput).result;
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
        final IStrategoTerm result = execContext.require(strIncrSubFront, frontInput).result;
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
    private static void collectUsedNames(IStrategoTerm strategyAST, StringSetWithPositions usedConstrs,
        StringSetWithPositions usedStrats, Map<String, Set<String>> usedAmbStrats,
        StringSetWithPositions ambStratPositions) {
        final TermVisitor visitor = new UsedNames(usedConstrs, usedStrats, usedAmbStrats, ambStratPositions);
        visitor.visit(strategyAST);
    }

    private static void collectUsedNames(IStrategoTerm overlayASTList, StringSetWithPositions usedConstrs) {
        final TermVisitor visitor = new UsedConstrs(usedConstrs);
        visitor.visit(overlayASTList);
    }

    private static Set<String> annoDefAnnotations(IStrategoAppl strategyAST) {
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

    private static boolean needsExternal(Set<String> annoDefAnnotations) {
        return annoDefAnnotations.contains("Override") || annoDefAnnotations.contains("Extend");
    }

    private static boolean isInternal(Set<String> annoDefAnnotations) {
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
