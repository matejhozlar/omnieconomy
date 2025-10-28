package com.saunhardy.omnieconomy.telemetry;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class TelemetryClient {
    private static final Gson GSON = new Gson();

    private static final String TELEMETRY_ENDPOINT = "http://localhost:5002/api/telemetry";
    private static final int TIMEOUT_MS = 5000;

    public static CompletableFuture<Boolean> sendHeartbeat(UUID serverId, String modVersion) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                JsonObject payload = new JsonObject();
                payload.addProperty("serverId", serverId.toString());
                payload.addProperty("modVersion", modVersion);
                payload.addProperty("timestamp", System.currentTimeMillis());
                payload.addProperty("type", "heartbeat");

                return sendRequest(payload);
            } catch (Exception e) {
                return false;
            }
        });
    }

    public static CompletableFuture<Boolean> sendRegistration(UUID serverId, String modVersion, String minecraftVersion) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                JsonObject payload = new JsonObject();
                payload.addProperty("serverId", serverId.toString());
                payload.addProperty("modVersion", modVersion);
                payload.addProperty("minecraftVersion", minecraftVersion);
                payload.addProperty("timestamp", System.currentTimeMillis());
                payload.addProperty("type", "registration");

                return sendRequest(payload);
            } catch (Exception e) {
                return false;
            }
        });
    }

    private static boolean sendRequest(JsonObject payload) throws Exception {
        URI uri = new URI(TELEMETRY_ENDPOINT);
        HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();

        try {
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("User-Agent", "OmniEconomy-Telemetry/1.0");
            conn.setConnectTimeout(TIMEOUT_MS);
            conn.setReadTimeout(TIMEOUT_MS);
            conn.setDoOutput(true);

            String jsonString = GSON.toJson(payload);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            return responseCode >= 200 && responseCode < 300;
        } finally {
            conn.disconnect();
        }
    }
}