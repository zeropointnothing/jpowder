package com.jpowder.powder;

public enum RelationshipType {
    /**
     * These two powders should merge together.
     */
    MERGE,
    /**
     * The 'out' powder should consume the other.
     */
    CONSUME,
    /**
     * The 'out' powder should be painted onto the other.
     */
    PAINT
}
