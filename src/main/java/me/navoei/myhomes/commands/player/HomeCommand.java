package me.navoei.myhomes.commands.player;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import me.navoei.myhomes.MyHomes;
import me.navoei.myhomes.language.Lang;
import me.navoei.myhomes.uuid.Fetcher;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class HomeCommand extends CommandAPICommand {

    MyHomes plugin;

    public HomeCommand(MyHomes plugin) {
        super("home");
        this.plugin = plugin;
        this.withFullDescription("Teleports the player to a specified home.");
        this.withPermission("myhomes.home");

        this.executesPlayer(this::onCommandPlayer);
        this.executesConsole(this::onCommandConsole);

        this.withOptionalArguments(new StringArgument("home_name_or_player").replaceSuggestions(ArgumentSuggestions.stringCollectionAsync((sender) -> CompletableFuture.supplyAsync(() -> {
            if (sender.sender() instanceof Player player) {
                List<String> playerHomes = plugin.getDatabase().getHomeList(player).join();
                playerHomes.addAll(plugin.getDatabase().getHomeInviteList(player.getUniqueId().toString()).join().keySet());
                playerHomes.removeAll(Collections.singletonList(null));
                return playerHomes;
            } else {
                return null;
            }
        }))));

        this.withOptionalArguments(new StringArgument("invited_home").replaceSuggestions(ArgumentSuggestions.stringCollectionAsync((sender) -> CompletableFuture.supplyAsync(() -> {
            String playerName = sender.previousArgs().getByClass("home_name_or_player", String.class);
            if (sender.sender() instanceof Player player) {
                return plugin.getDatabase().getHomeInviteList(player.getUniqueId().toString()).join().get(playerName);
            } else {
                return null;
            }
        }))));
    }

    private int onCommandPlayer(Player player, CommandArguments arguments) {
        String homeName_or_Player = arguments.getByClass("home_name_or_player", String.class);
        plugin.getDatabase().getHomeList(player)
                .thenAccept(result_homeList -> {
                    int maxHomes = plugin.getConfig().getInt("maximumhomes");
                    List<PermissionAttachmentInfo> effectivePermissions = player.getEffectivePermissions().stream().toList();
                    for (PermissionAttachmentInfo permissionAttachmentInfo : effectivePermissions) {
                        String permission = permissionAttachmentInfo.getPermission().toLowerCase();
                        if (permission.startsWith("myhomes.maximumhomes.")) {
                            int maxHomesPermission = Integer.parseInt(permission.substring(21));
                            if (maxHomesPermission > maxHomes) {
                                maxHomes = maxHomesPermission;
                            }
                        }
                    }
                    if (result_homeList.size() > maxHomes && !player.hasPermission("myhomes.maxhomebypass")) {
                        String exceededHomes = Lang.PREFIX + Lang.TOO_MANY_HOMES.toString().replace("%maximum_number_of_homes%", Integer.toString(maxHomes));
                        player.sendMessage(exceededHomes);
                        return;
                    }

                   if (homeName_or_Player!=null && result_homeList.stream().noneMatch(homeName_or_Player::equalsIgnoreCase)) {
                       Fetcher.getPlayerUUID(homeName_or_Player).thenAccept(result_homeownerUUID -> {
                           String invitedHome = arguments.getByClass("invited_home", String.class);
                           if (invitedHome!=null) {
                               if (!invitedHome.matches("[a-zA-Z0-9]*")) {
                                   player.sendMessage(Lang.PREFIX.toString() + Lang.INVALID_CHARACTERS);
                                   return;
                               }
                               plugin.getDatabase().getHomeUsingHomeownerUUID(result_homeownerUUID, invitedHome)
                                       .thenAccept(result_home -> plugin.getDatabase().getHomePrivacyStatus(result_homeownerUUID, invitedHome)
                                       .thenAccept(result_privacyStatus -> plugin.getDatabase().getHomeInvitedPlayers(result_homeownerUUID, invitedHome)
                                       .thenAccept(result_homeInvitedPlayers -> {
                                           if (result_home.isEmpty()) {
                                               player.sendMessage(Lang.PREFIX.toString() + Lang.HOME_NOT_EXISTS);
                                               return;
                                           }
                                           if (result_privacyStatus.stream().anyMatch("public"::equalsIgnoreCase) || result_homeInvitedPlayers.stream().anyMatch(player.getName()::equalsIgnoreCase)) {
                                               World world = plugin.getServer().getWorld(result_home.get(0));
                                               Location homeLocation = new Location(world, Double.parseDouble(result_home.get(1)), Double.parseDouble(result_home.get(2)), Double.parseDouble(result_home.get(3)), Float.parseFloat(result_home.get(4)), Float.parseFloat(result_home.get(5)));

                                               player.teleportAsync(homeLocation, PlayerTeleportEvent.TeleportCause.COMMAND);
                                               if (invitedHome.equalsIgnoreCase("Home")) {
                                                   player.sendMessage(Lang.PREFIX + Lang.HOME_OTHER.toString().replace("%player%", homeName_or_Player));
                                               } else {
                                                   player.sendMessage(Lang.PREFIX + Lang.SPECIFIED_HOME.toString().replace("%home%", invitedHome));
                                               }
                                           } else {
                                               if (invitedHome.equalsIgnoreCase("Home")) {
                                                   player.sendMessage(Lang.PREFIX + Lang.PLAYER_NOT_INVITED.toString().replace("%player%", homeName_or_Player));
                                               } else {
                                                   player.sendMessage(Lang.PREFIX + Lang.PLAYER_NOT_INVITED_SPECIFIED.toString().replace("%home%", invitedHome));
                                               }
                                           }
                                       })));
                           } else {
                               plugin.getDatabase().getHomeUsingHomeownerUUID(result_homeownerUUID, "Home")
                                       .thenAccept(result_home -> plugin.getDatabase().getHomePrivacyStatus(result_homeownerUUID, "Home")
                                       .thenAccept(result_privacyStatus -> plugin.getDatabase().getHomeInvitedPlayers(result_homeownerUUID, "Home")
                                       .thenAccept(result_homeInvitedPlayers -> {
                                           if (result_home.isEmpty()) {
                                               player.sendMessage(Lang.PREFIX.toString() + Lang.HOME_NOT_EXISTS);
                                               return;
                                           }
                                           if (result_homeInvitedPlayers.stream().anyMatch(player.getName()::equalsIgnoreCase) || result_privacyStatus.stream().anyMatch("public"::equalsIgnoreCase)) {
                                               World world = MyHomes.getInstance().getServer().getWorld(result_home.get(0));
                                               Location homeLocation = new Location(world, Double.parseDouble(result_home.get(1)), Double.parseDouble(result_home.get(2)), Double.parseDouble(result_home.get(3)), Float.parseFloat(result_home.get(4)), Float.parseFloat(result_home.get(5)));

                                               player.teleportAsync(homeLocation, PlayerTeleportEvent.TeleportCause.COMMAND);
                                               player.sendMessage(Lang.PREFIX + Lang.HOME_OTHER.toString().replace("%player%", homeName_or_Player));
                                           } else if (result_homeInvitedPlayers.stream().noneMatch(player.getName()::equalsIgnoreCase)) {
                                               player.sendMessage(Lang.PREFIX + Lang.PLAYER_NOT_INVITED.toString().replace("%player%", homeName_or_Player));
                                           }
                                       })));
                           }
                       });
                   } else if (homeName_or_Player!=null && result_homeList.stream().anyMatch(homeName_or_Player::equalsIgnoreCase)){
                       if (!homeName_or_Player.matches("[a-zA-Z0-9]*")) {
                           player.sendMessage(Lang.PREFIX.toString() + Lang.HOME_NOT_EXISTS);
                           return;
                       }
                       plugin.getDatabase().getHome(player, homeName_or_Player).thenAccept(result_home -> {
                           if (result_home.isEmpty()) {
                               player.sendMessage(Lang.PREFIX.toString() + Lang.HOME_NOT_EXISTS);
                           } else {
                               World world = plugin.getServer().getWorld(result_home.get(0));
                               Location homeLocation = new Location(world, Double.parseDouble(result_home.get(1)), Double.parseDouble(result_home.get(2)), Double.parseDouble(result_home.get(3)), Float.parseFloat(result_home.get(4)), Float.parseFloat(result_home.get(5)));
                               player.teleportAsync(homeLocation, PlayerTeleportEvent.TeleportCause.COMMAND);

                               if (homeName_or_Player.equalsIgnoreCase("Home")) {
                                   player.sendMessage(Lang.PREFIX.toString() + Lang.HOME);
                               } else {
                                   player.sendMessage(Lang.PREFIX + Lang.SPECIFIED_HOME.toString().replace("%home%", homeName_or_Player));
                               }
                           }
                       });
                   } else {
                       plugin.getDatabase().getHome(player, "Home").thenAccept(result_home -> {
                           if (result_home.isEmpty()) {
                               player.sendMessage(Lang.PREFIX.toString() + Lang.PLAYER_HAS_NO_HOME);
                           } else {
                               World world = plugin.getServer().getWorld(result_home.get(0));
                               Location homeLocation = new Location(world, Double.parseDouble(result_home.get(1)), Double.parseDouble(result_home.get(2)), Double.parseDouble(result_home.get(3)), Float.parseFloat(result_home.get(4)), Float.parseFloat(result_home.get(5)));

                               player.teleportAsync(homeLocation, PlayerTeleportEvent.TeleportCause.COMMAND);
                               player.sendMessage(Lang.PREFIX.toString() + Lang.HOME);
                           }
                       });
                   }
                });
        return 1;
    }

    private int onCommandConsole(ConsoleCommandSender executor, CommandArguments arguments) {
        executor.sendMessage(Lang.PREFIX + Lang.PLAYER_ONLY.toString());
        return 1;
    }

}
