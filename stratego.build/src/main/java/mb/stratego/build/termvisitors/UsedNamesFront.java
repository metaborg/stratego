package mb.stratego.build.termvisitors;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

import jakarta.annotation.Nullable;

import org.spoofax.interpreter.terms.IStrategoInt;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.StrategoInt;
import org.spoofax.terms.util.TermUtils;

import mb.stratego.build.strincr.data.ConstructorSignature;
import mb.stratego.build.strincr.data.StrategySignature;

public class UsedNamesFront extends UsedConstrs {

    private final Deque<Set<String>> scopes = new ArrayDeque<>();
    private final Set<String> sVars = new HashSet<>();
    private final Set<StrategySignature> letBoundStrats = new HashSet<>();

    private final Set<StrategySignature> usedStrategies;
    private final Set<String> usedAmbiguousStrategies;

    private @Nullable String currentTopLevelStrategyName = null;

    public UsedNamesFront(Set<ConstructorSignature> usedConstructors,
        Set<StrategySignature> usedStrategies, Set<String> usedAmbiguousStrategies) {
        super(usedConstructors);

        this.usedStrategies = usedStrategies;
        this.usedAmbiguousStrategies = usedAmbiguousStrategies;
    }

    @Override public void preVisit(IStrategoTerm term) {
        enterTopLevelStrategy(term);
        enterScope(term);
        registerConsUse(term);
        registerStratUse(term);
    }

    private void enterTopLevelStrategy(IStrategoTerm term) {
        if(currentTopLevelStrategyName == null && TermUtils.isAppl(term)) {
            switch(TermUtils.toAppl(term).getName()) {
                case "ExtTypedDefInl":
                    if(term.getSubtermCount() == 5) {
                        currentTopLevelStrategyName = TermUtils.toJavaStringAt(term, 0);
                    }
                    break;
                case "SDefT":
                case "SDefP":
                case "ExtSDefInl":
                case "RDefT":
                case "RDefP":
                    if(term.getSubtermCount() == 4) {
                        currentTopLevelStrategyName = TermUtils.toJavaStringAt(term, 0);
                    }
                    break;
                case "SDef":
                case "ExtSDef":
                case "RDef":
                    if(term.getSubtermCount() == 3) {
                        currentTopLevelStrategyName = TermUtils.toJavaStringAt(term, 0);
                    }
                    break;
                case "ExtTypedDef":
                case "SDefNoArgs":
                case "RDefNoArgs":
                    if(term.getSubtermCount() == 2) {
                        currentTopLevelStrategyName = TermUtils.toJavaStringAt(term, 0);
                    }
                    break;
            }
        }
    }

    private void enterScope(IStrategoTerm term) {
        if(TermUtils.isAppl(term)) {
            boolean count = false;
            switch(TermUtils.toAppl(term).getName()) {
                case "Let":
                    if(term.getSubtermCount() == 2) {
                        final IStrategoList defList = TermUtils.toListAt(term, 0);
                        final Set<String> defs = new HashSet<>(defList.size() * 2);
                        for(IStrategoTerm sdeft : defList) {
                            if(TermUtils.isAppl(sdeft, "AnnoDef", 2)) {
                                sdeft = sdeft.getSubterm(1);
                            }
                            final String def = TermUtils.toJavaStringAt(sdeft, 0);
                            final @Nullable StrategySignature sig =
                                StrategySignature.fromDefinition(sdeft);
                            assert sig != null;
                            defs.add(def);
                            letBoundStrats.add(sig);
                        }
                        scopes.push(defs);
                    }
                    break;
                case "SDefT":
                case "ExtSDefInl":
                case "RDefT":
                case "RDefP":
                    count = term.getSubtermCount() == 4;
                    //fall-through
                case "ExtTypedDefInl":
                    count |= term.getSubtermCount() == 5;
                    //fall-through
                case "SDef":
                case "ExtSDef":
                case "RDef":
                    count |= term.getSubtermCount() == 3;
                    if(count) {
                        final IStrategoList svars = TermUtils.toListAt(term, 1);
                        final Set<String> defs = new HashSet<>(svars.size() * 2);
                        for(IStrategoTerm svar : svars) {
                            final String def = TermUtils.toJavaStringAt(svar, 0);
                            defs.add(def);
                            sVars.add(def);
                        }
                        scopes.push(defs);
                    }
                    break;
                case "ExtTypedDef":
                case "SDefNoArgs":
                case "RDefNoArgs":
                    // no-op
                    break;
            }
        }
    }

    private void registerAmbStratUse(IStrategoTerm term) {
        final IStrategoList sargs = TermUtils.toListAt(term, 1);
        for(IStrategoTerm sarg : sargs) {
            final @Nullable StrategySignature strategySignature = StrategySignature.fromCall(sarg);
            if(strategySignature != null && strategySignature.noStrategyArgs == 0
                && strategySignature.noTermArgs == 0 && isNonLocalStrategy(strategySignature)) {
                sarg.putAttachment(AmbUseAttachment.INSTANCE);
                usedAmbiguousStrategies.add(strategySignature.name);
            }
        }
    }

    private void registerStratUse(IStrategoTerm term) {
        final @Nullable StrategySignature strategySignature = recognizeCall(term);
        if(strategySignature != null) {
            if(isNonLocalStrategy(strategySignature)) {
                usedStrategies.add(strategySignature);
                if(strategySignature.noTermArgs == 0) {
                    IStrategoString name = TermUtils.toStringAt(strategySignature, 0);
                    IStrategoInt noArgs = new StrategoInt(strategySignature.noStrategyArgs);
                    usedConstructors.add(new ConstructorSignature(name, noArgs));
                }
            }
            if(strategySignature.noStrategyArgs != 0) {
                registerAmbStratUse(term);
            }
        }
    }

    private static @Nullable StrategySignature recognizeCall(IStrategoTerm term) {
        if(term.getAttachment(AmbUseAttachment.TYPE) != null) {
            return null;
        }
        return StrategySignature.fromCall(term);
    }

    @Override public void postVisit(IStrategoTerm term) {
        if(term.getAttachment(AmbUseAttachment.TYPE) != null) {
            term.removeAttachment(AmbUseAttachment.TYPE);
        }
        leaveScope(term);
        leaveTopLevelStrategy(term);
    }

    private boolean isNonLocalStrategy(StrategySignature sig) {
        return !sVars.contains(sig.name) && !letBoundStrats.contains(sig);
    }

    private void leaveScope(IStrategoTerm term) {
        if(TermUtils.isAppl(term)) {
            boolean count = false;
            switch(TermUtils.tryGetName(term).orElse("")) {
                case "Let":
                    count = term.getSubtermCount() == 2;
                    // fallthrough
                case "ExtTypedDefInl":
                    count |= term.getSubtermCount() == 5;
                    //fall-through
                case "SDefT":
                case "ExtSDefInl":
                case "RDefT":
                case "RDefP":
                    count |= term.getSubtermCount() == 4;
                    //fall-through
                case "SDef":
                case "ExtSDef":
                case "RDef":
                    count |= term.getSubtermCount() == 3;
                    if(count) {
                        sVars.removeAll(scopes.pop());
                    }
                    break;
                case "ExtTypedDef":
                case "SDefNoArgs":
                case "RDefNoArgs":
                    // no-op
                    break;
            }
        }
    }

    private void leaveTopLevelStrategy(IStrategoTerm term) {
        if(currentTopLevelStrategyName != null && TermUtils.isAppl(term)) {
            switch(TermUtils.toAppl(term).getName()) {
                case "ExtTypedDefInl":
                    if(term.getSubtermCount() == 5 && currentTopLevelStrategyName
                        .equals(TermUtils.toJavaStringAt(term, 0))) {
                        currentTopLevelStrategyName = null;
                    }
                    break;
                case "SDefT":
                case "ExtSDefInl":
                case "RDefT":
                case "RDefP":
                    if(term.getSubtermCount() == 4 && currentTopLevelStrategyName
                        .equals(TermUtils.toJavaStringAt(term, 0))) {
                        currentTopLevelStrategyName = null;
                    }
                    break;
                case "SDef":
                case "ExtSDef":
                case "RDef":
                    if(term.getSubtermCount() == 3 && currentTopLevelStrategyName
                        .equals(TermUtils.toJavaStringAt(term, 0))) {
                        currentTopLevelStrategyName = null;
                    }
                    break;
                case "SDefNoArgs":
                case "RDefNoArgs":
                case "ExtTypedDef":
                    if(term.getSubtermCount() == 2 && currentTopLevelStrategyName
                        .equals(TermUtils.toJavaStringAt(term, 0))) {
                        currentTopLevelStrategyName = null;
                    }
                    break;
            }
        }
    }

}
