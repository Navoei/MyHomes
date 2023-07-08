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
    TOO_MANY_HOMES("toomanyhomes", "&cYou cannot set more than %maximum_number_of_homes% homes."),
    HOME_SPECIFIED_UPDATED("specifiedhomeupdated", "&aYour home, %home%, has been updated with a new location."),
    INVALID_CHARACTERS("invalidcharacters", "&cOnly alphanumeric characters can be used."),
    TOO_MANY_CHARACTERS("toomanycharacters", "&cHome names can only be %character_limit% characters long."),
    HOME("home", "&aWelcome to your home."),
    HOME_OTHER("homeother", "&aWelcome to %player%'s home."),
    SPECIFIED_HOME("homespecified", "&aWelcome to %home%."),
    HOME_NOT_EXISTS("homenotexist", "&cThis home does not exist."),
    HOME_DELETED("deletehome", "&aYour home has been deleted."),
    HOME_DELETED_SPECIFIED("deletespecifiedhome", "&aYour home, %home%, has been deleted."),
    RENAME_HOME("renamehome", "&aYour home, %home_name%, has been renamed to %new_home_name%."),
    RENAME_HOME_SAME_NAME("renamehome_samename", "&aThe home is already named %home%."),
    PLAYER_NOT_INVITED("playernotinvited", "&cYou are not invited to %player%'s home."),
    PLAYER_NOT_INVITED_SPECIFIED("playernotinvitedspecified", "&cYou are not invited to %home%."),
    PLAYER_HAS_NO_HOME("playerhasnohome", "&cYou do not have a home."),
    CANNOT_INVITE_SELF("cannotinviteself", "&cYou cannot invite yourself."),
    CANNOT_UNINVITE_SELF("cannotuninviteself", "&cYou cannot uninvite yourself."),
    INVITED_TO_DEFAULT_HOME("invitedtodefaulthome", "&aThe player, %player%, has been invited to your home."),
    INVITED_TO_SPECIFIED_HOME("invitedtospecifiedhome", "&aThe player, %player%, has been invited to %home%."),
    UNINVITED_FROM_DEFAULT_HOME("uninvitedfromdefaulthome", "&aThe player, %player%, has been uninvited from your home."),
    UNINVITED_FROM_SPECIFIED_HOME("uninvitedfromspecifiedhome", "&aThe player, %player%, has been uninvited from %home%."),
    ALREADY_INVITED_TO_DEFAULT_HOME("alreadyinvitedtodefaulthome", "&cThe player, %player%, is already invited to your home."),
    ALREADY_INVITED_TO_SPECIFIED_HOME("alreadyinvitedtospecifiedhome", "&cThe player, %player%, is already invited to %home%."),
    NOT_INVITED_TO_DEFAULT_HOME("notinvitedtodefaulthome", "&cThe player, %player%, has not been invited to your home."),
    NOT_INVITED_TO_SPECIFIED_HOME("notinvitedtospecifiedhome", "&cThe player, %player%, has not been invited to %home%."),
    PLAYER_NEVER_LOGGED_ON("playerhasneverloggedon", "&cThe player, %player%, has never joined the server."),
    MESSAGE_TO_INVITED_PLAYER_DEFAULT_HOME("messagetoinvitedplayerdefaulthome", "&aYou were invited to %homeowner%'s home."),
    MESSAGE_TO_INVITED_PLAYER_SPECIFIED_HOME("messagetoinvitedplayerspecifiedhome", "&aYou were invited to %home%, which is owned by %homeowner%."),
    DEFAULT_HOME_PUBLIC("defaulthomepublic", "&aYou have changed the privacy status of your home to public."),
    DEFAULT_HOME_PRIVATE("defaulthomeprivate", "&aYou have changed the privacy status of your home to private."),
    SPECIFIED_HOME_PUBLIC("specifiedhomepublic", "&aYou have changed the privacy status of %home% to public."),
    SPECIFIED_HOME_PRIVATE("specifiedhomeprivate", "&aYou have changed the privacy status of %home% to private."),
    NO_HOMES("nohomes", "&cYou do not have any homes."),
    NO_INVITES("noinvites", "&cYou do not have any invites."),
    NO_INVITES_TO_HOME("noinvitestohome", "&cThis home does not have any invites."),
    PLAYER_NO_HOMES("playernohomes" ,"&cThe player, %player%, does not have any homes."),
    PLAYER_NO_INVITES("playernoinvites", "&cThe player, %player%, does not have any invites."),
    MANAGE_HOMES_PLAYER_HAS_NO_HOME("managehomes_playerhasnohome", "&cThe player, %player%, does not have a home by the name %home%."),
    MANAGE_HOMES_CANNOT_INVITE_SELF("managehomes_cannotinviteself", "&cThe player, %player%, cannot be invited to their own home."),
    MANAGE_HOMES_PLAYER_ALREADY_INVITED_TO_DEFAULT_HOME("managehomes_playeralreadyinvitedtodefaulthome", "&cThe player, %invited_player%, has already been invited to %homeowner%'s home."),
    MANAGE_HOMES_PLAYER_ALREADY_INVITED_TO_SPECIFIED_HOME("managehomes_playeralreadyinvitedtospecifiedhome", "&cThe player, %invited_player%, has already been invited to %home%, which is owned by %homeowner%."),
    MANAGE_HOMES_INVITED_TO_DEFAULT_HOME("managehomes_invitedtodefaulthome", "&aThe player, %invited_player%, has been invited to %homeowner%'s home."),
    MANAGE_HOMES_INVITED_TO_SPECIFIED_HOME("managehomes_invitedtospecifiedhome", "&aThe player, %invited_player%, has been invited to %home%, which is owned by %homeowner%."),
    MANAGE_HOMES_CANNOT_UNINVITE_SELF("managehomes_cannotuninviteself", "&cThe player, %player%, cannot be uninvited from their own home."),
    MANAGE_HOMES_HAS_NOT_BEEN_INVITED_TO_DEFAULT_HOME("managehomes_hasnotbeeninvitedtodefaulthome", "&cThe player, %uninvited_player%, has not been invited to %homeowner%'s home."),
    MANAGE_HOMES_HAS_NOT_BEEN_INVITED_TO_SPECIFIED_HOME("managehomes_hasnotbeeninvitedtospecifiedhome", "&cThe player, %uninvited_player%, has not been invited to %home%, which is owned by %homeowner%."),
    MANAGE_HOMES_UNINVITED_FROM_DEFAULT_HOME("managehomes_uninvitedfromdefaulthome", "&aThe player, %uninvited_player%, has been uninvited from %homeowner%'s home."),
    MANAGE_HOMES_UNINVITED_FROM_SPECIFIED_HOME("managehomes_uninvitedfromspecifiedhome", "&aThe player, %uninvited_player%, has been uninvited from %home%, which is owned by %homeowner%."),
    MANAGE_HOMES_PRIVACY_STATUS_PRIVATE_DEFAULT_HOME("managehomes_privacystatusprivatedefaulthome", "&aYou have changed the privacy status of %homeowner%'s home to private."),
    MANAGE_HOMES_PRIVACY_STATUS_PUBLIC_DEFAULT_HOME("managehomes_privacystatuspublicdefaulthome", "&aYou have changed the privacy status of %homeowner%'s home to public."),
    MANAGE_HOMES_PRIVACY_STATUS_PRIVATE_SPECIFIED_HOME("managehomes_privacystatusprivatespecifiedhome", "&aYou have changed the privacy status of %home%, which is owned by %homeowner%, to private."),
    MANAGE_HOMES_PRIVACY_STATUS_PUBLIC_SPECIFIED_HOME("managehomes_privacystatuspublicspecifiedhome", "&aYou have changed the privacy status of %home%, which is owned by %homeowner%, to public."),
    MANAGE_HOMES_SET_PLAYER_DEFAULT_HOME("managehomes_setplayerdefaulthome", "&aYou have set %player%'s home."),
    MANAGE_HOMES_SET_PLAYER_SPECIFIED_HOME("managehomes_setplayerspecifiedhome", "&aYou have set a home, %home%, for %player%."),
    MANAGE_HOMES_UPDATED_LOCATION_DEFAULT_HOME("managehomes_updatedlocationdefaulthome", "&aYou have updated the location of %homeowner%'s home."),
    MANAGE_HOMES_UPDATED_LOCATION_SPECIFIED_HOME("managehomes_updatedlocationspecifiedhome", "&aYou have updated the location of %home%, which is owned by %homeowner%."),
    MANAGE_HOMES_DELETE_DEFAULT_HOME("managehomes_deletedefaulthome", "&aYou have deleted %player%'s home."),
    MANAGE_HOMES_DELETE_SPECIFIED_HOME("managehomes_deletespecifiedhome", "&aYou have deleted %home%, which belonged to %homeowner%."),
    MANAGE_HOMES_RENAME_HOME("managehomes_renamehome", "&aYou have renamed %homeowner%'s home, %previous_home_name%, to %new_home_name%."),
    MANAGE_HOMES_SAME_NAME("managehomes_samename", "&aThe home is already named %home%.");

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
