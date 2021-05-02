package net.slipcor.mobstats.commands;

import net.slipcor.core.CoreCommand;
import net.slipcor.core.CorePlugin;
import net.slipcor.mobstats.MobStats;
import net.slipcor.mobstats.api.DatabaseAPI;
import net.slipcor.mobstats.api.EntityStatisticsBuffer;
import net.slipcor.mobstats.classes.NameHandler;
import net.slipcor.mobstats.yml.Language;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandSet extends CoreCommand {
    public CommandSet(CorePlugin plugin) {
        super(plugin, "mobstats.set", Language.MSG.ERROR_INVALID_ARGUMENT_COUNT);
    }

    @Override
    public void commit(final CommandSender sender, final String[] args) {
        if (!hasPerms(sender)) {
            MobStats.getInstance().sendPrefixed(sender, Language.MSG.MSG_NOPERMSET.toString());
            return;
        }


        if (!argCountValid(sender, args, new Integer[]{4})) {
            return;
        }

        // /mobstats set [player] [type] amount

        try {
            int amount = Integer.parseInt(args[3]);

            OfflinePlayer player =  NameHandler.findPlayer(args[1]);

            if (player != null && DatabaseAPI.hasEntry(player.getUniqueId())) {
                if (args[2].toLowerCase().equals("kills")) {
                    EntityStatisticsBuffer.setKills(player.getUniqueId(), amount);
                    DatabaseAPI.setSpecificStat(player.getUniqueId(), "kills", amount);
                } else if (args[2].toLowerCase().equals("deaths")) {
                    EntityStatisticsBuffer.setDeaths(player.getUniqueId(), amount);
                    DatabaseAPI.setSpecificStat(player.getUniqueId(), "deaths", amount);
                } else if (args[2].toLowerCase().equals("streak")) {
                    EntityStatisticsBuffer.setStreak(player.getUniqueId(), amount);
                    DatabaseAPI.setSpecificStat(player.getUniqueId(), "streak", amount);
                } else if (args[2].toLowerCase().equals("currentstreak")) {
                    EntityStatisticsBuffer.setMaxStreak(player.getUniqueId(), amount);
                    DatabaseAPI.setSpecificStat(player.getUniqueId(), "currentstreak", amount);
                } else {
                    sender.sendMessage(this.getShortInfo());
                    return;
                }

                MobStats.getInstance().sendPrefixed(sender, Language.MSG.MSG_SET.parse(args[2], args[1], String.valueOf(amount)));

                DatabaseAPI.refresh();
            } else {
                MobStats.getInstance().sendPrefixed(sender, Language.MSG.INFO_PLAYERNOTFOUND.parse(args[1]));
            }
        } catch (Exception e) {
            MobStats.getInstance().sendPrefixed(sender, Language.MSG.ERROR_INVALID_NUMBER.parse(args[3]));
        }
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

        if (args.length > 3) {
            return results; // don't go too far!
        }

        if (args.length < 3) {
            // first argument!
            for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                addIfMatches(results, p.getName(), args[1].toLowerCase());
            }
        } else {

            // second argument!
            addIfMatches(results, "kills", args[2].toLowerCase());
            addIfMatches(results, "deaths", args[2].toLowerCase());
            addIfMatches(results, "streak", args[2].toLowerCase());
            addIfMatches(results, "currentstreak", args[2].toLowerCase());
        }

        return results;
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("set");
    }

    @Override
    public List<String> getShort() {
        return Collections.singletonList("!st");
    }

    @Override
    public String getShortInfo() {
        return "/mobstats set [player] [type] [amount] - set a player's [type] statistic - valid types:\nkills, deaths, streak, currentstrak";
    }
}
