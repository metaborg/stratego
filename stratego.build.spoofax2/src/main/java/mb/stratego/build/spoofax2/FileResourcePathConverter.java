package mb.stratego.build.spoofax2;

import mb.resource.ResourceService;
import mb.resource.hierarchical.ResourcePath;
import mb.stratego.build.strincr.ResourcePathConverter;

import jakarta.annotation.Nullable;
import java.io.File;

public class FileResourcePathConverter implements ResourcePathConverter {
    private final ResourceService resourceService;

    @jakarta.inject.Inject public FileResourcePathConverter(ResourceService resourceService) {
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
