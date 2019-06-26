package org.metaborg.meta.lang.stratego.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class nano_time_real_0_0 extends Strategy {
    public static final nano_time_real_0_0 instance = new nano_time_real_0_0();
    
    @Override
    public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        return context.getFactory().makeReal(System.nanoTime());
    }
}
