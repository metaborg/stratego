package strategolib.strategies;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Optional;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class times_0_0 extends Strategy {
    public static final times_0_0 instance = new times_0_0();

    public static final int TICKS_PER_SECOND = 100;

    /**
     * Stratego 2 type: {@code times :: (|) ? -> int * int * int * int}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        return callStatic(context, current);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current) {
        final ITermFactory factory = context.getFactory();

        int utime = (int) (getUserTime().orElse(0L) * TICKS_PER_SECOND / 1_000_000_000);
        int stime = (int) (getSystemTime().orElse(0L) * TICKS_PER_SECOND / 1_000_000_000);

        IStrategoTerm utimeTerm = factory.makeInt(utime);
        IStrategoTerm stimeTerm = factory.makeInt(stime);
        IStrategoTerm ctimeTerm = factory.makeInt(0);

        return factory.makeTuple(utimeTerm, stimeTerm, ctimeTerm, ctimeTerm);
    }

    /**
     * Get the user time in nanoseconds.
     */
    public static Optional<Long> getUserTime() {
        final ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        return bean.isCurrentThreadCpuTimeSupported() ? Optional.of(bean.getCurrentThreadUserTime()) : Optional.empty();
    }

    /**
     * Get the system time in nanoseconds.
     */
    public static Optional<Long> getSystemTime() {
        final ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        return bean.isCurrentThreadCpuTimeSupported()
            ? Optional.of(bean.getCurrentThreadCpuTime() - bean.getCurrentThreadUserTime()) : Optional.empty();
    }
}
