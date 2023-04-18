package net.slipcor.mobstats.runnables;

import net.slipcor.mobstats.api.LeaderboardBuffer;
import net.slipcor.mobstats.yml.Language;
import org.bukkit.command.CommandSender;

public class SendPlayerTop implements Runnable {
    final CommandSender sender;
    final String name;
    final int amount;
    final String displayAmount;
    final int offset;

    public SendPlayerTop(CommandSender sender, String name, int amount, int offset) {
        this (sender, name, amount, String.valueOf(amount), offset);
    }

    public SendPlayerTop(CommandSender sender, String name, int amount, String displayAmount, int offset) {
        this.sender = sender;
        this.name = name;
        this.amount = amount;
        this.displayAmount = displayAmount;
        this.offset = offset;
    }

    @Override
    public void run() {
        String[] top = LeaderboardBuffer.top(amount, name, offset);
        sender.sendMessage(Language.MSG.STATISTIC_SEPARATOR.toString());
        sender.sendMessage(Language.MSG.STATISTIC_HEADLINE_TOP.parse(
                displayAmount,
                Language.MSG.valueOf("HEAD_" + (name.equals("K-D") ? "RATIO" : name)).toString()));
        sender.sendMessage(Language.MSG.STATISTIC_SEPARATOR.toString());

        int pos = 1;
        for (String stat : top) {
            sender.sendMessage(Language.MSG.STATISTIC_FORMAT_NUMBER.parse(String.valueOf(pos++), stat));
        }
    }
}
