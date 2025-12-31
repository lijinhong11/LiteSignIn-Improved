package studio.trc.bukkit.litesignin.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Used to avoid serious errors caused by the absence of AdventureAPI when JVM loads classes in low version or non AdventureAPI servers.
 */
public class AdventureUtils {
    public static Component setHoverEvent(Component component, HoverEvent<?> event) {
        return component.hoverEvent(event);
    }

    public static Component setClickEvent(Component component, ClickEvent event) {
        return component.clickEvent(event);
    }

    public static Component toComponent(Component obj) {
        return obj;
    }

    public static Component serializeText(String text) {
        return LegacyComponentSerializer.legacySection().deserialize(text);
    }

    public static HoverEvent<Component> showText(String text) {
        return HoverEvent.showText(AdventureUtils.serializeText(text));
    }

    public static ClickEvent getClickEvent(String action, String text) {
        return switch (action.toUpperCase()) {
            case "SUGGEST_COMMAND" -> ClickEvent.suggestCommand(text);
            case "RUN_COMMAND" -> ClickEvent.runCommand(text);
            case "OPEN_URL" -> ClickEvent.openUrl(text);
            case "COPY_TO_CLIPBOARD" -> ClickEvent.copyToClipboard(text);
            case "OPEN_FILE" -> ClickEvent.openFile(text);
            default -> null;
        };
    }

    public static Map<String, Component> getItemDisplay(List<CustomItem> itemList) {
        Map<String, Component> json = new HashMap<>();
        Component component = Component.text("");
        for (int i = 0; i < itemList.size(); i++) {
            component = component.append(getAdventureHoverItemStack(itemList.get(i).itemStack()));
            if (i != itemList.size() - 1) {
                component = component.append(Component.text(", "));
            }
        }
        json.put("%list%", component);
        return json;
    }

    public static Component getAdventureHoverItemStack(ItemStack item) {
        if (item != null && !item.getType().equals(Material.AIR)) {
            String translationKey = item.getType().translationKey();
            return Component.translatable(translationKey).hoverEvent(item);
        }
        return Component.text("");
    }
}
