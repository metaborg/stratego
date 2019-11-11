package mb.stratego.build;

import com.google.inject.Singleton;

import mb.pie.taskdefs.guice.TaskDefsModule;
import mb.stratego.build.util.StrIncrContext;

public class StrIncrModule extends TaskDefsModule {
    @Override public void bindTaskDefs() {
        bindTaskDef(StrIncr.class, StrIncr.id);
        bindTaskDef(StrIncrFront.class, StrIncrFront.id);
        bindTaskDef(StrIncrSubFront.class, StrIncrSubFront.id);
        bindTaskDef(StrIncrFrontLib.class, StrIncrFrontLib.id);
        bindTaskDef(StrIncrBack.class, StrIncrBack.id);

        // bind special strategoxt context object used in all Tasks
        bind(StrIncrContext.class).in(Singleton.class);
    }
}
