package mb.stratego.build.strincr;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Objects;

import jakarta.annotation.Nullable;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.STask;
import mb.pie.api.Supplier;
import mb.resource.hierarchical.ResourcePath;
import mb.stratego.build.strincr.data.StrategySignature;
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
        boolean legacyStratego();

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

    class ImportResolutionInfo implements Serializable {
        public final Collection<STask<?>> strFileGeneratingTasks;
        public final Collection<? extends ResourcePath> includeDirs;
        public final Collection<? extends IModuleImportService.ModuleIdentifier> linkedLibraries;
        public final Collection<Supplier<Stratego2LibInfo>> str2libraries;
        public final boolean supportRTree;
        public final boolean supportStr1;
        public final @Nullable ResourcePath resolveExternals;

        public ImportResolutionInfo(Collection<STask<?>> strFileGeneratingTasks,
            Collection<? extends ResourcePath> includeDirs,
            Collection<? extends ModuleIdentifier> linkedLibraries,
            Collection<Supplier<Stratego2LibInfo>> str2libraries, boolean supportRTree,
            boolean supportStr1, @Nullable ResourcePath resolveExternals) {
            this.strFileGeneratingTasks = strFileGeneratingTasks;
            this.includeDirs = includeDirs;
            this.linkedLibraries = linkedLibraries;
            this.str2libraries = str2libraries;
            this.supportRTree = supportRTree;
            this.supportStr1 = supportStr1;
            this.resolveExternals = resolveExternals;
        }

        @Override public boolean equals(Object o) {
            if(this == o)
                return true;
            if(o == null || getClass() != o.getClass())
                return false;

            ImportResolutionInfo that = (ImportResolutionInfo) o;

            if(supportRTree != that.supportRTree)
                return false;
            if(supportStr1 != that.supportStr1)
                return false;
            if(!strFileGeneratingTasks.equals(that.strFileGeneratingTasks))
                return false;
            if(!includeDirs.equals(that.includeDirs))
                return false;
            if(!linkedLibraries.equals(that.linkedLibraries))
                return false;
            if(!Objects.equals(resolveExternals, that.resolveExternals))
                return false;
            return str2libraries.equals(that.str2libraries);
        }

        @Override public int hashCode() {
            int result = strFileGeneratingTasks.hashCode();
            result = 31 * result + includeDirs.hashCode();
            result = 31 * result + linkedLibraries.hashCode();
            result = 31 * result + str2libraries.hashCode();
            result = 31 * result + Boolean.hashCode(this.supportRTree);
            result = 31 * result + Boolean.hashCode(this.supportStr1);
            result = 31 * result + Objects.hashCode(this.resolveExternals);
            return result;
        }

        @Override public String toString() {
            //@formatter:off
            return "ImportResolutionInfo@" + System.identityHashCode(this) + '{'
                + "strFileGeneratingTasks=" + strFileGeneratingTasks
                + ", includeDirs=" + includeDirs
                + ", linkedLibraries=" + linkedLibraries
                + ", str2libraries=" + str2libraries
                + ", supportRTree=" + supportRTree
                + ", supportStr1=" + supportStr1
                + ", resolveExternals=" + resolveExternals
                + '}';
            //@formatter:on
        }
    }

    /**
     * @param anImport term of the module import from the Stratego AST
     * @return The resolution object that is either an unresolved import or a collection of unique
     * ids of modules
     * @throws IOException on IO exceptions while search the available paths for the module
     */
    ImportResolution resolveImport(ExecContext context, IStrategoTerm anImport,
        ImportResolutionInfo importResolutionInfo) throws IOException, ExecException;

    /**
     * @param moduleIdentifier The unique identifier previously created by this service during
     *                         import resolution
     * @param importResolutionInfo
     * @return The AST of the Stratego module
     * @throws IOException on IO exceptions during access to the file in which the module resides
     */
    LastModified<IStrategoTerm> getModuleAst(ExecContext context, ModuleIdentifier moduleIdentifier,
        ImportResolutionInfo importResolutionInfo)
        throws Exception;

    @Nullable String fileName(ModuleIdentifier moduleIdentifier);

    boolean externalStrategyExists(ExecContext context, StrategySignature strategySignature, ImportResolutionInfo importResolutionInfo);

    boolean equals(Object o);

    int hashCode();
}
