package eu.acewolf.spigot.worldtimeout;

import eu.acewolf.spigot.worldtimeout.listeners.JoinQuitListener;
import eu.acewolf.spigot.worldtimeout.listeners.PlayerCommandListener;
import eu.acewolf.spigot.worldtimeout.listeners.TeleportListener;
import eu.acewolf.spigot.worldtimeout.mysql.MySQL;
import eu.acewolf.spigot.worldtimeout.mysql.PlayerTimeoutMySQL;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.PermissionNode;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
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
        playerTimeoutMySQL.createTable();

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
        pm.registerEvents(new TeleportListener(this), this);
        pm.registerEvents(new PlayerCommandListener(), this);
        pm.registerEvents(new JoinQuitListener(), this);
    }

    public void registerCommands(){
    }

    public HashMap<Player, Long> getActivityTimeout() {
        return activityTimeout;
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
                    player.sendMessage(WorldTimeout.PREFIX +
                            WorldTimeout.getInstance().getConfig().getString("settings.time.expired").replace("&", "§"));
                    WorldTimeout.getInstance().getPlayerTimeoutMySQL().removePlayerTimeout(player.getUniqueId().toString(), world);
                    WorldTimeout.getInstance().activityTimeout.remove(player);
                    PermissionNode node = PermissionNode.builder(permission).value(true).build();
                    user.data().remove(node);
                    LuckPermsProvider.get().getUserManager().saveUser(user);
                    player.performCommand(WorldTimeout.getInstance().getConfig().getString("settings.time.command").replace("/", ""));
                    cancel();
                    return;
                }

                if(WorldTimeout.getInstance().getConfig().getBoolean("settings.actionbar.activated")){
                    long seconds = remainingTimeout / 1000;
                    long minutes = seconds / 60;
                    long remainingSeconds = seconds % 60;
                    long hours = minutes / 60;

                    String actionbarFormat = WorldTimeout.getInstance().getConfig().getString("settings.actionbar.format").replace("&", "§");
                    actionbarFormat = actionbarFormat.replace("%HOURS%", String.valueOf(hours));
                    actionbarFormat = actionbarFormat.replace("%MINUTES%", String.valueOf(minutes));
                    actionbarFormat = actionbarFormat.replace("%SECONDS%", String.valueOf(remainingSeconds));

                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(actionbarFormat));
                }
                WorldTimeout.getInstance().activityTimeout.replace(player, remainingTimeout);
            }
        }.runTaskTimer(WorldTimeout.getInstance(), 20, 20);
    }

    public boolean calculateTime(String timeoutString, long timeout, Player player, String permission, String key, long totalTimeout, long currentTime){

        if (timeoutString.contains("h")) {
            timeout = Long.parseLong(timeoutString.replace("h", ""));
        } else if (timeoutString.contains("m")) {
            timeout = Long.parseLong(timeoutString.replace("m", ""));
        }

        User user = LuckPermsProvider.get().getPlayerAdapter(Player.class).getUser(player);
        permission = "system.realm.world." + timeout + "." + key;

        if (!WorldTimeout.getInstance().hasPermission(user, permission)) {
            return false;
        }

        if (WorldTimeout.getInstance().getPlayerTimeoutMySQL().hasPlayerTimeoutInWorld(player.getUniqueId().toString(), key)) {
            totalTimeout = WorldTimeout.getInstance().getPlayerTimeoutMySQL().getPlayerTimeout(player.getUniqueId().toString(), key);
            totalTimeout = currentTime + totalTimeout + 1000;

            WorldTimeout.getInstance().getActivityTimeout().put(player, totalTimeout);
            WorldTimeout.getInstance().run(totalTimeout, player, key, permission, user);
            return true;
        }

        timeout = WorldTimeout.getInstance().durationStringToMilliseconds(timeoutString);
        totalTimeout = currentTime + timeout + 1000;
        WorldTimeout.getInstance().getPlayerTimeoutMySQL().addPlayerTimeout(player.getUniqueId().toString(), key, totalTimeout);
        WorldTimeout.getInstance().getActivityTimeout().put(player, totalTimeout);

        WorldTimeout.getInstance().run(totalTimeout, player, key, permission, user);
        return true;
    }

    public boolean hasPermission(User user, String permission) {
        return user.getCachedData().getPermissionData().checkPermission(permission).asBoolean();
    }

    public long durationStringToMilliseconds(String durationString) {
        Duration duration = Duration.parse("PT" + durationString.toUpperCase());
        return duration.toMillis();
    }

    @Override
    public void onDisable() {
        mySQL.disconnect();
    }
}
