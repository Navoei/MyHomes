package me.navoei.myhomes.commands.player;

import me.navoei.myhomes.MyHomes;
import me.navoei.myhomes.language.Lang;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DeleteHomeCommand implements CommandExecutor, TabCompleter {

    MyHomes plugin = MyHomes.getInstance();
    BukkitScheduler scheduler = plugin.getServer().getScheduler();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("myhomes.deletehome")) {
            sender.sendMessage(Lang.PREFIX.toString() + Lang.NO_PERMISSION);
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(Lang.PREFIX.toString() + Lang.PLAYER_ONLY);
            return true;
        }

        Player player = (Player) sender;

        if (args.length > 1) {
            player.sendMessage(Lang.PREFIX.toString() + Lang.TOO_MANY_ARGUMENTS);
            return true;
        }

        if (args.length == 1) {

            plugin.getRDatabase().getHomeList(player).thenAccept(result_homeList -> {

                if (!result_homeList.toString().toLowerCase().contains(args[0].toLowerCase())) {
                    player.sendMessage(Lang.PREFIX.toString() + Lang.HOME_NOT_EXISTS);
                    return;
                }

                plugin.getRDatabase().getHomeInfo(player, args[0]).thenAccept(result_homeInfo -> {
                    String homeName = result_homeInfo.get(0);

                    if (homeName.isEmpty()) {
                        player.sendMessage(Lang.PREFIX.toString() + Lang.HOME_NOT_EXISTS);
                        return;
                    }

                    scheduler.runTaskAsynchronously(plugin, () -> {
                        plugin.getRDatabase().deleteHome(player, homeName);
                        plugin.getRDatabase().deleteAllInviteColumns(player, homeName);
                    });

                    if (homeName.equalsIgnoreCase("home")) {
                        player.sendMessage(Lang.PREFIX.toString() + Lang.HOME_DELETED);
                    } else {
                        player.sendMessage(Lang.PREFIX + Lang.HOME_DELETED_SPECIFIED.toString().replace("%home%", homeName));
                    }

                });

            });

            return true;
        }

        plugin.getRDatabase().getHomeList(player).thenAccept(result_homeList -> {
            if (!result_homeList.toString().toLowerCase().contains("home")) {
                player.sendMessage(Lang.PREFIX.toString() + Lang.HOME_NOT_EXISTS);
                return;
            }

            plugin.getRDatabase().getHomeInfo(player, "Home").thenAccept(result_homeInfo -> {
                String homeName = result_homeInfo.get(0);

                if (homeName.isEmpty()) {
                    player.sendMessage(Lang.PREFIX.toString() + Lang.HOME_NOT_EXISTS);
                    return;
                }

                scheduler.runTaskAsynchronously(plugin, () -> {
                    plugin.getRDatabase().deleteHome(player, homeName);
                    plugin.getRDatabase().deleteAllInviteColumns(player, homeName);
                });

                player.sendMessage(Lang.PREFIX.toString() + Lang.HOME_DELETED);

            });

        });

        return true;

    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("myhomes.deletehome")) {
            return null;
        }

        if (!(sender instanceof Player)) {
            return null;
        }

        Player player = (Player) sender;

        List<String> homeList = plugin.getRDatabase().getHomeList(player).join();
        List<String> tabCompletions = new ArrayList<>();

        if (!homeList.isEmpty() && args.length == 1) {
            StringUtil.copyPartialMatches(args[0], homeList, tabCompletions);
            Collections.sort(tabCompletions);
            return tabCompletions;
        }

        return null;

    }

}
