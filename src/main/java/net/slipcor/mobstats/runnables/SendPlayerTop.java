package net.slipcor.mobstats.runnables;

import net.slipcor.mobstats.api.LeaderboardBuffer;
import net.slipcor.mobstats.yml.Language;
import org.bukkit.command.CommandSender;

public class SendPlayerTop implements Runnable {
    final CommandSender sender;
    final String name;
    final int amount;
    final String displayAmount;

    public SendPlayerTop(CommandSender sender, String name, int amount) {
        this.sender = sender;
        this.name = name;
        this.amount = amount;
        displayAmount = String.valueOf(amount);
    }

    public SendPlayerTop(CommandSender sender, String name, int amount, String displayAmount) {
        this.sender = sender;
        this.name = name;
        this.amount = amount;
        this.displayAmount = displayAmount;
    }

    @Override
    public void run() {
        String[] top = LeaderboardBuffer.top(amount, name);
        sender.sendMessage(Language.MSG.HEAD_LINE.toString());
        sender.sendMessage(Language.MSG.HEAD_HEADLINE.parse(
                displayAmount,
                Language.MSG.valueOf("HEAD_" + (name.equals("K-D") ? "RATIO" : name)).toString()));
        sender.sendMessage(Language.MSG.HEAD_LINE.toString());

        int pos = 1;
        for (String stat : top) {
            sender.sendMessage(Language.MSG.INFO_NUMBERS.parse(String.valueOf(pos++), stat));
        }
    }
}
