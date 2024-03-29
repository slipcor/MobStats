package net.slipcor.mobstats.listeners;

import net.slipcor.core.CoreDebugger;
import net.slipcor.mobstats.MobStats;
import net.slipcor.mobstats.api.DatabaseAPI;
import net.slipcor.mobstats.api.EntityStatisticsBuffer;
import net.slipcor.mobstats.classes.PlayerDamageHistory;
import net.slipcor.mobstats.display.SignDisplay;
import net.slipcor.mobstats.text.TextFormatter;
import net.slipcor.mobstats.yml.Config;
import net.slipcor.mobstats.yml.Language;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;

/**
 * Player Event Listener class
 *
 * All the things that happen when a Player is involved
 */

public class EntityListener implements Listener {
    private final MobStats plugin;

    public static CoreDebugger Debugger;

    private int assistSeconds;

    private final Map<UUID, PlayerDamageHistory> lastDamage = new HashMap<>();

    public EntityListener(final MobStats instance) {
        this.plugin = instance;
        assistSeconds = this.plugin.config().getInt(Config.Entry.STATISTICS_ASSIST_SECONDS);
    }

    /**
     * Hook into a Player getting damaged
     *
     * Remember this damage in case the damagee dies later
     *
     * @param event the EntityDamageByEntityEvent
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        Entity attacked = event.getEntity();
        Entity attacker = null;

        if (event.getDamager() instanceof Projectile) {
            Projectile projectile = (Projectile) event.getDamager();
            if (projectile.getShooter() instanceof Entity && !projectile.getShooter().equals(attacked)) {
                attacker = (Entity) projectile.getShooter();
            }
        } else if (event.getDamager() instanceof Tameable && plugin.config().getBoolean(Config.Entry.STATISTICS_COUNT_PET_DEATHS)) {
            AnimalTamer tamer = ((Tameable) event.getDamager()).getOwner();
            if (tamer instanceof Player) {
                attacker = (Player) tamer;
            }
        } else {
            attacker = event.getDamager();
        }

        if (attacker == null) {
            return;
        }

        if (!this.plugin.config().getBoolean(Config.Entry.STATISTICS_COUNT_MOB_VS_MOB) && (!(attacker instanceof Player) && !(attacked instanceof Player))) {
            Debugger.i("neither is a player");
            return;
        }

        if (lastDamage.containsKey(attacked.getUniqueId())) {
            PlayerDamageHistory history = lastDamage.get(attacked.getUniqueId());
            history.commitPlayerDamage(attacker);
        } else {
            PlayerDamageHistory history = new PlayerDamageHistory();
            history.commitPlayerDamage(attacker);
            lastDamage.put(attacked.getUniqueId(), history);
        }
    }

    /**
     * Hook into a Player joining the server
     *
     * Tell OPs that there was an update (if enabled)
     * Initiate Players for stats
     *
     * @param event the PlayerJoinEvent
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        if (event.getPlayer().isOp() && plugin.getUpdater() != null) {
            plugin.getUpdater().message(event.getPlayer());
        }
        DatabaseAPI.initiateEntity(event.getPlayer());
    }

    /**
     * Hook into a Player quitting the server
     *
     * If set, reset the kill streak of a Player
     *
     * @param event the PlayerQuitEvent
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerQuit(final PlayerQuitEvent event) {
        if (plugin.config().getBoolean(Config.Entry.STATISTICS_RESET_KILLSTREAK_ON_QUIT)) {
            EntityStatisticsBuffer.setStreak(event.getPlayer().getUniqueId(), 0);
        }
    }

    /**
     * Hook into a Player death, count deaths and kills, where applicable
     *
     * @param event the PlayerDeathEvent
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerDeath(final EntityDeathEvent event) {
        if (plugin.ignoresWorld(event.getEntity().getWorld().getName())) {
            TextFormatter.explainIgnoredWorld(event.getEntity());
            return;
        }

        final Entity player = event.getEntity();
        Debugger.i("Player killed!", player);

        lastDamage.remove(player.getUniqueId()); // clear map for next kill

        if (notCountingEntity(player)) {
            Debugger.i("not counting this one!");
            return;
        }
        Entity attacker = event.getEntity().getKiller();

        if (attacker == null) {
            Debugger.i("Killer is null", player);

            if (lastDamage.containsKey(player.getUniqueId())) {
                PlayerDamageHistory history = lastDamage.get(player.getUniqueId());
                List<UUID> damagers = history.getLastDamage(assistSeconds);
                if (damagers.size() > 0) {
                    attacker = Bukkit.getEntity(damagers.get(0));
                }
                lastDamage.remove(player.getUniqueId()); // clear map for next kill
            }

            if (attacker == null) {
                Debugger.i("Killer is still null", player);
                if (plugin.config().getBoolean(Config.Entry.STATISTICS_COUNT_REGULAR_DEATHS)) {
                    Debugger.i("Kill should be counted", event.getEntity());
                    DatabaseAPI.AkilledB(null, event.getEntity());
                }
                return;
            }
        }

        if (notCountingEntity(attacker)) {
            Debugger.i("not counting this one!");
            return;
        }

        DatabaseAPI.AkilledB(attacker, player);
    }

    /**
     * Check whether we should not be counting statistics when this Player is involved
     *
     * @param player the Player to check
     *
     * @return whether we should skip the current death event because of this Player
     */
    private boolean notCountingEntity(Entity player) {
        if (player == null) {
            return false; // we do eventually count this as regular death
        }

        List<String> tags = plugin.config().getStringList(Config.Entry.STATISTICS_PREVENTING_PLAYER_META, new ArrayList<>());

        for (String tag : tags) {
            if (player.hasMetadata(tag)) {
                Debugger.i("Tag found: " + tag);
                return true;
            }
        }

        // nothing bad found, let us count this!
        return false;
    }

    /**
     * Hook into a Player interacting
     *
     * If the Player is interacting with a Sign, try setting up a leaderboard
     *
     * @param event the PlayerInteractEvent
     */
    @EventHandler(ignoreCancelled = true)
    public void playerInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block != null) {
            if (block.getState() instanceof Sign) {
                SignDisplay display = SignDisplay.byLocation(block.getLocation());

                if (display == null) {
                    // it does not exist yet, try creating it
                    display = SignDisplay.init(block.getLocation());
                    if (display != null) {
                        if (!display.isValid()) {
                            // we could not create it!
                            MobStats.getInstance().sendPrefixed(event.getPlayer(), Language.MSG.DISPLAY_SETUP_INVALID.toString());
                        } else {
                            MobStats.getInstance().sendPrefixed(event.getPlayer(),
                                    Language.MSG.DISPLAY_SUCCESSFUL.parse(event.getClickedBlock().getLocation().toString()));

                            SignDisplay.saveAllDisplays();
                        }
                    }
                    return;
                }
                event.setCancelled(!event.getPlayer().isOp());
                if (event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
                    return;
                }

                display.cycleSortColumn();
                MobStats.getInstance().sendPrefixed(
                        event.getPlayer(),
                        Language.MSG.DISPLAY_SORTED_BY.parse(display.getSortColumn().name()));
            } else if (SignDisplay.needsProtection(event.getPlayer().getLocation())) {
                event.setCancelled(!event.getPlayer().isOp());
            }
        }
    }
}