package strategolib.strategies;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

import org.spoofax.interpreter.library.IOAgent;
import org.spoofax.interpreter.library.ssl.SSLLibrary;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class internal_copy_file_0_1 extends Strategy {
    public static final internal_copy_file_0_1 instance = new internal_copy_file_0_1();

    /**
     * Stratego 2 type: {@code copy-file :: (|C99FileLoc) C99FileLoc -> C99FileLoc}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm oldName, IStrategoTerm newName) {
        return callStatic(context, oldName, newName);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm oldName, IStrategoTerm newName) {
        final SSLLibrary op = (SSLLibrary) context.getOperatorRegistry(SSLLibrary.REGISTRY_NAME);
        final IOAgent agent = op.getIOAgent();

        if(isSameFile(oldName, newName, agent)) {
            return oldName;
        }

        try {
            final InputStream in;
            final boolean closeIn;
            if(TermUtils.isString(oldName)) {
                in = agent.openInputStream(TermUtils.toJavaString(oldName));
                closeIn = true;
            } else if(TermUtils.isAppl(oldName, "stdin")) {
                in = agent.internalGetInputStream(IOAgent.CONST_STDIN);
                closeIn = false;
            } else {
                return null;
            }

            final OutputStream out;
            final boolean closeOut;
            if(TermUtils.isString(newName)) {
                out = agent.openFileOutputStream(TermUtils.toJavaString(newName));
                closeOut = true;
            } else if(TermUtils.isAppl(newName, "stdout")) {
                out = agent.internalGetOutputStream(IOAgent.CONST_STDOUT);
                closeOut = false;
            } else if(TermUtils.isAppl(newName, "stderr")) {
                out = agent.internalGetOutputStream(IOAgent.CONST_STDERR);
                closeOut = false;
            } else {
                return null;
            }

            try {
                if(in instanceof FileInputStream && out instanceof FileOutputStream) {
                    final FileChannel inChannel = ((FileInputStream) in).getChannel();
                    final FileChannel outChannel = ((FileOutputStream) out).getChannel();
                    inChannel.transferTo(0, inChannel.size(), outChannel);
                } else {
                    final byte[] buffer = new byte[8192];
                    int n = in.read(buffer);
                    while (n != -1) {
                        out.write(buffer, 0, n);
                        n = in.read(buffer);
                    }
                }
            } finally {
                if(closeOut) {
                    out.close();
                }
                if(closeIn) {
                    in.close();
                }
            }
        } catch(IOException e) {
            agent.printError(
                "SSL_copy: Could not copy file (" + e.getMessage() + "-" + "attempted to copy to " + newName);
            return null;
        }

        return oldName;
    }

    private static boolean isSameFile(IStrategoTerm oldName, IStrategoTerm newName, IOAgent agent) {
        if(TermUtils.isString(oldName) && TermUtils.isString(newName)) {
            File file1 = agent.openFile(TermUtils.toJavaString(oldName));
            File file2 = agent.openFile(TermUtils.toJavaString(newName));
            try {
                if(file1.exists() && file1.getCanonicalPath().equals(file2.getCanonicalPath()))
                    return true;
            } catch(IOException e) {
                // Ignore: files may not exist yet
            }
        }
        return false;
    }
}
