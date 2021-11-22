package benchmark.chocopy;

import api.chocopy.ChocoPyProgram;
import benchmark.generic.OptimisationBenchmark;
import org.metaborg.core.MetaborgException;
import org.openjdk.jmh.annotations.Param;

import java.io.IOException;

public abstract class ChocoPyBenchmark extends OptimisationBenchmark<ChocoPyProgram> {

    private static final String LANGUAGE_SUBFOLDER = "chocopy";

    @Param({"", /*"elseif", "nested-switch",*/ "hash-switch"})
    public String switchImplementation = "";

//    @Param({"", /*"name-arity",*/ "arity-name"})
//    public String switchImplementationOrder = "";

//    @SuppressWarnings("unused")
//    @Param({"on", "off"})
//    String fusion = "";

    @Override
    public void instantiateProgram() throws MetaborgException, IOException {
        program = new ChocoPyProgram(sourcePath, optimisationLevel, metaborgVersion);
    }

    @Override
    protected String languageSubFolder() {
        return LANGUAGE_SUBFOLDER;
    }
}
