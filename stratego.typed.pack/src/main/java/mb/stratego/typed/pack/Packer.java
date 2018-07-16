package mb.stratego.typed.pack;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.Nullable;

public class Packer {
    private static final String SPEC_START = "Specification([Signature([Constructors([])]), Strategies([\n";
    private static final String SPEC_END = "\n])])";

    /**
     * Packs StrategoCore strategy bodies in a directory into a single strategy
     * definition in a file
     * 
     * @param inputDir
     *            Directory that only includes .aterm files with the strategy bodies
     * @param outputFile
     * @param strategyName 
     *            The strategy name to use for the strategy definition, expected to
     *            end with _a_b where a and b are the number of strategy and term
     *            variables resp.
     * @throws IOException
     *             When there is a file system problem
     */
    public static void packStrategy(Path inputDir, @Nullable Path outputFile, String strategyName) throws IOException {
        // find the latest underscore and assume the term variable count is between that
        // and the end of the string
        int uLast = strategyName.lastIndexOf('_');
        int tvars = Integer.parseInt(strategyName.substring(uLast + 1, strategyName.length()));
        // find the underscore before that and assume the strategy variable count is
        // between that and the latest underscore
        int uSecondLast = strategyName.lastIndexOf('_', uLast - 1);
        int svars = Integer.parseInt(strategyName.substring(uSecondLast + 1, uLast));

        if(outputFile == null) {
            outputFile = Paths.get(inputDir.getParent().getParent().toString(), "stratego.typed.pack", strategyName + ".ctree");
        }
        Files.createDirectories(outputFile.getParent());

        packStrategy(inputDir, outputFile, strategyName, svars, tvars);
    }

    /**
     * Packs StrategoCore strategy bodies in a directory into a single strategy
     * definition in a file
     * 
     * @param inputDir
     *            Directory that only includes .aterm files with the strategy bodies
     * @param outputFile
     *            The file to output to. This file will be truncated if one already exists.
     * @param strategyName
     *            The strategy name to use for the strategy definition, expected to
     *            end with _a_b where a and b are the number of strategy and term
     *            variables resp.
     * @param svars
     *            the a in the above string
     * @param tvars
     *            the b in the above string
     * @throws IOException
     *             When there is a file system problem
     */
    public static void packStrategy(Path inputDir, Path outputFile, String strategyName, int svars, int tvars) throws IOException {
        // We use the nio.Channel API here because it is supposed to be the fastest way
        // to append one file to another
        // This may need to be changed when using this tool inside Spoofax, dealing with
        // a virtual file system...
        openFile:
        try (final FileChannel outChannel = FileChannel.open(outputFile, StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
            write(outChannel, SPEC_START);
            write(outChannel, sdeftStart(strategyName, svars, tvars));

            // We need to be careful not to include the outputFile among the input files in
            // the directory
            try (Stream<Path> inputFiles = Files.list(inputDir).filter(relevantFiles(outputFile))) {
                int num_defs = 0;
                // Iterable instead of Stream.forEach, because of exceptions and counting
                // num_defs
                Iterable<Path> iterable = () -> inputFiles.iterator();
                String next_line = "GuardedLChoice(\n";
                for (Path inputFile : iterable) {
                    num_defs++;
                    write(outChannel, next_line);
                    try (final FileChannel inChannel = FileChannel.open(inputFile, StandardOpenOption.READ)) {
                        transferContents(inChannel, outChannel);
                    }
                    next_line = "\n, Id(), GuardedLChoice(\n";
                }
                if (num_defs == 0) {
                    // When there are no relevant files, close the file
                    break openFile;
                }
                write(outChannel, "\n, Id(), Id())");
                byte[] closing = new byte[num_defs];
                Arrays.fill(closing, (byte) ')');
                write(outChannel, closing);
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
            outputFile = Paths.get(inputDir.getParent().getParent().toString(), "stratego.typed.pack", "boilerplate.ctree");
        }
        Files.createDirectories(outputFile.getParent());

        try (final FileChannel outChannel = FileChannel.open(outputFile, StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
            write(outChannel, SPEC_START);

            // We need to be careful not to include the outputFile among the input files in
            // the directory
            try (Stream<Path> inputFiles = Files.list(inputDir).filter(relevantFiles(outputFile))) {
                int num_defs = 0;
                // Iterable instead of Stream.forEach, because of exceptions and counting
                // num_defs
                Iterable<Path> iterable = () -> inputFiles.iterator();
                String next_line = "\n";
                for (Path inputFile : iterable) {
                    num_defs++;
                    write(outChannel, next_line);
                    try (final FileChannel inChannel = FileChannel.open(inputFile, StandardOpenOption.READ)) {
                        transferContents(inChannel, outChannel);
                    }
                    next_line = "\n,\n";
                }
                if (num_defs == 0) {
                    throw new IOException("Input directory does not contain aterm files");
                }
            }
            write(outChannel, SPEC_END);
        }
    }

    private static Predicate<? super Path> relevantFiles(final Path outputFile) {
        return f -> {
            try {
                return f.getFileName().toString().endsWith(".aterm") && !Files.isSameFile(f, outputFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    private static void transferContents(final FileChannel inChannel, final FileChannel outChannel) throws IOException {
        long transfered = 0;
        long total = inChannel.size();
        while (transfered < total) {
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
        while (transfered < total) {
            transfered += channel.write(buffer);
        }
    }

    /**
     * @param strategyName
     * @param svars
     *            amount of strategy variables
     * @param tvars
     *            amount of term variables
     * @return the start of the CTree for a strategy definition with the given name
     *         and argument amounts
     */
    private static String sdeftStart(String strategyName, int svars, int tvars) {
        final StringBuilder out = new StringBuilder();

        out.append("SDefT(\"");
        out.append(strategyName);
        out.append("\", [");
        for (int i = 0; i < svars; i++) {
            out.append(svarArg(i));
            if (i < svars - 1) {
                out.append(", ");
            }
        }
        out.append("], [");
        for (int i = 0; i < tvars; i++) {
            out.append(tvarArg(i));
            if (i < tvars - 1) {
                out.append(", ");
            }
        }
        out.append("], \n");
        return out.toString();
    }

    /**
     * @param i
     *            index of the term variable
     * @return CTree for term variable argument of a strategy definition, with
     *         generic type
     */
    private static String tvarArg(int i) {
        return "VarDec(\"tvar_" + i + "\",ConstType(Sort(\"ATerm\",[])))";
    }

    /**
     * @param i
     *            index of the strategy variable
     * @return CTree for strategy variable argument of a strategy definition, with
     *         generic type
     */
    private static String svarArg(int i) {
        return "VarDec(\"svar_" + i + "\",FunType([ConstType(Sort(\"ATerm\",[]))],ConstType(Sort(\"ATerm\",[]))))";
    }

}
