package net.mika;

import com.google.gson.*;
import java.util.*;
import java.io.*;
import java.nio.file.*;
import javax.swing.*;

public class ThreadForGame extends Thread {
    private java.util.List<String> libraryPath;
    private java.util.List<String> forgeModulesPath;
    private final File gameDir;
    private final Map<String, String> versionsUrls;
    private final Map<String, String> authlibUrls;
    private final Map<String, String> mcpVersions;
    private final Map<String, String> forgeVersions;
    private String mainClass;
    private String assetIndexUrl;
    private String assetIndexId;
    private String username;
    private final Version version;
    private String accessToken;
    private String xmx;
    private String xms;
    private char key;
    public static String launchWrapperUrl = "https://libraries.minecraft.net/net/minecraft/launchwrapper/1.12/launchwrapper-1.12.jar";

    public ThreadForGame(java.util.List<String> libraryPaths, File gameDir, Map<String, String> versionsUrls, Map<String, String> authlibUrls, String assetIndexUrl,
                         String assetIndexId, Version version, char key) {
        this.libraryPath = libraryPaths;
        this.gameDir = gameDir;
        this.versionsUrls = versionsUrls;
        this.authlibUrls = authlibUrls;
        this.assetIndexUrl = assetIndexUrl;
        this.assetIndexId = assetIndexId;
        this.version = version;
        this.key = key;
        this.username = "";
        this.mcpVersions = new HashMap<>();
        mcpVersions.put("1.20.1", "20230612.114412");
        mcpVersions.put("1.19.2", "20220805.130853");
        mcpVersions.put("1.18.2", "20220404.173914");
        mcpVersions.put("1.17.1", "20210706.113038");
        mcpVersions.put("1.16.5", "20210115.111550");
        mcpVersions.put("1.13.2", "20190213.203750");
        this.forgeVersions = new HashMap<>();
        forgeVersions.put("1.20.1", "47.3.33");
        forgeVersions.put("1.19.2", "43.4.20");
        forgeVersions.put("1.18.2", "40.3.6");
        forgeVersions.put("1.17.1", "37.1.1");
        forgeVersions.put("1.16.5", "36.2.42");
        forgeVersions.put("1.13.2", "25.0.223");
    }

    public static boolean isNewForge(String version) {
        String[] parts = version.split("\\.");

        int major = Integer.parseInt(parts[0]);
        int minor = Integer.parseInt(parts[1]);

        return (major == 1 && minor >= 13);
    }

    public static boolean isNewForgeJava8(String version) {
        String[] parts = version.split("\\.");
        int major = Integer.parseInt(parts[0]);
        int minor = Integer.parseInt(parts[1]);
        return (major == 1 && minor >= 13) && (major == 1 && minor < 17);
    }

    public void loadSettings() {
        Properties properties = new Properties();

        try (InputStream is = new FileInputStream("game\\game.properties")) {
            properties.load(is);

            xmx = properties.getProperty("Xmx", "1G");
            xms = properties.getProperty("Xms", "256M");
            LoggerUtil.info("Xmx: " + xmx);
            LoggerUtil.info("Xms: " + xms);
        } catch (IOException e) {
            LoggerUtil.info("Got an error :(");
            LoggerUtil.error(LoggerUtil.getStackTraceAsString(e));
        }
    }

    public static String getJavaVersion(String mcVersion) {
        if (mcVersion.matches("1\\.([0-6]|1[0-6])(?:\\..*)?")) {
            return "java8\\bin\\java.exe";
        } else if (mcVersion.matches("1\\.17(?:\\..*)?")) {
            return "java16\\bin\\java.exe";
        } else if (mcVersion.matches("1\\.1[8-9](?:\\..*)?|1\\.20\\.[0-1]")) {
            return "java17\\bin\\java.exe";
        } else if (mcVersion.matches("1\\.20\\.2(?:\\..*)?|1\\.2[1-9](?:\\..*)?")) {
            return "java21\\bin\\java.exe";
        } else {
            return "Bro what are you launching?";
        }
    }

    @Override
    public void run() {
        loadSettings();
        libraryPath = new ArrayList<>();
        forgeModulesPath = new ArrayList<>();
        String versionUrl = versionsUrls.get(version.stringVersion);
        File librariesDirectory = new File("game\\libraries\\" + version.stringVersion);
        File versionDirectory = new File("game\\versions\\" + version.stringVersion);
        File assetsDirectory = new File("game\\assets");
        File nativesDirectory = new File("game\\versions\\" + version.stringVersion + "\\" + "natives");

        key = JOptionPane.showInputDialog("Enter your key: ").charAt(0);

        String encryptedAccessToken = FileUtil.readFromFile("game\\token");

        accessToken = FileUtil.xorEncryptDecrypt(encryptedAccessToken, key);

        String encryptedUsername = FileUtil.readFromFile("game\\username");

        username = FileUtil.xorEncryptDecrypt(encryptedUsername, key);

        JsonObject versionJson = FileUtil.getJsonFromUrl(versionUrl);

        if (versionJson.has("assetIndex")) {
            JsonObject assetIndex = versionJson.getAsJsonObject("assetIndex");
            assetIndexId = assetIndex.get("id").getAsString();
            assetIndexUrl = assetIndex.get("url").getAsString();
            try {
                if (!versionDirectory.isDirectory() || !librariesDirectory.isDirectory() || !assetsDirectory.isDirectory() || !nativesDirectory.isDirectory()) {
                    librariesDirectory.mkdirs();
                    versionDirectory.mkdirs();
                    assetsDirectory.mkdirs();
                    nativesDirectory.mkdirs();

                    JsonArray libraries = versionJson.getAsJsonArray("libraries");

                    if (versionJson.has("downloads")) {
                        JsonObject clientDownloads = versionJson.getAsJsonObject("downloads");
                        JsonObject client = clientDownloads.getAsJsonObject("client");
                        String url = client.get("url").getAsString();
                        FileUtil.downloadFile(url, versionDirectory.getAbsolutePath() + "\\" + version.stringVersion + ".jar");
                    }

                    for (int i = 0; i < libraries.size(); i++) {
                        JsonObject library = libraries.get(i).getAsJsonObject();
                        if (library.has("downloads")) {
                            boolean isDownloadedLwjgl = false;
                            String name = library.get("name").getAsString();
                            String[] parts = name.split(":");
                            JsonObject downloads = library.getAsJsonObject("downloads");
                            if (downloads.has("artifact")) {
                                JsonObject artifact = downloads.getAsJsonObject("artifact");
                                String path = artifact.get("path").getAsString();
                                String urlArtifact = artifact.get("url").getAsString();
                                if (!parts[1].startsWith("lwjgl") || !parts[2].equals("3.2.2")) {
                                    FileUtil.downloadFile(urlArtifact, librariesDirectory.getAbsolutePath() + "\\" + path);
                                }
                            }
                            if (downloads.has("classifiers") && downloads.getAsJsonObject("classifiers").has("natives-windows") && !library.get("name").getAsString().equals("org.lwjgl:lwjgl:3.2.2")) {
                                JsonObject nativeWindows = downloads.getAsJsonObject("classifiers").getAsJsonObject("natives-windows");
                                String pathWithDirectories = nativeWindows.get("path").getAsString();
                                File pathWithoutDirectories = new File(pathWithDirectories);
                                String path = pathWithoutDirectories.getName();
                                FileUtil.downloadFile(nativeWindows.get("url").getAsString(), nativesDirectory.getAbsolutePath() + "\\" + path);
                                FileUtil.extractJar(nativesDirectory.getAbsolutePath() + "\\" + path, nativesDirectory.getAbsolutePath() + "\\");
                                File fileForDelete = new File(nativesDirectory.getAbsolutePath() + "\\" + path);
                                fileForDelete.delete();
                            }
                            else if (!isDownloadedLwjgl && library.get("name").getAsString().equals("org.lwjgl:lwjgl:3.2.2") && (version.stringVersion.substring(0, 4).equals("1.18") || version.stringVersion.substring(0, 4).equals("1.16") || version.stringVersion.substring(0, 4).equals("1.17"))) {
                                String url = "https://build.lwjgl.org/release/3.2.1/bin/lwjgl/lwjgl-natives-windows.jar";
                                String savepath = nativesDirectory.getAbsolutePath() + "\\lwjgl.jar";
                                FileUtil.downloadFile(url, savepath);
                                File fileForDelete = new File(savepath);
                                FileUtil.extractJar(fileForDelete.getAbsolutePath(), nativesDirectory.getAbsolutePath() + "\\");
                                fileForDelete.delete();
                                isDownloadedLwjgl = true;
                            }
                        }
                    }

                    if (version.isForge && !isNewForge(version.stringVersion)) {
                        FileUtil.downloadFile(launchWrapperUrl, librariesDirectory.getAbsolutePath() + "\\launchwrapper.jar");
                    }

                    if (version.isForge) {
                        JsonObject forgeLibraries = FileUtil.getJsonFromFile("jsons\\" + version.stringVersion + "\\forge.json");
                        if (forgeLibraries.has("mainClass")) {
                            mainClass = forgeLibraries.get("mainClass").getAsString();
                            LoggerUtil.info("mainClass set to: " + mainClass);
                        }
                        if (forgeLibraries != null && forgeLibraries.has("id") &&
                                Objects.equals(forgeLibraries.get("id").getAsString(), version.stringVersion + "-forge") &&
                                forgeLibraries.has("libraries")) {
                            JsonArray array = forgeLibraries.getAsJsonArray("libraries"); // виправлено
                            for (JsonElement element : array) {
                                JsonObject library = element.getAsJsonObject();
                                if (library.has("downloads") && library.getAsJsonObject("downloads").has("artifact")) {
                                    JsonObject artifact = library.getAsJsonObject("downloads").getAsJsonObject("artifact");
                                    if (artifact.has("url") && (artifact.has("path") || library.has("name"))) {
                                        String url = artifact.get("url").getAsString();
                                        String path = artifact.has("path") ? artifact.get("path").getAsString() : GameThings.getPathFromMavenName(library.get("name").getAsString());
                                        FileUtil.downloadFile(url, librariesDirectory.getAbsolutePath() + "\\" + path);
                                    }
                                }
                            }
                        }
                    }

                    if (version.isFabric) {
                        JsonObject fabricLibraries = FileUtil.getJsonFromFile("jsons\\" + version.stringVersion + "\\fabric.json");
                        if (fabricLibraries.has("mainClass")) {
                            mainClass = fabricLibraries.get("mainClass").getAsString();
                            LoggerUtil.info("mainClass set to: " + mainClass);
                        }
                        if (fabricLibraries != null && fabricLibraries.has("id") && Objects.equals(fabricLibraries.get("id").getAsString(), version.stringVersion + "-fabric") && fabricLibraries.has("libraries")) {
                            JsonArray array = fabricLibraries.getAsJsonArray("libraries");
                            for (JsonElement element : array) {
                                JsonObject library = element.getAsJsonObject();
                                if (library.has("downloads") && library.getAsJsonObject("downloads").has("artifact")) {
                                    JsonObject artifact = library.getAsJsonObject("downloads").getAsJsonObject("artifact");
                                    if (artifact.has("url") && artifact.has("path")) {
                                        String url = artifact.get("url").getAsString();
                                        String path = artifact.get("path").getAsString();
                                        FileUtil.downloadFile(url, librariesDirectory.getAbsolutePath() + "\\" + path);
                                    }
                                }
                            }
                        }
                    }

                    GameThings.downloadAssetIndex(assetIndexId, assetsDirectory.getAbsolutePath(), assetIndexUrl);
                    GameThings.downloadAssets(assetsDirectory.getAbsolutePath(), assetIndexId);

                }

                try {
                    FileUtil.downloadFile(authlibUrls.get(version.stringVersion), "game\\authlib.zip");
                    File newAuthLib = FileUtil.extractJarFromZip("game\\authlib.zip");
                    File libDirectory = new File(librariesDirectory.getAbsolutePath() + "\\com\\mojang\\authlib\\");
                    File[] dirs = libDirectory.listFiles();
                    String libVersion = "";
                    if (dirs != null) {
                        for (File dir : dirs) {
                            if (dir.isDirectory()) {
                                libVersion = dir.getName();
                                break;
                            }
                        }
                    }
                    File oldAuthLib = new File(libDirectory.getAbsolutePath() + "\\" + libVersion + "\\authlib-" + libVersion + ".jar");
                    oldAuthLib.delete();
                    Files.move(Paths.get(newAuthLib.getAbsolutePath()), Paths.get(oldAuthLib.getAbsolutePath()));
                } catch (IOException e) {
                    LoggerUtil.info("Got an error :(");
                    LoggerUtil.error(LoggerUtil.getStackTraceAsString(e));
                }

                ModulesAndClasses.findJarFiles(librariesDirectory, libraryPath, false);
                ModulesAndClasses.findJarFiles(versionDirectory, libraryPath, false);
                String classPath = String.join(";", libraryPath);

                if (version.isFabric) {
                    JsonObject fabricLibraries = FileUtil.getJsonFromFile("jsons\\" + version.stringVersion + "\\fabric.json");
                    if (fabricLibraries.has("mainClass")) {
                        mainClass = fabricLibraries.get("mainClass").getAsString();
                        LoggerUtil.info("mainClass set to: " + mainClass);
                    }
                }

                if (version.isForge) {
                    ModulesAndClasses.findJarFiles(librariesDirectory, forgeModulesPath, true);
                    JsonObject forgeLibraries = FileUtil.getJsonFromFile("jsons\\" + version.stringVersion + "\\forge.json");
                    if (forgeLibraries.has("mainClass")) {
                        mainClass = forgeLibraries.get("mainClass").getAsString();
                        LoggerUtil.info("mainClass set to: " + mainClass);
                    }
                }

                LoggerUtil.info(new File(getJavaVersion(version.stringVersion)).getAbsolutePath());

                ProcessBuilder processBuilder;

                LoggerUtil.info(username);
                String uuid = GameThings.getUuid(username);
                LoggerUtil.info(uuid);

                if (!version.isForge && !version.isFabric) {
                    try {
                        LoggerUtil.info(classPath);
                        processBuilder = new ProcessBuilder(new File(getJavaVersion(version.stringVersion)).getAbsolutePath(), "-Xms" + xms, "-Xmx" + xmx, "-Djava.library.path=" + nativesDirectory.getAbsolutePath(), "-Dfile.encoding=UTF-8", "-Xss2M", "-cp", classPath, "net.minecraft.client.main.Main", "--username", username, "--accessToken", accessToken, "--uuid", uuid, "--gameDir", gameDir.getAbsolutePath(), "--assetsDir", assetsDirectory.getAbsolutePath(), "--version", version.stringVersion, "--assetIndex", assetIndexId);
                        processBuilder.inheritIO();
                        Process process = processBuilder.start();
                        key = ' ';
                        accessToken = "";
                    } catch (IOException e) {
                        LoggerUtil.info("Got an error :(");
                        LoggerUtil.error(LoggerUtil.getStackTraceAsString(e));
                    }
                }
                else if (version.isFabric) {
                    try {
                        processBuilder = new ProcessBuilder(getJavaVersion(version.stringVersion), "-Xms" + xms, "-Xmx" + xmx, "-Djava.library.path=" + nativesDirectory.getAbsolutePath(), "-Dfile.encoding=UTF-8", "-Xss2M", "-cp", classPath, mainClass, "--username", username, "--accessToken", accessToken, "--uuid", uuid, "--gameDir", "game", "--assetsDir", assetsDirectory.getAbsolutePath(), "--version", version.stringVersion, "--assetIndex", assetIndexId);
                        processBuilder.inheritIO();
                        Process process = processBuilder.start();
                        accessToken = "";
                        key = ' ';
                    } catch (IOException e) {
                        LoggerUtil.info("Got an error :(");
                        LoggerUtil.error(LoggerUtil.getStackTraceAsString(e));
                    }
                }
                else {
                    if (!isNewForge(version.stringVersion)) {
                        try {
                            processBuilder = new ProcessBuilder(getJavaVersion(version.stringVersion), "-Xmx" + xmx, "-Xms" + xms, "-Djava.library.path=" + nativesDirectory.getAbsolutePath(), "-Dfile.encoding=UTF-8", "-Xss2M", "-cp", classPath, mainClass, "--username", username, "--accessToken", accessToken, "--uuid", uuid, "--tweakClass", "net.mika.MikaTweaker", "--version", version.stringVersion,  "--gameDir", gameDir.getAbsolutePath(), "--assetsDir", assetsDirectory.getAbsolutePath(), "--assetIndex", assetIndexId);
                            processBuilder.inheritIO();
                            Process process = processBuilder.start();
                            accessToken = "";
                            key = ' ';
                        } catch (IOException e) {
                            LoggerUtil.info("Got an error :(");
                            LoggerUtil.error(LoggerUtil.getStackTraceAsString(e));
                        }
                    }
                    else {
                        if (!isNewForgeJava8(version.stringVersion)) {
                            try {
                                String modulePath = String.join(";", forgeModulesPath);
                                processBuilder = new ProcessBuilder(getJavaVersion(version.stringVersion), "-DignoreList=bootstraplauncher,securejarhandler,asm-commons,asm-util,asm-analysis,asm-tree,asm,JarJarFileSystems,client-extra,client,fmlcore,javafmllanguage,lowcodelanguage,mclanguage,forge-," + version.stringVersion + ".jar", "--add-exports", "java.base/sun.security.util=ALL-UNNAMED", "--add-opens", "java.base/java.util.jar=cpw.mods.securejarhandler", "--add-opens", "java.base/java.lang.invoke=cpw.mods.securejarhandler", "--add-modules", "ALL-MODULE-PATH", "-DlibraryDirectory=" + librariesDirectory.getAbsolutePath(), "-Xmx" + xmx, "-Xms" + xms, "-Djava.library.path=" + nativesDirectory.getAbsolutePath(), "-Dfile.encoding=UTF-8", "-Xss2M", "-cp", classPath, "-p", modulePath, mainClass, "--version", "Forge " + version.stringVersion, "--launchTarget", "forgeclient", "--fml.forgeVersion", forgeVersions.get(version.stringVersion), "--fml.mcVersion", version.stringVersion, "--fml.forgeGroup", "net.minecraftforge", "--fml.mcpVersion", mcpVersions.get(version.stringVersion), "--username", username, "--accessToken", accessToken, "--uuid", uuid,  "--userType", "mojang", "--versionType", "modified", "--clientId", " ", "--xuid", " ", "--gameDir", gameDir.getAbsolutePath(), "--assetsDir", assetsDirectory.getAbsolutePath(), "--assetIndex", assetIndexId);
                                processBuilder.inheritIO();
                                Process process = processBuilder.start();
                                accessToken = "";
                                key = ' ';
                            } catch (IOException e) {
                                LoggerUtil.info("Got an error :(");
                                LoggerUtil.error(LoggerUtil.getStackTraceAsString(e));
                            }
                        }
                        else {
                            try {
                                processBuilder = new ProcessBuilder(getJavaVersion(version.stringVersion), "-Xmx" + xmx, "-Xms" + xms, "-Djava.library.path=" + nativesDirectory.getAbsolutePath(), "-Dfile.encoding=UTF-8", "-Xss2M", "-cp", classPath, mainClass, "--version", "Forge " + version.stringVersion, "--launchTarget", "fmlclient", "--fml.forgeVersion", forgeVersions.get(version.stringVersion), "--fml.mcVersion", version.stringVersion, "--fml.forgeGroup", "net.minecraftforge", "--fml.mcpVersion", mcpVersions.get(version.stringVersion), "--username", username, "--accessToken", accessToken, "--gameDir", gameDir.getAbsolutePath(), "--assetsDir", assetsDirectory.getAbsolutePath(), "--assetIndex", assetIndexId);
                                processBuilder.inheritIO();
                                Process process = processBuilder.start();
                                accessToken = "";
                                key = ' ';
                            } catch (IOException e) {
                                LoggerUtil.info("Got an error :(");
                                LoggerUtil.error(LoggerUtil.getStackTraceAsString(e));
                            }
                        }
                    }
                }
            } catch (Exception e) {
                LoggerUtil.info("Got an error :(");
                LoggerUtil.error(LoggerUtil.getStackTraceAsString(e));
            }
        }
    }
}
