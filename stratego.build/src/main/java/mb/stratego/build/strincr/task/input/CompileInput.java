package mb.stratego.build.strincr.task.input;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;

import jakarta.annotation.Nullable;

import org.metaborg.util.cmd.Arguments;

import mb.pie.api.STask;
import mb.pie.api.Supplier;
import mb.resource.hierarchical.ResourcePath;
import mb.stratego.build.strincr.BuiltinLibraryIdentifier;
import mb.stratego.build.strincr.IModuleImportService;
import mb.stratego.build.strincr.Stratego2LibInfo;

public class CompileInput implements Serializable {
    public final CheckInput checkInput;
    public final ResourcePath outputDir;
    public final ResourcePath str2libReplicateDir;
    public final ArrayList<String> packageNames;
    public final @Nullable ResourcePath cacheDir;
    public final ArrayList<String> constants;
    public final Arguments extraArgs;
    public final boolean library;
    public final boolean createShadowJar;
    public final String libraryName;
    protected final int hashCode;

    public CompileInput(IModuleImportService.ModuleIdentifier mainModuleIdentifier,
        ResourcePath projectPath, ResourcePath outputDir, ResourcePath str2libReplicateDir,
        ArrayList<String> packageNames, @Nullable ResourcePath cacheDir, ArrayList<String> constants,
        LinkedHashSet<ResourcePath> includeDirs,
        ArrayList<? extends IModuleImportService.ModuleIdentifier> linkedLibraries,
        Arguments extraArgs, ArrayList<STask<?>> strFileGeneratingTasks, boolean library,
        boolean autoImportStd, boolean createShadowJar, String libraryName,
        LinkedHashSet<Supplier<Stratego2LibInfo>> str2libraries, boolean supportRTree,
        boolean supportStr1, @Nullable ResourcePath resolveExternals) {
        this.str2libReplicateDir = str2libReplicateDir;
        this.libraryName = libraryName;
        this.checkInput =
            new CheckInput(mainModuleIdentifier, projectPath, new IModuleImportService.ImportResolutionInfo(strFileGeneratingTasks, includeDirs,
                linkedLibraries, str2libraries, supportRTree, supportStr1, resolveExternals), autoImportStd);
        this.outputDir = outputDir.getNormalized();
        this.packageNames = packageNames;
        this.cacheDir = cacheDir;
        this.constants = constants;
        this.extraArgs = extraArgs;
        this.library = library;
        this.createShadowJar = createShadowJar;
        this.hashCode = hashFunction();
    }

    public CompileInput(IModuleImportService.ModuleIdentifier mainModuleIdentifier,
        ResourcePath projectPath, ResourcePath outputDir, ResourcePath str2libReplicateDir, String packageName,
        @Nullable ResourcePath cacheDir, ArrayList<String> constants,
        LinkedHashSet<ResourcePath> includeDirs,
        ArrayList<? extends IModuleImportService.ModuleIdentifier> linkedLibraries,
        Arguments extraArgs, ArrayList<STask<?>> strFileGeneratingTasks, boolean library,
        boolean autoImportStd, boolean createShadowJar, String libraryName,
        LinkedHashSet<Supplier<Stratego2LibInfo>> str2libraries, boolean supportRTree,
        boolean supportStr1, @Nullable ResourcePath resolveExternals) {
        this(mainModuleIdentifier, projectPath, outputDir, str2libReplicateDir,
            new ArrayList<>(Collections.singletonList(packageName)), cacheDir, constants,
            includeDirs, linkedLibraries, extraArgs, strFileGeneratingTasks, library, autoImportStd,
            createShadowJar, libraryName, str2libraries, supportRTree, supportStr1,
            resolveExternals);
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        CompileInput that = (CompileInput)o;

        if(hashCode != that.hashCode)
            return false;
        if(library != that.library)
            return false;
        if(createShadowJar != that.createShadowJar)
            return false;
        if(!checkInput.equals(that.checkInput))
            return false;
        if(!outputDir.equals(that.outputDir))
            return false;
        if(!str2libReplicateDir.equals(that.str2libReplicateDir))
            return false;
        if(!packageNames.equals(that.packageNames))
            return false;
        if(!Objects.equals(cacheDir, that.cacheDir))
            return false;
        if(!constants.equals(that.constants))
            return false;
        if(!extraArgs.equals(that.extraArgs))
            return false;
        return libraryName.equals(that.libraryName);
    }

    @Override public int hashCode() {
        return this.hashCode;
    }

    protected int hashFunction() {
        int result = checkInput.hashCode();
        result = 31 * result + outputDir.hashCode();
        result = 31 * result + str2libReplicateDir.hashCode();
        result = 31 * result + packageNames.hashCode();
        result = 31 * result + (cacheDir != null ? cacheDir.hashCode() : 0);
        result = 31 * result + constants.hashCode();
        result = 31 * result + extraArgs.hashCode();
        result = 31 * result + (library ? 1 : 0);
        result = 31 * result + (createShadowJar ? 1 : 0);
        result = 31 * result + libraryName.hashCode();
        return result;
    }

    @Override public String toString() {
        //@formatter:off
        return "CompileInput@" + System.identityHashCode(this) + '{'
            + "checkInput=" + checkInput
            + ", outputDir=" + outputDir
            + ", str2libReplicateDir=" + str2libReplicateDir
            + ", packageName='" + packageNames + '\''
            + (cacheDir == null ? "" : ", cacheDir=" + cacheDir)
            + ", constants=" + constants
            + ", extraArgs='" + extraArgs + '\''
            + ", library=" + library
            + ", createShadowJar=" + createShadowJar
            + ", libraryName='" + libraryName + '\''
            + '}';
        //@formatter:on
    }
}
