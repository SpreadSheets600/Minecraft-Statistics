package minecraftstatistics.Commands;

import minecraftstatistics.Main;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;

public class DebuggerCommand implements CommandExecutor {

    private final Main plugin;

    public DebuggerCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        sender.sendMessage("Debugging Information :");
        sender.sendMessage("Table Name : " + plugin.getMysql().getTableName());
        sender.sendMessage("Update Frequency : " + Main.updateFrequency);
        return true;
    }
}
