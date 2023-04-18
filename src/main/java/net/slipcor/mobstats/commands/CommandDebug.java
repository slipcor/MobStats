package net.slipcor.mobstats.commands;

import net.slipcor.core.CoreCommand;
import net.slipcor.core.CorePlugin;
import net.slipcor.mobstats.MobStats;
import net.slipcor.mobstats.yml.Language;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandDebug extends CoreCommand {
    public CommandDebug(CorePlugin plugin) {
        super(plugin, "mobstats.debug", Language.MSG.COMMAND_ARGUMENT_COUNT_INVALID);
    }

    @Override
    public void commit(CommandSender sender, String[] args) {
        if (!hasPerms(sender)) {
            MobStats.getInstance().sendPrefixed(sender, Language.MSG.NO_PERMISSION_DEBUG.toString());
            return;
        }
        if (!argCountValid(sender, args, new Integer[]{2})) {
            return;
        }
        MobStats.getInstance().destroyDebugger();
        if (args.length > 1) {
            MobStats.getInstance().getConfig().set("debug", args[1]);
        }

        MobStats.getInstance().loadDebugger("debug", sender);
    }

    public List<String> completeTab(String[] args) {
        List<String> results = new ArrayList<>();

        if (args.length < 2 || args[1].equals("")) {
            // list first argument possibilities
            results.add("on");
            results.add("off");
            results.add("all");
            results.add("none");
            results.add("true");
            results.add("false");
            return results;
        }

        if (args.length > 2) {
            return results; // don't go too far!
        }

        // we started typing!
        addIfMatches(results, "on", args[1].toLowerCase());
        addIfMatches(results, "off", args[1].toLowerCase());
        addIfMatches(results, "all", args[1].toLowerCase());
        addIfMatches(results, "none", args[1].toLowerCase());
        addIfMatches(results, "true", args[1].toLowerCase());
        addIfMatches(results, "false", args[1].toLowerCase());

        return results;
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("debug");
    }

    @Override
    public List<String> getShort() {
        return Collections.singletonList("!d");
    }

    @Override
    public String getShortInfo() {
        return "/mobstats debug [on/off] - activate or deactivate debugging";
    }
}
