package mb.stratego.build.strincr.message;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.stratego.build.strincr.IModuleImportService;

public class UnreachableModule extends Message {
    private final IModuleImportService.ModuleIdentifier moduleIdentifier;

    public UnreachableModule(IStrategoTerm moduleName, IModuleImportService.ModuleIdentifier moduleIdentifier, long lastModified) {
        super(moduleName, MessageSeverity.WARNING, lastModified);
        this.moduleIdentifier = moduleIdentifier;
    }

    @Override public String getMessage() {
        return "Unreachable module: '" + moduleIdentifier.moduleString() + "' is not reachable through imports from the project's main file.";
    }
}
