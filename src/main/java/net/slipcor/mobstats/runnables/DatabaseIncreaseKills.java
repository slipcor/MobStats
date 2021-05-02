package net.slipcor.mobstats.runnables;

import net.slipcor.core.CoreDebugger;
import net.slipcor.mobstats.MobStats;

import java.util.UUID;

public class DatabaseIncreaseKills implements Runnable {
    private final String name;
    private final UUID uuid;
    public static CoreDebugger debugger;
    public DatabaseIncreaseKills(String name, UUID uuid) {
        this.name = name;
        this.uuid = uuid;
    }
    @Override
    public void run() {
        MobStats.getInstance().getSQLHandler().increaseKillsAndStreak(name, uuid);
        debugger.i("kill addition IK sent!");
    }
}
