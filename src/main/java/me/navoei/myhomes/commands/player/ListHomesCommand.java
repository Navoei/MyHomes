package me.navoei.myhomes.commands.player;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.executors.CommandArguments;
import me.navoei.myhomes.MyHomes;
import me.navoei.myhomes.language.Lang;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class ListHomesCommand extends CommandAPICommand {

    MyHomes plugin;

    public ListHomesCommand(MyHomes plugin) {
        super("listhomes");
        this.plugin = plugin;
        this.withFullDescription("Lists homes.");
        this.withPermission("myhomes.listhomes");
        this.withAliases("homelist");
        this.executesPlayer(this::onCommandPlayer);
        this.executesConsole(this::onCommandConsole);
    }

    private int onCommandPlayer(Player player, CommandArguments arguments) {
        plugin.getDatabase().getHomeList(player).thenAccept(result_homeList -> {
            if (result_homeList.isEmpty()) {
                player.sendMessage(Lang.PREFIX.toString() + Lang.NO_HOMES);
                return;
            }

            String homesList = result_homeList.toString().substring(1, result_homeList.toString().length()-1);
            List<String> messageList = plugin.getLang().getStringList("listhomes");

            for (String message : messageList) {
                player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(message.replace("%homes_list%", homesList)));
            }

        });
        return 1;
    }

    private int onCommandConsole(ConsoleCommandSender executor, CommandArguments arguments) {
        executor.sendMessage(Lang.PREFIX + Lang.PLAYER_ONLY.toString());
        return 1;
    }

}
