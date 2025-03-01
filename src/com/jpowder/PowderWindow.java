package com.jpowder;

import com.jpowder.powder.*;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;

public class PowderWindow extends Canvas implements Runnable, MouseListener, KeyListener {
    private final Random rand = new Random();
    private JFrame frame;
    private BufferedImage image;
    private int[] lastPixels;
    private int[] pixels;
    public boolean webMode = false;
    public boolean reduGraphicsMode = false;

    private int width;
    private int height;

    private final int powderItemWidth;
    private final int powderItemHeight;
    private static ArrayList<JButton> toolboxButtons;

    private boolean rainbow = false;
    private final PowderGrid pg;
    private final Registry pr;
    private static String selectedPowder = "water_fluid";
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
        this.setBackground(new Color(0, 0, 0));
        frame = new JFrame("Powder Simulation");
        pg = new PowderGrid(50, 50);
        pr = new Registry();
        powderItemWidth = 150;
        powderItemHeight = 50;

        frame.add(this);
        frame.pack();
        frame.setSize(width, height);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        addMouseListener(this);
        addKeyListener(this);
    }

    private static class PowderButtonListener implements ActionListener {
        String powderID;
        JButton button;
        public PowderButtonListener(String powderID, JButton button) {
            this.powderID = powderID;
            this.button = button;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            for (JButton button : toolboxButtons) {
                if (button != this.button) {
                    button.setBorder(BorderFactory.createEmptyBorder());
                } else {
                    // Handle powder selection
                    System.out.println("Selected Powder: " + powderID);
                    Border underBorder = BorderFactory.createLineBorder(new Color(0,0,0), 8);
                    Border topBorder = BorderFactory.createLineBorder(Color.gray, 5);
                    Border buttonBorder = BorderFactory.createCompoundBorder(topBorder, underBorder);

                    button.setBorder(buttonBorder);
                    selectedPowder = powderID;
                }
            }
        }
    }

    public void createToolbar() {
        System.out.println("creating toolbar...");
        toolboxButtons = new ArrayList<>();
        // Create a scrollable button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(0, 0, 0));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder());
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        for (Registry.RegistryEntry entry : pr.getRegisteredPowder()) { // Example buttons
            JButton button = new JButton(entry.pretty);

            Border buttonBorder;
            if (Objects.equals(entry.id, selectedPowder)) {
                Border underBorder = BorderFactory.createLineBorder(new Color(0,0,0), 8);
                Border topBorder = BorderFactory.createLineBorder(Color.gray, 5);
                buttonBorder = BorderFactory.createCompoundBorder(topBorder, underBorder);
            } else {
                buttonBorder = BorderFactory.createEmptyBorder();
            }

            button.setPreferredSize(new Dimension(powderItemWidth-1, powderItemHeight-1)); // Set preferred size for each button
            button.setMaximumSize(new Dimension(powderItemWidth-1, powderItemHeight-1)); // Optional: Set maximum size to ensure consi
            button.setBackground(new Color(entry.powder.color));
            button.setForeground(PowderUtilities.colorIntToRGB(PowderUtilities.invertColorInt(entry.powder.color)));
            button.setBorder(buttonBorder);
            button.addActionListener(new PowderButtonListener(entry.id, button));
            buttonPanel.add(button);
            toolboxButtons.add(button);
        }

        // Add the button panel to a JScrollPane
        JScrollPane scrollPane = new JScrollPane(buttonPanel, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        // support horizontal scrolling

        scrollPane.setBackground(new Color(0,0,0));
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setPreferredSize(new Dimension(width, powderItemHeight+10)); // Adjust the height as needed
        frame.add(scrollPane, BorderLayout.SOUTH);
        frame.setVisible(true); // ensure new gui elements are reflected

    }

    /** Handle the key typed event from the text field. */
    public void keyTyped(KeyEvent e) {
        System.out.println("Key Typed: " + e);

        if (e.getKeyChar() == 'g') {
            rainbow = !rainbow;
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

            // no reason to hand focus over to other parts of the app
            if (!isFocusOwner() && frame.isFocused()) { // only try to focus if the window is focused
                requestFocus();
            }

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
        lastPixels = new int[pixels.length];
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
                    if (relationship.relationshipType == RelationshipType.MERGE) {
                        new_y = belowPixel.y;
                        new_x = belowPixel.x;

                        pg.erasePixel(gridPixel.x, gridPixel.y);
                        pg.erasePixel(new_x, new_y);

                        pg.setPixel(new_x, new_y, pr.createInstance(relationship.out));;
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
        int randX = Math.max(0, rand.nextInt(pg.getWidth()));
        int randY = Math.max(0, rand.nextInt(pg.getHeight())); // trim 25 for now so we dont go out of bounds

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
        BufferStrategy bs = getBufferStrategy();
        if (bs == null) {
            createBufferStrategy(webMode ? 1 : 3);
            return;
        }
        Graphics g = bs.getDrawGraphics();

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
            Point gridPos = frameToPowderGrid(mousePos.x, mousePos.y);
            // mouse position is based on the frame, not grid
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


        // only update the screen if we've changed something
        if (!Arrays.equals(pixels, lastPixels)) {
            if (!reduGraphicsMode) {
                g.drawImage(image, 0, 0, width, height, null);
            } else {
                // reduced graphics, only draw updated pixels
                for (int y = 0; y < height; y+= (int) pixelSize+1) {
                    for (int x = 0; x < width; x+= (int) pixelSize+1) {
                        if (pixels[y*width+x] != lastPixels[y*width+x]) {
                            g.setColor(new Color(pixels[y*width+x]));
                            g.fillRect(x-5, y-5, (int) pixelSize+1, (int) pixelSize+1);
                        }
                    }
                }
            }
        }
        lastPixels = pixels.clone();
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

        for (String arg : Arrays.toString(args).split(",")) {
            arg = arg.trim();
            arg = arg.replace("[", "").replace("]", "");

            if (Objects.equals(arg, "-web") || Objects.equals(arg, "--web")) {
                System.out.println("Running in Web mode. Buffering will be set to single.");
                simulation.webMode = true;
            } else if (Objects.equals(arg, "-redugraphics") || Objects.equals(arg, "--redugraphics")) {
                System.out.println("Enabling reduced graphics. Expect visual issues.");
                simulation.reduGraphicsMode = true;
            }
        }

        // register powders
        simulation.pr.register(new SandPowder(), "sand_powder", "Sand");
        simulation.pr.register(new WetSandPowder(), "wet_sand_powder", "Moist Sand");
        simulation.pr.register(new RockPowder(), "rock_powder", "Rock");
        simulation.pr.register(new WaterFluid(), "water_fluid", "Water");

        // register relationships
        simulation.pr.registerRelationship("sand_powder", "water_fluid", "wet_sand_powder", RelationshipType.MERGE);

        simulation.createToolbar();
        new Thread(simulation).start();
//        new Thread(simulation::mouseLoop).start();
    }
}
