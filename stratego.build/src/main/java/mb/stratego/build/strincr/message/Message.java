package mb.stratego.build.strincr.message;

import java.io.Serializable;
import java.util.Objects;

import jakarta.annotation.Nullable;

import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr2.messages.SourceRegion;
import org.spoofax.terms.attachments.OriginAttachment;
import org.spoofax.terms.util.TermUtils;

import mb.jsglr.shared.IToken;
import mb.jsglr.shared.ImploderAttachment;
import mb.stratego.build.strincr.message.type.AmbiguousConstructorUse;
import mb.stratego.build.strincr.message.type.DuplicateTypeDefinition;
import mb.stratego.build.strincr.message.type.GadtSort;
import mb.stratego.build.strincr.message.type.MatchNotSpecificEnoughForTP;
import mb.stratego.build.strincr.message.type.MissingDefinitionForTypeDefinition;
import mb.stratego.build.strincr.message.type.MissingTypeDefinition;
import mb.stratego.build.strincr.message.type.NoInjectionBetween;
import mb.stratego.build.strincr.message.type.STypeMismatch;
import mb.stratego.build.strincr.message.type.StrategyVariableTypedWithTermType;
import mb.stratego.build.strincr.message.type.TermVariableTypedWithStrategyType;
import mb.stratego.build.strincr.message.type.NoLUBBetween;
import mb.stratego.build.strincr.message.type.VariableBoundToIncompatibleType;
import mb.stratego.build.util.WithLastModified;

public abstract class Message implements WithLastModified, Serializable {
    public final String locationTermString;
    // TODO: require location to be non-null once gradual type system stops losing origins
    public final @Nullable SourceRegion sourceRegion;
    public final @Nullable String filename;
    public final MessageSeverity severity;
    public final long lastModified;

    public Message(IStrategoTerm locationTerm, MessageSeverity severity, long lastModified) {
        if(locationTerm instanceof IStrategoString) {
            this.locationTermString = ((IStrategoString) locationTerm).stringValue();
        } else {
            this.locationTermString = locationTerm.toString();
        }
        @Nullable ImploderAttachment location =
            ImploderAttachment.get(OriginAttachment.tryGetOrigin(locationTerm));
        this.sourceRegion = location == null ? null :
            sourceRegionFromTokens(location.getLeftToken(), location.getRightToken());
        this.filename = location == null ? null : location.getLeftToken().getFilename();
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
            case "NoLUBBetween": // Type * Type -> ErrorDesc
                return new NoLUBBetween(locationTerm, messageTerm.getSubterm(0),
                    messageTerm.getSubterm(1), severity, lastModified);
            case "STypeMismatch": // SType * SType -> ErrorDesc
                return new STypeMismatch(locationTerm, messageTerm.getSubterm(0),
                    messageTerm.getSubterm(1), severity, lastModified);
            case "UnresolvedLocal":
                return new UnresolvedLocal(locationTerm, messageTerm.getSubterm(0), severity, lastModified);
            case "UnresolvedConstructor": // Int * Type -> ErrorDesc
                return new UnresolvedConstructor(locationTerm, TermUtils.toJavaStringAt(messageTerm, 0),
                    TermUtils.toJavaIntAt(messageTerm, 1), messageTerm.getSubterm(2), severity,
                    lastModified);
            case "UnresolvedSort": // Int -> ErrorDesc
                return new UnresolvedSort(locationTerm, messageTerm.getSubterm(0).toString(), TermUtils.toJavaIntAt(messageTerm, 1),
                    severity, lastModified);
            case "UnresolvedSortVar":
                return new UnresolvedSortVar(locationTerm, TermUtils.toJavaStringAt(messageTerm, 0), severity, lastModified);
            case "UnresolvedStrategy":
                return new UnresolvedStrategy(locationTerm, TermUtils.toJavaStringAt(messageTerm, 0), TermUtils.toJavaIntAt(messageTerm, 1),
                    TermUtils.toJavaIntAt(messageTerm, 2), severity, lastModified);
            case "AmbiguousConstructorUse": // List(Type) -> ErrorDesc
                return new AmbiguousConstructorUse(locationTerm,
                    TermUtils.toJavaListAt(messageTerm, 0), severity, lastModified);
            case "AsInBuildTerm":
                return new AsInBuildTerm(locationTerm, severity, lastModified);
            case "WldInBuildTerm":
                return new WldInBuildTerm(locationTerm, severity, lastModified);
            case "AsInOverlay":
                return new AsInOverlay(locationTerm, severity, lastModified);
            case "WldInOverlay":
                return new WldInOverlay(locationTerm, severity, lastModified);
            case "BuildDefaultInBuildTerm":
                return new BuildDefaultInBuildTerm(locationTerm, severity, lastModified);
            case "BuildDefaultInMatchTerm":
                return new BuildDefaultInMatchTerm(locationTerm, severity, lastModified);
            case "StringQuotationInMatchTerm":
                return new StringQuotationInMatchTerm(locationTerm, severity, lastModified);
            case "StringQuotationInOverlay":
                return new StringQuotationInOverlay(locationTerm, severity, lastModified);
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
            case "DynRuleOverlapError":
                return new DynRuleOverlapError(locationTerm,
                    TermUtils.toJavaStringAt(messageTerm, 1),
                    TermUtils.toJavaStringAt(messageTerm, 2),
                    TermUtils.toJavaStringAt(messageTerm, 3),
                    TermUtils.toJavaStringAt(messageTerm, 4), severity, lastModified);
            case "ConstantCongruence":
                return new ConstantCongruence(locationTerm, severity, lastModified);
            case "WithClauseInDynRule":
                return new WithClauseInDynRule(locationTerm, severity, lastModified);
            case "StrategyCongruenceOverlap":
                return new StrategyCongruenceOverlap(locationTerm, severity, lastModified);
            case "GadtSort":
                return new GadtSort(locationTerm, severity, lastModified);
            case "MissingTypeDefinition":
                return new MissingTypeDefinition(locationTerm, severity, lastModified);
            case "MissingParsingInfoOnStringQuotation":
                return new MissingParsingInfoOnStringQuotation(locationTerm, severity, lastModified);
            default:
                return new RawTermMessage(locationTerm, messageTerm, severity, lastModified);
        }
    }

    public String toString() {
        return "In '" + locationString() + "':\n" + getMessage();
    }

    public String locationString() {
        if(filename == null) {
            return "[missing origin info]";
        }
        return filename + ":" + sourceRegion.startRow + ":" + sourceRegion.startColumn
            + " - " + sourceRegion.endRow + ":" + (sourceRegion.endColumn + 1);
    }

    public static SourceRegion sourceRegionFromTokens(IToken left, IToken right) {
        return new SourceRegion(left.getStartOffset(), left.getLine(), left.getColumn(),
            right.getEndOffset(), right.getEndLine(), right.getEndColumn());
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
        if(!locationTermString.equals(message.locationTermString))
            return false;
        if(!Objects.equals(sourceRegion, message.sourceRegion))
            return false;
        if(!Objects.equals(filename, message.filename))
            return false;
        return severity == message.severity;
    }

    @Override public int hashCode() {
        int result = locationTermString.hashCode();
        result = 31 * result + (sourceRegion != null ? sourceRegion.hashCode() : 0);
        result = 31 * result + (filename != null ? filename.hashCode() : 0);
        result = 31 * result + severity.hashCode();
        result = 31 * result + (int) (lastModified ^ lastModified >>> 32);
        return result;
    }
}
