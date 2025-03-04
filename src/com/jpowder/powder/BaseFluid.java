package com.jpowder.powder;

import com.jpowder.PowderUtilities;

public class BaseFluid extends BasePowder {
    public BaseFluid(int findex, int life, int color) {
        super(ShiftRule.FLUID, findex, color, life);

        canDisplaceHorizontal = true;
    }
}
