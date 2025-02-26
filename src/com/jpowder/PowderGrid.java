package com.jpowder;

import com.jpowder.powder.BasePowder;

public class PowderGrid {
    private final int width;
    private final int height;
    private BasePowder[] pixels;
    private final BasePowder[] updatablePixels;

    public PowderGrid(int width, int height) {
        this.width = width;
        this.height = height;
        this.pixels = new BasePowder[width*height];
        updatablePixels = new BasePowder[width*height];

    }

    public BasePowder[] getPixels() { return pixels; }
    public BasePowder[] getUpdatablePixels() { return updatablePixels; }
    public int getWidth() { return width; };
    public int getHeight() { return height; }

    /**
     * Get the length of the 1D array.
     *
     * @return the full length of the 1D array
     */
    public int get1DLength() { return width*height; }

    /**
     * Find the true location (on the 1D grid) of a pixel.
     * @param x X position
     * @param y Y position
     * @return The index on the 1D grid
     */
    public int findTrueLocation(int x, int y) {
        return y*width+x;
    }


//    public boolean isPowderAt(int x, int y) {
//        for (BasePowder gridPixel : pixels) {
//            if (gridPixel == null) {
//                continue;
//            }
//
//            if (gridPixel.x == x && gridPixel.y == y) {
//                return true;
//            }
//        }
//
//        return false;
//    }

    /**
     * Checks if there is a powder at x,y within the updatable grid.
     * @param x X position
     * @param y Y Positon
     * @return Whether there is a powder at x,y
     */
    public boolean isPowderAt(int x, int y) {
        return (updatablePixels[findTrueLocation(x, y)] != null);
    }

    public boolean hasNeighborsBelow(BasePowder powder) {
        return isPowderAt(powder.x+1, powder.y+1) &&
                isPowderAt(powder.x-1, powder.y+1);
    }

    public boolean hasNeighborsBeside(BasePowder powder) {
        return isPowderAt(powder.x+1, powder.y)&&
                isPowderAt(powder.x-1, powder.y);
    }

    public boolean hasNeighborBelow(BasePowder powder) {
        try {
            return isPowderAt(powder.x, powder.y+1);
        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    public void setPixel(int x, int y, BasePowder powder) {
        int pos = findTrueLocation(x, y);

        updatablePixels[pos] = powder;
        powder.x = x;
        powder.y = y;

    }

    public void erasePixel(int x, int y) {
        int pos  = findTrueLocation(x, y);
        updatablePixels[pos]= null;
    }

    /**
     * Merge the update grid with the actual grid.
     */
    public void mergeGrid() {
        pixels = updatablePixels.clone();
    }

    public void movePixel(int x, int y, BasePowder powder) {
        int oldPos = findTrueLocation(powder.x, powder.y);

        if (x >= 0 && x < width && y >= 0 && y <= height-1) {
            int newPos = findTrueLocation(x, y);

            updatablePixels[oldPos] = null;
            updatablePixels[newPos] = powder;

            powder.x = x;
            powder.y = y;
            }
    }

    public void refreshAllPixels() {
        for (int i = 0; i < updatablePixels.length; i++) {
            BasePowder gridPixel = updatablePixels[i];

            if (gridPixel == null) {
                continue;
            }

            // Ensure newX and newY are within bounds
            if (gridPixel.x >= 0 && gridPixel.x < width && gridPixel.y >= 0 && gridPixel.y <= height-1) {
                int newIndex = (gridPixel.y * width + gridPixel.x);

                updatablePixels[i] = null;
                updatablePixels[newIndex] = gridPixel;
            } else {
                throw new RuntimeException("Out of bounds! (" + gridPixel.x + "," + gridPixel.y + "}");
            }
        }
    }

}
