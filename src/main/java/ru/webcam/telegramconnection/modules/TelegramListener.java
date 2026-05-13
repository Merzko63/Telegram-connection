package ru.webcam.telegramconnection.modules;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerAchievementAwardedEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.stream.Collectors;

public class TelegramListener implements Listener {
    private final JavaPlugin plugin;
    private final ConfigManager config;
    private final TelegramSender sender;
    private final TelegramCommands commands;
    private int lastUpdateId = 0;
    private boolean polling = true;
    private Thread pollingThread;

    public TelegramListener(JavaPlugin plugin, ConfigManager config, TelegramSender sender) {
        this.plugin = plugin;
        this.config = config;
        this.sender = sender;
        this.commands = new TelegramCommands(plugin, config, sender, this);
        startPolling();
    }

    public void restartPolling() {
        polling = false;
        if (pollingThread != null && pollingThread.isAlive()) {
            pollingThread.interrupt();
        }
        try { Thread.sleep(500); } catch (InterruptedException e) { }
        polling = true;
        startPolling();
    }

    private void startPolling() {
        pollingThread = new Thread(() -> {
            while (polling && !Thread.currentThread().isInterrupted()) {
                try {
                    String api = config.getApiUrl();
                    String token = config.getBotToken();
                    URL url = new URL(api + token + "/getUpdates?offset=" + (lastUpdateId + 1) + "&timeout=30");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(35000);

                    if (conn.getResponseCode() == 200) {
                        String response = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))
                                .lines().collect(Collectors.joining());
                        if (response.contains("\"update_id\":")) {
                            parseUpdates(response);
                            String[] parts = response.split("\"update_id\":");
                            lastUpdateId = Integer.parseInt(parts[parts.length - 1].split("[,}]")[0]);
                        }
                    }
                    conn.disconnect();
                } catch (Exception e) {
                    plugin.getLogger().warning("[TG] Polling error: " + e.getMessage());
                }
                try { Thread.sleep(1000); } catch (InterruptedException e) { break; }
            }
        });
        pollingThread.setDaemon(true);
        pollingThread.start();
    }

    private void parseUpdates(String response) {
        String[] updates = response.split("\\{\"update_id\":");
        for (String update : updates) {
            if (!update.contains("\"message\":{")) continue;

            String firstName = extract(update, "\"first_name\":\"", "\"");
            String text = extract(update, "\"text\":\"", "\"");
            if (text == null) text = extract(update, "\"caption\":\"", "\"");

            // Handle /tab command
            if (text != null && text.equalsIgnoreCase("/tab")) {
                sendOnlineListToTelegram();
                continue;
            }

            // Handle text message
            if (text != null && firstName != null && !text.startsWith("/")) {
                broadcastToMinecraft(formatTGMessage(firstName, text, "text"));
                continue;
            }

            // Handle sticker
            if (update.contains("\"sticker\":")) {
                String stickerMsg = config.getTelegramStickerMessage().replace("%player%", firstName);
                broadcastToMinecraft(formatTGMessage(firstName, stickerMsg, "sticker"));
                continue;
            }

            // Handle GIF (animation)
            if (update.contains("\"animation\":")) {
                String gifMsg = config.getTelegramGifMessage().replace("%player%", firstName);
                broadcastToMinecraft(formatTGMessage(firstName, gifMsg, "gif"));
                continue;
            }

            // Handle photo
            if (update.contains("\"photo\":")) {
                String photoMsg = config.getTelegramPhotoMessage().replace("%player%", firstName);
                broadcastToMinecraft(formatTGMessage(firstName, photoMsg, "photo"));
                continue;
            }

            // Handle video
            if (update.contains("\"video\":")) {
                String videoMsg = config.getTelegramVideoMessage().replace("%player%", firstName);
                broadcastToMinecraft(formatTGMessage(firstName, videoMsg, "video"));
                continue;
            }

            // Handle voice message
            if (update.contains("\"voice\":")) {
                String voiceMsg = config.getTelegramVoiceMessage().replace("%player%", firstName);
                broadcastToMinecraft(formatTGMessage(firstName, voiceMsg, "voice"));
                continue;
            }

            // Handle document/file
            if (update.contains("\"document\":")) {
                String fileMsg = config.getTelegramFileMessage().replace("%player%", firstName);
                broadcastToMinecraft(formatTGMessage(firstName, fileMsg, "file"));
                continue;
            }

            // Handle audio/music
            if (update.contains("\"audio\":")) {
                String audioMsg = config.getTelegramAudioMessage().replace("%player%", firstName);
                broadcastToMinecraft(formatTGMessage(firstName, audioMsg, "audio"));
                continue;
            }

            // Handle location
            if (update.contains("\"location\":")) {
                String locationMsg = config.getTelegramLocationMessage().replace("%player%", firstName);
                broadcastToMinecraft(formatTGMessage(firstName, locationMsg, "location"));
                continue;
            }

            // Handle contact
            if (update.contains("\"contact\":")) {
                String contactMsg = config.getTelegramContactMessage().replace("%player%", firstName);
                broadcastToMinecraft(formatTGMessage(firstName, contactMsg, "contact"));
                continue;
            }

            // Handle poll
            if (update.contains("\"poll\":")) {
                String pollMsg = config.getTelegramPollMessage().replace("%player%", firstName);
                broadcastToMinecraft(formatTGMessage(firstName, pollMsg, "poll"));
                continue;
            }

            // Handle dice
            if (update.contains("\"dice\":")) {
                String diceMsg = config.getTelegramDiceMessage().replace("%player%", firstName);
                broadcastToMinecraft(formatTGMessage(firstName, diceMsg, "dice"));
                continue;
            }

            // Handle game
            if (update.contains("\"game\":")) {
                String gameMsg = config.getTelegramGameMessage().replace("%player%", firstName);
                broadcastToMinecraft(formatTGMessage(firstName, gameMsg, "game"));
                continue;
            }
        }
    }

    private void sendOnlineListToTelegram() {
        Collection<? extends Player> onlinePlayers = plugin.getServer().getOnlinePlayers();
        String list = onlinePlayers.stream()
                .map(OfflinePlayer::getName)
                .collect(Collectors.joining(", "));
        String message = config.getTabResponse()
                .replace("%count%", String.valueOf(onlinePlayers.size()))
                .replace("%list%", list.isEmpty() ? config.getNoPlayersText() : list);
        sender.sendMessage(message);
    }

    private String extract(String source, String key, String delimiter) {
        if (source.contains(key)) {
            int start = source.indexOf(key) + key.length();
            int end = source.indexOf(delimiter, start);
            if (end > start) return unescapeUnicode(source.substring(start, end));
        }
        return null;
    }

    private String unescapeUnicode(String input) {
        if (input == null) return null;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '\\' && i + 1 < input.length() && input.charAt(i + 1) == 'u') {
                try {
                    sb.append((char) Integer.parseInt(input.substring(i + 2, i + 6), 16));
                    i += 5;
                } catch (Exception e) { sb.append(c); }
            } else sb.append(c);
        }
        return sb.toString();
    }

    private String formatTGMessage(String name, String msg, String type) {
        String format = config.getFormatToMinecraft();
        String colored = String.format(format, msg, name);
        if (config.isColorsEnabled()) colored = ChatColor.translateAlternateColorCodes('&', colored);
        return colored;
    }

    private void broadcastToMinecraft(String message) {
        plugin.getServer().getScheduler().runTask(plugin, () -> Bukkit.broadcastMessage(message));
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        if (config.broadcastChat()) {
            String rank = "";
            String formatted = config.getFormatFromMinecraft()
                    .replace("%rank%", rank)
                    .replace("%player%", e.getPlayer().getName())
                    .replace("%message%", e.getMessage());
            sender.sendMessage(formatted);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        if (config.broadcastDeath() && e.getDeathMessage() != null) {
            sender.sendMessage(e.getDeathMessage());
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (config.broadcastJoin()) {
            sender.sendMessage(e.getPlayer().getName() + " " + commands.getMessage("joined"));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        if (config.broadcastQuit()) {
            sender.sendMessage(e.getPlayer().getName() + " " + commands.getMessage("left"));
        }
    }

    @EventHandler
    public void onAchievement(PlayerAchievementAwardedEvent e) {
        if (config.broadcastAchievements()) {
            String msg = commands.getMessage("got_achievement").replace("%achievement%", e.getAchievement().name());
            sender.sendMessage(e.getPlayer().getName() + " " + msg);
        }
    }

    public void shutdown() {
        polling = false;
        if (pollingThread != null) {
            pollingThread.interrupt();
        }
    }
}