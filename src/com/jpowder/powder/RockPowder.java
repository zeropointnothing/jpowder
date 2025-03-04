package com.jpowder.powder;

import com.jpowder.PowderUtilities;

public class RockPowder extends BasePowder {
    public RockPowder() {
        super(ShiftRule.SOLID, -1, PowderUtilities.rgbToColorInt(61, 59, 60), 0);

        canDisplaceHorizontal = false;
        canDisplaceVertical = false;
    }
}
