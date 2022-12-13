package me.navoei.myhomes.uuid;

import org.bukkit.Bukkit;

public class Fetcher {

    public String getOfflinePlayerUUID(String invitedPlayerName) {
        return Bukkit.getOfflinePlayer(invitedPlayerName).getUniqueId().toString();
    }

    public boolean checkPlayedBefore(String invitedPlayerName) {
        return Bukkit.getOfflinePlayer(invitedPlayerName).hasPlayedBefore();
    }

}
