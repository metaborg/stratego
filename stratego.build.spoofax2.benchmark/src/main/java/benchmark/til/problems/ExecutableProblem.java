package benchmark.til.problems;

import java.util.Arrays;
import java.util.Collection;

public enum ExecutableProblem {
    Add100("add100", "0"),
    Add200("add200", "0"),
    Add500("add500", "0"),
    Add1000("add1000", "0"),
    EBlock("eblock", "1,1"),
    Factorial4("factorial", "4"),
    Factorial5("factorial", "5"),
    Factorial6("factorial", "6"),
    Factorial7("factorial", "7"),
    Factorial8("factorial", "8"),
    Factorial9("factorial", "9");

    public final String name;
    public final Collection<String> input;
    ExecutableProblem(String name, String input) {
        this.name = name;
        this.input = Arrays.asList(input.split(","));
    }
}
