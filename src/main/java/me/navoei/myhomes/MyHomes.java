package me.navoei.myhomes;

import me.navoei.myhomes.commands.player.*;
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
        getCommand("deletehome").setExecutor(new DeleteHomeCommand());
        getCommand("home").setExecutor(new HomeCommand());

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
/home <homename>
/home <player> (takes you to the player's default home "home")
/home <player> <homename>

/managehome <homename> <invite/uninvite> <player>
/managehome <homename> <privacy> <public/private>
/managehome <homename> invitelist shows players that are invited to the home

/listhomes Lists all of the player's homes.
/homelist

/listinvites Lists homes the player is invited to.
/invitelist

/delhome
/delhome <homename>

/homeinfo <homename> Lists the home's information.

Admin Commands: (Some commands could be put without spaces to reduce conflicts.)
/manageplayerhomes <player> <homename> <invite/uninvite> <player>
/manageplayerhomes <player> <homename> <privacy> <public/private>
/manageplayerhomes <player> <homename> delete
/manageplayerhomes <player> <homename> invitelist
/manageplayerhomes <player> <homename> info Shows information such as who is invited, public/private status, and location.
/manageplayerhomes <player> <homename> set
/manageplayerhomes <player> <homename> Go to a player's home regardless of invites or privacy status.

/showhomelist <player> Lists the player's homes.

/showhomeinvites <player>

 */