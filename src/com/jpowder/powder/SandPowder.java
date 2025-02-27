package com.jpowder.powder;

import com.jpowder.PowderUtilities;

public class SandPowder extends BasePowder {
    public SandPowder() {
        super(ShiftRule.SLIP, 2);

        color = PowderUtilities.rgbToColorInt(246, 225, 176);
        canDisplaceHorizontal = true;
    }
}
