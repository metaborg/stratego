package mb.stratego.build.strincr.task;

import java.util.ArrayList;

import javax.inject.Inject;

import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.pie.task.archive.UnarchiveFromJar;
import mb.resource.ResourceService;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.path.string.ExtensionPathStringMatcher;
import mb.stratego.build.strincr.ResourcePathConverter;
import mb.stratego.build.strincr.Stratego2LibInfo;
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
        Stratego2LibInfo stratego2LibInfo = context.require(input.stratego2LibInfoSupplier);
        for(ResourcePath jarFileOrDir : stratego2LibInfo.jarFilesOrDirectories) {
            final ResourceService resourceService = context.getResourceService();
            final HierarchicalResource jarResourceOfDir =
                resourceService.getHierarchicalResource(jarFileOrDir);
            if(jarResourceOfDir.isFile()) {
                final UnarchiveFromJar.Input unarchiveInput =
                    new UnarchiveFromJar.Input(jarFileOrDir, input.outputDir,
                        new ExtensionPathStringMatcher("class"), false, true,
                        input.stratego2LibInfoSupplier);
                context.require(unarchiveFromJar, unarchiveInput);
            } else if(jarResourceOfDir.isDirectory()) {
                // TODO copy entire directory structure with files to input.outputDir
//                jarResourceOfDir.list(new FileResourceMatcher()).forEach(fileResource -> {
//                    final String relativePath = jarFileOrDir.relativize(fileResource.getPath());
//                    resourceService.getWritableResource(input.outputDir.appendAsRelativePath(relativePath))
//                });
            }
        }
        return new CLCFOutput(new ArrayList<>(0));
    }

    @Override public String getId() {
        return id;
    }
}
