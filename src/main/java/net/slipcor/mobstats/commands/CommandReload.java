package net.slipcor.mobstats.commands;

import net.slipcor.mobstats.MobStats;
import net.slipcor.mobstats.api.DatabaseAPI;
import net.slipcor.mobstats.core.Language;
import net.slipcor.mobstats.display.SignDisplay;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandReload extends AbstractCommand {
    public CommandReload() {
        super(new String[]{"mobstats.reload"});
    }

    @Override
    public void commit(CommandSender sender, String[] args) {
        if (!hasPerms(sender)) {
            sender.sendMessage(Language.MSG_NOPERMRELOAD.toString());
            return;
        }

        MobStats.getInstance().reloadConfig();
        MobStats.getInstance().loadConfig();
        MobStats.getInstance().loadLanguage();
        MobStats.getInstance().reloadStreaks();
        MobStats.getInstance().sendPrefixed(sender, Language.MSG_RELOADED.toString());

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
    public String getName() {
        return getClass().getName();
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
