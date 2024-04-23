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
        long totalTimeout;
        String permission;

        Map<String, Object> configWorlds = plugin.getConfig().getConfigurationSection("worlds").getValues(false);

        if (configWorlds.containsKey(world.getName()) && configWorlds.containsKey(fromWorld.getName())) {
            player.sendMessage(WorldTimeout.PREFIX + plugin.getConfig().getString("settings.time.twoWorlds").replace("&", "§"));
            event.setCancelled(true);
            return;
        }

        for (Map.Entry<String, Object> entry : configWorlds.entrySet()) {
            String key = entry.getKey();
            String timeoutString = (String) entry.getValue();

            if (fromWorld.getName().equalsIgnoreCase(key) && plugin.getActivityTimeout().get(player) != null &&
                    plugin.getPlayerTimeoutMySQL().hasPlayerTimeoutInWorld(player.getUniqueId().toString(), key)) {
                long activity = plugin.getActivityTimeout().get(player);
                plugin.getPlayerTimeoutMySQL().updatePlayerTimeout(player.getUniqueId().toString(), fromWorld.getName(), activity);
                plugin.getActivityTimeout().remove(player);
                break;
            }

            if (!world.getName().equalsIgnoreCase(key)) {
                continue;
            }

            if (timeoutString.contains("h")) {
                timeout = Long.parseLong(timeoutString.replace("h", ""));
            } else if (timeoutString.contains("m")) {
                timeout = Long.parseLong(timeoutString.replace("m", ""));
            }

            User user = LuckPermsProvider.get().getUserManager().getUser(player.getUniqueId());
            permission = "system.realm.world." + timeout + "." + key;

            if (!plugin.hasPermission(user, permission)) {
                player.sendMessage(WorldTimeout.PREFIX + plugin.getConfig().getString("settings.noPerms").replace("&", "§"));
                event.setCancelled(true);
                return;
            }

            if (plugin.getPlayerTimeoutMySQL().hasPlayerTimeoutInWorld(player.getUniqueId().toString(), key)) {
                totalTimeout = plugin.getPlayerTimeoutMySQL().getPlayerTimeout(player.getUniqueId().toString(), key);
                totalTimeout = currentTime + totalTimeout + 1000;

                plugin.getActivityTimeout().put(player, totalTimeout);
                plugin.run(totalTimeout, player, key, permission, user);
                return;
            }

            timeout = plugin.durationStringToMilliseconds(timeoutString);
            totalTimeout = currentTime + timeout + 1000;
            plugin.getPlayerTimeoutMySQL().addPlayerTimeout(player.getUniqueId().toString(), key, totalTimeout);
            plugin.getActivityTimeout().put(player, totalTimeout);

            plugin.run(totalTimeout, player, key, permission, user);
            return;
        }
    }
}
