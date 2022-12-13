package me.navoei.myhomes.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.entity.Player;

import me.navoei.myhomes.MyHomes; // Import main class!


public abstract class Database {
    MyHomes plugin;
    Connection connection;
    // The name of the table we created back in SQLite class.
    public String homesTable = "homes";
    public String invitesTable = "invites";
    public int tokens = 0;
    public Database(MyHomes instance){
        plugin = instance;
    }

    public abstract Connection getSQLConnection();

    public abstract void load();

    public void initialize(){
        connection = getSQLConnection();
        try{
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM " + homesTable + " WHERE player_uuid = ?");
            ResultSet rs = ps.executeQuery();
            close(ps,rs);

        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, "Unable to retreive connection", ex);
        }
    }

    // These are the methods you can use to get things out of your database. You of course can make new ones to return different things in the database.
    // This returns the number of people the player killed.
    public Integer getColumns(String string) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT * FROM " + homesTable + " WHERE player = '"+string+"';");
            rs = ps.executeQuery();
            while(rs.next()){
                if(rs.getString("player").equalsIgnoreCase(string.toLowerCase())){ // Tell database to search for the player you sent into the method. e.g getTokens(sam) It will look for sam.
                    return rs.getInt("kills"); // Return the players ammount of kills. If you wanted to get total (just a random number for an example for you guys) You would change this to total!
                }
            }
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return 0;
    }
    // Exact same method here, Except as mentioned above i am looking for total!
    public List<String> getHome(Player player, String homeName) {

        List<String> homeInfo = new ArrayList<>();

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT * FROM " + homesTable + " WHERE player_uuid = '"+player.getUniqueId()+"' AND home_name = '"+homeName+"';");

            rs = ps.executeQuery();
            while(rs.next()){
                    homeInfo.add(rs.getString("home_name"));
                    homeInfo.add(rs.getString("world"));
                    homeInfo.add(rs.getString("x"));
                    homeInfo.add(rs.getString("y"));
                    homeInfo.add(rs.getString("z"));
                    if (rs.getInt("privacy_status") == 0) {
                        homeInfo.add("False");
                    } else {
                        homeInfo.add("True");
                    }
            }
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return homeInfo;
    }

    public List<String> getHomeList(Player player) {

        List<String> homeList = new ArrayList<>();

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT * FROM " + homesTable + " WHERE player_uuid = '"+player.getUniqueId()+"';");

            rs = ps.executeQuery();
            while(rs.next()) {
                homeList.add(rs.getString("home_name"));
            }
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return homeList;
    }

    public Integer getHomeID(Player player, String homeName) {

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs;

        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT * FROM " + homesTable + " WHERE player_uuid = '"+player.getUniqueId()+"' AND home_name = '"+homeName+"';");

            rs = ps.executeQuery();
            return rs.getInt("id");

        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
        return null;
    }

    // Now we need methods to save things to the database
    public void setHomeColumns(Player player, String homeName, Boolean privacy_status) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("REPLACE INTO " + homesTable + " (player_uuid,home_name,world,x,y,z,yaw,pitch,privacy_status) VALUES(?,?,?,?,?,?,?,?,?)"); // IMPORTANT. In SQLite class, We made 3 colums. player, Kills, Total.
            ps.setString(1, player.getUniqueId().toString());
            ps.setString(2, homeName);
            ps.setString(3, player.getWorld().getName());
            ps.setDouble(4, player.getLocation().getX());
            ps.setDouble(5, player.getLocation().getY());
            ps.setDouble(6, player.getLocation().getZ());
            ps.setInt(7, (int) player.getLocation().getYaw());
            ps.setInt(8, (int) player.getLocation().getPitch());
            ps.setBoolean(9, privacy_status);
            // YOU MUST put these into this line!! And depending on how many
            // colums you put (say you made 5) All 5 need to be in the brackets
            // Seperated with comma's (,) AND there needs to be the same amount of
            // question marks in the VALUES brackets. Right now i only have 3 colums
            // So VALUES (?,?,?) If you had 5 colums VALUES(?,?,?,?,?)

            //ps.setInt(2, tokens); // This sets the value in the database. The colums go in order. Player is ID 1, kills is ID 2, Total would be 3 and so on. you can use
            // setInt, setString and so on. tokens and total are just variables sent in, You can manually send values in as well. p.setInt(2, 10) <-
            // This would set the players kills instantly to 10. Sorry about the variable names, It sets their kills to 10 i just have the variable called
            // Tokens from another plugin :/
            //ps.setInt(3, total);
            ps.executeUpdate();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
    }

    public void setInviteColumns(Player player, String homeName, String invitedPlayerUUID) {

        if (getHome(player, homeName).isEmpty()) {
            player.sendMessage("This is not a valid home.");
            return;
        }
        Integer homeID = getHomeID(player, homeName);

        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("REPLACE INTO " + invitesTable + " (invited_player_uuid,homeowner_uuid,home_name) VALUES(?,?,?)");
            ps.setString(1, invitedPlayerUUID);
            ps.setString(2, player.getUniqueId().toString());
            ps.setString(3, homeName);
            ps.executeUpdate();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
            }
        }
    }

    public void close(PreparedStatement ps,ResultSet rs){
        try {
            if (ps != null)
                ps.close();
            if (rs != null)
                rs.close();
        } catch (SQLException ex) {
            ErrorLogger.close(plugin, ex);
        }
    }
}
