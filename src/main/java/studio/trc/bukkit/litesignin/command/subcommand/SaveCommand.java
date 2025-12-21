package studio.trc.bukkit.litesignin.command.subcommand;

import org.bukkit.command.CommandSender;
import studio.trc.bukkit.litesignin.command.SignInSubCommand;
import studio.trc.bukkit.litesignin.command.SignInSubCommandType;
import studio.trc.bukkit.litesignin.message.MessageUtil;
import studio.trc.bukkit.litesignin.util.PluginControl;

import java.util.ArrayList;
import java.util.List;

public class SaveCommand
        implements SignInSubCommand {
    @Override
    public void execute(CommandSender sender, String subCommand, String... args) {
        PluginControl.savePlayerData();
        MessageUtil.sendCommandMessage(sender, "Save");
    }

    @Override
    public String getName() {
        return "save";
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String subCommand, String... args) {
        return new ArrayList<>();
    }

    @Override
    public SignInSubCommandType getCommandType() {
        return SignInSubCommandType.SAVE;
    }
}
