package mb.stratego.build;

import mb.pie.api.TaskDef;
import mb.pie.taskdefs.guice.TaskDefsModule;

import com.google.inject.Binder;
import com.google.inject.multibindings.MapBinder;
import javax.inject.Singleton;

public class StrIncrModule extends TaskDefsModule {
    @Override public void bindTaskDefs(Binder binder, MapBinder<String, TaskDef<?, ?>> mapBinder) {
        // TODO: extract method for binding a class
        binder.bind(StrIncr.class).in(Singleton.class);
        mapBinder.addBinding(StrIncr.id).to(StrIncr.class);

        binder.bind(StrIncrFront.class).in(Singleton.class);
        mapBinder.addBinding(StrIncrFront.id).to(StrIncrFront.class);

        binder.bind(StrIncrBack.class).in(Singleton.class);
        mapBinder.addBinding(StrIncrBack.id).to(StrIncrBack.class);
    }
}
