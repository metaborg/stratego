package strategolib.strategies;

import strategolib.strategies.binary.BinaryIntegerStrategy;

public class int_ior_0_1 extends BinaryIntegerStrategy {
    public static int_ior_0_1 instance = new int_ior_0_1();

    @Override public int operation(int left, int right) {
        return left | right;
    }
}
