package net.mika;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;


import java.io.*;
import java.net.URL;
import java.net.HttpURLConnection;
import java.nio.Buffer;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GameThings {
    public static Version fromStringToVersion(String item) {
        Version version = new Version("", false, false);
        if (item.contains("Forge ")) {
            version.isForge = true;
            version.isFabric = false;
            version.stringVersion = item.replace("Forge ", "");
        }
        else if (item.contains("Fabric ")) {
            version.isForge = false;
            version.isFabric = true;
            version.stringVersion = item.replace("Fabric ", "");
        }
        else {
            version.isForge = false;
            version.isFabric = false;
            version.stringVersion = item;
        }
        LoggerUtil.info("Selected version: " + version.stringVersion + " is Forge: " + version.isForge + " is Fabric: " + version.isFabric);
        return version;
    }

    public static String authenticateWithElyBy(String username, String password) {
        try {
            URL url = new URL("https://authserver.ely.by/auth/authenticate");
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("username", username);
            requestBody.addProperty("password", password);
            requestBody.addProperty("clientToken", "1");
            requestBody.addProperty("requestUser", false);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = reader.readLine()) != null) {
                response.append(responseLine.trim());
            }
            JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
            if (jsonResponse.has("accessToken")) {
                return jsonResponse.get("accessToken").getAsString();
            }
            else {
                LoggerUtil.error("Authentication failed: " + jsonResponse);
            }
            if (jsonResponse.has("errorMessage")) {
                LoggerUtil.info(jsonResponse.get("errorMessage").getAsString());
            }
        } catch (IOException e) {
            LoggerUtil.info("Got an error :(");
            LoggerUtil.error(LoggerUtil.getStackTraceAsString(e));
        }
        return null;
    }

    public static String getUuid(String username) {
        String apiUrl = "https://authserver.ely.by/api/users/profiles/minecraft/" + username;
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            JsonObject jsonObject = JsonParser.parseString(response.toString()).getAsJsonObject();

            return jsonObject.get("id").getAsString();
        } catch (Exception e) {
            LoggerUtil.info("Got an error :(");
            LoggerUtil.error(LoggerUtil.getStackTraceAsString(e));
        }
        return null;
    }

    public static void downloadAssetIndex(String assetIndex, String assetDirectory, String url) {
        File assetIndexFile = new File(assetDirectory + "\\indexes\\" + assetIndex + ".json");
        FileUtil.downloadFile(url, assetIndexFile.getAbsolutePath());
    }

    public static String getPathFromMavenName(String name) throws IllegalArgumentException {
        String[] parts = name.split(":");
        if (parts.length < 3) {
            throw new IllegalArgumentException("Invalid Maven name!");
        }

        String groupId = parts[0].replace(".", "/");
        String artifactId = parts[1];
        String version = parts[2];

        StringBuilder extraParts = new StringBuilder();
        for (int i = 3; i < parts.length; i++) {
            extraParts.append("-").append(parts[i]);
        }

        Path path = Paths.get(groupId, artifactId, version, artifactId + "-" + version + extraParts + ".jar");
        return path.toString();
    }

    public static void downloadAssets(String assetDirectory, String assetIndex) {
        try {
            File assetIndexFile = new File(assetDirectory + "\\indexes\\" + assetIndex + ".json");
            if (!assetIndexFile.exists()) {
                LoggerUtil.error("Failed to open asset index: " + assetIndexFile.getPath());
                return;
            }
            JsonObject assetIndexJson = JsonParser.parseString(new String(java.nio.file.Files.readAllBytes(assetIndexFile.toPath()))).getAsJsonObject();
            JsonObject objects = assetIndexJson.getAsJsonObject("objects");

            for (String key : objects.keySet()) {
                JsonObject asset = objects.getAsJsonObject(key);
                String assetHash = asset.get("hash").getAsString();
                String subDir = assetHash.substring(0, 2);
                File assetFile = new File(assetDirectory + "\\objects\\" + subDir + "\\" + assetHash);

                if (!assetFile.exists()) {
                    String assetUrl = "https://resources.download.minecraft.net/" + subDir + "/" + assetHash;
                    FileUtil.downloadFile(assetUrl, assetFile.getAbsolutePath());
                }
                else {
                    LoggerUtil.warn("Asset already exists: " + assetHash);
                }
            }
        } catch (IOException e) {
            LoggerUtil.info("Got an error :(");
            LoggerUtil.error(LoggerUtil.getStackTraceAsString(e));
        }
    }
}
