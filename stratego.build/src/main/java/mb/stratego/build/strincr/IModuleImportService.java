package mb.stratego.build.strincr;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.stratego.build.util.TermWithLastModified;

public interface IModuleImportService extends Serializable {
    /**
     * Unique identifier for a module. This is used by this service for retrieving its AST.
     * An import string is not unique as it may resolve to a file through multiple import paths
     *     where the compiler is instructed to look.
     */
    interface ModuleIdentifier {
        boolean isLibrary();
        boolean equals(Object other);
        int hashCode();

        /**
         * @return the module string this id was created from
         */
        String moduleString();
    }

    /**
     * Common superclass for resolving imports
     */
    interface ImportResolution {}

    /**
     * A resolved import, containing the unique ids of modules that match the import.
     */
    class ResolvedImport implements ImportResolution {
        public final Collection<ModuleIdentifier> modules;

        public ResolvedImport(Collection<ModuleIdentifier> modules) {
            this.modules = modules;
        }
    }

    /**
     * Singleton object that represents that the import did not resolve.
     */
    class UnresolvedImport implements ImportResolution {
        private static UnresolvedImport INSTANCE;

        private UnresolvedImport() {}

        public final UnresolvedImport instance() {
            if(INSTANCE == null) {
                INSTANCE = new UnresolvedImport();
            }
            return INSTANCE;
        }
    }

    /**
     *
     * @param anImport term of the module import from the Stratego AST
     * @return The resolution object that is either an unresolved import or a collection of unique
     *         ids of modules
     * @throws IOException on IO exceptions while search the available paths for the module
     */
    ImportResolution resolveImport(IStrategoTerm anImport) throws IOException;

    /**
     *
     * @param moduleIdentifier The unique identifier previously created by this service during
     *                         import resolution
     * @return The AST of the Stratego module
     * @throws IOException on IO exceptions during access to the file in which the module resides
     */
    TermWithLastModified getModuleAst(ModuleIdentifier moduleIdentifier) throws IOException;


}
