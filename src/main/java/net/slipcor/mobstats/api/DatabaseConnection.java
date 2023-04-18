package net.slipcor.mobstats.api;

import net.slipcor.mobstats.classes.EntityStatistic;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/**
 * Database connection interface, defines all necessary methods to handle the database
 *
 * @author slipcor
 */
public interface DatabaseConnection {

    boolean allowsAsync();

    /**
     * Try to connect to the database
     *
     * @param printError should we print errors that we encounter?
     * @return true if the connection was made successfully, false otherwise.
     */
    boolean connect(boolean printError);

    /**
     * Check whether a table exists
     *
     * @param database The database to check for the table in.
     * @param table    The table to check for existence.
     * @return true if the table exists, false if there was an error or the database doesn't exist.
     * <p/>
     * This method looks through the information schema that comes with a MySQL installation and checks
     * if a certain table exists within a database.
     */
    boolean tableExists(String database, String table);

    /*
     * ----------------------
     *  TABLE ENTRY CREATION
     * ----------------------
     */

    /**
     * Create the first statistic entry for an Entity
     *
     * @param entityName the Entity's name
     * @param uuid       the Entity's UUID
     * @param type       the Entity's type
     * @param kills      the kill amount
     * @param deaths     the death amount
     */
    void addFirstStat(String entityName, UUID uuid, EntityType type, int kills, int deaths);

    /**
     * Add a kill to the Entity's count
     *
     * @param entityName the killer's name
     * @param uuid       the killer's uuid
     * @param victimName the victim's name
     * @param victimUUID the victim's uuid
     * @param world      the world name where the kill happened
     */
    void addKill(String entityName, String uuid, String victimName, String victimUUID, String world);

    /**
     * Create the kill stat table
     *
     * @param printError should we print errors that we encounter?
     */
    void createStatsTable(boolean printError);

    /**
     * Create the statistics table
     *
     * @param printError should we print errors that we encounter?
     */
    void createKillStatsTable(boolean printError);

    /**
     * Delete ALL kill stats
     */
    void deleteKills();

    /**
     * Delete kill stats of an Entity
     *
     * @param uuid the Entity id
     */
    void deleteKillsByUUID(UUID uuid);

    /**
     * Delete kill stats older than a timestamp
     *
     * @param timestamp the timestamp to compare to
     * @throws SQLException
     */
    int deleteKillsOlderThan(long timestamp) throws SQLException;

    /**
     * Delete all statistics
     */
    void deleteStats();

    /**
     * Delete statistics by Entity id
     *
     * @param uuid the Entity's id
     */
    void deleteStatsByUUID(UUID uuid);

    /**
     * Delete statistics older than a timestamp
     *
     * @param timestamp the timestamp to compare to
     * @throws SQLException
     */
    int deleteStatsOlderThan(long timestamp) throws SQLException;

    /**
     * Delete mob statistics older than a timestamp
     *
     * @param timestamp the timestamp to compare to
     * @throws SQLException
     */
    int deleteMobsOlderThan(long timestamp) throws SQLException;

    /**
     * Get all statistics
     *
     * @return a list of all stats
     * @throws SQLException
     */
    List<EntityStatistic> getAll() throws SQLException;

    /**
     * Get a statistic value by exact Entity name
     *
     * @param stat       the statistic value
     * @param uuid the exact Entity's name to look for
     * @return a set of all matching entries
     * @throws SQLException
     */
    int getStats(String stat, UUID uuid) throws SQLException;

    /**
     * Get statistics by Entity
     *
     * @param entity the Entity to look for
     * @return the first matching Entity stat entry
     * @throws SQLException
     */
    EntityStatistic getStats(Entity entity) throws SQLException;

    /**
     * Get all Entity UUIDs
     *
     * @return all Entity UUIDs
     * @throws SQLException
     */
    List<UUID> getStatsUUIDs() throws SQLException;

    /**
     * Get the top players sorted by a given column
     *
     * @param amount    the amount to return
     * @param orderBy   the column to sort by
     * @param days      the amount of days to query
     * @return a list of all stats from the top players
     * @throws SQLException
     */
    public List<EntityStatistic> getTopPlusSorted(int amount, String orderBy, int days) throws SQLException;

    /**
     * Get a world's top players sorted by a given column
     *
     * @param amount    the amount to return
     * @param orderBy   the column to sort by
     * @param world     the world to filter by
     * @param days      the amount of days to query
     * @return a list of all stats from the top players
     * @throws SQLException
     */
    public List<EntityStatistic> getTopWorldSorted(int amount, String orderBy, String world, int days) throws SQLException;

    /**
     * Get the top Entities sorted by a given column
     *
     * @param amount    the amount to return
     * @param orderBy   the column to sort by
     * @param ascending true if ascending order, false otherwise
     * @return a list of all stats from the top Entities
     * @throws SQLException
     */
    List<EntityStatistic> getTopSorted(int amount, String orderBy, boolean ascending) throws SQLException;

    /**
     * Check whether an entry matches an Entity UUID
     *
     * @param uuid the UUID to find
     * @return true if found, false otherwise
     */
    boolean hasEntry(UUID uuid);

    /**
     * Increase Entity death count and reset streak
     *
     * @param name the Entity's name, in case it changed
     * @param uuid the Entity's UUID
     */
    void increaseDeaths(String name, UUID uuid);

    /**
     * Increase Entity kill count, update the max and current streak
     *
     * @param name the Entity's name, in case it changed
     * @param uuid the Entity's UUID
     */
    void increaseKillsAndMaxStreak(String name, UUID uuid);

    /**
     * Increase Entity kill count, update the current streak
     *
     * @param name the Entity's name, in case it changed
     * @param uuid the Entity's UUID
     */
    void increaseKillsAndStreak(String name, UUID uuid);

    /**
     * Add Entity statistic to the database
     *
     * @param stat the Entity's stats
     */
    void insert(EntityStatistic stat) throws SQLException;

    /**
     * @return whether the connection was established properly
     */
    boolean isConnected();

    /**
     * Set specific statistical value of an Entity
     *
     * @param uuid the Entity id to find
     * @param entry      the entry to set
     * @param value      the value to set
     */
    void setSpecificStat(UUID uuid, String entry, int value) throws SQLException;
}
