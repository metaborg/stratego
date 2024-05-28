package benchmark.stratego2.problems;

import static benchmark.stratego2.problems.InputType.*;

public enum ExecutableStr2Problem {
    Benchexpr_10("benchexpr", SNatNum, 10),
    Benchexpr_11("benchexpr", SNatNum, 11),
    Benchexpr_12("benchexpr", SNatNum, 12),
    Benchexpr_13("benchexpr", SNatNum, 13),
    Benchexpr_14("benchexpr", SNatNum, 14),
    Benchexpr_15("benchexpr", SNatNum, 15),
    Benchexpr_16("benchexpr", SNatNum, 16),
    Benchexpr_17("benchexpr", SNatNum, 17),
    Benchexpr_18("benchexpr", SNatNum, 18),
    Benchexpr_19("benchexpr", SNatNum, 19),
    Benchexpr_20("benchexpr", SNatNum, 20),
    Benchexpr_22("benchexpr", SNatNum, 22),

    Benchsym_10("benchsym", SNatNum, 10),
    Benchsym_11("benchsym", SNatNum, 11),
    Benchsym_12("benchsym", SNatNum, 12),
    Benchsym_13("benchsym", SNatNum, 13),
    Benchsym_14("benchsym", SNatNum, 14),
    Benchsym_15("benchsym", SNatNum, 15),
    Benchsym_16("benchsym", SNatNum, 16),
    Benchsym_17("benchsym", SNatNum, 17),
    Benchsym_18("benchsym", SNatNum, 18),
    Benchsym_19("benchsym", SNatNum, 19),
    Benchsym_20("benchsym", SNatNum, 20),
    Benchsym_22("benchsym", SNatNum, 22),

    Benchtree_2("benchtree", SNatNum, 2),
    Benchtree_4("benchtree", SNatNum, 4),
    Benchtree_6("benchtree", SNatNum, 6),
    Benchtree_7("benchtree", SNatNum, 7),
    Benchtree_8("benchtree", SNatNum, 8),
    Benchtree_10("benchtree", SNatNum, 10),
    Benchtree_11("benchtree", SNatNum, 11),
    Benchtree_12("benchtree", SNatNum,  12),
    Benchtree_13("benchtree", SNatNum, 13),
    Benchtree_14("benchtree", SNatNum, 14),
    Benchtree_15("benchtree", SNatNum, 15),
    Benchtree_16("benchtree", SNatNum, 16),
    Benchtree_17("benchtree", SNatNum, 17),
    Benchtree_18("benchtree", SNatNum, 18),
    Benchtree_19("benchtree", SNatNum, 19),
    Benchtree_20("benchtree", SNatNum, 20),
    Benchtree_22("benchtree", SNatNum, 22),

    Bubblesort_10("bubblesort", NatSD0Num, 10),
    Bubblesort_20("bubblesort", NatSD0Num, 20),
    Bubblesort_50("bubblesort", NatSD0Num, 50),
    Bubblesort_100("bubblesort", NatSD0Num, 100),
    Bubblesort_200("bubblesort", NatSD0Num, 200),
    Bubblesort_300("bubblesort", NatSD0Num, 300),
    Bubblesort_500("bubblesort", NatSD0Num, 500),
    Bubblesort_720("bubblesort", NatSD0Num, 720),
    Bubblesort_1000("bubblesort", NatSD0Num, 1000),

    Calls("calls", None, 0),

    GarbageCollection("garbagecollection", None, 0),

    Factorial_4("factorial", NatSD0Num, 4),
    Factorial_5("factorial", NatSD0Num, 5),
    Factorial_6("factorial", NatSD0Num, 6),
    Factorial_7("factorial", NatSD0Num, 7),
    Factorial_8("factorial", NatSD0Num, 8),
    Factorial_9("factorial", NatSD0Num, 9),

    Fibonacci_18("fibonacci", NatSD0Num, 18),
    Fibonacci_19("fibonacci", NatSD0Num, 19),
    Fibonacci_20("fibonacci", NatSD0Num, 20),
    Fibonacci_21("fibonacci", NatSD0Num, 21),

    Hanoi_4("hanoi", DNatNum, 4),
    Hanoi_5("hanoi", DNatNum, 5),
    Hanoi_6("hanoi", DNatNum, 6),
    Hanoi_7("hanoi", DNatNum, 7),
    Hanoi_8("hanoi", DNatNum, 8),
    Hanoi_9("hanoi", DNatNum, 9),
    Hanoi_10("hanoi", DNatNum, 10),
    Hanoi_11("hanoi", DNatNum, 11),
    Hanoi_12("hanoi", DNatNum, 12),
    Hanoi_16("hanoi", DNatNum, 16),
    Hanoi_20("hanoi", DNatNum, 20),

    Mergesort_10("mergesort", NatSD0Num, 10),
    Mergesort_20("mergesort", NatSD0Num, 20),
    Mergesort_30("mergesort", NatSD0Num, 30),
    Mergesort_40("mergesort", NatSD0Num, 40),
    Mergesort_50("mergesort", NatSD0Num, 50),
    Mergesort_100("mergesort", NatSD0Num, 100),
    Mergesort_200("mergesort", NatSD0Num, 200),
    Mergesort_300("mergesort", NatSD0Num, 300),
    Mergesort_500("mergesort", NatSD0Num, 500),
    Mergesort_720("mergesort", NatSD0Num, 720),
    Mergesort_1000("mergesort", NatSD0Num, 1000),

    Quicksort_10("quicksort", NatSD0Num, 10),
    Quicksort_12("quicksort", NatSD0Num, 12),
    Quicksort_14("quicksort", NatSD0Num, 14),
    Quicksort_16("quicksort", NatSD0Num, 16),
    Quicksort_18("quicksort", NatSD0Num, 18),
    Quicksort_20("quicksort", NatSD0Num, 20),
    Quicksort_100("quicksort", NatSD0Num, 100),
    Quicksort_1000("quicksort", NatSD0Num, 1000),

    Sieve_20("sieve", NatSZNum, 20),
    Sieve_40("sieve", NatSZNum, 40),
    Sieve_60("sieve", NatSZNum, 60),
    Sieve_80("sieve", NatSZNum, 80),
    Sieve_100("sieve", NatSZNum, 100),
    Sieve_1000("sieve", NatSZNum, 1000),
    Sieve_2000("sieve", NatSZNum, 2000),
    Sieve_100000("sieve", NatSZNum, 100_000);

    public final String name;
    public final String input;
    private final int size; // for debug purposes

    ExecutableStr2Problem(String name, InputType inputType, int size) {
        this.name = name;
        this.input = InputType.constructInput(inputType, size).toString(Integer.MAX_VALUE);
        this.size = size;
    }

    public String toString() {
        return String.format("%s(%d)", name, size);
    }
}
