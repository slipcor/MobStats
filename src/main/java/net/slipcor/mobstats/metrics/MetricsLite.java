package net.slipcor.mobstats.metrics;

import net.slipcor.mobstats.MobStats;
import org.bukkit.plugin.Plugin;

/**
 * bStats collects some data for plugin authors.
 * <p>
 * Check out https://bStats.org/ to learn more about bStats!
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class MetricsLite extends MetricsBase {

    /**
     * Class constructor.
     *
     * @param plugin The plugin which stats should be submitted.
     */
    public MetricsLite(Plugin plugin) {
        super(plugin);
        MobStats.getInstance().getLogger().info("sending minimum Metrics <3");
    }
}
