package net.mika;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.*;
import java.io.*;


// SCARIEST CLASS

public class Gui {
    private static String username;
    private static String password;
    private static String accessToken;
    private static Version version;
    private static char key;
    private static java.util.List<String> libraryPath;
    private static File gameDirectory;
    private static Map<String, String> versionsUrls;
    private static Map<String, String> authlibUrls;
    private static String assetIndexUrl;
    private static String assetIndexId;

    public static void init() {
        versionsUrls = new HashMap<>();
        versionsUrls.put("1.12.2", "https://piston-meta.mojang.com/v1/packages/832d95b9f40699d4961394dcf6cf549e65f15dc5/1.12.2.json");
        versionsUrls.put("1.13.2", "https://piston-meta.mojang.com/v1/packages/fa3ddc22146c46bfeb0e9d322c6f83b937e25005/1.13.2.json");
        versionsUrls.put("1.14", "https://piston-meta.mojang.com/v1/packages/0fd01dd81eaa451d3130b1cf025a10f129585b10/1.14.json");
        versionsUrls.put("1.16.5", "https://piston-meta.mojang.com/v1/packages/fba9f7833e858a1257d810d21a3a9e3c967f9077/1.16.5.json");
        versionsUrls.put("1.17.1", "https://piston-meta.mojang.com/v1/packages/e0e7ab5ed6f55bbd874ef95be3c9356d67e64b57/1.17.1.json");
        versionsUrls.put("1.18", "https://piston-meta.mojang.com/v1/packages/7367ea8b7cad7c7830192441bb2846be0d2ceeac/1.18.json");
        versionsUrls.put("1.18.1", "https://piston-meta.mojang.com/v1/packages/7ff864e988a2c29907154d5f9701e87e5d5e554a/1.18.1.json");
        versionsUrls.put("1.18.2", "https://piston-meta.mojang.com/v1/packages/334b33fcba3c9be4b7514624c965256535bd7eba/1.18.2.json");
        versionsUrls.put("1.19.2", "https://piston-meta.mojang.com/v1/packages/ed548106acf3ac7e8205a6ee8fd2710facfa164f/1.19.2.json");
        versionsUrls.put("1.20.1", "https://piston-meta.mojang.com/v1/packages/d8f8a41e5d63fec111ecab875fe01cc4d2bd96cc/1.20.1.json");
        authlibUrls = new HashMap<>();
        authlibUrls.put("1.20.1", "https://ely.by/load/system?minecraftVersion=1.20-authlib");
        authlibUrls.put("1.19.2", "https://ely.by/load/system?minecraftVersion=1.19.2-authlib");
        authlibUrls.put("1.12.2", "https://ely.by/load/system?minecraftVersion=1.12-1.15-authlib");
        authlibUrls.put("1.18.2", "https://ely.by/load/system?minecraftVersion=1.18.2-authlib");
        gameDirectory = new File("game\\");
        gameDirectory.mkdirs();
    }

    public static void createAndShowGUI() {
        JFrame frame = new JFrame("Mika Launcher");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 400);
        frame.setLayout(new CardLayout());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBackground(Color.getHSBColor(82.96f / 360, 80.0f / 100, 96.27f / 100));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        JLabel titleLabel = new JLabel("Mika Launcher");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        mainPanel.add(titleLabel, gbc);

        JLabel versionLabel = new JLabel("Select Version:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        mainPanel.add(versionLabel, gbc);

        String[] versions = {"1.20.1", "Forge 1.20.1", "Fabric 1.20.1", "1.19.2", "Fabric 1.19.2", "Forge 1.19.2",  "1.18.2", "Forge 1.18.2", "Fabric 1.18.2", "1.18.1", "1.18", "1.17.1", "Forge 1.17.1", "1.16.5", "Fabric 1.16.5", "Forge 1.16.5", "1.14", "1.13.2", "Forge 1.13.2", "1.12.2", "Forge 1.12.2"};
        JComboBox<String> versionComboBox = new JComboBox<>(versions);
        versionComboBox.setBackground(Color.CYAN);
        versionComboBox.addActionListener(e -> setVersion((String)versionComboBox.getSelectedItem()));
        gbc.gridx = 1;
        mainPanel.add(versionComboBox, gbc);

        JButton launchButton = new JButton("Launch");
        launchButton.setBackground(Color.CYAN);
        boolean isForge = false;
        launchButton.addActionListener(e -> {
            Thread launchGame = new ThreadForGame(libraryPath, gameDirectory, versionsUrls, authlibUrls, assetIndexUrl, assetIndexId, version, key);
            launchGame.start();
        });
        gbc.gridx = 0;
        gbc.gridy = 4; // Позиція для кнопки Launch
        gbc.gridwidth = 2;
        mainPanel.add(launchButton, gbc);

        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new GridBagLayout());
        optionsPanel.setBackground(Color.getHSBColor(82.96f / 360, 80.0f / 100, 96.27f / 100));
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        JLabel nicknameLabel = new JLabel("Enter your nickname:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        optionsPanel.add(nicknameLabel, gbc);

        JTextField nicknameField = new JTextField(20);
        nicknameField.setBackground(Color.CYAN);
        nicknameField.addActionListener(e -> {
            username = nicknameField.getText();
        });
        gbc.gridx = 1;
        optionsPanel.add(nicknameField, gbc);

        JButton backButton = new JButton("Back to Main");
        backButton.setBackground(Color.CYAN);
        backButton.addActionListener(e -> showMainPanel(frame));
        gbc.gridx = 0;
        gbc.gridy = 4;
        optionsPanel.add(backButton, gbc);

        JLabel passwordLabel = new JLabel("Enter your password:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        optionsPanel.add(passwordLabel, gbc);

        JTextField passwordField = new JTextField(20);
        passwordField.setBackground(Color.CYAN);
        passwordField.addActionListener(e -> {
            password = passwordField.getText();
        });
        gbc.gridx = 1;
        optionsPanel.add(passwordField, gbc);

        JTextField keyField = new JTextField(20);
        keyField.setBackground(Color.CYAN);
        keyField.addActionListener(e -> {
            key = keyField.getText().charAt(0);
        });
        gbc.gridx = 1;
        gbc.gridy = 2;
        optionsPanel.add(keyField, gbc);

        JLabel keyLabel = new JLabel("Enter your key:");
        gbc.gridx = 0;
        optionsPanel.add(keyLabel, gbc);

        JLabel xmxLabel = new JLabel("Max memory (Xmx):");
        gbc.gridx = 0;
        gbc.gridy = 6;
        optionsPanel.add(xmxLabel, gbc);

        JTextField xmxField = new JTextField(20);
        xmxField.setBackground(Color.CYAN);
        gbc.gridx = 1;
        optionsPanel.add(xmxField, gbc);

        JLabel xmsLabel = new JLabel("Initial memory (Xms):");
        gbc.gridx = 0;
        gbc.gridy = 7;
        optionsPanel.add(xmsLabel, gbc);

        JTextField xmsField = new JTextField(20);
        xmsField.setBackground(Color.CYAN);
        gbc.gridx = 1;
        optionsPanel.add(xmsField, gbc);

        JButton saveButton = new JButton("Save");
        saveButton.setBackground(Color.CYAN);
        saveButton.addActionListener(e -> {
            accessToken = GameThings.authenticateWithElyBy(username, password);
            String encryptedToken = FileUtil.xorEncryptDecrypt(accessToken, key);
            FileUtil.saveToFile("game\\token", encryptedToken);
            String encryptedUsername = FileUtil.xorEncryptDecrypt(username, key);
            FileUtil.saveToFile("game\\username", encryptedUsername);

            File properties = new File("game\\game.properties");
            if (properties.exists()) {
                properties.delete();
                try {
                    properties.createNewFile();
                } catch (IOException ex) {
                    LoggerUtil.info("Got an error :(");
                    LoggerUtil.error(LoggerUtil.getStackTraceAsString(ex));
                }
            }
            else {
                try {
                    properties.createNewFile();
                } catch (IOException ex) {
                    LoggerUtil.info("Got an error :(");
                    LoggerUtil.error(LoggerUtil.getStackTraceAsString(ex));
                }
            }

            String xmx = xmxField.getText();
            String xms = xmsField.getText();
            saveMemorySettings(xmx, xms);
        });
        gbc.gridy = 3;
        optionsPanel.add(saveButton, gbc);

        JButton optionsButton = new JButton("Options");
        optionsButton.setBackground(Color.CYAN);
        optionsButton.addActionListener(e -> showOptionsPanel(frame));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5; // Позиція для кнопки Options
        gbc.gridwidth = 2;
        mainPanel.add(optionsButton, gbc);

        frame.add(mainPanel, "Main");
        frame.add(optionsPanel, "Options");
        frame.setVisible(true);
    }

    public static void saveMemorySettings(String xmx, String xms) {
        try {
            File configFile = new File("game\\game.properties");
            if (!configFile.exists()) {
                configFile.createNewFile();
            }
            FileWriter writer = new FileWriter(configFile, true);
            writer.write("Xmx=" + xmx + "\n");
            writer.write("Xms=" + xms + "\n");
            writer.close();
        } catch (IOException e) {
            LoggerUtil.info("Got an error :(");
            LoggerUtil.error(LoggerUtil.getStackTraceAsString(e));
        }
    }

    public static void showMainPanel(JFrame frame) {
        CardLayout cl = (CardLayout)frame.getContentPane().getLayout();
        cl.show(frame.getContentPane(), "Main");
    }

    public static void showOptionsPanel(JFrame frame) {
        CardLayout cl = (CardLayout)frame.getContentPane().getLayout();
        cl.show(frame.getContentPane(), "Options");
    }

    public static void setVersion(String stringVersion) {
        version = GameThings.fromStringToVersion(stringVersion);
    }
}
