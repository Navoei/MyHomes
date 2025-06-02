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

public class ListPlayerHomes extends CommandAPICommand {

    private final MyHomes plugin;

    public ListPlayerHomes(MyHomes plugin) {
        super("listplayerhomes");
        this.plugin = plugin;
        this.withFullDescription("Allows admins to list a player's homes.");
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
                .thenCompose(result_playerUUID -> plugin.getDatabase().getHomeListUsingHomeownerUUIDAsynchronously(result_playerUUID))
                .thenAccept(result_homeList -> {
                    if (result_homeList.isEmpty()) {
                        player.sendMessage(Lang.PREFIX + Lang.PLAYER_NO_HOMES.toString().replace("%player%", playerName));
                        return;
                    }

                    String homesList = result_homeList.toString().substring(1, result_homeList.toString().length()-1);

                    List<String> messageList = plugin.getLang().getStringList("listplayerhomes");

                    for (String message : messageList) {
                        player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(message.replace("%player%", playerName).replace("%homes_list%", homesList)));
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
                .thenComposeAsync(result_playerUUID -> plugin.getDatabase().getHomeListUsingHomeownerUUIDAsynchronously(result_playerUUID))
                .thenAccept(result_homeList -> {
                    if (result_homeList.isEmpty()) {
                        executor.sendMessage(Lang.PREFIX + Lang.PLAYER_NO_HOMES.toString().replace("%player%", playerName));
                        return;
                    }

                    String homesList = result_homeList.toString().substring(1, result_homeList.toString().length()-1);

                    List<String> messageList = plugin.getLang().getStringList("listplayerhomes");

                    for (String message : messageList) {
                        executor.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(message.replace("%player%", playerName).replace("%homes_list%", homesList)));
                    }
                });
        return 1;
    }

}
