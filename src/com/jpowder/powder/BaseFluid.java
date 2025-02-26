package com.jpowder.powder;

import com.jpowder.PowderUtilities;

public class BaseFluid extends BasePowder {
    public BaseFluid() {
        super(ShiftRule.FLUID, 0);

        color = PowderUtilities.rgbToColorInt(0, 0, 255);
        canDisplaceHorizontal = true;
    }
}
