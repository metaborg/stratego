package mb.stratego.build.strincr.task.input;

import java.io.Serializable;
import java.util.ArrayList;

import javax.annotation.Nullable;

import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.core.project.NameUtil;
import org.metaborg.util.cmd.Arguments;

import mb.pie.api.STask;
import mb.resource.hierarchical.ResourcePath;
import mb.stratego.build.strincr.BuiltinLibraryIdentifier;
import mb.stratego.build.strincr.IModuleImportService;

public class CompileInput implements Serializable {
    public final CheckInput checkInput;
    public final ResourcePath outputDir;
    public final @Nullable String packageName;
    public final @Nullable ResourcePath cacheDir;
    public final ArrayList<String> constants;
    public final Arguments extraArgs;
    public final boolean library;
    public final boolean usingLegacyStrategoStdLib;
    public final String libraryName;
    public final LanguageIdentifier languageIdentifier;

    public CompileInput(IModuleImportService.ModuleIdentifier mainModuleIdentifier,
        ResourcePath projectPath, ResourcePath outputDir, @Nullable ResourcePath cacheDir,
        ArrayList<String> constants, ArrayList<ResourcePath> includeDirs,
        ArrayList<? extends IModuleImportService.ModuleIdentifier> linkedLibraries,
        Arguments extraArgs, ArrayList<STask<?>> strFileGeneratingTasks, boolean library,
        boolean autoImportStd, String libraryName, LanguageIdentifier languageIdentifier) {
        this(mainModuleIdentifier, projectPath, outputDir,
            NameUtil.toJavaId(languageIdentifier.id) + ".trans", cacheDir, constants, includeDirs,
            linkedLibraries, extraArgs, strFileGeneratingTasks, library, autoImportStd, libraryName,
            languageIdentifier);
    }

    public CompileInput(IModuleImportService.ModuleIdentifier mainModuleIdentifier,
        ResourcePath projectPath, ResourcePath outputDir, @Nullable String packageName,
        @Nullable ResourcePath cacheDir, ArrayList<String> constants,
        ArrayList<ResourcePath> includeDirs,
        ArrayList<? extends IModuleImportService.ModuleIdentifier> linkedLibraries,
        Arguments extraArgs, ArrayList<STask<?>> strFileGeneratingTasks, boolean library,
        boolean autoImportStd, String libraryName, LanguageIdentifier languageIdentifier) {
        this.libraryName = libraryName;
        this.languageIdentifier = languageIdentifier;
        this.checkInput =
            new CheckInput(mainModuleIdentifier, projectPath, strFileGeneratingTasks, includeDirs,
                linkedLibraries, autoImportStd);
        this.outputDir = outputDir.getNormalized();
        this.packageName = packageName;
        this.cacheDir = cacheDir;
        this.constants = constants;
        this.extraArgs = extraArgs;
        this.library = library;
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
        if(packageName != null ? !packageName.equals(that.packageName) : that.packageName != null)
            return false;
        if(cacheDir != null ? !cacheDir.equals(that.cacheDir) : that.cacheDir != null)
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
        result = 31 * result + (packageName != null ? packageName.hashCode() : 0);
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
