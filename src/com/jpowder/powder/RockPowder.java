package com.jpowder.powder;

import com.jpowder.PowderUtilities;

public class RockPowder extends BasePowder {
    public RockPowder() {
        super(ShiftRule.SOLID, -1);

        color = PowderUtilities.rgbToColorInt(61, 59, 60);
        canDisplaceHorizontal = false;
        canDisplaceVertical = false;
    }
}
