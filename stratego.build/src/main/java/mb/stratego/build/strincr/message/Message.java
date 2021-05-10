package mb.stratego.build.strincr.message;

import java.io.Serializable;

import javax.annotation.Nullable;

import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.IToken;
import org.spoofax.jsglr.client.imploder.ImploderAttachment;
import org.spoofax.terms.attachments.OriginAttachment;
import org.spoofax.terms.util.TermUtils;

import mb.log.api.Logger;
import mb.stratego.build.strincr.message.type.AmbiguousConstructorUse;
import mb.stratego.build.strincr.message.type.DuplicateTypeDefinition;
import mb.stratego.build.strincr.message.type.MatchNotSpecificEnoughForTP;
import mb.stratego.build.strincr.message.type.MissingDefinitionForTypeDefinition;
import mb.stratego.build.strincr.message.type.NoInjectionBetween;
import mb.stratego.build.strincr.message.type.STypeMismatch;
import mb.stratego.build.strincr.message.type.StrategyVariableTypedWithTermType;
import mb.stratego.build.strincr.message.type.TermVariableTypedWithStrategyType;
import mb.stratego.build.strincr.message.type.TypeMismatch;
import mb.stratego.build.strincr.message.type.VariableBoundToIncompatibleType;
import mb.stratego.build.util.WithLastModified;

public abstract class Message implements WithLastModified, Serializable {
    public final IStrategoTerm locationTerm;
    // TODO: require location to be non-null once gradual type system stops losing origins
    public final @Nullable ImploderAttachment location;
    public final MessageSeverity severity;
    public final long lastModified;

    public Message(IStrategoTerm name, MessageSeverity severity, long lastModified) {
        this.locationTerm = name;
        this.location = ImploderAttachment.get(OriginAttachment.tryGetOrigin(name));
        assert this.location != null : "The given term " + name + " did not contain a location";
        this.severity = severity;
        this.lastModified = lastModified;
    }

    public static Message from(IStrategoTerm messageTuple, MessageSeverity severity, long lastModified) {
        IStrategoTerm locationTerm = messageTuple.getSubterm(0);
        final IStrategoTerm messageTerm = messageTuple.getSubterm(1);
        switch(TermUtils.toAppl(messageTerm).getName()) {
            case "CallDynamicNotSupported":
                return new CallDynamicNotSupported(locationTerm, severity, lastModified);
            case "TermVariableTypedWithStrategyType":
                return new TermVariableTypedWithStrategyType(locationTerm, severity, lastModified);
            case "StrategyVariableTypedWithTermType":
                return new StrategyVariableTypedWithTermType(locationTerm, severity, lastModified);
            case "DuplicateTypeDefinition":
                return new DuplicateTypeDefinition(locationTerm, severity, lastModified);
            case "MissingDefinitionForTypeDefinition":
                return new MissingDefinitionForTypeDefinition(locationTerm, severity, lastModified);
            case "ProceedWrongNumberOfArguments":
                return new ProceedWrongNumberOfArguments(locationTerm,
                    TermUtils.toJavaIntAt(messageTerm, 0), TermUtils.toJavaIntAt(messageTerm, 1),
                    severity, lastModified);
            case "ProceedInNonExtendStrategy":
                return new ProceedInNonExtendStrategy(locationTerm, severity, lastModified);
            case "CallStrategyArgumentTakesParameters": // SFunType -> ErrorDesc
                return new CallStrategyArgumentTakesParameters(locationTerm,
                    messageTerm.getSubterm(0), severity, lastModified);
            case "NoInjectionBetween": // Type * Type -> ErrorDesc
                return new NoInjectionBetween(locationTerm, messageTerm.getSubterm(0),
                    messageTerm.getSubterm(1), severity, lastModified);
            case "VariableBoundToIncompatibleType": // Type * Type -> ErrorDesc
                return new VariableBoundToIncompatibleType(locationTerm, messageTerm.getSubterm(0),
                    messageTerm.getSubterm(1), severity, lastModified);
            case "TypeMismatch": // Type * Type -> ErrorDesc
                return new TypeMismatch(locationTerm, messageTerm.getSubterm(0),
                    messageTerm.getSubterm(1), severity, lastModified);
            case "STypeMismatch": // SType * SType -> ErrorDesc
                return new STypeMismatch(locationTerm, messageTerm.getSubterm(0),
                    messageTerm.getSubterm(1), severity, lastModified);
            case "UnresolvedLocal":
                return new UnresolvedLocal(locationTerm, severity, lastModified);
            case "UnresolvedConstructor": // Int * Type -> ErrorDesc
                return new UnresolvedConstructor(locationTerm,
                    TermUtils.toJavaIntAt(messageTerm, 0), messageTerm.getSubterm(1), severity,
                    lastModified);
            case "UnresolvedStrategy":
                return new UnresolvedStrategy(locationTerm, TermUtils.toJavaIntAt(messageTerm, 0),
                    TermUtils.toJavaIntAt(messageTerm, 1), severity, lastModified);
            case "AmbiguousConstructorUse": // List(Type) -> ErrorDesc
                return new AmbiguousConstructorUse(locationTerm,
                    TermUtils.toJavaListAt(messageTerm, 0), severity, lastModified);
            case "AsInBuildTerm":
                return new AsInBuildTerm(locationTerm, severity, lastModified);
            case "WldInBuildTerm":
                return new WldInBuildTerm(locationTerm, severity, lastModified);
            case "BuildDefaultInBuildTerm":
                return new BuildDefaultInBuildTerm(locationTerm, severity, lastModified);
            case "BuildDefaultInMatchTerm":
                return new BuildDefaultInMatchTerm(locationTerm, severity, lastModified);
            case "StringQuotationInMatchTerm":
                return new StringQuotationInMatchTerm(locationTerm, severity, lastModified);
            case "NonStringOrListInExplodeConsPosition": // Type -> ErrorDesc
                return new NonStringOrListInExplodeConsPosition(locationTerm,
                    messageTerm.getSubterm(0), severity, lastModified);
            case "NonListInAnno": // Type -> ErrorDesc
                return new NonListInAnno(locationTerm, messageTerm.getSubterm(0), severity,
                    lastModified);
            case "MultipleAppsInMatch":
                return new MultipleAppsInMatch(locationTerm, severity, lastModified);
            case "BuildUnboundTerm":
                return new BuildUnboundTerm(locationTerm, severity, lastModified);
            case "MatchNotSpecificEnoughForTP":
                return new MatchNotSpecificEnoughForTP(locationTerm, messageTerm.getSubterm(0),
                    severity, lastModified);
            case "UnsupportedCastRequiredInDynamicRule":
                return new UnsupportedCastRequiredInDynamicRule(locationTerm, severity,
                    lastModified);
            default:
                return new RawTermMessage(locationTerm, messageTerm, severity, lastModified);
        }
    }

    public String toString() {
        return "In '" + locationString() + "':\n" + getMessage();
    }

    public String locationString() {
        final IToken leftToken = location.getLeftToken();
        final IToken rightToken = location.getRightToken();
        final String filename = leftToken.getFilename();
        final int leftLine = leftToken.getLine();
        final int leftColumn = leftToken.getColumn();
        final int rightLine = rightToken.getEndLine();
        final int rightColumn = rightToken.getEndColumn()+1;
        if(leftLine == rightLine) {
            if(leftColumn == rightColumn) {
                return filename + ":" + leftLine + ":" + leftColumn;
            }
            return filename + ":" + leftLine + ":" + leftColumn + "-" + rightColumn;
        } else {
            return filename + ":" + leftLine + "-" + rightLine + ":" + leftColumn + "-"
                + rightColumn;
        }
    }

    protected String locationTermString() {
        if(locationTerm instanceof IStrategoString) {
            return ((IStrategoString) locationTerm).stringValue();
        } else {
            return locationTerm.toString();
        }
    }

    public String moduleFilePath() {
        final IToken leftToken = location.getLeftToken();
        return leftToken.getFilename();
    }

    public abstract String getMessage();

    public long lastModified() {
        return lastModified;
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        Message message = (Message) o;

        if(lastModified != message.lastModified)
            return false;
        if(!locationTerm.equals(message.locationTerm))
            return false;
        if(location != null ? !location.equals(message.location) : message.location != null)
            return false;
        return severity == message.severity;
    }

    @Override public int hashCode() {
        int result = locationTerm.hashCode();
        result = 31 * result + (location != null ? location.hashCode() : 0);
        result = 31 * result + severity.hashCode();
        result = 31 * result + (int) (lastModified ^ lastModified >>> 32);
        return result;
    }
}