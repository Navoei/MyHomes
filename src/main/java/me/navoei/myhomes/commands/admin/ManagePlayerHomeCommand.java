package me.navoei.myhomes.commands.admin;

import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import me.navoei.myhomes.MyHomes;
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
            sender.sendMessage("Too many arguments!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("Not enough arguments.");
            return true;
        }

        String playerName = args[0];
        String homeName = args[1];

        if (plugin.getRDatabase().getHomeUsingHomeownerUUID(uuidFetcher.getOfflinePlayerUUIDFromMojang(playerName).join(), homeName).join().isEmpty()) {
            sender.sendMessage("The player " + playerName + " does not have a home by the name " + homeName + ".");
            return true;
        }

        if (args.length == 4) {

            if (args[2].equalsIgnoreCase("invite")) {

                String invitedPlayerName = args[3];

                if (playerName.equalsIgnoreCase(invitedPlayerName)) {
                    sender.sendMessage("You cannot invite " + playerName + " to their own home.");
                    return true;
                }

                if (uuidFetcher.checkPlayedBefore(invitedPlayerName).join()) {

                    if (plugin.getRDatabase().getHomeInvitedPlayers(uuidFetcher.getOfflinePlayerUUID(playerName), homeName).join().toString().toLowerCase().contains(invitedPlayerName.toLowerCase())) {
                        if (homeName.equalsIgnoreCase("Home")) {
                            sender.sendMessage("The player "+ invitedPlayerName +" has already been invited to " + playerName + "'s home.");
                        } else {
                            sender.sendMessage("The player "+ playerName +" has already been invited to " + homeName + ".");
                        }
                        return true;
                    }

                    if (homeName.equalsIgnoreCase("Home")) {
                        plugin.getRDatabase().setInviteColumnsUsingHomeownerUUID(uuidFetcher.getOfflinePlayerUUID(playerName), "Home", uuidFetcher.getOfflinePlayerUUID(invitedPlayerName));
                        sender.sendMessage("The player " + invitedPlayerName + " has been invited to " + playerName + "'s home.");
                    } else {
                        plugin.getRDatabase().setInviteColumnsUsingHomeownerUUID(uuidFetcher.getOfflinePlayerUUID(playerName), homeName, uuidFetcher.getOfflinePlayerUUID(invitedPlayerName));
                        sender.sendMessage("The player " + invitedPlayerName + " has been invited to the home " + homeName + ", which is owned by " + playerName + ".");
                    }
                    return true;

                } else {
                    sender.sendMessage("The player "+invitedPlayerName+" has never played before.");
                }

                return true;

            } else if (args[2].equalsIgnoreCase("uninvite")) {

                String uninvitedPlayerName = args[3];

                if (playerName.equalsIgnoreCase(uninvitedPlayerName)) {
                    sender.sendMessage("You cannot uninvite " + playerName + " from their own home.");
                    return true;
                }

                if (!plugin.getRDatabase().getHomeInvitedPlayers(uuidFetcher.getOfflinePlayerUUID(playerName), homeName).join().toString().toLowerCase().contains(uninvitedPlayerName.toLowerCase())) {
                    if (homeName.equalsIgnoreCase("Home")) {
                        sender.sendMessage("The player "+ uninvitedPlayerName +" has not been invited to "+playerName+"'s home.");
                    } else {
                        sender.sendMessage("The player "+ uninvitedPlayerName +" has not been invited to " + homeName + ", which is owned by "+playerName+".");
                    }
                    return true;
                }

                plugin.getRDatabase().deleteInviteColumnsUsingHomeownerUUID(uuidFetcher.getOfflinePlayerUUID(playerName), homeName, uuidFetcher.getOfflinePlayerUUID(uninvitedPlayerName));

                if (homeName.equalsIgnoreCase("Home")) {
                    sender.sendMessage("The player "+ uninvitedPlayerName +" has been uninvited from " + playerName + "'s home.");
                } else {
                    sender.sendMessage("The player "+ uninvitedPlayerName +" has been uninvited from " + homeName + ", which is owned by " + playerName + ".");
                }
                return true;

            } else if (args[2].equalsIgnoreCase("privacy")) {

                if (args[3].equalsIgnoreCase("private")) {
                    plugin.getRDatabase().updatePrivacyStatusUsingHomeownerUUID(uuidFetcher.getOfflinePlayerUUID(playerName), homeName, false);

                    if (homeName.equalsIgnoreCase("Home")) {
                        sender.sendMessage("You have changed the privacy status of " + playerName + "'s home to private.");
                    } else {
                        sender.sendMessage("You have changed the privacy status of " + homeName + ", which is owned by " + playerName + ", to private.");
                    }
                } else if (args[3].equalsIgnoreCase("public")) {
                    plugin.getRDatabase().updatePrivacyStatusUsingHomeownerUUID(uuidFetcher.getOfflinePlayerUUID(playerName), homeName, true);

                    if (homeName.equalsIgnoreCase("Home")) {
                        sender.sendMessage("You have changed the privacy status of " + playerName + "'s home to public.");
                    } else {
                        sender.sendMessage("You have changed the privacy status of " + homeName + ", which is owned by " + playerName + ", to public.");
                    }
                } else {
                    sender.sendMessage("Invalid arguments.");

                }

                return true;

            } else {
                sender.sendMessage("Invalid arguments.");
            }
        }

        if (args.length == 3) {
            if (args[2].equalsIgnoreCase("set")) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Only players can use this command.");
                    return true;
                }
                if (!uuidFetcher.checkPlayedBefore(playerName).join()) {
                    sender.sendMessage("The player "+playerName+" has never played before.");
                    return true;
                }

                Player adminPlayer = (Player) sender;

                if (!plugin.getRDatabase().getHomeListUsingHomeownerUUID(uuidFetcher.getOfflinePlayerUUIDFromMojang(playerName).join()).join().toString().toLowerCase().contains(homeName.toLowerCase())) {
                    if (homeName.equalsIgnoreCase("Home")) {
                        adminPlayer.sendMessage("You have set " + playerName + "'s home.");
                        plugin.getRDatabase().setHomeColumnsUsingHomeownerUUID(uuidFetcher.getOfflinePlayerUUID(playerName), adminPlayer, "Home", false);
                    } else {
                        adminPlayer.sendMessage("You have set a home, " + homeName + ", for " + playerName + ".");
                        plugin.getRDatabase().setHomeColumnsUsingHomeownerUUID(uuidFetcher.getOfflinePlayerUUID(playerName), adminPlayer, homeName, false);
                    }
                } else {

                    if (homeName.equalsIgnoreCase("Home")) {
                        adminPlayer.sendMessage("You have updated the location of " + playerName + "'s home.");
                        plugin.getRDatabase().updateHomeLocationUsingHomeownerUUID(uuidFetcher.getOfflinePlayerUUID(playerName), adminPlayer, "Home");
                    } else {
                        adminPlayer.sendMessage("You have updated the location of " + homeName + ", which is owned by " + playerName + ".");
                        plugin.getRDatabase().updateHomeLocationUsingHomeownerUUID(uuidFetcher.getOfflinePlayerUUID(playerName), adminPlayer, homeName);
                    }

                }
                return true;
            } else if (args[2].equalsIgnoreCase("delete")) {

                if (!plugin.getRDatabase().getHomeListUsingHomeownerUUID(uuidFetcher.getOfflinePlayerUUIDFromMojang(playerName).join()).join().toString().toLowerCase().contains(homeName.toLowerCase())) {
                    sender.sendMessage("The player " + playerName + " does not have a home by the name " + homeName + ".");
                    return true;
                }

                if (homeName.equalsIgnoreCase("home")) {
                    sender.sendMessage("You have deleted " +playerName+ "'s home.");
                } else {
                    sender.sendMessage("You have deleted " +homeName+ " which belonged to " +playerName+ ".");
                }
                plugin.getRDatabase().deleteHomeUsingHomeownerUUID(uuidFetcher.getOfflinePlayerUUID(playerName), homeName);
                plugin.getRDatabase().deleteAllInviteColumnsUsingHomeownerUUID(uuidFetcher.getOfflinePlayerUUID(playerName), homeName);

            } else if (args[2].equalsIgnoreCase("listinvites")) {

                if (!plugin.getRDatabase().getHomeListUsingHomeownerUUID(uuidFetcher.getOfflinePlayerUUIDFromMojang(playerName).join()).join().toString().toLowerCase().contains(homeName.toLowerCase())) {
                    sender.sendMessage("The player " + playerName + " does not have a home by the name " + homeName + ".");
                    return true;
                }

                sender.sendMessage(plugin.getRDatabase().getHomeInvitedPlayers(uuidFetcher.getOfflinePlayerUUID(playerName), homeName).join().toString());

            } else if (args[2].equalsIgnoreCase("info")) {

                if (!plugin.getRDatabase().getHomeListUsingHomeownerUUID(uuidFetcher.getOfflinePlayerUUIDFromMojang(playerName).join()).join().toString().toLowerCase().contains(homeName.toLowerCase())) {
                    sender.sendMessage("The player " + playerName + " does not have a home by the name " + homeName + ".");
                    return true;
                }

                sender.sendMessage(plugin.getRDatabase().getHomeInfoUsingHomeownerUUID(uuidFetcher.getOfflinePlayerUUID(playerName), homeName).join().toString());

            } else {
                sender.sendMessage("Invalid arguments.");
                return true;
            }
        }

        if (args.length == 2) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Only players can use this command.");
                return true;
            }

            Player player = (Player) sender;

            List<String> home = plugin.getRDatabase().getHomeUsingHomeownerUUID(uuidFetcher.getOfflinePlayerUUIDFromMojang(playerName).join(), homeName).join();

            if (home.isEmpty()) {
                sender.sendMessage("The player " + playerName + " does not have a home by the name " + homeName + ".");
                return true;
            }

            World world = plugin.getServer().getWorld(home.get(0));
            Location homeLocation = new Location(world, Double.parseDouble(home.get(1)), Double.parseDouble(home.get(2)), Double.parseDouble(home.get(3)), Float.parseFloat(home.get(4)), Float.parseFloat(home.get(5)));

            player.teleport(homeLocation);

            if (homeName.equalsIgnoreCase("home")) {
                player.sendMessage("Welcome to " +playerName+ "'s home.");
            } else {
                player.sendMessage("Welcome to " +homeName+ ".");
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
            homeList = plugin.getRDatabase().getHomeListUsingHomeownerUUID(uuidFetcher.getOfflinePlayerUUIDFromMojang(args.get(0)).join()).join();
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
                List<String> invitedPlayersList = plugin.getRDatabase().getHomeInvitedPlayers(uuidFetcher.getOfflinePlayerUUIDFromMojang(args.get(0)).join(), args.get(1)).join();
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
            List<String> invitedPlayersList = plugin.getRDatabase().getHomeInvitedPlayers(uuidFetcher.getOfflinePlayerUUIDFromMojang(args.get(0)).join(), args.get(1)).join();
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
