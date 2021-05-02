package net.slipcor.mobstats.runnables;

import net.slipcor.core.CoreDebugger;
import net.slipcor.mobstats.MobStats;

import java.util.UUID;

public class DatabaseIncreaseKillsStreak implements Runnable {
    private final String name;
    private final UUID uuid;

    public static CoreDebugger debugger;
    public DatabaseIncreaseKillsStreak(String name, UUID uuid) {
        this.name = name;
        this.uuid = uuid;
    }
    @Override
    public void run() {
        MobStats.getInstance().getSQLHandler().increaseKillsAndMaxStreak(name, uuid);
        debugger.i("kill addition KS sent!");
    }
}
