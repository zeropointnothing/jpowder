package com.jpowder;

import com.jpowder.powder.*;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Stream;

public class PowderWindow extends Canvas implements Runnable, MouseListener, KeyListener {
    private final Random rand = new Random();
    private final JFrame frame;
    private BufferedImage image;
    private int[] lastPixels;
    private int[] pixels;
    public boolean webMode = false;
    public boolean reduGraphicsMode = false;
    public boolean allFramesMode = false;

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
        frame = new JFrame("jPowder - Simulation");
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

        java.net.URL url = ClassLoader.getSystemResource("com/jpowder/resources/icon.png");
        Toolkit kit = Toolkit.getDefaultToolkit();
        Image img = kit.createImage(url);
        this.frame.setIconImage(img);

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
            button.setMaximumSize(new Dimension(powderItemWidth-1, powderItemHeight-1)); // Optional: Set maximum size to ensure consistency
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
                    //noinspection BusyWait
                    Thread.sleep(sleepTime); // Sleep for ~60 FPS
                }
            } catch (InterruptedException e) {
                //noinspection CallToPrintStackTrace
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
            } else if (gridPixel instanceof BaseGas) {
                if (rand.nextInt(((BaseGas) gridPixel).floatMax) >= ((BaseGas) gridPixel).floatNeeded) {
                    new_y = (int) (gridPixel.y - gridPixel.velocity);
                } else if (rand.nextInt(((BaseGas) gridPixel).sinkMax) >= ((BaseGas) gridPixel).sinkNeeded) {
                    new_y = (int) (gridPixel.y + gridPixel.velocity);
                } else {
                    new_y = gridPixel.y;
                }

                if (pg.hasNeighborAbove(gridPixel)) {
                    new_y = gridPixel.y;
                }

                boolean canMoveLeft = (!pg.isPowderAt(gridPixel.x-1, new_y));
                boolean canMoveRight = (!pg.isPowderAt(gridPixel.x+1, new_y));
                boolean shouldMove = (rand.nextInt(((BaseGas) gridPixel).floatMax) >= ((BaseGas) gridPixel).shiftNeeded);

                if (!shouldMove) {
                    assert true; // do nothing
                } else if (canMoveLeft && canMoveRight) {
                    new_x += rand.nextBoolean() ? -1 : 1;
                } else if (canMoveLeft) {
                    new_x -= 1;
                } else if (canMoveRight) {
                    new_x += 1;
                }
                gridPixel.life -= 1;

                // darken the gas based on its life
                Color originalColor = PowderUtilities.colorIntToRGB(gridPixel.originalColor);
                int shiftedRed = (int) Math.min(Math.max(0, originalColor.getRed()*((float) gridPixel.life/gridPixel.originalLife)), 255);
                int shiftedGreen = (int) Math.min(Math.max(0, originalColor.getGreen()*((float) gridPixel.life/gridPixel.originalLife)), 255);
                int shiftedBlue = (int) Math.min(Math.max(0, originalColor.getBlue()*((float) gridPixel.life/gridPixel.originalLife)), 255);

                gridPixel.color = PowderUtilities.rgbToColorInt(shiftedRed, shiftedGreen, shiftedBlue);

                if (gridPixel.life <= 1) {
                    pg.erasePixel(gridPixel.x, gridPixel.y);
                    continue;
                }
            }

            // float shift, powder can move pixels of a lower fIndex that itself
            // relationship check
            PowderGrid.Surrounding surr = pg.getSurrounding(gridPixel); // get all surrounding pixels
            if (!Stream.of(surr).allMatch(Objects::isNull)) {
                BasePowder belowPixel = surr.below;
                BasePowder abovePixel = surr.above;
                BasePowder leftPixel = surr.left;
                BasePowder rightPixel = surr.right;

                // get the id's of the pixels for relationship checking
                String gridID = pr.getID(gridPixel); // current pixel
                String aboveID = pr.getID(abovePixel);
                String belowID = pr.getID(belowPixel);
                String leftID = pr.getID(leftPixel);
                String rightID = pr.getID(rightPixel);

                // select a direction to check against
                Registry.RelationshipEntry relationship;
                BasePowder relationshipWith;
                String relationshipID;
                if (pr.hasRelationship(gridID, belowID)) {
                    relationship = pr.getRelationship(gridID, belowID);
                    relationshipWith = belowPixel;
                    relationshipID = belowID;
                } else if (pr.hasRelationship(gridID, aboveID)) {
                    relationship = pr.getRelationship(gridID, aboveID);
                    relationshipWith = abovePixel;
                    relationshipID = aboveID;
                } else if (pr.hasRelationship(gridID, leftID)) {
                    relationship = pr.getRelationship(gridID, leftID);
                    relationshipWith = leftPixel;
                    relationshipID = leftID;
                } else if (pr.hasRelationship(gridID, rightID)) {
                    relationship = pr.getRelationship(gridID, rightID);
                    relationshipWith = rightPixel;
                    relationshipID = rightID;
                } else {
                    relationship = null;
                    relationshipWith = null;
                    relationshipID = null;
                }

                if (relationship != null && relationshipWith != null) {
                    if (relationship.relationshipType == RelationshipType.MERGE) {
                        // combine two powders into 'out'

                        new_y = relationshipWith.y;
                        new_x = relationshipWith.x;

                        pg.erasePixel(gridPixel.x, gridPixel.y);
                        pg.erasePixel(new_x, new_y);

                        pg.setPixel(new_x, new_y, pr.createInstance(relationship.out));
                        continue;
                    } else if (relationship.relationshipType == RelationshipType.CONSUME) {
                        // 'out' consumes the other powder

                        if (Objects.equals(gridID, relationship.out)) {
                            pg.erasePixel(relationshipWith.x, relationshipWith.y);
                        } else {
                            pg.erasePixel(gridPixel.x, gridPixel.y);
                        }
                        continue;
                    } else if (relationship.relationshipType == RelationshipType.PAINT) {
                        // other powder is painted with 'out'

                        if (!Objects.equals(relationshipID, relationship.out)) {
                            pg.erasePixel(relationshipWith.x, relationshipWith.y);
                            pg.setPixel(relationshipWith.x, relationshipWith.y, pr.createInstance(relationship.out));
                        }
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

    private Point frameToPowderGrid(int x, int y) {
        // Adjust mouse position based on the component and offset
        int adjustedX = (int) ((x - renderXOffset) / pixelSize);
        int adjustedY = (int) ((y - renderYOffset) / pixelSize);

        // Ensure grid coordinates are within bounds
        adjustedX = Math.max(0, Math.min(adjustedX, pg.getWidth() - 1));
        adjustedY = Math.max(0, Math.min(adjustedY, pg.getHeight() - 1));

        return new Point(adjustedX, adjustedY);
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
        if (mousePos != null) {
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
        if (!Arrays.equals(pixels, lastPixels) || allFramesMode) {
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

    public static void main(String[] args) {
        PowderWindow simulation = new PowderWindow(800, 800);

        for (String arg : Arrays.toString(args).split(",")) {
            arg = arg.trim();
            arg = arg.replace("[", "").replace("]", "");

            switch (arg) {
                case "-web":
                case "--web":
                    System.out.println("Running in Web mode. Buffering will be set to single.");
                    simulation.webMode = true;
                    break;
                case "-redugraphics":
                case "--redugraphics":
                    System.out.println("Enabling reduced graphics. Expect visual issues.");
                    simulation.reduGraphicsMode = true;
                    break;
                case "-allframes":
                case "--allframes":
                    System.out.println("Rendering all frames...");
                    simulation.allFramesMode = true;
                    break;
            }
        }

        // register powders

        System.out.println("registering powders...");
        simulation.pr.register(new SandPowder(), "sand_powder", "Sand");
        simulation.pr.register(new WetSandPowder(), "wet_sand_powder", "Moist Sand");
        simulation.pr.register(new RockPowder(), "rock_powder", "Rock");
        simulation.pr.register(new WaterFluid(), "water_fluid", "Water");
        simulation.pr.register(new FireGas(), "fire_gas", "Fire");
        simulation.pr.register(new WoodPowder(), "wood_powder", "Wood");
        simulation.pr.register(new HydrogenFluid(), "hydrogen_fluid", "Hydrogen");

        // register relationships
        System.out.println("registering relationships...");
        simulation.pr.registerRelationship("sand_powder", "water_fluid", "wet_sand_powder", RelationshipType.MERGE);
        simulation.pr.registerRelationship("fire_gas", "water_fluid", "water_fluid", RelationshipType.CONSUME);
        simulation.pr.registerRelationship("fire_gas", "wood_powder", "fire_gas", RelationshipType.PAINT);
        simulation.pr.registerRelationship("fire_gas", "hydrogen_fluid", "fire_gas", RelationshipType.PAINT);

        simulation.createToolbar();
        System.out.println("starting simulation! (" + simulation.fps + " FPS)");
        new Thread(simulation).start();
//        new Thread(simulation::mouseLoop).start();
    }
}
