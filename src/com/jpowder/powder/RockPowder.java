package com.jpowder.powder;

public class RockPowder extends BasePowder {
    public RockPowder() {
        super(ShiftRule.SOLID);

        color = rgbToColorInt(61, 59, 60);
    }
}
