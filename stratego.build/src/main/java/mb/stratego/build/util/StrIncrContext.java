package mb.stratego.build.util;

import javax.inject.Inject;

import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.strategoxt.lang.Context;
import org.strategoxt.strj.strj;

public class StrIncrContext extends Context {
    protected LocallyUniqueStringTermFactory factory;

    @Inject public StrIncrContext(ITermFactoryService termFactoryService) {
        super(new LocallyUniqueStringTermFactory(termFactoryService.getGeneric()));
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
