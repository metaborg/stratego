package mb.stratego.build.spoofax2;

import com.google.inject.Singleton;

import mb.pie.taskdefs.guice.TaskDefsModule;
import mb.resource.DefaultResourceService;
import mb.resource.ResourceService;
import mb.resource.fs.FSResourceRegistry;
import mb.stratego.build.strincr.IModuleImportService;
import mb.stratego.build.strincr.ModuleImportService;
import mb.stratego.build.strincr.StrategoLanguage;
import mb.stratego.build.strincr.ResourcePathConverter;
import mb.stratego.build.strincr.task.Back;
import mb.stratego.build.strincr.task.Check;
import mb.stratego.build.strincr.task.CheckModule;
import mb.stratego.build.strincr.task.Compile;
import mb.stratego.build.strincr.task.Front;
import mb.stratego.build.strincr.task.FrontSplit;
import mb.stratego.build.strincr.task.Resolve;
import mb.stratego.build.util.IOAgentTrackerFactory;
import mb.stratego.build.util.StrIncrContext;
import mb.stratego.build.util.GenerateStratego;

public class StrIncrModule extends TaskDefsModule {
    @Override public void bindTaskDefs() {
        // bind special strategoxt context object used in all Tasks
        bind(StrIncrContext.class).in(Singleton.class);
        bind(GenerateStratego.class).in(Singleton.class);

        bind(ResourceService.class).toInstance(new DefaultResourceService(new FSResourceRegistry()));
        bind(IOAgentTrackerFactory.class).to(ResourceAgentTrackerFactory.class).in(Singleton.class);
        bind(ResourcePathConverter.class).to(FileResourcePathConverter.class).in(Singleton.class);
        bind(StrategoLanguage.class).to(Spoofax2StrategoLanguage.class).in(Singleton.class);
        bind(IModuleImportService.class).to(ModuleImportService.class).in(Singleton.class);

        bindTaskDef(Compile.class, Compile.id);
        bindTaskDef(Back.class, Back.id);
        bindTaskDef(Check.class, Check.id);
        bindTaskDef(CheckModule.class, CheckModule.id);
        bindTaskDef(Resolve.class, Resolve.id);
        bindTaskDef(Front.class, Front.id);
        bindTaskDef(FrontSplit.class, FrontSplit.id);
    }
}
