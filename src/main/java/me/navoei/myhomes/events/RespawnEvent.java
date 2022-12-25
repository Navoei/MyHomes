package me.navoei.myhomes.events;

import me.navoei.myhomes.MyHomes;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.List;

public class RespawnEvent implements Listener {

    MyHomes plugin = MyHomes.getInstance();

    @EventHandler(ignoreCancelled = true)
    public void onPlayerRespawn(PlayerRespawnEvent event) {

        List<String> home = plugin.getRDatabase().getHome(event.getPlayer(), "Home").join();
        if (home.isEmpty()) return;

        World world = plugin.getServer().getWorld(home.get(0));
        Location homeLocation = new Location(world, Double.parseDouble(home.get(1)), Double.parseDouble(home.get(2)), Double.parseDouble(home.get(3)), Float.parseFloat(home.get(4)), Float.parseFloat(home.get(5)));

        event.setRespawnLocation(homeLocation);
    }

}
