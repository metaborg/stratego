package api.chocopy;

import api.Compiler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.action.EndNamedGoal;
import org.metaborg.core.context.IContext;
import org.metaborg.core.context.IContextService;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.project.IProject;
import org.metaborg.core.project.ISimpleProjectService;
import org.metaborg.core.source.ISourceTextService;
import org.metaborg.core.transform.TransformConfig;
import org.metaborg.spoofax.core.Spoofax;
import org.metaborg.spoofax.core.analysis.ISpoofaxAnalysisService;
import org.metaborg.spoofax.core.analysis.ISpoofaxAnalyzeResult;
import org.metaborg.spoofax.core.syntax.ISpoofaxSyntaxService;
import org.metaborg.spoofax.core.unit.*;
import org.metaborg.util.concurrent.IClosableLock;
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Objects;

public final class ChocoPyCompiler extends Compiler<Collection<ISpoofaxTransformUnit<ISpoofaxAnalyzeUnit>>> {

    private final Spoofax spoofax = new Spoofax();
    private final ISpoofaxUnitService unitService = spoofax.unitService;
    private final ISpoofaxSyntaxService syntaxService = spoofax.syntaxService;
    private final IContextService contextService = spoofax.contextService;
    private final ISpoofaxAnalysisService analysisService = spoofax.analysisService;
    private final ISourceTextService sourceTextService = spoofax.sourceTextService;
    private final ISimpleProjectService projectService = spoofax.injector.getInstance(ISimpleProjectService.class);

    private final ILanguageImpl chocoPy;
//    private final ILanguageImpl riscV;
    private IContext context;
    private IClosableLock lock;

    private final ISpoofaxParseUnit parseUnit;
    private File projectFolder;
    private ISpoofaxAnalyzeUnit analyzeUnit;
    private IProject project;
    private ISpoofaxTransformUnit<ISpoofaxAnalyzeUnit> riscvProgram;

    public ChocoPyCompiler(Path sourcePath, int optimisationLevel, String metaborgVersion) throws IOException, MetaborgException {
        super(metaborgVersion, sourcePath);

        // Languages
        chocoPy = spoofax.languageDiscoveryService.languageFromArchive(spoofax.resolve(getChocoPyPath(optimisationLevel).toFile()));
//        riscV = spoofax.languageDiscoveryService.languageFromArchive(spoofax.resolve(getRiscVPath(optimisationLevel).toFile()));

        // Get source file contents
        FileObject sourceFile = spoofax.resolve(sourcePath.toFile());
        String fileContents = sourceTextService.text(sourceFile);
        ISpoofaxInputUnit inputUnit = unitService.inputUnit(sourceFile, fileContents, chocoPy, null);
        parseUnit = syntaxService.parse(inputUnit);

        setupBuild();
    }

    @Override
    public void setupBuild() throws MetaborgException {
        System.out.println("Setting up build");

        try {
            projectFolder = Files.createTempDirectory("chocopybenchmark").toFile();
            FileUtils.forceDeleteOnExit(projectFolder);
        } catch (IOException ignored) {}

        project = projectService.create(spoofax.resolve(projectFolder));
        context = contextService.get(parseUnit.source(), project, parseUnit.input().langImpl());

        ISpoofaxAnalyzeResult analyzeResult;
        try (IClosableLock ignored = context.write()) {
            analyzeResult = analysisService.analyze(parseUnit, context);
        }

        analyzeUnit = analyzeResult.result();
    }

    @Override
    public void cleanup() {
        try {
            context.reset();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Collection<ISpoofaxTransformUnit<ISpoofaxAnalyzeUnit>> compileProgram() throws MetaborgException {
        try (IClosableLock ignored = context.read()) {
            compiledProgram = spoofax.transformService.transform(
                    Objects.requireNonNull(analyzeUnit),
                    context,
                    new EndNamedGoal("Generate RV32IM AST (.rv32im.aterm file)"),
                    new TransformConfig(true)
            );
            riscvProgram = compiledProgram.iterator().next();
        }
        return compiledProgram;
    }

//    @Override
    public String prettyPrint() throws MetaborgException {
        try (IClosableLock lock = context.read()) {
            IStrategoTerm result = spoofax.strategoCommon.invoke(chocoPy, context, riscvProgram.ast(), "pp-RV32IM-string");
            return Objects.requireNonNull(result).toString(8);
        }
    }

    private static Path getChocoPyPath(int optimisationLevel) {
        return localRepository.resolve(Paths.get("org", "example", "chocopy.backend", "0.1.0-SNAPSHOT", String.format("chocopy.backend-0.1.0-SNAPSHOT.spoofax-language.O%d", optimisationLevel)));
    }

    private static Path getRiscVPath(int optimisationLevel) {
        return localRepository.resolve(Paths.get("org", "metaborg", "RV32IM", "0.1.0-SNAPSHOT", String.format("RV32IM-0.1.0-SNAPSHOT.spoofax-language.O%d", optimisationLevel)));
    }
}
