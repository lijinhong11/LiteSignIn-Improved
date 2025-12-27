package studio.trc.bukkit.litesignin.util.woodsignscript;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import studio.trc.bukkit.litesignin.configuration.ConfigurationType;
import studio.trc.bukkit.litesignin.configuration.ConfigurationUtil;
import studio.trc.bukkit.litesignin.configuration.RobustConfiguration;
import studio.trc.bukkit.litesignin.message.MessageUtil;
import studio.trc.bukkit.litesignin.message.color.ColorUtils;
import studio.trc.bukkit.litesignin.reward.command.SignInRewardCommand;
import studio.trc.bukkit.litesignin.reward.command.SignInRewardCommandType;
import studio.trc.bukkit.litesignin.util.LiteSignInProperties;
import studio.trc.bukkit.litesignin.util.PluginControl;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WoodSignUtil {
    private static final List<WoodSign> scripts = new ArrayList<>();
    private static final Map<Location, WoodSign> scriptedSigns = new HashMap<>();
    private static final FileConfiguration database = new YamlConfiguration();

    public static void loadSigns() {
        scriptedSigns.clear();
        File file = new File("plugins/LiteSignIn/WoodSignsData.yml");
        if (!file.exists()) try {
            file.createNewFile();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            database.load(reader);
        } catch (IOException | InvalidConfigurationException ex) {
            ex.printStackTrace();
        }
        if (database.get("Database") != null) {
            for (String sections : database.getConfigurationSection("Database").getKeys(false)) {
                try {
                    World world = Bukkit.getWorld(database.getString("Database." + sections + ".Location.World"));
                    if (world == null) {
                        database.set("Database." + sections, null);
                        saveScriptedSigns();
                        continue;
                    }
                    Location location = new Location(
                            world,
                            database.getDouble("Database." + sections + ".Location.X"),
                            database.getDouble("Database." + sections + ".Location.Y"),
                            database.getDouble("Database." + sections + ".Location.Z"));
                    WoodSign woodSign = getWoodSign(database.getString("Database." + sections + ".Script"));
                    scriptedSigns.put(location, woodSign);
                } catch (Exception ex) {
                    database.set("Database." + sections, null);
                    saveScriptedSigns();
                }
            }
        }
    }

    public static void loadScripts() {
        scripts.clear();
        ConfigurationUtil.reloadConfig(ConfigurationType.WOOD_SIGN_SETTINGS);
        RobustConfiguration config = ConfigurationUtil.getConfig(ConfigurationType.WOOD_SIGN_SETTINGS);
        config.getConfigurationSection("Wood-Sign-Scripts").getKeys(false).forEach(woodSignTitle -> {
            try {
                WoodSignLine woodSignText = WoodSignLine.create()
                        .setLine1(config.getString("Wood-Sign-Scripts." + woodSignTitle + ".Sign-Text.Line-1"))
                        .setLine2(config.getString("Wood-Sign-Scripts." + woodSignTitle + ".Sign-Text.Line-2"))
                        .setLine3(config.getString("Wood-Sign-Scripts." + woodSignTitle + ".Sign-Text.Line-3"))
                        .setLine4(config.getString("Wood-Sign-Scripts." + woodSignTitle + ".Sign-Text.Line-4"));
                List<String> woodSignCommand = config.getStringList("Wood-Sign-Scripts." + woodSignTitle + ".Commands");
                String permission = null;
                if (!config.getBoolean("Wood-Sign-Scripts." + woodSignTitle + ".Permission.Default")) {
                    permission = config.getString("Wood-Sign-Scripts." + woodSignTitle + ".Permission.Permission");
                }
                scripts.add(new WoodSign(woodSignTitle, woodSignText, woodSignCommand, permission));
            } catch (Exception ex) {
                Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                placeholders.put("{signs}", woodSignTitle);
                LiteSignInProperties.sendOperationMessage("WoodSignScriptLoadFailed", placeholders);
            }
        });
    }

    public static WoodSign getWoodSign(String titleText) {
        for (WoodSign woodSign : scripts) {
            if (woodSign.woodSignTitle().equalsIgnoreCase(titleText)) {
                return woodSign;
            }
        }
        return null;
    }

    public static List<WoodSign> getWoodSignScripts() {
        return scripts;
    }

    public static Map<Location, WoodSign> getAllScriptedSign() {
        return scriptedSigns;
    }

    public static void createWoodSignScript(Block block, WoodSign woodSign, boolean reloadFile) {
        int number = 1;
        while (database.get("Database." + number) != null) {
            number++;
        }
        Location location = block.getLocation();
        database.set("Database." + number + ".Location.World", location.getWorld().getName());
        database.set("Database." + number + ".Location.X", location.getBlockX());
        database.set("Database." + number + ".Location.Y", location.getBlockY());
        database.set("Database." + number + ".Location.Z", location.getBlockZ());
        database.set("Database." + number + ".Script", woodSign.woodSignTitle());
        saveScriptedSigns();
        if (reloadFile) loadSigns();
        PluginControl.runBukkitTask(() -> {
            Sign sign = (Sign) block.getState();
            sign.setLine(0, ColorUtils.toColor(woodSign.woodSignText().getLine1()));
            sign.setLine(1, ColorUtils.toColor(woodSign.woodSignText().getLine2()));
            sign.setLine(2, ColorUtils.toColor(woodSign.woodSignText().getLine3()));
            sign.setLine(3, ColorUtils.toColor(woodSign.woodSignText().getLine4()));
            sign.update();
        }, 1);
    }

    public static boolean removeWoodSignScript(Location location) {
        if (database.get("Database") == null || location.getWorld() == null) return false;
        for (String sections : database.getConfigurationSection("Database").getKeys(false)) {
            if (location.getWorld().getName().equalsIgnoreCase(database.getString("Database." + sections + ".Location.World"))
                    && location.getBlockX() == database.getInt("Database." + sections + ".Location.X")
                    && location.getBlockY() == database.getInt("Database." + sections + ".Location.Y")
                    && location.getBlockZ() == database.getInt("Database." + sections + ".Location.Z")) {
                database.set("Database." + sections, null);
                saveScriptedSigns();
                loadSigns();
                return true;
            }
        }
        return false;
    }

    public static void saveScriptedSigns() {
        File file = new File("plugins/LiteSignIn/WoodSignsData.yml");
        if (!file.exists()) try {
            file.createNewFile();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        try {
            database.save(file);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void scan() {
        int number = 0;
        number = new ArrayList<>(scriptedSigns.keySet()).stream().filter(location -> {
            location.getBlock();
            return !(location.getBlock().getState() instanceof Sign);
        }).filter(WoodSignUtil::removeWoodSignScript).map(m -> 1).reduce(number, Integer::sum);
        if (number > 0) {
            Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
            placeholders.put("{signs}", String.valueOf(number));
            LiteSignInProperties.sendOperationMessage("WoodSignScriptCleared", placeholders);
        }
    }

    public static void clickScript(Player player, WoodSign scriptedSign) {
        List<SignInRewardCommand> list = new ArrayList<>();
        scriptedSign.woodSignCommand().forEach(command -> {
            if (command.toLowerCase().startsWith("server:")) {
                list.add(new SignInRewardCommand(SignInRewardCommandType.SERVER, command.substring(7)));
            } else if (command.toLowerCase().startsWith("op:")) {
                list.add(new SignInRewardCommand(SignInRewardCommandType.OP, command.substring(3)));
            } else {
                list.add(new SignInRewardCommand(SignInRewardCommandType.PLAYER, command));
            }
        });
        list.forEach(command -> {
            command.runWithThePlayer(player);
        });
    }

    public static class WoodSignLine {
        private String line_1 = "";
        private String line_2 = "";
        private String line_3 = "";
        private String line_4 = "";

        public static WoodSignLine create() {
            return new WoodSignLine();
        }

        public String getLine1() {
            return line_1;
        }

        public WoodSignLine setLine1(String text) {
            line_1 = text;
            return this;
        }

        public String getLine2() {
            return line_2;
        }

        public WoodSignLine setLine2(String text) {
            line_2 = text;
            return this;
        }

        public String getLine3() {
            return line_3;
        }

        public WoodSignLine setLine3(String text) {
            line_3 = text;
            return this;
        }

        public String getLine4() {
            return line_4;
        }

        public WoodSignLine setLine4(String text) {
            line_4 = text;
            return this;
        }
    }
}
