package net.slipcor.mobstats.classes;

import net.slipcor.mobstats.api.InformationType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A container class that holds all Entity stats, used when handling database results
 */
public class EntityStatistic {
    private final String name;
    private int kills;
    private int deaths;
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

    public double getRatio() {
        return ((double) kills) / (deaths + 1);
    }

    public UUID getUid() { return uid; }

    public Map<InformationType, String> toStringMap() {
        Map<InformationType, String> result = new HashMap<>();

        result.put(InformationType.NAME, String.valueOf(name));
        result.put(InformationType.DEATHS, String.valueOf(deaths));
        result.put(InformationType.KILLS, String.valueOf(kills));
        result.put(InformationType.CURRENTSTREAK, String.valueOf(currentstreak));
        result.put(InformationType.STREAK, String.valueOf(streak));

        return result;
    }

    public void addKill() {
        kills++;
    }

    public void addDeath() {
        deaths++;
    }
}
