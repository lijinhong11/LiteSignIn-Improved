package studio.trc.bukkit.litesignin.util;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import studio.trc.bukkit.litesignin.message.MessageUtil;
import studio.trc.bukkit.litesignin.message.color.ColorUtils;

import java.util.Map;
import java.util.Properties;

public class LiteSignInProperties {
    /**
     * System Language
     */
    public static Properties propertiesFile = new Properties();

    public static void sendOperationMessage(String path) {
        CommandSender sender = Bukkit.getConsoleSender();
        if (propertiesFile.containsKey(path)) {
            sender.sendMessage(ColorUtils.toColor(propertiesFile.getProperty(path)));
        }
    }

    public static void sendOperationMessage(String path, Map<String, String> placeholders) {
        CommandSender sender = Bukkit.getConsoleSender();
        if (propertiesFile.containsKey(path)) {
            String message = propertiesFile.getProperty(path);
            MessageUtil.sendMessage(sender, message, placeholders);
        }
    }

    public static String getMessage(String configPath) {
        return propertiesFile.getProperty(configPath);
    }

    public static String getMessage(String configPath, Map<String, String> placeholders) {
        return MessageUtil.replacePlaceholders(propertiesFile.getProperty(configPath), placeholders);
    }
}
