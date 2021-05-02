package net.slipcor.mobstats.classes;

import net.slipcor.mobstats.MobStats;
import net.slipcor.mobstats.yml.Config;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class NameHandler {

    public static OfflinePlayer findPlayer(String value) {
        OfflinePlayer result = null;
        for (OfflinePlayer off : Bukkit.getOfflinePlayers()) {
            if (off.getName().equalsIgnoreCase(value)) {
                return off;
            }
            if (off.getPlayer() != null && off.getPlayer().getDisplayName().toLowerCase().contains(value.toLowerCase())) {
                return off;
            }
            if (result == null && off.getName().toLowerCase().contains(value.toLowerCase())) {
                result = off;
            }
        }
        // only return match if no exact result was found
        return result;
    }

    public static String getName(Entity entity) {
        if (MobStats.getInstance().config().getBoolean(Config.Entry.OTHER_DISPLAYNAMES)) {
            if (entity instanceof Player) {
                return ((Player) entity).getDisplayName();
            } else {
                return entity.getCustomName();
            }
        }
        return entity.getName();
    }

    public static String getRawPlayerName(Entity entity) {
        if (MobStats.getInstance().config().getBoolean(Config.Entry.OTHER_DISPLAYNAMES)) {
            if (entity instanceof Player) {
                return ChatColor.stripColor(((Player) entity).getDisplayName());
            }
        }
        return entity.getName();
    }
}
