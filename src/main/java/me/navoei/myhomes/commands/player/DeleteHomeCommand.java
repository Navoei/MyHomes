package me.navoei.myhomes.commands.player;

import me.navoei.myhomes.MyHomes;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class DeleteHomeCommand implements CommandExecutor, TabCompleter {
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

        String homeName;
        try {
            homeName = MyHomes.getInstance().getRDatabase().getHomeInfo(player, args[0]).get().get(0);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        if (homeName.isEmpty()) {
            player.sendMessage("This home does not exist.");
            return true;
        }

        MyHomes.getInstance().getRDatabase().deleteHome(player, homeName);
        MyHomes.getInstance().getRDatabase().deleteAllInviteColumns(player, homeName);

        if (homeName.equalsIgnoreCase("home")) {
            player.sendMessage("Your home has been deleted.");
        } else {
            player.sendMessage(homeName + " has been deleted.");
        }
        return true;

    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            return null;
        }

        Player player = (Player) sender;

        List<String> homeList;
        try {
            homeList = MyHomes.getInstance().getRDatabase().getHomeList(player).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        List<String> tabCompletions = new ArrayList<>();

        if (!homeList.isEmpty() && args.length == 1) {
            StringUtil.copyPartialMatches(args[0], homeList, tabCompletions);
            Collections.sort(tabCompletions);
            return tabCompletions;
        }

        return null;

    }

}
