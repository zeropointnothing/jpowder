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
    SLIP
}
