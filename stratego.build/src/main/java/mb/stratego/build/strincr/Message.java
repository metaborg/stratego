package mb.stratego.build.strincr;

import java.util.Set;

import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.attachments.OriginAttachment;
import org.spoofax.terms.util.TermUtils;

import mb.pie.api.Logger;

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
        if(origin != null && TermUtils.isString(origin)) {
            definitionName = (IStrategoString) origin;
        }
        return new ExternalStrategyNotFound(module, definitionName);
    }

    public static Message<IStrategoString> strategyNotFound(String module, IStrategoString name,
        MessageSeverity severity) {
        IStrategoTerm origin = OriginAttachment.getOrigin(name);
        if(origin != null && TermUtils.isString(origin)) {
            name = (IStrategoString) origin;
        }
        return new StrategyNotFound(module, name, severity);
    }

    public static Message<IStrategoString> constructorNotFound(String module, IStrategoString name,
        MessageSeverity severity) {
        IStrategoTerm origin = OriginAttachment.getOrigin(name);
        if(origin != null && TermUtils.isString(origin)) {
            name = (IStrategoString) origin;
        }
        return new ConstructorNotFound(module, name, severity);
    }

    public static Message<IStrategoString> externalStrategyOverlap(String module, IStrategoString name) {
        IStrategoTerm origin = OriginAttachment.getOrigin(name);
        if(origin != null && TermUtils.isString(origin)) {
            name = (IStrategoString) origin;
        }
        return new ExternalStrategyOverlap(module, name);
    }

    public static Message<IStrategoString> cyclicOverlay(String module, IStrategoString name, Set<String> overlayScc) {
        IStrategoTerm origin = OriginAttachment.getOrigin(name);
        if(origin != null && TermUtils.isString(origin)) {
            name = (IStrategoString) origin;
        }
        return new CyclicOverlay(module, name, overlayScc);
    }

    public static Message<IStrategoString> ambiguousStrategyCall(String module, IStrategoString name,
        Set<String> defs) {
        IStrategoTerm origin = OriginAttachment.getOrigin(name);
        if(origin != null && TermUtils.isString(origin)) {
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

    public static Message<IStrategoTerm> from(Logger logger, String module, IStrategoTerm messageTerm, MessageSeverity severity) {
        switch(TermUtils.toAppl(messageTerm).getName()) {
            case "CallDynamicNotSupported":
                return new CallDynamicNotSupported(module, messageTerm, severity);
            case "TermVariableTypedWithStrategyType":
                return new TermVariableTypedWithStrategyType(module, messageTerm, severity);
            case "StrategyVariableTypedWithTermType":
                return new StrategyVariableTypedWithTermType(module, messageTerm, severity);
            case "DuplicateTypeDefinition":
                return new DuplicateTypeDefinition(module, messageTerm, severity);
            case "MissingDefinitionForTypeDefinition":
                return new MissingDefinitionForTypeDefinition(module, messageTerm, severity);
            default:
                logger.warn("Unrecognised message from type checker, passing raw message. ", null);
                return new RawTermMessage(module, messageTerm, severity);
        }
        /* TODO: implement
         *  ProceedWrongNumberOfArguments        : Int * Int -> ErrorDesc
         *  ProceedInNonExtendStrategy           : ErrorDesc
         *  CallStrategyArgumentTakesParameters  : SFunType -> ErrorDesc
         *  NoInjectionBetween                   : Type * Type -> ErrorDesc
         *  VariableBoundToIncompatibleType      : Type * Type -> ErrorDesc
         *  TypeMismatch                         : Type * Type -> ErrorDesc
         *  STypeMismatch                        : SType * SType -> ErrorDesc
         *  UnresolvedLocal                      : ErrorDesc
         *  UnresolvedConstructor                : Int * Type -> ErrorDesc
         *  UnresolvedStrategy                   : Int * Int -> ErrorDesc
         *  AmbiguousConstructorUse              : List(Type) -> ErrorDesc
         *  AsInBuildTerm                        : ErrorDesc
         *  WldInBuildTerm                       : ErrorDesc
         *  BuildDefaultInBuildTerm              : ErrorDesc
         *  BuildDefaultInMatchTerm              : ErrorDesc
         *  StringQuotationInMatchTerm           : ErrorDesc
         *  NonStringOrListInExplodeConsPosition : Type -> ErrorDesc
         *  NonListInAnno                        : Type -> ErrorDesc
         *  MultipleAppsInMatch                  : ErrorDesc
         *  BuildUnboundTerm                     : ErrorDesc
         */
    }

    public String toString() {
        return "In '" + moduleFilePath + "':\n" + getMessage();
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

class RawTermMessage extends Message<IStrategoTerm> {
    public RawTermMessage(String module, IStrategoTerm locationTerm, MessageSeverity severity) {
        super(module, locationTerm, severity);
    }

    @Override
    public String getMessage() {
        return this.locationTerm.toString();
    }
}

class CallDynamicNotSupported extends Message<IStrategoTerm> {
    public CallDynamicNotSupported(String module, IStrategoTerm callDynTerm, MessageSeverity severity) {
        super(module, callDynTerm, severity);
    }

    @Override
    public String getMessage() {
        return "The dynamic call construct is no longer supported.";
    }
}

class TermVariableTypedWithStrategyType extends Message<IStrategoTerm> {
    public TermVariableTypedWithStrategyType(String module, IStrategoTerm callDynTerm, MessageSeverity severity) {
        super(module, callDynTerm, severity);
    }

    @Override
    public String getMessage() {
        return "This is a term variable, but it has a strategy type.";
    }
}

class StrategyVariableTypedWithTermType extends Message<IStrategoTerm> {
    public StrategyVariableTypedWithTermType(String module, IStrategoTerm callDynTerm, MessageSeverity severity) {
        super(module, callDynTerm, severity);
    }

    @Override
    public String getMessage() {
        return "This is a strategy variable, but it has a term type.";
    }
}

class DuplicateTypeDefinition extends Message<IStrategoTerm> {
    public DuplicateTypeDefinition(String module, IStrategoTerm callDynTerm, MessageSeverity severity) {
        super(module, callDynTerm, severity);
    }

    @Override
    public String getMessage() {
        return "Duplicate type definition.";
    }
}

class MissingDefinitionForTypeDefinition extends Message<IStrategoTerm> {
    public MissingDefinitionForTypeDefinition(String module, IStrategoTerm callDynTerm, MessageSeverity severity) {
        super(module, callDynTerm, severity);
    }

    @Override
    public String getMessage() {
        return "Cannot find definition corresponding to this type definition.";
    }
}
