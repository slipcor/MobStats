package net.slipcor.mobstats.api;

import net.slipcor.core.CoreDebugger;
import net.slipcor.mobstats.MobStats;
import net.slipcor.mobstats.classes.EntityStatistic;
import net.slipcor.mobstats.classes.NameHandler;
import net.slipcor.mobstats.display.SignDisplay;
import net.slipcor.mobstats.impl.FlatFileConnection;
import net.slipcor.mobstats.impl.MySQLConnection;
import net.slipcor.mobstats.impl.SQLiteConnection;
import net.slipcor.mobstats.math.Formula;
import net.slipcor.mobstats.math.MathFormulaManager;
import net.slipcor.mobstats.runnables.CheckAndDo;
import net.slipcor.mobstats.runnables.DatabaseFirstEntry;
import net.slipcor.mobstats.runnables.DatabaseKillAddition;
import net.slipcor.mobstats.runnables.DatabaseSetSpecific;
import net.slipcor.mobstats.text.TextComponent;
import net.slipcor.mobstats.text.TextFormatter;
import net.slipcor.mobstats.yml.Config;
import net.slipcor.mobstats.yml.Language;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Database access class to handle Entity statistics, possibly from other plugins
 */

public final class DatabaseAPI {

    private DatabaseAPI() {
    }

    private static MobStats plugin = null;

    public static CoreDebugger DEBUGGER;

    private static final TextComponent DATABASE_CONNECTED = new TextComponent("Warning: Database is not connected! Kills will not be recorded.");

    private static Formula formula;

    private static Map<String, String> lastKill = new HashMap<>();

    /**
     * Entity A killed Entity B - use this to generally emulate a kill.
     *
     * There will be checks for newbie status, whether one entity is a Player
     *
     * @param attacker the killing Entity
     * @param victim   the killed Entity
     */
    public static void AkilledB(Entity attacker, Entity victim) {

        MobStatsEntityKillEvent event = new MobStatsEntityKillEvent(attacker, victim);

        Bukkit.getServer().getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            DEBUGGER.i("Another plugin prevented the kill!");
            return;
        }

        if (attacker instanceof Player && victim instanceof Player) {
            DEBUGGER.i("both entities are a Player");
            return;
        }

        if (!plugin.config().getBoolean(Config.Entry.STATISTICS_COUNT_MOB_VS_MOB) &&
                (!(attacker instanceof Player) && !(victim instanceof Player))) {
            DEBUGGER.i("neither is a player");
            return;
        }

        if (victim == null) {
            DEBUGGER.i("victim is null", attacker.getName());
            if (plugin.config().getBoolean(Config.Entry.STATISTICS_CHECK_NEWBIES) && isNewbie(attacker)) {
                DEBUGGER.i("killer has newbie status", attacker.getName());
                TextFormatter.explainNewbieStatus(attacker, null);
                return;
            }
            incKill(attacker);

            if (plugin.getSQLHandler().allowsAsync()) {
                Bukkit.getScheduler().runTaskAsynchronously(MobStats.getInstance(), new DatabaseKillAddition(
                        NameHandler.getName(attacker), attacker.getUniqueId().toString(),
                        "", "",
                        attacker.getWorld().getName()));
            } else {
                Bukkit.getScheduler().runTask(MobStats.getInstance(), new DatabaseKillAddition(
                        NameHandler.getName(attacker), attacker.getUniqueId().toString(),
                        "", "",
                        attacker.getWorld().getName()));
            }

            SignDisplay.updateAll();
            return;
        }

        if (attacker == null) {
            DEBUGGER.i("attacker is null", victim.getName());
            if (plugin.config().getBoolean(Config.Entry.STATISTICS_CHECK_NEWBIES) && isNewbie(victim)) {
                DEBUGGER.i("victim has newbie status", victim.getName());
                TextFormatter.explainNewbieStatus(null, victim);
                return;
            }

            final int streak = EntityStatisticsBuffer.getStreak(victim.getUniqueId());

            final int threshold = plugin.config().getInt(Config.Entry.STATISTICS_STREAK_BROKEN_THRESHOLD);

            if ((threshold == 0 && streak > 0) || (threshold > 0 && streak >= threshold)) {
                Bukkit.getScheduler().runTaskLaterAsynchronously(
                        MobStats.getInstance(), new Runnable() {
                            @Override
                            public void run() {
                                if (victim instanceof Player) {
                                    plugin.sendPrefixed((Player) victim,
                                            Language.MSG.PLAYER_KILLSTREAK_ENDED.parse(String.valueOf(streak)));
                                    String playerName =  NameHandler.getName(victim);
                                    for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                                        plugin.sendPrefixed(p, Language.MSG.PLAYER_KILLSTREAK_ENDED_GLOBAL.parse(playerName, String.valueOf(streak)));
                                    }
                                }
                            }
                        }, 1L
                );
            }

            incDeath(victim);

            if (plugin.getSQLHandler().allowsAsync()) {
                Bukkit.getScheduler().runTaskAsynchronously(MobStats.getInstance(), new DatabaseKillAddition(
                        "", "",
                        NameHandler.getName(victim), victim.getUniqueId().toString(),
                        victim.getWorld().getName()));
            } else {
                Bukkit.getScheduler().runTask(MobStats.getInstance(), new DatabaseKillAddition(
                        "", "",
                        NameHandler.getName(victim), victim.getUniqueId().toString(),
                        victim.getWorld().getName()));
            }

            SignDisplay.updateAll();
            return;
        }

        if (plugin.config().getBoolean(Config.Entry.STATISTICS_CHECK_NEWBIES) &&
                (isNewbie(attacker) || isNewbie(victim))) {

            DEBUGGER.i("either one has newbie status", victim.getName());
            TextFormatter.explainNewbieStatus(attacker, victim);
            return;
        }
        // here we go, PVE!

        final int streak = EntityStatisticsBuffer.getStreak(victim.getUniqueId());

        final int threshold = plugin.config().getInt(Config.Entry.STATISTICS_STREAK_BROKEN_THRESHOLD);

        if ((threshold == 0 && streak > 0) || (threshold > 0 && streak >= threshold)) {
            Bukkit.getScheduler().runTaskLaterAsynchronously(
                    MobStats.getInstance(), new Runnable() {
                        @Override
                        public void run() {
                            if (victim instanceof Player) {
                                plugin.sendPrefixed((Player) victim,
                                        Language.MSG.PLAYER_KILLSTREAK_ENDED.parse(String.valueOf(streak)));
                                String playerName =  NameHandler.getName(victim);
                                for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                                    plugin.sendPrefixed(p, Language.MSG.PLAYER_KILLSTREAK_ENDED_GLOBAL.parse(playerName, String.valueOf(streak)));
                                }
                            }
                        }
                    }, 1L
            );
        }

        DEBUGGER.i("Counting kill by " + attacker.getName(), victim);
        lastKill.put(attacker.getName(), NameHandler.getName(victim));

        incKill(attacker);
        incDeath(victim);

        if (plugin.getSQLHandler().allowsAsync()) {
            Bukkit.getScheduler().runTaskAsynchronously(MobStats.getInstance(), new DatabaseKillAddition(
                    NameHandler.getName(attacker), attacker.getUniqueId().toString(),
                    NameHandler.getName(victim), victim.getUniqueId().toString(),
                    victim.getWorld().getName()));
        } else {
            Bukkit.getScheduler().runTask(MobStats.getInstance(), new DatabaseKillAddition(
                    NameHandler.getName(attacker), attacker.getUniqueId().toString(),
                    NameHandler.getName(victim), victim.getUniqueId().toString(),
                    victim.getWorld().getName()));
        }

        SignDisplay.updateAll();
    }

    private static List<UUID> allUUIDs;

    /**
     * @return a list of all UUIDs of Entitys that have statistic entries
     */
    public static List<UUID> getAllUUIDs() {
        if (!plugin.getSQLHandler().isConnected()) {
            plugin.getLogger().severe("Database is not connected!");
            plugin.sendPrefixedOP(new ArrayList<>(), DATABASE_CONNECTED);
            return null;
        }

        if (allUUIDs == null) {
            List<UUID> output = new ArrayList<>();
            try {
                List<UUID> result = plugin.getSQLHandler().getStatsUUIDs();
                output.addAll(result);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            allUUIDs = output;
        }

        return allUUIDs;
    }

    private static List<String> allPlayerNames;

    /**
     * Return an Entity's statistic
     * @param entity the Entity to find
     * @return the Entity's statistic
     */
    public static EntityStatistic getAllStats(Entity entity) {
        if (!plugin.getSQLHandler().isConnected()) {
            plugin.getLogger().severe("Database is not connected!");
            plugin.sendPrefixedOP(new ArrayList<>(), DATABASE_CONNECTED);
            return new EntityStatistic(NameHandler.getName(entity),
                    0, 0, 0, 0, 0, entity.getUniqueId());
        }

        try {
            return plugin.getSQLHandler().getStats(entity);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new EntityStatistic(NameHandler.getName(entity),
                0, 0, 0, 0, 0, entity.getUniqueId());
    }

    /**
     * Get an Entity's statistic entry
     *
     * @param uuid the Entity id to find
     * @param entry      the entry to find
     * @return the entry value, 0 if not found, throwing an Exception if there was a bigger problem
     */
    public static Integer getEntry(UUID uuid, String entry) {
        if (entry == null) {
            throw new IllegalArgumentException("entry can not be null!");
        }

        if (!entry.equals("kills") &&
                !entry.equals("deaths") &&
                !entry.equals("streak") &&
                !entry.equals("currentstreak")) {
            throw new IllegalArgumentException("entry can not be '" + entry + "'. Valid values: kills, deaths, streak, currentstreak");
        }

        if (!plugin.getSQLHandler().isConnected()) {
            plugin.getLogger().severe("Database is not connected!");
            plugin.sendPrefixedOP(new ArrayList<>(), DATABASE_CONNECTED);
            return null;
        }
        int result = -1;
        try {
            result = plugin.getSQLHandler().getStats(entry, uuid);
            if (result < 0) {
                return 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result < 0 ? 0 : result;
    }

    public static String getLastKilled(String killer) {
        return lastKill.get(killer);
    }

    /**
     * Check whether an Entity has a statistic entry
     *
     * YML will return true always as this is only about deciding between INSERT and UPDATE query
     *
     * @param uuid the Entity id to find
     * @return true if an entry was found
     */
    public static boolean hasEntry(UUID uuid) {
        int result = -1;
        try {
            result = plugin.getSQLHandler().getStats("kills", uuid);
        } catch (SQLException e) {
        }
        return result > -1;
    }

    /**
     * Get an Entity's stats in the form of a string array
     *
     * @param entity the Entity to find
     * @return the Entity info in lines as overridable in the config
     */
    public static String[] info(final Entity entity) {
        if (!plugin.getSQLHandler().isConnected()) {
            plugin.getLogger().severe("Database is not connected!");
            plugin.sendPrefixedOP(new ArrayList<>(), DATABASE_CONNECTED);
            return null;
        }

        DEBUGGER.i("getting info for " + entity.getName());
        EntityStatistic result = null;
        try {
            result = plugin.getSQLHandler().getStats(entity);
            if (result == null) {
                String[] output = new String[1];
                output[0] = Language.MSG.COMMAND_PLAYER_NOT_FOUND.parse(NameHandler.getName(entity));
                return output;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (result == null) {
            String[] output = new String[2];
            output[0] = Language.MSG.COMMAND_PLAYER_NOT_FOUND.parse(NameHandler.getName(entity));
            output[1] = Language.MSG.COMMAND_PLAYER_NOT_FOUND_EXPLANATION.toString();
            return output;
        }
        String[] output;

        String name = result.getName();

        int kills = result.getKills();
        int deaths = result.getDeaths();
        int streak = result.getCurrentStreak();
        int maxStreak = result.getMaxStreak();
        Double ratio = calculateRatio(result);
        DecimalFormat df = new DecimalFormat("#.##");

        if (plugin.config().getBoolean(Config.Entry.MESSAGES_OVERRIDES)) {
            List<String> lines = plugin.config().getStringList(Config.Entry.MESSAGES_OVERRIDE_LIST, new ArrayList<>());
            output = new String[lines.size()];

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);

                line = line.replace("%d", String.valueOf(deaths));
                line = line.replace("%k", String.valueOf(kills));
                line = line.replace("%m", String.valueOf(maxStreak));
                line = line.replace("%n", name);
                line = line.replace("%r", df.format(ratio));
                line = line.replace("%s", String.valueOf(streak));

                output[i] = Language.colorize(line);
            }

            return output;
        }


        output = new String[6];

        output[0] = Language.MSG.STATISTIC_FORMAT_VALUE.parse(
                Language.MSG.STATISTIC_VALUE_NAME.toString(),
                name);
        output[1] = Language.MSG.STATISTIC_FORMAT_VALUE.parse(
                Language.MSG.STATISTIC_VALUE_KILLS.toString(),
                String.valueOf(kills));
        output[2] = Language.MSG.STATISTIC_FORMAT_VALUE.parse(
                Language.MSG.STATISTIC_VALUE_DEATHS.toString(),
                String.valueOf(deaths));
        output[3] = Language.MSG.STATISTIC_FORMAT_VALUE.parse(
                Language.MSG.STATISTIC_VALUE_RATIO.toString(),
                df.format(ratio));
        output[4] = Language.MSG.STATISTIC_FORMAT_VALUE.parse(
                Language.MSG.STATISTIC_VALUE_STREAK.toString(),
                String.valueOf(streak));
        output[5] = Language.MSG.STATISTIC_FORMAT_VALUE.parse(
                Language.MSG.STATISTIC_VALUE_MAX_STREAK.toString(),
                String.valueOf(maxStreak));
        return output;
    }

    /**
     * Initiate the access class
     *
     * @param plugin the plugin instance to use
     */
    public static void initiate(final MobStats plugin) {
        DatabaseAPI.plugin = plugin;
    }

    /**
     * Initiate an Entity by actually reading the database
     *
     * @param entity the Entity to initiate
     */
    public static void initiateEntity(Entity entity) {
        if (getAllUUIDs().contains(entity.getUniqueId())) {
            // an entry exists!
        } else if (plugin.config().getBoolean(Config.Entry.STATISTICS_CREATE_ON_JOIN)) {
            if (plugin.getSQLHandler().allowsAsync()) {
                Bukkit.getScheduler().runTaskAsynchronously(MobStats.getInstance(), new DatabaseFirstEntry(entity));
            } else {
                Bukkit.getScheduler().runTask(MobStats.getInstance(), new DatabaseFirstEntry(entity));
            }
            allUUIDs.add(entity.getUniqueId());
        } else {
            allUUIDs.add(entity.getUniqueId());
        }

        // read all the data from database
        EntityStatisticsBuffer.loadEntity(entity);
    }

    private static DatabaseConnection connectToOther(String method, CommandSender sender) {

        DatabaseConnection dbHandler = null;

        String dbHost = null;
        String dbUser = null;
        String dbPass = null;
        String dbDatabase = null;
        String dbTable = null;
        String dbOptions = null;
        String dbKillTable = null;
        int dbPort = 0;
        
        Config config = MobStats.getInstance().config();

        if (method.equals("yml")) {
            if (MobStats.getInstance().getSQLHandler() instanceof FlatFileConnection) {
                MobStats.getInstance().sendPrefixed(sender, Language.MSG.COMMAND_MIGRATE_DATABASE_METHOD_INVALID.toString());
                return null;
            }

            dbTable = config.getString(Config.Entry.YML_TABLE);
            if (config.getBoolean(Config.Entry.STATISTICS_COLLECT_PRECISE) &&
                config.getBoolean(Config.Entry.YML_COLLECT_PRECISE)) {
                dbKillTable = config.getString(Config.Entry.MYSQL_KILLTABLE);
            }

            dbHandler = new FlatFileConnection(dbTable, dbKillTable);
        } else if (method.equals("sqlite")) {
            if (MobStats.getInstance().getSQLHandler() instanceof SQLiteConnection) {
                MobStats.getInstance().sendPrefixed(sender, Language.MSG.COMMAND_MIGRATE_DATABASE_METHOD_INVALID.toString());
                return null;
            }

            dbDatabase = config.getString(Config.Entry.SQLITE_FILENAME);

            dbTable = config.getString(Config.Entry.SQLITE_TABLE);
            if (config.getBoolean(Config.Entry.STATISTICS_COLLECT_PRECISE)) {
                dbKillTable = config.getString(Config.Entry.SQLITE_KILLTABLE);
            }

            dbHandler = new SQLiteConnection(dbDatabase, dbTable, dbKillTable);
        } else if (method.equals("mysql")) {
            if (MobStats.getInstance().getSQLHandler() instanceof MySQLConnection) {
                MobStats.getInstance().sendPrefixed(sender, Language.MSG.COMMAND_MIGRATE_DATABASE_METHOD_INVALID.toString());
                return null;
            }

            dbHost = config.getString(Config.Entry.MYSQL_HOST);
            dbUser = config.getString(Config.Entry.MYSQL_USERNAME);
            dbPass = config.getString(Config.Entry.MYSQL_PASSWORD);
            dbDatabase = config.getString(Config.Entry.MYSQL_DATABASE);
            dbTable = config.getString(Config.Entry.MYSQL_TABLE);
            dbOptions = config.getString(Config.Entry.MYSQL_OPTIONS);

            if (config.getBoolean(Config.Entry.STATISTICS_COLLECT_PRECISE)) {
                dbKillTable = config.getString(Config.Entry.MYSQL_KILLTABLE);
            }

            dbPort = config.getInt(Config.Entry.MYSQL_PORT);

            try {
                dbHandler = new MySQLConnection(dbHost, dbPort, dbDatabase, dbUser,
                        dbPass, dbOptions, dbTable, dbKillTable);
            } catch (InstantiationException | ClassNotFoundException | IllegalAccessException e1) {
                e1.printStackTrace();
            }
        } else {
            return null;
        }


        if (dbHandler != null && dbHandler.connect(true)) {
            MobStats.getInstance().sendPrefixed(sender, "Database connection successful");
            // Check if the tables exist, if not, create them
            if (!dbHandler.tableExists(dbDatabase, dbTable)) {
                // normal table doesnt exist, create both

                MobStats.getInstance().sendPrefixed(sender, "Creating table " + dbTable);
                dbHandler.createStatsTable(true);

                if (dbKillTable != null) {
                    MobStats.getInstance().sendPrefixed(sender, "Creating table " + dbKillTable);
                    dbHandler.createKillStatsTable(true);
                }
            }
        } else {
            MobStats.getInstance().sendPrefixed(sender, "Database connection failed");
        }

        return dbHandler;
    }

    /**
     * Read the current statistics from another database implementation - this does NOT clear an existing database!
     *
     * @param method the other database method
     * @return the entries migrated
     */
    public static int migrateFrom(String method, CommandSender sender) {
        // database handler
        DatabaseConnection dbHandler = connectToOther(method, sender);
        if (dbHandler == null) {
            return -1;
        }

        try {
            List<EntityStatistic> entities = dbHandler.getAll();
            for (EntityStatistic stat : entities) {
                MobStats.getInstance().getSQLHandler().insert(stat);
            }
            return entities.size();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * Save the current statistics to another database implementation - this does NOT clear an existing database!
     * 
     * @param method the other database method
     * @return the entries migrated
     */
    public static int migrateTo(String method, CommandSender sender) {
        // database handler
        DatabaseConnection dbHandler = connectToOther(method, sender);
        if (dbHandler == null) {
            return -1;
        }

        try {
            List<EntityStatistic> entities = MobStats.getInstance().getSQLHandler().getAll();
            for(EntityStatistic stat : entities) {
                dbHandler.insert(stat);
            }
            return entities.size();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    private static boolean isNewbie(Entity entity) {
        // get the Player object, or null if they are not a player or offline
        Player p = (entity instanceof Player) ? ((Player) entity) : null;

        // if the player is offline, we assume they are not newbies (and log)
        if (p == null) {
            DEBUGGER.i("Player is offline, we assume they are not newbie...");
            return false;
        }

        // otherwise continue with our permission checks:
        // backwards compatibility
        boolean newbie = p.hasPermission("mobstats.newbie");

        if (p.hasPermission("mobstats.null")) {
            DEBUGGER.i("Player has ALL permissions, we assume they are not newbie...");
            /*
             * If a player does have the previous permission, we can assume that the permission
             * plugin either does always reply with TRUE or has ALL PERMS set to true, which means
             * they probably want to consider getting all access.
             *
             * This is a solution until a warning system is in place to ask admins to set it up
             * properly.
             */
            return false;
        }


        if (newbie) {
            DEBUGGER.i("Player has 'newbie'...");
            // backwards compatibility until we have a warning system in place to ask admins to change to
            // proper permission logic
            return true;
        }

        return !p.hasPermission("mobstats.nonewbie");
    }

    /**
     * Purge the kill statistics older than a certain amount of days
     *
     * @param days the amount
     * @return the amount of removed entries
     */
    public static int purgeKillStats(int days) {
        if (!plugin.getSQLHandler().isConnected()) {
            plugin.getLogger().severe("Database is not connected!");
            plugin.sendPrefixedOP(new ArrayList<>(), DATABASE_CONNECTED);
            return 0;
        }

        int count = 0;

        long timestamp = System.currentTimeMillis() / 1000 - ((long) days * 24L * 60L * 60L);

        try {
            count = plugin.getSQLHandler().deleteKillsOlderThan(timestamp);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return count;
    }

    /**
     * Purge Mob statistics older than a certain amount of days
     *
     * @param days the amount
     * @return the amount of removed entries
     */
    public static int purgeMobStats(int days) {
        if (!plugin.getSQLHandler().isConnected()) {
            plugin.getLogger().severe("Database is not connected!");
            plugin.sendPrefixedOP(new ArrayList<>(), DATABASE_CONNECTED);
            return 0;
        }

        int count = 0;

        long timestamp = System.currentTimeMillis() / 1000 - ((long) days * 24L * 60L * 60L);

        try {
            count = plugin.getSQLHandler().deleteMobsOlderThan(timestamp);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return count;
    }

    /**
     * Purge the general statistics older than a certain amount of days
     *
     * @param days the amount
     * @return the amount of removed entries
     */
    public static int purgeStats(int days) {
        if (!plugin.getSQLHandler().isConnected()) {
            plugin.getLogger().severe("Database is not connected!");
            plugin.sendPrefixedOP(new ArrayList<>(), DATABASE_CONNECTED);
            return 0;
        }
        int count = 0;

        long timestamp = System.currentTimeMillis() / 1000 - ((long) days * 24L * 60L * 60L);

        try {
            count = plugin.getSQLHandler().deleteStatsOlderThan(timestamp);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return count;
    }

    /**
     * Set an Entity's statistic value
     *
     * @param uuid the Entity id to update
     * @param entry      the entry to update
     * @param value      the value to set
     * @throws SQLException
     */
    public static void setSpecificStat(UUID uuid, String entry, int value) {
        if (!entry.equals("kills") &&
                !entry.equals("deaths") &&
                !entry.equals("streak") &&
                !entry.equals("currentstreak")) {
            throw new IllegalArgumentException("entry can not be '" + entry + "'. Valid values: kills, deaths, streak, currentstreak");
        }
        if (MobStats.getInstance().getSQLHandler().allowsAsync()) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, new DatabaseSetSpecific(uuid, entry, value));
        } else {
            Bukkit.getScheduler().runTask(plugin, new DatabaseSetSpecific(uuid, entry, value));
        }

    }

    /**
     * Get the top statistics sorted by type
     *
     * @param limit the amount to fetch
     * @param sort  the type to sort by
     * @return a sorted array
     */
    public static String[] top(final int limit, String sort) {
        if (!plugin.getSQLHandler().isConnected()) {
            plugin.getLogger().severe("Database is not connected!");
            plugin.sendPrefixedOP(new ArrayList<>(), DATABASE_CONNECTED);
            return null;
        }

        sort = sort.toUpperCase();
        List<EntityStatistic> result = null;
        final Map<String, Double> results = new HashMap<>();

        final List<String> sortedValues = new ArrayList<>();

        String order;
        try {

            switch (sort) {
                case "DEATHS":
                    order = "deaths";
                    break;
                case "STREAK":
                    order = "streak";
                    break;
                case "CURRENTSTREAK":
                    order = "currentstreak";
                    break;
                case "K-D":
                    order = "`kills`/(`deaths`+1.0)";
                    break;
                case "KILLS":
                default:
                    order = "kills";
                    break;
            }

            boolean isAscending = false;

            if (!MobStats.getInstance().config().getBoolean(Config.Entry.STATISTICS_DEATHS_DESCENDING) && sort.equals("DEATHS")) {
                isAscending = true;
            }

            result = plugin.getSQLHandler().getTopSorted(limit, order, isAscending);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (result != null) {
            for (EntityStatistic entry : result) {
                switch (sort) {

                    case "KILLS":
                        sortedValues.add(Language.MSG.STATISTIC_FORMAT_VALUE.parse(
                                entry.getName(), String.valueOf(entry.getKills())));
                        break;
                    case "DEATHS":
                        sortedValues.add(Language.MSG.STATISTIC_FORMAT_VALUE.parse(
                                entry.getName(),String.valueOf(entry.getDeaths())));
                        break;
                    case "STREAK":
                        sortedValues.add(Language.MSG.STATISTIC_FORMAT_VALUE.parse(
                                entry.getName(),String.valueOf(entry.getMaxStreak())));
                        break;
                    case "CURRENTSTREAK":
                        sortedValues.add(Language.MSG.STATISTIC_FORMAT_VALUE.parse(
                                entry.getName(),String.valueOf(entry.getCurrentStreak())));
                        break;
                    default:
                        results.put(entry.getName(), calculateRatio(entry));
                        break;
                }
            }
        }
        if (sort.equals("KILLS") || sort.equals("DEATHS") || sort.equals("STREAK") || sort.equals("CURRENTSTREAK")) {
            String[] output = new String[sortedValues.size()];

            int pos = 0;

            for (String s : sortedValues) {
                output[pos++] = s;
            }
            return output;
        }

        return sortParse(results, limit);
    }

    /**
     * Get a world's top statistics sorted by type
     *
     * @param limit the amount to fetch
     * @param sort  the type to sort by
     * @param world the world to filter by
     * @param days  the amount of days to query
     * @return a sorted array
     */
    public static String[] topWorld(final int limit, String sort, String world, int days) {
        if (!plugin.getSQLHandler().isConnected()) {
            plugin.getLogger().severe("Database is not connected!");
            plugin.sendPrefixedOP(new ArrayList<>(), DATABASE_CONNECTED);
            return null;
        }

        sort = sort.toUpperCase();
        List<EntityStatistic> result = null;
        final Map<String, Double> results = new HashMap<>();

        final List<String> sortedValues = new ArrayList<>();

        try {
            result = plugin.getSQLHandler().getTopPlusSorted(limit, sort, days);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (result != null) {
            for (EntityStatistic entry : result) {
                switch (sort) {

                    case "KILLS":
                        sortedValues.add(Language.MSG.STATISTIC_FORMAT_VALUE.parse(
                                entry.getName(), String.valueOf(entry.getKills())));
                        break;
                    case "DEATHS":
                        sortedValues.add(Language.MSG.STATISTIC_FORMAT_VALUE.parse(
                                entry.getName(),String.valueOf(entry.getDeaths())));
                        break;
                    default:
                        results.put(entry.getName(), calculateRatio(entry));
                        break;
                }
            }
        }
        if (sort.equals("KILLS") || sort.equals("DEATHS")) {
            String[] output = new String[sortedValues.size()];

            int pos = 0;

            for (String s : sortedValues) {
                output[pos++] = s;
            }
            return output;
        }

        return sortParse(results, limit);
    }

    /**
     * Get the top statistics sorted by type
     *
     * @param limit the amount to fetch
     * @param sort  the type to sort by
     * @param days  the amount of days to query
     * @return a sorted array
     */
    public static String[] topPlus(final int limit, String sort, int days) {
        if (!plugin.getSQLHandler().isConnected()) {
            plugin.getLogger().severe("Database is not connected!");
            plugin.sendPrefixedOP(new ArrayList<>(), DATABASE_CONNECTED);
            return null;
        }

        sort = sort.toUpperCase();
        List<EntityStatistic> result = null;
        final Map<String, Double> results = new HashMap<>();

        final List<String> sortedValues = new ArrayList<>();

        try {
            result = plugin.getSQLHandler().getTopPlusSorted(limit, sort, days);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (result != null) {
            for (EntityStatistic entry : result) {
                switch (sort) {

                    case "KILLS":
                        sortedValues.add(Language.MSG.STATISTIC_FORMAT_VALUE.parse(
                                entry.getName(), String.valueOf(entry.getKills())));
                        break;
                    case "DEATHS":
                        sortedValues.add(Language.MSG.STATISTIC_FORMAT_VALUE.parse(
                                entry.getName(),String.valueOf(entry.getDeaths())));
                        break;
                    default:
                        results.put(entry.getName(), calculateRatio(entry));
                        break;
                }
            }
        }
        if (sort.equals("KILLS") || sort.equals("DEATHS")) {
            String[] output = new String[sortedValues.size()];

            int pos = 0;

            for (String s : sortedValues) {
                output[pos++] = s;
            }
            return output;
        }

        return sortParse(results, limit);
    }

    /**
     * Get the top statistics sorted by type
     *
     * @param count the amount to fetch
     * @param sort  the type to sort by
     * @return a sorted array
     */
    public static String[] flop(final int count, String sort) {
        if (!plugin.getSQLHandler().isConnected()) {
            plugin.getLogger().severe("Database is not connected!");
            plugin.sendPrefixedOP(new ArrayList<>(), DATABASE_CONNECTED);
            return null;
        }

        sort = sort.toUpperCase();
        List<EntityStatistic> result = null;
        final Map<String, Double> results = new HashMap<>();

        final List<String> sortedValues = new ArrayList<>();

        String order;
        try {

            switch (sort) {
                case "DEATHS":
                    order = "deaths";
                    break;
                case "STREAK":
                    order = "streak";
                    break;
                case "CURRENTSTREAK":
                    order = "currentstreak";
                    break;
                case "K-D":
                    order = "`kills`/(`deaths`+1.0)";
                    break;
                case "KILLS":
                default:
                    order = "kills";
                    break;
            }

            int limit = count;

            boolean isAscending = true;

            if (!MobStats.getInstance().config().getBoolean(Config.Entry.STATISTICS_DEATHS_DESCENDING) && sort.equals("DEATHS")) {
                isAscending = false;
            }

            result = plugin.getSQLHandler().getTopSorted(limit, order, isAscending);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (result != null) {
            for (EntityStatistic entry : result) {
                switch (sort) {

                    case "KILLS":
                        sortedValues.add(Language.MSG.STATISTIC_FORMAT_VALUE.parse(
                                entry.getName(), String.valueOf(entry.getKills())));
                        break;
                    case "DEATHS":
                        sortedValues.add(Language.MSG.STATISTIC_FORMAT_VALUE.parse(
                                entry.getName(),String.valueOf(entry.getDeaths())));
                        break;
                    case "STREAK":
                        sortedValues.add(Language.MSG.STATISTIC_FORMAT_VALUE.parse(
                                entry.getName(),String.valueOf(entry.getMaxStreak())));
                        break;
                    case "CURRENTSTREAK":
                        sortedValues.add(Language.MSG.STATISTIC_FORMAT_VALUE.parse(
                                entry.getName(),String.valueOf(entry.getCurrentStreak())));
                        break;
                    default:
                        results.put(entry.getName(), calculateRatio(entry));
                        break;
                }
            }
        }
        if (sort.equals("KILLS") || sort.equals("DEATHS") || sort.equals("STREAK") || sort.equals("CURRENTSTREAK")) {
            String[] output = new String[sortedValues.size()];

            int pos = 0;

            for (String s : sortedValues) {
                output[pos++] = s;
            }
            return output;
        }

        return sortParse(results, count);
    }

    public static List<Map<InformationType, String>> detailedTop(int max, InformationType column) {
        List<Map<InformationType, String>> result = new ArrayList<>();

        try {
            String sort = "";
            // `name`,`kills`,`deaths`,`streak`,`currentstreak`,`time`,`uid`
            switch (column) {
                case NAME:
                case DEATHS:
                case KILLS:
                case CURRENTSTREAK:
                case STREAK:
                    sort = column.name().toLowerCase();
                    break;
            }
            List<EntityStatistic> stats = plugin.getSQLHandler().getTopSorted(max, sort, column == InformationType.DEATHS);

            for (EntityStatistic stat : stats) {
                result.add(stat.toStringMap());
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return result;
    }

    /**
     * Wipe all stats
     *
     * @param uuid Entity id to wipe or null to wipe all stats
     */
    public static void wipe(final UUID uuid) {
        if (uuid == null) {
            plugin.getSQLHandler().deleteStats();
            plugin.getSQLHandler().deleteKills();
        } else {
            plugin.getSQLHandler().deleteStatsByUUID(uuid);
            plugin.getSQLHandler().deleteKillsByUUID(uuid);
        }
        EntityStatisticsBuffer.clear(uuid);
    }

    /**
     * Calculate the kill / death ratio as defined in the config
     *
     * @param statistic the EntityStatistic to fill in
     * @return the calculated value
     */
    public static Double calculateRatio(EntityStatistic statistic) {
        if (plugin.config().getBoolean(Config.Entry.STATISTICS_KD_SIMPLE)) {
            if (statistic.getDeaths() < 1) {
                return (double) statistic.getKills();
            }
            return ((double) statistic.getKills()) / statistic.getDeaths();
        }

        if (formula == null) {
            String string = plugin.config().getString(Config.Entry.STATISTICS_KD_CALCULATION);
            formula = MathFormulaManager.getInstance().parse(string);
        }

        return formula.evaluate(statistic);
    }

    /**
     * Calculate the kill / death ratio as defined in the config
     *
     * @param kills     to take into account
     * @param deaths    to take into account
     * @param streak    to take into account
     * @param maxstreak to take into account
     * @return the calculated value
     */
    public static Double calculateRatio(final int kills, final int deaths, final int streak,
                                        final int maxstreak) {
        return calculateRatio(
                new EntityStatistic(
                        "legacy", kills, deaths, maxstreak, streak, 0, new UUID(0, 0)
                )
        );
    }

    /**
     * Do all the things we need to do when a kill happened. Kills, deaths, streaks, max streaks
     *
     * @param entity       the Entity to handle
     * @param kill         true if the Entity did a kill, false if they were killed
     * @param addMaxStreak should we increase the max streak?
     */
    private static void checkAndDo(final Entity entity, final boolean kill, final boolean addMaxStreak) {
        if (plugin.getSQLHandler().allowsAsync()) {
            DEBUGGER.i("checkAndDo will run async...");

            Bukkit.getScheduler().runTaskAsynchronously(MobStats.getInstance(), new CheckAndDo(
                    entity, kill, addMaxStreak
            ));
        } else {
            DEBUGGER.i("checkAndDo will run SYNC!");

            Bukkit.getScheduler().runTask(MobStats.getInstance(), new CheckAndDo(
                    entity, kill, addMaxStreak
            ));
        }
    }

    private static boolean incDeath(final Entity entity) {
        if (entity.hasPermission("mobstats.count")) {
            EntityStatisticsBuffer.setStreak(entity.getUniqueId(), 0);
            checkAndDo(entity,false, false);
            return true;
        }
        return false;
    }

    private static boolean incKill(final Entity entity) {
        if (entity.hasPermission("mobstats.count")) {
            boolean incMaxStreak;
            if (EntityStatisticsBuffer.hasStreak(entity.getUniqueId())) {
                incMaxStreak = EntityStatisticsBuffer.addStreak(entity.getUniqueId());
                EntityStatisticsBuffer.getStreak(entity.getUniqueId());
            } else {

                int streakCheck = EntityStatisticsBuffer.getStreak(entity.getUniqueId());
                if (streakCheck < 1) {
                    EntityStatisticsBuffer.setStreak(entity.getUniqueId(), 1);
                    EntityStatisticsBuffer.setMaxStreak(entity.getUniqueId(), 1);
                    incMaxStreak = true;
                } else {
                    incMaxStreak = EntityStatisticsBuffer.addStreak(entity.getUniqueId());
                }

            }
            checkAndDo(entity, true, incMaxStreak);
            return true;
        }
        return false;
    }

    private static String[] sortParse(final Map<String, Double> results,
                                      final int count) {
        String[] result = new String[results.size()];
        Double[] sort = new Double[results.size()];

        int pos = 0;

        DecimalFormat df = new DecimalFormat("#.##");

        for (String key : results.keySet()) {
            sort[pos] = results.get(key);
            result[pos] = Language.MSG.STATISTIC_FORMAT_VALUE.parse(key, df.format(sort[pos]));
            pos++;
        }

        int pos2 = results.size();
        boolean doMore = true;
        while (doMore) {
            pos2--;
            doMore = false; // assume this is our last pass over the array
            for (int i = 0; i < pos2; i++) {
                if (sort[i] < sort[i + 1]) {
                    // exchange elements

                    final double tempI = sort[i];
                    sort[i] = sort[i + 1];
                    sort[i + 1] = tempI;

                    final String tempR = result[i];
                    result[i] = result[i + 1];
                    result[i + 1] = tempR;

                    doMore = true; // after an exchange, must look again
                }
            }
        }
        if (result.length < count) {
            return result;
        }
        String[] output = new String[count];
        System.arraycopy(result, 0, output, 0, output.length);

        return output;
    }

    /**
     * Refresh the RAM values after making changes with a command
     */
    public static void refresh() {
        EntityStatisticsBuffer.refresh();
        formula = null;
    }
}
