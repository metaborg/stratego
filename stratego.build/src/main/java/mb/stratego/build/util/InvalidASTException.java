package mb.stratego.build.util;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.stratego.build.strincr.IModuleImportService;

public class InvalidASTException extends RuntimeException {
    public final IModuleImportService.ModuleIdentifier moduleIdentifier;
    public final IStrategoTerm ast;

    public InvalidASTException(IModuleImportService.ModuleIdentifier moduleIdentifier,
        IStrategoTerm ast) {
        this.moduleIdentifier = moduleIdentifier;
        this.ast = ast;
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        InvalidASTException that = (InvalidASTException) o;

        if(!moduleIdentifier.equals(that.moduleIdentifier))
            return false;
        return ast.equals(that.ast);
    }

    @Override public int hashCode() {
        int result = moduleIdentifier.hashCode();
        result = 31 * result + ast.hashCode();
        return result;
    }

    @Override public String getMessage() {
        return "Internal compiler bug detected:\nInvalid AST shape while checking/compiling Stratego module '" + moduleIdentifier + "': " + ast;
    }
}
