package com.jpowder.powder;

import com.jpowder.PowderUtilities;

public class WetSandPowder extends BasePowder {
    public WetSandPowder() {
        super(ShiftRule.STICK, 2);

        color = PowderUtilities.rgbToColorInt(149, 116, 73);
        canDisplaceHorizontal = false;
    }
}
