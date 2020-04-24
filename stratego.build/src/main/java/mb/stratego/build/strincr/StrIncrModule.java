package mb.stratego.build.strincr;

import com.google.inject.Singleton;

import mb.pie.taskdefs.guice.TaskDefsModule;
import mb.stratego.build.util.StrIncrContext;

public class StrIncrModule extends TaskDefsModule {
    @Override public void bindTaskDefs() {
        bindTaskDef(StrIncrAnalysis.class, StrIncrAnalysis.id);
        bindTaskDef(StrIncr.class, StrIncr.id);
        bindTaskDef(Frontend.class, Frontend.id);
        bindTaskDef(SubFrontend.class, SubFrontend.id);
        bindTaskDef(InsertCasts.class, InsertCasts.id);
        bindTaskDef(LibFrontend.class, LibFrontend.id);
        bindTaskDef(Backend.class, Backend.id);

        // bind special strategoxt context object used in all Tasks
        bind(StrIncrContext.class).in(Singleton.class);
        bind(Analysis.class).in(Singleton.class);
    }
}
