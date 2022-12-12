package me.navoei.myhomes.storage;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

import me.navoei.myhomes.MyHomes; // import your main class


public class SQLite extends Database {
    String dbname;
    public SQLite(MyHomes instance){
        super(instance);
        dbname = plugin.getConfig().getString("SQLite.myHomesDatabase", "homes"); // Set the table name here e.g player_kills
    }
    // make sure to put your table name in here too.
    // This creates the different colums you will save data too. varchar(32) Is a string, int = integer
    public String SQLiteCreateHomesTokensTable = "CREATE TABLE IF NOT EXISTS `homes` (`id` INTEGER PRIMARY KEY, `player_uuid` varchar(32) NOT NULL, `home_name` varchar(64) NOT NULL DEFAULT 'home', `world` varchar(64) NOT NULL DEFAULT '0', `x` DOUBLE NOT NULL DEFAULT '0', `y` DOUBLE NOT NULL DEFAULT '0', `z` DOUBLE NOT NULL DEFAULT '0', `yaw` smallint NOT NULL DEFAULT '0', `pitch` smallint NOT NULL DEFAULT '0', `privacy_status` TINYINT NOT NULL DEFAULT `0`, UNIQUE (`player_uuid`,`home_name`));";
    // This is creating 3 colums Player, Kills, Total. Primary key is what you are going to use as your indexer. Here we want to use player so
    // we can search by player, and get kills and total. If you some how were searching kills it would provide total and player.
    public String SQLiteCreateInviteTokensTable = "CREATE TABLE IF NOT EXISTS `invites` (`id` INTEGER PRIMARY KEY, `invited_player_uuid` varchar(32) NOT NULL, `homeowner_uuid` varchar(32) NOT NULL, `home_name` varchar(64) NOT NULL DEFAULT 'home', UNIQUE (`invited_player_uuid`,`homeowner_uuid`, `home_name`));";

    // SQL creation stuff, You can leave the blow stuff untouched.
    public Connection getSQLConnection() {
        File dataFolder = new File(plugin.getDataFolder(), dbname+".db");
        if (!dataFolder.exists()){
            try {
                dataFolder.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "File write error: "+dbname+".db");
            }
        }
        try {
            if(connection!=null&&!connection.isClosed()){
                return connection;
            }
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dataFolder);
            return connection;
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE,"SQLite exception on initialize", ex);
        } catch (ClassNotFoundException ex) {
            plugin.getLogger().log(Level.SEVERE, "You need the SQLite JBDC library. Google it. Put it in /lib folder.");
        }
        return null;
    }

    public void load() {
        connection = getSQLConnection();
        try {
            Statement s = connection.createStatement();
            s.executeUpdate(SQLiteCreateHomesTokensTable);
            s.executeUpdate(SQLiteCreateInviteTokensTable);
            s.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        initialize();
    }
}
