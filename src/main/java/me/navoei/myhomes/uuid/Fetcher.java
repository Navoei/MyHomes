package me.navoei.myhomes.uuid;

import me.navoei.myhomes.MyHomes;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class Fetcher {

    public CompletableFuture<String> getOfflinePlayerUUID(String playerName) {
        return CompletableFuture.supplyAsync(() ->
        {
            Player player = Bukkit.getPlayer(playerName);
            if (player!=null) {
                return player.getUniqueId().toString();
            }
            //MyHomes.getInstance().getLogger().log(Level.INFO, "Getting offline player UUID...");
            return Bukkit.getOfflinePlayer(playerName).getUniqueId().toString();
        });
    }

    public CompletableFuture<Boolean> checkPlayedBefore(String playerName) {
        return CompletableFuture.supplyAsync(() -> Bukkit.getOfflinePlayer(playerName).hasPlayedBefore());
    }

    public String getPlayerNameFromUUID(String playerUUID) {
        return Bukkit.getOfflinePlayer(UUID.fromString(playerUUID)).getName();
    }

}