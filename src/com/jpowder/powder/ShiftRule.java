package com.jpowder.powder;

/**
 *  Contains different types of 'shift rules'.
 */
public enum ShiftRule {
    STACK,
    /**
     * This powder should be static.
     */
    SOLID,
    /**
     * This powder should 'slip' off of other powders if there's free space.
     */
    SLIP,
    /**
     * This powder should only 'slip' off of powders of the same type.
     */
    STICK,
    /**
     * This powder should act as a fluid.
     */
    FLUID,
    /**
     * This powder should act as a gas.
     */
    GAS
}
