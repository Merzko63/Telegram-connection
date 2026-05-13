package ru.merzko63.telegramconnection.modules;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class TelegramCommands implements CommandExecutor {
    private final JavaPlugin plugin;
    private final ConfigManager config;
    private final TelegramSender sender;
    private final TelegramListener listener;
    private String currentLang;

    private final Map<String, Map<String, String>> messages = new HashMap<>();

    public TelegramCommands(JavaPlugin plugin, ConfigManager config, TelegramSender sender, TelegramListener listener) {
        this.plugin = plugin;
        this.config = config;
        this.sender = sender;
        this.listener = listener;
        loadMessages();
    }

    private void loadMessages() {
        Map<String, String> ru = new HashMap<>();
        ru.put("testing", "§e[Telegram] Проверка соединения...");
        ru.put("test_success_token", "§a✓ Токен бота действителен");
        ru.put("test_fail_token", "§c✗ Неверный токен бота (код: %code%)");
        ru.put("test_success_sent", "§a✓ Тестовое сообщение отправлено в чат %chat%");
        ru.put("test_check_telegram", "§7  Проверьте Telegram для подтверждения");
        ru.put("test_fail", "§c✗ Ошибка соединения: %error%");
        ru.put("reload_start", "§e[Telegram] Перезагрузка конфига...");
        ru.put("reload_success", "§a✓ Конфиг успешно перезагружен");
        ru.put("reload_fail", "§c✗ Ошибка перезагрузки: %error%");
        ru.put("help_header", "§6=== Команды Telegram плагина ===");
        ru.put("help_test", "§e/tg test §7- Проверить соединение с Telegram");
        ru.put("help_reload", "§e/tg reload §7- Перезагрузить конфиг");
        ru.put("help_reload_alt", "§e/tgreload §7- То же что и /tg reload");
        ru.put("help_help", "§e/tg help §7- Показать эту справку");
        ru.put("help_tg_commands", "§7Команды в Telegram:");
        ru.put("help_tg_tab", "§7  /tab §7- Показать список игроков онлайн");
        ru.put("help_tg_message", "§7  Любое сообщение - Отправить в чат Minecraft");
        ru.put("token_invalid", "invalid");
        ru.put("joined", "присоединился к игре");
        ru.put("left", "покинул игру");
        ru.put("got_achievement", "получил достижение: %achievement%");
        ru.put("server_started", "Сервер запущен");

        Map<String, String> en = new HashMap<>();
        en.put("testing", "§e[Telegram] Testing connection...");
        en.put("test_success_token", "§a✓ Bot token is valid");
        en.put("test_fail_token", "§c✗ Invalid bot token (code: %code%)");
        en.put("test_success_sent", "§a✓ Test message sent to chat ID: %chat%");
        en.put("test_check_telegram", "§7  Check Telegram to verify");
        en.put("test_fail", "§c✗ Connection failed: %error%");
        en.put("reload_start", "§e[Telegram] Reloading config...");
        en.put("reload_success", "§a✓ Config reloaded successfully");
        en.put("reload_fail", "§c✗ Reload failed: %error%");
        en.put("help_header", "§6=== Telegram Plugin Commands ===");
        en.put("help_test", "§e/tg test §7- Test connection to Telegram");
        en.put("help_reload", "§e/tg reload §7- Reload config");
        en.put("help_reload_alt", "§e/tgreload §7- Same as /tg reload");
        en.put("help_help", "§e/tg help §7- Show this help");
        en.put("help_tg_commands", "§7Telegram commands:");
        en.put("help_tg_tab", "§7  /tab §7- Show online players list");
        en.put("help_tg_message", "§7  Any message - Forward to Minecraft chat");
        en.put("token_invalid", "invalid");
        en.put("joined", "joined the game");
        en.put("left", "left the game");
        en.put("got_achievement", "got achievement: %achievement%");
        en.put("server_started", "Server started");

        messages.put("ru", ru);
        messages.put("en", en);
        updateLanguage();
    }

    public void updateLanguage() {
        currentLang = config.getLanguage();
        if (!messages.containsKey(currentLang)) {
            currentLang = "en";
        }
    }

    public String getMessage(String key) {
        updateLanguage();
        Map<String, String> langMap = messages.get(currentLang);
        if (langMap != null && langMap.containsKey(key)) {
            return langMap.get(key);
        }
        Map<String, String> enMap = messages.get("en");
        if (enMap != null && enMap.containsKey(key)) {
            return enMap.get(key);
        }
        return key;
    }

    public String getMessage(String key, Map<String, String> replacements) {
        String msg = getMessage(key);
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            msg = msg.replace("%" + entry.getKey() + "%", entry.getValue());
        }
        return msg;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("tgreload")) {
            return handleReload(sender);
        }

        if (cmd.getName().equalsIgnoreCase("telegram") || cmd.getName().equalsIgnoreCase("tg")) {
            if (args.length == 0) {
                sendHelp(sender);
                return true;
            }

            switch (args[0].toLowerCase()) {
                case "test":
                case "check":
                    return handleTest(sender);
                case "reload":
                    return handleReload(sender);
                case "help":
                    sendHelp(sender);
                    return true;
                default:
                    sendHelp(sender);
                    return true;
            }
        }

        return false;
    }

    private boolean handleTest(CommandSender sender) {
        sendMessageToSender(sender, getMessage("testing"));

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String api = config.getApiUrl();
                String token = config.getBotToken();
                String chatId = config.getChatId();

                URL getMeUrl = new URL(api + token + "/getMe");
                HttpURLConnection conn1 = (HttpURLConnection) getMeUrl.openConnection();
                conn1.setConnectTimeout(5000);
                conn1.setRequestMethod("GET");
                int code1 = conn1.getResponseCode();
                conn1.disconnect();

                if (code1 == 200) {
                    sendMessageToSender(sender, getMessage("test_success_token"));
                } else {
                    Map<String, String> repl = new HashMap<>();
                    repl.put("code", String.valueOf(code1));
                    sendMessageToSender(sender, getMessage("test_fail_token", repl));
                    return;
                }

                String testMsg = "✅ Connection test from Minecraft server at " + java.time.LocalDateTime.now();
                this.sender.sendMessage(testMsg);
                Map<String, String> repl = new HashMap<>();
                repl.put("chat", chatId);
                sendMessageToSender(sender, getMessage("test_success_sent", repl));
                sendMessageToSender(sender, getMessage("test_check_telegram"));

            } catch (Exception e) {
                Map<String, String> repl = new HashMap<>();
                repl.put("error", e.getMessage());
                sendMessageToSender(sender, getMessage("test_fail", repl));
                plugin.getLogger().warning("[TG] Test error: " + e.getMessage());
            }
        });

        return true;
    }

    private boolean handleReload(CommandSender sender) {
        sendMessageToSender(sender, getMessage("reload_start"));

        try {
            config.reload();
            listener.restartPolling();
            sendMessageToSender(sender, getMessage("reload_success"));
            sendMessageToSender(sender, "§7  Bot token: " + maskToken(config.getBotToken()));
            sendMessageToSender(sender, "§7  Chat ID: " + config.getChatId());
            sendMessageToSender(sender, "§7  API URL: " + config.getApiUrl());
        } catch (Exception e) {
            Map<String, String> repl = new HashMap<>();
            repl.put("error", e.getMessage());
            sendMessageToSender(sender, getMessage("reload_fail", repl));
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(getMessage("help_header"));
        sender.sendMessage(getMessage("help_test"));
        sender.sendMessage(getMessage("help_reload"));
        sender.sendMessage(getMessage("help_reload_alt"));
        sender.sendMessage(getMessage("help_help"));
        sender.sendMessage("§7");
        sender.sendMessage(getMessage("help_tg_commands"));
        sender.sendMessage(getMessage("help_tg_tab"));
        sender.sendMessage(getMessage("help_tg_message"));
    }

    private void sendMessageToSender(CommandSender sender, String message) {
        if (sender instanceof Player) {
            sender.sendMessage(message);
        } else {
            plugin.getLogger().info(ChatColor.stripColor(message));
        }
    }

    private String maskToken(String token) {
        if (token == null || token.length() < 8) return getMessage("token_invalid");
        return token.substring(0, 5) + "..." + token.substring(token.length() - 4);
    }
}