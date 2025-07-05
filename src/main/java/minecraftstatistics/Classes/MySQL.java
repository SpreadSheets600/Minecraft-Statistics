package minecraftstatistics.Classes;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import minecraftstatistics.Main;

import org.bukkit.Bukkit;
import org.bukkit.Statistic;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.concurrent.CompletableFuture;

public class MySQL {

    private final HikariDataSource dataSource;
    private final String tableName;
    private final Main plugin;

    public MySQL(Main plugin) {
        this.plugin = plugin;

        // Loading MySQL Configuration From The Main Config File
        String host = Main.config.getString("mysql.host", "localhost");
        int port = Main.config.getInt("mysql.port", 3306);

        String database = Main.config.getString("mysql.database", "minecraft");

        String username = Main.config.getString("mysql.username", "user");
        String password = Main.config.getString("mysql.password", "password");

        this.tableName = Main.config.getString("mysql.table", "statistics");

        // Configure HikariCP
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);

        config.setUsername(username);
        config.setPassword(password);

        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        config.setConnectionTimeout(5000);
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);

        this.dataSource = new HikariDataSource(config);

        // Initialize The Database Schema
        initializeDatabase();
    }

    private void initializeDatabase() {
        List<Statistic> statistics = getStatistics();

        // Base Table Structure
        StringBuilder createTableQuery = new StringBuilder("CREATE TABLE IF NOT EXISTS " + tableName + " (");
        createTableQuery.append("`SERVER` VARCHAR(100) NULL DEFAULT 'default',");
        createTableQuery.append("`UUID` VARCHAR(36) NOT NULL PRIMARY KEY,");
        createTableQuery.append("`NAME` VARCHAR(16) NOT NULL,");
        createTableQuery.append("`LAST_JOIN` BIGINT,");
        createTableQuery.append("`IS_ONLINE` BOOLEAN DEFAULT FALSE,");
        createTableQuery.append("`XP` INT DEFAULT 0,");
        createTableQuery.append("`XP_LEVEL` INT DEFAULT 0,");
        createTableQuery.append("`HEALTH` DOUBLE DEFAULT 20.0,");
        createTableQuery.append("`FOOD_LEVEL` INT DEFAULT 20");

        // Add Rest Of The Statistics Columns
        for (Statistic statistic : statistics) {
            if (statistic.getType() == Statistic.Type.UNTYPED) {
                createTableQuery.append(", `").append(statistic.name()).append("` INT DEFAULT 0");
            } else {
                plugin.getLogger().warning("Statistic '" + statistic.name()
                        + "' is a typed statistic and is not supported in the config.yml. It will be ignored.");
            }
        }

        createTableQuery.append(");");

        update(createTableQuery.toString()).exceptionally(ex -> {
            plugin.getLogger().log(Level.SEVERE, "Could Not Initialize Database!", ex);
            return null;
        });
    }

    public CompletableFuture<Void> update(String query, Object... params) {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                    PreparedStatement statement = connection.prepareStatement(query)) {

                for (int i = 0; i < params.length; i++) {
                    statement.setObject(i + 1, params[i]);
                }
                statement.executeUpdate();

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, runnable -> Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable));
    }

    public <T> CompletableFuture<T> query(String query, ResultSetTransformer<T> transformer, Object... params) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                    PreparedStatement statement = connection.prepareStatement(query)) {

                for (int i = 0; i < params.length; i++) {
                    statement.setObject(i + 1, params[i]);
                }

                try (ResultSet resultSet = statement.executeQuery()) {
                    return transformer.transform(resultSet);
                }

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, runnable -> Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable));
    }

    @FunctionalInterface
    public interface ResultSetTransformer<T> {
        T transform(ResultSet resultSet) throws SQLException;
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    public String getTableName() {
        return tableName;
    }

    public List<Statistic> getStatistics() {
        List<String> statNames = Main.config.getStringList("statistics");
        List<Statistic> statistics = new ArrayList<>();
        for (String statName : statNames) {
            try {
                statistics.add(Statistic.valueOf(statName.toUpperCase()));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid Statistics In Config : " + statName);
            }
        }
        return statistics;
    }
}
