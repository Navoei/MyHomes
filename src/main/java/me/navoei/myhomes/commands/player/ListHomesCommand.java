package me.navoei.myhomes.commands.player;

import me.navoei.myhomes.MyHomes;
import me.navoei.myhomes.language.Lang;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class ListHomesCommand implements CommandExecutor {

    MyHomes plugin = MyHomes.getInstance();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("myhomes.listhomes")) {
            sender.sendMessage(Lang.PREFIX.toString() + Lang.NO_PERMISSION);
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can execute this command!");
            return true;
        }

        if (args.length >= 1) {
            sender.sendMessage(Lang.PREFIX.toString() + Lang.TOO_MANY_ARGUMENTS);
            return true;
        }

        Player player = (Player) sender;

        plugin.getRDatabase().getHomeList(player).thenAccept(result_homeList -> {
            if (result_homeList.isEmpty()) {
                player.sendMessage(Lang.PREFIX.toString() + Lang.NO_HOMES);
                return;
            }

            String homesList = result_homeList.toString().substring(1, result_homeList.toString().length()-1);
            List<String> messageList = plugin.getLang().getStringList("listhomes");

            for (String message : messageList) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', message.replace("%homes_list%", homesList)));
            }

        });

        return false;
    }
}
