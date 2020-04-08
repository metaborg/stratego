package mb.stratego.build.strincr;

import java.nio.file.Path;

import javax.inject.Inject;

import mb.pie.api.ExecContext;
import mb.pie.api.STask;
import mb.pie.api.TaskDef;
import mb.stratego.build.strincr.Frontends.Output;

public class StrIncrAnalysis implements TaskDef<Frontends.Input, Frontends.Output> {
    public static final String id = StrIncrAnalysis.class.getCanonicalName();
    private final Frontends frontends;

    @Inject
    public StrIncrAnalysis(Frontends frontends) {
        this.frontends = frontends;
    }

    @Override public Output exec(ExecContext execContext, Frontends.Input input) throws Exception {
        /*
         * Note that we require the sdf tasks here to force it to generated needed str files. We then discover those in
         * this method with a directory search, and start a front-end task for each. Every front-end task also depends
         * on the sdf tasks so there is no hidden dep. To make sure that front-end tasks only run when their input
         * _files_ change, we need the front-end to depend on the sdf tasks with a simple stamper that allows the
         * execution of the sdf task to be ignored.
         */
        for(final STask t : input.originTasks) {
            execContext.require(t);
        }

        final Path projectLocationPath = input.projectLocation.toPath().toAbsolutePath().normalize();

        return frontends.collectInformation(execContext, input, projectLocationPath);
    }

    @Override public String getId() {
        return id;
    }
}