package me.navoei.myhomes;

import org.bukkit.plugin.java.JavaPlugin;

public final class Myhomes extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
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

/homelimit Shows the amount of homes that can be set.

/delhome
/delhome <homename>

/homeinfo <homename> Lists the home's information.

Admin Commands: (Some commands could be put without spaces to reduce conflicts.)
/homelimit <player> Shows how many homes this player can set.
/manageplayerhomes <player> <homename> <invite/uninvite> <player>
/manageplayerhomes <player> <homename> <public/private>
/manageplayerhomes <player> <homename> delete
/manageplayerhomes <player> <homename> invitelist
/manageplayerhomes <player> <homename> info Shows information such as who is invited, public/private status, and location.
/setplayerhome <player> <homename>
/listhomes <player>



/sendtohome <player> <homeowner> <home>
 */