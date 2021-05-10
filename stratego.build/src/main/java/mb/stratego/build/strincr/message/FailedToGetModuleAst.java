package mb.stratego.build.strincr.message;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.stratego.build.strincr.IModuleImportService;

public class FailedToGetModuleAst extends Message {
    private final IModuleImportService.ModuleIdentifier moduleIdentifier;

    public FailedToGetModuleAst(IStrategoTerm module, IModuleImportService.ModuleIdentifier moduleIdentifier) {
        super(module, MessageSeverity.ERROR, 0L);
        this.moduleIdentifier = moduleIdentifier;
    }

    @Override public String getMessage() {
        return "Cannot get module AST for module " + moduleIdentifier.moduleString()
            + ". Does it exist and contain no parse errors?";
    }
}
