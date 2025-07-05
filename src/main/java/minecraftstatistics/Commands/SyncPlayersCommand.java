package minecraftstatistics.Commands;

import minecraftstatistics.Main;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class SyncPlayersCommand implements CommandExecutor {

    private final Main plugin;

    public SyncPlayersCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        sender.sendMessage("Starting Player Statistics Sync...");

        for (Player player : Bukkit.getOnlinePlayers()) {
            plugin.getCollectPlayerDataTask().updatePlayerStats(player);
        }

        sender.sendMessage("Player Sync Completed Successfully!");
        return true;
    }
}
