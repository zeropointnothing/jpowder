package com.jpowder;

import com.jpowder.powder.BasePowder;

public class PowderGrid {
    private final int width;
    private final int height;
    private final BasePowder[] pixels;

    public PowderGrid(int width, int height) {
        this.width = width;
        this.height = height;
        this.pixels = new BasePowder[width*height];

    }

    public BasePowder[] getPixels() { return pixels; }
    public int getWidth() { return width; };
    public int getHeight() { return height; }

    /**
     * Get the length of the 1D array.
     *
     * @return the full length of the 1D array
     */
    public int get1DLength() { return width*height; }

    public int findTrueLocation(int x, int y) {
        return y*width+x;
    }

    public boolean hasNeighborsBelow(BasePowder powder) {
        return pixels[findTrueLocation(powder.x+1, powder.y+1)] != null &&
                pixels[findTrueLocation(powder.x-1, powder.y+1)] != null;
    }

    public boolean hasNeighborsBeside(BasePowder powder) {
        return pixels[findTrueLocation(powder.x+1, powder.y)] != null &&
                pixels[findTrueLocation(powder.x-1, powder.y)] != null;
    }

    public boolean hasNeighborBelow(BasePowder powder) {
        try {
            return pixels[findTrueLocation(powder.x, powder.y+1)] != null;

        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    public boolean hasPowder(int x, int y) {
        return pixels[findTrueLocation(x, y)] != null;
    }

    public void setPixel(int x, int y, BasePowder powder) {
        int pos = findTrueLocation(x, y);

        pixels[pos] = powder;
        powder.x = x;
        powder.y = y;

    }

    public void erasePixel(int x, int y) {
        int pos  = findTrueLocation(x, y);
        pixels[pos]= null;
    }

    public void movePixel(int ox, int oy, int nx, int ny, BasePowder powder) {
        pixels[ox*oy] = null;
        pixels[nx*ny] = powder;
    }

    public void refreshPixels() throws RuntimeException {
        for (int i = 0; i < pixels.length; i++) {
            BasePowder gridPixel = pixels[i];

            if (gridPixel == null) {
                continue;
            }

            // Ensure newX and newY are within bounds
            if (gridPixel.x >= 0 && gridPixel.x < width && gridPixel.y >= 0 && gridPixel.y <= height-1) {
                int newIndex = (gridPixel.y * width + gridPixel.x);

                pixels[i] = null;
                pixels[newIndex] = gridPixel;
            } else {
                throw new RuntimeException("Out of bounds! (" + gridPixel.x + "," + gridPixel.y + "}");
            }
        }
    }

}
