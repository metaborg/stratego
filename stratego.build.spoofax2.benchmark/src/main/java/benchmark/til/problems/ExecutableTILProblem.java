package benchmark.til.problems;

import java.util.Arrays;
import java.util.Collection;

public enum ExecutableTILProblem {
    Add_100("add100", "0"),
    Add_200("add200", "0"),
    Add_500("add500", "0"),
    Add_1000("add1000", "0"),
    EBlock("eblock", "1,1"),
    Factorial_4("factorial", "4"),
    Factorial_5("factorial", "5"),
    Factorial_6("factorial", "6"),
    Factorial_7("factorial", "7"),
    Factorial_8("factorial", "8"),
    Factorial_9("factorial", "9");

    public final String name;
    public final Collection<String> input;
    ExecutableTILProblem(String name, String input) {
        this.name = name;
        this.input = Arrays.asList(input.split(","));
    }
}
