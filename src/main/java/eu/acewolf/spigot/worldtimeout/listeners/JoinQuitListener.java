package eu.acewolf.spigot.worldtimeout.listeners;

import eu.acewolf.spigot.worldtimeout.WorldTimeout;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class JoinQuitListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        long currentTime = System.currentTimeMillis();

        for (String key : WorldTimeout.getInstance().getConfig().getConfigurationSection("worlds").getKeys(false)) {
            if (player.getWorld().getName().equalsIgnoreCase(key)) {
                String timeoutString = WorldTimeout.getInstance().getConfig().getString("worlds." + key);
                long timeout = 0;
                long totalTimeout = 0;
                String permission = "";

                boolean result = WorldTimeout.getInstance().calculateTime(timeoutString, timeout, player, permission, key, totalTimeout, currentTime);

                if(!result){
                    player.sendMessage(WorldTimeout.PREFIX +
                            WorldTimeout.getInstance().getConfig().getString("settings.noPerms").replace("&", "§"));
                    player.performCommand(WorldTimeout.getInstance().getConfig().getString("settings.time.command").replace("/", ""));
                    break;
                }

                break;
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (WorldTimeout.getInstance().activityTimeout.containsKey(player)) {
            long activty = WorldTimeout.getInstance().activityTimeout.get(player);
            WorldTimeout.getInstance().getPlayerTimeoutMySQL().updatePlayerTimeout(player.getUniqueId().toString(), player.getWorld().getName(), activty);
            WorldTimeout.getInstance().activityTimeout.remove(player);
        }
    }
}
