package mb.stratego.typed.pack;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class Packer {

    /**
     * Packs StrategoCore strategy bodies in a directory into a single strategy
     * definition in a file
     * 
     * @param dir
     *            Directory that only includes .aterm files with the strategy bodies
     * @param strategyName
     *            The strategy name to use for the strategy definition, expected to
     *            end with _a_b where a and b are the number of strategy and term
     *            variables resp.
     * @throws IOException
     *             When there is a file system problem
     */
    public static void pack(Path dir, String strategyName) throws IOException {
        // find the latest underscore and assume the term variable count is between that
        // and the end of the string
        int uLast = strategyName.lastIndexOf('_');
        int tvars = Integer.parseInt(strategyName.substring(uLast + 1, strategyName.length()));
        // find the underscore before that and assume the strategy variable count is
        // between that and the latest underscore
        int uSecondLast = strategyName.lastIndexOf('_', uLast - 1);
        int svars = Integer.parseInt(strategyName.substring(uSecondLast + 1, uLast));

        pack(dir, strategyName, svars, tvars);
    }

    /**
     * Packs StrategoCore strategy bodies in a directory into a single strategy
     * definition in a file
     * 
     * @param dir
     *            Directory that only includes .aterm files with the strategy bodies
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
    public static void pack(Path dir, String strategyName, int svars, int tvars) throws IOException {
        // TODO: make output file configurable
        final Path outputFile = dir.resolve("packed$.aterm");
        // We use the nio.Channel API here because it is supposed to be the fastest way
        // to append one file to another
        // This may need to be changed when using this tool inside Spoofax, dealing with
        // a virtual file system...
        try (final FileChannel outChannel = FileChannel.open(outputFile, StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
            write(outChannel, sdeftStart(strategyName, svars, tvars));
            outChannel.force(false);

            // We need to be careful not to include the outputFile among the input files in
            // the directory
            try (Stream<Path> inputFiles = Files.list(dir).filter(isThisFile(outputFile))) {
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
                write(outChannel, "\n, Id(), Id())");
                byte[] closing = new byte[num_defs];
                Arrays.fill(closing, (byte) ')');
                write(outChannel, closing);
            }
        }
    }

    private static Predicate<? super Path> isThisFile(final Path outputFile) {
        return f -> {
            try {
                return !Files.isSameFile(f, outputFile);
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
