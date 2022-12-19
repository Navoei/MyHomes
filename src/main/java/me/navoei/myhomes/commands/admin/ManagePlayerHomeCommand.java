package me.navoei.myhomes.commands.admin;

import me.navoei.myhomes.MyHomes;
import me.navoei.myhomes.uuid.Fetcher;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;

public class ManagePlayerHomeCommand implements CommandExecutor, TabCompleter {

    Fetcher uuidFetcher = new Fetcher();

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

        if (!uuidFetcher.checkPlayedBefore(args[0]).join()) {
            sender.sendMessage("This player has never joined before.");
            return true;
        }

        String playerName = args[0];
        String homeName = args[1];

        if (!MyHomes.getInstance().getRDatabase().getHomeListUsingHomeownerUUID(uuidFetcher.getOfflinePlayerUUID(playerName).join()).join().toString().toLowerCase().contains(homeName.toLowerCase())) {
            sender.sendMessage("This player " + playerName + " does not have a home by the name " + homeName + ".");
            return true;
        }

        if (args.length == 4) {

            if (args[2].equalsIgnoreCase("invite")) {

            } else if (args[2].equalsIgnoreCase("uninvite")) {

            } else if (args[2].equalsIgnoreCase("privacy")) {

            } else if (args[2].equalsIgnoreCase("set")) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Only players can use this command.");
                    return true;
                }



            }
        }

        if (args.length == 3) {
            if (args[2].equalsIgnoreCase("delete")) {

            } else if (args[2].equalsIgnoreCase("invitelist")) {

            } else if (args[2].equalsIgnoreCase("info")) {

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

            List<String> home = MyHomes.getInstance().getRDatabase().getHomeUsingHomeownerUUID(uuidFetcher.getOfflinePlayerUUID(playerName).join(), homeName).join();

            World world = MyHomes.getInstance().getServer().getWorld(home.get(0));
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

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return null;
    }
}
