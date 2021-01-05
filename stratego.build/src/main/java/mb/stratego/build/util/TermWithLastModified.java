package mb.stratego.build.util;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.StrategoWrapped;

public class TermWithLastModified extends StrategoWrapped {
    public final long lastModified;

    private TermWithLastModified(IStrategoTerm wrapped, long lastModified) {
        super(wrapped);
        this.lastModified = lastModified;
    }

    public static TermWithLastModified fromTimestamp(IStrategoTerm wrapped, long lastModified) {
        if(wrapped instanceof TermWithLastModified) {
            wrapped = ((TermWithLastModified) wrapped).getWrapped();
        }
        return new TermWithLastModified(wrapped, lastModified);
    }

    public static TermWithLastModified fromParent(IStrategoTerm wrapped, TermWithLastModified parent) {
        if(wrapped instanceof TermWithLastModified) {
            wrapped = ((TermWithLastModified) wrapped).getWrapped();
        }
        return new TermWithLastModified(wrapped, parent.lastModified);
    }

    public boolean doSlowMatch(IStrategoTerm second) {
        if(!super.doSlowMatch(second))
            return false;

        if(!(second instanceof TermWithLastModified)) {
            return false;
        }

        final TermWithLastModified that = (TermWithLastModified) second;
        return this.lastModified == that.lastModified;
    }

    @Override public int hashFunction() {
        int result = super.hashCode();
        result = 31 * result + (int) (lastModified ^ lastModified >>> 32);
        return result;
    }
}
