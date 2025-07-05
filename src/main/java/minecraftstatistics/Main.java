package minecraftstatistics;

import minecraftstatistics.Classes.MySQL;
import minecraftstatistics.Commands.StatsCommand;
import minecraftstatistics.Commands.DebuggerCommand;
import minecraftstatistics.Commands.SyncPlayersCommand;

import minecraftstatistics.Listeners.SetPlayerOfflineListener;
import minecraftstatistics.Listeners.SetPlayerOnlineListener;

import minecraftstatistics.Tasks.CollectPlayerDataTask;

import org.bukkit.Statistic;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class Main extends JavaPlugin {

    public static FileConfiguration config;
    public static Integer updateFrequency;
    public static Main plugin;

    private MySQL mysql;
    private CollectPlayerDataTask collectPlayerDataTask;

    @Override
    public void onEnable() {

        getLogger().info("Available Statistics :");
        for (Statistic stat : Statistic.values()) {
            getLogger().info("- " + stat.name());
        }

        createConfig();
        config = getConfig();
        plugin = this;

        updateFrequency = config.getInt("frequency");

        // Initialize TThe Database
        this.mysql = new MySQL(this);

        // Set All Players To Offline
        String query = "UPDATE " + mysql.getTableName() + " SET IS_ONLINE=0";
        this.mysql.update(query).exceptionally(ex -> {
            getLogger().severe("Could Not Set All Players To Offline!");
            return null;
        });

        // Register listeners
        getServer().getPluginManager().registerEvents(new SetPlayerOfflineListener(this), this);
        getServer().getPluginManager().registerEvents(new SetPlayerOnlineListener(this), this);

        // Collect Player Data
        this.collectPlayerDataTask = new CollectPlayerDataTask(this);
        this.collectPlayerDataTask.runTaskTimerAsynchronously(this, 20, updateFrequency * 20L);

        // Register Commands
        this.getCommand("statsync").setExecutor(new SyncPlayersCommand(this));
        this.getCommand("statistics").setExecutor(new StatsCommand(this));
        this.getCommand("debug").setExecutor(new DebuggerCommand(this));
    }

    @Override
    public void onDisable() {
        if (mysql != null) {
            mysql.close();
        }
    }

    public MySQL getMysql() {
        return mysql;
    }

    public CollectPlayerDataTask getCollectPlayerDataTask() {
        return collectPlayerDataTask;
    }

    private void createConfig() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        File file = new File(getDataFolder(), "config.yml");
        if (!file.exists()) {
            getLogger().info("Config.yml Not Found Creating!");
            saveDefaultConfig();
        }
    }

}
