package net.slipcor.mobstats.runnables;

import net.slipcor.mobstats.api.DatabaseAPI;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SendPlayerStats implements Runnable {
    private final Player infoPlayer;
    private final CommandSender inquirer;

    public SendPlayerStats(CommandSender inquirer, Player infoPlayer) {
        this.infoPlayer = infoPlayer;
        this.inquirer = inquirer;
    }

    @Override
    public void run() {
        final String[] info = DatabaseAPI.info(infoPlayer);
        inquirer.sendMessage(info);
    }
}
