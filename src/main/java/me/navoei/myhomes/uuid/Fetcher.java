package me.navoei.myhomes.uuid;

import org.bukkit.Bukkit;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class Fetcher {

    public CompletableFuture<String> getOfflinePlayerUUID(String playerName) {
        return CompletableFuture.supplyAsync(() -> Bukkit.getOfflinePlayer(playerName).getUniqueId().toString());
    }

    public CompletableFuture<Boolean> checkPlayedBefore(String playerName) {
        return CompletableFuture.supplyAsync(() -> Bukkit.getOfflinePlayer(playerName).hasPlayedBefore());
    }

    public CompletableFuture<String> getPlayerNameFromUUID(String playerUUID) {
        return CompletableFuture.supplyAsync(() -> Bukkit.getOfflinePlayer(UUID.fromString(playerUUID)).getName());
    }

}