package eu.acewolf.spigot.worldtimeout.listeners;

import eu.acewolf.spigot.worldtimeout.WorldTimeout;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.util.Map;

public class TeleportListener implements Listener {

    private WorldTimeout plugin;

    public TeleportListener(WorldTimeout plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        World world = event.getTo().getWorld();
        World fromWorld = event.getFrom().getWorld();
        long currentTime = System.currentTimeMillis();
        long timeout = 0;
        long totalTimeout = 0;
        String permission = "";

        Map<String, Object> configWorlds = plugin.getConfig().getConfigurationSection("worlds").getValues(false);

        if (configWorlds.containsKey(world.getName()) && configWorlds.containsKey(fromWorld.getName())) {
            player.sendMessage(WorldTimeout.PREFIX + plugin.getConfig().getString("settings.time.twoWorlds").replace("&", "§"));
            event.setCancelled(true);
            return;
        }

        for (Map.Entry<String, Object> entry : configWorlds.entrySet()) {
            String key = entry.getKey();
            String timeoutString = (String) entry.getValue();

            if (fromWorld.getName().equalsIgnoreCase(key) && plugin.getActivityTimeout().containsKey(player) &&
                    plugin.getPlayerTimeoutMySQL().hasPlayerTimeoutInWorld(player.getUniqueId().toString(), key)) {
                long activity = plugin.getActivityTimeout().get(player);
                plugin.getPlayerTimeoutMySQL().updatePlayerTimeout(player.getUniqueId().toString(), fromWorld.getName(), activity);
                plugin.getActivityTimeout().remove(player);
                break;
            }

            if (!world.getName().equalsIgnoreCase(key)) {
                continue;
            }

            boolean result = WorldTimeout.getInstance().calculateTime(timeoutString, timeout, player, permission, key, totalTimeout, currentTime);

            if(!result){
                player.sendMessage(WorldTimeout.PREFIX +
                        WorldTimeout.getInstance().getConfig().getString("settings.noPerms").replace("&", "§"));
                event.setCancelled(true);
                break;
            }
            return;
        }
    }
}
