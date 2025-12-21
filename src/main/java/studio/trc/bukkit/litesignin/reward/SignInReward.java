package studio.trc.bukkit.litesignin.reward;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import studio.trc.bukkit.litesignin.api.Storage;
import studio.trc.bukkit.litesignin.reward.command.SignInRewardCommand;
import studio.trc.bukkit.litesignin.reward.util.SignInGroup;
import studio.trc.bukkit.litesignin.reward.util.SignInSound;

import java.util.List;

public interface SignInReward {
    /**
     * Give reward.
     *
     * @param playerData
     */
    void giveReward(Storage playerData);

    /**
     * Get SignInReward permission group.
     *
     * @return
     */
    SignInGroup getGroup();

    /**
     * Get SignInReward module
     * It is used to indicate the reward form of SignInReward.
     *
     * @return
     */
    SignInRewardModule getModule();

    /**
     * @return
     */
    List<String> getMessages();

    /**
     * @return
     */
    List<String> getBroadcastMessages();

    /**
     * @return
     */
    List<SignInRewardCommand> getCommands();

    /**
     * @return
     */
    List<SignInSound> getSounds();

    /**
     * Get Reward items.
     *
     * @param player Use for PlaceholderAPI request.
     * @return
     */
    List<ItemStack> getRewardItems(Player player);
}
