package strategolib.strategies;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class internal_epoch2localtime_0_0 extends Strategy {
    public static final internal_epoch2localtime_0_0 instance = new internal_epoch2localtime_0_0();

    /**
     * Stratego 2 type: {@code internal-epoch2localtime :: (|) int -> int * int * int * int * int * int * int * int}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        return callStatic(context, current);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current) {
        final int secondsSinceEpoch = TermUtils.toJavaInt(current);
        final ZonedDateTime localDateTime = Instant.ofEpochSecond(secondsSinceEpoch).atZone(ZoneId.systemDefault());
        return internal_epoch2utc_0_0.timeTuple(context.getFactory(), localDateTime);
    }
}
