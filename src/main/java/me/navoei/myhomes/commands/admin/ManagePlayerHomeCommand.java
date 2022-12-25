package me.navoei.myhomes.commands.admin;

import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import me.navoei.myhomes.MyHomes;
import me.navoei.myhomes.language.Lang;
import me.navoei.myhomes.uuid.Fetcher;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ManagePlayerHomeCommand implements CommandExecutor, Listener {

    MyHomes plugin = MyHomes.getInstance();
    Fetcher uuidFetcher = new Fetcher();
    private final String[] SUB_COMMANDS = { "invite", "uninvite", "privacy", "set", "delete", "listinvites", "info" };
    private final String[] PRIVACY_STATUS_OPTIONS = { "private", "public" };

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length > 4) {
            sender.sendMessage(Lang.PREFIX.toString() + Lang.TOO_MANY_ARGUMENTS);
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(Lang.PREFIX.toString() + Lang.NOT_ENOUGH_ARGUMENTS);
            return true;
        }

        String playerName = args[0];
        String homeName = args[1];

        String playerHasNoHome = Lang.MANAGE_HOMES_PLAYER_HAS_NO_HOME.toString().replace("%player%", playerName).replace("%home%", homeName);
        if (plugin.getRDatabase().getHomeUsingHomeownerUUID(uuidFetcher.getOfflinePlayerUUIDFromMojang(playerName).join(), homeName).join().isEmpty()) {
            sender.sendMessage(Lang.PREFIX + playerHasNoHome);
            return true;
        }

        if (args.length == 4) {

            if (args[2].equalsIgnoreCase("invite")) {

                String invitedPlayerName = args[3];

                if (playerName.equalsIgnoreCase(invitedPlayerName)) {
                    sender.sendMessage(Lang.PREFIX + Lang.CANNOT_INVITE_SELF.toString().replace("%player%", playerName));
                    return true;
                }

                if (uuidFetcher.checkPlayedBefore(invitedPlayerName).join()) {

                    if (plugin.getRDatabase().getHomeInvitedPlayers(uuidFetcher.getOfflinePlayerUUID(playerName), homeName).join().toString().toLowerCase().contains(invitedPlayerName.toLowerCase())) {
                        if (homeName.equalsIgnoreCase("Home")) {
                            sender.sendMessage(Lang.PREFIX + Lang.MANAGE_HOMES_PLAYER_ALREADY_INVITED_TO_DEFAULT_HOME.toString().replace("%invited_player%", invitedPlayerName).replace("%homeowner%", playerName));
                        } else {
                            sender.sendMessage(Lang.PREFIX + Lang.MANAGE_HOMES_PLAYER_ALREADY_INVITED_TO_SPECIFIED_HOME.toString().replace("%invited_player%", invitedPlayerName).replace("%home%", homeName).replace("%homeowner%", playerName));
                        }
                        return true;
                    }

                    if (homeName.equalsIgnoreCase("Home")) {
                        plugin.getRDatabase().setInviteColumnsUsingHomeownerUUID(uuidFetcher.getOfflinePlayerUUID(playerName), "Home", uuidFetcher.getOfflinePlayerUUID(invitedPlayerName));
                        sender.sendMessage(Lang.PREFIX + Lang.MANAGE_HOMES_INVITED_TO_DEFAULT_HOME.toString().replace("%invited_player%", invitedPlayerName).replace("%homeowner%", playerName));
                    } else {
                        plugin.getRDatabase().setInviteColumnsUsingHomeownerUUID(uuidFetcher.getOfflinePlayerUUID(playerName), homeName, uuidFetcher.getOfflinePlayerUUID(invitedPlayerName));
                        sender.sendMessage(Lang.PREFIX + Lang.MANAGE_HOMES_INVITED_TO_SPECIFIED_HOME.toString().replace("%invited_player%", invitedPlayerName).replace("%home%", homeName).replace("%homeowner%", playerName));
                    }
                    return true;

                } else {
                    sender.sendMessage(Lang.PREFIX + Lang.PLAYER_NEVER_LOGGED_ON.toString().replace("%player%", invitedPlayerName));
                }

                return true;

            } else if (args[2].equalsIgnoreCase("uninvite")) {

                String uninvitedPlayerName = args[3];

                if (playerName.equalsIgnoreCase(uninvitedPlayerName)) {
                    sender.sendMessage(Lang.PREFIX + Lang.MANAGE_HOMES_CANNOT_UNINVITE_SELF.toString().replace("%player%", playerName));
                    return true;
                }

                if (!plugin.getRDatabase().getHomeInvitedPlayers(uuidFetcher.getOfflinePlayerUUID(playerName), homeName).join().toString().toLowerCase().contains(uninvitedPlayerName.toLowerCase())) {
                    if (homeName.equalsIgnoreCase("Home")) {
                        sender.sendMessage(Lang.PREFIX + Lang.MANAGE_HOMES_HAS_NOT_BEEN_INVITED_TO_DEFAULT_HOME.toString().replace("%uninvited_player%", uninvitedPlayerName).replace("%homeowner%", playerName));
                    } else {
                        sender.sendMessage(Lang.PREFIX + Lang.MANAGE_HOMES_HAS_NOT_BEEN_INVITED_TO_SPECIFIED_HOME.toString().replace("%uninvited_player%", uninvitedPlayerName).replace("%home%", homeName).replace("%homeowner%", playerName));
                    }
                    return true;
                }

                plugin.getRDatabase().deleteInviteColumnsUsingHomeownerUUID(uuidFetcher.getOfflinePlayerUUID(playerName), homeName, uuidFetcher.getOfflinePlayerUUID(uninvitedPlayerName));

                if (homeName.equalsIgnoreCase("Home")) {
                    sender.sendMessage(Lang.PREFIX + Lang.MANAGE_HOMES_UNINVITED_FROM_DEFAULT_HOME.toString().replace("%uninvited_player%", uninvitedPlayerName).replace("%homeowner%", playerName));
                } else {
                    sender.sendMessage(Lang.PREFIX + Lang.MANAGE_HOMES_UNINVITED_FROM_SPECIFIED_HOME.toString().replace("%uninvited_player%", uninvitedPlayerName).replace("%home%", homeName).replace("%homeowner%", playerName));
                }
                return true;

            } else if (args[2].equalsIgnoreCase("privacy")) {

                if (args[3].equalsIgnoreCase("private")) {
                    plugin.getRDatabase().updatePrivacyStatusUsingHomeownerUUID(uuidFetcher.getOfflinePlayerUUID(playerName), homeName, false);

                    if (homeName.equalsIgnoreCase("Home")) {
                        sender.sendMessage(Lang.PREFIX + Lang.MANAGE_HOMES_PRIVACY_STATUS_PRIVATE_DEFAULT_HOME.toString().replace("%homeowner%", playerName));
                    } else {
                        sender.sendMessage(Lang.PREFIX + Lang.MANAGE_HOMES_PRIVACY_STATUS_PRIVATE_SPECIFIED_HOME.toString().replace("%home%", homeName).replace("%homeowner%", playerName));
                    }
                } else if (args[3].equalsIgnoreCase("public")) {
                    plugin.getRDatabase().updatePrivacyStatusUsingHomeownerUUID(uuidFetcher.getOfflinePlayerUUID(playerName), homeName, true);

                    if (homeName.equalsIgnoreCase("Home")) {
                        sender.sendMessage(Lang.PREFIX + Lang.MANAGE_HOMES_PRIVACY_STATUS_PUBLIC_DEFAULT_HOME.toString().replace("%homeowner%", playerName));
                    } else {
                        sender.sendMessage(Lang.PREFIX + Lang.MANAGE_HOMES_PRIVACY_STATUS_PUBLIC_SPECIFIED_HOME.toString().replace("%home%", homeName).replace("%homeowner%", playerName));
                    }
                } else {
                    sender.sendMessage(Lang.PREFIX.toString() + Lang.INVALID_ARGUMENTS);

                }

                return true;

            } else {
                sender.sendMessage(Lang.PREFIX.toString() + Lang.INVALID_ARGUMENTS);
            }
        }

        if (args.length == 3) {
            if (args[2].equalsIgnoreCase("set")) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(Lang.PREFIX.toString() + Lang.PLAYER_ONLY);
                    return true;
                }
                if (!uuidFetcher.checkPlayedBefore(playerName).join()) {
                    sender.sendMessage(Lang.PREFIX + Lang.PLAYER_NEVER_LOGGED_ON.toString().replace("%player%", playerName));
                    return true;
                }

                Player adminPlayer = (Player) sender;

                if (!plugin.getRDatabase().getHomeListUsingHomeownerUUID(uuidFetcher.getOfflinePlayerUUIDFromMojang(playerName).join()).join().toString().toLowerCase().contains(homeName.toLowerCase())) {
                    if (homeName.equalsIgnoreCase("Home")) {
                        adminPlayer.sendMessage(Lang.PREFIX + Lang.MANAGE_HOMES_SET_PLAYER_DEFAULT_HOME.toString().replace("%player%", playerName));
                        plugin.getRDatabase().setHomeColumnsUsingHomeownerUUID(uuidFetcher.getOfflinePlayerUUID(playerName), adminPlayer, "Home", false);
                    } else {
                        adminPlayer.sendMessage(Lang.PREFIX + Lang.MANAGE_HOMES_SET_PLAYER_SPECIFIED_HOME.toString().replace("%home%", homeName).replace("%player%", playerName));
                        plugin.getRDatabase().setHomeColumnsUsingHomeownerUUID(uuidFetcher.getOfflinePlayerUUID(playerName), adminPlayer, homeName, false);
                    }
                } else {

                    if (homeName.equalsIgnoreCase("Home")) {
                        adminPlayer.sendMessage(Lang.PREFIX + Lang.MANAGE_HOMES_UPDATED_LOCATION_DEFAULT_HOME.toString().replace("%homeowner%", playerName));
                        plugin.getRDatabase().updateHomeLocationUsingHomeownerUUID(uuidFetcher.getOfflinePlayerUUID(playerName), adminPlayer, "Home");
                    } else {
                        adminPlayer.sendMessage(Lang.PREFIX + Lang.MANAGE_HOMES_UPDATED_LOCATION_SPECIFIED_HOME.toString().replace("%home%", homeName).replace("%homeowner%", playerName));
                        plugin.getRDatabase().updateHomeLocationUsingHomeownerUUID(uuidFetcher.getOfflinePlayerUUID(playerName), adminPlayer, homeName);
                    }

                }
                return true;
            } else if (args[2].equalsIgnoreCase("delete")) {

                if (!plugin.getRDatabase().getHomeListUsingHomeownerUUID(uuidFetcher.getOfflinePlayerUUIDFromMojang(playerName).join()).join().toString().toLowerCase().contains(homeName.toLowerCase())) {
                    sender.sendMessage(Lang.PREFIX + playerHasNoHome);
                    return true;
                }

                if (homeName.equalsIgnoreCase("home")) {
                    sender.sendMessage(Lang.PREFIX + Lang.MANAGE_HOMES_DELETE_DEFAULT_HOME.toString().replace("%player%", playerName));
                } else {
                    sender.sendMessage(Lang.PREFIX + Lang.MANAGE_HOMES_DELETE_SPECIFIED_HOME.toString().replace("%home%", homeName).replace("%homeowner%", playerName));
                }
                plugin.getRDatabase().deleteHomeUsingHomeownerUUID(uuidFetcher.getOfflinePlayerUUID(playerName), homeName);
                plugin.getRDatabase().deleteAllInviteColumnsUsingHomeownerUUID(uuidFetcher.getOfflinePlayerUUID(playerName), homeName);

            } else if (args[2].equalsIgnoreCase("listinvites")) {

                if (!plugin.getRDatabase().getHomeListUsingHomeownerUUID(uuidFetcher.getOfflinePlayerUUIDFromMojang(playerName).join()).join().toString().toLowerCase().contains(homeName.toLowerCase())) {
                    sender.sendMessage(Lang.PREFIX + playerHasNoHome);
                    return true;
                }

                sender.sendMessage(plugin.getRDatabase().getHomeInvitedPlayers(uuidFetcher.getOfflinePlayerUUIDFromMojang(playerName).join(), homeName).join().toString());

            } else if (args[2].equalsIgnoreCase("info")) {

                if (!plugin.getRDatabase().getHomeListUsingHomeownerUUID(uuidFetcher.getOfflinePlayerUUIDFromMojang(playerName).join()).join().toString().toLowerCase().contains(homeName.toLowerCase())) {
                    sender.sendMessage(Lang.PREFIX + playerHasNoHome);
                    return true;
                }

                sender.sendMessage(plugin.getRDatabase().getHomeInfoUsingHomeownerUUID(uuidFetcher.getOfflinePlayerUUID(playerName), homeName).join().toString());

            } else {
                sender.sendMessage(Lang.PREFIX.toString() + Lang.INVALID_ARGUMENTS);
                return true;
            }
        }

        if (args.length == 2) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(Lang.PREFIX.toString() + Lang.PLAYER_ONLY);
                return true;
            }

            Player player = (Player) sender;

            List<String> home = plugin.getRDatabase().getHomeUsingHomeownerUUID(uuidFetcher.getOfflinePlayerUUIDFromMojang(playerName).join(), homeName).join();

            if (home.isEmpty()) {
                sender.sendMessage(Lang.PREFIX + playerHasNoHome);
                return true;
            }

            World world = plugin.getServer().getWorld(home.get(0));
            Location homeLocation = new Location(world, Double.parseDouble(home.get(1)), Double.parseDouble(home.get(2)), Double.parseDouble(home.get(3)), Float.parseFloat(home.get(4)), Float.parseFloat(home.get(5)));

            player.teleport(homeLocation);

            if (homeName.equalsIgnoreCase("home")) {
                player.sendMessage(Lang.PREFIX + Lang.HOME_OTHER.toString().replace("%player%", playerName));
            } else {
                player.sendMessage(Lang.PREFIX + Lang.SPECIFIED_HOME.toString().replace("%home%", homeName));
            }
            return true;
        }
        return false;
    }

    @EventHandler(ignoreCancelled = true)
    public void onTabCompletion(AsyncTabCompleteEvent event) {

        if (!event.isCommand()) return;

        String buffer = event.getBuffer();
        if (buffer.isEmpty()) return;

        if (buffer.charAt(0) == '/') {
            buffer = buffer.substring(1);
        }

        int firstSpace = buffer.indexOf(' ');
        if (firstSpace < 0) {
            return;
        }

        if (!buffer.startsWith("manageplayerhome")) return;

        List<String> args = new ArrayList<>(Arrays.asList(buffer.split(" ")));
        args.remove(0);

        List<String> tabCompletions = new ArrayList<>();
        List<String> subCommands = new ArrayList<>(Arrays.asList(SUB_COMMANDS));
        List<String> privacyStatusOptions = new ArrayList<>(Arrays.asList(PRIVACY_STATUS_OPTIONS));
        List<String> homeList = new ArrayList<>();
        if (args.size() >= 1) {
            homeList = plugin.getRDatabase().getHomeListUsingHomeownerUUID(uuidFetcher.getOfflinePlayerUUID(args.get(0))).join();
        }
        List<String> onlinePlayersList = new ArrayList<>();
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayersList.add(onlinePlayer.getName());
        }

        if (args.size() == 1 && buffer.endsWith(" ")) {
            if (homeList.isEmpty()) return;
            event.setCompletions(homeList);
            event.setHandled(true);
        }

        if (args.size() == 2) {
            StringUtil.copyPartialMatches(args.get(1), homeList, tabCompletions);
            Collections.sort(tabCompletions);
            event.setCompletions(tabCompletions);
            event.setHandled(true);
        }

        if (args.size() == 2 && buffer.endsWith(" ")) {
            event.setCompletions(subCommands);
            event.setHandled(true);
        }

        if (args.size() == 3) {
            StringUtil.copyPartialMatches(args.get(2), subCommands, tabCompletions);
            Collections.sort(tabCompletions);
            event.setCompletions(tabCompletions);
            event.setHandled(true);
        }

        if (args.size() == 3 && buffer.endsWith(" ")) {
            if (args.get(2).equalsIgnoreCase("privacy")) {
                event.setCompletions(privacyStatusOptions);
                event.setHandled(true);
            } else if (args.get(2).equalsIgnoreCase("invite")) {
                event.setCompletions(onlinePlayersList);
                event.setHandled(true);
            } else if (args.get(2).equalsIgnoreCase("uninvite")) {
                List<String> invitedPlayersList = plugin.getRDatabase().getHomeInvitedPlayers(uuidFetcher.getOfflinePlayerUUID(args.get(0)), args.get(1)).join();
                if (invitedPlayersList.isEmpty()) {
                    event.setCompletions(new ArrayList<>());
                    event.setHandled(true);
                } else {
                    event.setCompletions(invitedPlayersList);
                    event.setHandled(true);
                }
            }
        }

        if (args.size() == 4 && args.get(2).equalsIgnoreCase("privacy")) {
            StringUtil.copyPartialMatches(args.get(3), privacyStatusOptions, tabCompletions);
            event.setCompletions(tabCompletions);
            event.setHandled(true);
        }

        if (args.size() == 4 && args.get(2).equalsIgnoreCase("invite")) {
            StringUtil.copyPartialMatches(args.get(3), onlinePlayersList, tabCompletions);
            event.setCompletions(tabCompletions);
            event.setHandled(true);
        }

        if (args.size() == 4 && args.get(2).equalsIgnoreCase("uninvite")) {
            List<String> invitedPlayersList = plugin.getRDatabase().getHomeInvitedPlayers(uuidFetcher.getOfflinePlayerUUID(args.get(0)), args.get(1)).join();
            if (invitedPlayersList.isEmpty()) {
                event.setCompletions(new ArrayList<>());
                event.setHandled(true);
            } else {
                StringUtil.copyPartialMatches(args.get(3), invitedPlayersList, tabCompletions);
                event.setCompletions(tabCompletions);
                event.setHandled(true);
            }
        }

    }

}
