package mb.stratego.build.util;

import org.strategoxt.lang.Context;
import org.strategoxt.strj.strj;

public class StrIncrContext extends Context {
    protected LocallyUniqueStringTermFactory factory;

    public StrIncrContext() {
        super(new LocallyUniqueStringTermFactory());
        factory = (LocallyUniqueStringTermFactory) super.getFactory();
        strj.init(this);
    }

    /**
     * Clears the usedStrings in the factory, so SSL_new and SSL_newname start over.
     */
    public void resetUsedStringsInFactory() {
        factory.clearUsedStrings();
    }
}
