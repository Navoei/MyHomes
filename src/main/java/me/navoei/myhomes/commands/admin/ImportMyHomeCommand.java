package me.navoei.myhomes.commands.admin;

import me.navoei.myhomes.MyHomes;
import me.navoei.myhomes.language.Lang;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.sql.SQLException;

public class ImportMyHomeCommand implements CommandExecutor {

    MyHomes plugin = MyHomes.getInstance();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (sender instanceof Player) {
            sender.sendMessage(Lang.PREFIX.toString() + Lang.CONSOLE_ONLY);
            return true;
        }

        File oldMyHomeDatabase = new File(plugin.getDataFolder(), "homes.db");
        if (!oldMyHomeDatabase.exists() || oldMyHomeDatabase.isDirectory()) {
            sender.sendMessage(Lang.PREFIX.toString() + Lang.OLD_DATABASE_NOT_EXIST);
            return true;
        }

        sender.sendMessage(Lang.PREFIX.toString() + Lang.IMPORTING_DATABASE);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                plugin.getRDatabase().importOldMyHomeDatabase(oldMyHomeDatabase, plugin);
            } catch (SQLException e) {
                sender.sendMessage(Lang.PREFIX.toString() + Lang.DATABASE_IMPORT_ERROR);
                e.printStackTrace();
            } finally {
                sender.sendMessage(Lang.PREFIX.toString() + Lang.DATABASE_IMPORT_SUCCESS);
            }
        });

        return false;
    }
}
