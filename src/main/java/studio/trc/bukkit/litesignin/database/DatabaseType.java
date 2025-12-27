package studio.trc.bukkit.litesignin.database;

import studio.trc.bukkit.litesignin.database.engine.MySQLEngine;
import studio.trc.bukkit.litesignin.database.engine.SQLiteEngine;

public enum DatabaseType {
    SQLITE,

    MYSQL;

    public static String getTableSyntax(DatabaseType type) {
        return switch (type) {
            case MYSQL -> MySQLEngine.getInstance().getTableSyntax(DatabaseTable.PLAYER_DATA);
            case SQLITE -> SQLiteEngine.getInstance().getTableSyntax(DatabaseTable.PLAYER_DATA);
        };
    }
}
