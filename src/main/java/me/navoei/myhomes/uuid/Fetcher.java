package me.navoei.myhomes.uuid;

import org.bukkit.Bukkit;

import java.io.FileNotFoundException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class Fetcher {

    public CompletableFuture<String> getOfflinePlayerUUIDFromMojang(String playerName) {
        return CompletableFuture.supplyAsync(() -> UUIDFetcher.getUUIDString(playerName));
    }

    public CompletableFuture<Boolean> checkPlayedBefore(String playerName) {
        return CompletableFuture.supplyAsync(() -> Bukkit.getOfflinePlayer(playerName).hasPlayedBefore());
    }

    public String getPlayerNameFromMojang(String playerUUID) {
        return NameFetcher.getName(UUID.fromString(playerUUID));
    }

    public String getPlayerNameFromUUID(String playerUUID) {
        return Bukkit.getOfflinePlayer(UUID.fromString(playerUUID)).getName();
    }

    public String getOfflinePlayerUUID(String playerName) {
        return Bukkit.getOfflinePlayer(playerName).getUniqueId().toString();
    }

}