package api.til;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;

import org.apache.commons.io.FileUtils;
import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.context.IContext;
import org.metaborg.core.context.IContextService;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.project.IProject;
import org.metaborg.core.project.ISimpleProjectService;
import org.metaborg.core.source.ISourceTextService;
import org.metaborg.spoofax.core.Spoofax;
import org.metaborg.spoofax.core.syntax.ISpoofaxSyntaxService;
import org.metaborg.spoofax.core.unit.ISpoofaxInputUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxUnitService;
import org.metaborg.util.concurrent.IClosableLock;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import api.Compiler;
import api.SimpleSpoofaxModule;

public final class TILLanguageProject extends Compiler<IStrategoTerm> {

    private final Spoofax spoofax = new Spoofax(new SimpleSpoofaxModule());
    private final IContextService contextService = spoofax.contextService;
    private final ISimpleProjectService projectService = spoofax.injector.getInstance(ISimpleProjectService.class);
    private final ITermFactory tf = spoofax.termFactory;

    private final ILanguageImpl til;
    private IContext context;

    private final ISpoofaxParseUnit parseUnit;
    private File projectFolder;
    private IProject project;

    public TILLanguageProject(Path sourcePath, int optimisationLevel, String metaborgVersion)
            throws IOException, MetaborgException {
        super(metaborgVersion, sourcePath);

        // Languages
        til = spoofax.languageDiscoveryService.languageFromArchive(
            spoofax.resolve(getTILPath(optimisationLevel).toFile()));

        // Get source file contents
        FileObject sourceFile = spoofax.resolve(sourcePath.toFile());
        ISourceTextService sourceTextService = spoofax.sourceTextService;
        String fileContents = sourceTextService.text(sourceFile);
        ISpoofaxUnitService unitService = spoofax.unitService;
        ISpoofaxInputUnit inputUnit = unitService.inputUnit(sourceFile, fileContents, til, null);
        ISpoofaxSyntaxService syntaxService = spoofax.syntaxService;
        parseUnit = syntaxService.parse(inputUnit);

        setupBuild();
    }

    @Override
    public void setupBuild() throws MetaborgException {
        try {
            projectFolder = Files.createTempDirectory("tilbenchmark").toFile();
            FileUtils.forceDeleteOnExit(projectFolder);
        } catch (IOException ignored) {}

        project = projectService.create(spoofax.resolve(projectFolder));
        context = contextService.get(parseUnit.source(), project, parseUnit.input().langImpl());
    }

    @Override
    public void cleanup() {
        try {
            project.location().delete();
            context.reset();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public IStrategoTerm compileProgram() throws MetaborgException {
        try(IClosableLock ignored = context.read()) {
            IStrategoTerm result =
                spoofax.strategoCommon.invoke(til, context, parseUnit.ast(), "til-optimise");
            return Objects.requireNonNull(result);
        }
    }

//    @Override
    public String run(Collection<String> inputStrings) throws MetaborgException {
        try(IClosableLock ignored = context.read()) {
            IStrategoTerm result = spoofax.strategoCommon.invoke(til, context,
                tf.makeTuple(parseUnit.ast(), makeList(inputStrings, tf::makeString)), "til-run");
            return Objects.requireNonNull(result).toString(8);
        }
    }

    private <T> IStrategoList makeList(Collection<T> items, Function<T, ? extends IStrategoTerm> f) {
        final IStrategoList.Builder b = tf.arrayListBuilder(items.size());

        for(T item : items) {
            b.add(f.apply(item));
        }

        return tf.makeList(b);
    }

    private static Path getTILPath(int optimisationLevel) {
        final String artifactName;
        if(optimisationLevel != -1) {
            artifactName = String.format("TIL-0.1.0-SNAPSHOT.spoofax-language.O%d", optimisationLevel);
        } else {
            artifactName = "TIL-0.1.0-SNAPSHOT.spoofax-language";
        }
        System.out.println("Loading language " + artifactName);
        return localRepository.resolve(Paths.get("mb", "cube", "TIL", "0.1.0-SNAPSHOT",
            artifactName));
    }
}
