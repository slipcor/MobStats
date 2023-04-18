package net.slipcor.mobstats.impl;

import net.slipcor.mobstats.api.DatabaseConnection;
import net.slipcor.mobstats.classes.EntityStatistic;
import net.slipcor.mobstats.classes.NameHandler;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A partial implementation of methods that are handled the same by all SQL implementations
 */
public abstract class AbstractSQLConnection implements DatabaseConnection {

    // Database tables
    final String dbTable;
    final String dbKillTable;

    // The connection object
    Connection databaseConnection;

    boolean collectPrecise = false;

    AbstractSQLConnection(String dbTable, String dbKillTable) {
        this.dbTable = dbTable;
        this.dbKillTable = dbKillTable;
    }

    /**
     * Actually execute an SQL query
     *
     * @param query    the query to send to the SQL server.
     * @param modifies tf the Query modifies the database, set this to true, otherwise set this to false
     * @return If modifies is true, returns a valid ResultSet obtained from the query, otherwise returns null.
     * @throws SQLException if the query had an error or there was not a valid connection.
     */
    protected ResultSet executeQuery(final String query, final boolean modifies) throws SQLException {
        //System.out.println(query);
        Statement statement = this.databaseConnection.createStatement();
        if (modifies) {
            statement.execute(query);
            return null;
        } else {
            return statement.executeQuery(query);
        }
    }

    /*
     * ----------------------
     *  TABLE ENTRY CREATION
     * ----------------------
     */

    /**
     * Create the first statistic entry for a player
     *
     * @param playerName the player's name
     * @param uuid       the player's UUID
     * @param kills      the kill amount
     * @param deaths     the death amount
     */
    @Override
    public void addFirstStat(String playerName, UUID uuid, EntityType type, int kills, int deaths) {
        long time = System.currentTimeMillis() / 1000;
        try {
            executeQuery("INSERT INTO `" + dbTable +
                    "` (`name`, `uid`, `type`, `kills`, `deaths`, `streak`, `currentstreak`, `time`) VALUES ('"
                    + playerName + "', '" + uuid + "', '" + type.name() + "', " + kills + ", " + deaths + ", " +
                    kills + ", " + kills + ", " + time + ")", true);
        } catch (SQLException e) {
        }
    }

    /**
     * Add a kill to the player's count
     *
     * @param entityName the killer's name
     * @param uuid       the killer's uuid
     * @param victimName the victim's name
     * @param victimUUID the victim's uuid
     * @param world      the world in which the kill happened
     */
    @Override
    public void addKill(String entityName, String uuid, String victimName, String victimUUID, String world) {
        if (!collectPrecise) {
            return;
        }
        long time = System.currentTimeMillis() / 1000;
        try {
            executeQuery("INSERT INTO " + dbKillTable + " (`name`,`uid`,`victim`,`victimuid`,`time`,`world`) VALUES(" +
                    "'" + entityName + "', '" + uuid + "', '" + victimName + "', '" + victimUUID + "', " + time + ", '" + world +"')", true);
        } catch (SQLException e) {
        }
    }

    /**
     * Delete ALL kill stats
     */
    @Override
    public void deleteKills() {
        if (!collectPrecise) {
            return;
        }
        try {
            executeQuery("DELETE FROM `" + dbKillTable + "` WHERE 1;", true);
        } catch (SQLException e) {
        }
    }

    /**
     * Delete kill stats of a player
     *
     * @param uuid the player's UUID
     */
    @Override
    public void deleteKillsByUUID(UUID uuid) {
        if (!collectPrecise) {
            return;
        }
        try {
            executeQuery("DELETE FROM `" + dbKillTable + "` WHERE `uid` = '" + uuid
                    + "';", true);
        } catch (SQLException e) {
        }
    }

    /**
     * Delete kill stats older than a timestamp
     *
     * @param timestamp the timestamp to compare to
     * @throws SQLException
     */
    @Override
    public int deleteKillsOlderThan(long timestamp) throws SQLException {
        if (!collectPrecise) {
            return 0;
        }

        int count = 0;

        ResultSet result = executeQuery("SELECT `time` FROM `" + dbKillTable + "` WHERE `time` < " + timestamp + ";", false);
        while (result.next()) {
            count++;
        }
        executeQuery("DELETE FROM `" + dbKillTable + "` WHERE `time` < " + timestamp + ";", true);
        return count;
    }

    /**
     * Delete mob statistics older than a timestamp
     *
     * @param timestamp the timestamp to compare to
     * @throws SQLException
     */
    @Override
    public int deleteMobsOlderThan(long timestamp) throws SQLException {

        int count = 0;

        ResultSet result = executeQuery("SELECT `time` FROM `" + dbTable + "` WHERE `type` <> 'PLAYER' AND `time` < " + timestamp + ";", false);
        while (result.next()) {
            count++;
        }
        executeQuery("DELETE FROM `" + dbTable + "` WHERE `time` < " + timestamp + ";", true);
        return count;
    }

    /**
     * Delete all statistics
     */
    @Override
    public void deleteStats() {
        try {
            executeQuery("DELETE FROM `" + dbTable + "` WHERE 1;", true);
        } catch (SQLException e) {
        }
    }

    /**
     * Delete statistics by player UUID
     *
     * @param uuid the player's UUID
     */
    @Override
    public void deleteStatsByUUID(UUID uuid) {
        try {
            executeQuery("DELETE FROM `" + dbTable + "` WHERE `uid` = '" + uuid
                    + "';", true);
        } catch (SQLException e) {

        }
    }

    /**
     * Delete statistics older than a timestamp
     *
     * @param timestamp the timestamp to compare to
     * @throws SQLException
     */
    @Override
    public int deleteStatsOlderThan(long timestamp) throws SQLException {

        int count = 0;

        ResultSet result = executeQuery("SELECT `time` FROM `" + dbTable + "` WHERE `time` < " + timestamp + ";", false);
        while (result.next()) {
            count++;
        }
        executeQuery("DELETE FROM `" + dbTable + "` WHERE `time` < " + timestamp + ";", true);
        return count;
    }

    /**
     * Get all statistics
     *
     * @return a list of all stats
     * @throws SQLException
     */
    @Override
    public List<EntityStatistic> getAll() throws SQLException {
        String query = "SELECT `name`,`kills`,`deaths`,`streak`,`currentstreak`,`time`,`uid` FROM `" +
                dbTable + "` WHERE 1;";

        List<EntityStatistic> list = new ArrayList<>();

        ResultSet result = executeQuery(query, false);

        if (result == null) {
            return null;
        }

        while (result.next()) {
            list.add(new EntityStatistic(result.getString("name"),
                    result.getInt("kills"),
                    result.getInt("deaths"),
                    result.getInt("streak"),
                    result.getInt("currentstreak"),
                    result.getLong("time"),
                    UUID.fromString(result.getString("uid"))));
        }
        return list;
    }

    /**
     * Get a statistic value by player UUID
     *
     * @param stat       the statistic value
     * @param uuid the player's UUID to look for
     * @return a set of all matching entries
     * @throws SQLException
     */
    @Override
    public int getStats(String stat, UUID uuid) throws SQLException {
        ResultSet result = executeQuery("SELECT `" + stat + "` FROM `" + dbTable + "` WHERE `uid` = '" + uuid + "' LIMIT 1;", false);
        return (result != null && result.next()) ? result.getInt(stat) : -1;
    }

    /**
     * Get statistics by player UUID
     *
     * @param offlinePlayer the player to look for
     * @return the first matching player stat entry
     * @throws SQLException
     */
    @Override
    public EntityStatistic getStats(Entity offlinePlayer) throws SQLException {
        ResultSet result = executeQuery("SELECT `name`,`kills`,`deaths`,`streak`,`currentstreak`, `time`,`uid` FROM `" + dbTable + "` WHERE `uid` = '" + offlinePlayer.getUniqueId() + "' LIMIT 1;", false);
        if (result.next()) {
            return new EntityStatistic(result.getString("name"),
                    result.getInt("kills"),
                    result.getInt("deaths"),
                    result.getInt("streak"),
                    result.getInt("currentstreak"),
                    result.getInt("time"),
                    UUID.fromString(result.getString("uid")));
        }
        return new EntityStatistic(
                NameHandler.getName(offlinePlayer),
                0, 0, 0, 0, 0,
                offlinePlayer.getUniqueId());
    }

    /**
     * Get all player UUIDs
     *
     * @return all player UUIDs
     * @throws SQLException
     */
    @Override
    public List<UUID> getStatsUUIDs() throws SQLException {
        List<UUID> ids = new ArrayList<>();
        ResultSet result = executeQuery("SELECT `uid` FROM `" + dbTable + "` WHERE 1;", false);
        while (result.next()) {
            ids.add(UUID.fromString(result.getString("uid")));
        }
        return ids;
    }

    /**
     * Get the top players sorted by a given column
     *
     * @param amount    the amount to return
     * @param orderBy   the column to sort by
     * @param ascending true if ascending order, false otherwise
     * @return a list of all stats from the top players
     * @throws SQLException
     */
    @Override
    public List<EntityStatistic> getTopSorted(int amount, String orderBy, boolean ascending) throws SQLException {
        String query = "SELECT `name`,`kills`,`deaths`,`streak`,`currentstreak`,`time`,`uid` FROM `" +
                dbTable + "` WHERE 1 ORDER BY `" + orderBy + "` " + (ascending ? "ASC" : "DESC") + " LIMIT " + amount + ";";

        if (orderBy.contains("`")) {
            // special delivery, just throw all of this into the mix without escaping
            query = "SELECT `name`,`kills`,`deaths`,`streak`,`currentstreak`,`time`,`uid` FROM `" +
                    dbTable + "` WHERE 1 ORDER BY " + orderBy + " " + (ascending ? "ASC" : "DESC") + " LIMIT " + amount + ";";
        }

        List<EntityStatistic> list = new ArrayList<>();

        ResultSet result = executeQuery(query, false);

        if (result == null) {
            return null;
        }

        while (result.next()) {
            list.add(new EntityStatistic(result.getString("name"),
                    result.getInt("kills"),
                    result.getInt("deaths"),
                    result.getInt("streak"),
                    result.getInt("currentstreak"),
                    result.getInt("time"),
                    UUID.fromString(result.getString("uid"))));
        }
        return list;
    }

    /**
     * Check whether an entry matches a player UUID
     *
     * @param uuid the UUID to find
     * @return true if found, false otherwise
     */
    @Override
    public boolean hasEntry(UUID uuid) {
        try {
            ResultSet result = executeQuery("SELECT * FROM `" + dbTable + "` WHERE `uid` = '" + uuid + "';", false);
            return result != null && result.next();
        } catch (SQLException e) {
        }
        return false;
    }

    /**
     * Increase player death count, reset streak
     *
     * @param name the player's name
     * @param uuid the player's UUID
     */
    @Override
    public void increaseDeaths(String name, UUID uuid) {
        long time = System.currentTimeMillis() / 1000;
        try {
            executeQuery("UPDATE `" + dbTable + "` SET `name` = '" + name +
                    "', `deaths` = `deaths`+1, `currentstreak` = 0, `time` = " + time + " WHERE `uid` = '" + uuid + "'", true);
        } catch (SQLException e) {
        }
    }

    /**
     * Increase player kill count, update the max and current streak
     *
     * @param name the player's name
     * @param uuid the player's UUID
     */
    @Override
    public void increaseKillsAndMaxStreak(String name, UUID uuid) {
        long time = System.currentTimeMillis() / 1000;
        try {
            executeQuery("UPDATE `" + dbTable + "` SET `name` = '" + name +
                    "', `kills` = `kills`+1, `streak` = `streak`+1, `currentstreak` = `currentstreak`+1, `time` = " + time +
                    " WHERE `uid` = '" + uuid + "'", true);
        } catch (SQLException e) {
        }
    }

    /**
     * Increase player kill count, update the current streak
     *
     * @param name the player's name
     * @param uuid the player's UUID
     */
    @Override
    public void increaseKillsAndStreak(String name, UUID uuid) {
        long time = System.currentTimeMillis() / 1000;
        try {
            executeQuery("UPDATE `" + dbTable + "` SET `name` = '" + name +
                    "', `kills` = `kills`+1, `currentstreak` = `currentstreak`+1, `time` = " + time +
                    " WHERE `uid` = '" + uuid + "'", true);
        } catch (SQLException e) {
        }
    }

    @Override
    public void insert(EntityStatistic stat) throws SQLException {
        executeQuery("INSERT INTO `" + dbTable +
                "` (`name`, `uid`, `kills`,`deaths`,`streak`,`currentstreak`,`time`) VALUES ('"
                + stat.getName() + "', '" + stat.getUid() + "', " + stat.getKills() + ", " + stat.getDeaths() + ", " +
                stat.getMaxStreak() + ", " + stat.getCurrentStreak() + ", " + stat.getTime() + ")", true);
    }

    /**
     * @return whether the connection was established properly
     */
    @Override
    public boolean isConnected() {
        return this.databaseConnection != null;
    }


    /**
     * Set specific statistical value of a player
     *
     * @param uuid the player to find
     * @param entry      the entry to set
     * @param value      the value to set
     */
    @Override
    public void setSpecificStat(UUID uuid, String entry, int value) throws SQLException {
        executeQuery("UPDATE `" + dbTable + "` SET `" + entry + "` = " + value + " WHERE `uid` = '" + uuid.toString() + "';", true);
    }
}
