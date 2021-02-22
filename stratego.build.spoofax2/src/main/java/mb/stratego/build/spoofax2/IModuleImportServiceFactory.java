package mb.stratego.build.spoofax2;

import java.util.Collection;

import javax.annotation.Nullable;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.pie.api.STask;
import mb.resource.hierarchical.ResourcePath;
import mb.stratego.build.strincr.IModuleImportService;
import mb.stratego.build.util.LastModified;

public interface IModuleImportServiceFactory {
    IModuleImportService create(Collection<STask<?>> strFileGeneratingTasks,
        Collection<ResourcePath> includeDirs);

    IModuleImportService create(Collection<STask<?>> strFileGeneratingTasks,
        Collection<ResourcePath> includeDirs, @Nullable String moduleName,
        @Nullable LastModified<IStrategoTerm> ast);
}
