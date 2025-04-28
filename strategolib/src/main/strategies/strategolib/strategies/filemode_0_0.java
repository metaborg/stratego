package strategolib.strategies;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;

import org.spoofax.interpreter.library.IOAgent;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class filemode_0_0 extends Strategy {
    public static final filemode_0_0 instance = new filemode_0_0();

    /**
     * Stratego 2 type: {@code filemode :: (|) string -> int}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        return callStatic(context, current);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current) {
        final IOAgent ioAgent = context.getIOAgent();
        final File file = ioAgent.openFile(TermUtils.toJavaString(current));
        try {
            final PosixFileAttributes attrs = Files.getFileAttributeView(file.toPath(), PosixFileAttributeView.class)
                .readAttributes();
            if(attrs == null) {
                return null;
            }
            int result = 0;
            if(attrs.isRegularFile()) {
                result |= S_IFREG;
            }
            if(attrs.isDirectory()) {
                result |= S_IFDIR;
            }
            if(attrs.isSymbolicLink()) {
                result |= S_IFLINK;
            }
            if(attrs.isOther()) {
                result |= S_IFCHR | S_IFBLK | S_IFFIFO | S_IFSOCK;
            }
            for(PosixFilePermission perm : attrs.permissions()) {
                switch(perm) {
                    case GROUP_EXECUTE:
                        result |= S_IXGRP;
                        break;
                    case GROUP_READ:
                        result |= S_IRGRP;
                        break;
                    case GROUP_WRITE:
                        result |= S_IWGRP;
                        break;
                    case OTHERS_EXECUTE:
                        result |= S_IXOTH;
                        break;
                    case OTHERS_READ:
                        result |= S_IROTH;
                        break;
                    case OTHERS_WRITE:
                        result |= S_IWOTH;
                        break;
                    case OWNER_EXECUTE:
                        result |= S_IXUSR;
                        break;
                    case OWNER_READ:
                        result |= S_IRUSR;
                        break;
                    case OWNER_WRITE:
                        result |= S_IWUSR;
                        break;
                    default:
                        break;
                }
            }
            return context.getFactory().makeInt(result);
        } catch(IOException e) {
            return null;
        }
    }

    public static final int S_IRWXU  = 0b0000000111000000;    /* RWX mask for owner */
    public static final int S_IRUSR  = 0b0000000100000000;    /* R for owner */
    public static final int S_IWUSR  = 0b0000000010000000;    /* W for owner */
    public static final int S_IXUSR  = 0b0000000001000000;    /* X for owner */

    public static final int S_IRWXG  = 0b0000000000111000;    /* RWX mask for group */
    public static final int S_IRGRP  = 0b0000000000100000;    /* R for group */
    public static final int S_IWGRP  = 0b0000000000010000;    /* W for group */
    public static final int S_IXGRP  = 0b0000000000001000;    /* X for group */

    public static final int S_IRWXO  = 0b0000000000000111;    /* RWX mask for other */
    public static final int S_IROTH  = 0b0000000000000100;    /* R for other */
    public static final int S_IWOTH  = 0b0000000000000010;    /* W for other */
    public static final int S_IXOTH  = 0b0000000000000001;    /* X for other */

    public static final int S_IFREG  = 0b1000000000000000;    /* Regular file */
    public static final int S_IFDIR  = 0b0100000000000000;    /* Directory file */
    public static final int S_IFCHR  = 0b0010000000000000;    /* Character special file */
    public static final int S_IFBLK  = 0b0001000000000000;    /* Block special file */
    public static final int S_IFFIFO = 0b0000100000000000;    /* FIFO special file */
    public static final int S_IFLINK = 0b0000010000000000;    /* Symbolic link file */
    public static final int S_IFSOCK = 0b0000001000000000;    /* Socket file */
}
