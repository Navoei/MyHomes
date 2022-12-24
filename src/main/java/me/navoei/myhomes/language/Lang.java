package me.navoei.myhomes.language;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * An enum for requesting strings from the language file.
 * @author gomeow
 */
public enum Lang {
    PREFIX("prefix", "&8[&bMyHomes&8] &7»&r"),
    NO_PERMISSION("nopermission", "&cYou do not have permission to execute this command."),
    PLAYER_ONLY("onlyplayerscommand", "&cOnly players can execute this command."),
    CONSOLE_ONLY("onlyconsolecommand", "&cOnly console can execute this command."),
    OLD_DATABASE_NOT_EXIST("olddatabasenotexist", "&cOld database does not exist."),
    IMPORTING_DATABASE("importingdatabase", "&aOld database found. Importing now..."),
    DATABASE_IMPORT_ERROR("databaseimporterror", "&cAn error occurred while importing the database."),
    DATABASE_IMPORT_SUCCESS("databaseimportsuccess", "&aImport successful! You may now delete the old database."),
    TOO_MANY_ARGUMENTS("toomanyarguments", "&cToo many arguments."),
    NOT_ENOUGH_ARGUMENTS("notenougharguments", "&cNot enough arguments."),
    INVALID_ARGUMENTS("invalidarguments", "&cInvalid arguments."),
    SET_HOME("sethome", "&aYour home has been set."),
    SET_HOME_SPECIFIED("sethomespecified", "&aYour home, %home%, has been set."),
    HOME_UPDATED("homeupdated", "&aYour home has been updated with a new location."),
    HOME_SPECIFIED_UPDATED("specifiedhomeupdated", "&aYour home, %home%, has been updated with a new location."),
    HOME("home", "&aWelcome to your home."),
    HOME_OTHER("homeother", "&aWelcome to %player%'s home."),
    SPECIFIED_HOME("homespecified", "&aWelcome to %home%."),
    HOME_NOT_EXISTS("homenotexist", "&cThis home does not exist."),
    HOME_DELETED("deletehome", "&aYour home has been deleted."),
    HOME_DELETED_SPECIFIED("deletespecifiedhome", "&aYour home, %home%, has been deleted."),
    PLAYER_NOT_INVITED("playernotinvited", "&cYou are not invited to %player%'s home."),
    PLAYER_NOT_INVITED_SPECIFIED("playernotinvitedspecified", "&cYou are not invited to %home%."),
    PLAYER_HAS_NO_HOME("playerhasnohome", "&cYou do not have a home."),
    CANNOT_INVITE_SELF("cannotinviteself", "&cYou cannot invite yourself."),
    CANNOT_UNINVITE_SELF("cannotuninviteself", "&cYou cannot uninvite yourself."),
    INVITED_TO_DEFAULT_HOME("invitedtodefaulthome", "&aThe player %player% has been invited to your home."),
    INVITED_TO_SPECIFIED_HOME("invitedtospecifiedhome", "&aThe player %player% has been invited to %home%."),
    UNINVITED_FROM_DEFAULT_HOME("uninvitedfromdefaulthome", "&aThe player %player% has been uninvited from your home."),
    UNINVITED_FROM_SPECIFIED_HOME("uninvitedfromspecifiedhome", "&aThe player %player% has been uninvited from %home%."),
    ALREADY_INVITED_TO_DEFAULT_HOME("alreadyinvitedtodefaulthome", "&cThe player %player% is already invited to your home."),
    ALREADY_INVITED_TO_SPECIFIED_HOME("alreadyinvitedtospecifiedhome", "&cThe player %player% is already invited to %home%."),
    NOT_INVITED_TO_DEFAULT_HOME("notinvitedtodefaulthome", "&cThe player %player% has not been invited to your home."),
    NOT_INVITED_TO_SPECIFIED_HOME("notinvitedtospecifiedhome", "&cThe player %player% has not been invited to %home%."),
    PLAYER_NEVER_LOGGED_ON("playerhasneverloggedon", "&cThe player %player% has never joined the server."),
    DEFAULT_HOME_PUBLIC("defaulthomepublic", "&aYou have changed the privacy status of your home to public."),
    DEFAULT_HOME_PRIVATE("defaulthomeprivate", "&aYou have changed the privacy status of your home to private."),
    SPECIFIED_HOME_PUBLIC("specifiedhomepublic", "&aYou have changed the privacy status of %home% to public."),
    SPECIFIED_HOME_PRIVATE("specifiedhomeprivate", "&aYou have changed the privacy status of %home% to private.");

    private final String path;
    private final String def;
    private static YamlConfiguration LANG;

    /**
     * Lang enum constructor.
     * @param path The string path.
     * @param start The default string.
     */
    Lang(String path, String start) {
        this.path = path;
        this.def = start;
    }

    /**
     * Set the {@code YamlConfiguration} to use.
     * @param config The config to set.
     */
    public static void setFile(YamlConfiguration config) {
        LANG = config;
    }

    @Override
    public String toString() {
        if (this == PREFIX)
            return ChatColor.translateAlternateColorCodes('&', LANG.getString(this.path, def)) + " ";
        return ChatColor.translateAlternateColorCodes('&', LANG.getString(this.path, def));
    }

    /**
     * Get the default value of the path.
     * @return The default value of the path.
     */
    public String getDefault() {
        return this.def;
    }

    /**
     * Get the path to the string.
     * @return The path to the string.
     */
    public String getPath() {
        return this.path;
    }
}
