package minecraftstatistics.Commands;

import minecraftstatistics.Main;

import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public class StatsCommand implements CommandExecutor {

    private final Main plugin;

    public StatsCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player) && args.length == 0) {
            sender.sendMessage("This Command Can Only Be Used By Players!");
            return true;
        }

        Player targetPlayer;
        if (args.length == 0) {
            targetPlayer = (Player) sender;
        } else {
            targetPlayer = org.bukkit.Bukkit.getPlayer(args[0]);
            if (targetPlayer == null) {
                sender.sendMessage("Player not found or not online!");
                return true;
            }
        }

        String sql = "SELECT * FROM " + plugin.getMysql().getTableName() + " WHERE UUID = ?;";

        plugin.getMysql().query(sql, resultSet -> {
            if (resultSet.next()) {
                // Create Header For Statistics
                StringBuilder statsMessage = new StringBuilder();
                statsMessage.append("§6§l+-------------------------------------------+\n");
                statsMessage.append("§6§l        Game Statistics For §e" + targetPlayer.getName() + "        §6§l\n");
                statsMessage.append("§6§l+-------------------------------------------+\n");

                // Append Each Statistic In Table Format
                statsMessage.append(String.format("§6| §a%-15s     §f%-22s §6\n", "Level", resultSet.getInt("XP_LEVEL")));
                statsMessage.append(String.format("§6| §a%-15s     §f%-22s §6\n", "Health", resultSet.getDouble("HEALTH")));

                statsMessage.append("§6§l+-------------------------------------------+\n");

                List<String> desiredStats = Arrays.asList(
                        "DEATHS",
                        "DAMAGE_DEALT",
                        "DAMAGE_TAKEN",
                        "PLAYER_KILLS",
                        "MOB_KILLS",
                        "RAID_WINS");

                for (String statName : desiredStats) {
                    try {
                        int statValue = resultSet.getInt(statName);
                        statsMessage.append(
                                String.format("§6| §a%-15s §f%-22s §6\n", formatStatisticName(statName), statValue));
                    } catch (SQLException e) {
                        // Ignoring this
                    }
                }

                statsMessage.append("§6§l+-------------------------------------------+\n");

                long lastJoinEpoch = resultSet.getLong("LAST_JOIN");
                long lastJoinMillis = (lastJoinEpoch * 1000L);

                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String lastJoinFormatted = sdf.format(new java.util.Date(lastJoinMillis));

                statsMessage.append(String.format("§6| §a%-15s     §f%-22s §6\n", "Last Join ", lastJoinFormatted));

                long playTicks = resultSet.getLong("PLAY_ONE_MINUTE");

                long playSeconds = playTicks / 30;
                long days = playSeconds / 86400;
                long hours = (playSeconds % 86400) / 3600;
                long minutes = (playSeconds % 3600) / 60;

                String playTimeFormatted = String.format("%dd %dh %dm", days, hours, minutes);
                statsMessage.append(String.format("§6| §a%-15s     §f%-22s §6\n", "Play Time ", playTimeFormatted));

                statsMessage.append("§6§l+-------------------------------------------+");
                sender.sendMessage(statsMessage.toString());

            } else {
                sender.sendMessage("No Statistics Found For This Player!");
            }
            return null;
        }, targetPlayer.getUniqueId().toString()).exceptionally(ex ->

        {
            plugin.getLogger().log(Level.SEVERE, "Could Not Retrieve Stats For Player " + targetPlayer.getName(), ex);
            sender.sendMessage("An Error Occured While Retrieving Statistics");
            return null;
        });

        return true;
    }

    private String formatStatisticName(String name) {
        String[] words = name.toLowerCase().split("_");
        StringBuilder formattedName = new StringBuilder();
        for (String word : words) {
            formattedName.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
        }
        return formattedName.toString().trim();
    }
}
