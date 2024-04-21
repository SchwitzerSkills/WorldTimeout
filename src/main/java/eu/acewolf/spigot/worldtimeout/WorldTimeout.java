package eu.acewolf.spigot.worldtimeout;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class WorldTimeout extends JavaPlugin {

    public static WorldTimeout instance;

    public static final String PREFIX = "§cWorldTimeout §8| §7";

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
    }

    public void registerListeners(PluginManager pm){

    }

    public void registerCommands(){

    }

    public static WorldTimeout getInstance() {
        return instance;
    }

    @Override
    public void onDisable() {

    }
}
