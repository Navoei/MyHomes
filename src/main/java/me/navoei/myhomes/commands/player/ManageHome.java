package me.navoei.myhomes.commands.player;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import me.navoei.myhomes.MyHomes;
import me.navoei.myhomes.language.Lang;
import me.navoei.myhomes.uuid.Fetcher;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ManageHome extends CommandAPICommand {

    MyHomes plugin;

    public ManageHome(MyHomes plugin) {
        super("managehome");
        this.plugin = plugin;
        this.withFullDescription("Manages the specified home.");
        this.withPermission("myhomes.managehome");
        this.withAliases("mhome");

        this.executesPlayer(this::onCommandPlayer);
        this.executesConsole(this::onCommandConsole);

        this.withArguments(new StringArgument("home_name").replaceSuggestions(ArgumentSuggestions.stringCollectionAsync((sender) -> CompletableFuture.supplyAsync(() -> {
            if (sender.sender() instanceof Player player) {
                return plugin.getDatabase().getHomeListUsingHomeownerUUIDAsynchronously(player.getUniqueId().toString()).join();
            } else {
                return null;
            }
        }))));
        this.withArguments(new StringArgument("argument").replaceSuggestions(ArgumentSuggestions.stringCollection((sender) -> List.of("invite", "uninvite", "privacy", "listinvites", "info", "rename"))));
        this.withOptionalArguments(new StringArgument("sub_argument").replaceSuggestions(ArgumentSuggestions.stringCollectionAsync((sender) -> CompletableFuture.supplyAsync(() -> {
            if (!(sender.sender() instanceof Player player)) return null;
            String argument = sender.previousArgs().getByClass("argument", String.class);
            if (argument==null || argument.isEmpty()) return null;
            String homeName = sender.previousArgs().getByClass("home_name", String.class);

            if (argument.equalsIgnoreCase("invite")) {
                List<String> playerNames = new ArrayList<>();
                Bukkit.getOnlinePlayers().forEach(bukkitPlayer -> playerNames.add(bukkitPlayer.getName()));
                return playerNames;
            }
            if (argument.equalsIgnoreCase("uninvite")) {
                return plugin.getDatabase().getHomeInvitedPlayers(player.getUniqueId().toString(), homeName).join();
            }
            if (argument.equalsIgnoreCase("privacy")) {
                return List.of("private", "public");
            }
            if (argument.equalsIgnoreCase("rename")) {
                return List.of("<new_home_name>");
            }
            return null;
        }))));

    }

    private int onCommandPlayer(Player player, CommandArguments arguments) {
        String homeName = arguments.getByClass("home_name", String.class);
        String argument = arguments.getByClass("argument", String.class);
        String subArgument = arguments.getByClass("sub_argument", String.class);
        if (homeName==null || homeName.isEmpty() || argument==null || argument.isEmpty()) {
            player.sendMessage(Lang.PREFIX + Lang.INVALID_ARGUMENTS.toString());
            return 0;
        }
        if (!homeName.matches("[a-zA-Z0-9]*")) {
            player.sendMessage(Lang.PREFIX.toString() + Lang.HOME_NOT_EXISTS);
            return 0;
        }

        plugin.getDatabase().getHomeInfo(player, homeName)
                .thenAccept(result_home -> {
                    if (result_home.isEmpty()) {
                        player.sendMessage(Lang.PREFIX.toString() + Lang.HOME_NOT_EXISTS);
                        return;
                    }

                    if (argument.equalsIgnoreCase("invite")) {
                        if (subArgument==null || subArgument.isEmpty()) {
                            player.sendMessage(Lang.PREFIX + Lang.INVALID_ARGUMENTS.toString());
                            return;
                        }
                        if (subArgument.equalsIgnoreCase(player.getName())) {
                            player.sendMessage(Lang.PREFIX.toString() + Lang.CANNOT_INVITE_SELF);
                            return;
                        }
                        String invitedPlayerName = subArgument;
                        Fetcher.checkPlayedBefore(invitedPlayerName).thenAccept(result_playedBefore -> {
                            if (result_playedBefore) {
                                plugin.getDatabase().getHomeInvitedPlayers(player.getUniqueId().toString(), homeName)
                                        .thenAccept(result_homeInvitedPlayers -> Fetcher.getPlayerUUID(invitedPlayerName)
                                        .thenAccept(result_invitedPlayerUUID -> {
                                            if (result_homeInvitedPlayers.stream().anyMatch(invitedPlayerName::equalsIgnoreCase)) {
                                                if (homeName.equalsIgnoreCase("Home")) {
                                                    player.sendMessage(Lang.PREFIX + Lang.ALREADY_INVITED_TO_DEFAULT_HOME.toString().replace("%player%", invitedPlayerName));
                                                } else {
                                                    player.sendMessage(Lang.PREFIX + Lang.ALREADY_INVITED_TO_SPECIFIED_HOME.toString().replace("%player%", invitedPlayerName).replace("%home%", homeName));
                                                }
                                            } else {
                                                if (homeName.equalsIgnoreCase("Home")) {
                                                    plugin.getDatabase().setInviteColumns(player, "Home", result_invitedPlayerUUID);
                                                    player.sendMessage(Lang.PREFIX + Lang.INVITED_TO_DEFAULT_HOME.toString().replace("%player%", invitedPlayerName));

                                                    Player invitedPlayer = plugin.getServer().getPlayerExact(invitedPlayerName);

                                                    if (invitedPlayer == null) return;

                                                    invitedPlayer.sendMessage(Lang.PREFIX + Lang.MESSAGE_TO_INVITED_PLAYER_DEFAULT_HOME.toString().replace("%homeowner%", player.getName()));
                                                } else {
                                                    plugin.getDatabase().getHomeList(player).thenAccept(result_homeList -> {
                                                        List<String> result_homeListLowerCase = new ArrayList<>();
                                                        for (String home_name : result_homeList) {
                                                            result_homeListLowerCase.add(home_name.toLowerCase());
                                                        }
                                                        result_homeListLowerCase.replaceAll(String::toLowerCase);
                                                        String homeNameLowerCase = homeName.toLowerCase();
                                                        int homeNameWithCaseIndex = result_homeListLowerCase.indexOf(homeNameLowerCase);
                                                        String homeNameWithCase = result_homeList.get(homeNameWithCaseIndex);

                                                        plugin.getDatabase().setInviteColumns(player, homeNameWithCase, result_invitedPlayerUUID);
                                                        player.sendMessage(Lang.PREFIX + Lang.INVITED_TO_SPECIFIED_HOME.toString().replace("%player%", invitedPlayerName).replace("%home%", homeNameWithCase));

                                                        Player invitedPlayer = plugin.getServer().getPlayerExact(invitedPlayerName);

                                                        if (invitedPlayer == null) return;

                                                        invitedPlayer.sendMessage(Lang.PREFIX + Lang.MESSAGE_TO_INVITED_PLAYER_SPECIFIED_HOME.toString().replace("%home%", homeNameWithCase).replace("%homeowner%", player.getName()));

                                                    });
                                                }
                                            }
                                        }));
                            } else {
                                player.sendMessage(Lang.PREFIX + Lang.PLAYER_NEVER_LOGGED_ON.toString().replace("%player%", invitedPlayerName));
                            }
                        });

                    } else if (argument.equalsIgnoreCase("uninvite")) {
                        if (subArgument==null || subArgument.isEmpty()) {
                            player.sendMessage(Lang.PREFIX + Lang.INVALID_ARGUMENTS.toString());
                            return;
                        }
                        if (subArgument.equalsIgnoreCase(player.getName())) {
                            player.sendMessage(Lang.PREFIX.toString() + Lang.CANNOT_UNINVITE_SELF);
                            return;
                        }
                        String uninvitedPlayerName = subArgument;
                        plugin.getDatabase().getHomeInvitedPlayers(player.getUniqueId().toString(), homeName).thenAccept(result_homeInvitedPlayers -> {

                            if (result_homeInvitedPlayers.stream().noneMatch(uninvitedPlayerName::equalsIgnoreCase)) {
                                if (homeName.equalsIgnoreCase("Home")) {
                                    player.sendMessage(Lang.PREFIX + Lang.NOT_INVITED_TO_DEFAULT_HOME.toString().replace("%player%", uninvitedPlayerName));
                                } else {
                                    player.sendMessage(Lang.PREFIX + Lang.NOT_INVITED_TO_SPECIFIED_HOME.toString().replace("%player%", uninvitedPlayerName).replace("%home%", homeName));
                                }
                            } else {
                                Fetcher.getPlayerUUID(uninvitedPlayerName).thenAccept(result_uninvitedPlayerUUID -> plugin.getDatabase().deleteInviteColumns(player, homeName, result_uninvitedPlayerUUID));
                                if (homeName.equalsIgnoreCase("Home")) {
                                    player.sendMessage(Lang.PREFIX + Lang.UNINVITED_FROM_DEFAULT_HOME.toString().replace("%player%", uninvitedPlayerName));
                                } else {
                                    player.sendMessage(Lang.PREFIX + Lang.UNINVITED_FROM_SPECIFIED_HOME.toString().replace("%player%", uninvitedPlayerName).replace("%home%", homeName));
                                }
                            }
                        });
                    } else if (argument.equalsIgnoreCase("listinvites")) {
                        plugin.getDatabase().getHomeInvitedPlayers(player.getUniqueId().toString(), homeName).thenAccept(result_homeInvitedPlayersList -> {
                            String invitedPlayersList = result_homeInvitedPlayersList.toString().substring(1, result_homeInvitedPlayersList.toString().length()-1);

                            if (invitedPlayersList.isEmpty()) {
                                player.sendMessage(Lang.PREFIX.toString() + Lang.NO_INVITES_TO_HOME);
                                return;
                            }

                            List<String> messageList = plugin.getLang().getStringList("listinvitestohome");

                            for (String message : messageList) {
                                player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(message.replace("%home%", homeName).replace("%invites_list%", invitedPlayersList)));
                            }

                        });
                    } else if (argument.equalsIgnoreCase("info")) {
                        plugin.getDatabase().getHomeInfo(player, homeName).thenAccept(result_homeInfo -> {

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
                    } else if (argument.equalsIgnoreCase("privacy")) {
                        if (subArgument==null || subArgument.isEmpty()) {
                            player.sendMessage(Lang.PREFIX + Lang.INVALID_ARGUMENTS.toString());
                            return;
                        }
                        String privacy = subArgument;

                        if (privacy.equalsIgnoreCase("private")) {
                            plugin.getDatabase().updatePrivacyStatus(player, homeName, false);

                            if (homeName.equalsIgnoreCase("Home")) {
                                player.sendMessage(Lang.PREFIX.toString() + Lang.DEFAULT_HOME_PRIVATE);
                            } else {
                                player.sendMessage(Lang.PREFIX + Lang.SPECIFIED_HOME_PRIVATE.toString().replace("%home%", homeName));
                            }

                        } else if (privacy.equalsIgnoreCase("public")) {
                            plugin.getDatabase().updatePrivacyStatus(player, homeName, true);

                            if (homeName.equalsIgnoreCase("Home")) {
                                player.sendMessage(Lang.PREFIX.toString() + Lang.DEFAULT_HOME_PUBLIC);
                            } else {
                                player.sendMessage(Lang.PREFIX + Lang.SPECIFIED_HOME_PUBLIC.toString().replace("%home%", homeName));
                            }

                        } else {
                            player.sendMessage(Lang.PREFIX.toString() + Lang.INVALID_ARGUMENTS);
                        }

                    } else if (argument.equalsIgnoreCase("rename")) {
                        if (subArgument==null || subArgument.isEmpty()) {
                            player.sendMessage(Lang.PREFIX + Lang.INVALID_ARGUMENTS.toString());
                            return;
                        }
                        String newHomeName = subArgument;
                        if (homeName.equalsIgnoreCase(newHomeName)) {
                            player.sendMessage(Lang.PREFIX + Lang.RENAME_HOME_SAME_NAME.toString().replace("%home%", newHomeName));
                            return;
                        }
                        if (!newHomeName.matches("[a-zA-Z0-9]*")) {
                            player.sendMessage(Lang.PREFIX.toString() + Lang.INVALID_CHARACTERS);
                            return;
                        }

                        plugin.getDatabase().updateHomeName(player.getUniqueId().toString(), homeName, newHomeName);
                        plugin.getDatabase().updateInviteColumnsNewHomeName(player.getUniqueId().toString(), homeName, newHomeName);
                        player.sendMessage(Lang.PREFIX + Lang.RENAME_HOME.toString().replace("%previous_home_name%", homeName).replace("%new_home_name%", newHomeName));
                    } else {
                        player.sendMessage(Lang.PREFIX + Lang.INVALID_ARGUMENTS.toString());
                    }
                });

        return 1;
    }

    private int onCommandConsole(ConsoleCommandSender executor, CommandArguments arguments) {
        executor.sendMessage(Lang.PREFIX + Lang.PLAYER_ONLY.toString());
        return 1;
    }

}
