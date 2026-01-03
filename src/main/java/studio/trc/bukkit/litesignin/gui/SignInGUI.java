package studio.trc.bukkit.litesignin.gui;

import io.github.projectunified.uniitem.all.AllItemProvider;
import io.github.projectunified.uniitem.api.ItemKey;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import studio.trc.bukkit.litesignin.api.Storage;
import studio.trc.bukkit.litesignin.configuration.ConfigurationType;
import studio.trc.bukkit.litesignin.configuration.ConfigurationUtil;
import studio.trc.bukkit.litesignin.gui.SignInGUIColumn.KeyType;
import studio.trc.bukkit.litesignin.message.MessageUtil;
import studio.trc.bukkit.litesignin.message.color.ColorUtils;
import studio.trc.bukkit.litesignin.queue.SignInQueue;
import studio.trc.bukkit.litesignin.util.LiteSignInProperties;
import studio.trc.bukkit.litesignin.util.PluginControl;
import studio.trc.bukkit.litesignin.util.SignInDate;
import studio.trc.bukkit.litesignin.util.SkullManager;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * GUI builder for sign-in calendar interface.
 * 
 * @author TRCStudioDean
 */
@SuppressWarnings("deprecation")
public class SignInGUI {
    private static final int GUI_SIZE = 54;
    private static final int[] DAYS_IN_MONTH = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };

    public static SignInInventory getGUI(Player player) {
        ConfigurationSection section = ConfigurationUtil.getConfig(ConfigurationType.GUI_SETTINGS)
                .getConfigurationSection(MessageUtil.getLanguage() + ".SignIn-GUI-Settings");
        /*
         * Create chest GUI
         */
        Inventory gui = Bukkit.createInventory(null, GUI_SIZE,
                ColorUtils.toColor(replace(player, section.getString("GUI-Name"), "{date}",
                        new SimpleDateFormat(section.getString("Date-Format", "")).format(new Date()))));

        /*
         * Elements
         */
        List<SignInGUIColumn> columns = new ArrayList<>();

        getKey(player).stream().peek(items -> gui.setItem(items.getKeyPostion(), items.getItemStack()))
                .forEach(columns::add);

        getOthers(player).stream().peek(items -> gui.setItem(items.getKeyPostion(), items.getItemStack()))
                .forEach(columns::add);

        return new SignInInventory(gui, columns);
    }

    public static SignInInventory getGUI(Player player, int day, int month) {
        /*
         * Chest GUI
         */
        Inventory gui;

        ConfigurationSection section = ConfigurationUtil.getConfig(ConfigurationType.GUI_SETTINGS)
                .getConfigurationSection(MessageUtil.getLanguage() + ".SignIn-GUI-Settings");

        /*
         * If month = specified month, return basic gui.
         */
        Date now = new Date();
        if (month == SignInDate.getInstance(now).getMonth()) {
            gui = Bukkit.createInventory(null, GUI_SIZE,
                    ColorUtils.toColor(replace(player, section.getString("GUI-Name"), "{date}",
                            new SimpleDateFormat(section.getString("Date-Format", "")).format(now))));
        } else {
            gui = Bukkit.createInventory(null, GUI_SIZE, ColorUtils.toColor(
                    replace(player, section.getString("Specified-Month-GUI-Name"), "{month}", String.valueOf(month))));
        }

        /*
         * Elements
         */
        List<SignInGUIColumn> columns = new ArrayList<>();

        getKey(player, month).stream().peek(items -> gui.setItem(items.getKeyPostion(), items.getItemStack()))
                .forEach(columns::add);

        getOthers(player, day, month).stream().peek(items -> gui.setItem(items.getKeyPostion(), items.getItemStack()))
                .forEach(columns::add);

        return new SignInInventory(gui, columns, month);
    }

    public static SignInInventory getGUI(Player player, int day, int month, int year) {
        /*
         * Chest GUI
         */
        Inventory gui;
        SignInDate today = SignInDate.getInstance(new Date());
        ConfigurationSection section = ConfigurationUtil.getConfig(ConfigurationType.GUI_SETTINGS)
                .getConfigurationSection(MessageUtil.getLanguage() + ".SignIn-GUI-Settings");

        /*
         * If month = specified month and year = specified year, return basic gui.
         */
        if (year == today.getYear()) {
            if (month == today.getMonth()) {
                gui = Bukkit.createInventory(null, GUI_SIZE,
                        ColorUtils.toColor(replace(player, section.getString("GUI-Name"), "{date}",
                                new SimpleDateFormat(section.getString("Date-Format", "")).format(new Date()))));
            } else {
                gui = Bukkit.createInventory(null, GUI_SIZE, ColorUtils.toColor(replace(player,
                        section.getString("Specified-Month-GUI-Name"), "{month}", String.valueOf(month))));
            }
        } else {
            Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
            placeholders.put("{month}", String.valueOf(month));
            placeholders.put("{year}", String.valueOf(year));
            gui = Bukkit.createInventory(null, GUI_SIZE, MessageUtil.replacePlaceholders(player,
                    section.getString("Specified-Year-GUI-Name"), placeholders));
        }

        /*
         * Elements
         */
        List<SignInGUIColumn> columns = new ArrayList<>();

        getKey(player, month, year).stream().peek(items -> gui.setItem(items.getKeyPostion(), items.getItemStack()))
                .forEach(columns::add);

        getOthers(player, day, month, year).stream().peek(items -> gui.setItem(items.getKeyPostion(), items.getItemStack()))
                .forEach(columns::add);

        return new SignInInventory(gui, columns, month, year);
    }

    /**
     * Return key buttons.
     *
     * @param player the player
     * @param month  specified month.
     * @param year   specified year.
     * @return a set of buttons
     */
    public static Set<SignInGUIColumn> getKey(Player player, int month, int year) {
        Set<SignInGUIColumn> set = new LinkedHashSet<>();
        SignInDate today = SignInDate.getInstance(new Date());
        if (today.getMonth() == month && today.getYear() == year)
            return getKey(player);
        Storage playerdata = Storage.getPlayer(player);
        String queue = String.valueOf(SignInQueue.getInstance().getRank(playerdata.getUserUUID()));
        String continuous = String.valueOf(playerdata.getContinuousSignIn());
        String totalNumber = String.valueOf(playerdata.getCumulativeNumber());
        String cards = String.valueOf(playerdata.getRetroactiveCard());
        int nextPageMonth = getNextPageMonth(month);
        int nextPageYear = getNextPageYear(month, year);
        int previousPageMonth = getPreviousPageMonth(month);
        int previousPageYear = getPreviousPageYear(month, year);
        int[] days = getDaysInMonth(year);
        SignInDate specifiedDate = SignInDate.getInstance(year, month, 1);
        ConfigurationSection section = ConfigurationUtil.getConfig(ConfigurationType.GUI_SETTINGS)
                .getConfigurationSection(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key");
        if (specifiedDate.compareTo(today) == -1) {
            List<ItemStack> items = new ArrayList<>();
            List<KeyType> keys = new ArrayList<>();
            for (int i = 0; i < days[month - 1]; i++) {
                SignInDate historicalDate = SignInDate.getInstance(year, month, i + 1);
                Map<String, String> placeholders = getPlaceholdersOfItemLore(i, continuous, queue, totalNumber, cards,
                        nextPageMonth, nextPageYear, previousPageMonth, previousPageYear, historicalDate);

                ItemStack key;
                KeyType keyType;
                if (playerdata.alreadySignIn(historicalDate)) {
                    key = createKeyItemWithPlaceholders(section, "Already-SignIn", historicalDate, player, placeholders);
                    keyType = KeyType.ALREADY_SIGNIN;
                } else {
                    key = createKeyItemWithPlaceholders(section, "Missed-SignIn", historicalDate, player, placeholders);
                    keyType = KeyType.MISSED_SIGNIN;
                }
                items.add(key);
                keys.add(keyType);
            }
            if (section.get("Slots") != null) {
                int i = 0;
                for (String slots : section.getStringList("Slots")) {
                    try {
                        Calendar cal = Calendar.getInstance();
                        cal.set(year, month - 1, i + 1);
                        set.add(new SignInGUIColumn(items.get(i), Integer.parseInt(slots),
                                SignInDate.getInstance(cal.getTime()), keys.get(i)));
                    } catch (IndexOutOfBoundsException ignored) {
                    }
                    i++;
                }
            }
            return set;
        } else {
            List<ItemStack> items = new ArrayList<>();
            List<KeyType> keys = new ArrayList<>();
            for (int i = 0; i < days[month - 1]; i++) {
                Map<String, String> placeholders = getPlaceholdersOfItemLore(i, continuous, queue, totalNumber, cards,
                        nextPageMonth, nextPageYear, previousPageMonth, previousPageYear, null);
                SignInDate date = SignInDate.getInstance(year, month, i + 1);
                ItemStack key = createKeyItemWithPlaceholders(section, "Comming-Soon", date, player, placeholders);
                items.add(key);
                keys.add(KeyType.COMMING_SOON);
            }
            if (section.get("Slots") != null) {
                int i = 0;
                for (String slots : section.getStringList("Slots")) {
                    try {
                        Calendar cal = Calendar.getInstance();
                        cal.set(year, month - 1, i + 1);
                        set.add(new SignInGUIColumn(items.get(i), Integer.parseInt(slots),
                                SignInDate.getInstance(cal.getTime()), keys.get(i)));
                    } catch (IndexOutOfBoundsException ignored) {
                    }
                    i++;
                }
            }
            return set;
        }
    }

    /**
     * Return key buttons.
     *
     * @param player the player
     * @param month  specified month.
     * @return a set of buttons
     */
    public static Set<SignInGUIColumn> getKey(Player player, int month) {
        SignInDate today = SignInDate.getInstance(new Date());
        if (today == null) {
            return new HashSet<>();
        }

        if (month == today.getMonth()) {
            return getKey(player);
        } else {
            return getKey(player, month, today.getYear());
        }
    }

    /**
     * Return key buttons.
     *
     * @param player the player
     * @return a set of buttons
     */
    public static Set<SignInGUIColumn> getKey(Player player) {
        Set<SignInGUIColumn> set = new LinkedHashSet<>();
        Storage playerdata = Storage.getPlayer(player);
        String queue = String.valueOf(SignInQueue.getInstance().getRank(playerdata.getUserUUID()));
        String continuous = String.valueOf(playerdata.getContinuousSignIn());
        String totalNumber = String.valueOf(playerdata.getCumulativeNumber());
        String cards = String.valueOf(playerdata.getRetroactiveCard());
        String[] times = new SimpleDateFormat("yyyy-MM-dd").format(new Date()).split("-");
        ConfigurationSection section = ConfigurationUtil.getConfig(ConfigurationType.GUI_SETTINGS)
                .getConfigurationSection(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key");
        int year = Integer.parseInt(times[0]);
        int month = Integer.parseInt(times[1]);
        int[] days = getDaysInMonth(year);
        int day = Integer.parseInt(times[2]);
        int nextPageMonth = getNextPageMonth(month);
        int nextPageYear = getNextPageYear(month, year);
        int previousPageMonth = getPreviousPageMonth(month);
        int previousPageYear = getPreviousPageYear(month, year);
        List<ItemStack> items = new ArrayList<>();
        List<KeyType> keys = new ArrayList<>();
        for (int i = 0; i < days[month - 1]; i++) {
            SignInDate historicalDate = SignInDate.getInstance(year, month, i + 1);
            Map<String, String> placeholders = getPlaceholdersOfItemLore(i, continuous, queue, totalNumber, cards,
                    nextPageMonth, nextPageYear, previousPageMonth, previousPageYear, historicalDate);

            ItemStack key;
            KeyType keyType;

            if (i + 1 < day) {
                // Past dates
                if (playerdata.alreadySignIn(historicalDate)) {
                    key = createKeyItemWithPlaceholders(section, "Already-SignIn", historicalDate, player, placeholders);
                    keyType = KeyType.ALREADY_SIGNIN;
                } else {
                    key = createKeyItemWithPlaceholders(section, "Missed-SignIn", historicalDate, player, placeholders);
                    keyType = KeyType.MISSED_SIGNIN;
                }
            } else if (i + 1 == day) {
                // Today
                if (playerdata.alreadySignIn(historicalDate)) {
                    key = createKeyItemWithPlaceholders(section, "Already-SignIn", historicalDate, player, placeholders);
                    keyType = KeyType.ALREADY_SIGNIN;
                } else {
                    key = createKeyItemWithPlaceholders(section, "Nothing-SignIn", historicalDate, player, placeholders);
                    keyType = KeyType.NOTHING_SIGNIN;
                }
            } else {
                // Future dates
                key = createKeyItemWithPlaceholders(section, "Comming-Soon", historicalDate, player, placeholders);
                keyType = KeyType.COMMING_SOON;
            }

            items.add(key);
            keys.add(keyType);
        }
        if (section.get("Slots") != null) {
            int i = 0;
            for (String slots : section.getStringList("Slots")) {
                try {
                    Calendar cal = Calendar.getInstance();
                    cal.set(year, month - 1, i + 1);
                    set.add(new SignInGUIColumn(items.get(i), Integer.parseInt(slots),
                            SignInDate.getInstance(cal.getTime()), keys.get(i)));
                } catch (IndexOutOfBoundsException ignored) {
                }
                i++;
            }
        }
        return set;
    }

    /**
     * Return other buttons.
     *
     * @param player the player
     * @return a set of other players
     */
    public static Set<SignInGUIColumn> getOthers(Player player) {
        SignInDate today = SignInDate.getInstance(new Date());
        return getOthers(player, today.getDay(), today.getMonth(), today.getYear());
    }

    public static Set<SignInGUIColumn> getOthers(Player player, int day, int month) {
        SignInDate today = SignInDate.getInstance(new Date());
        return getOthers(player, day, month, today.getYear());
    }

    public static Set<SignInGUIColumn> getOthers(Player player, int day, int month, int year) {
        Set<SignInGUIColumn> set = new HashSet<>();
        Storage playerdata = Storage.getPlayer(player);
        String queue = String.valueOf(SignInQueue.getInstance().getRank(playerdata.getUserUUID()));
        String continuous = String.valueOf(playerdata.getContinuousSignIn());
        String totalNumber = String.valueOf(playerdata.getCumulativeNumber());
        String cards = String.valueOf(playerdata.getRetroactiveCard());
        ConfigurationSection section = ConfigurationUtil.getConfig(ConfigurationType.GUI_SETTINGS)
                .getConfigurationSection(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Others");
        int nextPageMonth = getNextPageMonth(month);
        int nextPageYear = getNextPageYear(month, year);
        int previousPageMonth = getPreviousPageMonth(month);
        int previousPageYear = getPreviousPageYear(month, year);

        if (section != null) {
            section.getKeys(false).forEach(items -> {
                ItemStack other;
                try {
                    if (section.get(items + ".Uniitem-Override") != null) {
                        String keyString = section.getString(items + ".Uniitem-Override", "").replaceAll("\\{day}", String.valueOf(day));
                        ItemKey key = ItemKey.fromString(keyString);
                        AllItemProvider provider = new AllItemProvider();
                        ItemStack uniitem = provider.item(key);
                        if (uniitem != null) {
                            other = uniitem;
                        } else {
                            if (section.get(items + ".Data") != null) {
                                other = new ItemStack(Material.valueOf(section.getString(items + ".Item", "").toUpperCase()),
                                        day, (short) section.getInt(items + ".Data"));
                            } else {
                                other = new ItemStack(Material.valueOf(section.getString(items + ".Item", "").toUpperCase()), day);
                            }
                        }
                    } else {
                        if (section.get(items + ".Data") != null) {
                            other = new ItemStack(Material.valueOf(section.getString(items + ".Item", "").toUpperCase()),
                                    day, (short) section.getInt(items + ".Data"));
                        } else {
                            other = new ItemStack(Material.valueOf(section.getString(items + ".Item", "").toUpperCase()), day);
                        }
                    }
                } catch (IllegalArgumentException ex) {
                    other = new ItemStack(Material.BARRIER);
                }
                if (section.get(items + ".Head-Owner") != null) {
                    PluginControl.setHead(other,
                            replace(player, section.getString(items + ".Head-Owner"), "{player}", player.getName()));
                }
                if (section.get(items + ".Head-Textures") != null) {
                    setHeadTextures(player,
                            MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Others." + items + ".Head-Textures",
                            other);
                }
                if (section.get(items + ".Custom-Model-Data") != null) {
                    setCustomModelData(
                            MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Others." + items + ".Custom-Model-Data",
                            other);
                }
                if (section.get(items + ".Item-Model") != null) {
                    setItemModel(
                            MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Others." + items + ".Custom-Model-Data",
                            other,
                            day
                    );
                }
                ItemMeta im = other.getItemMeta();
                if (section.get(items + ".Lore") != null) {
                    List<String> lore = new ArrayList<>();
                    Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                    placeholders.put("{continuous}", continuous);
                    placeholders.put("{queue}", queue);
                    placeholders.put("{total-number}", totalNumber);
                    placeholders.put("{cards}", cards);
                    placeholders.put("{month}", String.valueOf(month));
                    placeholders.put("{year}", String.valueOf(year));
                    placeholders.put("{nextPageMonth}", String.valueOf(nextPageMonth));
                    placeholders.put("{nextPageYear}", String.valueOf(nextPageYear));
                    placeholders.put("{previousPageMonth}", String.valueOf(previousPageMonth));
                    placeholders.put("{previousPageYear}", String.valueOf(previousPageYear));
                    section.getStringList(items + ".Lore").forEach(lores -> lore.add(MessageUtil.replacePlaceholders(player, lores, placeholders)));
                    im.setLore(lore);
                }
                if (section.get(items + ".Enchantment") != null) {
                    setEnchantments(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Others." + items + ".Enchantment",
                            im);
                }
                if (section.get(items + ".Hide-Enchants") != null)
                    PluginControl.hideEnchants(im);
                if (section.get(items + ".Display-Name") != null) {
                    im.setDisplayName(ColorUtils.toColor(
                            MessageUtil.toPlaceholderAPIResult(player, section.getString(items + ".Display-Name"))));
                }
                if (section.get(items + ".Item-Model") != null) {
                    if (Bukkit.getUnsafe().getProtocolVersion() < 769) { //1.21.4
                        return;
                    }
                    im.setItemModel(NamespacedKey.fromString(
                            MessageUtil.toPlaceholderAPIResult(player, section.getString(items + ".Item-Model", ""))));
                }
                other.setItemMeta(im);
                other.setAmount(section.get(items + ".Amount") != null ? section.getInt(items + ".Amount") : 1);
                if (section.get(items + ".Slots") != null) {
                    for (int slot : section.getIntegerList(items + ".Slots")) {
                        set.add(new SignInGUIColumn(other, slot, items));
                    }
                }
                if (section.get(items + ".Slot") != null) {
                    set.add(new SignInGUIColumn(other, section.getInt(items + ".Slot"), items));
                }
            });
        }

        return set;
    }

    public static int getNextPageMonth(int month) {
        if (month == 12) {
            return 1;
        } else {
            return month + 1;
        }
    }

    public static int getNextPageYear(int month, int year) {
        if (month != 12) {
            return year;
        }
        return year + 1;
    }

    public static int getPreviousPageMonth(int month) {
        if (month == 1) {
            return 12;
        } else {
            return month - 1;
        }
    }

    public static int getPreviousPageYear(int month, int year) {
        if (month != 1) {
            return year;
        }
        return year - 1;
    }

    private static Map<String, String> getPlaceholdersOfItemLore(
            int day,
            String continuous,
            String queue,
            String totalNumber,
            String cards,
            int nextPageMonth,
            int nextPageYear,
            int previousPageMonth,
            int previousPageYear,
            SignInDate historicalDate) {
        Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
        if (historicalDate != null)
            placeholders.put("{date}",
                    historicalDate.getName(ConfigurationUtil.getConfig(ConfigurationType.GUI_SETTINGS)
                            .getString(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Date-Format")));
        placeholders.put("{day}", String.valueOf(day + 1));
        placeholders.put("{continuous}", continuous);
        placeholders.put("{queue}", queue);
        placeholders.put("{total-number}", totalNumber);
        placeholders.put("{cards}", cards);
        placeholders.put("{nextPageMonth}", String.valueOf(nextPageMonth));
        placeholders.put("{nextPageYear}", String.valueOf(nextPageYear));
        placeholders.put("{previousPageMonth}", String.valueOf(previousPageMonth));
        placeholders.put("{previousPageYear}", String.valueOf(previousPageYear));
        return placeholders;
    }

    private static String replace(Player player, String text, String target, String replacement) {
        Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
        placeholders.put(target, replacement);
        return MessageUtil.replacePlaceholders(player, text, placeholders);
    }

    private static void setEnchantments(String configPath, ItemMeta im) {
        for (String name : ConfigurationUtil.getConfig(ConfigurationType.GUI_SETTINGS).getStringList(configPath)) {
            try {
                String[] data = name.split(":");
                boolean invalid = true;
                for (Enchantment enchant : Enchantment.values()) {
                    if (enchant.getKey().getKey().equalsIgnoreCase(data[0])) {
                        try {
                            im.addEnchant(enchant, Integer.parseUnsignedInt(data[1]), true);
                            invalid = false;
                            break;
                        } catch (Exception ex) {
                            Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                            placeholders.put("{path}", configPath + "." + name);
                            LiteSignInProperties.sendOperationMessage("InvalidEnchantmentSetting", placeholders);
                        }
                    }
                }
                if (invalid) {
                    Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                    placeholders.put("{enchantment}", data[0]);
                    placeholders.put("{path}", configPath + "." + name);
                    LiteSignInProperties.sendOperationMessage("InvalidEnchantment", placeholders);
                }
            } catch (Exception ex) {
                Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                placeholders.put("{path}", configPath + "." + name);
                LiteSignInProperties.sendOperationMessage("InvalidEnchantmentSetting", placeholders);
            }
        }
    }

    private static void setCustomModelData(String configPath, ItemStack is) {
        String version = Bukkit.getBukkitVersion();
        if (version.startsWith("1.7") ||
                version.startsWith("1.8") ||
                version.startsWith("1.9") ||
                version.startsWith("1.10") ||
                version.startsWith("1.11") ||
                version.startsWith("1.12") ||
                version.startsWith("1.13") ||
                version.equals("1.21.4"))
            return;
        if (is.getItemMeta() == null)
            return;
        ItemMeta im = is.getItemMeta();
        String name = ConfigurationUtil.getConfig(ConfigurationType.GUI_SETTINGS).getString(configPath);
        if (im == null || name == null)
            return;
        try {
            im.setCustomModelData(Integer.valueOf(name));
        } catch (Exception ex) {
            Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
            placeholders.put("{data}", name);
            placeholders.put("{path}", configPath + "." + name);
            LiteSignInProperties.sendOperationMessage("InvalidCustomModelData", placeholders);
        }
        is.setItemMeta(im);
    }

    private static void setItemModel(String configPath, ItemStack is, int day) {
        if (is.getItemMeta() == null) {
            return;
        }
        if (Bukkit.getUnsafe().getProtocolVersion() < 769) { //1.21.4
            return;
        }
        ItemMeta im = is.getItemMeta();
        String name = ConfigurationUtil.getConfig(ConfigurationType.GUI_SETTINGS).getString(configPath);
        if (im == null || name == null)
            return;

        name = name.replaceAll("\\{day}", String.valueOf(day));

        try {
            im.setItemModel(NamespacedKey.fromString(name));
        } catch (Exception ex) {
            Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
            placeholders.put("{data}", name);
            placeholders.put("{path}", configPath + "." + name);
            LiteSignInProperties.sendOperationMessage("InvalidItemModel", placeholders);
        }
        is.setItemMeta(im);
    }

    private static void setHeadTextures(Player player, String configPath, ItemStack is) {
        String version = Bukkit.getBukkitVersion();
        if (version.startsWith("1.7"))
            return;
        ItemMeta im = is.getItemMeta();
        String textures = MessageUtil.toPlaceholderAPIResult(player,
                ConfigurationUtil.getConfig(ConfigurationType.GUI_SETTINGS).getString(configPath));
        if (im == null || textures == null)
            return;
        if (is.getItemMeta() instanceof SkullMeta) {
            is.setItemMeta(SkullManager.getHeadWithTextures(textures).getItemMeta());
        }
    }

    /**
     * Get days in month for a given year (handles leap years).
     */
    private static int[] getDaysInMonth(int year) {
        int[] days = DAYS_IN_MONTH.clone();
        if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) {
            days[1] = 29;
        }
        return days;
    }

    /**
     * Create an item stack for a key button based on configuration section.
     */
    private static ItemStack createKeyItem(ConfigurationSection section, String sectionPath, SignInDate date, Player player) {
        ItemStack item;
        int day = date.getDay();;
        int month = date.getMonth();
        int year = date.getYear();
        try {
            if (section.get(sectionPath + ".Uniitem-Override") != null) {
                String keyString = section.getString(sectionPath + ".Uniitem-Override", "").replaceAll("\\{day}", String.valueOf(day));
                ItemKey key = ItemKey.fromString(keyString);
                AllItemProvider provider = new AllItemProvider();
                ItemStack uniitem = provider.item(key);
                if (uniitem != null) {
                    item = uniitem;
                } else {
                    if (section.get(sectionPath + ".Data") != null) {
                        item = new ItemStack(Material.valueOf(section.getString(sectionPath + ".Item", "").toUpperCase()),
                                day, (short) section.getInt(sectionPath + ".Data"));
                    } else {
                        item = new ItemStack(Material.valueOf(section.getString(sectionPath + ".Item", "").toUpperCase()), day);
                    }
                }
            } else {
                if (section.get(sectionPath + ".Data") != null) {
                    item = new ItemStack(Material.valueOf(section.getString(sectionPath + ".Item", "").toUpperCase()),
                            day, (short) section.getInt(sectionPath + ".Data"));
                } else {
                    item = new ItemStack(Material.valueOf(section.getString(sectionPath + ".Item", "").toUpperCase()), day);
                }
            }
        } catch (IllegalArgumentException ex) {
            item = new ItemStack(Material.BARRIER, day);
        }

        // Set head owner
        if (section.get(sectionPath + ".Head-Owner") != null) {
            PluginControl.setHead(item, replace(player, section.getString(sectionPath + ".Head-Owner"),
                    "{player}", player.getName()));
        }

        // Set amount
        if (section.get(sectionPath + ".Amount") != null) {
            item.setAmount(section.getInt(sectionPath + ".Amount"));
        }

        // Set head textures
        if (section.get(sectionPath + ".Head-Textures") != null) {
            setHeadTextures(player,
                    MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key." + sectionPath + ".Head-Textures", item);
        }

        // Set custom model data
        if (section.get(sectionPath + ".Custom-Model-Data") != null) {
            setCustomModelData(
                    MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key." + sectionPath + ".Custom-Model-Data", item);
        }

        // Set item model
        if (section.get(sectionPath + ".Item-Model") != null) {
            setItemModel(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key." + sectionPath + ".Item-Model", item, day);
        }

        // Set item meta
        ItemMeta im = item.getItemMeta();
        if (im != null) {
            // Set lore
            if (section.get(sectionPath + ".Lore") != null) {
                List<String> lore = new ArrayList<>();
                section.getStringList(sectionPath + ".Lore").forEach(loreLine -> {
                    lore.add(MessageUtil.replacePlaceholders(player, loreLine, MessageUtil.getDefaultPlaceholders()));
                });
                im.setLore(lore);
            }

            // Set enchantments
            if (section.get(sectionPath + ".Enchantment") != null) {
                setEnchantments(MessageUtil.getLanguage() + ".SignIn-GUI-Settings.Key." + sectionPath + ".Enchantment",
                        im);
            }

            // Hide enchants
            if (section.get(sectionPath + ".Hide-Enchants") != null) {
                PluginControl.hideEnchants(im);
            }

            // Set display name
            if (section.get(sectionPath + ".Display-Name") != null) {
                String displayName = section.getString(sectionPath + ".Display-Name", "");
                displayName = replace(player, displayName, "{year}", String.valueOf(year));
                displayName = replace(player, displayName, "{month}", String.valueOf(month));
                displayName = replace(player, displayName, "{day}", String.valueOf(day));
                im.setDisplayName(ColorUtils.toColor(displayName));
            }

            item.setItemMeta(im);
        }

        return item;
    }

    /**
     * Create a key item with placeholders for lore.
     */
    private static ItemStack createKeyItemWithPlaceholders(ConfigurationSection section, String sectionPath,
            SignInDate date, Player player, Map<String, String> placeholders) {
        ItemStack item = createKeyItem(section, sectionPath, date, player);

        // Update lore with placeholders
        ItemMeta im = item.getItemMeta();
        if (im != null && section.get(sectionPath + ".Lore") != null) {
            List<String> lore = new ArrayList<>();
            section.getStringList(sectionPath + ".Lore").forEach(loreLine -> {
                lore.add(MessageUtil.replacePlaceholders(player, loreLine, placeholders));
            });
            im.setLore(lore);
            item.setItemMeta(im);
        }

        return item;
    }
}
