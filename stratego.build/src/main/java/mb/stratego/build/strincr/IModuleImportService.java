package mb.stratego.build.strincr;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;

import javax.annotation.Nullable;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.STask;
import mb.resource.hierarchical.ResourcePath;
import mb.stratego.build.util.LastModified;

/**
 * Service that provides unique identification of Stratego modules. Modules of the same name in
 * different locations that are both accessible (due to multiple non-overlapping include
 * directories) are each given unique identification based on where they are found to distinguish
 * them.
 */
public interface IModuleImportService {
    /**
     * Unique identifier for a module. This is used by this service for retrieving its AST.
     * An import string is not unique as it may resolve to a file through multiple import paths
     * from include directories.
     */
    interface ModuleIdentifier extends Serializable {
        boolean isLibrary();

        boolean equals(@Nullable Object other);

        int hashCode();

        /**
         * @return the module string this id was created from
         */
        String moduleString();
    }

    /**
     * Common superclass for resolving imports
     */
    interface ImportResolution extends Serializable {
        int hashCode();
        boolean equals(@Nullable Object obj);
        String toString();
    }

    /**
     * A resolved import, containing the unique ids of modules that match the import.
     */
    class ResolvedImport implements ImportResolution {
        public final Collection<? extends ModuleIdentifier> modules;

        public ResolvedImport(Collection<? extends ModuleIdentifier> modules) {
            this.modules = modules;
        }

        @Override public boolean equals(@Nullable Object o) {
            if(this == o)
                return true;
            if(o == null || getClass() != o.getClass())
                return false;

            ResolvedImport that = (ResolvedImport) o;

            return modules.equals(that.modules);
        }

        @Override public int hashCode() {
            return modules.hashCode();
        }

        @Override public String toString() {
            return "ResolvedImport(" + modules + ')';
        }
    }

    /**
     * Singleton object that represents that the import did not resolve.
     */
    class UnresolvedImport implements ImportResolution {
        public static final UnresolvedImport INSTANCE = new UnresolvedImport();

        private UnresolvedImport() {
        }

        @Override public int hashCode() {
            return 0;
        }

        @Override public boolean equals(@Nullable Object other) {
            return this == other || other != null && this.getClass() == other.getClass();
        }

        @Override public String toString() {
            return "UnresolvedImport";
        }

        private Object readResolve() {
            return INSTANCE;
        }
    }

    /**
     * @param anImport term of the module import from the Stratego AST
     * @return The resolution object that is either an unresolved import or a collection of unique
     * ids of modules
     * @throws IOException on IO exceptions while search the available paths for the module
     */
    ImportResolution resolveImport(ExecContext context, IStrategoTerm anImport,
        Collection<STask<?>> strFileGeneratingTasks, Collection<? extends ResourcePath> includeDirs,
        Collection<? extends IModuleImportService.ModuleIdentifier> linkedLibraries)
        throws IOException, ExecException;

    /**
     * @param moduleIdentifier The unique identifier previously created by this service during
     *                         import resolution
     * @return The AST of the Stratego module
     * @throws IOException on IO exceptions during access to the file in which the module resides
     */
    LastModified<IStrategoTerm> getModuleAst(ExecContext context, ModuleIdentifier moduleIdentifier,
        Collection<STask<?>> strFileGeneratingTasks)
        throws Exception;

    boolean equals(Object o);

    int hashCode();
}
