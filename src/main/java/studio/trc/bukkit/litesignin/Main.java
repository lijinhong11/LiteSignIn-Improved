package studio.trc.bukkit.litesignin;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import studio.trc.bukkit.litesignin.command.SignInCommand;
import studio.trc.bukkit.litesignin.command.SignInSubCommandType;
import studio.trc.bukkit.litesignin.configuration.ConfigurationType;
import studio.trc.bukkit.litesignin.configuration.ConfigurationUtil;
import studio.trc.bukkit.litesignin.database.engine.MySQLEngine;
import studio.trc.bukkit.litesignin.database.engine.SQLiteEngine;
import studio.trc.bukkit.litesignin.database.storage.MySQLStorage;
import studio.trc.bukkit.litesignin.database.storage.SQLiteStorage;
import studio.trc.bukkit.litesignin.database.util.BackupUtil;
import studio.trc.bukkit.litesignin.listeners.PlayerListener;
import studio.trc.bukkit.litesignin.listeners.MenuListener;
import studio.trc.bukkit.litesignin.message.MessageUtil;
import studio.trc.bukkit.litesignin.thread.LiteSignInThread;
import studio.trc.bukkit.litesignin.util.LiteSignInProperties;
import studio.trc.bukkit.litesignin.util.PluginControl;
import studio.trc.bukkit.litesignin.util.metrics.Metrics;
import studio.trc.bukkit.litesignin.listeners.WoodSignListener;

/**
 * Do not resell the source code of this plug-in.
 *
 * @author TRCStudioDean
 */
public class Main
        extends JavaPlugin {
    /**
     * Main instance
     */
    private static Main main;
    @Getter
    private static Metrics metrics;

    public static Main getInstance() {
        return main;
    }

    @Override
    public void onEnable() {
        main = this;

        if (!getDescription().getName().equals("LiteSignIn")) {
            LiteSignInProperties.sendOperationMessage("PluginNameChange");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        registerCommandExecutor();
        registerEvent();
        PluginControl.reload();
        LiteSignInProperties.sendOperationMessage("PluginEnabledSuccessfully", MessageUtil.getDefaultPlaceholders());

        //It will run after the server is started.
        PluginControl.runBukkitTask(() -> {
            //if (PluginControl.enableUpdater()) {
            //   Updater.checkUpdate();
            //}
            if (!MessageUtil.useAdventure()) {
                try {
                    if (Integer.parseInt(Bukkit.getBukkitVersion().split("\\.")[1]) > 19) {
                        LiteSignInProperties.sendOperationMessage("UnsupportedCore", MessageUtil.getDefaultPlaceholders());
                    }
                } catch (Throwable ex) {
                    LiteSignInProperties.sendOperationMessage("UnsupportedCore", MessageUtil.getDefaultPlaceholders());
                }
            }
        }, 0);

        //Metrics
        if (PluginControl.enableMetrics()) {
            metrics = new Metrics(main, 11849);
        }
    }

    @Override
    public void onDisable() {
        LiteSignInThread.getTaskThread().setRunning(false);
        LiteSignInProperties.sendOperationMessage("AsyncThreadStopped", MessageUtil.getDefaultPlaceholders());
        if (PluginControl.useMySQLStorage()) {
            MySQLStorage.cache.values().forEach(MySQLStorage::saveData);
        } else if (PluginControl.useSQLiteStorage()) {
            SQLiteStorage.cache.values().forEach(SQLiteStorage::saveData);
        }
        if (ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getBoolean("Database-Management.Backup.Auto-Backup")) {
            MessageUtil.sendMessage(getServer().getConsoleSender(), "Database-Management.Backup.Auto-Backup");
            Thread thread = BackupUtil.startBackup(getServer().getConsoleSender());
            while (thread.isAlive()) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
        if (SQLiteEngine.getInstance() != null) {
            SQLiteEngine.getInstance().disconnect();
        }
        if (MySQLEngine.getInstance() != null) {
            MySQLEngine.getInstance().disconnect();
        }
    }

    private void registerEvent() {
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new PlayerListener(), Main.getInstance());
        pm.registerEvents(new MenuListener(), Main.getInstance());
        pm.registerEvents(new WoodSignListener(), Main.getInstance());
        LiteSignInProperties.sendOperationMessage("PluginListenerRegistered");
    }

    private void registerCommandExecutor() {
        PluginCommand command = getCommand("signin");
        SignInCommand commandExecutor = new SignInCommand();
        command.setExecutor(commandExecutor);
        command.setTabCompleter(commandExecutor);
        for (SignInSubCommandType subCommandType : SignInSubCommandType.values()) {
            SignInCommand.getSubCommands().put(subCommandType.getSubCommandName(), subCommandType.getSubCommand());
        }
        LiteSignInProperties.sendOperationMessage("PluginCommandRegistered");
    }
}
