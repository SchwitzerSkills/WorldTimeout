package eu.acewolf.spigot.worldtimeout;

import eu.acewolf.spigot.worldtimeout.listeners.JoinListener;
import eu.acewolf.spigot.worldtimeout.listeners.TeleportListener;
import eu.acewolf.spigot.worldtimeout.mysql.MySQL;
import eu.acewolf.spigot.worldtimeout.mysql.PlayerTimeoutMySQL;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

public class WorldTimeout extends JavaPlugin {

    public static WorldTimeout instance;

    public static String PREFIX = "";

    private MySQL mySQL;
    private PlayerTimeoutMySQL playerTimeoutMySQL;
    public HashMap<Player, Long> activityTimeout = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        PREFIX = getConfig().getString("settings.prefix").replace("&", "§");

        connectMySQL();

        registerListeners(Bukkit.getPluginManager());
        registerCommands();
    }

    public void connectMySQL(){
        mySQL = new MySQL(
                getConfig().getString("mysql.host"),
                getConfig().getString("mysql.database"),
                getConfig().getString("mysql.username"),
                getConfig().getString("mysql.password")
        );

        playerTimeoutMySQL = new PlayerTimeoutMySQL();
        playerTimeoutMySQL.createTable();
    }

    public void registerListeners(PluginManager pm){
        pm.registerEvents(new TeleportListener(), this);
        pm.registerEvents(new JoinListener(), this);
    }

    public void registerCommands(){
    }

    public static WorldTimeout getInstance() {
        return instance;
    }

    public MySQL getMySQL() {
        return mySQL;
    }

    public PlayerTimeoutMySQL getPlayerTimeoutMySQL() {
        return playerTimeoutMySQL;
    }

    @Override
    public void onDisable() {
        mySQL.disconnect();
    }
}
