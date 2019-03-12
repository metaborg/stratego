package mb.stratego.build.bench;

import org.apache.commons.vfs2.FileSelectInfo;
import org.apache.commons.vfs2.FileSelector;
import org.metaborg.spoofax.core.resource.SpoofaxIgnoresSelector;

/**
 * Ignore the Stratego-Sugar table so we don't get a second (active) implementation of StrategoSugar after we loaded
 * the editor project.
 */
public class SpecialIgnoresSelector extends SpoofaxIgnoresSelector {
    @Override public boolean includeFile(FileSelectInfo fileInfo) throws Exception {
        return !fileInfo.getFile().getName().getBaseName().equals("Stratego-Sugar.tbl");
    }
}
