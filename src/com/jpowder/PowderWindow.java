package com.jpowder;

import com.jpowder.powder.*;

import javax.swing.JFrame;
import java.awt.Canvas;
import java.awt.Point;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Objects;
import java.util.Random;

public class PowderWindow extends Canvas implements Runnable, MouseListener, KeyListener {
    private final Random rand = new Random();
    private JFrame frame;
    private BufferedImage image;
    private int[] pixels;

    private int width;
    private int height;

    private boolean rainbow = false;
    private final PowderGrid pg;
    private final Registry pr;
    private String selectedPowder = "water_fluid";
    private boolean erase = false;
    private boolean paused = false;

    private boolean mouseDown = false;
    private Point mousePos;

    private final int fps = 60;

    float pixelSizeByWidth;
    float pixelSizeByHeight;
    float pixelSize;
    // Calculate offset for centering the grid vertically and horizontally
    float renderXOffset;
    float renderYOffset;

    public PowderWindow(int width, int height) {
        this.width = width;
        this.height = height;
        frame = new JFrame("Powder Simulation");
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        pg = new PowderGrid(350, 250);
        pr = new Registry();
        pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        frame.add(this);
        frame.pack();
        frame.setSize(width, height);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        addMouseListener(this);
        addKeyListener(this);
    }

    /** Handle the key typed event from the text field. */
    public void keyTyped(KeyEvent e) {
        System.out.println("Key Typed: " + e);

        if (e.getKeyChar() == 'g') {
            rainbow = !rainbow;
        } else if (e.getKeyChar() == 'r') {
            selectedPowder = "rock_powder";
        } else if (e.getKeyChar() == 's') {
            selectedPowder = "sand_powder";
        } else if (e.getKeyChar() == 'm') {
            selectedPowder = "wet_sand_powder";
        } else if (e.getKeyChar() == 'w') {
            selectedPowder = "water_fluid";
        } else if (e.getKeyChar() == 'e') {
            erase = !erase;
        } else if (e.getKeyChar() == 'c') {
            pg.clearGrid();
            pg.mergeGrid();
        } else if (e.getKeyChar() == 'f' && paused) {
            updatePixels();
            pg.mergeGrid();
        }
    }

    /** Handle the key pressed event from the text field. */
    public void keyPressed(KeyEvent e) {
        System.out.println("Key Pressed: " + e);

         if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            paused = !paused;
        }
    }

    /** Handle the key released event from the text field. */
    public void keyReleased(KeyEvent e) { }


    // MouseListener methods
    @Override
    public void mouseClicked(MouseEvent e) {
        System.out.println("Mouse Clicked at (" + e.getX() + ", " + e.getY() + ")");
    }

    @Override
    public void mousePressed(MouseEvent e) {
        mouseDown = true;
        System.out.println("Mouse Pressed at (" + e.getX() + ", " + e.getY() + ")");
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        mouseDown = false;
        System.out.println("Mouse Released at (" + e.getX() + ", " + e.getY() + ")");
    }

    @Override
    public void mouseEntered(MouseEvent e) { }

    @Override
    public void mouseExited(MouseEvent e) { }

    public void run() {
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        while (true) {
            long renderStart = System.currentTimeMillis();

            int newWidth = getWidth();
            int newHeight = getHeight();
            if (newHeight != height || newWidth != width) {
                height = newHeight;
                width = newWidth;

                createImage();
            }

            pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
            mousePos = frame.getMousePosition();

            // Calculate the pixel size based on both the window's width and height using floating-point arithmetic
            pixelSizeByWidth = (float) width / pg.getWidth();
            pixelSizeByHeight = (float) height / pg.getHeight();

            // Use the larger pixel size that fits within both the width and height constraints
            pixelSize = Math.min(pixelSizeByWidth, pixelSizeByHeight);

            // Calculate offset for centering the grid vertically and horizontally
            renderXOffset = (width - (pixelSize * pg.getWidth())) / 2.0f;
            renderYOffset = (height - (pixelSize * pg.getHeight())) / 2.0f;

            if (mouseDown && mousePos != null) {

                Point transPos = frameToPowderGrid(mousePos.x, mousePos.y);

                if (!pg.isPowderAt(transPos.x, transPos.y) && !erase) {
                    BasePowder newPowder = pr.createInstance(selectedPowder);
                    pg.setPixel(transPos.x, transPos.y, newPowder);
                    pg.mergeGrid(); // make sure the grid reflects our changes
                } else if (pg.isPowderAt(transPos.x, transPos.y) && erase) {
                    pg.erasePixel(transPos.x, transPos.y);
                    pg.mergeGrid(); // make sure the grid reflects our changes
                }
            }

            if (!paused) {
                updatePixels();
//            pg.refreshAllPixels();
                pg.mergeGrid();
            }
            render();

            long renderEnd = System.currentTimeMillis();
            long sleepTime = 1000/fps - (renderEnd-renderStart);

            try {
                if (sleepTime > 0) {
                    Thread.sleep(sleepTime); // Sleep for ~60 FPS
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    private void createImage() {
        image = null;
        pixels = null;
        System.gc();

        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
    }

    /**
     * Update all pixels on the grid.
     * <p>
     * Iterates over the main pixel grid, but applies updates to the updatable grid
     * while using its values to calculate positions.
     */
    private void updatePixels() {
        for (BasePowder gridPixel : pg.getPixels()) {
            if (gridPixel == null || gridPixel.erased) { // no pixel here
                continue;
            }

            if (rainbow) {
                gridPixel.color = (int) (Math.random() * 0xffffff);
            }

            gridPixel.calculateNextPos(.5f);
            int new_y = (int) (gridPixel.y + gridPixel.velocity);
            int new_x = gridPixel.x;

            if (gridPixel.shift == ShiftRule.SLIP || gridPixel.shift == ShiftRule.STICK) {
                shiftNaturally: {
                    if (pg.hasNeighborBelow(gridPixel)) {
                        BasePowder belowPixel = pg.getUpdatablePixels()[pg.findTrueLocation(gridPixel.x, gridPixel.y+1)];
                        String gridID = pr.getID(gridPixel);
                        String belowID = pr.getID(belowPixel);

                        gridPixel.velocity = 0; // Stay in place if there's a neighbor below
                        new_y = gridPixel.y;

                        if (gridPixel.shift == ShiftRule.STICK && !Objects.equals(gridID, belowID)) {
                            break shiftNaturally; // dont shift, but dont block float shifting
                        }

                        boolean canMoveLeft = (!pg.isPowderAt(gridPixel.x-1, gridPixel.y+1) &&
                                !pg.isPowderAt(gridPixel.x-1, gridPixel.y));
                        boolean canMoveRight = (!pg.isPowderAt(gridPixel.x+1, gridPixel.y+1) &&
                                !pg.isPowderAt(gridPixel.x+1, gridPixel.y));

                        if (canMoveLeft && canMoveRight) {
                            new_x += rand.nextBoolean() ? -1 : 1;
                        } else if (canMoveLeft) {
                            new_x -= 1;
                        } else if (canMoveRight) {
                            new_x += 1;
                        }
                    }
                }
            } else if (gridPixel.shift == ShiftRule.SOLID) {
                gridPixel.velocity = 0;
                new_y = gridPixel.y;
            } else if (gridPixel.shift == ShiftRule.FLUID)  {
                if (pg.hasNeighborBelow(gridPixel) || gridPixel.y >= pg.getHeight()-1) {
                    gridPixel.velocity = 0;
                    new_y = gridPixel.y;

                    boolean canMoveLeft = (!pg.isPowderAt(gridPixel.x-1, gridPixel.y));
                    boolean canMoveRight = (!pg.isPowderAt(gridPixel.x+1, gridPixel.y));
                    boolean shouldMove = rand.nextBoolean();

                    if (!shouldMove) {
                        assert true; // do nothing
                    } else if (canMoveLeft && canMoveRight) {
                        new_x += rand.nextBoolean() ? -1 : 1;
                    } else if (canMoveLeft) {
                        new_x -= 1;
                    } else if (canMoveRight) {
                        new_x += 1;
                    }
                }
            }

            // float shift, powder can move pixels of a lower fIndex that itself
            if (pg.hasNeighborBelow(gridPixel)) {
                BasePowder belowPixel = pg.getUpdatablePixels()[pg.findTrueLocation(gridPixel.x, gridPixel.y+1)];
                String gridID = pr.getID(gridPixel);
                String belowID = pr.getID(belowPixel);

                // relationship check
                if (pr.hasRelationship(gridID, belowID)) {
                    Registry.RelationshipEntry relationship = pr.getRelationship(gridID, belowID);
                    if (relationship.relationshipType() == RelationshipType.MERGE) {
                        new_y = belowPixel.y;
                        new_x = belowPixel.x;

                        pg.erasePixel(gridPixel.x, gridPixel.y);
                        pg.erasePixel(new_x, new_y);

                        pg.setPixel(new_x, new_y, pr.createInstance(relationship.out()));;
                        continue;
                    }
                } else if (pg.canDisplace(belowPixel, gridPixel)) {
                    new_y = gridPixel.y+1;

                    pg.movePixel(new_x, new_y, gridPixel);
                    continue;
                }
            }
            boolean shouldMoveLeft = rand.nextBoolean();
            if (pg.isPowderAt(gridPixel.x-1, gridPixel.y) && shouldMoveLeft) {
                BasePowder sidePixel = pg.getUpdatablePixels()[pg.findTrueLocation(gridPixel.x-1, gridPixel.y)];
                if (pg.canDisplace(sidePixel, gridPixel) && sidePixel.canDisplaceHorizontal && rand.nextBoolean()) {
                    new_x = gridPixel.x-1;
                    pg.movePixel(new_x, gridPixel.y, gridPixel);
                    continue;
                }
            } else if (pg.isPowderAt(gridPixel.x+1, gridPixel.y)) {
                BasePowder sidePixel = pg.getUpdatablePixels()[pg.findTrueLocation(gridPixel.x+1, gridPixel.y)];
                if (pg.canDisplace(sidePixel, gridPixel) && sidePixel.canDisplaceHorizontal && rand.nextBoolean()) {
                    new_x = gridPixel.x+1;
                    pg.movePixel(new_x, gridPixel.y, gridPixel);
                    continue;
                }
            }

            // Ensure new_x and new_y are within bounds
            if (new_x >= pg.getWidth()) {
                new_x = pg.getWidth() - 2;
            } else if (new_x < 0) {
                new_x = 0;
            }

            if (new_y >= pg.getHeight()) {
                new_y = pg.getHeight();
            } else if (new_y < 0) {
                new_y = 0;
            }

            pg.movePixel(new_x, new_y, gridPixel); // we should update the grid, too
        }
    }


    private void addRandPixel() {
        int randX = rand.nextInt(1, pg.getWidth());
        int randY = rand.nextInt(1, pg.getHeight()); // trim 25 for now so we dont go out of bounds

        if (!pg.isPowderAt(randX, randY)) {
            pg.setPixel(randX, randY, new SandPowder());
        }


        // Update pixel positions based on your simulation logic
//        for (int i = 0; i < grid_width * grid_height; i++) {
//            grid_pixels[i] = (int) (Math.random() * 0xffffff); // Example: Random colors
//        }
    }

    private Point frameToPowderGrid(int x, int y) {
        // Adjust mouse position based on the component and offset
        int adjustedX = (int) ((mousePos.x - renderXOffset) / pixelSize);
        int adjustedY = (int) ((mousePos.y - renderYOffset) / pixelSize);

        // Ensure grid coordinates are within bounds
        adjustedX = Math.max(0, Math.min(adjustedX, pg.getWidth() - 1));
        adjustedY = Math.max(0, Math.min(adjustedY, pg.getHeight() - 1));

        return new Point(adjustedX, adjustedY);
    }

    public int rgbToColorInt(int r, int g, int b) {
        return (r << 16) | (g << 8) | b;
    }

    private void render() {
        for (int gridY = 0; gridY < pg.getHeight(); gridY++) {
            for (int gridX = 0; gridX < pg.getWidth(); gridX++) {
                int color;
                if (pg.getPixels()[gridY * pg.getWidth() + gridX] != null) {
                    color = pg.getPixels()[gridY * pg.getWidth() + gridX].color;
                } else {
                    color = PowderUtilities.rgbToColorInt(10, 10, 10);
                }

                for (int y = 0; y < pixelSize; y++) {
                    for (int x = 0; x < pixelSize; x++) {
                        int px = (int) (gridX * pixelSize + x + renderXOffset);
                        int py = (int) (gridY * pixelSize + y + renderYOffset);
                        if (px < width && py < height && py >= 0) {
                            pixels[py * width + px] = color;
                        }
                    }
                }
            }
        }

        // render the cursor
        if (mousePos != null) {;
            // mouse position is based on the frame, not grid
            Point gridPos = frameToPowderGrid(mousePos.x, mousePos.y);
            for (int y = 0; y < pixelSize; y++) {
                for (int x = 0; x < pixelSize; x++) {
                    int px = (int) (gridPos.x * pixelSize + x + renderXOffset);
                    int py = (int) (gridPos.y * pixelSize + y + renderYOffset);

                    if (px < width && py < height && py >=0) {
                        int color;
                        if (erase) {
                            color = PowderUtilities.rgbToColorInt(255, 0, 0);
                        } else {
                            color = PowderUtilities.rgbToColorInt(183, 183, 183);
                        }

                        pixels[py * width + px] = color;
                    }
                }
            }

        }

        BufferStrategy bs = getBufferStrategy();
        if (bs == null) {
            createBufferStrategy(3);
            return;
        }
        Graphics g = bs.getDrawGraphics();
        g.drawImage(image, 0, 0, width, height, null);
        g.dispose();
        bs.show();
    }

    private void mouseLoop() {
        while (true) {
            if (mouseDown && mousePos != null) {

                Point transPos = frameToPowderGrid(mousePos.x, mousePos.y);

                if (!pg.isPowderAt(transPos.x, transPos.y)) {
                    pg.setPixel(transPos.x, transPos.y, pr.createInstance(selectedPowder));
                }
            }

            try {
                Thread.sleep(10); // Sleep for ~60 FPS
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    public static void main(String[] args) {
        PowderWindow simulation = new PowderWindow(800, 800);

        // register powders
        simulation.pr.register(new SandPowder(), "sand_powder");
        simulation.pr.register(new WetSandPowder(), "wet_sand_powder");
        simulation.pr.register(new RockPowder(), "rock_powder");
        simulation.pr.register(new WaterFluid(), "water_fluid");

        // register relationships
        simulation.pr.registerRelationship("sand_powder", "water_fluid", "wet_sand_powder", RelationshipType.MERGE);

        new Thread(simulation).start();
//        new Thread(simulation::mouseLoop).start();
    }
}
