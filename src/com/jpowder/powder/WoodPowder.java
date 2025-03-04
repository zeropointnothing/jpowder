package com.jpowder.powder;

import com.jpowder.PowderUtilities;

public class WoodPowder extends BasePowder {
    public WoodPowder() {
        super(ShiftRule.SOLID, -1, PowderUtilities.rgbToColorInt(225, 145, 39), 0);

        canDisplaceHorizontal = false;
        canDisplaceVertical = false;
    }
}
