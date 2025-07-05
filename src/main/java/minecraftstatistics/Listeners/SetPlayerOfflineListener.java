package minecraftstatistics.Listeners;

import minecraftstatistics.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.logging.Level;

public class SetPlayerOfflineListener implements Listener {

    private final Main plugin;

    public SetPlayerOfflineListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        String sql = "UPDATE " + plugin.getMysql().getTableName() + " SET is_online = ? WHERE UUID = ?;";

        plugin.getMysql().update(sql, false, player.getUniqueId().toString())
                .exceptionally(ex -> {
                    plugin.getLogger().log(Level.SEVERE, "Could Not Set Player" + player.getName() + " To Offline!", ex);
                    return null;
                });
    }
}
