package me.navoei.myhomes.commands.admin;

import me.navoei.myhomes.MyHomes;
import me.navoei.myhomes.language.Lang;
import me.navoei.myhomes.uuid.Fetcher;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.List;

public class ListPlayerHomesCommand implements CommandExecutor {

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

        uuidFetcher.getOfflinePlayerUUID(args[0])
        .thenCompose(result_playerUUID -> plugin.getDatabase().getHomeListUsingHomeownerUUIDAsynchronously(result_playerUUID))
        .thenAccept(result_homeList -> {
            String playerName = args[0];
            if (result_homeList.isEmpty()) {
                sender.sendMessage(Lang.PREFIX + Lang.PLAYER_NO_HOMES.toString().replace("%player%", playerName));
                return;
            }

            String homesList = result_homeList.toString().substring(1, result_homeList.toString().length()-1);

            List<String> messageList = plugin.getLang().getStringList("listplayerhomes");

            for (String message : messageList) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message.replace("%player%", playerName).replace("%homes_list%", homesList)));
            }
        });
        return false;
    }
}
