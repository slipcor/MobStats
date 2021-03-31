package net.slipcor.mobstats.runnables;

import net.slipcor.mobstats.MobStats;
import net.slipcor.mobstats.classes.NameHandler;
import org.bukkit.entity.Entity;

public class DatabaseFirstEntry implements Runnable {
    private final Entity entity;
    public DatabaseFirstEntry(Entity entity) {
        this.entity = entity;
    }
    @Override
    public void run() {
        MobStats.getInstance().getSQLHandler().addFirstStat(
                NameHandler.getName(entity), entity.getUniqueId(), entity.getType(), 0, 0);
    }
}
