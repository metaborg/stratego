package mb.stratego.build.util;

import org.spoofax.jsglr.client.imploder.ImploderOriginTermFactory;
import org.spoofax.terms.TermFactory;
import org.strategoxt.lang.Context;
import org.strategoxt.strj.strj;

public class StrIncrContext extends Context {
    protected LocallyUniqueStringTermFactory factory;

    public StrIncrContext() {
        super(new LocallyUniqueStringTermFactory(new ImploderOriginTermFactory(new TermFactory())));
        factory = (LocallyUniqueStringTermFactory) super.getFactory();
        strj.init(this);
    }

    /**
     * Clears the usedStrings in the factory, so SSL_new and SSL_newname start over.
     *
     * @param context
     * @throws ClassCastException if given a context with a term factory that isn't of this class
     */
    public void resetUsedStringsInFactory() {
        factory.clearUsedStrings();
    }
}
