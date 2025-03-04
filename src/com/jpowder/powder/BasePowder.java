package com.jpowder.powder;

public class BasePowder implements Cloneable {
    public int x;
    public int y;

    public boolean erased; // whether the powder is marked as 'erased' and should be ignored

    public final ShiftRule shift;
    public final int fIndex;
    public int life;
    public final int originalLife;
    public boolean canDisplaceHorizontal;
    public boolean canDisplaceVertical;

    public float velocity;
    public final float TERMINAL_VELOCITY = 25;
    private final double gravity = 9.8;

    public int color;
    public final int originalColor;

    public void calculateNextPos(float timeElapsed) {
//        xf = x0 + v0*t + (1/2)*g*t^2

        velocity += (int) (0.5 * gravity * (timeElapsed * timeElapsed));

        if (velocity > TERMINAL_VELOCITY) {
            velocity = TERMINAL_VELOCITY;
        }

        velocity = 1.0f;
    }

    public BasePowder(ShiftRule shift, int fIndex, int color, int life) {
        x = 0;
        y = 0;
        velocity = 0.0f;
        this.life = life;

//        color = (int) (Math.random() * 0xffffff);
        this.color = color;
        this.shift = shift;
        this.fIndex = fIndex;

        canDisplaceHorizontal = true;
        canDisplaceVertical = true;
        erased = false;
        originalColor = this.color;
        originalLife = this.life;
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
