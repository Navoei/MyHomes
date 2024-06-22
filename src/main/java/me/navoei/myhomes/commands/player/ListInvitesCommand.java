package me.navoei.myhomes.commands.player;

import me.navoei.myhomes.MyHomes;
import me.navoei.myhomes.language.Lang;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListInvitesCommand implements CommandExecutor {

    MyHomes plugin = MyHomes.getInstance();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("myhomes.listinvites")) {
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

        plugin.getRDatabase().getHomeInviteList(player.getUniqueId().toString()).thenAccept(result_homeInviteList -> {
            if (result_homeInviteList.isEmpty()) {
                player.sendMessage(Lang.PREFIX.toString() + Lang.NO_INVITES);
                return;
            }

            List<String> messageList = plugin.getLang().getStringList("listinvites");

            for (String message : messageList) {
                if (message.contains("%homeowner%") || message.contains("%homes_list%")) {
                    for (Map.Entry<String, ArrayList<String>> entry : result_homeInviteList.entrySet()) {
                        String homeowner = entry.getKey();
                        String homesList = entry.getValue().toString().substring(1, entry.getValue().toString().length()-1);
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message.replace("%homeowner%", homeowner).replace("%homes_list%", homesList)));
                    }
                } else {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                }
            }

        });
        return false;
    }
}
