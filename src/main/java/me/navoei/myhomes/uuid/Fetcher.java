package me.navoei.myhomes.uuid;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class Fetcher {

    public static CompletableFuture<String> getPlayerUUID(String playerName) {
        return CompletableFuture.supplyAsync(() ->
        {
            Player player = Bukkit.getPlayerExact(playerName);
            if (player!=null) {
                return player.getUniqueId().toString();
            }
            //MyHomes.getInstance().getLogger().log(Level.INFO, "Getting offline player UUID...");
            return UUIDFetcher.getUUIDString(playerName);
        });
    }

    public static CompletableFuture<Boolean> checkPlayedBefore(String playerName) {
        return CompletableFuture.supplyAsync(() -> Bukkit.getOfflinePlayer(playerName).hasPlayedBefore());
    }

    public static String getPlayerNameFromUUID(String playerUUID) {
        return Bukkit.getOfflinePlayer(UUID.fromString(playerUUID)).getName();
    }

}