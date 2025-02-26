package com.jpowder.powder;

import com.jpowder.PowderUtilities;

public class RockPowder extends BasePowder {
    public RockPowder() {
        super(ShiftRule.SOLID);

        color = PowderUtilities.rgbToColorInt(61, 59, 60);
    }
}
