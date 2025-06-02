package me.navoei.myhomes.commands.player;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import me.navoei.myhomes.MyHomes;
import me.navoei.myhomes.language.Lang;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

public class DeleteHome extends CommandAPICommand {

    private final MyHomes plugin;

    public DeleteHome(MyHomes plugin) {
        super("deletehome");
        this.plugin = plugin;
        this.withFullDescription("Deletes the specified home.");
        this.withPermission("myhomes.deletehome");
        this.withAliases("delhome");

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

        if (homeName!=null) {
            plugin.getDatabase().getHomeList(player).thenAccept(result_homeList -> {

                if (result_homeList.stream().noneMatch(homeName::equalsIgnoreCase) || !homeName.matches("[a-zA-Z0-9]*")) {
                    player.sendMessage(Lang.PREFIX.toString() + Lang.HOME_NOT_EXISTS);
                    return;
                }

                plugin.getDatabase().getHomeInfo(player, homeName).thenAccept(result_homeInfo -> {
                    String result_homeName = result_homeInfo.getFirst();

                    if (result_homeName.isEmpty()) {
                        player.sendMessage(Lang.PREFIX.toString() + Lang.HOME_NOT_EXISTS);
                        return;
                    }

                    plugin.getDatabase().deleteHome(player, result_homeName);
                    plugin.getDatabase().deleteAllInviteColumns(player, result_homeName);

                    if (result_homeName.equalsIgnoreCase("home")) {
                        player.sendMessage(Lang.PREFIX.toString() + Lang.HOME_DELETED);
                    } else {
                        player.sendMessage(Lang.PREFIX + Lang.HOME_DELETED_SPECIFIED.toString().replace("%home%", result_homeName));
                    }

                });

            });
        } else {
            plugin.getDatabase().getHomeList(player).thenAccept(result_homeList -> {
                if (result_homeList.stream().noneMatch("Home"::equalsIgnoreCase)) {
                    player.sendMessage(Lang.PREFIX.toString() + Lang.HOME_NOT_EXISTS);
                    return;
                }

                plugin.getDatabase().getHomeInfo(player, "Home").thenAccept(result_homeInfo -> {
                    String result_homeName = result_homeInfo.getFirst();

                    if (result_homeName.isEmpty()) {
                        player.sendMessage(Lang.PREFIX.toString() + Lang.HOME_NOT_EXISTS);
                        return;
                    }

                    plugin.getDatabase().deleteHome(player, result_homeName);
                    plugin.getDatabase().deleteAllInviteColumns(player, result_homeName);

                    player.sendMessage(Lang.PREFIX.toString() + Lang.HOME_DELETED);

                });

            });
        }
        return 1;
    }

    private int onCommandConsole(ConsoleCommandSender executor, CommandArguments arguments) {
        executor.sendMessage(Lang.PREFIX + Lang.PLAYER_ONLY.toString());
        return 1;
    }

}
