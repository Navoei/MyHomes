package me.navoei.myhomes;

import me.navoei.myhomes.commands.admin.*;
import me.navoei.myhomes.commands.player.*;
import me.navoei.myhomes.language.Lang;
import me.navoei.myhomes.storage.Database;
import me.navoei.myhomes.storage.SQLite;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class MyHomes extends JavaPlugin {

    private Database database;
    static MyHomes instance;
    private Logger log;
    public static YamlConfiguration LANG;
    public static File LANG_FILE;

    @Override
    public void onEnable() {
            // Plugin startup logic

            MyHomes.instance = this;
            saveDefaultConfig();

            log = getLogger();
            loadLang();

            this.database = new SQLite(this);
            this.database.load();

            getCommand("sethome").setExecutor(new SetHomeCommand());
            getCommand("listhomes").setExecutor(new ListHomesCommand());
            getCommand("managehome").setExecutor(new ManageHomeCommand());
            getCommand("deletehome").setExecutor(new DeleteHomeCommand());
            getCommand("home").setExecutor(new HomeCommand());
            getCommand("listinvites").setExecutor(new ListInvitesCommand());
            getCommand("importolddatabase").setExecutor(new ImportMyHomeCommand());
            getCommand("listplayerhomes").setExecutor(new ListPlayerHomesCommand());
            getCommand("listplayerinvites").setExecutor(new ListPlayerInvitesCommand());
            getCommand("manageplayerhome").setExecutor(new ManagePlayerHomeCommand());
            getServer().getPluginManager().registerEvents(new ManagePlayerHomeCommand(), this);

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

    /**
     * Load the lang.yml file.
     * @return The lang.yml config.
     */
    public void loadLang() {
        File lang = new File(getDataFolder(), "lang.yml");
        if (!lang.exists()) {
            try {
                getDataFolder().mkdir();
                lang.createNewFile();
                InputStream defConfigStream = this.getResource("lang.yml");
                if (defConfigStream != null) {
                    copyInputStreamToFile(defConfigStream, lang);
                    YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(lang);
                    defConfig.save(lang);
                    Lang.setFile(defConfig);
                }
            } catch(IOException e) {
                e.printStackTrace(); // So they notice
                log.severe("Failed to create lang.yml for MyHomes.");
                log.severe("Now disabling...");
                this.setEnabled(false); // Without it loaded, we can't send them messages
            }
        }
        YamlConfiguration conf = YamlConfiguration.loadConfiguration(lang);
        for(Lang item:Lang.values()) {
            if (conf.getString(item.getPath()) == null) {
                conf.set(item.getPath(), item.getDefault());
            }
        }
        Lang.setFile(conf);
        LANG = conf;
        LANG_FILE = lang;
        try {
            conf.save(getLangFile());
        } catch(IOException e) {
            log.log(Level.WARNING, "Failed to save lang.yml for MyHomes");
            log.log(Level.WARNING, "Now disabling...");
            e.printStackTrace();
        }
    }

    /**
     * Gets the lang.yml config.
     * @return The lang.yml config.
     */
    public YamlConfiguration getLang() {
        return LANG;
    }

    /**
     * Get the lang.yml file.
     * @return The lang.yml file.
     */
    public File getLangFile() {
        return LANG_FILE;
    }

    public static void copyInputStreamToFile(InputStream input, File file) {

        try (OutputStream output = new FileOutputStream(file)) {
            input.transferTo(output);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

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
/managehome <homename> listinvites shows players that are invited to the home

/listhomes Lists all of the player's homes.
/homelist

/listinvites Lists homes the player is invited to.
/invitelist

/delhome
/delhome <homename>

/homeinfo <homename> Lists the home's information.

Admin Commands: (Some commands could be put without spaces to reduce conflicts.)
/manageplayerhome <player> <homename> <invite/uninvite> <player>
/manageplayerhome <player> <homename> <privacy> <public/private>
/manageplayerhome <player> <homename> set
/manageplayerhome <player> <homename> delete
/manageplayerhome <player> <homename> invitelist
/manageplayerhome <player> <homename> info Shows information such as who is invited, public/private status, and location.
/manageplayerhome <player> <homename> Teleport to a player's home regardless of invites or privacy status.

/showhomelist <player> Lists the player's homes.

/showhomeinvites <player>

 */