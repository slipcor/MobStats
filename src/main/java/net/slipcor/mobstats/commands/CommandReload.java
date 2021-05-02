package net.slipcor.mobstats.commands;

import net.slipcor.core.CoreCommand;
import net.slipcor.core.CorePlugin;
import net.slipcor.mobstats.MobStats;
import net.slipcor.mobstats.api.DatabaseAPI;
import net.slipcor.mobstats.display.SignDisplay;
import net.slipcor.mobstats.yml.Language;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandReload extends CoreCommand {
    public CommandReload(CorePlugin plugin) {
        super(plugin, "mobstats.reload", Language.MSG.ERROR_INVALID_ARGUMENT_COUNT);
    }

    @Override
    public void commit(CommandSender sender, String[] args) {
        if (!hasPerms(sender)) {
            MobStats.getInstance().sendPrefixed(sender, Language.MSG.MSG_NOPERMRELOAD.toString());
            return;
        }

        MobStats.getInstance().reloadConfig();
        MobStats.getInstance().loadConfig();
        MobStats.getInstance().loadCommands();
        String error = MobStats.getInstance().loadLanguage();
        if (error != null) {
            MobStats.getInstance().sendPrefixed(sender, ChatColor.RED + error);
            return;
        }
        MobStats.getInstance().reloadStreaks();
        MobStats.getInstance().sendPrefixed(sender, Language.MSG.MSG_RELOADED.parse());

        DatabaseAPI.refresh();

        SignDisplay.loadAllDisplays();
    }

    public List<String> completeTab(String[] args) {
        return new ArrayList<>(); // we have no arguments
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("reload");
    }

    @Override
    public List<String> getShort() {
        return Collections.singletonList("!r");
    }

    @Override
    public String getShortInfo() {
        return "/mobstats reload - reload the configs";
    }
}
