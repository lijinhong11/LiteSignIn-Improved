package studio.trc.bukkit.litesignin.database;

import studio.trc.bukkit.litesignin.database.engine.SQLQuery;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface DatabaseEngine {
    /**
     * Connect to database.
     */
    void connect();

    /**
     * Disconnect from database.
     */
    void disconnect();

    /**
     * Check connection status.
     *
     * @throws SQLException
     */
    void checkConnection() throws SQLException;

    /**
     * Execute update syntax.
     *
     * @param sqlSyntax
     * @param values
     * @return How much rows affected.
     */
    int executeUpdate(String sqlSyntax, String... values);

    /**
     * Execute multi queries.
     *
     * @param sqlSyntax
     * @param parameters
     * @return How much rows affected.
     */
    int[] executeMultiQueries(String sqlSyntax, List<Map<Integer, String>> parameters);

    /**
     * Execute query syntax.
     *
     * @param sqlSyntax
     * @param values
     * @return The results.
     */
    SQLQuery executeQuery(String sqlSyntax, String... values);

    /**
     * Get database connection intance.
     *
     * @return
     */
    Connection getConnection();

    /**
     * Throw SQL exception.
     *
     * @param exception
     * @param path
     * @param reconnect
     */
    void throwSQLException(Exception exception, String path, boolean reconnect);

    /**
     * Initialization method.
     */
    void initialize();
}
