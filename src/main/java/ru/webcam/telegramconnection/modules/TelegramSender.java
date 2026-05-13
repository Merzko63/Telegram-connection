package ru.webcam.telegramconnection.modules;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class TelegramSender {
    private final JavaPlugin plugin;
    private final ConfigManager config;

    public TelegramSender(JavaPlugin plugin, ConfigManager config) {
        this.plugin = plugin;
        this.config = config;
    }

    public void sendMessage(String text) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            HttpURLConnection conn = null;
            try {
                String api = config.getApiUrl();
                String token = config.getBotToken();
                String chatId = config.getChatId();
                URL url = new URL(api + token + "/sendMessage");
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setConnectTimeout(5000);
                String data = "chat_id=" + chatId + "&text=" + URLEncoder.encode(text, "UTF-8");
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(data.getBytes(StandardCharsets.UTF_8));
                }
                conn.getResponseCode();
            } catch (Exception e) {
                plugin.getLogger().warning("[TG] Send error: " + e.getMessage());
            } finally {
                if (conn != null) conn.disconnect();
            }
        });
    }
}