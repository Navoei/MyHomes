package me.navoei.myhomes.commands.player;

import me.navoei.myhomes.MyHomes;
import me.navoei.myhomes.uuid.Fetcher;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ManageHomeCommand implements CommandExecutor, TabCompleter {

    Fetcher uuidFetcher = new Fetcher();
    private final String[] SUB_COMMANDS = { "invite", "uninvite", "listinvites", "privacy", "info" };
    private final String[] PRIVACY_STATUS_OPTIONS = { "private", "public" };

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can execute this command.");
            return true;
        }
        if(args.length == 1) {
            sender.sendMessage("Not enough arguments.");
            return true;
        }
        if (args.length > 3) {
            sender.sendMessage("Too many arguments.");
            return true;
        }

        Player player = (Player) sender;

        if (MyHomes.getInstance().getRDatabase().getHomeInfo(player, args[0]).join().isEmpty()) {
            player.sendMessage("This home does not exist.");
            return true;
        }

        String homeName = MyHomes.getInstance().getRDatabase().getHomeInfo(player, args[0]).join().get(0);

        if (args[1].equalsIgnoreCase("invite")) {

            if (args.length != 3) {
                sender.sendMessage("Invalid arguments.");
                return true;
            }

            String playerName = args[2];

            if (playerName.equalsIgnoreCase(player.getName())) {
                sender.sendMessage("You cannot invite yourself.");
                return true;
            }

            if (uuidFetcher.checkPlayedBefore(args[2]).join()) {

                if (MyHomes.getInstance().getRDatabase().getHomeInvitedPlayers(player.getUniqueId().toString(), homeName).toString().toLowerCase().contains(playerName.toLowerCase())) {
                    if (homeName.equals("Home")) {
                        player.sendMessage("The player "+ playerName +" has already been invited to your home.");
                    } else {
                        player.sendMessage("The player "+ playerName +" has already been invited to " + homeName + ".");
                    }
                    return true;
                }

                MyHomes.getInstance().getRDatabase().setInviteColumns(player, homeName, uuidFetcher.getOfflinePlayerUUID(playerName).join());

                if (homeName.equals("Home")) {
                    player.sendMessage("The player "+ playerName +" has been invited to your home.");
                } else {
                    player.sendMessage("The player "+ playerName +" has been invited to " + homeName + ".");
                }
            } else {
                player.sendMessage("The player "+playerName+" has never logged on before.");
            }
            return true;

        } else if (args[1].equalsIgnoreCase("uninvite")) {

            if (args.length != 3) {
                sender.sendMessage("Invalid arguments.");
                return true;
            }

            String playerName = args[2];

            if (playerName.equalsIgnoreCase(player.getName())) {
                sender.sendMessage("You cannot uninvite yourself.");
                return true;
            }

            if (!MyHomes.getInstance().getRDatabase().getHomeInvitedPlayers(player.getUniqueId().toString(), homeName).toString().toLowerCase().contains(playerName.toLowerCase())) {
                if (homeName.equals("Home")) {
                    player.sendMessage("The player "+ playerName +" has not been invited to your home.");
                } else {
                    player.sendMessage("The player "+ playerName +" has not been invited to " + homeName + ".");
                }
                return true;
            }

            MyHomes.getInstance().getRDatabase().deleteInviteColumns(player, homeName, uuidFetcher.getOfflinePlayerUUID(playerName).join());

            if (homeName.equals("Home")) {
                player.sendMessage("The player "+ playerName +" has been uninvited to your home.");
            } else {
                player.sendMessage("The player "+ playerName +" has been uninvited to " + homeName + ".");
            }

        } else if (args[1].equalsIgnoreCase("listinvites")) {

            if (args.length != 2) {
                sender.sendMessage("Invalid arguments.");
                return true;
            }

            player.sendMessage(MyHomes.getInstance().getRDatabase().getHomeInvitedPlayers(player.getUniqueId().toString(), homeName).toString());
        } else if (args[1].equalsIgnoreCase("info")) {

            if (args.length != 2) {
                sender.sendMessage("Invalid arguments.");
                return true;
            }

            player.sendMessage(MyHomes.getInstance().getRDatabase().getHomeInfo(player, homeName).toString());
        } else if (args[1].equalsIgnoreCase("privacy")) {

            if (args.length != 3) {
                sender.sendMessage("Invalid arguments.");
                return true;
            }
            if (args[2].equalsIgnoreCase("private")) {
                MyHomes.getInstance().getRDatabase().updatePrivacyStatus(player, homeName, false);
            } else if (args[2].equalsIgnoreCase("public")) {
                MyHomes.getInstance().getRDatabase().updatePrivacyStatus(player, homeName, true);
            } else {
                player.sendMessage("Invalid arguments.");
            }

        } else {
            player.sendMessage("Invalid arguments.");
            return true;
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            return null;
        }

        Player player = (Player) sender;

        List<String> homeList = MyHomes.getInstance().getRDatabase().getHomeList(player).join();
        List<String> subCommands = new ArrayList<>(Arrays.asList(SUB_COMMANDS));
        List<String> privacyStatusOptions = new ArrayList<>(Arrays.asList(PRIVACY_STATUS_OPTIONS));
        List<String> tabCompletions = new ArrayList<>();

        if (!homeList.isEmpty() && args.length == 1) {
            StringUtil.copyPartialMatches(args[0], homeList, tabCompletions);
            Collections.sort(tabCompletions);
            return tabCompletions;
        }

        if (args.length >= 2 && MyHomes.getInstance().getRDatabase().getHome(player, args[0]).join().isEmpty()) {
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

                String homeName = MyHomes.getInstance().getRDatabase().getHomeInfo(player, args[0]).join().get(0);
                List<String> invitedPlayersList = MyHomes.getInstance().getRDatabase().getHomeInvitedPlayers(player.getUniqueId().toString(), homeName).join();

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
