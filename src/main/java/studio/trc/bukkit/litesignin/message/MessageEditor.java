package studio.trc.bukkit.litesignin.message;

import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import studio.trc.bukkit.litesignin.util.AdventureUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageEditor {
    public static Component createAdventureJSONMessage(CommandSender sender, String message, Map<String, Component> components) {
        List<MessageSection> sections = parse(message, components);
        Component component = null;
        for (MessageSection section : sections) {
            if (section.isPlaceholder()) {
                component = component == null ? section.getAdventureComponent() : component.append(section.getAdventureComponent());
            } else {
                String text = MessageUtil.toPlaceholderAPIResult(sender, section.getText()).replace("/n", "\n");
                component = component == null ? AdventureUtils.serializeText(text) : component.append(AdventureUtils.serializeText(text));
            }
        }
        return component != null ? component : Component.empty();
    }

    public static <T> List<MessageSection> parse(String message, Map<String, T> placeholders) {
        //Convert all placeholders to lowercase (in order to ignore case matching in the following code)
        Map<String, T> normalizedMap = new HashMap<>();
        placeholders.forEach((key, value) -> normalizedMap.put(key.toLowerCase(), value));

        //Sort placeholders in descending order by length (avoid short placeholders matching the prefix of long placeholders)
        List<String> sortedKeys = new ArrayList<>(normalizedMap.keySet());
        sortedKeys.sort((s1, s2) -> Integer.compare(s2.length(), s1.length()));

        List<MessageSection> result = new ArrayList<>();
        StringBuilder currentText = new StringBuilder();
        int index = 0;
        int messageLength = message.length();
        int textStart = 0;

        while (index < messageLength) {
            boolean matched = false;
            //Scan each placeholder for matching
            for (String keyLower : sortedKeys) {
                int keyLength = keyLower.length();
                int endIndex = index + keyLength;
                if (endIndex > messageLength) continue;
                String paragraph = message.substring(index, endIndex);
                if (paragraph.toLowerCase().equals(keyLower)) {
                    //When the match is successful, add the accumulated text to the result first
                    if (!currentText.isEmpty()) {
                        result.add(new MessageSection(
                                currentText.toString(),
                                null,
                                textStart,
                                textStart + currentText.length()
                        ));
                        currentText.setLength(0);
                    }
                    T replacement = normalizedMap.get(keyLower);
                    if (replacement instanceof String) {
                        result.add(new MessageSection(
                                replacement.toString(),
                                paragraph,
                                index,
                                index + keyLength
                        ));
                    } else if (replacement instanceof Component component) {
                        result.add(new MessageSection(
                                component,
                                paragraph,
                                index,
                                index + keyLength
                        ));
                    }
                    //Point the pointer after the placeholder to skip the current placeholder position
                    index = endIndex;
                    textStart = index;
                    matched = true;
                    break;
                }
            }
            //If no placeholder is found, proceed to the next character
            if (!matched) {
                if (currentText.isEmpty()) {
                    textStart = index;
                }
                currentText.append(message.charAt(index));
                index++;
            }
        }

        //Process the remaining text
        if (!currentText.isEmpty()) {
            result.add(new MessageSection(
                    currentText.toString(),
                    null,
                    textStart,
                    textStart + currentText.length()
            ));
        }
        return result;
    }
}
