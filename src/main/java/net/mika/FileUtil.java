package net.mika;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.io.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileUtil {
    public static void extractJar(String jarFilePath, String outputDir) {
        try {
            JarFile jarFile = new JarFile(jarFilePath);
            Enumeration<JarEntry> entries = jarFile.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();

                if (entry.getName().endsWith(".dll")) {
                    File file = new File(outputDir + entry.getName());

                    file.getParentFile().mkdirs();

                    InputStream is = jarFile.getInputStream(entry);
                    FileOutputStream fos = new FileOutputStream(file);

                    byte[] buffer = new byte[1024];
                    int bytesRead;

                    while ((bytesRead = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                    }

                    fos.close();
                    is.close();

                    LoggerUtil.info("Extracted: " + outputDir + "\\" + file.getName());
                }
            }
        } catch (IOException e) {
            LoggerUtil.info("Got an error :(");
            LoggerUtil.error(LoggerUtil.getStackTraceAsString(e));
        }
    }

    public static File extractJarFromZip(String zipFilePath) throws IOException {
        File jarFile = null;
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(new File(zipFilePath).toPath()))) {
            ZipEntry zipEntry;
            while ((zipEntry = zis.getNextEntry()) != null) {
                if (zipEntry.getName().endsWith(".jar")) {
                    jarFile = new File(zipEntry.getName());
                    try (FileOutputStream fos = new FileOutputStream(jarFile)) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
            }
        }
        return jarFile;
    }

    public static String readFromFile(String filename) {
        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(new File(filename)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
        } catch (IOException e) {
            LoggerUtil.info("Got an error :(");
            LoggerUtil.error(LoggerUtil.getStackTraceAsString(e));
        }
        return result.toString();
    }

    public static void saveToFile(String filename, String data) {
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write(data);
        } catch (IOException e) {
            LoggerUtil.info("Got an error :(");
            LoggerUtil.error(LoggerUtil.getStackTraceAsString(e));
        }
    }

    public static String xorEncryptDecrypt(String text, char key) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            result.append((char)(text.charAt(i) ^ key));
        }
        return result.toString();
    }

    public static void downloadFile(String fileUrl, String savePath) {
        try {
            URL url = new URL(fileUrl);
            InputStream is = new BufferedInputStream(url.openStream());
            File file = new File(savePath);

            File parent = file.getParentFile();
            if (parent != null) {
                parent.mkdirs();
            }

            try (FileOutputStream fos = new FileOutputStream(file)) {
                byte[] buffer = new byte[4096]; // Збільшено буфер для ефективності
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) { // Перевірка на -1
                    fos.write(buffer, 0, bytesRead);
                }
                LoggerUtil.info("Downloaded: " + savePath);
            }
        } catch (IOException e) {
            LoggerUtil.error("Got an error :(");
            LoggerUtil.info(e.getMessage());
            LoggerUtil.error(LoggerUtil.getStackTraceAsString(e));
        }
    }

    public static JsonObject getJsonFromUrl(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream is = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder jsonString = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonString.append(line);
                }

                reader.close();
                is.close();
                return JsonParser.parseString(jsonString.toString()).getAsJsonObject();
            }
        } catch (Exception e) {
            LoggerUtil.info("Got an error :(");
            LoggerUtil.error(LoggerUtil.getStackTraceAsString(e));
        }
        return null;
    }

    public static JsonObject getJsonFromFile(String filepath) {
        try {
            String content = new String(Files.readAllBytes(Paths.get(filepath)));
            return JsonParser.parseString(content).getAsJsonObject();
        } catch (IOException e) {
            LoggerUtil.info("Got an error :(");
            LoggerUtil.error(LoggerUtil.getStackTraceAsString(e));
        }
        return null;
    }
}
