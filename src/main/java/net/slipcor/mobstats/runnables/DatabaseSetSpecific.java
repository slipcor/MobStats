package net.slipcor.mobstats.runnables;

import net.slipcor.mobstats.MobStats;

import java.sql.SQLException;
import java.util.UUID;

public class DatabaseSetSpecific implements Runnable {
    private final UUID uuid;
    private final String column;
    private final int value;

    public DatabaseSetSpecific(UUID uuid, String column, int value) {
        this.uuid = uuid;
        this.column = column;
        this.value = value;
    }

    @Override
    public void run() {
        try {
            MobStats.getInstance().getSQLHandler().setSpecificStat(uuid, column, value);
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }
}
