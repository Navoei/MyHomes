package me.navoei.myhomes.commands.admin;

import me.navoei.myhomes.MyHomes;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.sql.SQLException;

public class ImportMyHomeCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (sender instanceof Player) {
            sender.sendMessage("Only console is allowed to use this command.");
            return true;
        }

        File oldMyHomeDatabase = new File(MyHomes.getInstance().getDataFolder(), "homes.db");
        if (!oldMyHomeDatabase.exists() || oldMyHomeDatabase.isDirectory()) {
            sender.sendMessage("Old database does not exist.");
            return true;
        }

        sender.sendMessage("The old database exists. Importing now...");

        Bukkit.getScheduler().runTaskAsynchronously(MyHomes.getInstance(), () -> {
            try {
                MyHomes.getInstance().getRDatabase().importOldMyHomeDatabase(oldMyHomeDatabase, MyHomes.getInstance());
            } catch (SQLException e) {
                sender.sendMessage("An error occurred while importing the database.");
                e.printStackTrace();
            } finally {
                sender.sendMessage("Import successful! You may now delete the old database.");
            }
        });

        return false;
    }
}
