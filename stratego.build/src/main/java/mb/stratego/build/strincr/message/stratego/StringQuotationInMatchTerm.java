package mb.stratego.build.strincr.message.stratego;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.stratego.build.strincr.message.MessageSeverity;
import mb.stratego.build.strincr.message.StrategoMessage;

public class StringQuotationInMatchTerm extends StrategoMessage {
    public StringQuotationInMatchTerm(String module, IStrategoTerm locationTerm, MessageSeverity severity) {
        super(module, locationTerm, severity);
    }

    @Override
    public String getMessageWithoutLocation() {
        return "The string quotation pattern may not be used in match context.";
    }
}
