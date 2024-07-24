package mb.stratego.build.util;


import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.lang.Context;
import org.strategoxt.strj.strj;

public class StrIncrContext extends Context {
    protected LocallyUniqueStringTermFactory factory;

    /**
     * Hide zero-argument constructor from superclass so Guice understands to use the @jakarta.inject.Inject constructor
     */
    @SuppressWarnings("unused")
    private StrIncrContext() {
        super();
    }

    @jakarta.inject.Inject public StrIncrContext(ITermFactory termFactory) {
        super(new LocallyUniqueStringTermFactory(termFactory));
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
