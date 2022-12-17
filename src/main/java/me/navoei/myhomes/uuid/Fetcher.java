package me.navoei.myhomes.uuid;

import org.bukkit.Bukkit;

import java.util.UUID;

public class Fetcher {

    public String getOfflinePlayerUUID(String playerName) {
        return Bukkit.getOfflinePlayer(playerName).getUniqueId().toString();
    }

    public boolean checkPlayedBefore(String playerName) {
        return Bukkit.getOfflinePlayer(playerName).hasPlayedBefore();
    }

    public String getPlayerNameFromUUID(String playerUUID) {
        return Bukkit.getOfflinePlayer(UUID.fromString(playerUUID)).getName();
    }

}