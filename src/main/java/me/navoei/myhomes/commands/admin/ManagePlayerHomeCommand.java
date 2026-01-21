package me.navoei.myhomes.commands.admin;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import me.navoei.myhomes.MyHomes;
import me.navoei.myhomes.language.Lang;
import me.navoei.myhomes.uuid.Fetcher;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ManagePlayerHomeCommand extends CommandAPICommand {

    MyHomes plugin;

    public ManagePlayerHomeCommand(MyHomes plugin) {
        super("manageplayerhome");
        this.plugin = plugin;
        this.withAliases("mphome");
        this.withFullDescription("Allows admins to manage other player's homes.");
        this.withPermission("myhomes.manageplayerhome");

        this.executesPlayer(this::onCommandPlayer);
        this.executesConsole(this::onCommandConsole);

        this.withArguments(new StringArgument("player").replaceSuggestions(ArgumentSuggestions.stringCollection((sender) -> {
            if (sender.sender() instanceof Player p) {
                if (!p.hasPermission("myhomes.manageplayerhome")) {
                    return null;
                }
            }
            List<String> playerNames = new ArrayList<>();
            Bukkit.getOnlinePlayers().forEach(player -> playerNames.add(player.getName()));
            return playerNames;
        })));
        this.withArguments(new StringArgument("home_name").replaceSuggestions(ArgumentSuggestions.stringCollectionAsync((sender) -> CompletableFuture.supplyAsync(() -> {
            String playerName = sender.previousArgs().getByClass("player", String.class);
            return Fetcher.getPlayerUUID(playerName).thenComposeAsync(playerUUID -> plugin.getDatabase().getHomeListUsingHomeownerUUIDAsynchronously(playerUUID)).join();
        }))));
        this.withOptionalArguments(new StringArgument("optional_argument").replaceSuggestions(ArgumentSuggestions.stringCollection((sender) -> List.of("invite", "uninvite", "privacy", "set", "delete", "listinvites", "info", "rename"))));
        this.withOptionalArguments(new StringArgument("sub_argument").replaceSuggestions(ArgumentSuggestions.stringCollectionAsync((sender) -> CompletableFuture.supplyAsync(() -> {
            String optionalArg = sender.previousArgs().getByClass("optional_argument", String.class);
            if (optionalArg==null || optionalArg.isEmpty()) return null;
            String playerName = sender.previousArgs().getByClass("player", String.class);
            String homeName = sender.previousArgs().getByClass("home_name", String.class);

            if (optionalArg.equalsIgnoreCase("invite")) {
                List<String> playerNames = new ArrayList<>();
                Bukkit.getOnlinePlayers().forEach(player -> playerNames.add(player.getName()));
                return playerNames;
            }
            if (optionalArg.equalsIgnoreCase("uninvite")) {
                return Fetcher.getPlayerUUID(playerName).thenComposeAsync(playerUUID -> plugin.getDatabase().getHomeInvitedPlayers(playerUUID, homeName)).join();
            }
            if (optionalArg.equalsIgnoreCase("privacy")) {
                return List.of("private", "public");
            }
            if (optionalArg.equalsIgnoreCase("rename")) {
                return List.of("<new_home_name>");
            }
            return null;
        }))));
    }

    private int onCommandPlayer(Player player, CommandArguments arguments) {
        String playerName = arguments.getByClass("player", String.class);
        String homeName = arguments.getByClass("home_name", String.class);
        String optionalArgument = arguments.getByClass("optional_argument", String.class);
        if (playerName==null || playerName.isEmpty() || homeName==null || homeName.isEmpty()) {
            player.sendMessage(Lang.PREFIX + Lang.INVALID_ARGUMENTS.toString());
            return 0;
        }

        if (optionalArgument==null) {
            Fetcher.getPlayerUUID(playerName)
                    .thenComposeAsync(playerUUID -> plugin.getDatabase().getHomeUsingHomeownerUUID(playerUUID, homeName))
                    .thenAccept(result_home -> {
                        if (result_home.isEmpty()) {
                            String playerHasNoHome = Lang.PREFIX + Lang.MANAGE_HOMES_PLAYER_HAS_NO_HOME.toString().replace("%player%", playerName).replace("%home%", homeName);
                            player.sendMessage(playerHasNoHome);
                            return;
                        }
                        World world = plugin.getServer().getWorld(result_home.get(0));
                        Location homeLocation = new Location(world, Double.parseDouble(result_home.get(1)), Double.parseDouble(result_home.get(2)), Double.parseDouble(result_home.get(3)), Float.parseFloat(result_home.get(4)), Float.parseFloat(result_home.get(5)));

                        player.teleportAsync(homeLocation, PlayerTeleportEvent.TeleportCause.COMMAND);

                        if (homeName.equalsIgnoreCase("home")) {
                            player.sendMessage(Lang.PREFIX + Lang.HOME_OTHER.toString().replace("%player%", playerName));
                        } else {
                            player.sendMessage(Lang.PREFIX + Lang.SPECIFIED_HOME.toString().replace("%home%", homeName));
                        }
                    });
        } else {
            //("invite", "uninvite", "privacy", "set", "delete", "listinvites", "info", "rename")

            if (optionalArgument.equalsIgnoreCase("invite")) {
                String invitedPlayerName = arguments.getByClass("sub_argument", String.class);
                if (invitedPlayerName==null || invitedPlayerName.isEmpty()) {
                    player.sendMessage(Lang.PREFIX + Lang.INVALID_ARGUMENTS.toString());
                    return 0;
                }
                Fetcher.getPlayerUUID(playerName)
                        .thenAccept(result_playerUUID -> plugin.getDatabase().getHomeUsingHomeownerUUID(result_playerUUID, homeName)
                        .thenAccept(result_home -> {
                            if (result_home.isEmpty()) {
                                String playerHasNoHome = Lang.PREFIX + Lang.MANAGE_HOMES_PLAYER_HAS_NO_HOME.toString().replace("%player%", playerName).replace("%home%", homeName);
                                player.sendMessage(playerHasNoHome);
                                return;
                            }
                            if (playerName.equalsIgnoreCase(invitedPlayerName)) {
                                player.sendMessage(Lang.PREFIX + Lang.MANAGE_HOMES_CANNOT_INVITE_SELF.toString().replace("%player%", playerName));
                                return;
                            }
                            Fetcher.checkPlayedBefore(invitedPlayerName)
                                    .thenAccept(result_playedBefore -> {
                                        if (result_playedBefore) {
                                            plugin.getDatabase().getHomeInvitedPlayers(result_playerUUID, homeName).thenAccept(result_homeInvitedPlayers -> {
                                                if (result_homeInvitedPlayers.stream().anyMatch(invitedPlayerName::equalsIgnoreCase)) {
                                                    if (homeName.equalsIgnoreCase("Home")) {
                                                        player.sendMessage(Lang.PREFIX + Lang.MANAGE_HOMES_PLAYER_ALREADY_INVITED_TO_DEFAULT_HOME.toString().replace("%invited_player%", invitedPlayerName).replace("%homeowner%", playerName));
                                                    } else {
                                                        player.sendMessage(Lang.PREFIX + Lang.MANAGE_HOMES_PLAYER_ALREADY_INVITED_TO_SPECIFIED_HOME.toString().replace("%invited_player%", invitedPlayerName).replace("%home%", homeName).replace("%homeowner%", playerName));
                                                    }
                                                } else {
                                                    if (homeName.equalsIgnoreCase("Home")) {
                                                        Fetcher.getPlayerUUID(invitedPlayerName).thenAccept(result_invitedPlayerUUID -> plugin.getDatabase().setInviteColumnsUsingHomeownerUUID(result_playerUUID, "Home", result_invitedPlayerUUID));
                                                        player.sendMessage(Lang.PREFIX + Lang.MANAGE_HOMES_INVITED_TO_DEFAULT_HOME.toString().replace("%invited_player%", invitedPlayerName).replace("%homeowner%", playerName));

                                                        Player invitedPlayer = plugin.getServer().getPlayer(invitedPlayerName);

                                                        if (invitedPlayer == null) return;

                                                        invitedPlayer.sendMessage(Lang.PREFIX + Lang.MESSAGE_TO_INVITED_PLAYER_DEFAULT_HOME.toString().replace("%homeowner%", playerName));

                                                    } else {
                                                        plugin.getDatabase().getHomeListUsingHomeownerUUIDAsynchronously(result_playerUUID).thenAccept(result_homeList -> {
                                                            List<String> result_homeListLowerCase = new ArrayList<>();
                                                            for (String home_name : result_homeList) {
                                                                result_homeListLowerCase.add(home_name.toLowerCase());
                                                            }
                                                            result_homeListLowerCase.replaceAll(String::toLowerCase);
                                                            String homeNameLowerCase = homeName.toLowerCase();
                                                            int homeNameWithCaseIndex = result_homeListLowerCase.indexOf(homeNameLowerCase);
                                                            String homeNameWithCase = result_homeList.get(homeNameWithCaseIndex);
                                                            Fetcher.getPlayerUUID(invitedPlayerName).thenAccept(result_invitedPlayerUUID -> plugin.getDatabase().setInviteColumnsUsingHomeownerUUID(result_playerUUID, homeNameWithCase, result_invitedPlayerUUID));
                                                            player.sendMessage(Lang.PREFIX + Lang.MANAGE_HOMES_INVITED_TO_SPECIFIED_HOME.toString().replace("%invited_player%", invitedPlayerName).replace("%home%", homeNameWithCase).replace("%homeowner%", playerName));

                                                            Player invitedPlayer = plugin.getServer().getPlayer(invitedPlayerName);

                                                            if (invitedPlayer == null) return;

                                                            invitedPlayer.sendMessage(Lang.PREFIX + Lang.MESSAGE_TO_INVITED_PLAYER_SPECIFIED_HOME.toString().replace("%home%", homeNameWithCase).replace("%homeowner%", playerName));

                                                        });
                                                    }
                                                }
                                            });
                                        } else {
                                            player.sendMessage(Lang.PREFIX + Lang.PLAYER_NEVER_LOGGED_ON.toString().replace("%player%", invitedPlayerName));
                                        }
                                    });
                        }));
                return 1;
            } else if (optionalArgument.equalsIgnoreCase("uninvite")) {
                String uninvitedPlayerName = arguments.getByClass("sub_argument", String.class);
                if (uninvitedPlayerName==null || uninvitedPlayerName.isEmpty()) {
                    player.sendMessage(Lang.PREFIX + Lang.INVALID_ARGUMENTS.toString());
                    return 0;
                }
                Fetcher.getPlayerUUID(playerName)
                        .thenAccept(result_playerUUID -> plugin.getDatabase().getHomeUsingHomeownerUUID(result_playerUUID, homeName)
                        .thenAccept(result_home -> {
                            if (result_home.isEmpty()) {
                                String playerHasNoHome = Lang.PREFIX + Lang.MANAGE_HOMES_PLAYER_HAS_NO_HOME.toString().replace("%player%", playerName).replace("%home%", homeName);
                                player.sendMessage(Lang.PREFIX + playerHasNoHome);
                                return;
                            }
                            if (playerName.equalsIgnoreCase(uninvitedPlayerName)) {
                                player.sendMessage(Lang.PREFIX + Lang.MANAGE_HOMES_CANNOT_UNINVITE_SELF.toString().replace("%player%", playerName));
                                return;
                            }
                            plugin.getDatabase().getHomeInvitedPlayers(result_playerUUID, homeName).thenAccept(result_homeInvitedPlayers -> {
                                if (result_homeInvitedPlayers.stream().noneMatch(uninvitedPlayerName::equalsIgnoreCase)) {
                                    if (homeName.equalsIgnoreCase("Home")) {
                                        player.sendMessage(Lang.PREFIX + Lang.MANAGE_HOMES_HAS_NOT_BEEN_INVITED_TO_DEFAULT_HOME.toString().replace("%uninvited_player%", uninvitedPlayerName).replace("%homeowner%", playerName));
                                    } else {
                                        player.sendMessage(Lang.PREFIX + Lang.MANAGE_HOMES_HAS_NOT_BEEN_INVITED_TO_SPECIFIED_HOME.toString().replace("%uninvited_player%", uninvitedPlayerName).replace("%home%", homeName).replace("%homeowner%", playerName));
                                    }
                                } else {
                                    Fetcher.getPlayerUUID(uninvitedPlayerName).thenAccept(result_uninvitedPlayerUUID -> plugin.getDatabase().deleteInviteColumnsUsingHomeownerUUID(result_playerUUID, homeName, result_uninvitedPlayerUUID));
                                    if (homeName.equalsIgnoreCase("Home")) {
                                        player.sendMessage(Lang.PREFIX + Lang.MANAGE_HOMES_UNINVITED_FROM_DEFAULT_HOME.toString().replace("%uninvited_player%", uninvitedPlayerName).replace("%homeowner%", playerName));
                                    } else {
                                        player.sendMessage(Lang.PREFIX + Lang.MANAGE_HOMES_UNINVITED_FROM_SPECIFIED_HOME.toString().replace("%uninvited_player%", uninvitedPlayerName).replace("%home%", homeName).replace("%homeowner%", playerName));
                                    }
                                }
                            });
                        }));
                return 1;
            } else if (optionalArgument.equalsIgnoreCase("privacy")) {
                String newPrivacyStatus = arguments.getByClass("sub_argument", String.class);
                if (newPrivacyStatus==null || newPrivacyStatus.isEmpty()) {
                    player.sendMessage(Lang.PREFIX + Lang.INVALID_ARGUMENTS.toString());
                    return 0;
                }
                Fetcher.getPlayerUUID(playerName).thenAccept(result_playerUUID -> plugin.getDatabase().getHomeUsingHomeownerUUID(result_playerUUID, homeName).thenAccept(result_home -> {
                    if (result_home.isEmpty()) {
                        String playerHasNoHome = Lang.PREFIX + Lang.MANAGE_HOMES_PLAYER_HAS_NO_HOME.toString().replace("%player%", playerName).replace("%home%", homeName);
                        player.sendMessage(Lang.PREFIX + playerHasNoHome);
                        return;
                    }

                    if (newPrivacyStatus.equalsIgnoreCase("private")) {
                        plugin.getDatabase().updatePrivacyStatusUsingHomeownerUUID(result_playerUUID, homeName, false);

                        if (homeName.equalsIgnoreCase("Home")) {
                            player.sendMessage(Lang.PREFIX + Lang.MANAGE_HOMES_PRIVACY_STATUS_PRIVATE_DEFAULT_HOME.toString().replace("%homeowner%", playerName));
                        } else {
                            player.sendMessage(Lang.PREFIX + Lang.MANAGE_HOMES_PRIVACY_STATUS_PRIVATE_SPECIFIED_HOME.toString().replace("%home%", homeName).replace("%homeowner%", playerName));
                        }
                    } else if (newPrivacyStatus.equalsIgnoreCase("public")) {
                        plugin.getDatabase().updatePrivacyStatusUsingHomeownerUUID(result_playerUUID, homeName, true);

                        if (homeName.equalsIgnoreCase("Home")) {
                            player.sendMessage(Lang.PREFIX + Lang.MANAGE_HOMES_PRIVACY_STATUS_PUBLIC_DEFAULT_HOME.toString().replace("%homeowner%", playerName));
                        } else {
                            player.sendMessage(Lang.PREFIX + Lang.MANAGE_HOMES_PRIVACY_STATUS_PUBLIC_SPECIFIED_HOME.toString().replace("%home%", homeName).replace("%homeowner%", playerName));
                        }
                    } else {
                        player.sendMessage(Lang.PREFIX.toString() + Lang.INVALID_ARGUMENTS);
                    }

                }));
                return 1;
            } else if (optionalArgument.equalsIgnoreCase("rename")) {
                String newHomeName = arguments.getByClass("sub_argument", String.class);
                if (newHomeName==null || newHomeName.isEmpty()) {
                    player.sendMessage(Lang.PREFIX + Lang.INVALID_ARGUMENTS.toString());
                    return 0;
                }
                Fetcher.getPlayerUUID(playerName).thenAccept(result_playerUUID -> plugin.getDatabase().getHomeUsingHomeownerUUID(result_playerUUID, homeName).thenAccept(result_home -> {
                    if (result_home.isEmpty()) {
                        String playerHasNoHome = Lang.PREFIX + Lang.MANAGE_HOMES_PLAYER_HAS_NO_HOME.toString().replace("%player%", playerName).replace("%home%", homeName);
                        player.sendMessage(Lang.PREFIX + playerHasNoHome);
                        return;
                    }
                    if (homeName.equalsIgnoreCase(newHomeName)) {
                        player.sendMessage(Lang.PREFIX + Lang.MANAGE_HOMES_SAME_NAME.toString().replace("%home%", newHomeName));
                        return;
                    }
                    if (!newHomeName.matches("[a-zA-Z0-9]*")) {
                        player.sendMessage(Lang.PREFIX.toString() + Lang.INVALID_CHARACTERS);
                        return;
                    }
                    int characterLimit = plugin.getConfig().getInt("characterlimit");
                    if (newHomeName.length() > characterLimit) {
                        player.sendMessage(Lang.PREFIX + Lang.TOO_MANY_CHARACTERS.toString().replace("%character_limit%", Integer.toString(characterLimit)));
                        return;
                    }

                    plugin.getDatabase().updateHomeName(result_playerUUID, homeName, newHomeName);
                    plugin.getDatabase().updateInviteColumnsNewHomeName(result_playerUUID, homeName, newHomeName);
                    player.sendMessage(Lang.PREFIX + Lang.MANAGE_HOMES_RENAME_HOME.toString().replace("%homeowner%", playerName).replace("%previous_home_name%", homeName).replace("%new_home_name%", newHomeName));

                }));
                return 1;
            } else if (optionalArgument.equalsIgnoreCase("set")) {
                Fetcher.checkPlayedBefore(playerName).thenAccept(result_playedBefore -> {
                    if (!result_playedBefore) {
                        player.sendMessage(Lang.PREFIX + Lang.PLAYER_NEVER_LOGGED_ON.toString().replace("%player%", playerName));
                        return;
                    }
                    Fetcher.getPlayerUUID(playerName).thenAccept(result_playerUUID -> plugin.getDatabase().getHomeListUsingHomeownerUUIDAsynchronously(result_playerUUID).thenAccept(result_homeList -> {

                        int maxHomes = plugin.getConfig().getInt("maximumhomes");

                        if (result_homeList.stream().noneMatch(homeName::equalsIgnoreCase)) {
                            int characterLimit = plugin.getConfig().getInt("characterlimit");
                            if (homeName.length() > characterLimit) {
                                player.sendMessage(Lang.PREFIX + Lang.TOO_MANY_CHARACTERS.toString().replace("%character_limit%", Integer.toString(characterLimit)));
                                return;
                            }
                            if (!homeName.matches("[a-zA-Z0-9]*")) {
                                player.sendMessage(Lang.PREFIX.toString() + Lang.INVALID_CHARACTERS);
                                return;
                            }
                            if (!player.hasPermission("myhomes.maxhomebypass") && result_homeList.size() >= maxHomes) {
                                String exceededHomes = Lang.PREFIX + Lang.TOO_MANY_HOMES.toString().replace("%maximum_number_of_homes%", Integer.toString(maxHomes));
                                player.sendMessage(exceededHomes);
                                return;
                            }

                            if (homeName.equalsIgnoreCase("Home")) {
                                player.sendMessage(Lang.PREFIX + Lang.MANAGE_HOMES_SET_PLAYER_DEFAULT_HOME.toString().replace("%player%", playerName));
                                plugin.getDatabase().setHomeColumnsUsingHomeownerUUID(result_playerUUID, player, "Home", false);
                            } else {
                                player.sendMessage(Lang.PREFIX + Lang.MANAGE_HOMES_SET_PLAYER_SPECIFIED_HOME.toString().replace("%home%", homeName).replace("%player%", playerName));
                                plugin.getDatabase().setHomeColumnsUsingHomeownerUUID(result_playerUUID, player, homeName, false);
                            }
                        } else {
                            if (homeName.equalsIgnoreCase("Home")) {
                                player.sendMessage(Lang.PREFIX + Lang.MANAGE_HOMES_UPDATED_LOCATION_DEFAULT_HOME.toString().replace("%homeowner%", playerName));
                                plugin.getDatabase().updateHomeLocationUsingHomeownerUUID(result_playerUUID, player, "Home");
                            } else {
                                player.sendMessage(Lang.PREFIX + Lang.MANAGE_HOMES_UPDATED_LOCATION_SPECIFIED_HOME.toString().replace("%home%", homeName).replace("%homeowner%", playerName));
                                plugin.getDatabase().updateHomeLocationUsingHomeownerUUID(result_playerUUID, player, homeName);
                            }
                        }

                    }));
                });
                return 1;
            } else if (optionalArgument.equalsIgnoreCase("delete")) {

                Fetcher.getPlayerUUID(playerName).thenAccept(result_playerUUID -> plugin.getDatabase().getHomeListUsingHomeownerUUIDAsynchronously(result_playerUUID).thenAccept(result_homeList -> {

                    if (result_homeList.stream().noneMatch(homeName::equalsIgnoreCase)) {
                        String playerHasNoHome = Lang.PREFIX + Lang.MANAGE_HOMES_PLAYER_HAS_NO_HOME.toString().replace("%player%", playerName).replace("%home%", homeName);
                        player.sendMessage(Lang.PREFIX + playerHasNoHome);
                        return;
                    }

                    if (homeName.equalsIgnoreCase("home")) {
                        player.sendMessage(Lang.PREFIX + Lang.MANAGE_HOMES_DELETE_DEFAULT_HOME.toString().replace("%player%", playerName));
                    } else {
                        player.sendMessage(Lang.PREFIX + Lang.MANAGE_HOMES_DELETE_SPECIFIED_HOME.toString().replace("%home%", homeName).replace("%homeowner%", playerName));
                    }

                    plugin.getDatabase().deleteHomeUsingHomeownerUUID(result_playerUUID, homeName);
                    plugin.getDatabase().deleteAllInviteColumnsUsingHomeownerUUID(result_playerUUID, homeName);

                }));

                return 1;
            } else if (optionalArgument.equalsIgnoreCase("listinvites")) {
                Fetcher.getPlayerUUID(playerName).thenAccept(result_playerUUID -> plugin.getDatabase().getHomeListUsingHomeownerUUIDAsynchronously(result_playerUUID).thenAccept(result_homeList -> {

                    if (result_homeList.stream().noneMatch(homeName::equalsIgnoreCase)) {
                        String playerHasNoHome = Lang.PREFIX + Lang.MANAGE_HOMES_PLAYER_HAS_NO_HOME.toString().replace("%player%", playerName).replace("%home%", homeName);
                        player.sendMessage(Lang.PREFIX + playerHasNoHome);
                        return;
                    }

                    plugin.getDatabase().getHomeInvitedPlayers(result_playerUUID, homeName).thenAccept(result_homeInvitedPlayersList -> {
                        String invitedPlayersList = result_homeInvitedPlayersList.toString().substring(1, result_homeInvitedPlayersList.toString().length() - 1);

                        if (invitedPlayersList.isEmpty()) {
                            player.sendMessage(Lang.PREFIX.toString() + Lang.NO_INVITES_TO_HOME);
                            return;
                        }

                        List<String> messageList = plugin.getLang().getStringList("listinvitestohome");

                        for (String message : messageList) {
                            player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(message.replace("%home%", homeName).replace("%invites_list%", invitedPlayersList)));
                        }

                    });

                }));
                return 1;
            } else if (optionalArgument.equalsIgnoreCase("info")) {
                Fetcher.getPlayerUUID(playerName).thenAccept(result_playerUUID -> plugin.getDatabase().getHomeListUsingHomeownerUUIDAsynchronously(result_playerUUID).thenAccept(result_homeList -> {
                    if (result_homeList.stream().noneMatch(homeName::equalsIgnoreCase)) {
                        String playerHasNoHome = Lang.PREFIX + Lang.MANAGE_HOMES_PLAYER_HAS_NO_HOME.toString().replace("%player%", playerName).replace("%home%", homeName);
                        player.sendMessage(Lang.PREFIX + playerHasNoHome);
                        return;
                    }

                    plugin.getDatabase().getHomeInfoUsingHomeownerUUID(result_playerUUID, homeName).thenAccept(result_homeInfo -> {

                        List<String> messageList = plugin.getLang().getStringList("homeinfo");

                        String name = result_homeInfo.get(0);
                        String world = result_homeInfo.get(1);
                        String x = String.valueOf((int)Double.parseDouble(result_homeInfo.get(2)));
                        String y = String.valueOf((int)Double.parseDouble(result_homeInfo.get(3)));
                        String z = String.valueOf((int)Double.parseDouble(result_homeInfo.get(4)));
                        String privacy = result_homeInfo.get(5);

                        for (String message : messageList) {
                            player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(message.replace("%home_name%", name).replace("%home_x%", x).replace("%home_y%", y).replace("%home_z%", z).replace("%home_world%", world).replace("%privacy_status%", privacy)));
                        }

                    });

                }));
                return 1;
            } else {
                player.sendMessage(Lang.PREFIX + Lang.INVALID_ARGUMENTS.toString());
            }

        }

        return 1;
    }

    private int onCommandConsole(ConsoleCommandSender executor, CommandArguments arguments) {
        executor.sendMessage(Lang.PREFIX + Lang.PLAYER_ONLY.toString());
        return 1;
    }

}
