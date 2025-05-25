package me.navoei.myhomes.commands.player;

import me.navoei.myhomes.MyHomes;
import me.navoei.myhomes.language.Lang;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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

        plugin.getDatabase().getHomeList(player).thenAccept(result_homeList -> {

            AtomicInteger maxHomes = new AtomicInteger(plugin.getConfig().getInt("maximumhomes"));
            List<PermissionAttachmentInfo> effectivePermissions = player.getEffectivePermissions().stream().toList();
            effectivePermissions.forEach(permissionAttachmentInfo -> {
               String permission = permissionAttachmentInfo.getPermission().toLowerCase();
               if (permission.startsWith("myhomes.maximumhomes.")) {
                   int maxHomesPermission = Integer.parseInt(permission.substring(21));
                   if (maxHomesPermission > maxHomes.get()) {
                       maxHomes.set(maxHomesPermission);
                   }
               }
            });

            int characterLimit = plugin.getConfig().getInt("characterlimit");

            String exceededHomes = Lang.PREFIX + Lang.TOO_MANY_HOMES.toString().replace("%maximum_number_of_homes%", Integer.toString(maxHomes.get()));

            if (args.length == 1) {

                if (args[0].length() > characterLimit) {
                    player.sendMessage(Lang.PREFIX + Lang.TOO_MANY_CHARACTERS.toString().replace("%character_limit%", Integer.toString(characterLimit)));
                    return;
                }

                if (!args[0].matches("[a-zA-Z0-9]*")) {
                    player.sendMessage(Lang.PREFIX.toString() + Lang.INVALID_CHARACTERS);
                    return;
                }

                if (result_homeList.size() >= maxHomes.get() && result_homeList.stream().noneMatch(args[0]::equalsIgnoreCase) && !sender.hasPermission("myhomes.maxhomebypass")) {
                    player.sendMessage(exceededHomes);
                    return;
                }

                plugin.getDatabase().getHomeInfo(player, args[0]).thenAccept(result_homeInfo -> {

                    if (!result_homeInfo.isEmpty()) {
                        String homeName = result_homeInfo.get(0);
                        plugin.getDatabase().updateHomeLocation(player, homeName);

                        if (homeName.equalsIgnoreCase("Home")) {
                            player.sendMessage(Lang.PREFIX.toString() + Lang.HOME_UPDATED);
                        } else {
                            player.sendMessage(Lang.PREFIX + Lang.HOME_SPECIFIED_UPDATED.toString().replace("%home%", homeName));
                        }

                        return;
                    }

                    if (args[0].equalsIgnoreCase("Home")) {
                        plugin.getDatabase().setHomeColumns(player, "Home", false);
                        player.sendMessage(Lang.PREFIX.toString() + Lang.SET_HOME);
                    } else {
                        plugin.getDatabase().setHomeColumns(player, args[0], false);
                        player.sendMessage(Lang.PREFIX + Lang.SET_HOME_SPECIFIED.toString().replace("%home%", args[0]));
                    }

                });
                return;
            }

            if (result_homeList.size() >= maxHomes.get() && result_homeList.stream().noneMatch("home"::equalsIgnoreCase) && !player.hasPermission("myhomes.maxhomebypass")) {
                player.sendMessage(exceededHomes);
                return;
            }

            plugin.getDatabase().getHomeInfo(player, "Home").thenAccept(result_homeInfo -> {
                if (!result_homeInfo.isEmpty()) {
                    plugin.getDatabase().updateHomeLocation(player, "Home");
                    player.sendMessage(Lang.PREFIX.toString() + Lang.HOME_UPDATED);
                    return;
                }

                plugin.getDatabase().setHomeColumns(player, "Home", false);
                player.sendMessage(Lang.PREFIX.toString() + Lang.SET_HOME);

            });

        });
        return false;
    }
}
