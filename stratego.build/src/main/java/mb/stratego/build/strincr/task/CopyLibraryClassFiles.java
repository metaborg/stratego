package mb.stratego.build.strincr.task;

import java.util.ArrayList;

import javax.inject.Inject;

import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.pie.task.archive.UnarchiveFromJar;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.path.string.ExtensionPathStringMatcher;
import mb.stratego.build.strincr.ResourcePathConverter;
import mb.stratego.build.strincr.task.input.CLCFInput;
import mb.stratego.build.strincr.task.output.CLCFOutput;

public class CopyLibraryClassFiles implements TaskDef<CLCFInput, CLCFOutput> {
    public static final String id = "stratego." + CopyLibraryClassFiles.class.getSimpleName();

    public final ResourcePathConverter resourcePathConverter;
    public final UnarchiveFromJar unarchiveFromJar;

    @Inject public CopyLibraryClassFiles(ResourcePathConverter resourcePathConverter,
        UnarchiveFromJar unarchiveFromJar) {
        this.resourcePathConverter = resourcePathConverter;
        this.unarchiveFromJar = unarchiveFromJar;
    }

    @Override public CLCFOutput exec(ExecContext context, CLCFInput input) throws Exception {
        for(ResourcePath jarFile : input.stratego2LibInfo.jarFiles) {
            final UnarchiveFromJar.Input unarchiveInput =
                new UnarchiveFromJar.Input(jarFile, input.outputDir,
                    new ExtensionPathStringMatcher("class"), false, true, null);
            context.require(unarchiveFromJar, unarchiveInput);
        }
        return new CLCFOutput(new ArrayList<>(0));
    }

    @Override public String getId() {
        return id;
    }
}
