package org.example;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Objects;
import java.util.Random;
public class WallPaper extends JFrame {
    private final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    private int currentGifX = 50;
    private int currentGifY = 50;
    private ImageIcon currentGif;
    private int deltaX = 2;
    private int deltaY = 2;
    private boolean dragging = false;
    private boolean isPressed = false;
    private boolean canSwap = true;
    private final String subDirectory;
    private final File[] gifFiles;
    private final File startingGifFile;
    private final File defaultGifFile;
    private final File mouseHoverGifFile;
    private final File pressedGifFile;
    private final File draggedGif;
    private final File[] randomGifsFiles;
    private Timer randomGifTimer;
    public WallPaper(String subDirectory, File[] gifFiles) {
        super("Transparent Moving GIF");
        this.subDirectory = subDirectory;
        this.gifFiles = gifFiles;
        this.startingGifFile = getStartingImageIcon();
        this.defaultGifFile = getDefaultGif();
        this.mouseHoverGifFile = getMouseHoverGifFile();
        this.pressedGifFile = getPressedGifFile();
        this.draggedGif=getDraggedGif();
        this.randomGifsFiles = getRandomGifFiles();
        if (startingGifFile == null || defaultGifFile == null || mouseHoverGifFile == null || pressedGifFile == null || randomGifsFiles == null) {
            System.err.println("Something went wrong while loading the GIFs.");
            System.exit(0);
        }
        initializeComponents();
        initializeRandomGifTimer();
    }
    private void initializeComponents() {
        currentGif = new ImageIcon(startingGifFile.getAbsolutePath());
        int gifWidth = currentGif.getIconWidth();
        int gifHeight = currentGif.getIconHeight();

        JPanel panel = createPanel(gifWidth, gifHeight);
        setupFrame(panel);

        attachMouseListeners(panel, gifWidth, gifHeight);
        startAnimationTimer(panel, gifWidth, gifHeight);
    }

    private JPanel createPanel(int gifWidth, int gifHeight) {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                currentGif.paintIcon(this, g, currentGifX, currentGifY);
            }
        };
    }

    private void setupFrame(JPanel panel) {
        setIconImage(new ImageIcon("src/filesUtility/program-icon.png").getImage());
        setContentPane(panel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        setSize(screenSize.width, screenSize.height);
        setAlwaysOnTop(true);
    }

    private void attachMouseListeners(JPanel panel, int gifWidth, int gifHeight) {
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (isMouseOverGIF(e.getX(), e.getY(), gifWidth, gifHeight)) {
                    isPressed = true;
                    dragging = true;
                    canSwap=false;
                    currentGif = new ImageIcon(Objects.requireNonNull(pressedGifFile).getAbsolutePath());
                    panel.repaint();
                }
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                dragging = false;
                if (isPressed) {
                    int duration = GifDuration.getGifDuration(Objects.requireNonNull(pressedGifFile).getAbsolutePath());
                    if (duration < 2000) {
                        duration *= 2;
                    }
                    if (duration > 0) {
                        new Timer(duration, evt -> {
                        //    currentGif = new ImageIcon(Objects.requireNonNull(defaultGifFile).getAbsolutePath());
                            canSwap = true;
                            panel.repaint();
                        }).start();
                    }
                    isPressed = false;
                } else {
                    //currentGif = new ImageIcon(Objects.requireNonNull(defaultGifFile).getAbsolutePath());
                    canSwap = true;
                    panel.repaint();
                }
            }

        });

        panel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragging) {
                    int oldX = currentGifX;
                    int oldY = currentGifY;
                    currentGifX = e.getX() - gifWidth / 2;
                    currentGifY = e.getY() - gifHeight / 2;

                    if (isPressed && (Math.abs(currentGifX - oldX) > 5 || Math.abs(currentGifY - oldY) > 5)) {
                        currentGif = new ImageIcon(Objects.requireNonNull(draggedGif).getAbsolutePath());
                        isPressed = false;
                        canSwap=false;
                    }
                    panel.repaint();
                }
            }
            @Override
            public void mouseMoved(MouseEvent e) {
                if (canSwap && isMouseOverGIF(e.getX(), e.getY(), gifWidth, gifHeight)) {
                    currentGif = new ImageIcon(Objects.requireNonNull(mouseHoverGifFile).getAbsolutePath());
                    panel.repaint();
                }
            }
        });


        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    System.exit(0);
                }else if (e.getKeyCode()==KeyEvent.VK_CAPS_LOCK){
                    new Menu();
                    dispose();
                }
            }
        });
    }
    private void startAnimationTimer(JPanel panel, int gifWidth, int gifHeight) {
        new Timer(15, e -> {
            if (!dragging) {
                updateGIFPosition(gifWidth, gifHeight);
            }
            panel.repaint();
        }).start();
    }

    private void updateGIFPosition(int gifWidth, int gifHeight) {
        currentGifX += deltaX;
        currentGifY += deltaY;

        if (currentGifX < 0 || currentGifX > screenSize.width - gifWidth) {
            deltaX *= -1;
        }
        if (currentGifY < 0 || currentGifY > screenSize.height - gifHeight) {
            deltaY *= -1;
        }
    }

    private boolean isMouseOverGIF(int mouseX, int mouseY, int width, int height) {
        return mouseX >= currentGifX && mouseX <= currentGifX + width &&
                mouseY >= currentGifY && mouseY <= currentGifY + height;
    }

    private void initializeRandomGifTimer() {
        randomGifTimer = new Timer(getRandomInterval(), e -> switchRandomGif());
        randomGifTimer.setRepeats(false);
        randomGifTimer.start();
    }

    private int getRandomInterval() {
        return 10000 + new Random().nextInt(20001);
    }

    private void switchRandomGif() {
        if (canSwap){
            int gifIndex = new Random().nextInt(randomGifsFiles.length);
            ImageIcon randomImageIcon;
            do {
                randomImageIcon=new ImageIcon(randomGifsFiles[gifIndex].getAbsolutePath());
            }while (randomImageIcon==currentGif);
            currentGif = randomImageIcon;
            randomGifTimer.setInitialDelay(getRandomInterval());
            randomGifTimer.restart();
        }
    }
    private File getStartingImageIcon() {
        try {
            if (subDirectory.equals("stitch")) {
                return gifFiles[0];
            } else if (subDirectory.equals("spongebob")) {
                return gifFiles[0];
            }else if (subDirectory.equals("girl1")){
                return gifFiles[0];
            }else if (subDirectory.equals("girl2")){
                return gifFiles[0];
            }else if (subDirectory.equals("kirby")){
                return gifFiles[0];
            }
        } catch (NullPointerException | ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        return null;
    }

    private File getDefaultGif() {
        try {
            if (subDirectory.equals("stitch")) {
                return gifFiles[1];
            } else if (subDirectory.equals("spongebob")) {
                return gifFiles[1];
            }else if (subDirectory.equals("girl1")){
                return gifFiles[1];
            }else if (subDirectory.equals("girl2")){
                return gifFiles[1];
            }else if (subDirectory.equals("kirby")){
                return gifFiles[1];
            }
        } catch (NullPointerException | ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        return null;
    }

    private File getMouseHoverGifFile() {
        try {
            if (subDirectory.equals("stitch")) {
                return gifFiles[2];
            } else if (subDirectory.equals("spongebob")) {
                return gifFiles[2];
            }else if (subDirectory.equals("girl1")){
                return gifFiles[2];
            }else if (subDirectory.equals("girl2")){
                return gifFiles[2];
            }else if (subDirectory.equals("kirby")){
                return gifFiles[2];
            }
        } catch (NullPointerException | ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        return null;
    }

    private File getPressedGifFile() {
        try {
            if (subDirectory.equals("stitch")) {
                return gifFiles[3];
            } else if (subDirectory.equals("spongebob")) {
                return gifFiles[3];
            }else if (subDirectory.equals("girl1")){
                return gifFiles[3];
            }else if (subDirectory.equals("girl2")){
                return gifFiles[3];
            }else if (subDirectory.equals("kirby")){
                return gifFiles[3];
            }
        } catch (NullPointerException | ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        return null;
    }
    private File getDraggedGif(){
        try {
            if (subDirectory.equals("stitch")) {
                return gifFiles[4];
            } else if (subDirectory.equals("spongebob")) {
                return gifFiles[4];
            }else if (subDirectory.equals("girl1")){
                return gifFiles[4];
            }else if (subDirectory.equals("girl2")){
                return gifFiles[4];
            }else if (subDirectory.equals("kirby")){
                return gifFiles[4];
            }
        } catch (NullPointerException | ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        return null;
    }
    private File[] getRandomGifFiles() {
        File[] randomImageIconsFiles = new File[3];
        try {
            if (subDirectory.equals("stitch")) {
                randomImageIconsFiles[2]=gifFiles[0];
                randomImageIconsFiles[0] = gifFiles[1];
                randomImageIconsFiles[1] = gifFiles[5];
                return randomImageIconsFiles;
            } else if (subDirectory.equals("spongebob")) {
                randomImageIconsFiles[2]=gifFiles[0];
                randomImageIconsFiles[0] = gifFiles[1];
                randomImageIconsFiles[1] = gifFiles[5];
                return randomImageIconsFiles;
            }else if (subDirectory.equals("girl1")){
                randomImageIconsFiles[2]=gifFiles[0];
                randomImageIconsFiles[0] = gifFiles[1];
                randomImageIconsFiles[1] = gifFiles[5];
                return randomImageIconsFiles;
            }else if (subDirectory.equals("girl2")){
                randomImageIconsFiles[2]=gifFiles[0];
                randomImageIconsFiles[0] = gifFiles[1];
                randomImageIconsFiles[1] = gifFiles[5];
                return randomImageIconsFiles;
            }else if (subDirectory.equals("kirby")){
                randomImageIconsFiles[2]=gifFiles[0];
                randomImageIconsFiles[0] = gifFiles[1];
                randomImageIconsFiles[1] = gifFiles[5];
                return randomImageIconsFiles;
            }
        } catch (NullPointerException | ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        return null;
    }
}
