package com.jpowder.powder;

import com.jpowder.PowderUtilities;

public class FireGas extends BaseGas {
    public FireGas() {
        super(1, PowderUtilities.rgbToColorInt(242, 78, 13), 255);

        floatNeeded = 80;
        shiftNeeded = 95;
        sinkNeeded = 100;
    }
}
