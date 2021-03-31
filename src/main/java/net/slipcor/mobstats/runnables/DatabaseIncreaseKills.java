package net.slipcor.mobstats.runnables;

import net.slipcor.mobstats.MobStats;
import net.slipcor.mobstats.classes.Debugger;

import java.util.UUID;

public class DatabaseIncreaseKills implements Runnable {
    private final String name;
    private final UUID uuid;
    static Debugger debugger = new Debugger(17);
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
