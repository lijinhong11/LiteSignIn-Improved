package studio.trc.bukkit.litesignin.util.woodsignscript;

import studio.trc.bukkit.litesignin.util.woodsignscript.WoodSignUtil.WoodSignLine;

import java.util.List;

public record WoodSign(String woodSignTitle, WoodSignLine woodSignText, List<String> woodSignCommand,
                       String permission) {
}
