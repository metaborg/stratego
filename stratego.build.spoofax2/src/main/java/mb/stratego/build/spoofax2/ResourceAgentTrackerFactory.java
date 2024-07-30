package mb.stratego.build.spoofax2;

import java.io.File;
import java.io.OutputStream;


import org.metaborg.core.resource.IResourceService;

import mb.stratego.build.util.IOAgentTracker;
import mb.stratego.build.util.IOAgentTrackerFactory;

public class ResourceAgentTrackerFactory implements IOAgentTrackerFactory {
    private final IResourceService resourceService;


    @jakarta.inject.Inject public ResourceAgentTrackerFactory(IResourceService resourceService) {
        this.resourceService = resourceService;
    }


    @Override public IOAgentTracker create(File initialDir, String... excludePatterns) {
        return new ResourceAgentTracker(resourceService, resourceService.resolve(initialDir), excludePatterns);
    }

    @Override public IOAgentTracker create(File initialDir, OutputStream stdoutStream, OutputStream stderrStream) {
        return new ResourceAgentTracker(resourceService, resourceService.resolve(initialDir), stdoutStream, stderrStream);
    }
}
