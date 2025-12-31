package studio.trc.bukkit.litesignin.api;

import studio.trc.bukkit.litesignin.util.PluginControl;
import studio.trc.bukkit.litesignin.util.SignInDate;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Statistics on Users
 *
 * @author Dean
 */
public interface Statistics {
    Map<UUID, Long> lastSignInTime = new HashMap<>();

    default boolean isRetroactiveCardCooldown() {
        return lastSignInTime.containsKey(getUserUUID()) && System.currentTimeMillis() - lastSignInTime.get(getUserUUID()) <= PluginControl.getRetroactiveCardIntervals() * 1000;
    }

    default double getRetroactiveCardCooldown() {
        return lastSignInTime.containsKey(getUserUUID()) ? Double.parseDouble(new DecimalFormat("#.0").format(PluginControl.getRetroactiveCardIntervals() - ((double) (System.currentTimeMillis() - lastSignInTime.get(getUserUUID())) / 1000))) : 0;
    }

    UUID getUserUUID();

    /**
     * Check whether players sign in continuously.
     */
    void checkContinuousSignIn();

    /**
     * Check whether users sign in on that day.
     *
     * @return
     */
    boolean alreadySignIn();

    /**
     * Check whether the user is signed in on the day of user history.
     *
     * @param date
     * @return
     */
    boolean alreadySignIn(SignInDate date);

    /**
     * Get the cumulative numbers of user sign in.
     *
     * @return
     */
    int getCumulativeNumber();

    /**
     * Get the cumulative numbers of this month by user sign-in.
     *
     * @param year
     * @param month
     * @return
     */
    int getCumulativeNumberOfMonth(int year, int month);

    /**
     * Clean up duplicate sign in records.
     *
     * @param history
     * @return
     */
    List<SignInDate> clearUselessData(List<SignInDate> history);
}
