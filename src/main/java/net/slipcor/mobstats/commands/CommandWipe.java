package net.slipcor.mobstats.commands;

import net.slipcor.mobstats.MobStats;
import net.slipcor.mobstats.api.DatabaseAPI;
import net.slipcor.mobstats.classes.NameHandler;
import net.slipcor.mobstats.core.Language;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandWipe extends AbstractCommand {
    public CommandWipe() {
        super(new String[]{"mobstats.wipe"});
    }

    @Override
    public void commit(CommandSender sender, String[] args) {
        if (!hasPerms(sender)) {
            sender.sendMessage(Language.MSG_NOPERMWIPE.toString());
            return;
        }

        if (args.length < 2) {
            DatabaseAPI.wipe(null);
            MobStats.getInstance().sendPrefixed(sender, Language.MSG_WIPED.toString());
        } else {
            OfflinePlayer player =  NameHandler.findPlayer(args[1]);

            if (player == null) {
                sender.sendMessage("Player not found: " + args[1]);
                return;
            }

            DatabaseAPI.wipe(player.getUniqueId());
            MobStats.getInstance().sendPrefixed(sender, Language.MSG_WIPEDFOR.toString(args[1]));
        }

        DatabaseAPI.refresh();
    }

    public List<String> completeTab(String[] args) {
        List<String> results = new ArrayList<>();

        if (args.length < 2 || args[1].equals("")) {
            // list first argument possibilities
            for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                results.add(p.getName());
            }
            return results;
        }

        if (args.length > 2) {
            return results; // don't go too far!
        }

        // we started typing!
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            addIfMatches(results, p.getName(), args[1]);
        }
        return results;
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("wipe");
    }

    @Override
    public String getName() {
        return getClass().getName();
    }

    @Override
    public List<String> getShort() {
        return Collections.singletonList("!w");
    }

    @Override
    public String getShortInfo() {
        return "/mobstats wipe {player} - wipe all/player statistics";
    }
}
