package minecraftstatistics.Tasks;

import minecraftstatistics.Main;

import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;

public class CollectPlayerDataTask extends BukkitRunnable {

    private final Main plugin;

    public CollectPlayerDataTask(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updatePlayerStats(player);
        }
    }

    public void updatePlayerStats(Player player) {
        List<Statistic> statistics = plugin.getMysql().getStatistics();

        List<Object> params = new ArrayList<>();
        StringBuilder columns = new StringBuilder();
        StringBuilder placeholders = new StringBuilder();
        StringBuilder updateClause = new StringBuilder();


        columns.append("UUID, NAME, LAST_JOIN, IS_ONLINE, XP, XP_LEVEL, HEALTH, FOOD_LEVEL, ");
        placeholders.append("?, ?, ?, TRUE, ?, ?, ?, ?, ");
        updateClause.append(
                "NAME = VALUES(NAME), LAST_JOIN = VALUES(LAST_JOIN), IS_ONLINE = VALUES(IS_ONLINE), XP = VALUES(XP), XP_LEVEL = VALUES(XP_LEVEL), HEALTH = VALUES(HEALTH), FOOD_LEVEL = VALUES(FOOD_LEVEL), ");


        params.add(player.getUniqueId().toString());
        params.add(player.getName());
        params.add(System.currentTimeMillis() / 1000L);
        params.add(player.getTotalExperience());
        params.add(player.getLevel());
        params.add(player.getHealth());
        params.add(player.getFoodLevel());

        for (Statistic statistic : statistics) {
            if (statistic.getType() == Statistic.Type.UNTYPED) {
                String colName = "`" + statistic.name() + "`";
                columns.append(colName).append(", ");
                placeholders.append("?, ");
                updateClause.append(colName).append(" = VALUES(").append(colName).append("), ");
                params.add(player.getStatistic(statistic));
            }
        }

        columns.setLength(columns.length() - 2);
        placeholders.setLength(placeholders.length() - 2);
        updateClause.setLength(updateClause.length() - 2);

        String sql = "INSERT INTO " + plugin.getMysql().getTableName() + " (" + columns.toString() + ") VALUES ("
                + placeholders.toString() + ") ON DUPLICATE KEY UPDATE " + updateClause.toString() + ";";

        plugin.getMysql().update(sql, params.toArray()).exceptionally(ex -> {
            plugin.getLogger().log(Level.SEVERE, "Could Not Sync Player " + player.getName(), ex);
            return null;
        });
    }
}
