package eu.acewolf.spigot.worldtimeout.listeners;

import com.Zrips.CMI.Modules.Teleportations.CMITeleportType;
import com.Zrips.CMI.events.CMIAsyncPlayerTeleportEvent;
import eu.acewolf.spigot.worldtimeout.WorldTimeout;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerCommandListener implements Listener {

    @EventHandler
    public void onTeleport(CMIAsyncPlayerTeleportEvent event){
        Player player = event.getPlayer();
        if(event.getType() == CMITeleportType.Back) {
            if (WorldTimeout.getInstance().getConfig().getConfigurationSection("worlds").getKeys(false).contains(event.getTo().getWorld().getName())) {
                player.sendMessage(WorldTimeout.PREFIX + WorldTimeout.getInstance().getConfig().getString("settings.time.twoWorlds")
                        .replace("&", "§"));
                event.setCancelled(true);
            }
        }
    }
}
