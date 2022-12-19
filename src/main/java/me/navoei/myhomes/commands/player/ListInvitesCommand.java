package me.navoei.myhomes.commands.player;

import me.navoei.myhomes.MyHomes;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.concurrent.ExecutionException;

public class ListInvitesCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can execute this command!");
            return true;
        }

        Player player = (Player) sender;

        try {
            player.sendMessage(MyHomes.getInstance().getRDatabase().getHomeInviteList(player.getUniqueId().toString()).get().toString());
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }


        return false;
    }
}
