package mb.stratego.build.strincr;

import java.util.Set;

import org.spoofax.interpreter.terms.IStrategoString;

public abstract class ErrorMessage {
    public final String module;
    public final IStrategoString name;

    public ErrorMessage(String module, IStrategoString name) {
        this.module = module;
        this.name = name;
    }

    public static ErrorMessage externalStrategyNotFound(String module, IStrategoString definitionName) {
        return new ExternalStrategyNotFound(module, definitionName);
    }

    public static ErrorMessage strategyNotFound(String module, IStrategoString name) {
        return new StrategyNotFound(module, name);
    }

    public static ErrorMessage constructorNotFound(String module, IStrategoString name) {
        return new ConstructorNotFound(module, name);
    }

    public static ErrorMessage externalStrategyOverlap(String module, IStrategoString name) {
        return new ExternalStrategyOverlap(module, name);
    }

    public static ErrorMessage cyclicOverlay(String module, IStrategoString name, Set<String> overlayScc) {
        return new CyclicOverlay(module, name, overlayScc);
    }

    public static ErrorMessage ambiguousStrategyCall(String module, IStrategoString name, Set<String> defs) {
        return new AmbiguousStrategyCall(module, name, defs);
    }

    public String toString() {
        return "In module '" + module + "': " + getMessage();
    }

    public abstract String getMessage();
}

class ExternalStrategyNotFound extends ErrorMessage {
    public ExternalStrategyNotFound(String module, IStrategoString name) {
        super(module, name);
    }

    @Override public String getMessage() {
        return "Cannot find external strategy or rule '" + name + "'";
    }
}

class StrategyNotFound extends ErrorMessage {
    public StrategyNotFound(String module, IStrategoString name) {
        super(module, name);
    }

    @Override public String getMessage() {
        return "Cannot find strategy or rule '" + name + "'";
    }
}

class ExternalStrategyOverlap extends ErrorMessage {
    public ExternalStrategyOverlap(String module, IStrategoString name) {
        super(module, name);
    }

    @Override public String getMessage() {
        return "Strategy '" + name + "' overlaps with an externally defined strategy";
    }
}

class ConstructorNotFound extends ErrorMessage {
    public ConstructorNotFound(String module, IStrategoString name) {
        super(module, name);
    }

    @Override public String getMessage() {
        return "Cannot find constructor '" + name + "'";
    }
}

class CyclicOverlay extends ErrorMessage {
    public final Set<String> cycle;

    public CyclicOverlay(String module, IStrategoString name, Set<String> cycle) {
        super(module, name);
        this.cycle = cycle;
    }

    @Override public String getMessage() {
        return "Cyclic overlay definitions " + cycle;
    }
}

class AmbiguousStrategyCall extends ErrorMessage {
    public final Set<String> defs;

    public AmbiguousStrategyCall(String module, IStrategoString name, Set<String> defs) {
        super(module, name);
        this.defs = defs;
    }

    @Override public String getMessage() {
        return "The call to '" + name + "' is ambiguous, it may resolve to " + defs;
    }
}
