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

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;
        if(!super.equals(o))
            return false;

        FailedToGetModuleAst that = (FailedToGetModuleAst) o;

        if(!moduleIdentifier.equals(that.moduleIdentifier))
            return false;
        return exception.equals(that.exception);
    }

    @Override public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + moduleIdentifier.hashCode();
        result = 31 * result + exception.hashCode();
        return result;
    }
}
