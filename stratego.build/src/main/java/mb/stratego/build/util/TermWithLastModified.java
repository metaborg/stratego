package mb.stratego.build.util;

import org.spoofax.interpreter.terms.IStrategoTerm;

public class TermWithLastModified {
    public final IStrategoTerm term;
    public final long lastModified;

    private TermWithLastModified(IStrategoTerm term, long lastModified) {
        this.term = term;
        this.lastModified = lastModified;
    }

    public static TermWithLastModified fromTimestamp(IStrategoTerm wrapped, long lastModified) {
        if(wrapped instanceof TermWithLastModified) {
            wrapped = ((TermWithLastModified) wrapped).term;
        }
        return new TermWithLastModified(wrapped, lastModified);
    }

    public static TermWithLastModified fromParent(IStrategoTerm wrapped, TermWithLastModified parent) {
        if(wrapped instanceof TermWithLastModified) {
            wrapped = ((TermWithLastModified) wrapped).term;
        }
        return new TermWithLastModified(wrapped, parent.lastModified);
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        TermWithLastModified that = (TermWithLastModified) o;

        if(lastModified != that.lastModified)
            return false;
        return term.equals(that.term);
    }

    @Override public int hashCode() {
        int result = term.hashCode();
        result = 31 * result + (int) (lastModified ^ lastModified >>> 32);
        return result;
    }
}
