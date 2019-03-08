package mb.stratego.build.bench;

import org.metaborg.core.editor.IEditorRegistry;
import org.metaborg.core.editor.NullEditorRegistry;
import org.metaborg.core.project.ConfigBasedProjectService;
import org.metaborg.core.project.IProjectService;
import org.metaborg.spoofax.core.SpoofaxModule;
import javax.inject.Singleton;

public class NullEditorConfigBasedProject extends SpoofaxModule {
    @Override protected void bindEditor() {
        bind(IEditorRegistry.class).to(NullEditorRegistry.class).in(Singleton.class);
    }

    @Override protected void bindProject() {
        bind(IProjectService.class).to(ConfigBasedProjectService.class).in(Singleton.class);
    }
}
