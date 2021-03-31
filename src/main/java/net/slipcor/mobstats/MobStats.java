package net.slipcor.mobstats;

import net.slipcor.mobstats.api.DatabaseAPI;
import net.slipcor.mobstats.api.DatabaseConnection;
import net.slipcor.mobstats.classes.Debugger;
import net.slipcor.mobstats.classes.PlaceholderAPIHook;
import net.slipcor.mobstats.classes.NameHandler;
import net.slipcor.mobstats.commands.*;
import net.slipcor.mobstats.core.*;
import net.slipcor.mobstats.display.SignDisplay;
import net.slipcor.mobstats.impl.FlatFileConnection;
import net.slipcor.mobstats.impl.MySQLConnection;
import net.slipcor.mobstats.impl.SQLiteConnection;
import net.slipcor.mobstats.listeners.MobArenaListener;
import net.slipcor.mobstats.listeners.EntityListener;
import net.slipcor.mobstats.listeners.PluginListener;
import net.slipcor.mobstats.metrics.MetricsLite;
import net.slipcor.mobstats.metrics.MetricsMain;
import net.slipcor.mobstats.text.TextComponent;
import net.slipcor.mobstats.text.TextFormatter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Main Plugin class
 *
 * @author slipcor
 */

public class MobStats extends JavaPlugin {
    // Plugin instance to use staticly all over the place
    private static MobStats instance;
    private final static int CFGVERSION = 1;

    // database type setting
    private boolean mySQL = false;
    private boolean SQLite = false;

    // database handler
    private DatabaseConnection dbHandler;

    // managers
    private final Debugger debugger = new Debugger(8);
    private Plugin maHandler = null;
    private Updater updater = null;
    private Config configHandler = null;

    private FileConfiguration announcements = null; // configurable announcements per streak level
    private FileConfiguration commands = null;      // configurable commands per streak level

    // listeners
    private EntityListener entityListener;
    private final MobArenaListener pluginListener = new MobArenaListener(this);

    // commands
    private final Map<String, AbstractCommand> commandMap = new HashMap<>();
    private final List<AbstractCommand> commandList = new ArrayList<>();

    public static MobStats getInstance() {
        return instance;
    }

    /**
     * @return the Config instance, create if not yet instantiated
     */
    public Config config() {
        if (configHandler == null) {
            if (getConfig().getInt("ver", 0) < CFGVERSION) {
                getConfig().options().copyDefaults(true);
                getConfig().set("ver", CFGVERSION);
                saveConfig();
            }
            this.reloadConfig();
            configHandler = new Config(this);
            getLogger().info("Loaded config file!");
        }
        return configHandler;
    }

    /**
     * @return the MobArena plugin
     */
    public Plugin getMAHandler() {
        return maHandler;
    }

    /**
     * @return the MobArena Listener
     */
    public MobArenaListener getPAListener() {
        return pluginListener;
    }

    /**
     * @return the DatabaseConnection instance
     */
    public DatabaseConnection getSQLHandler() {
        return dbHandler;
    }

    /**
     * @return the Updater instance
     */
    public Updater getUpdater() {
        return updater;
    }

    /**
     * Handle an Entity gaining a streak level, maybe issuing commands or announcements
     *
     * @param uuid  the Entity's UUID
     * @param value the new streak value
     */
    public void handleStreak(UUID uuid, int value) {
        String key = String.valueOf(value);
        Entity entity = Bukkit.getEntity(uuid);
        try {
            if (config().getBoolean(Config.Entry.STATISTICS_STREAK_ANNOUNCEMENTS)) {
                if (announcements == null) {
                    announcements = new YamlConfiguration();
                    announcements.load(new File(getDataFolder(), "streak_announcements.yml"));
                }
                String message = announcements.getString(key, "");
                if (!message.isEmpty()) {
                    Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', message)
                            .replace("%entity%", NameHandler.getName(entity))
                            .replace("%entityid%", entity.getUniqueId().toString()));
                }
            }
            if (config().getBoolean(Config.Entry.STATISTICS_STREAK_COMMANDS)) {
                if (commands == null) {
                    commands = new YamlConfiguration();
                    commands.load(new File(getDataFolder(), "streak_commands.yml"));
                }
                String command = commands.getString(key, "");
                if (!command.isEmpty()) {
                    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),
                            command
                                    .replace("%entity%", NameHandler.getName(entity))
                                    .replace("%entityid%", entity.getUniqueId().toString()));
                }
            }
        } catch (IOException | InvalidConfigurationException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Check whether a world is disabled for this plugin
     *
     * @param name the world name
     * @return true if it is disabled, false otherwise
     */
    public boolean ignoresWorld(final String name) {
        if (!getConfig().contains(Config.Entry.IGNORE_WORLDS.getNode())) {
            return false;
        }
        return config().getList(Config.Entry.IGNORE_WORLDS).contains(name);
    }

    /**
     * Instantiate command
     */
    private void loadCommands() {
        new CommandConfig().load(commandList, commandMap);
        new CommandDebug().load(commandList, commandMap);
        new CommandDebugKill().load(commandList, commandMap);
        new CommandMigrate().load(commandList, commandMap);
        new CommandPurge().load(commandList, commandMap);
        new CommandShow().load(commandList, commandMap);
        new CommandSet().load(commandList, commandMap);
        new CommandTop().load(commandList, commandMap);
        new CommandReload().load(commandList, commandMap);
        new CommandWipe().load(commandList, commandMap);
    }

    /**
     * Read the config and try to connect to the database
     */
    public void loadConfig() {
        DatabaseAPI.initiate(this);

        config().reload();

        String dbHost = null;
        String dbUser = null;
        String dbPass = null;
        String dbDatabase = null;
        String dbTable = null;
        String dbOptions = null;
        String dbKillTable = null;
        int dbPort = 0;

        if (config().getBoolean(Config.Entry.MYSQL_ACTIVE)) {
            this.mySQL = true;

            dbHost = config().get(Config.Entry.MYSQL_HOST);
            dbUser = config().get(Config.Entry.MYSQL_USERNAME);
            dbPass = config().get(Config.Entry.MYSQL_PASSWORD);
            dbDatabase = config().get(Config.Entry.MYSQL_DATABASE);
            dbTable = config().get(Config.Entry.MYSQL_TABLE);
            dbOptions = config().get(Config.Entry.MYSQL_OPTIONS);

            if (config().getBoolean(Config.Entry.STATISTICS_COLLECT_PRECISE)) {
                dbKillTable = config().get(Config.Entry.MYSQL_KILLTABLE);
            }

            dbPort = config().getInt(Config.Entry.MYSQL_PORT);

        } else if (config().getBoolean(Config.Entry.SQLITE_ACTIVE)) {
            this.SQLite = true;
            dbDatabase = config().get(Config.Entry.SQLITE_FILENAME);

            dbTable = config().get(Config.Entry.SQLITE_TABLE);
            if (config().getBoolean(Config.Entry.STATISTICS_COLLECT_PRECISE)) {
                dbKillTable = config().get(Config.Entry.SQLITE_KILLTABLE);
                getLogger().warning("Specific stats can be turned off as they are never used, they are intended for SQL and web frontend usage!");
                getLogger().warning("We recommend you set '" + Config.Entry.STATISTICS_COLLECT_PRECISE.getNode() + "' to false");
            }
        } else {
            dbTable = config().get(Config.Entry.YML_TABLE);
            if (config().getBoolean(Config.Entry.STATISTICS_COLLECT_PRECISE) &&
                config().getBoolean(Config.Entry.YML_COLLECT_PRECISE)) {
                dbKillTable = config().get(Config.Entry.MYSQL_KILLTABLE);
            } else if (config().getBoolean(Config.Entry.STATISTICS_COLLECT_PRECISE)) {
                getLogger().warning("Specific stats can be turned off as they are never used, they are intended for SQL and web frontend usage!");
                getLogger().warning("Please either switch to SQLite or re-enable this by setting '" + Config.Entry.YML_COLLECT_PRECISE.getNode() + "' to true");
            }
        }

        // verify settings
        if (this.mySQL) {
            if (dbHost.equals("") ||
                    dbUser.equals("") ||
                    dbPass.equals("") ||
                    dbDatabase.equals("") ||
                    dbPort == 0) {
                this.mySQL = false;
            }
        } else if (this.SQLite) {
            if (dbDatabase.equals("")) {
                this.SQLite = false;
            }
        }

        // Enabled SQL/MySQL
        if (this.mySQL) {
            // Declare MySQL Handler
            getLogger().info("Database: mySQL");
            try {
                dbHandler = new MySQLConnection(dbHost, dbPort, dbDatabase, dbUser,
                        dbPass, dbOptions, dbTable, dbKillTable);
            } catch (InstantiationException | ClassNotFoundException | IllegalAccessException e1) {
                e1.printStackTrace();
            }
        } else if (this.SQLite) {
            getLogger().info("Database: SQLite");
            dbHandler = new SQLiteConnection(dbDatabase, dbTable, dbKillTable);
        } else {
            // default to flatfile
            getLogger().warning("Database: YML");
            dbHandler = new FlatFileConnection(dbTable, dbKillTable);
        }


        getLogger().info("Database Initializing");
        // Initialize MySQL Handler

        if (dbHandler != null && dbHandler.connect(true)) {
            getLogger().info("Database connection successful");
            // Check if the tables exist, if not, create them
            if (!dbHandler.tableExists(dbDatabase, dbTable)) {
                // normal table doesnt exist, create both

                getLogger().info("Creating table " + dbTable);
                dbHandler.createStatsTable(true);

                if (dbKillTable != null) {
                    getLogger().info("Creating table " + dbKillTable);
                    dbHandler.createKillStatsTable(true);
                }
            }
        } else {
            getLogger().severe("Database connection failed");
            this.mySQL = false;
            this.SQLite = false;
        }
    }

    /**
     * Load the language file
     */
    public void loadLanguage() {
        final File langFile = new File(this.getDataFolder(), "lang.yml");
        if (!langFile.exists()) {
            try {
                langFile.createNewFile();
            } catch (IOException e) {
                this.getLogger().warning("Language file could not be created. Using defaults!");
                e.printStackTrace();
            }
        }
        final YamlConfiguration cfg = YamlConfiguration.loadConfiguration(langFile);
        if (Language.load(cfg)) {
            try {
                cfg.save(langFile);
            } catch (IOException e) {
                this.getLogger().warning("Language file could not be written. Using defaults!");
                e.printStackTrace();
            }
        }
    }

    /**
     * Hook into other plugins
     */
    private void loadHooks() {
        final Plugin maPlugin = getServer().getPluginManager().getPlugin("MobArena");
        if (maPlugin != null && maPlugin.isEnabled()) {
            getLogger().info("<3 MobArena");
            this.maHandler = maPlugin;
        }
    }

    public boolean onCommand(final CommandSender sender, final Command cmd, final String commandLabel, final String[] args) {

        debugger.i("onCommand!", sender);

        final AbstractCommand acc = (args.length > 0) ? commandMap.get(args[0].toLowerCase()) : null;
        if (acc != null) {
            acc.commit(sender, args);
            return true;
        }

        if (args.length < 1) {
            commandMap.get("show").commit(sender, new String[0]);
            return true;
        }
        int legacy = 0;
        try {
            legacy = Integer.parseInt(args[0]);
        } catch (Exception e) {
        }

        if (legacy > 0) {
            commandMap.get("top").commit(sender, args);
            return true;
        }

        boolean found = false;
        for (AbstractCommand command : commandList) {
            if (command.hasPerms(sender)) {
                sender.sendMessage(ChatColor.YELLOW + command.getShortInfo());
                found = true;
            }
        }

        final OfflinePlayer player = NameHandler.findPlayer(args[0]);

        if (player == null) {
            sender.sendMessage("Player not found: " + args[0]);
        }

        if (!found && DatabaseAPI.hasEntry(player.getUniqueId())) {
            commandMap.get("show").commit(sender, args);
            return true;
        }

        return found;
    }

    public void onDisable() {
        Debugger.destroy();
        getLogger().info("disabled. (version " + getDescription().getVersion() + ")");
    }

    public void onEnable() {
        instance = this;
        configHandler = config();

        final PluginDescriptionFile pdfFile = getDescription();

        loadConfig();
        loadCommands();
        if (!new File(getDataFolder(), "streak_announcements.yml").exists()) {
            saveResource("streak_announcements.yml", false);
        }
        if (!new File(getDataFolder(), "streak_commands.yml").exists()) {
            saveResource("streak_commands.yml", false);
        }

        entityListener = new EntityListener(this);
        getServer().getPluginManager().registerEvents(entityListener, this);

        if (dbHandler == null || !dbHandler.isConnected()) {
            getLogger().severe("Database not connected, plugin DISABLED!");
            getServer().getPluginManager().disablePlugin(this);
            return; //to ensure the rest of the plugins code is not executed as this can lead to problems.
        }
        loadHooks();

        if (config().getBoolean(Config.Entry.OTHER_MOBARENA)) {
            if (getServer().getPluginManager().isPluginEnabled("mobarena")) {
                getServer().getPluginManager().registerEvents(pluginListener, this);
            } else {
                PluginListener paPluginListener = new PluginListener(this);
                getServer().getPluginManager().registerEvents(paPluginListener, this);
            }
        }

        updater = new Updater(this, getFile());

        loadLanguage();

        if (config().getBoolean(Config.Entry.BSTATS_ENABLED)) {
            if (config().getBoolean(Config.Entry.BSTATS_FULL)) {
                MetricsMain mainMetrics = new MetricsMain(this);
            } else {
                MetricsLite liteMetrics = new MetricsLite(this);
            }
        }

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            getLogger().info("MobStats - PlaceholderAPI found.");
            new PlaceholderAPIHook().register();
        }

        Debugger.load(this, Bukkit.getConsoleSender());

        if (config().getBoolean(Config.Entry.STATISTICS_PURGE_MOBS_ON_START)) {
            int days = config().getInt(Config.Entry.STATISTICS_PURGE_MOBS_DAYS);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mobstats purge mobs " + days);
        }

        SignDisplay.loadAllDisplays();

        getLogger().info("enabled. (version " + pdfFile.getVersion() + ")");
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command cmd, final String alias, final String[] args) {
        return TabComplete.getMatches(sender, commandList, args);
    }

    public void reloadStreaks() {
        commands = null;
        announcements = null;
    }

    public void sendPrefixed(final CommandSender sender, final String message) {
        if (!"".equals(message)) {
            sender.sendMessage(Language.MSG_PREFIX + message);
        }
    }

    public void sendPrefixedOP(List<CommandSender> senders, final TextComponent... message) {
        // if the admin has a config node set to false, noop
        if (!config().getBoolean(Config.Entry.OTHER_OP_MESSAGES)) {
            debugger.i("Would opmsg but config is false");
            return;
        }

        // if we list no senders then send to all users with permission (who are online)
        if (senders.size() == 0) {
            senders.addAll(Bukkit.getServer().getOnlinePlayers());
        } else {
            // deduplicate
            senders = new ArrayList<>(new HashSet<>(senders));
            senders.remove(null);
        }

        for (CommandSender sender : senders) {
            // if the user does not have permission for debug messages, noop
            if (!sender.hasPermission("mobstats.opmessages")) {
                debugger.i("Would opmsg but permission is false");
                return;
            }

            // otherwise send the message
            if (TextFormatter.hasContent(message)) {
                TextFormatter.send(sender, TextFormatter.addPrefix(message));
                TextFormatter.explainDisableOPMessages(sender);
            }
        }
    }

    /**
     * Set our MobArena handler when the plugin has been enabled
     *
     * @param plugin the MobArena plugin
     */
    public void setMAHandler(Plugin plugin) {
        this.maHandler = plugin;
    }
}
