package net.slipcor.mobstats.runnables;

import net.slipcor.mobstats.MobStats;
import net.slipcor.mobstats.classes.Debugger;

public class DatabaseKillAddition implements Runnable {
    private final String attackerName;
    private final String attackerUUID;
    private final String victimName;
    private final String victimUUID;
    private final String world;

    static Debugger debugger = new Debugger(14);

    public DatabaseKillAddition(String attackerName, String attackerUUID, String victimName, String victimUUID, String world) {
        this.attackerName = attackerName;
        this.attackerUUID = attackerUUID;
        this.victimName = victimName;
        this.victimUUID = victimUUID;
        this.world = world;
    }

    @Override
    public void run() {
        MobStats.getInstance().getSQLHandler().addKill(attackerName, attackerUUID, victimName, victimUUID, world);
        debugger.i("kill addition KA sent!");
    }
}
