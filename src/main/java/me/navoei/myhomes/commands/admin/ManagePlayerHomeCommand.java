package me.navoei.myhomes.commands.admin;

import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import me.navoei.myhomes.MyHomes;
import me.navoei.myhomes.language.Lang;
import me.navoei.myhomes.uuid.Fetcher;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ManagePlayerHomeCommand implements CommandExecutor, Listener, TabCompleter {

    MyHomes plugin = MyHomes.getInstance();
    BukkitScheduler scheduler = plugin.getServer().getScheduler();
    Fetcher uuidFetcher = new Fetcher();
    private final String[] SUB_COMMANDS = { "invite", "uninvite", "privacy", "set", "delete", "listinvites", "info", "rename" };
    private final String[] PRIVACY_STATUS_OPTIONS = { "private", "public" };

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        int characterLimit = plugin.getConfig().getInt("characterlimit");

        if (!sender.hasPermission("myhomes.manageplayerhome")) {
            sender.sendMessage(Lang.PREFIX.toString() + Lang.NO_PERMISSION);
            return true;
        }

        if (args.length > 4) {
            sender.sendMessage(Lang.PREFIX.toString() + Lang.TOO_MANY_ARGUMENTS);
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(Lang.PREFIX.toString() + Lang.NOT_ENOUGH_ARGUMENTS);
            return true;
        }

        String playerName = args[0];
        String homeName = args[1];

        if (!homeName.matches("[a-zA-Z0-9]*")) {
            sender.sendMessage(Lang.PREFIX.toString() + Lang.INVALID_CHARACTERS);
            return true;
        }

        if (homeName.length() > characterLimit) {
            sender.sendMessage(Lang.PREFIX + Lang.TOO_MANY_CHARACTERS.toString().replace("%character_limit%", Integer.toString(characterLimit)));
            return true;
        }

        String playerHasNoHome = Lang.MANAGE_HOMES_PLAYER_HAS_NO_HOME.toString().replace("%player%", playerName).replace("%home%", homeName);

        if (args.length == 4) {

            if (args[2].equalsIgnoreCase("invite")) {

                String invitedPlayerName = args[3];

                uuidFetcher.getOfflinePlayerUUIDFromMojang(playerName).thenAccept(result_playerUUID -> plugin.getRDatabase().getHomeUsingHomeownerUUID(result_playerUUID, homeName).thenAccept(result_home -> {

                    if (result_home.isEmpty()) {
                        sender.sendMessage(Lang.PREFIX + playerHasNoHome);
                        return;
                    }

                    if (playerName.equalsIgnoreCase(invitedPlayerName)) {
                        sender.sendMessage(Lang.PREFIX + Lang.MANAGE_HOMES_CANNOT_INVITE_SELF.toString().replace("%player%", playerName));
                        return;
                    }

                    uuidFetcher.checkPlayedBefore(invitedPlayerName).thenAccept(result_playedBefore -> {

                        if (result_playedBefore) {

                            plugin.getRDatabase().getHomeInvitedPlayersAsync(result_playerUUID, homeName).thenAccept(result_homeInvitedPlayers -> {
                                if (result_homeInvitedPlayers.stream().anyMatch(invitedPlayerName::equalsIgnoreCase)) {
                                    if (homeName.equalsIgnoreCase("Home")) {
                                        sender.sendMessage(Lang.PREFIX + Lang.MANAGE_HOMES_PLAYER_ALREADY_INVITED_TO_DEFAULT_HOME.toString().replace("%invited_player%", invitedPlayerName).replace("%homeowner%", playerName));
                                    } else {
                                        sender.sendMessage(Lang.PREFIX + Lang.MANAGE_HOMES_PLAYER_ALREADY_INVITED_TO_SPECIFIED_HOME.toString().replace("%invited_player%", invitedPlayerName).replace("%home%", homeName).replace("%homeowner%", playerName));
                                    }
                                } else {
                                    if (homeName.equalsIgnoreCase("Home")) {
                                        scheduler.runTaskAsynchronously(plugin, () -> plugin.getRDatabase().setInviteColumnsUsingHomeownerUUID(result_playerUUID, "Home", uuidFetcher.getOfflinePlayerUUIDFromMojang(invitedPlayerName).join()));
                                        sender.sendMessage(Lang.PREFIX + Lang.MANAGE_HOMES_INVITED_TO_DEFAULT_HOME.toString().replace("%invited_player%", invitedPlayerName).replace("%homeowner%", playerName));

                                        Player invitedPlayer = plugin.getServer().getPlayer(invitedPlayerName);

                                        if (invitedPlayer == null) return;

                                        invitedPlayer.sendMessage(Lang.PREFIX + Lang.MESSAGE_TO_INVITED_PLAYER_DEFAULT_HOME.toString().replace("%homeowner%", playerName));

                                    } else {
                                        scheduler.runTaskAsynchronously(plugin, () -> plugin.getRDatabase().setInviteColumnsUsingHomeownerUUID(result_playerUUID, homeName, uuidFetcher.getOfflinePlayerUUIDFromMojang(invitedPlayerName).join()));
                                        sender.sendMessage(Lang.PREFIX + Lang.MANAGE_HOMES_INVITED_TO_SPECIFIED_HOME.toString().replace("%invited_player%", invitedPlayerName).replace("%home%", homeName).replace("%homeowner%", playerName));

                                        Player invitedPlayer = plugin.getServer().getPlayer(invitedPlayerName);

                                        if (invitedPlayer == null) return;

                                        invitedPlayer.sendMessage(Lang.PREFIX + Lang.MESSAGE_TO_INVITED_PLAYER_SPECIFIED_HOME.toString().replace("%home%", homeName).replace("%homeowner%", playerName));
                                    }
                                }

                            });

                        } else {
                            sender.sendMessage(Lang.PREFIX + Lang.PLAYER_NEVER_LOGGED_ON.toString().replace("%player%", invitedPlayerName));
                        }

                    });

                }));

                return true;

            } else if (args[2].equalsIgnoreCase("uninvite")) {

                String uninvitedPlayerName = args[3];

                uuidFetcher.getOfflinePlayerUUIDFromMojang(playerName).thenAccept(result_playerUUID -> plugin.getRDatabase().getHomeUsingHomeownerUUID(result_playerUUID, homeName).thenAccept(result_home -> {

                    if (result_home.isEmpty()) {
                        sender.sendMessage(Lang.PREFIX + playerHasNoHome);
                        return;
                    }

                    if (playerName.equalsIgnoreCase(uninvitedPlayerName)) {
                        sender.sendMessage(Lang.PREFIX + Lang.MANAGE_HOMES_CANNOT_UNINVITE_SELF.toString().replace("%player%", playerName));
                        return;
                    }

                    plugin.getRDatabase().getHomeInvitedPlayersAsync(result_playerUUID, homeName).thenAccept(result_homeInvitedPlayers -> {
                       if (result_homeInvitedPlayers.stream().noneMatch(uninvitedPlayerName::equalsIgnoreCase)) {
                           if (homeName.equalsIgnoreCase("Home")) {
                               sender.sendMessage(Lang.PREFIX + Lang.MANAGE_HOMES_HAS_NOT_BEEN_INVITED_TO_DEFAULT_HOME.toString().replace("%uninvited_player%", uninvitedPlayerName).replace("%homeowner%", playerName));
                           } else {
                               sender.sendMessage(Lang.PREFIX + Lang.MANAGE_HOMES_HAS_NOT_BEEN_INVITED_TO_SPECIFIED_HOME.toString().replace("%uninvited_player%", uninvitedPlayerName).replace("%home%", homeName).replace("%homeowner%", playerName));
                           }
                       } else {
                           scheduler.runTaskAsynchronously(plugin, () -> plugin.getRDatabase().deleteInviteColumnsUsingHomeownerUUID(result_playerUUID, homeName, uuidFetcher.getOfflinePlayerUUIDFromMojang(uninvitedPlayerName).join()));
                           if (homeName.equalsIgnoreCase("Home")) {
                               sender.sendMessage(Lang.PREFIX + Lang.MANAGE_HOMES_UNINVITED_FROM_DEFAULT_HOME.toString().replace("%uninvited_player%", uninvitedPlayerName).replace("%homeowner%", playerName));
                           } else {
                               sender.sendMessage(Lang.PREFIX + Lang.MANAGE_HOMES_UNINVITED_FROM_SPECIFIED_HOME.toString().replace("%uninvited_player%", uninvitedPlayerName).replace("%home%", homeName).replace("%homeowner%", playerName));
                           }
                       }
                    });

                }));
                return true;

            } else if (args[2].equalsIgnoreCase("privacy")) {

                uuidFetcher.getOfflinePlayerUUIDFromMojang(playerName).thenAccept(result_playerUUID -> {
                    plugin.getRDatabase().getHomeUsingHomeownerUUID(result_playerUUID, homeName).thenAccept(result_home -> {
                       if (result_home.isEmpty()) {
                           sender.sendMessage(Lang.PREFIX + playerHasNoHome);
                           return;
                       }

                       if (args[3].equalsIgnoreCase("private")) {
                           scheduler.runTaskAsynchronously(plugin, () -> plugin.getRDatabase().updatePrivacyStatusUsingHomeownerUUID(result_playerUUID, homeName, false));

                           if (homeName.equalsIgnoreCase("Home")) {
                               sender.sendMessage(Lang.PREFIX + Lang.MANAGE_HOMES_PRIVACY_STATUS_PRIVATE_DEFAULT_HOME.toString().replace("%homeowner%", playerName));
                           } else {
                               sender.sendMessage(Lang.PREFIX + Lang.MANAGE_HOMES_PRIVACY_STATUS_PRIVATE_SPECIFIED_HOME.toString().replace("%home%", homeName).replace("%homeowner%", playerName));
                           }
                       } else if (args[3].equalsIgnoreCase("public")) {
                           scheduler.runTaskAsynchronously(plugin, () -> plugin.getRDatabase().updatePrivacyStatusUsingHomeownerUUID(result_playerUUID, homeName, true));

                           if (homeName.equalsIgnoreCase("Home")) {
                               sender.sendMessage(Lang.PREFIX + Lang.MANAGE_HOMES_PRIVACY_STATUS_PUBLIC_DEFAULT_HOME.toString().replace("%homeowner%", playerName));
                           } else {
                               sender.sendMessage(Lang.PREFIX + Lang.MANAGE_HOMES_PRIVACY_STATUS_PUBLIC_SPECIFIED_HOME.toString().replace("%home%", homeName).replace("%homeowner%", playerName));
                           }
                       } else {
                           sender.sendMessage(Lang.PREFIX.toString() + Lang.INVALID_ARGUMENTS);
                       }

                    });
                });
                return true;
            } else if (args[2].equalsIgnoreCase("rename")) {
                uuidFetcher.getOfflinePlayerUUIDFromMojang(playerName).thenAccept(result_playerUUID -> {
                   plugin.getRDatabase().getHomeUsingHomeownerUUID(result_playerUUID, homeName).thenAccept(result_home -> {
                       if (result_home.isEmpty()) {
                           sender.sendMessage(Lang.PREFIX + playerHasNoHome);
                           return;
                       }
                       String newHomeName = args[3];

                       if (homeName.equalsIgnoreCase(newHomeName)) {
                           sender.sendMessage(Lang.PREFIX + Lang.MANAGE_HOMES_SAME_NAME.toString().replace("%home%", newHomeName));
                           return;
                       }

                       if (!newHomeName.matches("[a-zA-Z0-9]*")) {
                           sender.sendMessage(Lang.PREFIX.toString() + Lang.INVALID_CHARACTERS);
                           return;
                       }

                       scheduler.runTaskAsynchronously(plugin, () -> {
                           plugin.getRDatabase().updateHomeName(result_playerUUID, homeName, newHomeName);
                           plugin.getRDatabase().updateInviteColumnsNewHomeName(result_playerUUID, homeName, newHomeName);
                           sender.sendMessage(Lang.PREFIX + Lang.MANAGE_HOMES_RENAME_HOME.toString().replace("%homeowner%", playerName).replace("%previous_home_name%", homeName).replace("%new_home_name%", newHomeName));
                       });
                   });
                });
            } else {
                sender.sendMessage(Lang.PREFIX.toString() + Lang.INVALID_ARGUMENTS);
            }
        }

        if (args.length == 3) {
            if (args[2].equalsIgnoreCase("set")) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(Lang.PREFIX.toString() + Lang.PLAYER_ONLY);
                    return true;
                }

                Player adminPlayer = (Player) sender;

                uuidFetcher.checkPlayedBefore(playerName).thenAccept(result_playedBefore -> {

                    if (!result_playedBefore) {
                        sender.sendMessage(Lang.PREFIX + Lang.PLAYER_NEVER_LOGGED_ON.toString().replace("%player%", playerName));
                        return;
                    }

                    uuidFetcher.getOfflinePlayerUUIDFromMojang(playerName).thenAccept(result_playerUUID -> plugin.getRDatabase().getHomeListUsingHomeownerUUIDAsynchronously(result_playerUUID).thenAccept(result_homeList -> {

                        int maxHomes = plugin.getConfig().getInt("maximumhomes");

                        if (result_homeList.stream().noneMatch(homeName::equalsIgnoreCase)) {

                            if (!sender.hasPermission("myhomes.maxhomebypass") && result_homeList.size() >= maxHomes) {
                                String exceededHomes = Lang.PREFIX + Lang.TOO_MANY_HOMES.toString().replace("%maximum_number_of_homes%", Integer.toString(maxHomes));
                                adminPlayer.sendMessage(exceededHomes);
                                return;
                            }

                            if (homeName.equalsIgnoreCase("Home")) {
                                adminPlayer.sendMessage(Lang.PREFIX + Lang.MANAGE_HOMES_SET_PLAYER_DEFAULT_HOME.toString().replace("%player%", playerName));
                                scheduler.runTaskAsynchronously(plugin, () -> plugin.getRDatabase().setHomeColumnsUsingHomeownerUUID(result_playerUUID, adminPlayer, "Home", false));
                            } else {
                                adminPlayer.sendMessage(Lang.PREFIX + Lang.MANAGE_HOMES_SET_PLAYER_SPECIFIED_HOME.toString().replace("%home%", homeName).replace("%player%", playerName));
                                scheduler.runTaskAsynchronously(plugin, () -> plugin.getRDatabase().setHomeColumnsUsingHomeownerUUID(result_playerUUID, adminPlayer, homeName, false));
                            }
                        } else {
                            if (homeName.equalsIgnoreCase("Home")) {
                                adminPlayer.sendMessage(Lang.PREFIX + Lang.MANAGE_HOMES_UPDATED_LOCATION_DEFAULT_HOME.toString().replace("%homeowner%", playerName));
                                scheduler.runTaskAsynchronously(plugin, () -> plugin.getRDatabase().updateHomeLocationUsingHomeownerUUID(result_playerUUID, adminPlayer, "Home"));
                            } else {
                                adminPlayer.sendMessage(Lang.PREFIX + Lang.MANAGE_HOMES_UPDATED_LOCATION_SPECIFIED_HOME.toString().replace("%home%", homeName).replace("%homeowner%", playerName));
                                scheduler.runTaskAsynchronously(plugin, () -> plugin.getRDatabase().updateHomeLocationUsingHomeownerUUID(result_playerUUID, adminPlayer, homeName));
                            }
                        }

                    }));

                });
                return true;
            } else if (args[2].equalsIgnoreCase("delete")) {

                uuidFetcher.getOfflinePlayerUUIDFromMojang(playerName).thenAccept(result_playerUUID -> plugin.getRDatabase().getHomeListUsingHomeownerUUIDAsynchronously(result_playerUUID).thenAccept(result_homeList -> {

                    if (result_homeList.stream().noneMatch(homeName::equalsIgnoreCase)) {
                        sender.sendMessage(Lang.PREFIX + playerHasNoHome);
                        return;
                    }

                    if (homeName.equalsIgnoreCase("home")) {
                        sender.sendMessage(Lang.PREFIX + Lang.MANAGE_HOMES_DELETE_DEFAULT_HOME.toString().replace("%player%", playerName));
                    } else {
                        sender.sendMessage(Lang.PREFIX + Lang.MANAGE_HOMES_DELETE_SPECIFIED_HOME.toString().replace("%home%", homeName).replace("%homeowner%", playerName));
                    }

                    scheduler.runTaskAsynchronously(plugin, () -> {
                        plugin.getRDatabase().deleteHomeUsingHomeownerUUID(result_playerUUID, homeName);
                        plugin.getRDatabase().deleteAllInviteColumnsUsingHomeownerUUID(result_playerUUID, homeName);
                    });

                }));

            } else if (args[2].equalsIgnoreCase("listinvites")) {

                uuidFetcher.getOfflinePlayerUUIDFromMojang(playerName).thenAccept(result_playerUUID -> {
                   plugin.getRDatabase().getHomeListUsingHomeownerUUIDAsynchronously(result_playerUUID).thenAccept(result_homeList -> {

                       if (result_homeList.stream().noneMatch(homeName::equalsIgnoreCase)) {
                           sender.sendMessage(Lang.PREFIX + playerHasNoHome);
                           return;
                       }

                       plugin.getRDatabase().getHomeInvitedPlayersAsync(result_playerUUID, homeName).thenAccept(result_homeInvitedPlayersList -> {
                           String invitedPlayersList = result_homeInvitedPlayersList.toString().substring(1, result_homeInvitedPlayersList.toString().length()-1);

                           if (invitedPlayersList.isEmpty()) {
                               sender.sendMessage(Lang.PREFIX.toString() + Lang.NO_INVITES_TO_HOME);
                               return;
                           }

                           List<String> messageList = plugin.getLang().getStringList("listinvitestohome");

                           for (String message : messageList) {
                               sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message.replace("%home%", homeName).replace("%invites_list%", invitedPlayersList)));
                           }

                       });

                   });
                });
                return true;
            } else if (args[2].equalsIgnoreCase("info")) {

                uuidFetcher.getOfflinePlayerUUIDFromMojang(playerName).thenAccept(result_playerUUID -> {
                   plugin.getRDatabase().getHomeListUsingHomeownerUUIDAsynchronously(result_playerUUID).thenAccept(result_homeList -> {
                      if (result_homeList.stream().noneMatch(homeName::equalsIgnoreCase)) {
                          sender.sendMessage(Lang.PREFIX + playerHasNoHome);
                          return;
                      }

                      plugin.getRDatabase().getHomeInfoUsingHomeownerUUID(result_playerUUID, homeName).thenAccept(result_homeInfo -> {

                          List<String> messageList = plugin.getLang().getStringList("homeinfo");

                          String name = result_homeInfo.get(0);
                          String world = result_homeInfo.get(1);
                          String x = String.valueOf((int)Double.parseDouble(result_homeInfo.get(2)));
                          String y = String.valueOf((int)Double.parseDouble(result_homeInfo.get(3)));
                          String z = String.valueOf((int)Double.parseDouble(result_homeInfo.get(4)));
                          String privacy = result_homeInfo.get(5);

                          for (String message : messageList) {
                              sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message.replace("%home_name%", name).replace("%home_x%", x).replace("%home_y%", y).replace("%home_z%", z).replace("%home_world%", world).replace("%privacy_status%", privacy)));
                          }

                      });

                   });
                });
                return true;
            } else {
                sender.sendMessage(Lang.PREFIX.toString() + Lang.INVALID_ARGUMENTS);
                return true;
            }
        }

        if (args.length == 2) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(Lang.PREFIX.toString() + Lang.PLAYER_ONLY);
                return true;
            }

            Player player = (Player) sender;

            uuidFetcher.getOfflinePlayerUUIDFromMojang(playerName).thenAccept(result_playerUUID -> {
                plugin.getRDatabase().getHomeUsingHomeownerUUID(result_playerUUID, homeName).thenAccept(result_home -> {

                    if (result_home.isEmpty()) {
                        sender.sendMessage(Lang.PREFIX + playerHasNoHome);
                        return;
                    }

                    scheduler.runTask(plugin, () -> {
                        World world = plugin.getServer().getWorld(result_home.get(0));
                        Location homeLocation = new Location(world, Double.parseDouble(result_home.get(1)), Double.parseDouble(result_home.get(2)), Double.parseDouble(result_home.get(3)), Float.parseFloat(result_home.get(4)), Float.parseFloat(result_home.get(5)));

                        player.teleport(homeLocation);

                        if (homeName.equalsIgnoreCase("home")) {
                            player.sendMessage(Lang.PREFIX + Lang.HOME_OTHER.toString().replace("%player%", playerName));
                        } else {
                            player.sendMessage(Lang.PREFIX + Lang.SPECIFIED_HOME.toString().replace("%home%", homeName));
                        }
                    });

                });
            });
            return true;
        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!sender.hasPermission("myhomes.manageplayerhome")) {
            return null;
        }

        if (!(sender instanceof Player)) {
            return null;
        }

        Player player = (Player) sender;

        List<String> tabCompletions = new ArrayList<>();
        List<String> subCommands = new ArrayList<>(Arrays.asList(SUB_COMMANDS));
        List<String> privacyStatusOptions = new ArrayList<>(Arrays.asList(PRIVACY_STATUS_OPTIONS));

        if (args.length == 1) {
            List<String> onlinePlayersList = new ArrayList<>();

            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                onlinePlayersList.add(onlinePlayer.getName());
            }

            StringUtil.copyPartialMatches(args[0], onlinePlayersList, tabCompletions);
            Collections.sort(tabCompletions);
            return tabCompletions;
        }

        if (args.length == 2) {
            String playerName = args[0];
            List<String> homeList = plugin.getRDatabase().getHomeListUsingHomeownerUUID(uuidFetcher.getOfflinePlayerUUID(playerName));
            if (!homeList.isEmpty()) {
                StringUtil.copyPartialMatches(args[1], homeList, tabCompletions);
                Collections.sort(tabCompletions);
                return tabCompletions;
            } else {
                return null;
            }
        }

        if (args.length == 3) {
            StringUtil.copyPartialMatches(args[2], subCommands, tabCompletions);
            Collections.sort(tabCompletions);
            return tabCompletions;
        }

        if (args.length == 4) {
            String subCommand = args[2];

            if (subCommand.equalsIgnoreCase("privacy")) {
                StringUtil.copyPartialMatches(args[3], privacyStatusOptions, tabCompletions);
                Collections.sort(tabCompletions);
                return tabCompletions;
            } else if (subCommand.equalsIgnoreCase("invite")) {
                List<String> onlinePlayersList = new ArrayList<>();

                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    if (player.canSee(onlinePlayer)) {
                        onlinePlayersList.add(onlinePlayer.getName());
                    }
                }

                if (onlinePlayersList.isEmpty()) return tabCompletions;

                StringUtil.copyPartialMatches(args[3], onlinePlayersList, tabCompletions);
                Collections.sort(tabCompletions);
                return tabCompletions;

            } else if (subCommand.equalsIgnoreCase("uninvite")) {
                String playerName = args[0];
                String homeName = args[1];

                List<String> invitedPlayersList = plugin.getRDatabase().getHomeInvitedPlayers(uuidFetcher.getOfflinePlayerUUID(playerName), homeName);
                if (!invitedPlayersList.isEmpty()) {
                    StringUtil.copyPartialMatches(args[3], invitedPlayersList, tabCompletions);
                    Collections.sort(tabCompletions);
                    return tabCompletions;
                } else {
                    return null;
                    }
                }

            }

        return tabCompletions;
    }
}