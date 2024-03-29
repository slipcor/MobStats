package net.slipcor.mobstats.commands;

import net.slipcor.core.CoreCommand;
import net.slipcor.core.CoreDebugger;
import net.slipcor.core.CorePlugin;
import net.slipcor.mobstats.MobStats;
import net.slipcor.mobstats.api.DatabaseAPI;
import net.slipcor.mobstats.classes.NameHandler;
import net.slipcor.mobstats.yml.Language;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandDebugKill extends CoreCommand {
    public CommandDebugKill(CorePlugin plugin) {
        super(plugin, "mobstats.debugkill", Language.MSG.COMMAND_ARGUMENT_COUNT_INVALID);
    }

    public static CoreDebugger debugger;

    @Override
    public void commit(CommandSender sender, String[] args) {
        debugger.i("debug kill!");
        if (!hasPerms(sender)) {
            MobStats.getInstance().sendPrefixed(sender, Language.MSG.NO_PERMISSION_DEBUG.toString());
            return;
        }
        if (!argCountValid(sender, args, new Integer[]{3})) {
            return;
        }
        debugger.i("let's go!");

        String attacker = args[1];
        String victim = args[2];

        OfflinePlayer offlineAttacker = Bukkit.getServer().getOfflinePlayer(attacker);
        OfflinePlayer offlineVictim = Bukkit.getServer().getOfflinePlayer(victim);

        DatabaseAPI.AkilledB(offlineAttacker.getPlayer(), offlineVictim.getPlayer());
    }

    public List<String> completeTab(String[] args) {
        List<String> results = new ArrayList<>();

        if (args.length < 2 || args[1].equals("")) {
            // list first argument possibilities
            for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                results.add(NameHandler.getRawPlayerName(p));
            }
            return results;
        }

        if (args.length > 3) {
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
        return Collections.singletonList("debugkill");
    }

    @Override
    public List<String> getShort() {
        return Collections.singletonList("!dk");
    }

    @Override
    public String getShortInfo() {
        return "/mobstats debugkill killer killed - add a manual kill";
    }
}
