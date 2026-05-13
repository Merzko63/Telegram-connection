package ru.merzko63.telegramconnection.modules;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigManager {
    private final JavaPlugin plugin;
    private FileConfiguration config;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
        this.config = plugin.getConfig();
    }

    public void reload() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    public String getLanguage() {
        return config.getString("language", "en");
    }

    public String getBotToken() {
        return config.getString("bot-token");
    }

    public String getChatId() {
        return config.getString("chat-id");
    }

    public String getApiUrl() {
        return config.getString("api-url");
    }

    public boolean broadcastChat() {
        return config.getBoolean("minecraft.broadcast-chat", true);
    }

    public boolean broadcastDeath() {
        return config.getBoolean("minecraft.broadcast-death", true);
    }

    public boolean broadcastJoin() {
        return config.getBoolean("minecraft.broadcast-join", true);
    }

    public boolean broadcastQuit() {
        return config.getBoolean("minecraft.broadcast-quit", true);
    }

    public boolean broadcastAchievements() {
        return config.getBoolean("minecraft.broadcast-achievements", true);
    }

    public boolean broadcastServerStart() {
        return config.getBoolean("minecraft.broadcast-server-start", true);
    }

    public String getFormatFromMinecraft() {
        return config.getString("telegram.format-from-minecraft", "<%rank%%player%> %message%");
    }

    public String getFormatToMinecraft() {
        return config.getString("telegram.format-to-minecraft", "[TG] &f<%2$s&f> %1$s");
    }

    public boolean isColorsEnabled() {
        return config.getBoolean("telegram.colors-enabled", true);
    }

    public String getTabResponse() {
        return config.getString("telegram-commands.tab-response", "Online players (%count%): %list%");
    }

    public String getNoPlayersText() {
        return config.getString("telegram-commands.no-players", "empty");
    }

    // Telegram media messages
    public String getTelegramStickerMessage() {
        return config.getString("telegram-messages.sticker", "📖 sent a sticker");
    }

    public String getTelegramGifMessage() {
        return config.getString("telegram-messages.gif", "🎬 sent a GIF");
    }

    public String getTelegramPhotoMessage() {
        return config.getString("telegram-messages.photo", "🖼 sent a photo");
    }

    public String getTelegramVideoMessage() {
        return config.getString("telegram-messages.video", "🎥 sent a video");
    }

    public String getTelegramVoiceMessage() {
        return config.getString("telegram-messages.voice", "🎤 sent a voice message");
    }

    public String getTelegramFileMessage() {
        return config.getString("telegram-messages.file", "📎 sent a file");
    }

    public String getTelegramAudioMessage() {
        return config.getString("telegram-messages.audio", "🎵 sent an audio");
    }

    public String getTelegramLocationMessage() {
        return config.getString("telegram-messages.location", "📍 sent a location");
    }

    public String getTelegramContactMessage() {
        return config.getString("telegram-messages.contact", "📇 sent a contact");
    }

    public String getTelegramPollMessage() {
        return config.getString("telegram-messages.poll", "📊 sent a poll");
    }

    public String getTelegramDiceMessage() {
        return config.getString("telegram-messages.dice", "🎲 threw a dice");
    }

    public String getTelegramGameMessage() {
        return config.getString("telegram-messages.game", "🎮 sent a game invite");
    }
}