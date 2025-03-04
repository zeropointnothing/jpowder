package com.jpowder.powder;

import com.jpowder.PowderUtilities;

public class BaseGas extends BasePowder {
    /**
     * The (random) number needed for this Gas to float.
     */
    public int floatNeeded;
    /**
     * The (random) number needed for this Gas to sink.
     */
    public int sinkNeeded;
    /**
     * The (random) number needed for this Gas to shift horizontally.
     */
    public int shiftNeeded;

    /**
     * The maximum number this Gas should check against for floating.
     */
    public int floatMax = 100;
    /**
     * The maximum number this Gas should check against for sinking.
     */
    public int sinkMax = 100;

    public BaseGas(int findex, int color, int life) {
        super(ShiftRule.GAS, findex, color, life);
    }
}
