package mb.stratego.compiler.pack;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
// import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.Nullable;

public class Packer {
    private static final String SPEC_START = "Specification([Signature([Constructors([])]), Strategies([\n";
    private static final String SPEC_END = "\n])])";

    /**
     * Packs StrategoCore strategy definitions in a directory into a single strategy definition in a file
     * 
     * @param inputDir
     *            Directory that only includes .aterm files with the strategy bodies
     * @param outputFile
     * @param strategyName
     *            The strategy name to use for the strategy definition, expected to end with _a_b where a and b are the
     *            number of strategy and term variables resp.
     * @throws IOException
     *             When there is a file system problem
     */
    public static void packStrategy(Path inputDir, @Nullable Path outputFile, String strategyName) throws IOException {
        if(outputFile == null) {
            outputFile = Paths.get(inputDir.getParent().getParent().toString(), "stratego.compiler.pack",
                strategyName + ".ctree");
        }
        try(Stream<Path> inputFiles = Files.list(inputDir).filter(relevantFiles(outputFile))) {
            Iterable<Path> iterable = () -> inputFiles.iterator();
            packStrategy(iterable, outputFile, strategyName);
        }
    }

    /**
     * Packs StrategoCore strategy definition in some paths into a single strategy definition in a file
     * 
     * @param paths
     *            Paths that only include .aterm files with the strategy bodies
     * @param outputFile
     * @param strategyName
     *            The strategy name to use for the strategy definition, expected to end with _a_b where a and b are the
     *            number of strategy and term variables resp.
     * @throws IOException
     *             When there is a file system problem
     */
    public static void packStrategy(Iterable<Path> paths, Path outputFile, String strategyName) throws IOException {
        Files.createDirectories(outputFile.getParent());

        // We use the nio.Channel API here because it is supposed to be the fastest way
        // to append one file to another
        // This may need to be changed when using this tool inside Spoofax, dealing with
        // a virtual file system...
        openFile: try(final FileChannel outChannel = FileChannel.open(outputFile, StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
            write(outChannel, SPEC_START);

            String next_line = "";
            for(Path inputFile : paths) {
                write(outChannel, next_line);
                try(final FileChannel inChannel = FileChannel.open(inputFile, StandardOpenOption.READ)) {
                    transferContents(inChannel, outChannel);
                }
                next_line = "\n,";
            }
            if(next_line == "") {
                // When there are no relevant files, close the file
                break openFile;
            }
            write(outChannel, SPEC_END);
            return; // success
        }
        // Break to here when there are no relevant files; clean up incomplete file
        Files.delete(outputFile);
    }

    /**
     * Packs Strategy definitions in a directory into a single CTree definition in a file
     * 
     * @param inputDir
     *            Directory that only includes .aterm files with the strategy definitions
     * @param outputFile
     * @throws IOException
     *             When there is a file system problem or no aterm files were found
     */
    public static void packBoilerplate(Path inputDir, @Nullable Path outputFile) throws IOException {
        if(outputFile == null) {
            outputFile = Paths.get(inputDir.getParent().toString(), "stratego.compiler.pack", "boilerplate.ctree");
        }
        try(Stream<Path> inputFiles = Files.list(inputDir).filter(relevantFiles(outputFile))) {
            Iterable<Path> iterable = () -> inputFiles.iterator();
            packBoilerplate(iterable, outputFile);
        }
    }

    /**
     * Packs Strategy definitions of some paths into a single CTree definition in a file
     * 
     * @param paths
     *            Paths that only include .aterm files with the strategy definitions
     * @param outputFile
     * @throws IOException
     *             When there is a file system problem or no aterm files were found
     */
    public static void packBoilerplate(Iterable<Path> paths, Path outputFile) throws IOException {
        Files.createDirectories(outputFile.getParent());

        try(final FileChannel outChannel = FileChannel.open(outputFile, StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
            write(outChannel, SPEC_START);

            int num_defs = 0;
            String next_line = "\n";
            for(Path inputFile : paths) {
                num_defs++;
                write(outChannel, next_line);
                try(final FileChannel inChannel = FileChannel.open(inputFile, StandardOpenOption.READ)) {
                    transferContents(inChannel, outChannel);
                }
                next_line = "\n,\n";
            }
            if(num_defs == 0) {
                throw new IOException("Input directory does not contain aterm files");
            }
            write(outChannel, SPEC_END);
        }
    }

    private static Predicate<? super Path> relevantFiles(final Path outputFile) {
        return f -> {
            try {
                return f.getFileName().toString().endsWith(".aterm") && !Files.isSameFile(f, outputFile);
            } catch(IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    private static void transferContents(final FileChannel inChannel, final FileChannel outChannel) throws IOException {
        long transfered = 0;
        long total = inChannel.size();
        while(transfered < total) {
            transfered += outChannel.transferFrom(inChannel, outChannel.position(), inChannel.size());
            outChannel.position(outChannel.position() + transfered);
        }
    }

    private static void write(final FileChannel channel, final String string) throws IOException {
        write(channel, string.getBytes());
    }

    private static void write(final FileChannel channel, final byte[] bytes) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        long transfered = 0;
        long total = bytes.length;
        while(transfered < total) {
            transfered += channel.write(buffer);
        }
    }
}
