package net.slipcor.mobstats.listeners;

import com.garbagemule.MobArena.events.ArenaKillEvent;
import com.garbagemule.MobArena.events.ArenaPlayerDeathEvent;
import net.slipcor.mobstats.MobStats;
import net.slipcor.mobstats.api.DatabaseAPI;
import net.slipcor.mobstats.yml.Config;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * PVP Arena Event Listener class
 *
 * All the orderly PVP Thrashing
 */

public class MobArenaListener implements Listener {

    private final MobStats plugin;

    public MobArenaListener(final MobStats plugin) {
        this.plugin = plugin;
    }

    /**
     * Hook into a Player killing someone
     *
     * @param event the PAKillEvent
     */
    @EventHandler
    public void onArenaKill(final ArenaKillEvent event) {
        if (plugin.ignoresWorld(event.getPlayer().getWorld().getName())) {
            return;
        }
        if (plugin.config().getBoolean(Config.Entry.OTHER_MOBARENA)) {
            DatabaseAPI.AkilledB(event.getPlayer(), event.getVictim());
        }
    }

    /**
     * Hook into a Player being killed
     *
     * @param event the PADeathEvent
     */
    @EventHandler
    public void onArenaDeath(final ArenaPlayerDeathEvent event) {
        if (plugin.ignoresWorld(event.getPlayer().getWorld().getName())) {
            return;
        }
        if (plugin.config().getBoolean(Config.Entry.OTHER_MOBARENA)) {
            DatabaseAPI.AkilledB(null, event.getPlayer());
        }
    }
}