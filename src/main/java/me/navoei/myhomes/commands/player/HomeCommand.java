package me.navoei.myhomes.commands.player;

import me.navoei.myhomes.MyHomes;
import me.navoei.myhomes.language.Lang;
import me.navoei.myhomes.uuid.Fetcher;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class HomeCommand implements CommandExecutor, TabCompleter {

    Fetcher uuidFetcher = new Fetcher();
    MyHomes plugin = MyHomes.getInstance();
    BukkitScheduler scheduler = plugin.getServer().getScheduler();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("myhomes.home")) {
            sender.sendMessage(Lang.PREFIX.toString() + Lang.NO_PERMISSION);
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(Lang.PREFIX.toString() + Lang.PLAYER_ONLY);
            return true;
        }

        Player player = (Player) sender;

        if (args.length > 2) {
            player.sendMessage(Lang.PREFIX.toString() + Lang.TOO_MANY_ARGUMENTS);
            return true;
        }

        plugin.getRDatabase().getHomeList(player).thenAccept(result_homeList -> {
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
            String exceededHomes = Lang.PREFIX + Lang.TOO_MANY_HOMES.toString().replace("%maximum_number_of_homes%", Integer.toString(maxHomes.get()));
            if (result_homeList.size() > maxHomes.get() && !sender.hasPermission("myhomes.maxhomebypass")) {
                player.sendMessage(exceededHomes);
                return;
            }

            if (args.length>0 && result_homeList.stream().noneMatch(args[0]::equalsIgnoreCase)) {

                uuidFetcher.getOfflinePlayerUUIDFromMojang(args[0]).thenAccept(result_homeownerUUID -> {

                    if (args.length == 1) {

                        plugin.getRDatabase().getHomeUsingHomeownerUUID(result_homeownerUUID, "Home").thenAccept(result_home -> plugin.getRDatabase().getHomeInvitedPlayersAsync(result_homeownerUUID, "Home").thenAccept(result_homeInvitedPlayers -> plugin.getRDatabase().getHomePrivacyStatus(result_homeownerUUID, "Home").thenAccept(result_homePrivacyStatus -> scheduler.runTask(plugin, () -> {
                            if (result_home.isEmpty()) {
                                player.sendMessage(Lang.PREFIX.toString() + Lang.HOME_NOT_EXISTS);
                                return;
                            }

                            if (result_homeInvitedPlayers.stream().anyMatch(player.getName()::equalsIgnoreCase) || result_homePrivacyStatus.stream().anyMatch("public"::equalsIgnoreCase)) {
                                World world = MyHomes.getInstance().getServer().getWorld(result_home.get(0));
                                Location homeLocation = new Location(world, Double.parseDouble(result_home.get(1)), Double.parseDouble(result_home.get(2)), Double.parseDouble(result_home.get(3)), Float.parseFloat(result_home.get(4)), Float.parseFloat(result_home.get(5)));

                                player.teleport(homeLocation);
                                player.sendMessage(Lang.PREFIX + Lang.HOME_OTHER.toString().replace("%player%", args[0]));
                            } else if (result_homeInvitedPlayers.stream().noneMatch(player.getName()::equalsIgnoreCase)) {
                                player.sendMessage(Lang.PREFIX + Lang.PLAYER_NOT_INVITED.toString().replace("%player%", args[0]));
                            }

                        }))));
                    }

                    if (args.length == 2) {

                        String homeName = args[1];

                        if (!homeName.matches("[a-zA-Z0-9]*")) {
                            player.sendMessage(Lang.PREFIX.toString() + Lang.INVALID_CHARACTERS);
                            return;
                        }

                        plugin.getRDatabase().getHomeUsingHomeownerUUID(result_homeownerUUID, homeName).thenAccept(result_home -> plugin.getRDatabase().getHomeInvitedPlayersAsync(result_homeownerUUID, homeName).thenAccept(result_homeInvitedPlayers -> plugin.getRDatabase().getHomePrivacyStatus(result_homeownerUUID, homeName).thenAccept(result_homePrivacyStatus -> scheduler.runTask(plugin, () -> {
                            if (result_home.isEmpty()) {
                                player.sendMessage(Lang.PREFIX.toString() + Lang.HOME_NOT_EXISTS);
                                return;
                            }

                            if (result_homePrivacyStatus.stream().anyMatch("public"::equalsIgnoreCase) || result_homeInvitedPlayers.stream().anyMatch(player.getName()::equalsIgnoreCase)) {
                                World world = plugin.getServer().getWorld(result_home.get(0));
                                Location homeLocation = new Location(world, Double.parseDouble(result_home.get(1)), Double.parseDouble(result_home.get(2)), Double.parseDouble(result_home.get(3)), Float.parseFloat(result_home.get(4)), Float.parseFloat(result_home.get(5)));

                                player.teleport(homeLocation);
                                if (homeName.equalsIgnoreCase("home")) {
                                    player.sendMessage(Lang.PREFIX + Lang.HOME_OTHER.toString().replace("%player%", args[0]));
                                } else {
                                    player.sendMessage(Lang.PREFIX + Lang.SPECIFIED_HOME.toString().replace("%home%", homeName));
                                }
                            } else if (result_homeInvitedPlayers.stream().noneMatch(player.getName()::equalsIgnoreCase)) {
                                if (homeName.equalsIgnoreCase("home")) {
                                    player.sendMessage(Lang.PREFIX + Lang.PLAYER_NOT_INVITED.toString().replace("%player%", args[0]));
                                } else {
                                    player.sendMessage(Lang.PREFIX + Lang.PLAYER_NOT_INVITED_SPECIFIED.toString().replace("%home%", homeName));
                                }
                            }

                        }))));

                    }

                });

            } else {
                if (args.length == 0) {

                    plugin.getRDatabase().getHome(player, "Home").thenAccept(result_home -> scheduler.runTask(plugin, () -> {
                        if (result_home.isEmpty()) {
                            player.sendMessage(Lang.PREFIX.toString() + Lang.PLAYER_HAS_NO_HOME);
                        } else {
                            World world = plugin.getServer().getWorld(result_home.get(0));
                            Location homeLocation = new Location(world, Double.parseDouble(result_home.get(1)), Double.parseDouble(result_home.get(2)), Double.parseDouble(result_home.get(3)), Float.parseFloat(result_home.get(4)), Float.parseFloat(result_home.get(5)));

                            player.teleport(homeLocation);
                            player.sendMessage(Lang.PREFIX.toString() + Lang.HOME);
                        }
                    }));

                } else if (args.length == 1) {

                    String homeName = args[0];

                    if (!homeName.matches("[a-zA-Z0-9]*")) {
                        player.sendMessage(Lang.PREFIX.toString() + Lang.HOME_NOT_EXISTS);
                        return;
                    }

                    plugin.getRDatabase().getHome(player, homeName).thenAccept(result_home -> scheduler.runTask(plugin, () -> {
                        if (result_home.isEmpty()) {
                            player.sendMessage(Lang.PREFIX.toString() + Lang.HOME_NOT_EXISTS);
                        } else {
                            World world = plugin.getServer().getWorld(result_home.get(0));
                            Location homeLocation = new Location(world, Double.parseDouble(result_home.get(1)), Double.parseDouble(result_home.get(2)), Double.parseDouble(result_home.get(3)), Float.parseFloat(result_home.get(4)), Float.parseFloat(result_home.get(5)));
                            player.teleport(homeLocation);

                            if (homeName.equalsIgnoreCase("Home")) {
                                player.sendMessage(Lang.PREFIX.toString() + Lang.HOME);
                            } else {
                                player.sendMessage(Lang.PREFIX + Lang.SPECIFIED_HOME.toString().replace("%home%", homeName));
                            }
                        }
                    }));

                } else {
                    scheduler.runTask(plugin, () -> player.sendMessage(Lang.PREFIX.toString() + Lang.HOME_NOT_EXISTS));
                }
            }
        });
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("myhomes.home")) {
            return null;
        }

        if (!(sender instanceof Player)) {
            return null;
        }

        Player player = (Player) sender;

        List<String> homesList = plugin.getRDatabase().getHomeList(player).join();
        homesList.addAll(plugin.getRDatabase().getHomeInviteList(player.getUniqueId().toString()).join().keySet());
        homesList.removeAll(Collections.singletonList(null));

        HashMap<String, ArrayList<String>> inviteList = plugin.getRDatabase().getHomeInviteList(player.getUniqueId().toString()).join();

        List<String> tabCompletions = new ArrayList<>();

        if (!homesList.isEmpty() && args.length == 1) {
            StringUtil.copyPartialMatches(args[0], homesList, tabCompletions);
            Collections.sort(tabCompletions);
            return tabCompletions;
        }

        if (!args[0].isEmpty() && args.length == 2 && inviteList.containsKey(args[0])) {
            List<String> invitedHomesList = inviteList.get(args[0]);
            StringUtil.copyPartialMatches(args[1], invitedHomesList, tabCompletions);
            Collections.sort(tabCompletions);
            return tabCompletions;
        }

        return null;
    }

}

