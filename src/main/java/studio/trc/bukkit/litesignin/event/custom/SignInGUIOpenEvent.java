package studio.trc.bukkit.litesignin.event.custom;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import studio.trc.bukkit.litesignin.gui.SignInInventory;
import studio.trc.bukkit.litesignin.util.SignInDate;

import java.util.Date;

public class SignInGUIOpenEvent extends Event implements Cancellable {
    public static HandlerList handlers = new HandlerList();

    @Getter
    private final SignInDate time = SignInDate.getInstance(new Date());
    @Getter
    private final Player player;
    @Getter
    private final SignInInventory inventory;
    @Getter
    private int month = time.getMonth();
    @Getter
    private int year = time.getYear();

    private boolean cancelled = false;

    public SignInGUIOpenEvent(Player player, SignInInventory inventory) {
        this.player = player;
        this.inventory = inventory;
    }

    public SignInGUIOpenEvent(Player player, SignInInventory inventory, int month) {
        this.player = player;
        this.inventory = inventory;
        this.month = month;
    }

    public SignInGUIOpenEvent(Player player, SignInInventory inventory, int month, int year) {
        this.player = player;
        this.inventory = inventory;
        this.month = month;
        this.year = year;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        cancelled = b;
    }
}
