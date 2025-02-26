package com.jpowder.powder;

public class BaseFluid extends BasePowder {

    public BaseFluid() {
        super(ShiftRule.FLUID);

        color = rgbToColorInt(0, 0, 255);
    }
}
