package net.slipcor.mobstats.commands;

import net.slipcor.core.CoreCommand;
import net.slipcor.core.CorePlugin;
import net.slipcor.mobstats.MobStats;
import net.slipcor.mobstats.classes.NameHandler;
import net.slipcor.mobstats.runnables.SendPlayerStats;
import net.slipcor.mobstats.yml.Language;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandShow extends CoreCommand {
    public CommandShow(CorePlugin plugin) {
        super(plugin, "mobstats.count", Language.MSG.ERROR_INVALID_ARGUMENT_COUNT);
    }

    @Override
    public void commit(final CommandSender sender, final String[] args) {
        if (!hasPerms(sender)) {
            return;
        }

        if (args == null || args.length < 1 || (args.length == 1 && args[0].equals("show"))) {
            // /mobstats - show your kill stats
            if (sender instanceof Player) {
                Bukkit.getScheduler().runTaskAsynchronously(MobStats.getInstance(),
                        new SendPlayerStats(sender, (Player) sender));
            } else {
                MobStats.getInstance().sendPrefixed(sender, Language.MSG.MSG_NOSTATS.toString());
            }
            return;
        }
        if (sender.hasPermission("mobstats.show")) {

            // /mobstats [player] - show player's kill stats

            final OfflinePlayer player = NameHandler.findPlayer(args[1]);

            if (player == null || player.getPlayer() == null) {
                MobStats.getInstance().sendPrefixed(sender, Language.MSG.INFO_PLAYERNOTFOUND.parse(args[1]));
                return;
            }
            Bukkit.getScheduler().runTaskAsynchronously(
                    MobStats.getInstance(), new SendPlayerStats(sender, player.getPlayer()));
        } else {
            MobStats.getInstance().sendPrefixed(sender, Language.MSG.MSG_NOPERMSHOW.toString());
        }
    }

    private boolean isVanished(Player p) {
        for (MetadataValue meta : p.getMetadata("vanished")) {
            if (meta.asBoolean()) {
                return true;
            }
        }
        return false;
    }

    public List<String> completeTab(String[] args) {
        List<String> results = new ArrayList<>();

        if (args.length < 2 || args[1].equals("")) {
            // list first argument possibilities
            for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                if (!isVanished(p)) {
                    results.add(p.getName());
                }
            }
            return results;
        }

        if (args.length > 2) {
            return results; // don't go too far!
        }

        // we started typing!
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            if (!isVanished(p)) {
                addIfMatches(results, p.getName(), args[1]);
            }
        }
        return results;
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("show");
    }

    @Override
    public List<String> getShort() {
        return Collections.singletonList("!sh");
    }

    @Override
    public String getShortInfo() {
        return "/mobstats - show your kill stats";
    }
}
