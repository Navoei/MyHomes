package me.navoei.myhomes.commands.player;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.executors.CommandArguments;
import me.navoei.myhomes.MyHomes;
import me.navoei.myhomes.language.Lang;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ListInvitesCommand extends CommandAPICommand {

    MyHomes plugin;

    public ListInvitesCommand(MyHomes plugin) {
        super("listinvites");
        this.plugin = plugin;
        this.withFullDescription("Lists homes.");
        this.withPermission("myhomes.listinvites");
        this.withAliases("invitelist", "ilist");
        this.executesPlayer(this::onCommandPlayer);
        this.executesConsole(this::onCommandConsole);
    }

    private int onCommandPlayer(Player player, CommandArguments arguments) {
        plugin.getDatabase().getHomeInviteList(player.getUniqueId().toString()).thenAccept(result_homeInviteList -> {
            if (result_homeInviteList.isEmpty()) {
                player.sendMessage(Lang.PREFIX.toString() + Lang.NO_INVITES);
                return;
            }

            List<String> messageList = plugin.getLang().getStringList("listinvites");

            for (String message : messageList) {
                if (message.contains("%homeowner%") || message.contains("%homes_list%")) {
                    for (Map.Entry<String, ArrayList<String>> entry : result_homeInviteList.entrySet()) {
                        String homeowner = entry.getKey();
                        String homesList = entry.getValue().toString().substring(1, entry.getValue().toString().length()-1);
                        player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(message.replace("%homeowner%", homeowner).replace("%homes_list%", homesList)));
                    }
                } else {
                    player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(message));
                }
            }

        });
        return 1;
    }

    private int onCommandConsole(ConsoleCommandSender executor, CommandArguments arguments) {
        executor.sendMessage(Lang.PREFIX + Lang.PLAYER_ONLY.toString());
        return 1;
    }

}
