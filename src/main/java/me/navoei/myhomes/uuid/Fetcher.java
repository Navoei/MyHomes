package me.navoei.myhomes.uuid;

import me.navoei.myhomes.MyHomes;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class Fetcher {

    public String getOfflinePlayerUUID(String invitedPlayerName) {
        return Bukkit.getOfflinePlayer(invitedPlayerName).getUniqueId().toString();
    }

    public boolean checkPlayedBefore(String invitedPlayerName) {
        return Bukkit.getOfflinePlayer(invitedPlayerName).hasPlayedBefore();
    }

}
