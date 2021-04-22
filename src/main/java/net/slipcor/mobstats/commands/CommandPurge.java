package net.slipcor.mobstats.commands;

import net.slipcor.mobstats.MobStats;
import net.slipcor.mobstats.api.DatabaseAPI;
import net.slipcor.mobstats.core.Language;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandPurge extends AbstractCommand {
    public CommandPurge() {
        super(new String[]{"mobstats.purge"});
    }

    @Override
    public void commit(CommandSender sender, String[] args) {
        if (!hasPerms(sender)) {
            MobStats.getInstance().sendPrefixed(sender, Language.MSG_NOPERMPURGE.toString());
            return;
        }

        if (!argCountValid(sender, args, new Integer[]{2, 3})) {
            return;
        }

        int days = 30;

        if (args.length > 2) {
            try {
                days = Integer.parseInt(args[args.length - 1]);
            } catch (Exception e) {

            }
        }

        if (args.length > 1) {
            if (args[1].equalsIgnoreCase("specific")) {
                final int count = DatabaseAPI.purgeKillStats(days);
                MobStats.getInstance().sendPrefixed(sender, Language.MSG_PURGED.toString(String.valueOf(count)));
            } else if (args[1].equalsIgnoreCase("standard")) {
                final int count = DatabaseAPI.purgeStats(days);
                MobStats.getInstance().sendPrefixed(sender, Language.MSG_PURGED.toString(String.valueOf(count)));
            } else if (args[1].equalsIgnoreCase("all")) {
                final int count = DatabaseAPI.purgeKillStats(days) + DatabaseAPI.purgeStats(days);
                MobStats.getInstance().sendPrefixed(sender, Language.MSG_PURGED.toString(String.valueOf(count)));
            } else if (args[1].equalsIgnoreCase("mobs")) {
                final int count = DatabaseAPI.purgeMobStats(days);
                MobStats.getInstance().sendPrefixed(sender, Language.MSG_PURGED.toString(String.valueOf(count)));
            } else {
                MobStats.getInstance().sendPrefixed(sender, "/mobstats purge [specific | standard | mobs | all] [days]");
            }

            DatabaseAPI.refresh();
        } else {
            MobStats.getInstance().sendPrefixed(sender, "/mobstats purge [specific | standard | mobs | all] [days]");
        }
    }

    public List<String> completeTab(String[] args) {
        List<String> results = new ArrayList<>();

        if (args.length < 2 || args[1].equals("")) {
            // list first argument possibilities
            results.add("specific");
            results.add("standard");
            results.add("both");
            return results;
        }

        if (args.length > 2) {
            return results; // don't go too far!
        }

        // we started typing!
        addIfMatches(results, "specific", args[1].toLowerCase());
        addIfMatches(results, "standard", args[1].toLowerCase());
        addIfMatches(results, "both", args[1].toLowerCase());

        return results;
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("purge");
    }

    @Override
    public String getName() {
        return getClass().getName();
    }

    @Override
    public List<String> getShort() {
        return Collections.singletonList("!p");
    }

    @Override
    public String getShortInfo() {
        return "/mobstats purge [specific/standard/both] {days} - remove entries older than {days} (defaults to 30)";
    }
}
