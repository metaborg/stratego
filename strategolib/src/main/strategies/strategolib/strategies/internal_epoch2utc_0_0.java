package strategolib.strategies;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class internal_epoch2utc_0_0 extends Strategy {
    public static final internal_epoch2utc_0_0 instance = new internal_epoch2utc_0_0();

    /**
     * Stratego 2 type: {@code internal-epoch2utc :: (|) int -> int * int * int * int * int * int * int * int}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        return callStatic(context, current);
    }

    public static IStrategoTerm callStatic(Context context, IStrategoTerm current) {
        final int secondsSinceEpoch = TermUtils.toJavaInt(current);
        final ZonedDateTime localDateTime = Instant.ofEpochSecond(secondsSinceEpoch).atZone(ZoneId.of("UTC"));
        return timeTuple(context.getFactory(), localDateTime);
    }

    public static IStrategoTerm timeTuple(ITermFactory f, final ZonedDateTime localDateTime) {
        // @formatter:off
        return f.makeTuple(
            f.makeInt(localDateTime.getSecond()), 
            f.makeInt(localDateTime.getMinute()),
            f.makeInt(localDateTime.getHour()), 
            f.makeInt(localDateTime.getDayOfMonth()),
            f.makeInt(localDateTime.getMonthValue()), 
            f.makeInt(localDateTime.getYear()),
            f.makeInt(localDateTime.getDayOfWeek().ordinal()),
            f.makeInt(ZoneId.systemDefault().getRules().isDaylightSavings(localDateTime.toInstant()) ? 1 : 0));
        // @formatter:on
    }
}
