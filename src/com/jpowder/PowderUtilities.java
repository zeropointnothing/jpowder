package com.jpowder;

import java.awt.Color;

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

    /**
     * Convert an RGB-INT sequence into an RGB AWT Color object.
     * @param color The color int to revert
     * @return an AWT Color object
     */
    public static Color colorIntToRGB(int color) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        return new Color(r, g, b);
    }

    /**
     * Invert an RGB-INT sequence.
     * @param color The color int to invert
     * @return the inverted color int
     */
    public static int invertColorInt(int color) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        int ir = enhanceContrast(255 - r);
        int ig = enhanceContrast(255 - g);
        int ib = enhanceContrast(255 - b);

        return (ir << 16) | (ig << 8) | ib;
    }
    private static int enhanceContrast(int value) {
        if (value < 128) {
            return Math.max(0, value - 30); // Darken dark values
        } else {
            return Math.min(255, value + 30); // Brighten light values
        }
    }
}
