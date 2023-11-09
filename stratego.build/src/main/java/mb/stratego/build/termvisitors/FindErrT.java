package mb.stratego.build.termvisitors;

import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.Nullable;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.TermVisitor;
import org.spoofax.terms.util.TermUtils;

import mb.stratego.build.strincr.data.StrategySignature;

public class FindErrT extends TermVisitor {
    public final List<StrategySignature> definitions = new ArrayList<>();

    private @Nullable StrategySignature currentTopLevelStrategyName = null;

    public static List<StrategySignature> findErrT(IStrategoTerm ast) {
        final FindErrT termSizeTermVisitor = new FindErrT();
        termSizeTermVisitor.visit(ast);
        return termSizeTermVisitor.definitions;
    }

    @Override public void preVisit(IStrategoTerm term) {
        enterTopLevelStrategy(term);
        registerErrT(term);
    }

    @Override public void postVisit(IStrategoTerm term) {
        leaveTopLevelStrategy(term);
    }

    private void registerErrT(IStrategoTerm term) {
        if(TermUtils.isAppl(term, "ErrT", 0)) {
            definitions.add(currentTopLevelStrategyName);
        }
        if(TermUtils.isAppl(term, "Cast", 1) && TermUtils.isApplAt(term, 0, "Fail", 0)) {
            definitions.add(currentTopLevelStrategyName);
        }
        if(TermUtils.isAppl(term, "SFail", 0)) {
            definitions.add(currentTopLevelStrategyName);
        }
    }

    private void enterTopLevelStrategy(IStrategoTerm term) {
        if(currentTopLevelStrategyName == null && TermUtils.isAppl(term)) {
            currentTopLevelStrategyName = StrategySignature.fromDefinition(term);
        }
    }

    private void leaveTopLevelStrategy(IStrategoTerm term) {
        if(currentTopLevelStrategyName != null && currentTopLevelStrategyName.equals(StrategySignature.fromDefinition(term))) {
            currentTopLevelStrategyName = null;
        }
    }
}
