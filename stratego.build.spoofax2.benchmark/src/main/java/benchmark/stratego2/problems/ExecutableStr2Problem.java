package benchmark.stratego2.problems;

import static benchmark.stratego2.problems.InputType.*;

public enum ExecutableStr2Problem {
    Benchexpr10("benchexpr", SNatNum, 10),
    Benchexpr11("benchexpr", SNatNum, 11),
    Benchexpr12("benchexpr", SNatNum, 12),
    Benchexpr13("benchexpr", SNatNum, 13),
    Benchexpr14("benchexpr", SNatNum, 14),
    Benchexpr15("benchexpr", SNatNum, 15),
    Benchexpr16("benchexpr", SNatNum, 16),
    Benchexpr17("benchexpr", SNatNum, 17),
    Benchexpr18("benchexpr", SNatNum, 18),
    Benchexpr19("benchexpr", SNatNum, 19),
    Benchexpr20("benchexpr", SNatNum, 20),
    Benchexpr22("benchexpr", SNatNum, 22),

    Benchsym10("benchsym", SNatNum, 10),
    Benchsym11("benchsym", SNatNum, 11),
    Benchsym12("benchsym", SNatNum, 12),
    Benchsym13("benchsym", SNatNum, 13),
    Benchsym14("benchsym", SNatNum, 14),
    Benchsym15("benchsym", SNatNum, 15),
    Benchsym16("benchsym", SNatNum, 16),
    Benchsym17("benchsym", SNatNum, 17),
    Benchsym18("benchsym", SNatNum, 18),
    Benchsym19("benchsym", SNatNum, 19),
    Benchsym20("benchsym", SNatNum, 20),
    Benchsym22("benchsym", SNatNum, 22),

    Benchtree2("benchtree", SNatNum, 2),
    Benchtree4("benchtree", SNatNum, 4),
    Benchtree6("benchtree", SNatNum, 6),
    Benchtree7("benchtree", SNatNum, 7),
    Benchtree8("benchtree", SNatNum, 8),
    Benchtree10("benchtree", SNatNum, 10),
    Benchtree11("benchtree", SNatNum, 11),
    Benchtree12("benchtree", SNatNum,  12),
    Benchtree13("benchtree", SNatNum, 13),
    Benchtree14("benchtree", SNatNum, 14),
    Benchtree15("benchtree", SNatNum, 15),
    Benchtree16("benchtree", SNatNum, 16),
    Benchtree17("benchtree", SNatNum, 17),
    Benchtree18("benchtree", SNatNum, 18),
    Benchtree19("benchtree", SNatNum, 19),
    Benchtree20("benchtree", SNatNum, 20),
    Benchtree22("benchtree", SNatNum, 22),

    Bubblesort10("bubblesort", NatSD0Num, 10),
    Bubblesort20("bubblesort", NatSD0Num, 20),
    Bubblesort50("bubblesort", NatSD0Num, 50),
    Bubblesort100("bubblesort", NatSD0Num, 100),
    Bubblesort200("bubblesort", NatSD0Num, 200),
    Bubblesort300("bubblesort", NatSD0Num, 300),
    Bubblesort500("bubblesort", NatSD0Num, 500),
    Bubblesort720("bubblesort", NatSD0Num, 720),
    Bubblesort1000("bubblesort", NatSD0Num, 1000),

    Calls("calls", None, 0),

    GarbageCollection("garbagecollection", None, 0),

    Factorial4("factorial", NatSD0Num, 4),
    Factorial5("factorial", NatSD0Num, 5),
    Factorial6("factorial", NatSD0Num, 6),
    Factorial7("factorial", NatSD0Num, 7),
    Factorial8("factorial", NatSD0Num, 8),
    Factorial9("factorial", NatSD0Num, 9),

    Fibonacci18("fibonacci", NatSD0Num, 18),
    Fibonacci19("fibonacci", NatSD0Num, 19),
    Fibonacci20("fibonacci", NatSD0Num, 20),
    Fibonacci21("fibonacci", NatSD0Num, 21),

    Hanoi4("hanoi", DNatNum, 4),
    Hanoi5("hanoi", DNatNum, 5),
    Hanoi6("hanoi", DNatNum, 6),
    Hanoi7("hanoi", DNatNum, 7),
    Hanoi8("hanoi", DNatNum, 8),
    Hanoi9("hanoi", DNatNum, 9),
    Hanoi10("hanoi", DNatNum, 10),
    Hanoi11("hanoi", DNatNum, 11),
    Hanoi12("hanoi", DNatNum, 12),
    Hanoi16("hanoi", DNatNum, 16),
    Hanoi20("hanoi", DNatNum, 20),

    Mergesort10("mergesort", NatSD0Num, 10),
    Mergesort20("mergesort", NatSD0Num, 20),
    Mergesort30("mergesort", NatSD0Num, 30),
    Mergesort40("mergesort", NatSD0Num, 40),
    Mergesort50("mergesort", NatSD0Num, 50),
    Mergesort100("mergesort", NatSD0Num, 100),
    Mergesort200("mergesort", NatSD0Num, 200),
    Mergesort300("mergesort", NatSD0Num, 300),
    Mergesort500("mergesort", NatSD0Num, 500),
    Mergesort720("mergesort", NatSD0Num, 720),
    Mergesort1000("mergesort", NatSD0Num, 1000),

    Quicksort10("quicksort", NatSD0Num, 10),
    Quicksort12("quicksort", NatSD0Num, 12),
    Quicksort14("quicksort", NatSD0Num, 14),
    Quicksort16("quicksort", NatSD0Num, 16),
    Quicksort18("quicksort", NatSD0Num, 18),
    Quicksort20("quicksort", NatSD0Num, 20),
    Quicksort100("quicksort", NatSD0Num, 100),
    Quicksort1000("quicksort", NatSD0Num, 1000),

    Sieve20("sieve", NatSZNum, 20),
    Sieve40("sieve", NatSZNum, 40),
    Sieve60("sieve", NatSZNum, 60),
    Sieve80("sieve", NatSZNum, 80),
    Sieve100("sieve", NatSZNum, 100),
    Sieve1000("sieve", NatSZNum, 1000),
    Sieve2000("sieve", NatSZNum, 2000),
    Sieve100000("sieve", NatSZNum, 100_000);

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
