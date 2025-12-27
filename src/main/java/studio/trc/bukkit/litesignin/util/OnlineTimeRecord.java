package studio.trc.bukkit.litesignin.util;

import org.bukkit.entity.Player;
import studio.trc.bukkit.litesignin.configuration.ConfigurationType;
import studio.trc.bukkit.litesignin.configuration.ConfigurationUtil;
import studio.trc.bukkit.litesignin.configuration.RobustConfiguration;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public record OnlineTimeRecord(long timeInMillis, SignInDate recordTime) {
    private static final Map<UUID, Long> joinTimeRecord = new HashMap<>();
    private static final Map<UUID, OnlineTimeRecord> onlineTimeRecords = new HashMap<>();

    public static void savePlayerOnlineTime(Player player) {
        onlineTimeRecords.put(player.getUniqueId(), new OnlineTimeRecord(getTodayOnlineTime(player), SignInDate.getInstance(new Date())));
    }

    public static Map<UUID, Long> getJoinTimeRecord() {
        return joinTimeRecord;
    }

    public static long getTodayOnlineTime(Player player) {
        return getTodayOnlineTime(player.getUniqueId());
    }

    public static long getTodayOnlineTime(UUID uuid) {
        if (!joinTimeRecord.containsKey(uuid)) return 0;
        SignInDate lastPlayed = SignInDate.getInstance(new Date(joinTimeRecord.get(uuid)));
        SignInDate now = SignInDate.getInstance(new Date());
        if (onlineTimeRecords.containsKey(uuid) && ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getBoolean("Online-Duration-Condition.Statistics")) {
            if (lastPlayed.getYear() == now.getYear() && lastPlayed.getMonth() == now.getMonth() && lastPlayed.getDay() == now.getDay()) {
                OnlineTimeRecord record = onlineTimeRecords.get(uuid);
                if (record.recordTime().getYear() == now.getYear() && record.recordTime().getMonth() == now.getMonth() && record.recordTime().getDay() == now.getDay()) {
                    return record.timeInMillis() + now.getMillisecond() - lastPlayed.getMillisecond();
                } else {
                    return now.getMillisecond() - lastPlayed.getMillisecond();
                }
            } else {
                lastPlayed = SignInDate.getInstance(now.getYear(), now.getMonth(), now.getDay(), 0, 0, 0);
                return now.getMillisecond() - lastPlayed.getMillisecond();
            }
        } else {
            if (lastPlayed.getYear() != now.getYear() || lastPlayed.getMonth() != now.getMonth() || lastPlayed.getDay() != now.getDay()) {
                lastPlayed = SignInDate.getInstance(now.getYear(), now.getMonth(), now.getDay(), 0, 0, 0);
            }
            return now.getMillisecond() - lastPlayed.getMillisecond();
        }
    }

    public static long getSignInRequirement(Player player) {
        RobustConfiguration config = ConfigurationUtil.getConfig(ConfigurationType.CONFIG);
        if (config.getBoolean("Online-Duration-Condition.Enabled")) {
            String[] time = config.getString("Online-Duration-Condition.Time").split(":");
            if (time.length == 3 && LiteSignInUtils.isInteger(time[0]) && LiteSignInUtils.isInteger(time[1]) && LiteSignInUtils.isInteger(time[2])) {
                long requirement = Long.parseLong(time[0]) * 1000 * 60 * 60 + Long.parseLong(time[1]) * 1000 * 60 + Long.parseLong(time[2]) * 1000;
                return getTodayOnlineTime(player) >= requirement ? -1 : requirement - getTodayOnlineTime(player);
            }
        }
        return -1;
    }
}
