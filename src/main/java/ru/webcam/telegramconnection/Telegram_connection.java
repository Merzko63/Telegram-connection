package ru.webcam.telegramconnection;

import org.bukkit.plugin.java.JavaPlugin;
import ru.webcam.telegramconnection.modules.ConfigManager;
import ru.webcam.telegramconnection.modules.TelegramCommands;
import ru.webcam.telegramconnection.modules.TelegramListener;
import ru.webcam.telegramconnection.modules.TelegramSender;

public final class Telegram_connection extends JavaPlugin {
    private ConfigManager config;
    private TelegramSender sender;
    private TelegramListener listener;
    private TelegramCommands commands;

    @Override
    public void onEnable() {
        config = new ConfigManager(this);
        sender = new TelegramSender(this, config);
        listener = new TelegramListener(this, config, sender);
        commands = new TelegramCommands(this, config, sender, listener);

        getServer().getPluginManager().registerEvents(listener, this);

        if (getCommand("telegram") != null) getCommand("telegram").setExecutor(commands);
        if (getCommand("tg") != null) getCommand("tg").setExecutor(commands);
        if (getCommand("tgreload") != null) getCommand("tgreload").setExecutor(commands);

        if (config.broadcastServerStart()) {
            sender.sendMessage(commands.getMessage("server_started"));
        }

        getLogger().info("Telegram-connection enabled");
    }

    @Override
    public void onDisable() {
        if (listener != null) listener.shutdown();
        getLogger().info("Telegram-connection disabled");
    }
}