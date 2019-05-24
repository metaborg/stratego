package mb.stratego.build;

import mb.pie.taskdefs.guice.TaskDefsModule;

public class StrIncrModule extends TaskDefsModule {
    @Override public void bindTaskDefs() {
        bindTaskDef(StrIncr.class, StrIncr.id);
        bindTaskDef(StrIncrFront.class, StrIncrFront.id);
        bindTaskDef(StrIncrFront2.class, StrIncrFront2.id);
        bindTaskDef(StrIncrFrontLib.class, StrIncrFrontLib.id);
        bindTaskDef(StrIncrBack.class, StrIncrBack.id);
    }
}
