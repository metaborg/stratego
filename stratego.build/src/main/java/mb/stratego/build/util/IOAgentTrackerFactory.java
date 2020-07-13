package mb.stratego.build.util;

import java.io.File;
import java.io.OutputStream;

public interface IOAgentTrackerFactory {
    IOAgentTracker create(File initialDir, String... excludePatterns);

    IOAgentTracker create(File initialDir, OutputStream stdoutStream, OutputStream stderrStream);
}
