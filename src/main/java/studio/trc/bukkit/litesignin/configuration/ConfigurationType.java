package studio.trc.bukkit.litesignin.configuration;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import studio.trc.bukkit.litesignin.Main;
import studio.trc.bukkit.litesignin.message.MessageUtil;
import studio.trc.bukkit.litesignin.util.LiteSignInProperties;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

@Deprecated(forRemoval = true)
/*
  为什么一个enum要干这么多没用的事
  而且配置还tm分两个版本
 */
public enum ConfigurationType {
    /**
     * Config.yml
     */
    CONFIG("Config.yml", false),

    /**
     * Messages.yml
     */
    MESSAGES("Messages.yml", true),

    /**
     * GUISettings.yml
     */
    GUI_SETTINGS("GUISettings.yml", true, ConfigurationVersion.GUI_SETTINGS_V1),

    /**
     * RewardSettings.yml
     */
    REWARD_SETTINGS("RewardSettings.yml", false, ConfigurationVersion.REWARD_SETTINGS_V1),

    /**
     * CustomItems.yml
     */
    CUSTOM_ITEMS("CustomItems.yml", false),

    /**
     * WoodSignSettings.yml
     */
    WOOD_SIGN_SETTINGS("WoodSignSettings.yml", false);

    @Getter
    private final boolean universal;
    @Getter
    private final String fileName;
    @Getter
    private final YamlConfiguration config;
    @Getter
    private final ConfigurationVersion[] versions;

    ConfigurationType(String fileName, boolean universal, ConfigurationVersion... versions) {
        this.fileName = fileName;
        this.universal = universal;
        this.config = new YamlConfiguration();
        this.versions = versions;
    }

    public static void saveLanguageConfig(ConfigurationType type) {
        String language = MessageUtil.getLanguage();
        if (!type.getRobustConfig().getConfig().contains(language)) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(Main.class.getResourceAsStream("/Languages/Universal/" + type.getLocalFilePath()), StandardCharsets.UTF_8))) {
                String line;
                StringBuilder source = new StringBuilder();
                try (BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream("plugins/LiteSignIn/" + type.getFileName()), StandardCharsets.UTF_8))) {
                    while ((line = input.readLine()) != null) {
                        source.append(line);
                        source.append('\n');
                    }
                }
                try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("plugins/LiteSignIn/" + type.getFileName()), StandardCharsets.UTF_8))) {
                    writer.append(source.toString());
                    boolean keepWriting = false;
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith(language + ":")) {
                            keepWriting = true;
                        }
                        if (!line.startsWith("    ") && !line.startsWith(language)) {
                            keepWriting = false;
                        }
                        if (keepWriting) {
                            writer.append(line);
                            writer.append('\n');
                        }
                    }
                }
                try (Reader reloader = new InputStreamReader(new FileInputStream("plugins/LiteSignIn/" + type.getFileName()), StandardCharsets.UTF_8)) {
                    type.config.load(reloader);
                } catch (IOException | InvalidConfigurationException ex) {
                    ex.printStackTrace();
                    Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                    placeholders.put("{file}", type.getFileName());
                    LiteSignInProperties.sendOperationMessage("ConfigurationLoadingError", placeholders);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                placeholders.put("{file}", type.getFileName());
                LiteSignInProperties.sendOperationMessage("ConfigurationLoadingError", placeholders);
            }
        }
    }

    public void saveResource() {
        File dataFolder = new File("plugins/LiteSignIn/");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        try {
            File configFile = new File(dataFolder, fileName);
            if (!configFile.exists()) {
                configFile.createNewFile();
                if (!universal) {
                    InputStream is = Main.class.getResourceAsStream("/Languages/" + MessageUtil.Language.getLocaleLanguage().getFolderName() + "/" + getLocalFilePath());
                    byte[] bytes = new byte[is.available()];
                    for (int len = 0; len != bytes.length; len += is.read(bytes, len, bytes.length - len)) ;
                    try (OutputStream out = new FileOutputStream(configFile)) {
                        out.write(bytes);
                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void saveConfig() {
        try {
            config.save("plugins/LiteSignIn/" + fileName);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void reloadConfig() {
        try (InputStreamReader configFile = new InputStreamReader(new FileInputStream("plugins/LiteSignIn/" + fileName), StandardCharsets.UTF_8)) {
            config.load(configFile);
            if (universal) {
                saveLanguageConfig(this);
            }
        } catch (IOException | InvalidConfigurationException ex) {
            File oldFile = new File("plugins/LiteSignIn/" + fileName + ".old");
            File file = new File("plugins/LiteSignIn/" + fileName);
            Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
            placeholders.put("{file}", fileName);
            LiteSignInProperties.sendOperationMessage("ConfigurationLoadingError", placeholders);
            if (oldFile.exists()) {
                oldFile.delete();
            }
            file.renameTo(oldFile);
            saveResource();
            try (InputStreamReader newConfig = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
                config.load(newConfig);
                LiteSignInProperties.sendOperationMessage("ConfigurationRepair", MessageUtil.getDefaultPlaceholders());
            } catch (IOException | InvalidConfigurationException ex1) {
                ex1.printStackTrace();
            }
        }
    }

    public String getLocalFilePath() {
        if (versions.length == 0) {
            return fileName;
        } else {
            try {
                String nms = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
                ConfigurationVersion specialVersion = Arrays.stream(versions).filter(version -> version.getVersions().length == 0 || Arrays.stream(version.getVersions()).anyMatch(type -> nms.equalsIgnoreCase(type.name()))).findFirst().get();
                return specialVersion.getFolder() + specialVersion.getFileName();
            } catch (Exception ex) {
                ConfigurationVersion specialVersion = Arrays.stream(versions).filter(version -> version.getVersions().length == 0).findFirst().orElseThrow();
                return specialVersion.getFolder() + specialVersion.getFileName();
            }
        }
    }

    public RobustConfiguration getRobustConfig() {
        return ConfigurationUtil.getConfig(this);
    }
}
