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
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ListPlayerInvites extends CommandAPICommand {

    MyHomes plugin;

    public ListPlayerInvites(MyHomes plugin) {
        super("listplayerinvites");
        this.plugin = plugin;
        this.withFullDescription("Allows admins to list homes a player is invited to.");
        this.withPermission("myhomes.manageplayerhome");

        this.executesPlayer(this::onCommandPlayer);
        this.executesConsole(this::onCommandConsole);

        this.withArguments(new StringArgument("player").replaceSuggestions(ArgumentSuggestions.stringCollection((sender) -> {
            List<String> playerNames = new ArrayList<>();
            Bukkit.getOnlinePlayers().forEach(player -> playerNames.add(player.getName()));
            return playerNames;
        })));
    }

    private int onCommandPlayer(Player player, CommandArguments arguments) {
        String playerName = arguments.getByClass("player", String.class);
        if (playerName==null || playerName.isEmpty()) {
            player.sendMessage(Lang.PREFIX + Lang.INVALID_ARGUMENTS.toString());
            return 0;
        }
        Fetcher.getPlayerUUID(playerName)
                .thenComposeAsync(result_playerUUID -> plugin.getDatabase().getHomeInviteList(result_playerUUID))
                .thenAccept(result_homeInviteList -> {
                    if (result_homeInviteList.isEmpty()) {
                        player.sendMessage(Lang.PREFIX + Lang.PLAYER_NO_INVITES.toString().replace("%player%", playerName));
                        return;
                    }

                    List<String> messageList = plugin.getLang().getStringList("listplayerinvites");
                    for (String message : messageList) {
                        if (message.contains("%homeowner%") || message.contains("%homes_list%")) {
                            for (Map.Entry<String, ArrayList<String>> entry : result_homeInviteList.entrySet()) {
                                String homeowner = entry.getKey();
                                String homesList = entry.getValue().toString().substring(1, entry.getValue().toString().length()-1);
                                player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(message.replace("%homeowner%", homeowner).replace("%homes_list%", homesList)));
                            }
                        } else {
                            player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(message.replace("%player%", playerName)));
                        }
                    }
                });
        return 1;
    }

    private int onCommandConsole(ConsoleCommandSender executor, CommandArguments arguments) {
        String playerName = arguments.getByClass("player", String.class);
        if (playerName==null || playerName.isEmpty()) {
            executor.sendMessage(Lang.PREFIX + Lang.INVALID_ARGUMENTS.toString());
            return 0;
        }
        Fetcher.getPlayerUUID(playerName)
                .thenComposeAsync(result_playerUUID -> plugin.getDatabase().getHomeInviteList(result_playerUUID))
                .thenAccept(result_homeInviteList -> {
                    if (result_homeInviteList.isEmpty()) {
                        executor.sendMessage(Lang.PREFIX + Lang.PLAYER_NO_INVITES.toString().replace("%player%", playerName));
                        return;
                    }

                    List<String> messageList = plugin.getLang().getStringList("listplayerinvites");
                    for (String message : messageList) {
                        if (message.contains("%homeowner%") || message.contains("%homes_list%")) {
                            for (Map.Entry<String, ArrayList<String>> entry : result_homeInviteList.entrySet()) {
                                String homeowner = entry.getKey();
                                String homesList = entry.getValue().toString().substring(1, entry.getValue().toString().length()-1);
                                executor.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(message.replace("%homeowner%", homeowner).replace("%homes_list%", homesList)));
                            }
                        } else {
                            executor.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(message.replace("%player%", playerName)));
                        }
                    }
                });
        return 1;
    }

}
