package me.navoei.myhomes.commands.player;

import me.navoei.myhomes.MyHomes;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.concurrent.ExecutionException;

public class SetHomeCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length > 1) {
            player.sendMessage(ChatColor.RED + "Too many arguments!");
            return true;
        }



        if (args.length == 1) {

            try {
                if (!MyHomes.getInstance().getRDatabase().getHomeInfo(player, args[0]).get().isEmpty()) {
                    String homeName = MyHomes.getInstance().getRDatabase().getHomeInfo(player, args[0]).get().get(0);
                    MyHomes.getInstance().getRDatabase().updateHomeLocation(player, homeName);
                    player.sendMessage("Your home has been updated with a new location.");
                    return true;
                }
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }

            if (args[0].equalsIgnoreCase("home")) {
                MyHomes.getInstance().getRDatabase().setHomeColumns(player, "Home", false);
                player.sendMessage("Your home has been set.");
                return true;
            }

            MyHomes.getInstance().getRDatabase().setHomeColumns(player, args[0], false);
            player.sendMessage("Your home has been set.");
            return true;
        }

        try {
            if (!MyHomes.getInstance().getRDatabase().getHomeInfo(player, "Home").get().isEmpty()) {
                MyHomes.getInstance().getRDatabase().updateHomeLocation(player, "Home");
                player.sendMessage("Your home has been updated with a new location.");
                return true;
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        MyHomes.getInstance().getRDatabase().setHomeColumns(player, "Home", false);
        player.sendMessage("Your home has been set.");
        return false;
    }
}
