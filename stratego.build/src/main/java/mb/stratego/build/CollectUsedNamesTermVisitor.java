package mb.stratego.build;

import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CollectUsedNamesTermVisitor extends CollectUsedConstrsTermVisitor {

    private final Deque<Set<String>> scopes = new ArrayDeque<>();
    private final Set<String> inScope = new HashSet<>();

    private final Set<String> usedStrats;
    private final Map<String, Set<String>> usedAmbStrats;

    private @Nullable String currentTopLevelStrategyName = null;

    CollectUsedNamesTermVisitor(Set<String> usedConstrs, Set<String> usedStrats,
        Map<String, Set<String>> usedAmbStrats) {
        super(usedConstrs);

        this.usedStrats = usedStrats;
        this.usedAmbStrats = usedAmbStrats;
    }

    @Override public void preVisit(IStrategoTerm term) {
        enterTopLevelStrategy(term);
        enterScope(term);
        registerConsUse(term);
        registerAmbStratUse(term);
        registerStratUse(term);
    }

    private void enterTopLevelStrategy(IStrategoTerm term) {
        if(currentTopLevelStrategyName == null && Tools.isTermAppl(term) && Tools
            .hasConstructor((IStrategoAppl) term, "SDefT", 4)) {
            currentTopLevelStrategyName = Tools.javaStringAt(term, 0);
        }
    }

    private void enterScope(IStrategoTerm term) {
        if(Tools.isTermAppl(term)) {
            boolean count = false;
            switch(Tools.constructorName(term)) {
                case "Let":
                    if(term.getSubtermCount() == 2) {
                        final IStrategoList defList = Tools.listAt(term, 0);
                        final Set<String> defs = new HashSet<>(defList.size() * 2);
                        for(IStrategoTerm sdeft : defList) {
                            if(Tools.hasConstructor((IStrategoAppl) sdeft, "AnnoDef", 2)) {
                                sdeft = sdeft.getSubterm(1);
                            }
                            final String def = Tools.javaStringAt(sdeft, 0);
                            defs.add(def);
                            inScope.add(def);
                        }
                        scopes.push(defs);
                    }
                    break;
                case "ExtSDef":
                    count = term.getSubtermCount() == 3;
                case "SDefT":
                    count |= term.getSubtermCount() == 4;
                case "ExtSDefInl":
                    count |= term.getSubtermCount() == 4;
                    if(count) {
                        final IStrategoList svars = Tools.listAt(term, 1);
                        final Set<String> defs = new HashSet<>(svars.size() * 2);
                        for(IStrategoTerm tvar : svars) {
                            final String def = Tools.javaStringAt(tvar, 0);
                            defs.add(def);
                            inScope.add(def);
                        }
                        scopes.push(defs);
                    }
                    break;
            }
        }
    }

    private void registerAmbStratUse(IStrategoTerm term) {
        if(Tools.isTermAppl(term) && Tools.hasConstructor((IStrategoAppl) term, "CallT", 3)) {
            final IStrategoList sargs = Tools.listAt(term, 1);
            for(IStrategoTerm sarg : sargs) {
                if(Tools.isTermAppl(sarg) && Tools.hasConstructor((IStrategoAppl) sarg, "CallT", 3)) {
                    if(sarg.getSubterm(1).getSubtermCount() == 0 && sarg.getSubterm(2).getSubtermCount() == 0) {
                        // Mark svar so it is not counted as a normal strategy use
                        sarg.getSubterm(0).putAttachment(AmbUseAttachment.INSTANCE);

                        final String ambName = Tools.javaStringAt(Tools.applAt(sarg, 0), 0);
                        if(!ambName.endsWith("_0_0")) {
                            // Inner strategies that were lifted don't have any arity info in their name and aren't ambiguous uses
                            if(!StrIncr.stripArityPattern.matcher(ambName).matches()) {
                                continue;
                            }
                        }
                        StrIncr.getOrInitialize(usedAmbStrats, ambName, HashSet::new).add(currentTopLevelStrategyName);
                    }
                }
            }
        }
    }

    private void registerStratUse(IStrategoTerm term) {
        if(Tools.isTermAppl(term) && Tools.hasConstructor((IStrategoAppl) term, "SVar", 1)
            && term.getAttachment(AmbUseAttachment.TYPE) == null) {
            String strategyName = Tools.javaStringAt(term, 0);
            if(!inScope.contains(strategyName)) {
                usedStrats.add(strategyName);
            }
        }
    }

    @Override public void postVisit(IStrategoTerm term) {
        leaveScope(term);
        leaveTopLevelStrategy(term);
    }

    private void leaveScope(IStrategoTerm term) {
        if(Tools.isTermAppl(term)) {
            boolean count = false;
            switch(Tools.constructorName(term)) {
                case "Let":
                    count = term.getSubtermCount() == 2;
                case "ExtSDef":
                    count |= term.getSubtermCount() == 3;
                case "SDefT":
                    count |= term.getSubtermCount() == 4;
                case "ExtSDefInl":
                    count |= term.getSubtermCount() == 4;
                    if(count) {
                        inScope.removeAll(scopes.pop());
                    }
                    break;
            }
        }
    }

    private void leaveTopLevelStrategy(IStrategoTerm term) {
        if(currentTopLevelStrategyName != null && Tools.isTermAppl(term) && Tools
            .hasConstructor((IStrategoAppl) term, "SDefT", 4) && currentTopLevelStrategyName
            .equals(Tools.javaStringAt(term, 0))) {
            currentTopLevelStrategyName = null;
        }
    }

}