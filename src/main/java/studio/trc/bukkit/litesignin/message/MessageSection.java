package studio.trc.bukkit.litesignin.message;

import lombok.Getter;
import net.kyori.adventure.text.Component;

public class MessageSection {
    @Getter
    private final int startsWith;
    @Getter
    private final int endsWith;
    @Getter
    private final Component adventureComponent;
    @Getter
    private final String text;
    @Getter
    private final String placeholder;

    public MessageSection(String text, String placeholder, int startsWith, int endsWith) {
        this.text = text;
        this.startsWith = startsWith;
        this.endsWith = endsWith;
        this.placeholder = placeholder;
        adventureComponent = null;
    }

    public MessageSection(Component adventureComponent, String placeholder, int startsWith, int endsWith) {
        this.startsWith = startsWith;
        this.endsWith = endsWith;
        this.placeholder = placeholder;
        this.adventureComponent = adventureComponent;
        text = null;
    }

    public boolean isPlaceholder() {
        return placeholder != null;
    }

    @Override
    public String toString() {
        return text;
    }
}