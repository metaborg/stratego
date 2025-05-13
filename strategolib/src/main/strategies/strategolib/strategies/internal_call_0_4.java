package strategolib.strategies;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.IOAgent;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.StrategoException;
import org.strategoxt.lang.Strategy;
import org.strategoxt.lang.compat.NativeCallHelper;

public class internal_call_0_4 extends Strategy {
    public static final internal_call_0_4 instance = new internal_call_0_4();

    private static final NativeCallHelper caller = new NativeCallHelper();

    /**
     * SSL_EXT_call
     *
     * Stratego 2 type: {@code internal-call :: (|List(string), int, int, int) string -> int}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm programTerm, IStrategoTerm argsTerm,
        IStrategoTerm fdIn, IStrategoTerm fdOut, IStrategoTerm fdErr) {
        return callStatic(context, programTerm, argsTerm, fdIn, fdOut, fdErr);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm programTerm, IStrategoTerm argsTerm,
                                          IStrategoTerm fdIn, IStrategoTerm fdOut, IStrategoTerm fdErr) {
        try {
            final String program = TermUtils.toJavaString(programTerm);
            String[] environment = null;

            String[] commandArgs = toCommandArgs(program, TermUtils.toList(argsTerm));
            if(commandArgs == null) {
                return null;
            }

            // I/O setup
            IOAgent io = context.getIOAgent();
            File dir = io.openFile(io.getWorkingDir());
            // TODO: stdin?
            Writer stdout = io.getWriter(TermUtils.toJavaInt(fdOut));
            Writer stderr = io.getWriter(TermUtils.toJavaInt(fdErr));

            // Invocation
            int returnCode = caller.call(commandArgs, environment, dir, stdout, stderr);
            return context.getFactory().makeInt(returnCode);
        } catch(InterruptedException e) {
            throw new StrategoException("Exception in execution of primitive 'SSL_EXT_CALL'",
                new InterpreterException("SSL_EXT_CALL system call interrupted", e));
        } catch(IOException e) {
            return null;
        } catch(IllegalArgumentException e) {
            return null;
        }
    }

    public static String[] toCommandArgs(String program, IStrategoList args) throws IllegalArgumentException {
        List<String> results = new ArrayList<String>(1 + args.size());
        results.add(handleSpacesInPath(addExecutableExtension(program)));

        for(IStrategoTerm arg : args) {
            results.add(handleSpacesInPath(TermUtils.toJavaString(arg)));
        }

        return results.toArray(new String[0]);
    }

    private static String handleSpacesInPath(String potentialPath) {
        if(potentialPath.indexOf(' ') != -1 && isWindows() && !potentialPath.startsWith("\"")) {
            return "\"" + potentialPath + "\"";
        } else {
            return potentialPath;
        }
    }

    private static String addExecutableExtension(String command) {
        if(!new File(command).exists() && isWindows()) {
            if(new File(command + ".exe").exists()) {
                return command + ".exe";
            }
        }
        return command;
    }

    private static boolean isWindows() {
        // Java only publishes this as a string
        return System.getProperty("os.name").toLowerCase().indexOf("win") != -1;
    }
}
