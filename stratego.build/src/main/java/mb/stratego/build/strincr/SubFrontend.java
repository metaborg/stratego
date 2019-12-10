package mb.stratego.build.strincr;

import java.io.File;
import java.io.Serializable;

import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.spoofax.core.stratego.ResourceAgent;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Strategy;
import org.strategoxt.strc.compile_top_level_def_0_0;
import org.strategoxt.strc.split_module_0_0;

import com.google.inject.Inject;

import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.TaskDef;
import mb.stratego.build.termvisitors.TermSize;
import mb.stratego.build.util.ResourceAgentTracker;
import mb.stratego.build.util.StrIncrContext;
import mb.stratego.build.util.StrategoExecutor;

public class SubFrontend implements TaskDef<SubFrontend.Input, SubFrontend.Output> {
    public static final String id = SubFrontend.class.getCanonicalName();

    public static final class Input implements Serializable {
        final File projectLocation;
        final String inputFileString;
        final String cifiedName;
        final InputType inputType;
        final IStrategoTerm ast;

        public Input(File projectLocation, String inputFileString, String cifiedName, InputType inputType,
            IStrategoTerm ast) {
            this.projectLocation = projectLocation;
            this.inputFileString = inputFileString;
            this.cifiedName = cifiedName;
            this.inputType = inputType;
            this.ast = ast;
        }

        @Override public String toString() {
            return "StrIncrSubFront$Input(" + inputType.name() + ", " + cifiedName + ')';
        }


        @Override public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((ast == null) ? 0 : ast.hashCode());
            result = prime * result + ((cifiedName == null) ? 0 : cifiedName.hashCode());
            result = prime * result + ((inputFileString == null) ? 0 : inputFileString.hashCode());
            result = prime * result + ((inputType == null) ? 0 : inputType.hashCode());
            result = prime * result + ((projectLocation == null) ? 0 : projectLocation.hashCode());
            return result;
        }


        @Override public boolean equals(Object obj) {
            if(this == obj)
                return true;
            if(obj == null)
                return false;
            if(getClass() != obj.getClass())
                return false;
            Input other = (Input) obj;
            if(ast == null) {
                if(other.ast != null)
                    return false;
            } else if(!ast.equals(other.ast))
                return false;
            if(cifiedName == null) {
                if(other.cifiedName != null)
                    return false;
            } else if(!cifiedName.equals(other.cifiedName))
                return false;
            if(inputFileString == null) {
                if(other.inputFileString != null)
                    return false;
            } else if(!inputFileString.equals(other.inputFileString))
                return false;
            if(inputType != other.inputType)
                return false;
            if(projectLocation == null) {
                if(other.projectLocation != null)
                    return false;
            } else if(!projectLocation.equals(other.projectLocation))
                return false;
            return true;
        }

    }

    public static final class Output implements Serializable {
        final IStrategoTerm result;

        public Output(IStrategoTerm result) {
            this.result = result;
        }

        @Override public String toString() {
            return "StrIncrSubFront$Output";
        }

        @Override public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((this.result == null) ? 0 : this.result.hashCode());
            return result;
        }

        @Override public boolean equals(Object obj) {
            if(this == obj)
                return true;
            if(obj == null)
                return false;
            if(getClass() != obj.getClass())
                return false;
            Output other = (Output) obj;
            if(result == null) {
                if(other.result != null)
                    return false;
            } else if(!result.equals(other.result))
                return false;
            return true;
        }
    }

    public enum InputType {
        TopLevelDefinition(compile_top_level_def_0_0.instance),
        Split(split_module_0_0.instance); // Split is for convenience, not because it *must* be cached
        final Strategy strategy;

        InputType(Strategy strategy) {
            this.strategy = strategy;
        }
    }

    private final IResourceService resourceService;
    private final StrIncrContext strContext;
    ILanguageImpl strategoLang;

    @Inject public SubFrontend(IResourceService resourceService, StrIncrContext strContext) {
        this.resourceService = resourceService;
        this.strContext = strContext;
    }


    @Override public String getId() {
        return id;
    }

    @Override public Serializable key(SubFrontend.Input input) {
        return input.inputFileString + ":" + input.cifiedName;
    }


    @Override public SubFrontend.Output exec(ExecContext context, SubFrontend.Input input) throws Exception {
        if(input.inputType == InputType.TopLevelDefinition) {
            BuildStats.tldSubFrontendCTreeSize.put(input.toString(), TermSize.computeTermSize(input.ast));
        }
        final StrategoExecutor.ExecutionResult result = Backend.runLocallyUniqueStringStrategy(context.logger(), true,
            newResourceTracker(new File(System.getProperty("user.dir")), true), input.inputType.strategy, input.ast,
            strContext);

        if(!result.success) {
            throw new ExecException("Call to strc frontend failed", result.exception);
        }

        return new Output(result.result);
    }

    private ResourceAgentTracker newResourceTracker(File baseFile, boolean silent, String... excludePatterns) {
        final FileObject base = resourceService.resolve(baseFile);
        final ResourceAgentTracker tracker;
        if(silent) {
            tracker = new ResourceAgentTracker(resourceService, base, new NullOutputStream(), new NullOutputStream());
        } else {
            tracker = new ResourceAgentTracker(resourceService, base, excludePatterns);
        }
        final ResourceAgent agent = tracker.agent();
        agent.setAbsoluteWorkingDir(base);
        agent.setAbsoluteDefinitionDir(base);
        return tracker;
    }

}
