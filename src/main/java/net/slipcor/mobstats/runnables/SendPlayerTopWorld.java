package net.slipcor.mobstats.runnables;

import net.slipcor.core.LanguageEntry;
import net.slipcor.mobstats.api.LeaderboardBuffer;
import net.slipcor.mobstats.yml.Language;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;

public class SendPlayerTopWorld implements Runnable {
    final CommandSender sender;
    final String name;
    final int amount;
    final int days;
    final String displayAmount;
    final World world;

    static Map<String, LanguageEntry> stringToEntry = new HashMap<>();

    static {
        stringToEntry.put("LINE", Language.MSG.STATISTIC_SEPARATOR);
        stringToEntry.put("KILLS", Language.MSG.STATISTIC_HEADLINE_KILLS);
        stringToEntry.put("DEATHS", Language.MSG.STATISTIC_HEADLINE_DEATHS);
        stringToEntry.put("RATIO", Language.MSG.STATISTIC_HEADLINE_RATIO);
        stringToEntry.put("K-D", Language.MSG.STATISTIC_HEADLINE_RATIO);
    }

    public SendPlayerTopWorld(CommandSender sender, String name, World world, int amount, int days) {
        this (sender, name, world, amount, days, String.valueOf(amount));
    }

    public SendPlayerTopWorld(CommandSender sender, String name, World world, int amount, int days, String displayAmount) {
        this.sender = sender;
        this.name = name;
        this.world = world;
        this.amount = amount;
        this.days = days;
        this.displayAmount = displayAmount;
    }

    @Override
    public void run() {
        String[] top = LeaderboardBuffer.topWorld(amount, name, world.getName(), days);
        sender.sendMessage(Language.MSG.STATISTIC_SEPARATOR.parse());
        sender.sendMessage(Language.MSG.STATISTIC_HEADLINE_TOPWORLD.parse(
                displayAmount,
                stringToEntry.get(name).parse(),
                world.getName()));
        sender.sendMessage(Language.MSG.STATISTIC_SEPARATOR.parse());

        int pos = 1;
        for (String stat : top) {
            sender.sendMessage(Language.MSG.STATISTIC_FORMAT_NUMBER.parse(String.valueOf(pos++), stat));
        }
    }
}
