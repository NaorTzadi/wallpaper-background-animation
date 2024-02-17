package org.example;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.HashMap;
public class Menu extends JFrame {
    private final HashMap<String, File[]> subDirImageIconsMap = getSubDirectoryImageIconsMap();
    private final JLabel currentGifLabel;
    private int currentIndex = 0;
    private String currentSubDirectory;
    public Menu() {
        setIconImage(new ImageIcon("src/filesUtility/program-icon.png").getImage());
        setTitle("Graphic Wallpaper Setup");
        setSize(400, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setResizable(false);

        JComboBox<String> dropdown = new JComboBox<>(subDirImageIconsMap.keySet().toArray(new String[0]));
        dropdown.setMaximumSize(dropdown.getPreferredSize());
        dropdown.setAlignmentX(CENTER_ALIGNMENT);
        add(dropdown, BorderLayout.NORTH);

        currentGifLabel = new JLabel();
        add(currentGifLabel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));

        JButton enterButton = new JButton("ENTER");
        enterButton.setVisible(false);
        enterButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonPanel.add(enterButton);

        JPanel navigationPanel = new JPanel();
        JButton leftButton = new JButton("<");
        JButton rightButton = new JButton(">");
        rightButton.setVisible(false);
        leftButton.setVisible(false);
        navigationPanel.add(leftButton);
        navigationPanel.add(rightButton);
        buttonPanel.add(navigationPanel);

        add(buttonPanel, BorderLayout.SOUTH);

        leftButton.addActionListener(e -> navigateImage(-1));
        rightButton.addActionListener(e -> navigateImage(1));

        dropdown.addActionListener(e -> {
            currentSubDirectory = (String) dropdown.getSelectedItem();
            if (currentSubDirectory != null) {
                rightButton.setVisible(true);
                leftButton.setVisible(true);
                enterButton.setVisible(true);
                currentIndex = 0;
                updateImage();
            }
        });
        enterButton.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> {
                WallPaper wallpaper = new WallPaper(currentSubDirectory, subDirImageIconsMap.get(currentSubDirectory));
                wallpaper.setVisible(true);
            });
            dispose();
        });
        setVisible(true);
    }
    private void navigateImage(int direction) {
        if (currentSubDirectory != null && subDirImageIconsMap.containsKey(currentSubDirectory)) {
            File[] files = subDirImageIconsMap.get(currentSubDirectory);
            if (files != null && files.length > 0) {
                int length = files.length;
                currentIndex = (currentIndex + direction + length) % length;
                updateImage();
            }
        }
    }
    private void updateImage() {
        File[] files = subDirImageIconsMap.get(currentSubDirectory);
        if (files != null && files.length > 0) {
            File selectedFile = files[currentIndex];
            ImageIcon selectedIcon = new ImageIcon(selectedFile.getPath());
            if (selectedIcon.getIconWidth() > 350 || selectedIcon.getIconHeight() > 350) {
                ImageIcon[] resizedImageIcons = resizeImageIcons(new ImageIcon[]{selectedIcon}, new Dimension(350, 350));
                currentGifLabel.setIcon(resizedImageIcons[0]);
            } else {
                currentGifLabel.setIcon(selectedIcon);
            }
            repaint();
            revalidate();
        }
    }
    private ImageIcon[] resizeImageIcons(ImageIcon[] imageIcons, Dimension targetSize) {
        ImageIcon[] resizedIcons = new ImageIcon[imageIcons.length];
        for (int i = 0; i < imageIcons.length; i++) {
            ImageIcon originalIcon = imageIcons[i];
            Image originalImage = originalIcon.getImage();
            int originalWidth = originalImage.getWidth(null);
            int originalHeight = originalImage.getHeight(null);
            int scaledWidth = targetSize.width;
            int scaledHeight = (int) ((double) originalHeight / originalWidth * scaledWidth);
            Image scaledImage = originalImage.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
            resizedIcons[i] = new ImageIcon(scaledImage);
        }
        return resizedIcons;
    }
    private HashMap<String, File[]> getSubDirectoryImageIconsMap() {
        HashMap<String, File[]> subDirImageIconsMap = new HashMap<>();
        File directory = new File("src/gifs");
        if (directory.isDirectory()) {
            File[] subDirectories = directory.listFiles(File::isDirectory);
            if (subDirectories != null) {
                for (File subDirectory : subDirectories) {
                    File[] filesInSubDirectory = subDirectory.listFiles();
                    if (filesInSubDirectory != null && filesInSubDirectory.length > 0) {
                        subDirImageIconsMap.put(subDirectory.getName(), filesInSubDirectory);
                    }
                }
            }
        }
        return subDirImageIconsMap;
    }
}
