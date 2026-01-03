package studio.trc.bukkit.litesignin.util;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import studio.trc.bukkit.litesignin.Main;
import studio.trc.bukkit.litesignin.configuration.ConfigurationType;
import studio.trc.bukkit.litesignin.configuration.ConfigurationUtil;
import studio.trc.bukkit.litesignin.message.MessageUtil;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Deprecated
public class Updater {
    /**
     * -- GETTER --
     *  Return whether found a new version.
     *
     * @return
     */
    @Getter
    private static boolean foundANewVersion = false;
    /**
     * -- GETTER --
     *  Get new version.
     *
     * @return
     */
    @Getter
    private static String newVersion;
    /**
     * -- GETTER --
     *  Get download link.
     *
     * @return
     */
    @Getter
    private static String link;
    /**
     * -- GETTER --
     *  Get new version's update description.
     *
     * @return
     */
    @Getter
    private static String description;
    /**
     * -- GETTER --
     *  Get extra messages.
     *
     * @return
     */
    @Getter
    private static List<String> extraMessages;
    private static Date date = new Date();
    private static final Runnable checkUpdate = () -> {
        try {
            URL url = new URL("https://api.trc.studio/resources/spigot/litesignin/update.yml");
            try (Reader reader = new InputStreamReader(url.openStream(), StandardCharsets.UTF_8)) {
                YamlConfiguration yaml = new YamlConfiguration();
                yaml.load(reader);
                String version = yaml.getString("latest-version");
                String versionBelongingTo = yaml.getString("Version-Belonging-to");
                String downloadLink = yaml.getString("link");
                String description_ = "description.Default";
                List<String> extra = yaml.getStringList("Extra.Default");
                if (yaml.get("description." + MessageUtil.getLanguage()) != null) {
                    description_ = yaml.getString("description." + MessageUtil.getLanguage());
                    extra = yaml.getStringList("Extra." + MessageUtil.getLanguage());
                } else {
                    for (String languages : yaml.getConfigurationSection("description").getKeys(false)) {
                        if (MessageUtil.getLanguage().contains(languages)) {
                            description_ = yaml.getString("description." + MessageUtil.getLanguage());
                            break;
                        }
                    }
                }
                String nowVersion = Main.getInstance().getDescription().getVersion();
                if (!nowVersion.startsWith(versionBelongingTo)) {
                    newVersion = version;
                    foundANewVersion = true;
                    link = downloadLink;
                    description = description_;
                    extraMessages = extra;
                    Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                    placeholders.put("{version}", version);
                    placeholders.put("%link%", downloadLink);
                    placeholders.put("{link}", downloadLink);
                    placeholders.put("{nowVersion}", nowVersion);
                    placeholders.put("{description}", description_);
                    MessageUtil.sendMessage(Bukkit.getConsoleSender(), ConfigurationUtil.getConfig(ConfigurationType.MESSAGES), "Updater.Checked", placeholders);
                    if (!extra.isEmpty()) {
                        extra.forEach(message -> MessageUtil.sendMessage(Bukkit.getConsoleSender(), message));
                    }
                }
            } catch (InvalidConfigurationException | IOException ex) {
                MessageUtil.sendMessage(Bukkit.getConsoleSender(), ConfigurationUtil.getConfig(ConfigurationType.MESSAGES), "Updater.Error");
            }
        } catch (MalformedURLException ex) {
            MessageUtil.sendMessage(Bukkit.getConsoleSender(), ConfigurationUtil.getConfig(ConfigurationType.MESSAGES), "Updater.Error");
        }
        date = new Date();
    };

    /**
     * Start check updater.
     */
    public static void checkUpdate() {
        new Thread(checkUpdate, "LiteSignIn-Updater").start();
    }

    /**
     * Get the time of last check update.
     *
     * @return
     */
    public static Date getTimeOfLastCheckUpdate() {
        return date;
    }
}
