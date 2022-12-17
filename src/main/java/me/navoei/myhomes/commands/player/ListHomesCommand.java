package me.navoei.myhomes.commands.player;

import me.navoei.myhomes.MyHomes;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ListHomesCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can execute this command!");
            return true;
        }

        if (args.length >= 1) {
            sender.sendMessage("Too many arguments!");
            return true;
        }

        Player player = (Player) sender;

        sender.sendMessage(MyHomes.getInstance().getRDatabase().getHomeList(player).toString());
        return false;
    }
}
