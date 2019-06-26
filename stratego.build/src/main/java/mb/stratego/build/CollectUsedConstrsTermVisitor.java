package mb.stratego.build;

import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.TermVisitor;
import java.util.Set;

public class CollectUsedConstrsTermVisitor extends TermVisitor {
    private final Set<String> usedConstrs;

    CollectUsedConstrsTermVisitor(Set<String> usedConstrs) {
        this.usedConstrs = usedConstrs;
    }

    @Override public void preVisit(IStrategoTerm term) {
        registerConsUse(term);
    }

    void registerConsUse(IStrategoTerm term) {
        if(Tools.isTermAppl(term) && Tools.hasConstructor((IStrategoAppl) term, "Op", 2)) {
            if(Tools.isTermString(term.getSubterm(0))) {
                if(!Tools.javaStringAt(term, 0).equals("")) {
                    usedConstrs.add(Tools.javaStringAt(term, 0) + "_" + Tools.listAt(term, 1).size());
                }
            } else {
                usedConstrs.add(
                    strategoEscape(Tools.javaStringAt(Tools.<IStrategoTerm>termAt(term, 0), 0)) + Tools.listAt(term, 1)
                        .size());
            }
        }
    }

    private String strategoEscape(String s) {
        //@formatter:off
        return s
            .replace("\"", "\\\"")
            .replace("\\", "\\\\")
            .replace("\n", "\\n")
            .replace("\r", "\\r");
        //@formatter:on
    }
}
