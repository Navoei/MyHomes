package me.navoei.myhomes;

import me.navoei.myhomes.commands.player.HomeInfoCommand;
import me.navoei.myhomes.commands.player.ListHomesCommand;
import me.navoei.myhomes.commands.player.ManageHomeCommand;
import me.navoei.myhomes.commands.player.SetHomeCommand;
import me.navoei.myhomes.storage.Database;
import me.navoei.myhomes.storage.SQLite;
import org.bukkit.plugin.java.JavaPlugin;

public final class MyHomes extends JavaPlugin {

    private Database database;
    static MyHomes instance;

    @Override
    public void onEnable() {
        // Plugin startup logic

        MyHomes.instance = this;

        this.saveDefaultConfig();

        this.database = new SQLite(this);
        this.database.load();

        getCommand("sethome").setExecutor(new SetHomeCommand());
        getCommand("listhomes").setExecutor(new ListHomesCommand());
        getCommand("homeinfo").setExecutor(new HomeInfoCommand());
        getCommand("managehome").setExecutor(new ManageHomeCommand());

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

    }

    public Database getRDatabase() {
        return this.database;
    }

    public static MyHomes getInstance() {
        return instance;
    }
}


/*
Player Commands:
/sethome
/sethome <homename>

/home (takes you to the default home "home")
/home <player> (takes you to the player's default home "home")
/home <player> <homename>

/managehome <homename> <invite/uninvite> <player>
/managehome <homename> <public/private>
/managehome <homename> invitelist shows players that are invited to the home

/listhomes Lists all of the player's homes.
/homeinvites Lists homes the player is invited to.

/delhome
/delhome <homename>

/homeinfo <homename> Lists the home's information.

Admin Commands: (Some commands could be put without spaces to reduce conflicts.)
/manageplayerhomes <player> <homename> <invite/uninvite> <player>
/manageplayerhomes <player> <homename> <public/private>
/manageplayerhomes <player> <homename> delete
/manageplayerhomes <player> <homename> invitelist
/manageplayerhomes <player> <homename> info Shows information such as who is invited, public/private status, and location.
/setplayerhome <player> <homename>
/listhomes <player>

/showinvites <player> Shows the homes the player is invited to.

/sendtohome <player> <homeowner> <home>
 */