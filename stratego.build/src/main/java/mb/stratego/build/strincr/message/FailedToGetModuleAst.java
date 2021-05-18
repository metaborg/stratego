package mb.stratego.build.strincr.message;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.stratego.build.strincr.IModuleImportService;

public class FailedToGetModuleAst extends Message {
    private final IModuleImportService.ModuleIdentifier moduleIdentifier;
    private final Exception exception;

    public FailedToGetModuleAst(IStrategoTerm module, IModuleImportService.ModuleIdentifier moduleIdentifier, Exception exception) {
        super(module, MessageSeverity.ERROR, 0L);
        this.moduleIdentifier = moduleIdentifier;
        this.exception = exception;
    }

    @Override public String getMessage() {
        return "Cannot get module AST for module " + moduleIdentifier.moduleString()
            + "with exception:\n" + exception.toString();
    }
}
