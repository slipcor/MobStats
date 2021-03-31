package net.slipcor.mobstats.api;

import net.slipcor.mobstats.MobStats;
import net.slipcor.mobstats.classes.EntityStatistic;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

import java.util.*;

/**
 * Class for fast temporary access to Entity statistics
 * <p>
 * Should never be publicly used to SET variables, only for quick access to existing values
 */
public final class EntityStatisticsBuffer {

    private static final Map<UUID, Integer> kills = new HashMap<>();
    private static final Map<UUID, Integer> deaths = new HashMap<>();
    private static final Map<UUID, Integer> streaks = new HashMap<>();
    private static final Map<UUID, Integer> maxStreaks = new HashMap<>();

    private EntityStatisticsBuffer() {
    }

    /**
     * Increase an Entity's death count
     *
     * @param uuid the Entity's UUID
     */
    public static void addDeath(UUID uuid) {
        int value = deaths.containsKey(uuid) ? deaths.get(uuid) : 0;

        deaths.put(uuid, ++value);
    }

    /**
     * Increase an Entity's kill count
     *
     * @param uuid the Entity's UUID
     */
    public static void addKill(UUID uuid) {
        int value = kills.containsKey(uuid) ? kills.get(uuid) : 0;

        kills.put(uuid, ++value);
    }

    /**
     * Increase an Entity's killstreak - eventually increases the maximum killstreak
     *
     * @param uuid the Entity UUID to handle
     * @return true if the maximum streak should be increased wise
     */
    public static boolean addStreak(UUID uuid) {
        final int streak = streaks.get(uuid) + 1;

        Bukkit.getScheduler().runTaskLater(
                MobStats.getInstance(), new Runnable() {
                    @Override
                    public void run() {
                        // issue streak commands AFTER the death message has gone through
                        MobStats.getInstance().handleStreak(uuid, streak);
                    }
                }, 1L
        );

        streaks.put(uuid, streak);
        if (hasMaxStreak(uuid)) {
            if (EntityStatisticsBuffer.maxStreaks.get(uuid) < streak) {
                EntityStatisticsBuffer.maxStreaks.put(uuid, Math.max(EntityStatisticsBuffer.maxStreaks.get(uuid), streak));
                return true;
            }
        } else {
            int max = getMaxStreak(uuid); // load the streaks
            if (max > streak) {
                return false;
            }
            maxStreaks.put(uuid, streak);
            return true;
        }
        return false;
    }

    /**
     * Clear an Entity's temporary variables
     *
     * @param uuid the Entity UUID to clear, null to clear everything
     */
    public static void clear(UUID uuid) {
        if (uuid == null) {
            deaths.clear();
            kills.clear();
            maxStreaks.clear();
            streaks.clear();
        } else {
            clearDeaths(uuid);
            clearKills(uuid);
            clearMaxStreak(uuid);
            clearStreak(uuid);
        }
    }

    /**
     * Clear an Entity's death count
     *
     * @param uuid the Entity UUID to clear
     */
    public static void clearDeaths(UUID uuid) {
        deaths.remove(uuid);
    }

    /**
     * Clear an Entity's kill count
     *
     * @param uuid the Entity UUID to clear
     */
    public static void clearKills(UUID uuid) {
        kills.remove(uuid);
    }

    /**
     * Clear an Entity's maximum kill streak
     *
     * @param uuid the Entity UUID to clear
     */
    public static void clearMaxStreak(UUID uuid) {
        maxStreaks.remove(uuid);
    }

    /**
     * Clear an Entity's current kill streak
     *
     * @param uuid the Entity UUID to clear
     */
    public static void clearStreak(UUID uuid) {
        streaks.remove(uuid);
    }

    /**
     * Get an Entity's death count
     *
     * @param uuid the Entity UUID to read
     * @return the Entity's death count
     */
    public static Integer getDeaths(UUID uuid) {
        if (deaths.containsKey(uuid)) {
            return deaths.get(uuid);
        }

        final int value = DatabaseAPI.getEntry(uuid, "deaths");
        deaths.put(uuid, value);
        return value;
    }

    /**
     * Get an Entity's kill count
     *
     * @param uuid the Entity id to read
     * @return the Entity's kill count
     */
    public static Integer getKills(UUID uuid) {
        if (kills.containsKey(uuid)) {
            return kills.get(uuid);
        }

        final int value = DatabaseAPI.getEntry(uuid, "kills");
        kills.put(uuid, value);
        return value;
    }

    /**
     * Get an Entity's maximum kill streak
     *
     * @param uuid the Entity id to read
     * @return the Entity's maximum kill streak
     */
    public static Integer getMaxStreak(UUID uuid) {
        if (hasMaxStreak(uuid)) {
            return maxStreaks.get(uuid);
        }

        final int value = DatabaseAPI.getEntry(uuid, "streak");
        maxStreaks.put(uuid, value);
        return value;
    }

    /**
     * Get an Entity's current kill streak
     *
     * @param uuid the Entity id to read
     * @return the Entity's current kill streak
     */
    public static Integer getStreak(UUID uuid) {
        if (hasStreak(uuid)) {
            return streaks.get(uuid);
        }

        final int value = DatabaseAPI.getEntry(uuid, "currentstreak");
        streaks.put(uuid, value);
        return value;
    }

    /**
     * Get an Entity's current configurable kill/death ratio
     *
     * @param uuid the Entity id UUID to read
     * @return the Entity's current k/d ratio
     */
    public static Double getRatio(UUID uuid) {
        return DatabaseAPI.calculateRatio(getKills(uuid), getDeaths(uuid), getStreak(uuid), getMaxStreak(uuid));
    }

    /**
     * Does an Entity already have a maximum kill streak
     *
     * @param uuid the Entity UUID to check
     * @return true if the Entity has a maximum kill streak
     */
    public static boolean hasMaxStreak(UUID uuid) {
        return maxStreaks.containsKey(uuid);
    }

    /**
     * Does an Entity already have a kill streak
     *
     * @param uuid the Entity UUID to check
     * @return true if the Entity has a kill streak
     */
    public static boolean hasStreak(UUID uuid) {
        return streaks.containsKey(uuid);
    }

    /**
     * Make sure all statistics are loaded, query the database if necessary
     * @param entity the Entity whose stats to load
     */
    public static void loadEntity(Entity entity) {
        UUID uuid = entity.getUniqueId();
        if (deaths.containsKey(uuid) && kills.containsKey(uuid)) {
            return; // we already did it
        }

        EntityStatistic statistic = DatabaseAPI.getAllStats(entity);

        deaths.put(uuid, statistic.getDeaths());
        kills.put(uuid, statistic.getKills());
        streaks.put(uuid, statistic.getCurrentStreak());
        maxStreaks.put(uuid, statistic.getMaxStreak());
    }

    /**
     * Force set an Entity's death count - this does NOT update the database!
     *
     * @param uuid  the Entity UUID to update
     * @param value the value to set
     */
    public static void setDeaths(UUID uuid, int value) {
        deaths.put(uuid, value);
    }

    /**
     * Force set an Entity's kill count - this does NOT update the database!
     *
     * @param uuid  the Entity UUID to update
     * @param value the value to set
     */
    public static void setKills(UUID uuid, int value) {
        kills.put(uuid, value);
    }

    /**
     * Force set an Entity's max killstreak count - this does NOT update the database!
     *
     * @param uuid  the Entity UUID to update
     * @param value the value to set
     */
    public static void setMaxStreak(UUID uuid, int value) {
        maxStreaks.put(uuid, value);
    }

    /**
     * Force set an Entity's killstreak count - this does NOT update the database!
     *
     * @param uuid  the Entity UUID to update
     * @param value the value to set
     */
    public static void setStreak(UUID uuid, int value) {
        streaks.put(uuid, value);
    }

    /**
     * Refresh the maps after making changes by command
     */
    static void refresh() {
        List<UUID> uuids = new ArrayList<>(DatabaseAPI.getAllUUIDs());

        clear(null); // clear all entries

        for (UUID uuid : uuids) {
            Entity entity = Bukkit.getServer().getEntity(uuid);
            DatabaseAPI.info(entity); // pre-load previously loaded Entities
        }
    }
}
