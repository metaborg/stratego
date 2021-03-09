package mb.stratego.build.strincr.data;

import org.spoofax.interpreter.terms.IStrategoAppl;

public class BoilerplateConstructorData extends ConstructorData {
    public BoilerplateConstructorData(ConstructorData constructorData) {
        super(new ConstructorSignatureMatcher(constructorData.signature), constructorData.astTerm,
            constructorData.type);
    }

    public BoilerplateConstructorData(ConstructorSignatureMatcher signature, IStrategoAppl astTerm,
        ConstructorType type) {
        super(signature, astTerm, type);
    }
}
