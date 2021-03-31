package net.slipcor.mobstats.listeners;

import net.slipcor.mobstats.MobStats;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;

/**
 * Plugin Event Listener class
 *
 * All the things about other Plugins
 */

public class PluginListener implements Listener {
    final MobStats plugin;

    public PluginListener(final MobStats plugin) {
        this.plugin = plugin;
    }

    /**
     * Hook into Plugins enabling, to maybe hook into them
     *
     * @param event the PluginEnableEvent
     */
    @EventHandler
    public void onPluginEnable(final PluginEnableEvent event) {
        if (plugin.getMAHandler() != null) {
            return;
        }
        if (event.getPlugin().getName().equals("MobArena")) {
            plugin.getLogger().info("<3 MobArena");
            plugin.setMAHandler(event.getPlugin());
            plugin.getServer().getPluginManager().registerEvents(plugin.getPAListener(), plugin);
        }
    }
}