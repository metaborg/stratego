package mb.stratego.build.spoofax2;

import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;

import mb.pie.taskdefs.guice.TaskDefsModule;
import mb.resource.DefaultResourceService;
import mb.resource.ResourceService;
import mb.resource.fs.FSResourceRegistry;
import mb.stratego.build.strincr.Back;
import mb.stratego.build.strincr.Check;
import mb.stratego.build.strincr.CheckModule;
import mb.stratego.build.strincr.Compile;
import mb.stratego.build.strincr.Front;
import mb.stratego.build.strincr.IModuleImportService;
import mb.stratego.build.strincr.IModuleImportServiceFactory;
import mb.stratego.build.strincr.InsertCasts;
import mb.stratego.build.strincr.Lib;
import mb.stratego.build.strincr.Resolve;
import mb.stratego.build.strincr.ResourcePathConverter;
import mb.stratego.build.strincr.StrategyStubs;
import mb.stratego.build.util.IOAgentTrackerFactory;
import mb.stratego.build.util.StrIncrContext;

public class StrIncrModule extends TaskDefsModule {
    @Override public void bindTaskDefs() {
        // bind special strategoxt context object used in all Tasks
        bind(StrIncrContext.class).in(Singleton.class);
        bind(StrategyStubs.class).in(Singleton.class);

        bind(ResourceService.class).toInstance(new DefaultResourceService(new FSResourceRegistry()));
        bind(IOAgentTrackerFactory.class).to(ResourceAgentTrackerFactory.class).in(Singleton.class);
        bind(ResourcePathConverter.class).to(FileResourcePathConverter.class).in(Singleton.class);

        bindTaskDef(Compile.class, Compile.id);
        bindTaskDef(Back.class, Back.id);
        bindTaskDef(Check.class, Check.id);
        bindTaskDef(CheckModule.class, CheckModule.id);
        bindTaskDef(InsertCasts.class, InsertCasts.id);
        bindTaskDef(Resolve.class, Resolve.id);
        bindTaskDef(Front.class, Front.id);
        bindTaskDef(Lib.class, Lib.id);
        install(new FactoryModuleBuilder()
            .implement(IModuleImportService.class, ModuleImportService.class)
            .build(IModuleImportServiceFactory.class));
    }
}
