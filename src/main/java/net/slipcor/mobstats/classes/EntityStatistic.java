package net.slipcor.mobstats.classes;

import net.slipcor.mobstats.display.SortColumn;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A container class that holds all Entity stats, used when handling database results
 */
public class EntityStatistic {
    private final String name;
    private final int kills;
    private final int deaths;
    private final int streak;
    private final int currentstreak;
    private final long time;
    private final UUID uid;

    public EntityStatistic(String name, int kills, int deaths, int streak, int currentstreak, long time, UUID uid) {
        this.name = name;
        this.kills = kills;
        this.deaths = deaths;
        this.streak = streak;
        this.currentstreak = currentstreak;
        this.time = time;
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public int getKills() {
        return kills;
    }

    public int getDeaths() {
        return deaths;
    }

    public int getMaxStreak() {
        return streak;
    }

    public int getCurrentStreak() {
        return currentstreak;
    }

    public long getTime() {
        return time;
    }

    public UUID getUid() { return uid; }

    public Map<SortColumn, String> toStringMap() {
        Map<SortColumn, String> result = new HashMap<>();

        result.put(SortColumn.NAME, String.valueOf(name));
        result.put(SortColumn.DEATHS, String.valueOf(deaths));
        result.put(SortColumn.KILLS, String.valueOf(kills));
        result.put(SortColumn.CURRENTSTREAK, String.valueOf(currentstreak));
        result.put(SortColumn.STREAK, String.valueOf(streak));

        return result;
    }
}
