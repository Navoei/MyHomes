package me.navoei.myhomes.commands.player;

import me.navoei.myhomes.MyHomes;
import me.navoei.myhomes.language.Lang;
import me.navoei.myhomes.uuid.Fetcher;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ManageHomeCommand implements CommandExecutor, TabCompleter {

    MyHomes plugin = MyHomes.getInstance();
    BukkitScheduler scheduler = plugin.getServer().getScheduler();
    Fetcher uuidFetcher = new Fetcher();
    private final String[] SUB_COMMANDS = { "invite", "uninvite", "listinvites", "privacy", "info" };
    private final String[] PRIVACY_STATUS_OPTIONS = { "private", "public" };

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(Lang.PREFIX.toString() + Lang.PLAYER_ONLY);
            return true;
        }
        if(args.length <= 1) {
            sender.sendMessage(Lang.PREFIX.toString() + Lang.NOT_ENOUGH_ARGUMENTS);
            return true;
        }

        if (args.length > 3) {
            sender.sendMessage(Lang.PREFIX.toString() + Lang.TOO_MANY_ARGUMENTS);
            return true;
        }

        Player player = (Player) sender;

        if (plugin.getRDatabase().getHomeInfo(player, args[0]).join().isEmpty()) {
            player.sendMessage(Lang.PREFIX.toString() + Lang.HOME_NOT_EXISTS);
            return true;
        }

        String homeName = args[0];

        if (args[1].equalsIgnoreCase("invite")) {

            if (args.length != 3) {
                sender.sendMessage(Lang.PREFIX.toString() + Lang.INVALID_ARGUMENTS);
                return true;
            }

            String playerName = args[2];

            if (playerName.equalsIgnoreCase(player.getName())) {
                sender.sendMessage(Lang.PREFIX.toString() + Lang.CANNOT_INVITE_SELF);
                return true;
            }

            uuidFetcher.checkPlayedBefore(args[2]).thenAccept(result_playedBefore -> {

                if (result_playedBefore) {

                    plugin.getRDatabase().getHomeInvitedPlayers(player.getUniqueId().toString(), homeName).thenAccept(result_homeInvitedPlayers -> {

                        if (result_homeInvitedPlayers.toString().toLowerCase().contains(playerName.toLowerCase())) {
                            if (homeName.equalsIgnoreCase("Home")) {
                                scheduler.runTask(plugin, () -> player.sendMessage(Lang.PREFIX + Lang.ALREADY_INVITED_TO_DEFAULT_HOME.toString().replace("%player%", playerName)));
                            } else {
                                scheduler.runTask(plugin, () -> player.sendMessage(Lang.PREFIX + Lang.ALREADY_INVITED_TO_SPECIFIED_HOME.toString().replace("%player%", playerName).replace("%home%", homeName)));
                            }
                        } else {
                            if (homeName.equalsIgnoreCase("Home")) {
                                scheduler.runTaskAsynchronously(plugin, () -> plugin.getRDatabase().setInviteColumns(player, "Home", uuidFetcher.getOfflinePlayerUUIDFromMojang(playerName).join()));
                                scheduler.runTask(plugin, () -> player.sendMessage(Lang.PREFIX + Lang.INVITED_TO_DEFAULT_HOME.toString().replace("%player%", playerName)));
                            } else {
                                scheduler.runTaskAsynchronously(plugin, () -> plugin.getRDatabase().setInviteColumns(player, homeName, uuidFetcher.getOfflinePlayerUUIDFromMojang(playerName).join()));
                                scheduler.runTask(plugin, () -> player.sendMessage(Lang.PREFIX + Lang.INVITED_TO_SPECIFIED_HOME.toString().replace("%player%", playerName).replace("%home%", homeName)));
                            }
                        }

                    });

                } else {
                    scheduler.runTask(plugin, () -> player.sendMessage(Lang.PREFIX + Lang.PLAYER_NEVER_LOGGED_ON.toString().replace("%player%", playerName)));
                }


            });
            return true;

        } else if (args[1].equalsIgnoreCase("uninvite")) {

            if (args.length != 3) {
                sender.sendMessage(Lang.PREFIX.toString() + Lang.INVALID_ARGUMENTS);
                return true;
            }

            String playerName = args[2];

            if (playerName.equalsIgnoreCase(player.getName())) {
                sender.sendMessage(Lang.PREFIX.toString() + Lang.CANNOT_UNINVITE_SELF);
                return true;
            }

            plugin.getRDatabase().getHomeInvitedPlayers(player.getUniqueId().toString(), homeName).thenAccept(result_homeInvitedPlayers -> {

                if (!result_homeInvitedPlayers.toString().toLowerCase().contains(playerName.toLowerCase())) {
                    if (homeName.equalsIgnoreCase("Home")) {
                        scheduler.runTask(plugin, () -> player.sendMessage(Lang.PREFIX + Lang.NOT_INVITED_TO_DEFAULT_HOME.toString().replace("%player%", playerName)));
                    } else {
                        scheduler.runTask(plugin, () -> player.sendMessage(Lang.PREFIX + Lang.NOT_INVITED_TO_SPECIFIED_HOME.toString().replace("%player%", playerName).replace("%home%", homeName)));
                    }
                } else {
                    scheduler.runTaskAsynchronously(plugin, () -> plugin.getRDatabase().deleteInviteColumns(player, homeName, uuidFetcher.getOfflinePlayerUUIDFromMojang(playerName).join()));
                    if (homeName.equalsIgnoreCase("Home")) {
                        scheduler.runTask(plugin, () -> player.sendMessage(Lang.PREFIX + Lang.UNINVITED_FROM_DEFAULT_HOME.toString().replace("%player%", playerName)));
                    } else {
                        scheduler.runTask(plugin, () -> player.sendMessage(Lang.PREFIX + Lang.UNINVITED_FROM_SPECIFIED_HOME.toString().replace("%player%", playerName).replace("%home%", homeName)));
                    }
                }

            });

            return true;

        } else if (args[1].equalsIgnoreCase("listinvites")) {

            if (args.length != 2) {
                sender.sendMessage(Lang.PREFIX.toString() + Lang.INVALID_ARGUMENTS);
                return true;
            }

            plugin.getRDatabase().getHomeInvitedPlayers(player.getUniqueId().toString(), homeName).thenAccept(result_homeInvitedPlayersList -> {
                String invitedPlayersList = result_homeInvitedPlayersList.toString().substring(1, result_homeInvitedPlayersList.toString().length()-1);
                List<String> messageList = plugin.getLang().getStringList("listinvitestohome");

                for (String message : messageList) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', message.replace("%home%", homeName).replace("%invites_list%", invitedPlayersList)));
                }

            });
            return true;
        } else if (args[1].equalsIgnoreCase("info")) {

            if (args.length != 2) {
                sender.sendMessage(Lang.PREFIX.toString() + Lang.INVALID_ARGUMENTS);
                return true;
            }

            plugin.getRDatabase().getHomeInfo(player, homeName).thenAccept(result_homeInfo -> {
                scheduler.runTask(plugin, () -> player.sendMessage(result_homeInfo.toString()));
                List<String> messageList = plugin.getLang().getStringList("homeinfo");

                String name = result_homeInfo.get(0);
                String x = String.valueOf((int)Double.parseDouble(result_homeInfo.get(1)));
                String y = String.valueOf((int)Double.parseDouble(result_homeInfo.get(2)));
                String z = String.valueOf((int)Double.parseDouble(result_homeInfo.get(3)));
                String world = result_homeInfo.get(4);
                String privacy = result_homeInfo.get(5);

                for (String message : messageList) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', message.replace("%home_name%", name).replace("%home_x%", x).replace("%home_y%", y).replace("%home_z%", z).replace("%home_world%", world).replace("%privacy_status%", privacy)));
                }

            });
            player.sendMessage(plugin.getRDatabase().getHomeInfo(player, homeName).join().toString());
            return true;
        } else if (args[1].equalsIgnoreCase("privacy")) {

            if (args.length != 3) {
                sender.sendMessage(Lang.PREFIX.toString() + Lang.INVALID_ARGUMENTS);
                return true;
            }

            if (args[2].equalsIgnoreCase("private")) {
                scheduler.runTaskAsynchronously(plugin, () -> plugin.getRDatabase().updatePrivacyStatus(player, homeName, false));

                if (homeName.equalsIgnoreCase("Home")) {
                    player.sendMessage(Lang.PREFIX.toString() + Lang.DEFAULT_HOME_PRIVATE);
                } else {
                    player.sendMessage(Lang.PREFIX + Lang.SPECIFIED_HOME_PRIVATE.toString().replace("%home%", homeName));
                }
                return true;

            } else if (args[2].equalsIgnoreCase("public")) {
                scheduler.runTaskAsynchronously(plugin, () -> plugin.getRDatabase().updatePrivacyStatus(player, homeName, true));

                if (homeName.equalsIgnoreCase("Home")) {
                    player.sendMessage(Lang.PREFIX.toString() + Lang.DEFAULT_HOME_PUBLIC);
                } else {
                    player.sendMessage(Lang.PREFIX + Lang.SPECIFIED_HOME_PUBLIC.toString().replace("%home%", homeName));
                }
                return true;

            } else {
                player.sendMessage(Lang.PREFIX.toString() + Lang.INVALID_ARGUMENTS);
                return true;
            }

        } else {
            player.sendMessage(Lang.PREFIX.toString() + Lang.INVALID_ARGUMENTS);
            return true;
        }

    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            return null;
        }

        Player player = (Player) sender;

        List<String> tabCompletions = new ArrayList<>();
        List<String> subCommands = new ArrayList<>(Arrays.asList(SUB_COMMANDS));
        List<String> privacyStatusOptions = new ArrayList<>(Arrays.asList(PRIVACY_STATUS_OPTIONS));
        List<String> homeList = plugin.getRDatabase().getHomeList(player).join();

        if (!homeList.isEmpty() && args.length == 1) {
            StringUtil.copyPartialMatches(args[0], homeList, tabCompletions);
            Collections.sort(tabCompletions);
            return tabCompletions;
        }

        if (args.length >= 2 && plugin.getRDatabase().getHome(player, args[0]).join().isEmpty()) {
            return null;
        }

        if (args.length == 2) {
            StringUtil.copyPartialMatches(args[1], subCommands, tabCompletions);
            Collections.sort(tabCompletions);
            return tabCompletions;
        }

        if (args.length == 3) {
            if (args[1].equalsIgnoreCase("invite")) {

                List<String> onlinePlayersList = new ArrayList<>();

                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    onlinePlayersList.add(onlinePlayer.getName());
                }

                StringUtil.copyPartialMatches(args[2], onlinePlayersList, tabCompletions);
                Collections.sort(tabCompletions);
                return tabCompletions;

            } else if (args[1].equalsIgnoreCase("uninvite")) {

                String homeName = plugin.getRDatabase().getHomeInfo(player, args[0]).join().get(0);
                List<String> invitedPlayersList = plugin.getRDatabase().getHomeInvitedPlayers(player.getUniqueId().toString(), homeName).join();
                invitedPlayersList.removeAll(Collections.singletonList(null));

                StringUtil.copyPartialMatches(args[2], invitedPlayersList, tabCompletions);
                Collections.sort(tabCompletions);
                return tabCompletions;
            } else if (args[1].equalsIgnoreCase("privacy")) {
                StringUtil.copyPartialMatches(args[2], privacyStatusOptions, tabCompletions);
                Collections.sort(tabCompletions);
                return tabCompletions;
            }
        }

        return tabCompletions;
    }
}
