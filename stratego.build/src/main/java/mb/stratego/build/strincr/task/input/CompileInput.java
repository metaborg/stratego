package mb.stratego.build.strincr.task.input;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

import javax.annotation.Nullable;

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
    public final String packageName;
    public final @Nullable ResourcePath cacheDir;
    public final ArrayList<String> constants;
    public final Arguments extraArgs;
    public final boolean library;
    public final boolean usingLegacyStrategoStdLib;
    public final boolean createShadowJar;
    public final String libraryName;

    public CompileInput(IModuleImportService.ModuleIdentifier mainModuleIdentifier,
        ResourcePath projectPath, ResourcePath outputDir, ResourcePath str2libReplicateDir, String packageName,
        @Nullable ResourcePath cacheDir, ArrayList<String> constants,
        ArrayList<ResourcePath> includeDirs,
        ArrayList<? extends IModuleImportService.ModuleIdentifier> linkedLibraries,
        Arguments extraArgs, ArrayList<STask<?>> strFileGeneratingTasks, boolean library,
        boolean autoImportStd, boolean createShadowJar, String libraryName,
        ArrayList<Supplier<Stratego2LibInfo>> str2libraries) {
        this.str2libReplicateDir = str2libReplicateDir;
        this.libraryName = libraryName;
        this.checkInput =
            new CheckInput(mainModuleIdentifier, projectPath, new IModuleImportService.ImportResolutionInfo(strFileGeneratingTasks, includeDirs,
                linkedLibraries, str2libraries), autoImportStd);
        this.outputDir = outputDir.getNormalized();
        this.packageName = packageName;
        this.cacheDir = cacheDir;
        this.constants = constants;
        this.extraArgs = extraArgs;
        this.library = library;
        this.createShadowJar = createShadowJar;
        this.usingLegacyStrategoStdLib =
            linkedLibraries.contains(BuiltinLibraryIdentifier.StrategoLib);
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        CompileInput that = (CompileInput) o;

        if(!checkInput.equals(that.checkInput))
            return false;
        if(!outputDir.equals(that.outputDir))
            return false;
        if(!str2libReplicateDir.equals(that.str2libReplicateDir))
            return false;
        if(!packageName.equals(that.packageName))
            return false;
        if(!Objects.equals(cacheDir, that.cacheDir))
            return false;
        if(!constants.equals(that.constants))
            return false;
        if(!extraArgs.equals(that.extraArgs))
            return false;
        if(library != that.library)
            return false;
        return usingLegacyStrategoStdLib == that.usingLegacyStrategoStdLib;
    }

    @Override public int hashCode() {
        int result = checkInput.hashCode();
        result = 31 * result + outputDir.hashCode();
        result = 31 * result + str2libReplicateDir.hashCode();
        result = 31 * result + packageName.hashCode();
        result = 31 * result + (cacheDir != null ? cacheDir.hashCode() : 0);
        result = 31 * result + constants.hashCode();
        result = 31 * result + extraArgs.hashCode();
        result = 31 * result + (library ? 1 : 0);
        result = 31 * result + (usingLegacyStrategoStdLib ? 1 : 0);
        return result;
    }

    @Override public String toString() {
        return "Compile.Input(" + checkInput.mainModuleIdentifier + ")";
    }
}
