package mb.stratego.build.strincr;

import java.io.IOException;
import java.io.Serializable;

import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;

public final class Import implements Serializable {
    public enum ImportType {
        normal, wildcard, library
    }

    final Import.ImportType type;
    final String path;
    final IStrategoString pathTerm;

    Import(Import.ImportType type, IStrategoString path) {
        this.type = type;
        this.pathTerm = path;
        this.path = this.pathTerm.stringValue();
    }

    static Import normal(IStrategoString importString) {
        return new Import(ImportType.normal, importString);
    }

    static Import wildcard(IStrategoString importString) {
        return new Import(ImportType.wildcard, importString);
    }

    static Import library(IStrategoString libraryName) {
        return new Import(ImportType.library, Library.normalizeBuiltin(libraryName));
    }

    static Import fromTerm(IStrategoTerm importTerm) throws IOException {
        if(!TermUtils.isAppl(importTerm)) {
            throw new IOException("Import term was not a constructor: " + importTerm);
        }
        final IStrategoAppl appl = (IStrategoAppl) importTerm;
        switch(appl.getName()) {
            case "Import":
                IStrategoString importString = TermUtils.toStringAt(appl, 0);
                if(Library.Builtin.isBuiltinLibrary(importString.stringValue())) {
                    return library(importString);
                }
                return normal(importString);
            case "ImportWildcard":
                return wildcard(TermUtils.toStringAt(appl, 0));
            default:
                throw new IOException("Import term was not the expected Import or ImportWildcard: " + appl);
        }
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        Import anImport = (Import) o;

        // noinspection SimplifiableIfStatement
        if(type != anImport.type)
            return false;
        return path.equals(anImport.path);
    }

    @Override public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + path.hashCode();
        return result;
    }

    @Override public String toString() {
        return "Import(" + type + ", '" + path + '\'' + ')';
    }
}