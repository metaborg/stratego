package mb.stratego.build.spoofax2;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import org.apache.commons.io.output.TeeOutputStream;
import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.spoofax.core.stratego.ResourceAgent;

import mb.stratego.build.util.IOAgentTracker;

public class ResourceAgentTracker implements IOAgentTracker {
    final ResourceAgent ioAgent;
    final ByteArrayOutputStream stdoutLog = new ByteArrayOutputStream();
    final ByteArrayOutputStream stderrLog = new ByteArrayOutputStream();


    public ResourceAgentTracker(IResourceService resourceService, FileObject initialDir, String... excludePatterns) {
        this(resourceService, initialDir, ResourceAgent.defaultStdout(excludePatterns),
            ResourceAgent.defaultStderr(excludePatterns));
    }

    public ResourceAgentTracker(IResourceService resourceService, FileObject initialDir, OutputStream stdoutStream,
        OutputStream stderrStream) {
        final TeeOutputStream stdout = new TeeOutputStream(stdoutStream, stdoutLog);
        final TeeOutputStream stderr = new TeeOutputStream(stderrStream, stderrLog);
        this.ioAgent = new ResourceAgent(resourceService, initialDir, stdout, stderr);
    }


    @Override public ResourceAgent agent() {
        return ioAgent;
    }

    @Override public String stdout() {
        return stdoutLog.toString();
    }

    @Override public String stderr() {
        return stderrLog.toString();
    }
}
