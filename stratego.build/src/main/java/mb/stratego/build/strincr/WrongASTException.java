package mb.stratego.build.strincr;

import org.spoofax.interpreter.terms.IStrategoTerm;

public class WrongASTException extends Exception {
    public final IModuleImportService.ModuleIdentifier moduleIdentifier;
    public final IStrategoTerm ast;

    public WrongASTException(IModuleImportService.ModuleIdentifier moduleIdentifier,
        IStrategoTerm ast) {
        this.moduleIdentifier = moduleIdentifier;
        this.ast = ast;
    }
}
