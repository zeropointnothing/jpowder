package com.jpowder.powder;

public class SandPowder extends BasePowder {
    public SandPowder() {
        super(ShiftRule.SLIP);

        color = rgbToColorInt(246, 225, 176);
    }
}
