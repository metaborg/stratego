package mb.stratego.build.spoofax2;

import java.io.IOException;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.pie.api.ExecContext;
import mb.stratego.build.strincr.IModuleImportService;
import mb.stratego.build.util.LastModified;

public class ModuleImportService implements IModuleImportService {
    @Override public ImportResolution resolveImport(ExecContext context, IStrategoTerm anImport)
        throws IOException {
        // TODO
        return null;
    }

    @Override public LastModified<IStrategoTerm> getModuleAst(ExecContext context,
        ModuleIdentifier moduleIdentifier) throws IOException {
        // TODO
        return null;
    }
}
