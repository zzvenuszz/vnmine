package com.vnmine.npc;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

/**
 * SkinFetcher - Lấy dữ liệu skin từ Mojang API
 */
public class SkinFetcher {

    private static final String UUID_API = "https://api.mojang.com/users/profiles/minecraft/";
    private static final String SESSION_API = "https://sessionserver.mojang.com/session/minecraft/profile/";

    /**
     * Lấy texture + signature từ tên người chơi
     * @return SkinData chứa texture và signature, null nếu lỗi
     */
    public static SkinData fetchSkin(String playerName) {
        try {
            // Bước 1: Lấy UUID từ tên
            String uuid = getUUID(playerName);
            if (uuid == null) return null;

            // Bước 2: Lấy profile chứa skin properties
            return getSkinData(uuid);
        } catch (Exception e) {
            return null;
        }
    }

    private static String getUUID(String playerName) throws Exception {
        URL url = new URL(UUID_API + playerName);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        conn.setRequestProperty("User-Agent", "VNMinePlugin/2.1.0");

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) return null;

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
        reader.close();

        String rawId = json.get("id").getAsString();
        // Chuyển về UUID format có dấu gạch
        return rawId.replaceFirst(
                "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{12})",
                "$1-$2-$3-$4-$5"
        );
    }

    private static SkinData getSkinData(String uuid) throws Exception {
        URL url = new URL(SESSION_API + uuid + "?unsigned=false");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        conn.setRequestProperty("User-Agent", "VNMinePlugin/2.1.0");

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) return null;

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
        reader.close();

        if (json.has("properties")) {
            var properties = json.getAsJsonArray("properties");
            for (int i = 0; i < properties.size(); i++) {
                JsonObject prop = properties.get(i).getAsJsonObject();
                if ("textures".equals(prop.get("name").getAsString())) {
                    String texture = prop.get("value").getAsString();
                    String signature = prop.has("signature") ? prop.get("signature").getAsString() : "";
                    return new SkinData(texture, signature);
                }
            }
        }
        return null;
    }

    /**
     * Dữ liệu skin: texture base64 + chữ ký
     */
    public static class SkinData {
        private final String texture;
        private final String signature;

        public SkinData(String texture, String signature) {
            this.texture = texture;
            this.signature = signature;
        }

        public String getTexture() { return texture; }
        public String getSignature() { return signature; }

        /**
         * Kiểm tra dữ liệu có hợp lệ không
         */
        public boolean isValid() {
            return texture != null && !texture.isEmpty();
        }
    }
}