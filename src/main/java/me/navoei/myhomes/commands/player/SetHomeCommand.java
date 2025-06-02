package me.navoei.myhomes.commands.player;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import me.navoei.myhomes.MyHomes;
import me.navoei.myhomes.language.Lang;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SetHomeCommand extends CommandAPICommand {

    MyHomes plugin;

    public SetHomeCommand(MyHomes plugin) {
        super("sethome");
        this.plugin = plugin;
        this.withFullDescription("Sets a player's home.");
        this.withPermission("myhomes.sethome");
        this.withAliases("homeset");

        this.executesPlayer(this::onCommandPlayer);
        this.executesConsole(this::onCommandConsole);

        this.withOptionalArguments(new StringArgument("home_name").replaceSuggestions(ArgumentSuggestions.stringCollectionAsync((sender) -> CompletableFuture.supplyAsync(() -> {
            if (sender.sender() instanceof Player player) {
                return plugin.getDatabase().getHomeList(player).join();
            } else {
                return null;
            }
        }))));
    }

    private int onCommandPlayer(Player player, CommandArguments arguments) {
        String homeName = arguments.getByClass("home_name", String.class);
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
                    String exceededHomes = Lang.PREFIX + Lang.TOO_MANY_HOMES.toString().replace("%maximum_number_of_homes%", Integer.toString(maxHomes));
                    if (homeName!=null) {
                        if (!homeName.matches("[a-zA-Z0-9]*")) {
                            player.sendMessage(Lang.PREFIX.toString() + Lang.INVALID_CHARACTERS);
                            return;
                        }
                        int characterLimit = plugin.getConfig().getInt("characterlimit");
                        if (homeName.length() > characterLimit) {
                            player.sendMessage(Lang.PREFIX + Lang.TOO_MANY_CHARACTERS.toString().replace("%character_limit%", Integer.toString(characterLimit)));
                            return;
                        }
                        if (result_homeList.size() >= maxHomes && result_homeList.stream().noneMatch(homeName::equalsIgnoreCase) && !player.hasPermission("myhomes.maxhomebypass")) {
                            player.sendMessage(exceededHomes);
                            return;
                        }

                        plugin.getDatabase().getHomeInfo(player, homeName).thenAccept(result_homeInfo -> {
                            if (!result_homeInfo.isEmpty()) {
                                String result_homeName = result_homeInfo.getFirst();
                                plugin.getDatabase().updateHomeLocation(player, result_homeName);

                                if (result_homeName.equalsIgnoreCase("Home")) {
                                    player.sendMessage(Lang.PREFIX.toString() + Lang.HOME_UPDATED);
                                } else {
                                    player.sendMessage(Lang.PREFIX + Lang.HOME_SPECIFIED_UPDATED.toString().replace("%home%", result_homeName));
                                }
                                return;
                            }

                            if (homeName.equalsIgnoreCase("Home")) {
                                plugin.getDatabase().setHomeColumns(player, "Home", false);
                                player.sendMessage(Lang.PREFIX.toString() + Lang.SET_HOME);
                            } else {
                                plugin.getDatabase().setHomeColumns(player, homeName, false);
                                player.sendMessage(Lang.PREFIX + Lang.SET_HOME_SPECIFIED.toString().replace("%home%", homeName));
                            }
                        });
                    } else {
                        if (result_homeList.size() >= maxHomes && result_homeList.stream().noneMatch("Home"::equalsIgnoreCase) && !player.hasPermission("myhomes.maxhomebypass")) {
                            player.sendMessage(exceededHomes);
                            return;
                        }

                        plugin.getDatabase().getHomeInfo(player, "Home").thenAccept(result_homeInfo -> {
                            if (!result_homeInfo.isEmpty()) {
                                plugin.getDatabase().updateHomeLocation(player, "Home");
                                player.sendMessage(Lang.PREFIX.toString() + Lang.HOME_UPDATED);
                                return;
                            }

                            plugin.getDatabase().setHomeColumns(player, "Home", false);
                            player.sendMessage(Lang.PREFIX.toString() + Lang.SET_HOME);

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
