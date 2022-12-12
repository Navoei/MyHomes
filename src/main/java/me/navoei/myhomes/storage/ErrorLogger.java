package me.navoei.myhomes.storage;

import me.navoei.myhomes.MyHomes;

import java.util.logging.Level;

public class ErrorLogger {
    public static void execute(MyHomes plugin, Exception ex){
        plugin.getLogger().log(Level.SEVERE, "Couldn't execute MySQL statement: ", ex);
    }
    public static void close(MyHomes plugin, Exception ex){
        plugin.getLogger().log(Level.SEVERE, "Failed to close MySQL connection: ", ex);
    }
}
