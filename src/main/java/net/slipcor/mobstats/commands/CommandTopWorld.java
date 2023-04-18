package net.slipcor.mobstats.commands;

import net.slipcor.core.CoreCommand;
import net.slipcor.core.CorePlugin;
import net.slipcor.mobstats.MobStats;
import net.slipcor.mobstats.yml.Language;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandTopWorld extends CoreCommand {
    public CommandTopWorld(CorePlugin plugin) {
        super(plugin, "mobstats.topworld", Language.MSG.COMMAND_ARGUMENT_COUNT_INVALID);
    }

    @Override
    public void commit(final CommandSender sender, final String[] args) {
        if (!hasPerms(sender)) {
            MobStats.getInstance().sendPrefixed(sender, Language.MSG.NO_PERMISSION_TOPPLUS.toString());
            return;
        }

        // /mobstats topworld [type] [world] [days]

        if (args.length > 3) {
            int days;

            try {
                days = Integer.parseInt(args[3]);
            } catch (Exception e) {
                MobStats.getInstance().sendPrefixed(sender, Language.MSG.COMMAND_ARGUMENT_INVALID_NUMBER.parse(args[2]));
                return;
            }

            World world = (sender instanceof Player) ? ((Player) sender).getWorld() : null;
            for (World w : Bukkit.getServer().getWorlds()) {
                if (w.getName().equalsIgnoreCase(args[2])) {
                    world = w;
                    break;
                }
            }

            if (world == null) {
                MobStats.getInstance().sendPrefixed(sender, Language.MSG.COMMAND_TOPWORLD_INVALID_WORLD.parse(args[2]));
                return;
            }

            int amount = 10;

            if (args.length > 4) {
                // /mobstats topworld [type] [world] [days] [amount] - show the top [amount] players of the last [days] days in world [world]
                try {
                    amount = Integer.parseInt(args[4]);
                } catch (Exception e2) {
                    MobStats.getInstance().sendPrefixed(sender, Language.MSG.COMMAND_ARGUMENT_INVALID_NUMBER.parse(args[4]));
                    amount = 10;
                }
            }

            if (args[1].equalsIgnoreCase("kills")) {
                Bukkit.getScheduler().runTaskAsynchronously(MobStats.getInstance(), new SendPlayerTopWorld(sender, "KILLS", world, amount, days));
            } else if (args[1].equalsIgnoreCase("deaths")) {
                Bukkit.getScheduler().runTaskAsynchronously(MobStats.getInstance(), new SendPlayerTopWorld(sender, "DEATHS", world, amount, days));
            } else if (args[1].equalsIgnoreCase("ratio")) {
                Bukkit.getScheduler().runTaskAsynchronously(MobStats.getInstance(), new SendPlayerTopWorld(sender, "K-D", world, amount, days));
            } else {
                return;
            }
        }
    }

    public List<String> completeTab(String[] args) {
        List<String> results = new ArrayList<>();

        if (args.length < 2 || args[1].equals("")) {
            // list first argument possibilities
            results.add("kills");
            results.add("deaths");
            results.add("ratio");
            return results;
        }

        if (args.length > 2) {
            return results; // don't go too far!
        }

        // we started typing!
        addIfMatches(results, "kills", args[1].toLowerCase());
        addIfMatches(results, "deaths", args[1].toLowerCase());
        addIfMatches(results, "ratio", args[1].toLowerCase());

        return results;
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("topplus");
    }

    @Override
    public List<String> getShort() {
        return Collections.singletonList("!tp");
    }

    @Override
    public String getShortInfo() {
        return "/mobstats topplus [type] [days] - show the top 10 players of given type, in the last [days] days\n" +
                "/mobstats topplus [type] [days] [amount] - show the top [amount] players of the given type, in the last [days] days";
    }
}
