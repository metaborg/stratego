package mb.stratego.junit_bridge;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public abstract class StrTests {
    protected static Path getStrjUnderTest() {
        return Paths.get(System.getProperty("strj-sut"));
    }

    protected Path getLocalResourcePath() throws URISyntaxException {
        return Paths.get(this.getClass().getResource(".").toURI());
    }

    protected Path getResourcePathRoot() throws URISyntaxException {
        return Paths.get(this.getClass().getResource("/").toURI());
    }

    protected static Iterable<? extends File> javaFiles(Path packageDir) throws IOException {
        final List<File> result = new ArrayList<>();
        try(DirectoryStream<Path> javaPaths = Files.newDirectoryStream(packageDir,
            p -> Files.isRegularFile(p) && p.getFileName().toString().endsWith(".java"))) {
            for(Path javaPath : javaPaths) {
                result.add(javaPath.toFile());
            }
        }
        return result;
    }
}
