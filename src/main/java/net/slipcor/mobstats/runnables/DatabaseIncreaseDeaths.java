package net.slipcor.mobstats.runnables;

import net.slipcor.mobstats.MobStats;
import net.slipcor.mobstats.classes.Debugger;

import java.util.UUID;

public class DatabaseIncreaseDeaths implements Runnable {
    private final String name;
    private final UUID uuid;
    static Debugger debugger = new Debugger(19);
    public DatabaseIncreaseDeaths(String name, UUID uuid) {
        this.name = name;
        this.uuid = uuid;
    }
    @Override
    public void run() {
        MobStats.getInstance().getSQLHandler().increaseDeaths(name, uuid);
        debugger.i("death addition sent!");
    }
}
