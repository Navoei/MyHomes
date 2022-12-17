package me.navoei.myhomes.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import me.navoei.myhomes.uuid.Fetcher;
import org.bukkit.entity.Player;

import me.navoei.myhomes.MyHomes; // Import main class!


public abstract class Database {
    MyHomes plugin;
    Connection connection;
    Fetcher uuidFetcher = new Fetcher();
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
            ps.setFloat(7, player.getLocation().getYaw());
            ps.setFloat(8, player.getLocation().getPitch());
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

    public void updateHomeLocation(Player player, String homeName) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("UPDATE " + homesTable + " SET world = ?, x = ?, y = ?, z = ?, yaw = ?, pitch = ? WHERE player_uuid = '"+player.getUniqueId()+"' AND home_name LIKE '"+homeName+"';");
            ps.setString(1, player.getWorld().getName());
            ps.setDouble(2, player.getLocation().getX());
            ps.setDouble(3, player.getLocation().getY());
            ps.setDouble(4, player.getLocation().getZ());
            ps.setFloat(5, player.getLocation().getYaw());
            ps.setFloat(6, player.getLocation().getPitch());
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

    public void updatePrivacyStatus(Player player, String homeName, Boolean privacy_status) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("UPDATE " + homesTable + " SET privacy_status = ? WHERE player_uuid = '"+player.getUniqueId()+"' AND home_name LIKE '"+homeName+"';");
            ps.setBoolean(1, privacy_status);
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

    public void deleteHome(Player player, String homeName) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("DELETE FROM " + homesTable + " WHERE player_uuid = '"+player.getUniqueId()+"' AND home_name LIKE '"+homeName+"';");
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

    public List<String> getHomeInfo(Player player, String homeName) {

        List<String> homeInfo = new ArrayList<>();

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT * FROM " + homesTable + " WHERE player_uuid = '"+player.getUniqueId()+"' AND home_name LIKE '"+homeName+"';");

            rs = ps.executeQuery();
            while(rs.next()){
                    homeInfo.add(rs.getString("home_name"));
                    homeInfo.add(rs.getString("world"));
                    homeInfo.add(rs.getString("x"));
                    homeInfo.add(rs.getString("y"));
                    homeInfo.add(rs.getString("z"));
                    if (rs.getInt("privacy_status") == 0) {
                        homeInfo.add("Private");
                    } else {
                        homeInfo.add("Public");
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

    public List<String> getHome(Player player, String homeName) {

        List<String> home = new ArrayList<>();

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT * FROM " + homesTable + " WHERE player_uuid = '"+player.getUniqueId()+"' AND home_name LIKE '"+homeName+"';");

            rs = ps.executeQuery();
            while(rs.next()){
                home.add(rs.getString("world"));
                home.add(rs.getString("x"));
                home.add(rs.getString("y"));
                home.add(rs.getString("z"));
                home.add(rs.getString("yaw"));
                home.add(rs.getString("pitch"));
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
        return home;
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

    public void setInviteColumns(Player player, String homeName, String invitedPlayerUUID) {

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

    public void deleteInviteColumns(Player player, String homeName, String invitedPlayerUUID) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("DELETE FROM " + invitesTable + " WHERE invited_player_uuid = '"+invitedPlayerUUID+"' AND homeowner_uuid = '"+player.getUniqueId()+"' AND home_name LIKE '"+homeName+"';");
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

    public void deleteAllInviteColumns(Player player, String homeName) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("DELETE FROM " + invitesTable + " WHERE homeowner_uuid = '"+player.getUniqueId()+"' AND home_name LIKE '"+homeName+"';");
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

    public List<String> getHomeInvitedPlayers(String homeownerUUID, String homeName) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs;

        List<String> invitedPlayers = new ArrayList<>();

        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT * FROM " + invitesTable + " WHERE homeowner_uuid = '"+homeownerUUID+"' AND home_name LIKE '"+homeName+"';");

            rs = ps.executeQuery();
            while(rs.next()){
                invitedPlayers.add(uuidFetcher.getPlayerNameFromUUID(rs.getString("invited_player_uuid")));
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
        return invitedPlayers;
    }

    public List<String> listHomeownersOfInvitedHomes(Player player) {

        List<String> invitedHomeowners = new ArrayList<>();

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT * FROM " + invitesTable + " WHERE invited_player_uuid = '"+player.getUniqueId()+"';");

            rs = ps.executeQuery();
            while(rs.next()){
                invitedHomeowners.add(uuidFetcher.getPlayerNameFromUUID(rs.getString("homeowner_uuid")));
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
        return invitedHomeowners;

    }

    public List<String> getHomeUsingHomeOwnerUUID(String homeowner_uuid, String homeName) {

        List<String> home = new ArrayList<>();

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT * FROM " + homesTable + " WHERE player_uuid = '"+homeowner_uuid+"' AND home_name LIKE '"+homeName+"';");

            rs = ps.executeQuery();
            while(rs.next()){
                home.add(rs.getString("world"));
                home.add(rs.getString("x"));
                home.add(rs.getString("y"));
                home.add(rs.getString("z"));
                home.add(rs.getString("yaw"));
                home.add(rs.getString("pitch"));
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
        return home;
        //Check all instances where the invited_player_uuid, homeowner_uuid, and the home_name is the same.
        //If the homeowner_uuid, invited player, and home name match, then teleport.
        //However, if they do not match, check for the publicity status.
        //If it is true, teleport, otherwise do not teleport.
    }

    public List<String> getInvitedHomesList(String homeowner_uuid, String invitedPlayer_uuid) {

        List<String> invitedHomesList = new ArrayList<>();

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT * FROM " + invitesTable + " WHERE invited_player_uuid = '"+invitedPlayer_uuid+"' AND homeowner_uuid = '"+homeowner_uuid+"';");

            rs = ps.executeQuery();
            while(rs.next()){
                invitedHomesList.add(rs.getString("home_name"));
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
        return invitedHomesList;

    }

    public List<String> getHomePrivacyStatus(String homeowner_uuid, String homeName) {

        ArrayList<String> privacy_status = new ArrayList<>();

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT * FROM " + homesTable + " WHERE player_uuid = '"+homeowner_uuid+"' AND home_name LIKE '"+homeName+"';");

            rs = ps.executeQuery();
            while(rs.next()) {
                privacy_status.add(rs.getString("privacy_status"));
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
        return privacy_status;
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
