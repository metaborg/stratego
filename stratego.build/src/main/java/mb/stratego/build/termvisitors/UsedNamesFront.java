package mb.stratego.build.termvisitors;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;

import mb.stratego.build.strincr.ConstructorSignature;
import mb.stratego.build.strincr.StrategySignature;

public class UsedNamesFront extends UsedConstrs {

    private final Deque<Set<String>> scopes = new ArrayDeque<>();
    private final Set<String> inScope = new HashSet<>();

    private final Set<StrategySignature> usedStrategies;
    private final Set<String> usedAmbiguousStrategies;

    private @Nullable String currentTopLevelStrategyName = null;

    public UsedNamesFront(Set<ConstructorSignature> usedConstructors,
        Set<StrategySignature> usedStrategies, Set<String> usedAmbiguousStrategies,
        long lastModified) {
        super(usedConstructors, lastModified);

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
                case "SDefT":
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
                            defs.add(def);
                            inScope.add(def);
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
                            inScope.add(def);
                        }
                        scopes.push(defs);
                    }
                    break;
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
            final @Nullable StrategySignature strategySignature = recognizeCall(sarg);
            if(strategySignature != null && strategySignature.noStrategyArgs == 0
                && strategySignature.noTermArgs == 0) {
                sarg.putAttachment(AmbUseAttachment.INSTANCE);
                usedAmbiguousStrategies.add(strategySignature.name);
            }
        }
    }

    private void registerStratUse(IStrategoTerm term) {
        final @Nullable StrategySignature strategySignature = recognizeCall(term);
        if(strategySignature != null) {
            if(!inScope.contains(strategySignature.name)) {
                usedStrategies.add(strategySignature);
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
        if(TermUtils.isAppl(term, "CallT", 3)) {
            final String name = TermUtils.toJavaStringAt(term.getSubterm(0), 0);
            final IStrategoList sargs = TermUtils.toListAt(term, 1);
            final IStrategoList targs = TermUtils.toListAt(term, 2);
            return new StrategySignature(name, sargs.size(), targs.size());
        } else if(TermUtils.isAppl(term, "Call", 2)) {
            final String name = TermUtils.toJavaStringAt(term.getSubterm(0), 0);
            final IStrategoList sargs = TermUtils.toListAt(term, 1);
            return new StrategySignature(name, sargs.size(), 0);
        } else if(TermUtils.isAppl(term, "CallNoArgs", 2)) {
            final String name = TermUtils.toJavaStringAt(term.getSubterm(0), 0);
            return new StrategySignature(name, 0, 0);
        }
        return null;
    }

    @Override public void postVisit(IStrategoTerm term) {
        leaveScope(term);
        leaveTopLevelStrategy(term);
    }

    private void leaveScope(IStrategoTerm term) {
        if(TermUtils.isAppl(term)) {
            boolean count = false;
            switch(TermUtils.tryGetName(term).orElse("")) {
                case "Let":
                    count = term.getSubtermCount() == 2;
                    // fallthrough
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
                        inScope.removeAll(scopes.pop());
                    }
                    break;
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
                    if(term.getSubtermCount() == 2 && currentTopLevelStrategyName
                        .equals(TermUtils.toJavaStringAt(term, 0))) {
                        currentTopLevelStrategyName = null;
                    }
                    break;
            }
        }
    }

}