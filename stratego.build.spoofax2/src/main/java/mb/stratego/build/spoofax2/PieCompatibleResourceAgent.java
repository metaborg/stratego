package mb.stratego.build.spoofax2;

import mb.resource.DefaultResourceService;
import mb.resource.ResourceKey;
import mb.resource.ResourceKeyString;
import mb.resource.ResourceRuntimeException;
import mb.resource.ResourceService;
import mb.resource.fs.FSResourceRegistry;
import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.spoofax.core.stratego.ResourceAgent;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

public class PieCompatibleResourceAgent extends ResourceAgent {
    // HACK: create a new resource service that supports Java (Path) resources. This is ok because Spoofax 2 only uses local files in the incremental Stratego compiler.
    private final ResourceService pieResourceService = new DefaultResourceService(new FSResourceRegistry());

    public PieCompatibleResourceAgent(IResourceService resourceService, FileObject initialDir, OutputStream stdout, OutputStream stderr) {
        super(resourceService, initialDir, stdout, stderr);
    }

    @Override public int openRandomAccessFile(String fn, String mode) throws IOException {
        try {
            // If `fn` is a resource key string pointing to a File, convert it to a File and use its absolute path.
            final ResourceKey resourceKey = pieResourceService.getResourceKey(ResourceKeyString.parse(fn));
            final @Nullable File localFile = pieResourceService.toLocalFile(resourceKey);
            if(localFile != null) {
                return super.openRandomAccessFile(localFile.getAbsolutePath(), mode);
            }
        } catch(ResourceRuntimeException e) {
            // Ignore
        }
        return super.openRandomAccessFile(fn, mode);
    }
}
