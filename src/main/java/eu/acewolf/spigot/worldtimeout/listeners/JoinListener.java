package eu.acewolf.spigot.worldtimeout.listeners;

import eu.acewolf.spigot.worldtimeout.WorldTimeout;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();

        for(String key : WorldTimeout.getInstance().getConfig().getConfigurationSection("worlds").getKeys(false)) {
            if(player.getWorld().getName().equalsIgnoreCase(key)){
                player.performCommand(WorldTimeout.getInstance().getConfig().getString("settings.time.command").replace("/", ""));
                break;
            }
            break;
        }
    }
}
