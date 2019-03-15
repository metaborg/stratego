package mb.stratego.build;

import mb.pie.api.TaskDef;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;

public class StrIncrModule extends AbstractModule {
    @Override public void configure() {
        MapBinder<String, TaskDef<?, ?>> taskDefsBinder = MapBinder.newMapBinder(binder(), new TypeLiteral<String>() {
        }, new TypeLiteral<TaskDef<?, ?>>() {
        });
        this.bindTaskDefs(binder(), taskDefsBinder);
    }

    private void bindTaskDefs(Binder binder, MapBinder<String, TaskDef<?, ?>> mapBinder) {
        bindTaskDef(StrIncr.class, binder, mapBinder, StrIncr.id);
        bindTaskDef(StrIncrFront.class, binder, mapBinder, StrIncrFront.id);
        bindTaskDef(StrIncrBack.class, binder, mapBinder, StrIncrBack.id);
    }

    private <B extends TaskDef<?, ?>> void bindTaskDef(Class<B> clazz, Binder binder,
        MapBinder<String, TaskDef<?, ?>> builderBinder, String id) {
        binder.bind(clazz).in(Singleton.class);
        builderBinder.addBinding(id).to(clazz);
    }
}
