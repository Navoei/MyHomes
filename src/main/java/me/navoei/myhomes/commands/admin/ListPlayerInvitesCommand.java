package me.navoei.myhomes.commands.admin;

import me.navoei.myhomes.MyHomes;
import me.navoei.myhomes.uuid.Fetcher;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ListPlayerInvitesCommand implements CommandExecutor {

    Fetcher uuidFetcher = new Fetcher();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length >= 2) {
            sender.sendMessage("Too many arguments!");
            return true;
        }
        if (args.length < 1 ) {
            sender.sendMessage("Not enough arguments.");
            return true;
        }

        sender.sendMessage(MyHomes.getInstance().getRDatabase().getHomeInviteList(uuidFetcher.getOfflinePlayerUUID(args[0]).join()).join().toString());

        return true;
    }
}
