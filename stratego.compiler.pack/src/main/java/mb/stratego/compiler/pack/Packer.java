package mb.stratego.compiler.pack;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.util.iterators.Iterables2;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import javax.annotation.Nullable;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class Packer {
    private static final String SPEC_START = "Specification([Signature([Constructors([])]), Overlays([\n";
    private static final String SPEC_MIDDLE = "\n]), Strategies([\n";
    private static final String SPEC_END = "\n])])";
    private static final String ANNO_START = "\n{";
    private static final String ANNO_END = "}";
    private static final int BUFFER = 8192;

    /**
     * Packs StrategoCore strategy definitions in a directory into a single strategy definition in a file
     *
     * @param strategyDir  Directory that only includes .aterm files with the strategy bodies
     * @param outputFile   Path of the outputFile
     * @param strategyName The strategy name to use for the strategy definition
     * @throws IOException When there is a file system problem
     */
    public static void packStrategy(Path strategyDir, @Nullable Path outputFile, String strategyName)
        throws IOException {
        if(outputFile == null) {
            outputFile = Paths
                .get(strategyDir.getParent().getParent().toString(), "stratego.compiler.pack", strategyName + ".ctree");
        }
        try(Stream<Path> strategyFiles = Files.list(strategyDir).filter(relevantFiles(outputFile))) {
            Iterable<Path> strategyIterable = strategyFiles::iterator;
            Iterable<Path> overlayIterable = Iterables2.empty();
            final Map<String, String> ambStrategyResolution = Collections.emptyMap();
            packStrategy(overlayIterable, strategyIterable, ambStrategyResolution, outputFile);
        }
    }

    /**
     * Packs Strategy definitions in a directory into a single CTree definition in a file
     *
     * @param inputDir   Directory that only includes .aterm files with the strategy definitions
     * @param outputFile Path of the outputFile
     * @throws IOException When there is a file system problem or no aterm files were found
     */
    public static void packBoilerplate(Path inputDir, @Nullable Path outputFile) throws IOException {
        if(outputFile == null) {
            outputFile = Paths.get(inputDir.getParent().toString(), "stratego.compiler.pack", "boilerplate.ctree");
        }
        try(Stream<Path> inputFiles = Files.list(inputDir).filter(relevantFiles(outputFile))) {
            Iterable<Path> iterable = inputFiles::iterator;
            packBoilerplate(iterable, outputFile);
        }
    }

    /**
     * Packs StrategoCore strategy definition in some paths into a single strategy definition in a file
     *
     * @param strategyPaths Paths that only include .aterm files with the strategy bodies
     * @param outputFile    Path of the outputFile
     * @throws IOException When there is a file system problem
     */
    public static void packStrategy(Iterable<Path> overlayPaths, Iterable<Path> strategyPaths,
        Map<String, String> ambStrategyResolution, Path outputFile) throws IOException {
        Files.createDirectories(outputFile.getParent());

        // We use the nio.Channel API here because it is supposed to be the fastest way
        // to append one file to another
        // This may need to be changed when using this tool inside Spoofax, dealing with
        // a virtual file system...
        openFile:
        try(final FileChannel outChannel = FileChannel
            .open(outputFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE)) {
            write(outChannel, SPEC_START);

            String next_line = "";
            for(Path inputFile : overlayPaths) {
                try(final FileChannel inChannel = FileChannel.open(inputFile, StandardOpenOption.READ)) {
                    if(inChannel.size() == 0)
                        continue;
                    write(outChannel, next_line);
                    transferContents(inChannel, outChannel);
                }
                next_line = "\n,";
            }

            write(outChannel, SPEC_MIDDLE);

            next_line = "";
            for(Path inputFile : strategyPaths) {
                try(final FileChannel inChannel = FileChannel.open(inputFile, StandardOpenOption.READ)) {
                    if(inChannel.size() == 0)
                        continue;
                    write(outChannel, next_line);
                    transferContents(inChannel, outChannel);
                }
                next_line = "\n,";
            }
            if(Objects.equals(next_line, "")) {
                // When there are no relevant files, close the file
                break openFile;
            }

            write(outChannel, SPEC_END);
            if(!ambStrategyResolution.isEmpty()) {
                next_line = "";
                write(outChannel, ANNO_START);
                for(Map.Entry<String, String> entry : ambStrategyResolution.entrySet()) {
                    write(outChannel, next_line);

                    write(outChannel, "(\"");
                    write(outChannel, entry.getKey());
                    write(outChannel, "\",\"");
                    write(outChannel, entry.getValue());
                    write(outChannel, "\")");

                    next_line = "\n,";
                }
                write(outChannel, ANNO_END);
            }
            return; // success
        }
        // Break to here when there are no relevant files; clean up incomplete file
        Files.delete(outputFile);
        throw new IOException("Input directory does not contain aterm files");
    }

    /**
     * Packs StrategoCore strategy definition in some paths into a single strategy definition in a file
     *
     * @param strategyPaths Paths that only include .aterm files with the strategy bodies
     * @param outputFile    Path of the outputFile
     * @throws IOException When there is a file system problem
     */
    public static void packStrategy(Iterable<File> overlayPaths, Iterable<File> strategyPaths,
        Map<String, String> ambStrategyResolution, FileObject outputFile) throws IOException {
        if(!outputFile.exists()) {
            outputFile.createFile();
        }

        openFile:
        try(final OutputStream outputStream = outputFile.getContent().getOutputStream()) {
            try(final OutputStream fos = new BufferedOutputStream(outputStream)) {
                byte[] buf = new byte[BUFFER];

                fos.write(SPEC_START.getBytes());


                String next_line = "";
                for(File inputFile : overlayPaths) {
                    try(final InputStream inputStream = new FileInputStream(inputFile)) {
                        if(inputFile.length() == 0)
                            continue;
                        fos.write(next_line.getBytes());
                        try(final InputStream fis = new BufferedInputStream(inputStream)) {
                            int i;
                            while((i = fis.read(buf)) != -1) {
                                fos.write(buf, 0, i);
                            }
                        }
                    }
                    next_line = "\n,";
                }

                fos.write(SPEC_MIDDLE.getBytes());

                next_line = "";
                for(File inputFile : strategyPaths) {
                    try(final InputStream inputStream = new FileInputStream(inputFile)) {
                        if(inputFile.length() == 0)
                            continue;
                        fos.write(next_line.getBytes());

                        try(final InputStream fis = new BufferedInputStream(inputStream)) {
                            int i;
                            while((i = fis.read(buf)) != -1) {
                                fos.write(buf, 0, i);
                            }
                        }
                    }
                    next_line = "\n,";
                }
                if(Objects.equals(next_line, "")) {
                    // When there are no relevant files, close the file
                    break openFile;
                }

                fos.write(SPEC_END.getBytes());
                if(!ambStrategyResolution.isEmpty()) {
                    next_line = "";
                    fos.write(ANNO_START.getBytes());
                    for(Map.Entry<String, String> entry : ambStrategyResolution.entrySet()) {
                        fos.write(next_line.getBytes());

                        fos.write("(\"".getBytes());
                        fos.write(entry.getKey().getBytes());
                        fos.write("\",\"".getBytes());
                        fos.write(entry.getValue().getBytes());
                        fos.write("\")".getBytes());

                        next_line = "\n,";
                    }
                    fos.write(ANNO_END.getBytes());
                }
                return; // success
            }
        }
        // Break to here when there are no relevant files; clean up incomplete file
        outputFile.delete();
        throw new IOException("Input directory does not contain aterm files");
    }

    /**
     * Packs Strategy definitions of some paths into a single CTree definition in a file
     *
     * @param paths      Paths that only include .aterm files with the strategy definitions
     * @param outputFile Path of the outputFile
     * @throws IOException When there is a file system problem or no aterm files were found
     */
    public static void packBoilerplate(Iterable<Path> paths, Path outputFile) throws IOException {
        Files.createDirectories(outputFile.getParent());

        try(final FileChannel outChannel = FileChannel
            .open(outputFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE)) {
            write(outChannel, SPEC_START);
            write(outChannel, SPEC_MIDDLE);

            int num_defs = 0;
            String next_line = "";
            for(Path inputFile : paths) {
                num_defs++;
                try(final FileChannel inChannel = FileChannel.open(inputFile, StandardOpenOption.READ)) {
                    if(inChannel.size() == 0)
                        continue;
                    write(outChannel, next_line);
                    transferContents(inChannel, outChannel);
                }
                next_line = "\n,";
            }
            if(num_defs == 0) {
                throw new IOException("Input directory does not contain aterm files");
            }
            write(outChannel, SPEC_END);
        }
    }

    /**
     * Packs Strategy definitions of some paths into a single CTree definition in a file
     *
     * @param paths      Paths that only include .aterm files with the strategy definitions
     * @param outputFile Path of the outputFile
     * @throws IOException When there is a file system problem or no aterm files were found
     */
    public static void packBoilerplate(Iterable<File> paths, FileObject outputFile) throws IOException {
        if(!outputFile.exists()) {
            outputFile.createFile();
        }

        try(final OutputStream outputStream = outputFile.getContent().getOutputStream()) {
            try(final OutputStream fos = new BufferedOutputStream(outputStream)) {
                byte[] buf = new byte[BUFFER];

                fos.write(SPEC_START.getBytes());
                fos.write(SPEC_MIDDLE.getBytes());

                int num_defs = 0;
                String next_line = "";
                for(File inputFile : paths) {
                    num_defs++;
                    try(final InputStream inputStream = new FileInputStream(inputFile)) {
                        if(inputFile.length() == 0)
                            continue;
                        fos.write(next_line.getBytes());
                        try(final InputStream fis = new BufferedInputStream(inputStream)) {
                            int i;
                            while((i = fis.read(buf)) != -1) {
                                fos.write(buf, 0, i);
                            }
                        }
                    }
                    next_line = "\n,";
                }
                if(num_defs == 0) {
                    throw new IOException("Input directory does not contain aterm files");
                }

                fos.write(SPEC_END.getBytes());
            }
        }
    }

    private static Predicate<? super Path> relevantFiles(final Path outputFile) {
        return f -> {
            try {
                return Files.exists(f) && f.getFileName().toString().endsWith(".aterm") && !Files
                    .isSameFile(f, outputFile) && Files.size(f) != 0;
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


    public static IStrategoTerm packBoilerplate(ITermFactory f, Collection<IStrategoAppl> strategyContributions) {
        return f.makeAppl("Specification",
            f.makeList(f.makeAppl("Signature", f.makeList(f.makeAppl("Constructors", f.makeList()))),
                f.makeAppl("Strategies", f.makeList(strategyContributions.toArray(new IStrategoTerm[0])))));
    }

    public static IStrategoTerm packStrategy(ITermFactory f, Collection<IStrategoAppl> overlayContributions,
        Collection<IStrategoAppl> strategyContributions, Map<String, String> ambStrategyResolution) {
        final IStrategoTerm[] annos = new IStrategoTerm[ambStrategyResolution.size()];
        int i = 0;
        for(Map.Entry<String, String> entry : ambStrategyResolution.entrySet()) {
            annos[i] = f.makeTuple(f.makeString(entry.getKey()), f.makeString(entry.getValue()));
            i++;
        }
        final IStrategoAppl term;
        if(overlayContributions.isEmpty()) {
            term = f.makeAppl("Specification",
                f.makeList(f.makeAppl("Signature", f.makeList(f.makeAppl("Constructors", f.makeList()))),
                    f.makeAppl("Strategies", f.makeList(strategyContributions.toArray(new IStrategoTerm[0])))));
        } else {
            term = f.makeAppl("Specification",
                f.makeList(f.makeAppl("Signature", f.makeList(f.makeAppl("Constructors", f.makeList()))),
                    f.makeAppl("Overlays", f.makeList(overlayContributions.toArray(new IStrategoTerm[0]))),
                    f.makeAppl("Strategies", f.makeList(strategyContributions.toArray(new IStrategoTerm[0])))));
        }
        if(ambStrategyResolution.isEmpty()) {
            return term;
        } else {
            return f.annotateTerm(term, f.makeList(annos));
        }
    }
}
