package mb.stratego.build.strincr.task.output;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.TreeSet;

import mb.resource.hierarchical.ResourcePath;
import mb.stratego.build.strincr.data.StrategySignature;

public class CompileDynamicRulesOutput implements Serializable {
    public final TreeSet<StrategySignature> newGenerated;
    public final TreeSet<StrategySignature> undefineGenerated;
    public final LinkedHashSet<ResourcePath> resultFiles;
    protected final int hashCode;

    public CompileDynamicRulesOutput(TreeSet<StrategySignature> compiledThroughDynamicRule,
        Collection<StrategySignature> dynamicRules, LinkedHashSet<ResourcePath>  resultFiles) {
        this.resultFiles = resultFiles;
        this.newGenerated = new TreeSet<>();
        this.undefineGenerated = new TreeSet<>();
        for(StrategySignature dynamicRule : dynamicRules) {
            final StrategySignature dynamicRuleNew =
                new StrategySignature("new-" + dynamicRule.name, 0, 2);
            if(compiledThroughDynamicRule.contains(dynamicRuleNew)) {
                newGenerated.add(dynamicRule);
            }
            final StrategySignature dynamicRuleUndefine =
                new StrategySignature("undefine-" + dynamicRule.name, 0, 1);
            if(compiledThroughDynamicRule.contains(dynamicRuleUndefine)) {
                undefineGenerated.add(dynamicRule);
            }
        }
        this.hashCode = hashFunction();
    }

    @Override public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;

        CompileDynamicRulesOutput that = (CompileDynamicRulesOutput) o;

        if(hashCode != that.hashCode)
            return false;
        if(!newGenerated.equals(that.newGenerated))
            return false;
        if(!undefineGenerated.equals(that.undefineGenerated))
            return false;
        return resultFiles.equals(that.resultFiles);
    }

    @Override public int hashCode() {
        return this.hashCode;
    }

    protected int hashFunction() {
        int result = newGenerated.hashCode();
        result = 31 * result + undefineGenerated.hashCode();
        result = 31 * result + resultFiles.hashCode();
        return result;
    }

    @Override public String toString() {
        //@formatter:off
        return "CompileDynamicRulesOutput@" + System.identityHashCode(this) + '{'
            + "newGenerated=" + newGenerated.size()
            + ", undefineGenerated=" + undefineGenerated.size()
            + ", resultFiles=" + resultFiles.size()
            + '}';
        //@formatter:on
    }
}
