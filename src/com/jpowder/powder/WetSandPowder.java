package com.jpowder.powder;

import com.jpowder.PowderUtilities;

public class WetSandPowder extends BasePowder {
    public WetSandPowder() {
        super(ShiftRule.STICK, 2);

        color = PowderUtilities.rgbToColorInt(204, 183, 133);
        canDisplaceHorizontal = false;
    }
}
