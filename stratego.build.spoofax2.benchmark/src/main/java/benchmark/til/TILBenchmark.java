package benchmark.til;

import api.til.TILProgram;
import benchmark.generic.OptimisationBenchmark;
import org.metaborg.core.MetaborgException;
import org.openjdk.jmh.annotations.Param;

import java.io.IOException;

public abstract class TILBenchmark extends OptimisationBenchmark<TILProgram> {

    private static final String LANGUAGE_SUBFOLDER = "TIL";

    @Param({"", "hash-switch"})
    public String switchImplementation = "";

    @Override
    public void instantiateProgram() throws MetaborgException, IOException {
        program = new TILProgram(sourcePath, optimisationLevel, metaborgVersion);
    }

    @Override
    protected String languageSubFolder() {
        return LANGUAGE_SUBFOLDER;
    }

    @Override
    protected final String sourceFileName() {
        return problemFileName();
    }
}
