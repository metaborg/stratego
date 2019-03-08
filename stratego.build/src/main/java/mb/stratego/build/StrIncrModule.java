package mb.stratego.build;

import mb.pie.api.TaskDef;
import mb.pie.taskdefs.guice.TaskDefsModule;

import com.google.inject.Binder;
import com.google.inject.multibindings.MapBinder;
import javax.inject.Singleton;

public class StrIncrModule extends TaskDefsModule {
    @Override public void bindTaskDefs(Binder binder, MapBinder<String, TaskDef<?, ?>> mapBinder) {
        bindTaskDef(binder, mapBinder, StrIncr.id,      StrIncr.class);
        bindTaskDef(binder, mapBinder, StrIncrFront.id, StrIncrFront.class);
        bindTaskDef(binder, mapBinder, StrIncrBack.id,  StrIncrBack.class);
    }

    private void bindTaskDef(Binder binder, MapBinder<String, TaskDef<?, ?>> mapBinder, String id,
            Class<? extends TaskDef<?, ?>> c) {
        binder.bind(c).in(Singleton.class);
        mapBinder.addBinding(id).to(c);
    }
}
