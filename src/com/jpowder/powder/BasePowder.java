package com.jpowder.powder;

public class BasePowder implements Cloneable {
    public int x;
    public int y;

    public final ShiftRule shift;
    public final int fIndex;

    public float velocity;
    public final float TERMINAL_VELOCITY = 25;
    private final double gravity = 9.8;

    public int color;

    public void calculateNextPos(float timeElapsed) {
//        xf = x0 + v0*t + (1/2)*g*t^2

        velocity += (int) (0.5 * gravity * (timeElapsed * timeElapsed));

        if (velocity > TERMINAL_VELOCITY) {
            velocity = TERMINAL_VELOCITY;
        }

        velocity = 1.0f;
    }

    public BasePowder(ShiftRule shift, int fIndex) {
        x = 0;
        y = 0;
        velocity = 0.0f;

        color = (int) (Math.random() * 0xffffff);
        this.shift = shift;
        this.fIndex = fIndex;
    }

    @Override
    public BasePowder clone() {
        try {
            // TODO: copy mutable state here, so the clone can't change the internals of the original
            return (BasePowder) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
