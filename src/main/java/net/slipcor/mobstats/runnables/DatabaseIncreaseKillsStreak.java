package net.slipcor.mobstats.runnables;

import net.slipcor.mobstats.MobStats;
import net.slipcor.mobstats.classes.Debugger;

import java.util.UUID;

public class DatabaseIncreaseKillsStreak implements Runnable {
    private final String name;
    private final UUID uuid;

    static Debugger debugger = new Debugger(15);
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
