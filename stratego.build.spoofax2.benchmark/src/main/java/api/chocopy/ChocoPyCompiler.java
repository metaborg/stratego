package api.chocopy;

import api.Compiler;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.action.EndNamedGoal;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.project.IProject;
import org.metaborg.spoofax.core.unit.ISpoofaxInputUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxTransformUnit;
import org.metaborg.util.concurrent.IClosableLock;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Objects;

public final class ChocoPyCompiler extends Compiler<Collection<ISpoofaxTransformUnit<ISpoofaxParseUnit>>> {

    private final int optimisationLevel;
    private IContext context;
    private IClosableLock lock;
    private ISpoofaxParseUnit parseUnit;

    public ChocoPyCompiler(String metaborgVersion, Path sourcePath, int optimisationLevel) throws IOException {
        super(metaborgVersion, sourcePath);

        this.optimisationLevel = optimisationLevel;
    }

    @Override
    protected void setupBuild() throws MetaborgException, IOException {
        // Get ChocoPy language
        ILanguageImpl language = spoofax.languageDiscoveryService.languageFromArchive(spoofax.resolve(getChocoPyPath(optimisationLevel).toFile()));

        // Get source file contents
        String fileContents = spoofax.sourceTextService.text(spoofax.resolve(sourcePath.toFile()));
        // TODO Does this work?
        IProject project = spoofax.projectService.get(spoofax.resolve(sourcePath.getParent().toFile()));

        ISpoofaxInputUnit inputUnit = spoofax.unitService.inputUnit(fileContents, language, null);
        parseUnit = spoofax.syntaxService.parse(inputUnit);
        context = spoofax.contextService.get(parseUnit.source(), project, parseUnit.input().langImpl());
    }

    public void lock() {
        lock = context.read();
    }

    public void unlock() {
        lock.close();
    }

    @Override
    public Collection<ISpoofaxTransformUnit<ISpoofaxParseUnit>> compileProgram() throws MetaborgException {
        compiledProgram = spoofax.transformService.transform(parseUnit, context, new EndNamedGoal("Generate RV32IM AST (.rv32im.aterm file)"));
        return compiledProgram;
    }

    @Override
    public String run() throws MetaborgException {
        return Objects.requireNonNull(spoofax.strategoCommon.invoke(parseUnit.input().langImpl(), context, parseUnit.ast(), "execute-rv32im-ast")).toString(8);
    }

    @Override
    public void cleanup() { }

    private static Path getChocoPyPath(int optimisationLevel) {
        // TODO Fix path
        return localRepository.resolve(Paths.get("org", "example", "chocopy.backend", "0.1.0-SNAPSHOT", "chocopy.backend-0.1.0-SNAPSHOT.spoofax-language"));
    }

    private static Path getRiscVPath(int optimisationLevel) {
        // TODO Fix path
        return localRepository.resolve(Paths.get("org", "metaborg", "RV32IM", "0.1.0-SNAPSHOT", "RV32IM-0.1.0-SNAPSHOT.spoofax-language"));
    }
}
