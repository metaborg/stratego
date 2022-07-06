package mb.stratego.build.strincr.message;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;

public class UnresolvedImport extends Message {
    public final String moduleName;

    public UnresolvedImport(IStrategoTerm importTerm, long lastModified) {
        super(importTerm, MessageSeverity.ERROR, lastModified);
        this.moduleName = TermUtils.toJavaStringAt(importTerm,0);
    }

    @Override public String getMessage() {
        return "Cannot find module for import '" + moduleName + "'";
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;
        if(!super.equals(o))
            return false;

        UnresolvedImport that = (UnresolvedImport) o;

        return moduleName.equals(that.moduleName);
    }

    @Override public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + moduleName.hashCode();
        return result;
    }
}
