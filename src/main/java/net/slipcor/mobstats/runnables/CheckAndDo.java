package net.slipcor.mobstats.runnables;

import net.slipcor.mobstats.MobStats;
import net.slipcor.mobstats.api.EntityStatisticsBuffer;
import net.slipcor.mobstats.classes.Debugger;
import net.slipcor.mobstats.classes.NameHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.UUID;

public class CheckAndDo implements Runnable {

    private final Entity entity;
    private final boolean kill;
    private final boolean addMaxStreak;

    private static final Debugger DEBUGGER = new Debugger(20);
    private final MobStats plugin = MobStats.getInstance();

    public CheckAndDo(final Entity entity, final boolean kill, final boolean addMaxStreak) {
        this.entity = entity;
        this.kill = kill;
        this.addMaxStreak = addMaxStreak;
    }

    @Override
    public void run() {

        DEBUGGER.i("checkAndDo running in thread: " + Thread.currentThread().getName());
        DEBUGGER.i("checkAndDo isMainThread: " + (Bukkit.isPrimaryThread() ? "YES" : "NO"));

        if (!plugin.getSQLHandler().hasEntry(entity.getUniqueId())) {

            DEBUGGER.i("Entity has no entry yet, adding!");

            final int kills = kill ? 1 : 0;
            final int deaths = kill ? 0 : 1;

            plugin.getSQLHandler().addFirstStat(NameHandler.getName(entity), entity.getUniqueId(), entity.getType(), kills, deaths);

            EntityStatisticsBuffer.setKills(entity.getUniqueId(), kills);
            EntityStatisticsBuffer.setDeaths(entity.getUniqueId(), deaths);
            return;
        }

        if (addMaxStreak && kill) {
            DEBUGGER.i("increasing kills and max streak");
            (new DatabaseIncreaseKillsStreak(NameHandler.getName(entity), entity.getUniqueId())).run();
        } else if (kill) {
            DEBUGGER.i("increasing kills and current streak");
            (new DatabaseIncreaseKills(NameHandler.getName(entity), entity.getUniqueId())).run();
        } else {
            DEBUGGER.i("increasing deaths");
            (new DatabaseIncreaseDeaths(NameHandler.getName(entity), entity.getUniqueId())).run();
        }

        if (kill) {
            EntityStatisticsBuffer.addKill(entity.getUniqueId());
        } else {
            EntityStatisticsBuffer.addDeath(entity.getUniqueId());
        }
    }
}
