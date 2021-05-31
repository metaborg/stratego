package mb.stratego.build.strincr.message.type;

import java.util.List;
import java.util.StringJoiner;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.stratego.build.strincr.message.MessageSeverity;

public class AmbiguousConstructorUse extends TypeMessage<IStrategoTerm> {
    public final List<IStrategoTerm> sorts;

    public AmbiguousConstructorUse(IStrategoTerm locationTerm, List<IStrategoTerm> sorts,
        MessageSeverity severity, long lastModified) {
        super(locationTerm, severity, lastModified);
        this.sorts = sorts;
    }

    @Override
    public String getMessage() {
        StringJoiner types = new StringJoiner(", ");
        for(IStrategoTerm sort : sorts) {
            String toString = sort.toString();
            types.add(toString);
        }
        return "Ambiguous use of constructor, could be of the following types: " + types.toString();
    }
}
