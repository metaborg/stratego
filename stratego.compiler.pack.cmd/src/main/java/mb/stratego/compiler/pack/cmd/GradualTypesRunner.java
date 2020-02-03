package mb.stratego.compiler.pack.cmd;

import mb.pie.api.ExecException;
import mb.pie.api.Logger;
import mb.stratego.build.strincr.Backend;
import mb.stratego.build.util.ResourceAgentTracker;
import mb.stratego.build.util.StrIncrContext;
import mb.stratego.build.util.StrategoExecutor;
import mb.stratego.gradual_types.Main;
import mb.stratego.gradual_types.insert_casts_top_level_0_0;

import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.spoofax.core.stratego.ResourceAgent;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.strj.strj;
import java.io.File;

public class GradualTypesRunner {

    private final StrIncrContext strContext;
    private final IResourceService resourceService;

    public GradualTypesRunner(StrIncrContext strContext, IResourceService resourceService) {
        this.strContext = strContext;
        this.resourceService = resourceService;
    }

    public IStrategoTerm exec(Logger logger, IStrategoTerm tuple) throws Exception {
        Main.init(strContext);
        final StrategoExecutor.ExecutionResult result = Backend.runLocallyUniqueStringStrategy(logger, true,
            newResourceTracker(new File(System.getProperty("user.dir")), false), insert_casts_top_level_0_0.instance, tuple,
            strContext);

        if(!result.success) {
            throw new ExecException("Call to gradual type checker failed on " + result.result + ": \n" + result.strategoTrace, result.exception);
        }

        return result.result;
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
