package studio.trc.bukkit.litesignin.api.events;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import studio.trc.bukkit.litesignin.reward.SignInRewardSchedule;

public class SignInRewardEvent extends Event implements Cancellable {
    public static HandlerList handlers = new HandlerList();

    @Getter
    private final Player player;
    @Getter
    private final SignInRewardSchedule rewardQueue;

    private boolean cancelled = false;

    public SignInRewardEvent(Player player, SignInRewardSchedule rewardQueue) {
        this.player = player;
        this.rewardQueue = rewardQueue;
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
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }
}
