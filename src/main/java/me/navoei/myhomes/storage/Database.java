package me.navoei.myhomes.storage;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
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

    public void setHomeColumnsUsingHomeownerUUID(String homeownerUUID, Player adminPlayer, String homeName, Boolean privacy_status) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("REPLACE INTO " + homesTable + " (player_uuid,home_name,world,x,y,z,yaw,pitch,privacy_status) VALUES(?,?,?,?,?,?,?,?,?)"); // IMPORTANT. In SQLite class, We made 3 colums. player, Kills, Total.
            ps.setString(1, homeownerUUID);
            ps.setString(2, homeName);
            ps.setString(3, adminPlayer.getWorld().getName());
            ps.setDouble(4, adminPlayer.getLocation().getX());
            ps.setDouble(5, adminPlayer.getLocation().getY());
            ps.setDouble(6, adminPlayer.getLocation().getZ());
            ps.setFloat(7, adminPlayer.getLocation().getYaw());
            ps.setFloat(8, adminPlayer.getLocation().getPitch());
            ps.setBoolean(9, privacy_status);
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

    public void updateHomeLocationUsingHomeownerUUID(String homeownerUUID, Player adminPlayer, String homeName) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("UPDATE " + homesTable + " SET world = ?, x = ?, y = ?, z = ?, yaw = ?, pitch = ? WHERE player_uuid = '"+homeownerUUID+"' AND home_name LIKE '"+homeName+"';");
            ps.setString(1, adminPlayer.getWorld().getName());
            ps.setDouble(2, adminPlayer.getLocation().getX());
            ps.setDouble(3, adminPlayer.getLocation().getY());
            ps.setDouble(4, adminPlayer.getLocation().getZ());
            ps.setFloat(5, adminPlayer.getLocation().getYaw());
            ps.setFloat(6, adminPlayer.getLocation().getPitch());
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

    public CompletableFuture<List<String>> getHomeInfo(Player player, String homeName) {
        return CompletableFuture.supplyAsync(() -> {
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
        });
    }

    public CompletableFuture<List<String>> getHome(Player player, String homeName) {
        return CompletableFuture.supplyAsync(() -> {
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
        });
    }

    public CompletableFuture<List<String>> getHomeList(Player player) {
        return CompletableFuture.supplyAsync(() -> {
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
        });
    }

    public CompletableFuture<List<String>> getHomeListUsingHomeownerUUID(String playerUUID) {
        return CompletableFuture.supplyAsync(() -> {
            List<String> homeList = new ArrayList<>();

            Connection conn = null;
            PreparedStatement ps = null;
            ResultSet rs;
            try {
                conn = getSQLConnection();
                ps = conn.prepareStatement("SELECT * FROM " + homesTable + " WHERE player_uuid = '"+playerUUID+"';");

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
        });
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

    public CompletableFuture<List<String>> getHomeInvitedPlayers(String homeownerUUID, String homeName) {
        return CompletableFuture.supplyAsync(() -> {
            Connection conn = null;
            PreparedStatement ps = null;
            ResultSet rs;

            List<String> invitedPlayers = new ArrayList<>();

            try {
                conn = getSQLConnection();
                ps = conn.prepareStatement("SELECT * FROM " + invitesTable + " WHERE homeowner_uuid = '"+homeownerUUID+"' AND home_name LIKE '"+homeName+"';");

                rs = ps.executeQuery();
                while(rs.next()){
                    invitedPlayers.add(uuidFetcher.getPlayerNameFromUUID(rs.getString("invited_player_uuid")).join());
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
        });
    }

    public CompletableFuture<List<String>> listHomeownersOfInvitedHomes(Player player) {
        return CompletableFuture.supplyAsync(() -> {
            List<String> invitedHomeowners = new ArrayList<>();

            Connection conn = null;
            PreparedStatement ps = null;
            ResultSet rs;
            try {
                conn = getSQLConnection();
                ps = conn.prepareStatement("SELECT * FROM " + invitesTable + " WHERE invited_player_uuid = '"+player.getUniqueId()+"';");

                rs = ps.executeQuery();
                while(rs.next()){
                    invitedHomeowners.add(uuidFetcher.getPlayerNameFromUUID(rs.getString("homeowner_uuid")).join());
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
        });
    }

    public CompletableFuture<HashMap<String, String>> getHomeInviteList(String invitedPlayer_uuid) {
        return CompletableFuture.supplyAsync(() -> {
            HashMap<String, String> invitedHomesHashMap = new HashMap<>();

            Connection conn = null;
            PreparedStatement ps = null;
            ResultSet rs;
            try {
                conn = getSQLConnection();
                ps = conn.prepareStatement("SELECT * FROM " + invitesTable + " WHERE invited_player_uuid = '"+invitedPlayer_uuid+"';");

                rs = ps.executeQuery();
                while(rs.next()){
                    invitedHomesHashMap.put(rs.getString("home_name"), uuidFetcher.getPlayerNameFromUUID(rs.getString("homeowner_uuid")).join());
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
            return invitedHomesHashMap;
        });
    }

    public CompletableFuture<List<String>> getHomeUsingHomeownerUUID(String homeowner_uuid, String homeName) {
        return CompletableFuture.supplyAsync(() -> {
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
        });
        //Check all instances where the invited_player_uuid, homeowner_uuid, and the home_name is the same.
        //If the homeowner_uuid, invited player, and home name match, then teleport.
        //However, if they do not match, check for the publicity status.
        //If it is true, teleport, otherwise do not teleport.
    }

    public CompletableFuture<List<String>> getInvitedHomesThatAreOwnedByHomeowner(String homeowner_uuid, String invitedPlayer_uuid) {
        return CompletableFuture.supplyAsync(() -> {
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
        });
    }

    public CompletableFuture<List<String>> getHomePrivacyStatus(String homeowner_uuid, String homeName) {
        return CompletableFuture.supplyAsync(() -> {
            ArrayList<String> privacy_status = new ArrayList<>();

            Connection conn = null;
            PreparedStatement ps = null;
            ResultSet rs;
            try {
                conn = getSQLConnection();
                ps = conn.prepareStatement("SELECT * FROM " + homesTable + " WHERE player_uuid = '"+homeowner_uuid+"' AND home_name LIKE '"+homeName+"';");

                rs = ps.executeQuery();
                while(rs.next()) {
                    if (rs.getString("privacy_status").equals("1")) {
                        privacy_status.add("public");
                    } else {
                        privacy_status.add("private");
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
            return privacy_status;
        });
    }

    public void importOldMyHomeDatabase(File file, MyHomes plugin) throws SQLException {

        Connection conn = DriverManager.getConnection("jdbc:sqlite:" + file);
        Statement ps = conn.createStatement();

        try {
            ResultSet rs = ps.executeQuery("SELECT * FROM homeTable");
                while (rs.next()) {
                    setHomeColumnsFromOldDatabase(uuidFetcher.getOfflinePlayerUUID(rs.getString("name")).join(), "Home", rs.getString("world"), rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z"), rs.getFloat("yaw"), rs.getFloat("pitch"), rs.getBoolean("publicAll"));

                    String[] elements = rs.getString("permissions").split(",");
                    List<String> inviteList = Arrays.asList(elements);

                    if (!rs.getString("permissions").isEmpty()) {
                        for (String invitedPlayer : inviteList) {
                            setInviteColumnsFromOldDatabase(uuidFetcher.getOfflinePlayerUUID(rs.getString("name")).join(), "Home", uuidFetcher.getOfflinePlayerUUID(invitedPlayer).join());
                            plugin.getLogger().info("Imported invite: [ Homeowner UUID: " + uuidFetcher.getOfflinePlayerUUID(rs.getString("name")).join() + ", Home Name: Home, Invited Player: " + uuidFetcher.getOfflinePlayerUUID(invitedPlayer).join());
                        }
                    } else {
                        inviteList = List.of("No invites.");
                    }

                    plugin.getLogger().info("Imported home: [ Homeowner Name: " + rs.getString("name") + ", Homeowner UUID: " + uuidFetcher.getOfflinePlayerUUID(rs.getString("name")).join() + ", Home Name: Home, World: " + rs.getString("world") + ", X: " + rs.getDouble("x") + ", Y: " + rs.getDouble("y") + ", Z: " + rs.getDouble("z") + ", Yaw: " + rs.getFloat("yaw") + ", Pitch: " + rs.getFloat("pitch") + ", Privacy Status: " + rs.getBoolean("publicAll") + ", Invites: " + inviteList +" ]");
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
    }

    public void setHomeColumnsFromOldDatabase(String playerUUID, String homeName, String worldName, Double x, Double y, Double z, Float yaw, Float pitch, Boolean privacy_status) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("REPLACE INTO " + homesTable + " (player_uuid,home_name,world,x,y,z,yaw,pitch,privacy_status) VALUES(?,?,?,?,?,?,?,?,?)"); // IMPORTANT. In SQLite class, We made 3 colums. player, Kills, Total.
            ps.setString(1, playerUUID);
            ps.setString(2, homeName);
            ps.setString(3, worldName);
            ps.setDouble(4, x);
            ps.setDouble(5, y);
            ps.setDouble(6, z);
            ps.setFloat(7, yaw);
            ps.setFloat(8, pitch);
            ps.setBoolean(9, privacy_status);
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

    public void setInviteColumnsFromOldDatabase(String homeOwnerUUID, String homeName, String invitedPlayerUUID) {

        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("REPLACE INTO " + invitesTable + " (invited_player_uuid,homeowner_uuid,home_name) VALUES(?,?,?)");
            ps.setString(1, invitedPlayerUUID);
            ps.setString(2, homeOwnerUUID);
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
