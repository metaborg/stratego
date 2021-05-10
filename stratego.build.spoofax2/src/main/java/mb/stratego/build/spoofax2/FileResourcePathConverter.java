package mb.stratego.build.spoofax2;

import mb.resource.ResourceService;
import mb.resource.hierarchical.ResourcePath;
import mb.stratego.build.strincr.ResourcePathConverter;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.File;

public class FileResourcePathConverter implements ResourcePathConverter {
    private final ResourceService resourceService;

    @Inject public FileResourcePathConverter(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @Override public String toString(ResourcePath resourcePath) {
        final @Nullable File file = resourceService.toLocalFile(resourcePath);
        if(file != null) {
            return file.toString();
        } else {
            return resourcePath.asString();
        }
    }
}
