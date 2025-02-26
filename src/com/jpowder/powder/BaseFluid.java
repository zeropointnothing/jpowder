package com.jpowder.powder;

import com.jpowder.PowderUtilities;

public class BaseFluid extends BasePowder {

    public BaseFluid() {
        super(ShiftRule.FLUID);

        color = PowderUtilities.rgbToColorInt(0, 0, 255);
    }
}
