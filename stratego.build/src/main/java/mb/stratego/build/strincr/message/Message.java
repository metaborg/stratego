package mb.stratego.build.strincr.message;

import java.io.Serializable;
import java.util.Set;

import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.ImploderAttachment;
import org.spoofax.terms.attachments.OriginAttachment;
import org.spoofax.terms.util.TermUtils;

import mb.pie.api.Logger;
import mb.stratego.build.strincr.MessageSeverity;
import mb.stratego.build.strincr.message.java.AmbiguousStrategyCall;
import mb.stratego.build.strincr.message.java.ConstantCongruence;
import mb.stratego.build.strincr.message.java.ConstructorNotFound;
import mb.stratego.build.strincr.message.java.CyclicOverlay;
import mb.stratego.build.strincr.message.java.ExternalStrategyNotFound;
import mb.stratego.build.strincr.message.java.ExternalStrategyOverlap;
import mb.stratego.build.strincr.message.java.StrategyNotFound;
import mb.stratego.build.strincr.message.java.UnresolvedImport;
import mb.stratego.build.strincr.message.java.UnresolvedWildcardImport;
import mb.stratego.build.strincr.message.java.VarConstrOverlap;
import mb.stratego.build.strincr.message.stratego.AmbiguousConstructorUse;
import mb.stratego.build.strincr.message.stratego.AsInBuildTerm;
import mb.stratego.build.strincr.message.stratego.BuildDefaultInBuildTerm;
import mb.stratego.build.strincr.message.stratego.BuildDefaultInMatchTerm;
import mb.stratego.build.strincr.message.stratego.BuildUnboundTerm;
import mb.stratego.build.strincr.message.stratego.CallDynamicNotSupported;
import mb.stratego.build.strincr.message.stratego.CallStrategyArgumentTakesParameters;
import mb.stratego.build.strincr.message.stratego.DuplicateTypeDefinition;
import mb.stratego.build.strincr.message.stratego.MatchNotSpecificEnoughForTP;
import mb.stratego.build.strincr.message.stratego.MissingDefinitionForTypeDefinition;
import mb.stratego.build.strincr.message.stratego.MultipleAppsInMatch;
import mb.stratego.build.strincr.message.stratego.NoInjectionBetween;
import mb.stratego.build.strincr.message.stratego.NonListInAnno;
import mb.stratego.build.strincr.message.stratego.NonStringOrListInExplodeConsPosition;
import mb.stratego.build.strincr.message.stratego.ProceedInNonExtendStrategy;
import mb.stratego.build.strincr.message.stratego.ProceedWrongNumberOfArguments;
import mb.stratego.build.strincr.message.stratego.RawTermMessage;
import mb.stratego.build.strincr.message.stratego.STypeMismatch;
import mb.stratego.build.strincr.message.stratego.StrategyVariableTypedWithTermType;
import mb.stratego.build.strincr.message.stratego.StringQuotationInMatchTerm;
import mb.stratego.build.strincr.message.stratego.TermVariableTypedWithStrategyType;
import mb.stratego.build.strincr.message.stratego.TypeMismatch;
import mb.stratego.build.strincr.message.stratego.UnresolvedConstructor;
import mb.stratego.build.strincr.message.stratego.UnresolvedLocal;
import mb.stratego.build.strincr.message.stratego.UnresolvedStrategy;
import mb.stratego.build.strincr.message.stratego.VariableBoundToIncompatibleType;
import mb.stratego.build.strincr.message.stratego.WldInBuildTerm;

public abstract class Message<T extends IStrategoTerm> implements Serializable {
    public final String moduleFilePath;
    public final T locationTerm;
    public final ImploderAttachment location;
    public final MessageSeverity severity;

    public Message(String module, T name, MessageSeverity severity) {
        this.moduleFilePath = module;
        this.locationTerm = name;
        this.location = ImploderAttachment.get(OriginAttachment.tryGetOrigin(name));
        this.severity = severity;
    }

    public static JavaMessage<IStrategoString> externalStrategyNotFound(String module, IStrategoString definitionName) {
        IStrategoTerm origin = OriginAttachment.getOrigin(definitionName);
        if(origin != null && TermUtils.isString(origin)) {
            definitionName = (IStrategoString) origin;
        }
        return new ExternalStrategyNotFound(module, definitionName);
    }

    public static JavaMessage<IStrategoString> strategyNotFound(String module, IStrategoString name,
        MessageSeverity severity) {
        IStrategoTerm origin = OriginAttachment.getOrigin(name);
        if(origin != null && TermUtils.isString(origin)) {
            name = (IStrategoString) origin;
        }
        return new StrategyNotFound(module, name, severity);
    }

    public static JavaMessage<IStrategoString> constructorNotFound(String module, IStrategoString name,
        MessageSeverity severity) {
        IStrategoTerm origin = OriginAttachment.getOrigin(name);
        if(origin != null && TermUtils.isString(origin)) {
            name = (IStrategoString) origin;
        }
        return new ConstructorNotFound(module, name, severity);
    }

    public static JavaMessage<IStrategoString> externalStrategyOverlap(String module, IStrategoString name) {
        IStrategoTerm origin = OriginAttachment.getOrigin(name);
        if(origin != null && TermUtils.isString(origin)) {
            name = (IStrategoString) origin;
        }
        return new ExternalStrategyOverlap(module, name);
    }

    public static JavaMessage<IStrategoString> cyclicOverlay(String module, IStrategoString name, Set<String> overlayScc) {
        IStrategoTerm origin = OriginAttachment.getOrigin(name);
        if(origin != null && TermUtils.isString(origin)) {
            name = (IStrategoString) origin;
        }
        return new CyclicOverlay(module, name, overlayScc);
    }

    public static JavaMessage<IStrategoString> ambiguousStrategyCall(String module, IStrategoString name,
        Set<String> defs) {
        IStrategoTerm origin = OriginAttachment.getOrigin(name);
        if(origin != null && TermUtils.isString(origin)) {
            name = (IStrategoString) origin;
        }
        return new AmbiguousStrategyCall(module, name, defs);
    }

    public static JavaMessage<IStrategoTerm> unresolvedImport(String module, IStrategoString path) {
        return new UnresolvedImport(module, path);
    }

    public static JavaMessage<IStrategoString> unresolvedWildcardImport(String module, IStrategoString path) {
        return new UnresolvedWildcardImport(module, path);
    }

    public static JavaMessage<IStrategoTerm> constantCongruence(String module, IStrategoTerm congruence) {
        return new ConstantCongruence(module, congruence);
    }

    public static JavaMessage<IStrategoString> varConstrOverlap(String module, IStrategoString name) {
        return new VarConstrOverlap(module, name);
    }

    public static StrategoMessage from(Logger logger, String module, IStrategoTerm messageTuple, MessageSeverity severity) {
        IStrategoTerm locationTerm = messageTuple.getSubterm(0);
        final IStrategoTerm messageTerm = messageTuple.getSubterm(1);
        switch(TermUtils.toAppl(messageTerm).getName()) {
            case "CallDynamicNotSupported":
                return new CallDynamicNotSupported(module, locationTerm, severity);
            case "TermVariableTypedWithStrategyType":
                return new TermVariableTypedWithStrategyType(module, locationTerm, severity);
            case "StrategyVariableTypedWithTermType":
                return new StrategyVariableTypedWithTermType(module, locationTerm, severity);
            case "DuplicateTypeDefinition":
                return new DuplicateTypeDefinition(module, locationTerm, severity);
            case "MissingDefinitionForTypeDefinition":
                return new MissingDefinitionForTypeDefinition(module, locationTerm, severity);
            case "ProceedWrongNumberOfArguments":
                return new ProceedWrongNumberOfArguments(module, locationTerm, TermUtils.toJavaIntAt(messageTerm, 0),
                    TermUtils.toJavaIntAt(messageTerm, 1), severity);
            case "ProceedInNonExtendStrategy":
                return new ProceedInNonExtendStrategy(module, locationTerm, severity);
            case "CallStrategyArgumentTakesParameters": // SFunType -> ErrorDesc
                return new CallStrategyArgumentTakesParameters(module, locationTerm, messageTerm.getSubterm(0),
                    severity);
            case "NoInjectionBetween": // Type * Type -> ErrorDesc
                return new NoInjectionBetween(module, locationTerm, messageTerm.getSubterm(0),
                    messageTerm.getSubterm(1), severity);
            case "VariableBoundToIncompatibleType": // Type * Type -> ErrorDesc
                return new VariableBoundToIncompatibleType(module, locationTerm, messageTerm.getSubterm(0),
                    messageTerm.getSubterm(1), severity);
            case "TypeMismatch": // Type * Type -> ErrorDesc
                return new TypeMismatch(module, locationTerm, messageTerm.getSubterm(0),
                    messageTerm.getSubterm(1), severity);
            case "STypeMismatch": // SType * SType -> ErrorDesc
                return new STypeMismatch(module, locationTerm, messageTerm.getSubterm(0),
                    messageTerm.getSubterm(1), severity);
            case "UnresolvedLocal":
                return new UnresolvedLocal(module, locationTerm, severity);
            case "UnresolvedConstructor": // Int * Type -> ErrorDesc
                return new UnresolvedConstructor(module, locationTerm, TermUtils.toJavaIntAt(messageTerm, 0),
                    messageTerm.getSubterm(1), severity);
            case "UnresolvedStrategy":
                return new UnresolvedStrategy(module, locationTerm, TermUtils.toJavaIntAt(messageTerm, 0),
                    TermUtils.toJavaIntAt(messageTerm, 1), severity);
            case "AmbiguousConstructorUse": // List(Type) -> ErrorDesc
                return new AmbiguousConstructorUse(module, locationTerm, TermUtils.toJavaListAt(messageTerm, 0), severity);
            case "AsInBuildTerm":
                return new AsInBuildTerm(module, locationTerm, severity);
            case "WldInBuildTerm":
                return new WldInBuildTerm(module, locationTerm, severity);
            case "BuildDefaultInBuildTerm":
                return new BuildDefaultInBuildTerm(module, locationTerm, severity);
            case "BuildDefaultInMatchTerm":
                return new BuildDefaultInMatchTerm(module, locationTerm, severity);
            case "StringQuotationInMatchTerm":
                return new StringQuotationInMatchTerm(module, locationTerm, severity);
            case "NonStringOrListInExplodeConsPosition": // Type -> ErrorDesc
                return new NonStringOrListInExplodeConsPosition(module, locationTerm, messageTerm.getSubterm(0),
                    severity);
            case "NonListInAnno": // Type -> ErrorDesc
                return new NonListInAnno(module, locationTerm, messageTerm.getSubterm(0), severity);
            case "MultipleAppsInMatch":
                return new MultipleAppsInMatch(module, locationTerm, severity);
            case "BuildUnboundTerm":
                return new BuildUnboundTerm(module, locationTerm, severity);
            case "ErrorDesc.MatchNotSpecificEnoughForTP":
                return new MatchNotSpecificEnoughForTP(module, locationTerm, messageTerm.getSubterm(0),
                    severity);
            default:
                logger.warn("Unrecognised message from type checker, passing raw message. ", null);
                return new RawTermMessage(module, locationTerm, messageTerm, severity);
        }
    }

    public String toString() {
        return "In '" + moduleFilePath + "':\n" + getMessage();
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final Message<?> message = (Message<?>) o;
        if(!moduleFilePath.equals(message.moduleFilePath)) return false;
        if(!locationTerm.equals(message.locationTerm)) return false;
        if(severity != message.severity) return false;
        return getMessage().equals(message.getMessage());
    }

    @Override public int hashCode() {
        int result = moduleFilePath.hashCode();
        result = 31 * result + locationTerm.hashCode();
        result = 31 * result + severity.hashCode();
        result = 31 * result +  getMessage().hashCode();
        return result;
    }

    public abstract String getMessage();
}

