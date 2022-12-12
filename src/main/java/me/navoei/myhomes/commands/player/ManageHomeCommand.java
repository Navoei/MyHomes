package me.navoei.myhomes.commands.player;

import me.navoei.myhomes.MyHomes;
import me.navoei.myhomes.uuid.Fetcher;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ManageHomeCommand implements CommandExecutor {

    Fetcher uuidFetcher = new Fetcher();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can execute this command!");
            return true;
        }

        if(args.length == 0) {
            sender.sendMessage("Not enough arguments!");
            return true;
        }
        if (args.length > 3) {
            sender.sendMessage("Too many arguments!");
            return true;
        }

        Player player = (Player) sender;

        if (args[1].equalsIgnoreCase("invite")) {
            if (uuidFetcher.checkPlayedBefore(args[2])) {
                MyHomes.getInstance().getRDatabase().setInviteColumns(player, args[0], uuidFetcher.getOfflinePlayerUUID(args[2]));
            } else {
                player.sendMessage("This player has never logged on before.");
            }
            return true;
        } else {
            player.sendMessage("Invalid arguments.");
        }

        return false;
    }
}
