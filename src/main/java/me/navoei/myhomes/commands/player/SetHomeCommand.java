package me.navoei.myhomes.commands.player;

import me.navoei.myhomes.MyHomes;
import me.navoei.myhomes.language.Lang;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

public class SetHomeCommand implements CommandExecutor {

    MyHomes plugin = MyHomes.getInstance();
    BukkitScheduler scheduler = plugin.getServer().getScheduler();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("myhomes.sethome")) {
            sender.sendMessage(Lang.PREFIX.toString() + Lang.NO_PERMISSION);
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length > 1) {
            player.sendMessage(ChatColor.RED + "Too many arguments!");
            return true;
        }

        plugin.getRDatabase().getHomeList(player).thenAccept(result_homeList -> {

            int maxHomes = plugin.getConfig().getInt("maximumhomes");
            int characterLimit = plugin.getConfig().getInt("characterlimit");

            String exceededHomes = Lang.PREFIX + Lang.TOO_MANY_HOMES.toString().replace("%maximum_number_of_homes%", Integer.toString(maxHomes));

            if (args.length == 1) {

                if (args[0].length() > characterLimit) {
                    player.sendMessage(Lang.PREFIX + Lang.TOO_MANY_CHARACTERS.toString().replace("%character_limit%", Integer.toString(characterLimit)));
                    return;
                }

                if (!args[0].matches("[a-zA-Z0-9]*")) {
                    player.sendMessage(Lang.PREFIX.toString() + Lang.INVALID_CHARACTERS);
                    return;
                }

                if (result_homeList.size() >= maxHomes && !result_homeList.toString().toLowerCase().contains(args[0].toLowerCase())) {
                    player.sendMessage(exceededHomes);
                    return;
                }

                plugin.getRDatabase().getHomeInfo(player, args[0]).thenAccept(result_homeInfo -> {

                    if (!result_homeInfo.isEmpty()) {
                        String homeName = result_homeInfo.get(0);
                        scheduler.runTaskAsynchronously(plugin, () -> plugin.getRDatabase().updateHomeLocation(player, homeName));

                        if (homeName.equalsIgnoreCase("Home")) {
                            player.sendMessage(Lang.PREFIX.toString() + Lang.HOME_UPDATED);
                        } else {
                            player.sendMessage(Lang.PREFIX + Lang.HOME_SPECIFIED_UPDATED.toString().replace("%home%", homeName));
                        }

                        return;
                    }

                    if (args[0].equalsIgnoreCase("Home")) {
                        scheduler.runTaskAsynchronously(plugin, () -> plugin.getRDatabase().setHomeColumns(player, "Home", false));
                        player.sendMessage(Lang.PREFIX.toString() + Lang.SET_HOME);
                    } else {
                        scheduler.runTaskAsynchronously(plugin, () -> plugin.getRDatabase().setHomeColumns(player, args[0], false));
                        player.sendMessage(Lang.PREFIX + Lang.SET_HOME_SPECIFIED.toString().replace("%home%", args[0]));
                    }

                });
                return;
            }

            if (result_homeList.size() >= maxHomes && !result_homeList.toString().toLowerCase().contains("home")) {
                player.sendMessage(exceededHomes);
                return;
            }

            plugin.getRDatabase().getHomeInfo(player, "Home").thenAccept(result_homeInfo -> {
                if (!result_homeInfo.isEmpty()) {
                    scheduler.runTaskAsynchronously(plugin, () -> plugin.getRDatabase().updateHomeLocation(player, "Home"));
                    player.sendMessage(Lang.PREFIX.toString() + Lang.HOME_UPDATED);
                    return;
                }

                scheduler.runTaskAsynchronously(plugin, () -> plugin.getRDatabase().setHomeColumns(player, "Home", false));
                player.sendMessage(Lang.PREFIX.toString() + Lang.SET_HOME);

            });

        });
        return false;
    }
}
