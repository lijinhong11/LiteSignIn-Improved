package studio.trc.bukkit.litesignin.database.engine;

import lombok.Getter;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLQuery
        implements AutoCloseable {
    @Getter
    private final ResultSet result;
    @Getter
    private final PreparedStatement statement;

    public SQLQuery(ResultSet result, PreparedStatement statement) {
        this.result = result;
        this.statement = statement;
    }

    @Override
    public void close() {
        try {
            result.close();
            statement.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
