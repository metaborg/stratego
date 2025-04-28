package strategolib.strategies;

import strategolib.strategies.binary.BinaryRealStrategy;

public class real_subt_0_1 extends BinaryRealStrategy {
    public static final real_subt_0_1 instance = new real_subt_0_1();

    @Override public double operation(double left, double right) {
        return left - right;
    }
}
