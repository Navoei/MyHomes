package me.navoei.myhomes.commands.player;

import me.navoei.myhomes.MyHomes;
import me.navoei.myhomes.uuid.Fetcher;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeCommand implements CommandExecutor, TabCompleter {

    Fetcher uuidFetcher = new Fetcher();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can execute this command!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length > 2) {
            player.sendMessage(ChatColor.RED + "Too many arguments!");
            return true;
        }

        //Check if the first argument refers to another player's home.
        // If the player has played before and the first argument isnt the person running the command,
        // Then check if the home's invited players list contains the player's name.
        // If so, get the home info and teleport the player.
        // /home <otherplayer> <theirhome>
        if (args.length>0 && !MyHomes.getInstance().getRDatabase().getHomeList(player).join().toString().toLowerCase().contains(args[0].toLowerCase())) {

            List<String> home;
            List<String> homeInvitedPlayers;
            List<String> homePrivacyStatus;

            if (args.length == 1) {
                home = MyHomes.getInstance().getRDatabase().getHomeUsingHomeownerUUID(uuidFetcher.getOfflinePlayerUUID(args[0]).join(), "Home").join();
                homeInvitedPlayers = MyHomes.getInstance().getRDatabase().getHomeInvitedPlayers(uuidFetcher.getOfflinePlayerUUID(args[0]).join(), "Home").join();
                homePrivacyStatus = MyHomes.getInstance().getRDatabase().getHomePrivacyStatus(uuidFetcher.getOfflinePlayerUUID(args[0]).join(), "Home").join();

                if (home.isEmpty()) {
                    player.sendMessage("This home does not exist.");
                    return true;
                }

                if (homePrivacyStatus.contains("public")) {
                    World world = MyHomes.getInstance().getServer().getWorld(home.get(0));
                    Location homeLocation = new Location(world, Double.parseDouble(home.get(1)), Double.parseDouble(home.get(2)), Double.parseDouble(home.get(3)), Float.parseFloat(home.get(4)), Float.parseFloat(home.get(5)));

                    player.teleport(homeLocation);
                    player.sendMessage("Welcome to " + args[0] + "'s home.");
                    return true;
                } else if (homeInvitedPlayers.contains(player.getName())) {
                    World world = MyHomes.getInstance().getServer().getWorld(home.get(0));
                    Location homeLocation = new Location(world, Double.parseDouble(home.get(1)), Double.parseDouble(home.get(2)), Double.parseDouble(home.get(3)), Float.parseFloat(home.get(4)), Float.parseFloat(home.get(5)));

                    player.teleport(homeLocation);
                    player.sendMessage("Welcome to " + args[0] + "'s home.");
                    return true;
                } else if (!homeInvitedPlayers.contains(player.getName())) {
                    player.sendMessage("You are not invited to " + args[0] + "'s home.");
                    return true;
                }
            }

            if (args.length == 2) {
                home = MyHomes.getInstance().getRDatabase().getHomeUsingHomeownerUUID(uuidFetcher.getOfflinePlayerUUID(args[0]).join(), args[1]).join();
                homeInvitedPlayers = MyHomes.getInstance().getRDatabase().getHomeInvitedPlayers(uuidFetcher.getOfflinePlayerUUID(args[0]).join(), args[1]).join();
                homePrivacyStatus = MyHomes.getInstance().getRDatabase().getHomePrivacyStatus(uuidFetcher.getOfflinePlayerUUID(args[0]).join(), args[1]).join();

                if (home.isEmpty()) {
                    player.sendMessage("This home does not exist.");
                    return true;
                }

                if (homePrivacyStatus.contains("public")) {
                    List<String> otherPlayerHome = MyHomes.getInstance().getRDatabase().getHomeUsingHomeownerUUID(uuidFetcher.getOfflinePlayerUUID(args[0]).join(), args[1]).join();

                    World world = MyHomes.getInstance().getServer().getWorld(otherPlayerHome.get(0));
                    Location homeLocation = new Location(world, Double.parseDouble(otherPlayerHome.get(1)), Double.parseDouble(otherPlayerHome.get(2)), Double.parseDouble(otherPlayerHome.get(3)), Float.parseFloat(otherPlayerHome.get(4)), Float.parseFloat(otherPlayerHome.get(5)));

                    player.teleport(homeLocation);
                    if (args[1].equalsIgnoreCase("home")) {
                        player.sendMessage("Welcome to " + args[0] + "'s home.");
                    } else {
                        player.sendMessage("Welcome to " + args[1] + ".");
                    }

                    return true;
                } else if (homeInvitedPlayers.contains(player.getName())) {
                    World world = MyHomes.getInstance().getServer().getWorld(home.get(0));
                    Location homeLocation = new Location(world, Double.parseDouble(home.get(1)), Double.parseDouble(home.get(2)), Double.parseDouble(home.get(3)), Float.parseFloat(home.get(4)), Float.parseFloat(home.get(5)));

                    player.teleport(homeLocation);

                    if (args[1].equalsIgnoreCase("home")) {
                        player.sendMessage("Welcome to " + args[0] + "'s home.");
                    } else {
                        player.sendMessage("Welcome to " + args[1] + ".");
                    }

                    return true;
                } else if (!homeInvitedPlayers.contains(player.getName())) {

                    if (args[1].equalsIgnoreCase("home")) {
                        player.sendMessage("You are not invited to " + args[0] + "'s home.");
                    } else {
                        player.sendMessage("You are not invited to " + args[1] + ".");
                    }
                    return true;
                }
            }
        }

        if (args.length == 0) {
            List<String> home = MyHomes.getInstance().getRDatabase().getHome(player, "Home").join();
            if (home.isEmpty()) {
                player.sendMessage("You do not have a home.");
                return true;
            }

            World world = MyHomes.getInstance().getServer().getWorld(home.get(0));
            Location homeLocation = new Location(world, Double.parseDouble(home.get(1)), Double.parseDouble(home.get(2)), Double.parseDouble(home.get(3)), Float.parseFloat(home.get(4)), Float.parseFloat(home.get(5)));

            player.teleport(homeLocation);
            player.sendMessage("Welcome to your home.");
            return true;
        } else if (args.length == 1) {
            List<String> home = MyHomes.getInstance().getRDatabase().getHome(player, args[0]).join();
            if (home.isEmpty()) {
                player.sendMessage("This home does not exist.");
                return true;
            }

            World world = MyHomes.getInstance().getServer().getWorld(home.get(0));
            Location homeLocation = new Location(world, Double.parseDouble(home.get(1)), Double.parseDouble(home.get(2)), Double.parseDouble(home.get(3)), Float.parseFloat(home.get(4)), Float.parseFloat(home.get(5)));

            String homeName = MyHomes.getInstance().getRDatabase().getHomeInfo(player, args[0]).join().get(0);

            player.teleport(homeLocation);
            player.sendMessage("Welcome to " + homeName + ".");
            return true;
        } else {
            player.sendMessage("This home does not exist.");
            return false;
        }

    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            return null;
        }

        Player player = (Player) sender;

        List<String> homesList = MyHomes.getInstance().getRDatabase().getHomeList(player).join();
        homesList.addAll(MyHomes.getInstance().getRDatabase().listHomeownersOfInvitedHomes(player).join());

        List<String> tabCompletions = new ArrayList<>();

        if (!homesList.isEmpty() && args.length == 1) {
            StringUtil.copyPartialMatches(args[0], homesList, tabCompletions);
            Collections.sort(tabCompletions);
            return tabCompletions;
        }

        if (!args[0].isEmpty() && args.length == 2 && !MyHomes.getInstance().getRDatabase().getHomeList(player).join().contains(args[0]) && MyHomes.getInstance().getRDatabase().getHomeInviteList(player.getUniqueId().toString()).join().containsValue(args[0])) {
            List<String> invitedHomesList = MyHomes.getInstance().getRDatabase().getInvitedHomesThatAreOwnedByHomeowner(uuidFetcher.getOfflinePlayerUUID(args[0]).join(), player.getUniqueId().toString()).join();
            StringUtil.copyPartialMatches(args[1], invitedHomesList, tabCompletions);
            Collections.sort(tabCompletions);
            return tabCompletions;
        }

        return null;

    }
}
