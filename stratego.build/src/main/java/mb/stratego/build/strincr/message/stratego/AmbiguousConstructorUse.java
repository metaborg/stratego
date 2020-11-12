package mb.stratego.build.strincr.message.stratego;

import java.util.List;
import java.util.StringJoiner;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.stratego.build.strincr.MessageSeverity;
import mb.stratego.build.strincr.message.StrategoMessage;

public class AmbiguousConstructorUse extends StrategoMessage {
    public final List<IStrategoTerm> sorts;

    public AmbiguousConstructorUse(String module, IStrategoTerm locationTerm, List<IStrategoTerm> sorts,
        MessageSeverity severity) {
        super(module, locationTerm, severity);
        this.sorts = sorts;
    }

    @Override
    public String getMessageWithoutLocation() {
        StringJoiner types = new StringJoiner(", ");
        for(IStrategoTerm sort : sorts) {
            String toString = sort.toString();
            types.add(toString);
        }
        return "Ambiguous use of constructor, could be of the following types: " + types.toString();
    }
}
