package net.slipcor.mobstats.commands;

import net.slipcor.core.CoreCommand;
import net.slipcor.core.CorePlugin;
import net.slipcor.mobstats.MobStats;
import net.slipcor.mobstats.runnables.SendPlayerTop;
import net.slipcor.mobstats.yml.Language;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandTop extends CoreCommand {
    public CommandTop(CorePlugin plugin) {
        super(plugin, "mobstats.top", Language.MSG.ERROR_INVALID_ARGUMENT_COUNT);
    }

    @Override
    public void commit(final CommandSender sender, final String[] args) {
        if (!hasPerms(sender)) {
            MobStats.getInstance().sendPrefixed(sender, Language.MSG.MSG_NOPERMTOP.toString());
            return;
        }

        int legacyTop = 0;

        try {
            legacyTop = Integer.parseInt(args[0]);
        } catch (Exception e) {

        }

        if ((args[0].equals("top") || legacyTop > 0)) {

            if (args.length > 1) {
                int amount = -1;

                try {
                    amount = Integer.parseInt(args[1]);
                } catch (Exception e) {


                    if (args.length > 2) {
                        // /mobstats top [type] [amount] - show the top [amount] players of the type
                        try {
                            amount = Integer.parseInt(args[2]);
                        } catch (Exception e2) {
                            amount = 10;
                        }
                    }

                    //   /mobstats top [type] - show the top 10 players of the type
                    if (amount == -1) {
                        amount = 10;
                    }

                    if (args[1].equalsIgnoreCase("kills")) {
                        Bukkit.getScheduler().runTaskAsynchronously(MobStats.getInstance(), new SendPlayerTop(sender, "KILLS", amount));
                    } else if (args[1].equalsIgnoreCase("deaths")) {
                        Bukkit.getScheduler().runTaskAsynchronously(MobStats.getInstance(), new SendPlayerTop(sender, "DEATHS", amount));
                    } else if (args[1].equalsIgnoreCase("streak")) {
                        Bukkit.getScheduler().runTaskAsynchronously(MobStats.getInstance(), new SendPlayerTop(sender, "STREAK", amount));
                    } else {
                        return;
                    }

                    return;
                }
                //   /mobstats top [amount] - show the top [amount] players (K-D)
                args[0] = args[1];
                legacyTop = 1;
            }

            // /mobstats [amount] - show the top [amount] players (K-D)
            try {
                int count = legacyTop == 0 ? 10 : Integer.parseInt(args[0]);
                if (count > 20) {
                    count = 20;
                }
                if (legacyTop == 0) {
                    args[0] = String.valueOf(count);
                }
                Bukkit.getScheduler().runTaskAsynchronously(MobStats.getInstance(), new SendPlayerTop(sender, "K-D", count, args[0]));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public List<String> completeTab(String[] args) {
        List<String> results = new ArrayList<>();

        if (args.length < 2 || args[1].equals("")) {
            // list first argument possibilities
            results.add("kills");
            results.add("deaths");
            results.add("streak");
            return results;
        }

        if (args.length > 2) {
            return results; // don't go too far!
        }

        // we started typing!
        addIfMatches(results, "kills", args[1].toLowerCase());
        addIfMatches(results, "deaths", args[1].toLowerCase());
        addIfMatches(results, "streak", args[1].toLowerCase());

        return results;
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("top");
    }

    @Override
    public List<String> getShort() {
        return Collections.singletonList("!t");
    }

    @Override
    public String getShortInfo() {
        return "/mobstats top [amount] - show the top [amount] players (K-D)\n" +
                "/mobstats top [type] - show the top 10 players of the type\n" +
                "/mobstats top [type] [amount] - show the top [amount] players of the type";
    }
}
