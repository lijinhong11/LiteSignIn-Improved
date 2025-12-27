package studio.trc.bukkit.litesignin.message;

import lombok.Getter;
import lombok.Setter;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import studio.trc.bukkit.litesignin.Main;
import studio.trc.bukkit.litesignin.configuration.ConfigurationType;
import studio.trc.bukkit.litesignin.configuration.RobustConfiguration;
import studio.trc.bukkit.litesignin.message.color.ColorUtils;
import studio.trc.bukkit.litesignin.nms.NMSManager;
import studio.trc.bukkit.litesignin.util.AdventureUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MessageUtil {
    private static final Map<String, String> defaultPlaceholders = new HashMap<>();

    @Getter
    @Setter
    private static boolean enabledPAPI = false;

    public static void loadPlaceholders() {
        defaultPlaceholders.clear();
        defaultPlaceholders.put("{plugin_version}", Main.getInstance().getDescription().getVersion());
        defaultPlaceholders.put("{language}", getLanguage());
        defaultPlaceholders.put("{prefix}", getPrefix());
    }

    public static void sendMessage(CommandSender sender, String message) {
        sendMessage(sender, message, defaultPlaceholders);
    }

    public static void sendMessage(CommandSender sender, String message, Map<String, String> placeholders) {
        sendAdventureMessage(sender, message, placeholders, null);
    }

    public static void sendAdventureMessage(CommandSender sender, String message, Map<String, String> placeholders, Map<String, Object> additionalComponents) {
        if (sender == null) return;
        String sample = replacePlaceholders(sender, message, placeholders);
        if (additionalComponents != null && !additionalComponents.isEmpty()) {
            sendAdventureJSONMessage(sender, MessageEditor.createAdventureJSONMessage(sender, sample, AdventureUtils.toAdventureComponents(additionalComponents)));
        } else {
            sender.sendMessage(sample);
        }
    }

    public static void sendMixedMessage(CommandSender sender, String message, Map<String, String> placeholders, Map<String, JSONComponent> additionalComponents, Map<String, String> additionalPlaceholders) {
        if (sender == null) return;
        String sample = replacePlaceholders(sender, message, placeholders);
        Map<String, Component> components = additionalComponents.entrySet()
                .stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> AdventureUtils.toComponent(entry.getValue().getAdventureComponent(additionalPlaceholders))));
        if (!components.isEmpty()) {
            sendAdventureJSONMessage(sender, MessageEditor.createAdventureJSONMessage(sender, sample, components));
        } else {
            sender.sendMessage(sample);
        }
    }

    public static void sendMessageWithItem(CommandSender sender, String message, Map<String, String> placeholders, ItemStack item) {
        Map<String, Object> json = new HashMap<>();
        json.put("%item%", NMSManager.getAdventureJSONItemStack(item));
        sendAdventureMessage(sender, message, placeholders, json);
    }

    public static void sendMessageWithJSONComponent(CommandSender sender, String message, Map<String, String> placeholders, String componentKey, JSONComponent jsonComponent) {
        Map<String, Object> json = new HashMap<>();
        json.put(componentKey, jsonComponent.getAdventureComponent());
        sendAdventureMessage(sender, message, placeholders, json);
    }

    public static void sendMessage(CommandSender sender, List<String> messages) {
        messages.forEach(rawMessage -> sendMessage(sender, rawMessage));
    }

    public static void sendMessage(CommandSender sender, List<String> messages, Map<String, String> placeholders) {
        messages.forEach(rawMessage -> sendMessage(sender, rawMessage, placeholders));
    }

    public static void sendAdventureMessage(CommandSender sender, List<String> messages, Map<String, String> placeholders, Map<String, Object> jsonComponents) {
        messages.forEach(rawMessage -> sendAdventureMessage(sender, rawMessage, placeholders, jsonComponents));
    }

    public static void sendMessage(CommandSender sender, RobustConfiguration configuration, String configPath) {
        sendMessage(sender, configuration, configPath, defaultPlaceholders);
    }

    public static void sendMessage(CommandSender sender, RobustConfiguration configuration, String configPath, Map<String, String> placeholders) {
        sendAdventureMessage(sender, configuration, configPath, placeholders, null);
    }

    public static void sendAdventureMessage(CommandSender sender, RobustConfiguration configuration, String configPath, Map<String, String> placeholders, Map<String, Object> jsonComponents) {
        List<String> messages = configuration.getStringList(getLanguage() + "." + configPath);
        if (messages.isEmpty() && !ConfigurationType.MESSAGES.getRobustConfig().getString(getLanguage() + "." + configPath).equals("[]")) {
            sendAdventureMessage(sender, configuration.getString(getLanguage() + "." + configPath), placeholders, jsonComponents);
        } else {
            sendAdventureMessage(sender, messages, placeholders, jsonComponents);
        }
    }

    public static void sendAdventureJSONMessage(CommandSender sender, Component component) {
        sender.sendMessage(component);
    }

    public static void sendConsoleMessage(String configPath, ConfigurationType type) {
        sendMessage(Bukkit.getConsoleSender(), type.getRobustConfig(), configPath, defaultPlaceholders);
    }

    public static void sendConsoleMessage(String configPath, ConfigurationType type, Map<String, String> placeholders) {
        sendMessage(Bukkit.getConsoleSender(), type.getRobustConfig(), configPath, placeholders);
    }

    public static void sendConsoleAdventureMessage(String configPath, ConfigurationType type, Map<String, String> placeholders, Map<String, Object> jsonComponents) {
        sendAdventureMessage(Bukkit.getConsoleSender(), type.getRobustConfig(), configPath, placeholders, jsonComponents);
    }

    public static void sendCommandMessage(CommandSender sender, String configPath) {
        sendMessage(sender, ConfigurationType.MESSAGES.getRobustConfig(), "Command-Messages." + configPath, defaultPlaceholders);
    }

    public static void sendCommandMessage(CommandSender sender, String configPath, Map<String, String> placeholders) {
        sendMessage(sender, ConfigurationType.MESSAGES.getRobustConfig(), "Command-Messages." + configPath, placeholders);
    }

    public static void sendCommandAdventureMessage(CommandSender sender, String configPath, Map<String, String> placeholders, Map<String, Object> jsonComponents) {
        sendAdventureMessage(sender, ConfigurationType.MESSAGES.getRobustConfig(), "Command-Messages." + configPath, placeholders, jsonComponents);
    }

    public static void sendCommandMessageWithItem(CommandSender sender, String configPath, Map<String, String> placeholders, ItemStack item) {
        Map<String, Object> json = new HashMap<>();
        json.put("%item%", NMSManager.getAdventureJSONItemStack(item));
        sendCommandAdventureMessage(sender, configPath, placeholders, json);
    }

    public static void sendCommandMessageWithJSONComponent(CommandSender sender, String configPath, Map<String, String> placeholders, String componentKey, JSONComponent jsonComponent) {
        Map<String, Object> json = new HashMap<>();
        json.put(componentKey, jsonComponent.getAdventureComponent());
        sendCommandAdventureMessage(sender, configPath, placeholders, json);
    }

    public static String replacePlaceholders(String message, String key, String value) {
        Map<String, String> map = new HashMap<>();
        map.put(key, value);
        return replacePlaceholders(message, map, true);
    }

    public static String replacePlaceholders(String message, Map<String, String> placeholders) {
        return replacePlaceholders(message, placeholders, true);
    }

    public static String replacePlaceholders(String message, Map<String, String> placeholders, boolean toColor) {
        if (message == null || placeholders.isEmpty()) return ColorUtils.toColor(message);
        StringBuilder builder = new StringBuilder();
        try {
            //Execute replacements
            List<MessageSection> sections = MessageEditor.parse(message, placeholders);
            sections.forEach(section -> {
                if (section.isPlaceholder()) {
                    builder.append(placeholders.getOrDefault(section.getPlaceholder(), placeholders.entrySet().stream().collect(Collectors.toMap(key -> key.getKey().toLowerCase(), Map.Entry::getValue)).get(section.getPlaceholder().toLowerCase())));
                } else {
                    builder.append(section.getText().replace("/n", "\n"));
                }
            });

            //Update result
            message = builder.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return toColor ? ColorUtils.toColor(message) : message;
    }

    public static String replacePlaceholders(CommandSender sender, String message, Map<String, String> placeholders) {
        if (message == null || placeholders.isEmpty()) return ColorUtils.toColor(message);
        StringBuilder builder = new StringBuilder();
        try {
            //Execute replacements
            List<MessageSection> sections = MessageEditor.parse(message, placeholders);
            sections.forEach(section -> {
                if (section.isPlaceholder()) {
                    builder.append(placeholders.getOrDefault(section.getPlaceholder(), placeholders.entrySet().stream().collect(Collectors.toMap(key -> key.getKey().toLowerCase(), Map.Entry::getValue)).get(section.getPlaceholder().toLowerCase())));
                } else {
                    builder.append(section.getText().replace("/n", "\n"));
                }
            });

            //Update result
            message = builder.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ColorUtils.toColor(toPlaceholderAPIResult(sender, message));
    }

    public static String escape(String text) {
        return text.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)").replace("[", "\\[").replace("]", "\\]").replace("{", "\\{").replace("}", "\\}").replace("+", "\\+").replace("*", "\\*").replace("|", "\\|").replace("?", "\\?").replace("$", "\\$").replace("^", "\\^");
    }

    public static String toPlaceholderAPIResult(CommandSender sender, String text) {
        return text != null && isEnabledPAPI() && sender instanceof Player ? PlaceholderAPI.setPlaceholders((Player) sender, text) : text;
    }

    public static String getMessage(String configPath) {
        return ConfigurationType.MESSAGES.getRobustConfig().getString(getLanguage() + "." + configPath);
    }

    public static String getMessage(ConfigurationType configType, String configPath) {
        return configType.getRobustConfig().getString(getLanguage() + "." + configPath);
    }

    public static String getMessage(YamlConfiguration config, String configPath) {
        return config.getString(getLanguage() + "." + configPath);
    }

    public static List<String> getMessageList(String path) {
        return getMessageList(ConfigurationType.MESSAGES, getLanguage() + "." + path);
    }

    public static List<String> getMessageList(ConfigurationType configType, String configPath) {
        List<String> messages = configType.getRobustConfig().getStringList(configPath);
        if (messages.isEmpty() && !configType.getRobustConfig().getString(configPath).equals("[]")) {
            messages.add(configType.getRobustConfig().getString(configPath));
        }
        return messages;
    }

    public static List<String> getMessageList(YamlConfiguration config, String configPath) {
        List<String> messages = config.getStringList(configPath);
        if (config.contains(configPath)) {
            if (messages.isEmpty() && !"[]".equals(config.getString(configPath))) {
                messages.add(config.getString(configPath));
            }
        }
        return messages;
    }

    public static String getLanguage() {
        return ConfigurationType.CONFIG.getRobustConfig().getString("Language");
    }

    public static String getItemDisplayLanguagePath() {
        return ConfigurationType.CONFIG.getRobustConfig().getString("Item-Display-Language-Path");
    }

    public static String doBasicProcessing(String text) {
        return replacePlaceholders(text, defaultPlaceholders);
    }

    public static String getPrefix() {
        return ConfigurationType.CONFIG.getRobustConfig().getString("Prefix");
    }

    public static String getLanguageName() {
        return ConfigurationType.MESSAGES.getRobustConfig().getString(getLanguage() + ".Language-Name");
    }

    public static String[] splitStringBySymbol(String text, char symbol) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean escape = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (escape) {
                current.append(c);
                escape = false;
            } else if (c == '\\') {
                escape = true;
            } else if (c == symbol) {
                result.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        if (!current.isEmpty() || escape) {
            result.add(current.toString());
        }
        return result.toArray(new String[0]);
    }

    public static Map<String, String> getDefaultPlaceholders() {
        return new HashMap<>(defaultPlaceholders);
    }

    public static boolean useAdventure() {
        return true;
    }

    /**
     * Plugin langauge
     */
    public enum Language {

        /**
         * Simplified Chinese
         */
        SIMPLIFIED_CHINESE("Simplified-Chinese"),

        /**
         * Traditional Chinese
         */
        TRADITIONAL_CHINESE("Traditional-Chinese"),

        /**
         * Japanese
         */
        JAPANESE("Japanese"),

        /**
         * English
         */
        ENGLISH("English");

        private final String folderName;

        Language(String folderName) {
            this.folderName = folderName;
        }

        public static Language getLocaleLanguage() {
            String language = System.getProperty("user.language");
            String country = System.getProperty("user.country");
            if (language.equalsIgnoreCase("zh")) {
                if (country != null && country.equalsIgnoreCase("cn")) {
                    return SIMPLIFIED_CHINESE;
                } else {
                    return TRADITIONAL_CHINESE;
                }
            } else if (language.equalsIgnoreCase("ja")) {
                return JAPANESE;
            } else {
                return ENGLISH;
            }
        }

        public String getFolderName() {
            return folderName;
        }
    }
}
