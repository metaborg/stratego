package mb.stratego.build.spoofax2;

import com.google.inject.Singleton;

import mb.pie.taskdefs.guice.TaskDefsModule;
import mb.resource.DefaultResourceService;
import mb.resource.ResourceService;
import mb.resource.fs.FSResourceRegistry;
import mb.stratego.build.strincr.Backend;
import mb.stratego.build.strincr.Frontend;
import mb.stratego.build.strincr.Frontends;
import mb.stratego.build.strincr.InsertCasts;
import mb.stratego.build.strincr.LibFrontend;
import mb.stratego.build.strincr.ParseStratego;
import mb.stratego.build.strincr.StrIncr;
import mb.stratego.build.strincr.StrIncrAnalysis;
import mb.stratego.build.strincr.SubFrontend;
import mb.stratego.build.util.IOAgentTrackerFactory;
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
        bind(Frontends.class).in(Singleton.class);

        bind(ResourceService.class).toInstance(new DefaultResourceService(new FSResourceRegistry()));
        bind(ParseStratego.class).to(Spoofax2ParseStratego.class).in(Singleton.class);
        bind(IOAgentTrackerFactory.class).to(ResourceAgentTrackerFactory.class).in(Singleton.class);
        bind(Backend.ResourcePathConverter.class).to(FileResourcePathConverter.class).in(Singleton.class);
    }
}
