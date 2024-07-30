package mb.stratego.build.strincr.task;

import java.io.IOException;
import java.util.ArrayList;


import mb.pie.api.ExecContext;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.pie.task.archive.UnarchiveFromJar;
import mb.resource.ResourceService;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.ResourceMatcher;
import mb.resource.hierarchical.match.path.string.ExtensionPathStringMatcher;
import mb.stratego.build.strincr.ResourcePathConverter;
import mb.stratego.build.strincr.Stratego2LibInfo;
import mb.stratego.build.strincr.task.input.CLCFInput;
import mb.stratego.build.strincr.task.output.CLCFOutput;

public class CopyLibraryClassFiles implements TaskDef<CLCFInput, CLCFOutput> {
    public static final String id = "stratego." + CopyLibraryClassFiles.class.getSimpleName();

    public final ResourcePathConverter resourcePathConverter;
    public final UnarchiveFromJar unarchiveFromJar;

    @jakarta.inject.Inject public CopyLibraryClassFiles(ResourcePathConverter resourcePathConverter,
        UnarchiveFromJar unarchiveFromJar) {
        this.resourcePathConverter = resourcePathConverter;
        this.unarchiveFromJar = unarchiveFromJar;
    }

    @Override public CLCFOutput exec(ExecContext context, CLCFInput input) throws Exception {
        Stratego2LibInfo stratego2LibInfo = context.require(input.stratego2LibInfoSupplier);
        for(ResourcePath jarFileOrDir : stratego2LibInfo.jarFilesOrDirectories) {
            final ResourceService resourceService = context.getResourceService();
            final HierarchicalResource jarResourceOrDir =
                resourceService.getHierarchicalResource(jarFileOrDir);
            if(jarResourceOrDir.isFile()) {
                final UnarchiveFromJar.Input unarchiveInput =
                    new UnarchiveFromJar.Input(jarFileOrDir, input.outputDir,
                        new ExtensionPathStringMatcher("class"), false, true,
                        input.stratego2LibInfoSupplier);
                context.require(unarchiveFromJar, unarchiveInput);
            } else if(jarResourceOrDir.isDirectory()) {
                copyDirectory(context, jarFileOrDir, input.outputDir);
            }
        }
        return new CLCFOutput(new ArrayList<>(0));
    }

    @Override public String getId() {
        return id;
    }

    public static void copyDirectory(ExecContext context, ResourcePath from, ResourcePath to)
        throws IOException {
        final ResourceService resourceService = context.getResourceService();
        final HierarchicalResource fromHR = resourceService.getHierarchicalResource(from);
        final HierarchicalResource toHR = resourceService.getHierarchicalResource(to);
        fromHR.walkForEach(ResourceMatcher.ofTrue(), fileOrDir -> {
            final HierarchicalResource fileorDirDest = toHR.appendAsRelativePath(from.relativize(fileOrDir.getPath()));
            if(fileOrDir.isDirectory()) {
                context.require(fileOrDir);
                if(!fileorDirDest.exists()) {
                    fileOrDir.copyTo(fileorDirDest);
                }
                context.provide(fileorDirDest);
            } else if(fileOrDir.isFile()) {
                context.require(fileOrDir, ResourceStampers.modifiedFile());
                fileOrDir.copyTo(fileorDirDest);
                context.provide(fileorDirDest, ResourceStampers.hashFile());
            }
        });
    }

}
