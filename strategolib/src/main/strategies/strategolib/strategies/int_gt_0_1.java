package strategolib.strategies;

import strategolib.strategies.binary.BinaryIntegerCompStrategy;

public class int_gt_0_1 extends BinaryIntegerCompStrategy {
    public static int_gt_0_1 instance = new int_gt_0_1();

    @Override public boolean operation(int left, int right) {
        return left > right;
    }
}
