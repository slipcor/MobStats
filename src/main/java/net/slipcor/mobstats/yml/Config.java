package net.slipcor.mobstats.yml;

import net.slipcor.core.ConfigEntry;
import net.slipcor.core.CoreConfig;
import net.slipcor.core.CorePlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * Config access class
 *
 * @author slipcor
 */
public class Config extends CoreConfig {
    
    /**
     * The Config entry class
     *
     * Each entry has an explicit class type, a node, optional comments
     * and a default value
     */
    public enum Entry implements ConfigEntry {
        MYSQL(Type.COMMENT, "MySQL", null, 
                "=== [ MySQL Settings ] ==="),
        MYSQL_ACTIVE(Type.BOOLEAN, "MySQL.active", false, 
                "activate MySQL"),
        MYSQL_HOST(Type.STRING, "MySQL.hostname", "host", 
                "hostname to use to connect to the database, in most cases 'localhost'"),
        MYSQL_USERNAME(Type.STRING, "MySQL.username", "user", 
                "username to use to connect to the database"),
        MYSQL_PASSWORD(Type.STRING, "MySQL.password", "pw", 
                "password to use to connect to the database"),
        MYSQL_DATABASE(Type.STRING, "MySQL.database", "db", 
                "database name to connect to"),
        MYSQL_PORT(Type.INT, "MySQL.port", 3306, 
                "database port to connect to"),
        MYSQL_TABLE(Type.STRING, "MySQL.table", "mobstats", 
                "general statistic table name"),
        MYSQL_KILLTABLE(Type.STRING, "MySQL.killtable", "mobkillstats", 
                "kill statistic table name"),
        MYSQL_OPTIONS(Type.STRING, "MySQL.options", "?autoReconnect=true", 
                "connection options"),

        SQLITE(Type.COMMENT, "SQLite", null, 
                "=== [ SQLite Settings ] ==="),
        SQLITE_ACTIVE(Type.BOOLEAN, "SQLite.active", false, 
                "activate SQLite"),
        SQLITE_FILENAME(Type.STRING, "SQLite.filename", "database", 
                "database file name, plugin will append extension '.db'"),
        SQLITE_TABLE(Type.STRING, "SQLite.table", "mobstats", 
                "general statistic table name"),
        SQLITE_KILLTABLE(Type.STRING, "SQLite.killtable", "mobkillstats", 
                "kill statistic table name"),

        YML(Type.COMMENT, "YML", null, 
                "=== [ YML Database Emulation Settings ] ==="),
        YML_TABLE(Type.STRING, "YML.table", "mobstats", 
                "general statistic file name, plugin will append extension '.yml'"),
        YML_KILLTABLE(Type.STRING, "YML.killtable", "mobkillstats", 
                "kill statistic file name, plugin will append extension '.yml'"),
        YML_COLLECT_PRECISE(Type.BOOLEAN, "YML.collectPreciseOverride", false, 
                "really save every kill? This is not advised for YML!"),

        STATISTICS(Type.COMMENT, "statistics", null, 
                "=== [ Statistic Settings ] ==="),
        STATISTICS_ASSIST_SECONDS(Type.INT, "statistics.assistSeconds", 60, 
                "time in seconds to consider a former kill an assist"),
        STATISTICS_LIST_LENGTH(Type.INT, "statistics.maxListLength", 10,
                "amount of players to be stored in cached top and flop lists"),
        STATISTICS_CREATE_ON_JOIN(Type.BOOLEAN, "statistics.createOnJoin", true, 
                "create empty player entry when they join"),
        STATISTICS_COLLECT_PRECISE(Type.BOOLEAN, "statistics.collectPrecise", false, 
                "save every kill - is never read internally, so only for web stats or alike"),
        STATISTICS_COUNT_REGULAR_DEATHS(Type.BOOLEAN, "statistics.countRegularDeaths", false, 
                "count dying from other sources than players towards death count and resetting of streaks"),
        STATISTICS_COUNT_PET_DEATHS(Type.BOOLEAN, "statistics.countPetDeaths", false, 
                "count dying from other player's tamed pets for death count and resetting of streaks"),
        STATISTICS_COUNT_MOB_VS_MOB(Type.BOOLEAN, "statistics.countMobVsMob", true, 
                "count mobs killing other mobs"),
        STATISTICS_CHECK_NEWBIES(Type.BOOLEAN, "statistics.checkNewbies", true, 
                "prevent stats for players with the permission 'mobstats.newbie'"),
        STATISTICS_DEATHS_DESCENDING(Type.BOOLEAN, "statistics.deathsDescending", false, 
                "order deaths descending by default (rather than TOP deaths being LESS deaths)"),
        STATISTICS_KD_CALCULATION(Type.STRING, "statistics.killDeathCalculation", "&k/&d", 
                "mathematical formula to calculate kill/death ratio"),
        STATISTICS_KD_SIMPLE(Type.BOOLEAN, "statistics.killDeathSimple", true, 
                "simplify kill/death ratio calculation to just kills per deaths"),
        STATISTICS_PREVENTING_PLAYER_META(Type.LIST, "statistics.preventingPlayerMeta", Collections.singletonList("NPC"), 
                "player meta that marks players as not counting, for example 'NPC' for most plugins that add them"),
        STATISTICS_PURGE_MOBS_ON_START(Type.BOOLEAN, "statistics.purgeMobsOnStart", true, 
                "clear old mob entries on every start"),
        STATISTICS_PURGE_MOBS_DAYS(Type.BOOLEAN, "statistics.purgeMobsDays", 1, 
                "days to keep record of awesome mobs"),
        STATISTICS_RESET_KILLSTREAK_ON_QUIT(Type.BOOLEAN, "statistics.resetKillstreakOnQuit", false, 
                "always reset a streak when a player disconnects"),
        STATISTICS_LEADERBOARD_REFRESH(Type.INT, "statistics.leaderboardRefresh", 60, 
                "seconds to wait before the top/flop leaderboard values will query the database again"),
        STATISTICS_SHORT_PLACEHOLDERS(Type.BOOLEAN, "statistics.shortPlaceholders", false,
                "try using super short placeholders"),
        STATISTICS_STREAK_ANNOUNCEMENTS(Type.BOOLEAN, "statistics.streakAnnouncements", false, 
                "announce lines from streak_announcements.yml on specific streak values"),
        STATISTICS_STREAK_COMMANDS(Type.BOOLEAN, "statistics.streakCommands", false, 
                "issue commands from streak_commands.yml on specific streak values"),
        STATISTICS_STREAK_BROKEN_THRESHOLD(Type.INT, "statistics.streakBrokenThreshold", 0,
                "the minimum streak required to announce it has been broken (0=announce all)"),
        STATISTICS_FORCE_RELOAD_INTERVAL(Type.INT, "statistics.forceReloadInterval", -1,
                "force reload the database from outside changes after how many seconds"),

        MESSAGES(Type.COMMENT, "msg", null, 
                "=== [ Message Settings ] ==="),
        MESSAGES_OVERRIDES(Type.BOOLEAN, "msg.overrides", false, "activate the following overrides"),
        MESSAGES_OVERRIDE_LIST(Type.LIST, "msg.main",
                Arrays.asList("&cName: &7%n", "&cKills: &7%k", "&cDeaths: &7%d", "&cRatio: &7%r", "&cStreak: &7%s", "&cMax Streak: &7%m"),
                ""),

        UPDATE(Type.COMMENT, "update", null, 
                "=== [ Updater Settings ] ==="),
        UPDATE_MODE(Type.STRING, "update.mode", "both", 
                "what to do? Valid values: disable, announce, download, both"),
        UPDATE_TYPE(Type.STRING, "update.type", "beta", 
                "which type of branch to get updates? Valid values: dev, alpha, beta, release"),

        OTHER(Type.COMMENT, "other", null, 
                "=== [ Other Features ] ==="),
        OTHER_DISPLAYNAMES(Type.BOOLEAN, "other.displayNames", false, "use players' display names"),
        OTHER_MOBARENA(Type.BOOLEAN, "other.MobArena", false, "count Mob Arena kills/deaths"),
        OTHER_OP_MESSAGES(Type.BOOLEAN, "other.OPMessages", true, "provide helpful debug messages for new installations"),

        BSTATS(Type.COMMENT, "bStats", null, 
                "=== [ bStats Metrics Settings ] ==="),
        BSTATS_ENABLED(Type.BOOLEAN, "bStats.enabled", true, 
                "Should we send stats at all? Please keep this in so we have an overview of spread of versions <3"),
        BSTATS_FULL(Type.BOOLEAN, "bStats.full", true, 
                "This setting sends a bit more detailed information about which features are used at all. Thank you for supporting me!"),

        IGNORE_WORLDS(Type.LIST, "ignoreworlds", Collections.singletonList("doNotTrack"), "world names where not to count statistics"),

        LEADERBOARDS(Type.MAP, "leaderboards", null, "Leaderboards go here. Do not change these unless you know what you are doing!"),

        VERSION(Type.INT, "ver", null, "Version for automatic config updating"),
        DEBUG(Type.STRING, "debug", null, "Debug - only change this when you know what you are doing"),

        DISPLAYS(Type.LIST, "leaderboards", new ArrayList<String>(), "locations of leaderboards");


        static {
            MYSQL_USERNAME.secret = true;
            MYSQL_PASSWORD.secret = true;
            MYSQL_PORT.secret = true;
            MYSQL_DATABASE.secret = true;
            MYSQL_HOST.secret = true;
        }

        final ConfigEntry.Type type;
        final String node;
        final Object value;
        final String comment;
        public boolean secret;

        Entry(final ConfigEntry.Type type, final String node, final Object def, final String comment) {
            this.type = type;
            this.node = node;
            value = def;
            this.comment = comment;
        }

        /**
         * Try to get a ConfigEntry based on a node string
         *
         * @param node the node to search for
         * @return the entry or null if not found
         */
        public static Entry getByNode(final String node) {
            for (Entry c : values()) {
                if (c.getNode().equals(node)) {
                    return c;
                }
            }
            return null;
        }

        /**
         * @return the Class type
         */
        public Type getType() {
            return type;
        }

        @Override
        public String getComment() {
            return comment;
        }

        @Override
        public String getNode() {
            return node;
        }

        @Override
        public Object getValue() {
            return value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }

    public Config(final CorePlugin plugin) {
        super(plugin, "Mob Stats Config", new File(plugin.getDataFolder(), "config.yml"));

        emptyNodes = new String[]{
                "MySQL", "SQLite", "YML", "statistics", "msg", "update", "bStats", "other"
        };
    }

    @Override
    public ConfigEntry getByNode(String node) {
        for (Entry c : Entry.values()) {
            if (c.getNode().equals(node)) {
                return c;
            }
        }
        return null;
    }

    /**
     * Load the config-file into the YamlConfiguration, and then populate the
     * value maps.
     *
     * @return true, if the load succeeded, false otherwise.
     */
    public boolean load() {
        try {
            cfg.load(configFile);
            reloadMaps();
            return true;
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected boolean checkMaterials(String node) {
        // not needed
        return false;
    }

    @Override
    protected void loadMaterials() {
        // not needed
    }
}
