package studio.trc.bukkit.litesignin.message;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import studio.trc.bukkit.litesignin.util.AdventureUtils;
import studio.trc.bukkit.litesignin.util.LiteSignInProperties;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JSONComponent {
    @Getter
    private final String text;
    @Getter
    private final String clickAction;
    @Getter
    private final String clickContent;
    @Getter
    private final List<String> hoverContent;

    private Component adventureComponent = null;

    public JSONComponent(String text, List<String> hoverContent, String clickAction, String clickContent) {
        this.text = text;
        this.hoverContent = hoverContent;
        this.clickAction = clickAction;
        this.clickContent = clickContent;
    }

    public Component getAdventureComponent() {
        if (adventureComponent == null) {
            HoverEvent<?> hoverEvent = null;
            ClickEvent clickEvent = null;
            try {
                if (!hoverContent.isEmpty()) {
                    hoverEvent = AdventureUtils.showText(hoverContent.stream().map(MessageUtil::doBasicProcessing).collect(Collectors.joining("\n")));
                }
                if (clickAction != null) {
                    clickEvent = AdventureUtils.getClickEvent(clickAction, MessageUtil.doBasicProcessing(clickContent));
                }
            } catch (Exception ex) {
                Map<String, String> placeholders = MessageUtil.getDefaultPlaceholders();
                placeholders.put("{exception}", ex.getLocalizedMessage() != null ? ex.getLocalizedMessage() : "null");
                LiteSignInProperties.sendOperationMessage("LoadingJSONComponentFailed", placeholders);
                ex.printStackTrace();
            }
            Component component = AdventureUtils.serializeText(MessageUtil.doBasicProcessing(text));
            if (hoverEvent != null) component = AdventureUtils.setHoverEvent(component, hoverEvent);
            if (clickEvent != null) component = AdventureUtils.setClickEvent(component, clickEvent);
            adventureComponent = component;
        }

        return adventureComponent;
    }

    public Component getAdventureComponent(Map<String, String> placeholders) {
        try {
            HoverEvent<?> hoverEvent = null;
            ClickEvent clickEvent = null;
            try {
                if (!hoverContent.isEmpty()) {
                    hoverEvent = AdventureUtils.showText(String.join("\n", hoverContent.stream().map(hover -> MessageUtil.replacePlaceholders(hover, placeholders)).collect(Collectors.toList())));
                }
                if (clickAction != null) {
                    clickEvent = AdventureUtils.getClickEvent(clickAction, MessageUtil.replacePlaceholders(clickContent, placeholders));
                }
            } catch (Exception ex) {
                placeholders.put("{exception}", ex.getLocalizedMessage() != null ? ex.getLocalizedMessage() : "null");
                LiteSignInProperties.sendOperationMessage("LoadingJSONComponentFailed", placeholders);
                ex.printStackTrace();
            }
            Component component = AdventureUtils.serializeText(MessageUtil.replacePlaceholders(text, placeholders));
            if (hoverEvent != null) component = AdventureUtils.setHoverEvent(component, hoverEvent);
            if (clickEvent != null) component = AdventureUtils.setClickEvent(component, clickEvent);
            return component;
        } catch (Exception ex) {
            placeholders.put("{exception}", ex.getLocalizedMessage() != null ? ex.getLocalizedMessage() : "null");
            LiteSignInProperties.sendOperationMessage("LoadingJSONComponentFailed", placeholders);
            ex.printStackTrace();
        }
        return null;
    }
}
