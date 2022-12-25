package me.navoei.myhomes.commands.admin;

import me.navoei.myhomes.MyHomes;
import me.navoei.myhomes.language.Lang;
import me.navoei.myhomes.uuid.Fetcher;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ListPlayerInvitesCommand implements CommandExecutor {

    MyHomes plugin = MyHomes.getInstance();
    Fetcher uuidFetcher = new Fetcher();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("myhomes.manageplayerhome")) {
            sender.sendMessage(Lang.PREFIX.toString() + Lang.NO_PERMISSION);
            return true;
        }

        if (args.length >= 2) {
            sender.sendMessage("Too many arguments!");
            return true;
        }
        if (args.length < 1 ) {
            sender.sendMessage("Not enough arguments.");
            return true;
        }

        uuidFetcher.getOfflinePlayerUUIDFromMojang(args[0]).thenAccept(result_playerUUID -> plugin.getRDatabase().getHomeInviteList(result_playerUUID).thenAccept(result_homeInviteList -> {
            String playerName = args[0];
            if (result_homeInviteList.isEmpty()) {
                sender.sendMessage(Lang.PREFIX + Lang.PLAYER_NO_INVITES.toString().replace("%player%", playerName));
                return;
            }

            List<String> messageList = plugin.getLang().getStringList("listplayerinvites");

            for (String message : messageList) {
                if (message.contains("%homeowner%") || message.contains("%homes_list%")) {
                    for (Map.Entry<String, ArrayList<String>> entry : result_homeInviteList.entrySet()) {
                        String homeowner = entry.getKey();
                        String homesList = entry.getValue().toString().substring(1, entry.getValue().toString().length()-1);
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message.replace("%homeowner%", homeowner).replace("%homes_list%", homesList)));
                    }
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message.replace("%player%", playerName)));
                }
            }

        }));

        return true;
    }
}
