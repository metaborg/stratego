package mb.stratego.build.strincr;

import java.util.Set;

import org.metaborg.core.messages.MessageSeverity;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.attachments.OriginAttachment;

public abstract class Message<T extends IStrategoTerm> {
    public final String moduleFilePath;
    public final T locationTerm;
    public final MessageSeverity severity;

    public Message(String module, T name, MessageSeverity severity) {
        this.moduleFilePath = module;
        this.locationTerm = name;
        this.severity = severity;
    }

    public static Message<IStrategoString> externalStrategyNotFound(String module, IStrategoString definitionName) {
        IStrategoTerm origin = OriginAttachment.getOrigin(definitionName);
        if(origin != null && origin instanceof IStrategoString) {
            definitionName = (IStrategoString) origin;
        }
        return new ExternalStrategyNotFound(module, definitionName);
    }

    public static Message<IStrategoString> strategyNotFound(String module, IStrategoString name,
        MessageSeverity severity) {
        IStrategoTerm origin = OriginAttachment.getOrigin(name);
        if(origin != null && origin instanceof IStrategoString) {
            name = (IStrategoString) origin;
        }
        return new StrategyNotFound(module, name, severity);
    }

    public static Message<IStrategoString> constructorNotFound(String module, IStrategoString name,
        MessageSeverity severity) {
        IStrategoTerm origin = OriginAttachment.getOrigin(name);
        if(origin != null && origin instanceof IStrategoString) {
            name = (IStrategoString) origin;
        }
        return new ConstructorNotFound(module, name, severity);
    }

    public static Message<IStrategoString> externalStrategyOverlap(String module, IStrategoString name) {
        IStrategoTerm origin = OriginAttachment.getOrigin(name);
        if(origin != null && origin instanceof IStrategoString) {
            name = (IStrategoString) origin;
        }
        return new ExternalStrategyOverlap(module, name);
    }

    public static Message<IStrategoString> cyclicOverlay(String module, IStrategoString name, Set<String> overlayScc) {
        IStrategoTerm origin = OriginAttachment.getOrigin(name);
        if(origin != null && origin instanceof IStrategoString) {
            name = (IStrategoString) origin;
        }
        return new CyclicOverlay(module, name, overlayScc);
    }

    public static Message<IStrategoString> ambiguousStrategyCall(String module, IStrategoString name,
        Set<String> defs) {
        IStrategoTerm origin = OriginAttachment.getOrigin(name);
        if(origin != null && origin instanceof IStrategoString) {
            name = (IStrategoString) origin;
        }
        return new AmbiguousStrategyCall(module, name, defs);
    }

    public static Message<IStrategoString> unresolvedImport(String module, IStrategoString path) {
        return new UnresolvedImport(module, path);
    }

    public static Message<IStrategoString> unresolvedWildcardImport(String module, IStrategoString path) {
        return new UnresolvedWildcardImport(module, path);
    }

    public static Message<IStrategoTerm> constantCongruence(String module, IStrategoTerm congruence) {
        return new ConstantCongruence(module, congruence);
    }

    public static Message<IStrategoString> varConstrOverlap(String module, IStrategoString name) {
        return new VarConstrOverlap(module, name);
    }

    public String toString() {
        return "In module '" + moduleFilePath + "': " + getMessage();
    }

    public abstract String getMessage();
}

class ExternalStrategyNotFound extends Message<IStrategoString> {
    public ExternalStrategyNotFound(String module, IStrategoString name) {
        super(module, name, MessageSeverity.ERROR);
    }

    @Override public String getMessage() {
        return "Cannot find external strategy or rule '" + locationTerm.stringValue() + "'";
    }
}

class StrategyNotFound extends Message<IStrategoString> {
    public StrategyNotFound(String module, IStrategoString name, MessageSeverity severity) {
        super(module, name, severity);
    }

    @Override public String getMessage() {
        if(severity == MessageSeverity.ERROR) {
            return "Cannot find strategy or rule '" + locationTerm.stringValue() + "'";
        } else {
            return "Found '" + locationTerm.stringValue() + "' in a compiled library that was not imported directly or indirectly by this module";
        }
    }
}

class ExternalStrategyOverlap extends Message<IStrategoString> {
    public ExternalStrategyOverlap(String module, IStrategoString name) {
        super(module, name, MessageSeverity.ERROR);
    }

    @Override public String getMessage() {
        return "Strategy '" + locationTerm.stringValue() + "' overlaps with an externally defined strategy";
    }
}

class ConstructorNotFound extends Message<IStrategoString> {
    public ConstructorNotFound(String module, IStrategoString name, MessageSeverity severity) {
        super(module, name, severity);
    }

    @Override public String getMessage() {
        return "Cannot find constructor '" + locationTerm.stringValue() + "'";
    }
}

class CyclicOverlay extends Message<IStrategoString> {
    public final Set<String> cycle;

    public CyclicOverlay(String module, IStrategoString name, Set<String> cycle) {
        super(module, name, MessageSeverity.ERROR);
        this.cycle = cycle;
    }

    @Override public String getMessage() {
        return "Cyclic overlay definitions " + cycle;
    }
}

class AmbiguousStrategyCall extends Message<IStrategoString> {
    public final Set<String> defs;

    public AmbiguousStrategyCall(String module, IStrategoString name, Set<String> defs) {
        super(module, name, MessageSeverity.ERROR);
        this.defs = defs;
    }

    @Override public String getMessage() {
        return "The call to '" + locationTerm.stringValue() + "' is ambiguous, it may resolve to " + defs;
    }
}

class UnresolvedImport extends Message<IStrategoString> {
    public UnresolvedImport(String module, IStrategoString name) {
        super(module, name, MessageSeverity.ERROR);
    }

    @Override public String getMessage() {
        return "Cannot find module for import '" + locationTerm.stringValue() + "'";
    }
}

class UnresolvedWildcardImport extends Message<IStrategoString> {
    public UnresolvedWildcardImport(String module, IStrategoString name) {
        super(module, name, MessageSeverity.ERROR);
    }

    @Override public String getMessage() {
        return "Cannot find directory for wildcard import '" + locationTerm.stringValue() + "'";
    }
}

class ConstantCongruence extends Message<IStrategoTerm> {
    public ConstantCongruence(String module, IStrategoTerm congruence) {
        super(module, congruence, MessageSeverity.WARNING);
    }

    @Override public String getMessage() {
        return "Simple matching congruence: prefix with '?'. Or with '!' if you meant to build.";
    }
}

class VarConstrOverlap extends Message<IStrategoString> {
    public VarConstrOverlap(String module, IStrategoString name) {
        super(module, name, MessageSeverity.ERROR);
    }

    @Override public String getMessage() {
        return "Nullary constructor '" + locationTerm.stringValue() + "' should be followed by round brackets ().";
    }
}
