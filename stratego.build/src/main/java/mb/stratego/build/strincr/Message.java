package mb.stratego.build.strincr;

import java.util.Set;

import org.metaborg.core.messages.MessageSeverity;
import org.spoofax.interpreter.terms.IStrategoString;

public abstract class Message {
    public final String moduleFilePath;
    public final IStrategoString name;
    public final MessageSeverity severity = MessageSeverity.ERROR;

    public Message(String module, IStrategoString name) {
        this.moduleFilePath = module;
        this.name = name;
    }

    public static Message externalStrategyNotFound(String module, IStrategoString definitionName) {
        return new ExternalStrategyNotFound(module, definitionName);
    }

    public static Message strategyNotFound(String module, IStrategoString name) {
        return new StrategyNotFound(module, name);
    }

    public static Message constructorNotFound(String module, IStrategoString name) {
        return new ConstructorNotFound(module, name);
    }

    public static Message externalStrategyOverlap(String module, IStrategoString name) {
        return new ExternalStrategyOverlap(module, name);
    }

    public static Message cyclicOverlay(String module, IStrategoString name, Set<String> overlayScc) {
        return new CyclicOverlay(module, name, overlayScc);
    }

    public static Message ambiguousStrategyCall(String module, IStrategoString name, Set<String> defs) {
        return new AmbiguousStrategyCall(module, name, defs);
    }

    public static Message unresolvedImport(String module, IStrategoString path) {
        return new UnresolvedImport(module, path);
    }

    public static Message unresolvedWildcardImport(String module, IStrategoString path) {
        return new UnresolvedWildcardImport(module, path);
    }

    public String toString() {
        return "In module '" + moduleFilePath + "': " + getMessage();
    }

    public abstract String getMessage();
}

class ExternalStrategyNotFound extends Message {
    public ExternalStrategyNotFound(String module, IStrategoString name) {
        super(module, name);
    }

    @Override public String getMessage() {
        return "Cannot find external strategy or rule '" + name + "'";
    }
}

class StrategyNotFound extends Message {
    public StrategyNotFound(String module, IStrategoString name) {
        super(module, name);
    }

    @Override public String getMessage() {
        return "Cannot find strategy or rule '" + name + "'";
    }
}

class ExternalStrategyOverlap extends Message {
    public ExternalStrategyOverlap(String module, IStrategoString name) {
        super(module, name);
    }

    @Override public String getMessage() {
        return "Strategy '" + name + "' overlaps with an externally defined strategy";
    }
}

class ConstructorNotFound extends Message {
    public ConstructorNotFound(String module, IStrategoString name) {
        super(module, name);
    }

    @Override public String getMessage() {
        return "Cannot find constructor '" + name + "'";
    }
}

class CyclicOverlay extends Message {
    public final Set<String> cycle;

    public CyclicOverlay(String module, IStrategoString name, Set<String> cycle) {
        super(module, name);
        this.cycle = cycle;
    }

    @Override public String getMessage() {
        return "Cyclic overlay definitions " + cycle;
    }
}

class AmbiguousStrategyCall extends Message {
    public final Set<String> defs;

    public AmbiguousStrategyCall(String module, IStrategoString name, Set<String> defs) {
        super(module, name);
        this.defs = defs;
    }

    @Override public String getMessage() {
        return "The call to '" + name + "' is ambiguous, it may resolve to " + defs;
    }
}

class UnresolvedImport extends Message {
    public UnresolvedImport(String module, IStrategoString name) {
        super(module, name);
    }

    @Override public String getMessage() {
        return "Cannot find module for import '" + name + "'";
    }
}

class UnresolvedWildcardImport extends Message {
    public UnresolvedWildcardImport(String module, IStrategoString name) {
        super(module, name);
    }

    @Override public String getMessage() {
        return "Cannot find directory for wildcard import '" + name + "'";
    }
}
