package eu.acewolf.spigot.worldtimeout.listeners;

import eu.acewolf.spigot.worldtimeout.WorldTimeout;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.PermissionNode;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;

public class TeleportListener implements Listener {

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event){
        Player player = event.getPlayer();
        World world = event.getTo().getWorld();
        World fromWorld = event.getFrom().getWorld();
        long currentTime = System.currentTimeMillis();
        long timeout = 0;
        long totalTimeout;
        String permission;

        for(String key : WorldTimeout.getInstance().getConfig().getConfigurationSection("worlds").getKeys(false)){
            if (fromWorld.getName().equalsIgnoreCase(key) && WorldTimeout.getInstance().activityTimeout.get(player) != null &&
            WorldTimeout.getInstance().getPlayerTimeoutMySQL().hasPlayerTimeoutInWorld(player.getUniqueId().toString(), key)) {
                long activty = WorldTimeout.getInstance().activityTimeout.get(player);
                WorldTimeout.getInstance().getPlayerTimeoutMySQL().updatePlayerTimeout(player.getUniqueId().toString(), fromWorld.getName(), activty);
                WorldTimeout.getInstance().activityTimeout.remove(player);
                break;
            }
            if (!world.getName().equalsIgnoreCase(key)) {
                break;
            }
            String timeoutString = WorldTimeout.getInstance().getConfig().getString("worlds." + key);

            if(timeoutString.contains("h")){
                timeout = Integer.parseInt(timeoutString.replace("h", ""));
            } else if(timeoutString.contains("m")){
                timeout = Integer.parseInt(timeoutString.replace("m", ""));
            }

            User user = LuckPermsProvider.get().getPlayerAdapter(Player.class).getUser(player);

            permission = "system.realm.world." + timeout + "." + key;
            if(!hasPermission(user, permission)){
                player.sendMessage("Keine Berechtigung");
                event.setCancelled(true);
                break;
            }
            if (WorldTimeout.getInstance().getPlayerTimeoutMySQL().hasPlayerTimeoutInWorld(player.getUniqueId().toString(), key)) {
                totalTimeout = WorldTimeout.getInstance().getPlayerTimeoutMySQL().getPlayerTimeout(player.getUniqueId().toString(), key);
                totalTimeout = currentTime + totalTimeout + 1000;

                WorldTimeout.getInstance().activityTimeout.put(player, totalTimeout);

                run(totalTimeout, player, key, permission, user);
                break;
            }


            timeout = durationStringToMilliseconds(timeoutString);
            totalTimeout = currentTime + timeout + 1000;
            WorldTimeout.getInstance().getPlayerTimeoutMySQL().addPlayerTimeout(player.getUniqueId().toString(), key, totalTimeout);
            WorldTimeout.getInstance().activityTimeout.put(player, totalTimeout);

            run(totalTimeout, player, key, permission, user);
            break;
        }


    }

    public boolean hasPermission(User user, String permission) {
        return user.getCachedData().getPermissionData().checkPermission(permission).asBoolean();
    }

    private long durationStringToMilliseconds(String durationString) {
        Duration duration = Duration.parse("PT" + durationString.toUpperCase());
        return duration.toMillis();
    }

    public void run(long end, Player player, String world, String permission, User user) {

        new BukkitRunnable() {
            @Override
            public void run() {
                if(!player.isOnline()){
                    cancel();
                    return;
                }

                long start = System.currentTimeMillis();
                long remainingTimeout = end - start;

                if (!WorldTimeout.getInstance().activityTimeout.containsKey(player)) {
                    cancel();
                    return;
                }
                if (remainingTimeout <= 0) {
                    player.sendMessage("Zeit abgelaufen");
                    WorldTimeout.getInstance().getPlayerTimeoutMySQL().removePlayerTimeout(player.getUniqueId().toString(), world);
                    WorldTimeout.getInstance().activityTimeout.remove(player);
                    PermissionNode node = PermissionNode.builder(permission).value(true).build();
                    user.data().remove(node);
                    LuckPermsProvider.get().getUserManager().saveUser(user);
                    player.performCommand("spawn");
                    cancel();
                    return;
                }

                long seconds = remainingTimeout / 1000;
                long minutes = seconds / 60;
                long remainingSeconds = seconds % 60;

                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("ZEIT: " + minutes + "m " + remainingSeconds + "s"));
                WorldTimeout.getInstance().activityTimeout.replace(player, remainingTimeout);
            }
        }.runTaskTimer(WorldTimeout.getInstance(), 20, 20);
    }
}
