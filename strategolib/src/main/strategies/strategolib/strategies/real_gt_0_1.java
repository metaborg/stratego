package strategolib.strategies;

import strategolib.strategies.binary.BinaryRealCompStrategy;

public class real_gt_0_1 extends BinaryRealCompStrategy {
    public static final real_gt_0_1 instance = new real_gt_0_1();

    @Override public boolean operation(double left, double right) {
        return left > right;
    }
}
