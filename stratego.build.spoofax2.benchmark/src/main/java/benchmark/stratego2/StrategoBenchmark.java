package benchmark.stratego2;

import api.stratego2.Stratego2Program;
import benchmark.generic.OptimisationBenchmark;
import org.metaborg.core.MetaborgException;
import org.openjdk.jmh.annotations.Param;

import java.io.IOException;

public abstract class StrategoBenchmark extends OptimisationBenchmark<Stratego2Program> {

    private static final String LANGUAGE_SUBFOLDER = "stratego2";

    @Param({"", "elseif", "nested-switch", "hash-switch"})
    public String switchImplementation = "";

//    @Param({"", /*"name-arity",*/ "arity-name"})
//    public String switchImplementationOrder = "";

//    @SuppressWarnings("unused")
//    @Param({"on", "off"})
//    String fusion = "";

    @Override
    public void instantiateProgram() throws MetaborgException, IOException {
        program = new Stratego2Program(sourcePath, args, metaborgVersion);
    }

    @Override
    protected String languageSubFolder() {
        return LANGUAGE_SUBFOLDER;
    }
}
