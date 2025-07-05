package minecraftstatistics.Listeners;

import minecraftstatistics.Main;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.logging.Level;

public class SetPlayerOnlineListener implements Listener {

    private final Main plugin;

    public SetPlayerOnlineListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        long unixTime = System.currentTimeMillis() / 1000L;

        String sql = "INSERT INTO " + plugin.getMysql().getTableName() + " (UUID, name, last_join, is_online) VALUES (?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE name = VALUES(name), last_join = VALUES(last_join), is_online = VALUES(is_online);";

        plugin.getMysql().update(sql, player.getUniqueId().toString(), player.getName(), unixTime, true)
                .exceptionally(ex -> {
                    plugin.getLogger().log(Level.SEVERE, "Could Not Set " + player.getName() + " To Online!", ex);
                    return null;
                });
    }
}
