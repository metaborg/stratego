package mb.stratego.build.strincr.task;

import java.io.IOException;
import java.util.ArrayList;

import javax.inject.Inject;

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

    @Inject public CopyLibraryClassFiles(ResourcePathConverter resourcePathConverter,
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
                copyDirectory(context, jarResourceOrDir.getPath(), input.outputDir);
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
        fromHR.walkForEach(ResourceMatcher.ofTrue(), f -> {
            final HierarchicalResource fToHR = resourceService
                .getHierarchicalResource(to.appendAsRelativePath(from.relativize(f.getPath())));
            if(f.isDirectory()) {
                context.require(f);
                f.copyTo(fToHR);
                context.provide(fToHR);
            } else if(f.isFile()) {
                context.require(f, ResourceStampers.modifiedFile());
                f.copyTo(fToHR);
                context.provide(fToHR, ResourceStampers.hashFile());
            }
        });
    }

}
