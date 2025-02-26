package com.jpowder;

/**
 * Several powder related utilities.
 */
public final class PowderUtilities {
    /**
     * Convert an RGB sequence into a JFrame supported RGB-INT value.
     *
     * @param r Red value from 0 to 255
     * @param g Green value from 0 to 255
     * @param b Blue value from 0 to 255
     * @return the converted RGB-INT value.
     * @throws IllegalArgumentException if the supplied RGB value is invalid.
     */
    public static int rgbToColorInt(int r, int g, int b) throws IllegalArgumentException {
        if (!(0 <= r && r <= 255 && 0 <= g && g <= 255 && 0 <= b && b <= 255)) {
            throw new IllegalArgumentException("RGB value (" + r + "," + g + "," + b + ") is not valid!");
        }

        return (r << 16) | (g << 8) | b;
    }
}
