//BIG THANKS to EvilMidget38 for providing this handy UUID lookup tool to the Bukkit community!  :)
package me.navoei.myhomes.uuid;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.navoei.myhomes.MyHomes;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class UUIDFetcher {
    private static int PROFILES_PER_REQUEST = 100;
    private static final String PROFILE_URL = "https://api.mojang.com/profiles/minecraft";
    private final Gson gson = new Gson();
    private final List<String> names;
    private final boolean rateLimiting;
    private final MyHomes plugin = MyHomes.getInstance();
    //cache for username -> uuid lookups
    static HashMap<String, UUID> uuidLookupCache;

    //cache for uuid -> username lookups
    static HashMap<UUID, String> usernameLookupCache;

    //record of username -> proper casing updates
    static HashMap<String, String> correctedNames;

    public UUIDFetcher(List<String> names, boolean rateLimiting) {
        this.names = names;
        this.rateLimiting = rateLimiting;
    }

    public UUIDFetcher(List<String> names) {
        this(names, true);
    }

    public void call() throws Exception {
        if (uuidLookupCache == null)
        {
            uuidLookupCache = new HashMap<>();
        }

        if (correctedNames == null)
        {
            correctedNames = new HashMap<>();
        }

        if (usernameLookupCache == null)
        {
            usernameLookupCache = new HashMap<>();
        }

        OfflinePlayer[] players = MyHomes.getInstance().getServer().getOfflinePlayers();
        int offlinePlayersLength = players.length;
        plugin.getLogger().log(Level.INFO, "Caching "+offlinePlayersLength+ " player UUIDs.");
        for (OfflinePlayer player : players) {
            if (player.getName() != null) {
                uuidLookupCache.put(player.getName(), player.getUniqueId());
                uuidLookupCache.put(player.getName().toLowerCase(), player.getUniqueId());
                usernameLookupCache.put(player.getUniqueId(), player.getName());
                correctedNames.put(player.getName().toLowerCase(), player.getName());
            }
        }

        //try to get correct casing from local data
        if (names!=null) {
            for (int i = 0; i < names.size(); i++) {
                String name = names.get(i);
                String correctCasingName = correctedNames.get(name);
                if (correctCasingName != null && !name.equals(correctCasingName))
                {
                    //GriefPrevention.AddLogEntry(name + " --> " + correctCasingName);
                    names.set(i, correctCasingName);
                }
            }
            //look for local uuid's first
            for (int i = 0; i < names.size(); i++)
            {
                String name = names.get(i);
                UUID uuid = uuidLookupCache.get(name);
                if (uuid != null)
                {
                    names.remove(i--);
                }
            }

            names.removeIf(Objects::isNull);


            //for online mode, call Mojang to resolve the rest
            if (MyHomes.getInstance().getServer().getOnlineMode()) {
                Pattern validNamePattern = Pattern.compile("^\\w+$");

                // Don't bother requesting UUIDs for invalid names from Mojang.
                names.removeIf(name ->
                {
                    if (name.length() >= 3 && name.length() <= 16 && validNamePattern.matcher(name).find())
                        return false;

                    return true;
                });

                for (int i = 0; i * PROFILES_PER_REQUEST < names.size(); i++) {
                    boolean retry = false;
                    JsonArray array = null;
                    do
                    {
                        HttpURLConnection connection = createConnection();
                        String body = gson.toJson(names.subList(i * PROFILES_PER_REQUEST, Math.min((i + 1) * PROFILES_PER_REQUEST, names.size())));
                        writeBody(connection, body);
                        retry = false;
                        array = null;
                        try
                        {
                            array = gson.fromJson(new InputStreamReader(connection.getInputStream()), JsonArray.class);
                        }
                        catch (Exception e)
                        {
                            //in case of error 429 too many requests, pause and then retry later
                            if (e.getMessage().contains("429"))
                            {
                                retry = true;

                                //if this is the first time we're sending anything, the batch size must be too big
                                //try reducing it
                                if (i == 0 && PROFILES_PER_REQUEST > 1)
                                {
                                    PROFILES_PER_REQUEST = Math.max(PROFILES_PER_REQUEST - 5, 1);
                                }

                                //otherwise, keep the batch size which has worked for previous iterations
                                //but wait a little while before trying again.
                                else
                                {
                                    //Mojang says we're sending requests too fast. Will retry every 30 seconds until we succeed...
                                    Thread.sleep(30000);
                                }
                            }
                            else
                            {
                                throw e;
                            }
                        }
                    } while (retry);

                    for (JsonElement profile : array)
                    {
                        JsonObject jsonProfile = profile.getAsJsonObject();
                        String id = jsonProfile.get("id").getAsString();
                        String name = jsonProfile.get("name").getAsString();
                        UUID uuid = UUIDFetcher.getUUID(id);
                        uuidLookupCache.put(name, uuid);
                        uuidLookupCache.put(name.toLowerCase(), uuid);
                        usernameLookupCache.put(uuid, name);
                    }
                    if (rateLimiting)
                    {
                        Thread.sleep(200L);
                    }
                }
            } else { //for offline mode, generate UUIDs for the rest
                for (String name : names)
                {
                    UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(Charsets.UTF_8));
                    uuidLookupCache.put(name, uuid);
                    uuidLookupCache.put(name.toLowerCase(), uuid);
                    usernameLookupCache.put(uuid, name);
                }
            }
        }
    }

    private static void writeBody(HttpURLConnection connection, String body) throws Exception {
        OutputStream stream = connection.getOutputStream();
        stream.write(body.getBytes());
        stream.flush();
        stream.close();
    }

    private static HttpURLConnection createConnection() throws Exception {
        URL url = new URL(PROFILE_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setUseCaches(false);
        connection.setDoInput(true);
        connection.setDoOutput(true);
        return connection;
    }

    private static UUID getUUID(String id) {
        return UUID.fromString(id.substring(0, 8) + "-" + id.substring(8, 12) + "-" + id.substring(12, 16) + "-" + id.substring(16, 20) + "-" + id.substring(20, 32));
    }

    public static byte[] toBytes(UUID uuid) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[16]);
        byteBuffer.putLong(uuid.getMostSignificantBits());
        byteBuffer.putLong(uuid.getLeastSignificantBits());
        return byteBuffer.array();
    }

    public static UUID fromBytes(byte[] array) {
        if (array.length != 16)
        {
            throw new IllegalArgumentException("Illegal byte array length: " + array.length);
        }
        ByteBuffer byteBuffer = ByteBuffer.wrap(array);
        long mostSignificant = byteBuffer.getLong();
        long leastSignificant = byteBuffer.getLong();
        return new UUID(mostSignificant, leastSignificant);
    }

    public static void addPlayer(Player player) {
        uuidLookupCache.put(player.getName(), player.getUniqueId());
        uuidLookupCache.put(player.getName().toLowerCase(), player.getUniqueId());
        usernameLookupCache.put(player.getUniqueId(), player.getName());
    }

    public static String getPlayerUUIDSync(String name) {
        Player player = Bukkit.getPlayerExact(name);
        if (player != null) {
            uuidLookupCache.putIfAbsent(name, player.getUniqueId());
        }
        return uuidLookupCache.get(name).toString();
    }

    public static CompletableFuture<String> getPlayerUUID(String name) {
        return CompletableFuture.supplyAsync(() ->
        {
            UUID uuid = uuidLookupCache.computeIfAbsent(name, key -> {
                Player player = Bukkit.getPlayerExact(key);
                return player != null ? player.getUniqueId() : null;
            });
            //System.out.println(uuid);
            return uuid != null ? uuid.toString() : null;
        });
    }

    public static String getPlayerNameFromUUID(String playerUUID) {
        return usernameLookupCache.computeIfAbsent(UUID.fromString(playerUUID), key -> {
            Player player = Bukkit.getPlayer(key);
            return player != null ? player.getName() : null;
        });
    }

    public static CompletableFuture<Boolean> checkPlayedBefore(String playerName) {
        return CompletableFuture.supplyAsync(() -> {
            String offlinePlayerUUID = getPlayerUUIDSync(playerName);
            if (offlinePlayerUUID!=null && !offlinePlayerUUID.isEmpty()) {
                return Bukkit.getPlayer(UUID.fromString(offlinePlayerUUID))!=null || Bukkit.getOfflinePlayer(UUID.fromString(offlinePlayerUUID)).hasPlayedBefore();
            } else {
                return false;
            }
        });
    }

    public static Set<UUID> getAllPlayerUUIDs() {
        return usernameLookupCache.keySet();
    }
}