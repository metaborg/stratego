package mb.stratego.build.strincr.task.input;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

import javax.annotation.Nullable;

import org.metaborg.util.cmd.Arguments;

import mb.pie.api.STask;
import mb.pie.api.STaskDef;
import mb.pie.api.Supplier;
import mb.resource.hierarchical.ResourcePath;
import mb.stratego.build.strincr.BuiltinLibraryIdentifier;
import mb.stratego.build.strincr.IModuleImportService;
import mb.stratego.build.strincr.Stratego2LibInfo;
import mb.stratego.build.strincr.task.output.CheckModuleOutput;

public class CompileDynamicRulesInput implements Serializable {
    public final CheckInput checkInput;
    public final ResourcePath outputDirWithPackage;
    public final String packageName;
    public final @Nullable ResourcePath cacheDir;
    public final ArrayList<String> constants;
    public final Arguments extraArgs;
    public final boolean usingLegacyStrategoStdLib;
    public final STaskDef<CheckModuleInput, CheckModuleOutput> strategyAnalysisDataTask;

    public CompileDynamicRulesInput(ResourcePath outputDirWithPackage, String packageName,
        ResourcePath cacheDir, ArrayList<String> constants, Arguments extraArgs, CheckInput checkInput,
        STaskDef<CheckModuleInput, CheckModuleOutput> strategyAnalysisDataTask,
        boolean usingLegacyStrategoStdLib) {
        this.checkInput = checkInput;
        this.outputDirWithPackage = outputDirWithPackage;
        this.packageName = packageName;
        this.cacheDir = cacheDir;
        this.constants = constants;
        this.extraArgs = extraArgs;
        this.usingLegacyStrategoStdLib = usingLegacyStrategoStdLib;
        this.strategyAnalysisDataTask = strategyAnalysisDataTask;
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        CompileDynamicRulesInput that = (CompileDynamicRulesInput) o;

        if(!checkInput.equals(that.checkInput))
            return false;
        if(!outputDirWithPackage.equals(that.outputDirWithPackage))
            return false;
        if(!packageName.equals(that.packageName))
            return false;
        if(!Objects.equals(cacheDir, that.cacheDir))
            return false;
        if(!constants.equals(that.constants))
            return false;
        if(!extraArgs.equals(that.extraArgs))
            return false;
        return usingLegacyStrategoStdLib == that.usingLegacyStrategoStdLib;
    }

    @Override public int hashCode() {
        int result = checkInput.hashCode();
        result = 31 * result + outputDirWithPackage.hashCode();
        result = 31 * result + packageName.hashCode();
        result = 31 * result + (cacheDir != null ? cacheDir.hashCode() : 0);
        result = 31 * result + constants.hashCode();
        result = 31 * result + extraArgs.hashCode();
        result = 31 * result + (usingLegacyStrategoStdLib ? 1 : 0);
        return result;
    }

    @Override public String toString() {
        //@formatter:off
        return "CompileDynamicRulesInput@" + System.identityHashCode(this) + '{'
            + "checkInput=" + checkInput
            + ", outputDirWithPackage=" + outputDirWithPackage
            + ", packageName='" + packageName + '\''
            + (cacheDir == null ? "" : ", cacheDir=" + cacheDir)
            + ", constants=" + constants
            + ", extraArgs=" + extraArgs
            + ", usingLegacyStrategoStdLib=" + usingLegacyStrategoStdLib
            + ", strategyAnalysisDataTask=" + strategyAnalysisDataTask
            + '}';
        //@formatter:on
    }
}
