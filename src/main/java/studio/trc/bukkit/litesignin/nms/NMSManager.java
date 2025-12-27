package studio.trc.bukkit.litesignin.nms;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import studio.trc.bukkit.litesignin.util.AdventureUtils;

@ApiStatus.ScheduledForRemoval
//It is a shit.
public class NMSManager {
    public static Class<?> craftItemStack;
    public static Class<?> nbtTagCompound = null;
    //    public static Class<?> gameProfileSerializer1 = null;
    public static Class<?> itemStack = null;
    public static boolean nmsFound;

    public static String getPackageName() {
        return Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    }

    public static void reloadNMS() {
        //craftbukkit
        try {
            craftItemStack = Class.forName("org.bukkit.craftbukkit." + getPackageName() + ".inventory.CraftItemStack");
        } catch (ArrayIndexOutOfBoundsException ex) {
            try {
                craftItemStack = Class.forName("org.bukkit.craftbukkit.inventory.CraftItemStack");
            } catch (ClassNotFoundException ex1) {
                nmsFound = false;
            }
        } catch (ClassNotFoundException ex) {
            nmsFound = false;
        }

        //net.minecraft.server
        if (!Bukkit.getBukkitVersion().startsWith("1.7") && !Bukkit.getBukkitVersion().startsWith("1.8") && !Bukkit.getBukkitVersion().startsWith("1.9") && !Bukkit.getBukkitVersion().startsWith("1.10")
                && !Bukkit.getBukkitVersion().startsWith("1.11") && !Bukkit.getBukkitVersion().startsWith("1.12") && !Bukkit.getBukkitVersion().startsWith("1.13") && !Bukkit.getBukkitVersion().startsWith("1.14")
                && !Bukkit.getBukkitVersion().startsWith("1.15") && !Bukkit.getBukkitVersion().startsWith("1.16")) {
            try {
                nbtTagCompound = Class.forName("net.minecraft.nbt.NBTTagCompound");
//                gameProfileSerializer = Class.forName("net.minecraft.nbt.GameProfileSerializer");
                itemStack = Class.forName("net.minecraft.world.item.ItemStack");
            } catch (ClassNotFoundException ex) {
                // 1.21.9+ No longer needed.
            }
        } else {
            try {
                nbtTagCompound = Class.forName("net.minecraft.server." + getPackageName() + ".NBTTagCompound");
//                gameProfileSerializer = Class.forName("net.minecraft.server." + getPackageName() + ".GameProfileSerializer");
                itemStack = Class.forName("net.minecraft.server." + getPackageName() + ".ItemStack");
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
                nmsFound = false;
            }
        }
    }

    public static Component setItemHover(ItemStack item, Object component) {
        return AdventureUtils.toComponent(component).hoverEvent(item);
    }

    public static Component getAdventureJSONItemStack(ItemStack item) {
        if (item != null && !item.getType().equals(Material.AIR)) {
            try {
                String translationKey = Material.class.getMethod("translationKey").invoke(item.getType()).toString();
                return setItemHover(item, Component.translatable(translationKey));
            } catch (Exception ex) {
                return setItemHover(item, AdventureUtils.serializeText(toDisplayName(item.getType().name())));
            }
        }
        return Component.text("");
    }

    private static String toDisplayName(String text) {
        String[] words = text.split("_", -1);
        for (int i = 0; i < words.length; i++) {
            if (words[i].length() <= 1) continue;
            words[i] = words[i].substring(0, 1).toUpperCase() + words[i].substring(1).toLowerCase();
        }
        return String.join(" ", words);
    }
}
