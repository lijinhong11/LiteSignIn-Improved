package studio.trc.bukkit.litesignin.database;

import lombok.Getter;
import studio.trc.bukkit.litesignin.configuration.ConfigurationType;
import studio.trc.bukkit.litesignin.configuration.ConfigurationUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum DatabaseTable {
    /**
     * Players data.
     */
    PLAYER_DATA("PlayerData", Arrays.asList(new DatabaseElement("UUID", "VARCHAR(36)", false, true),
            new DatabaseElement("Name", "VARCHAR(16)", true, false),
            new DatabaseElement("Year", "INT", true, false),
            new DatabaseElement("Month", "INT", true, false),
            new DatabaseElement("Day", "INT", true, false),
            new DatabaseElement("Hour", "INT", true, false),
            new DatabaseElement("Minute", "INT", true, false),
            new DatabaseElement("Second", "INT", true, false),
            new DatabaseElement("Continuous", "INT", true, false),
            new DatabaseElement("RetroactiveCard", "INT", true, false),
            new DatabaseElement("History", "LONGTEXT", true, false)
    ));

    @Getter
    private final String name;
    @Getter
    private final List<DatabaseElement> elements;

    DatabaseTable(String name, List<DatabaseElement> elements) {
        this.name = name;
        this.elements = elements;
    }

    public String getCreateTableSyntax(DatabaseType type) {
        StringBuilder builder = new StringBuilder();
        builder.append("CREATE TABLE IF NOT EXISTS ").append(DatabaseType.getTableSyntax(type)).append("(");
        int number = 0;
        for (DatabaseElement element : elements) {
            number++;
            builder.append(element.field()).append(" ").append(element.type());
            if (!element.isNull()) builder.append(" NOT NULL");
            if (number < elements.size()) builder.append(",");
        }
        builder.append(getPrimaryKeysSyntax()).append(")");
        return builder.toString();
    }

    public String getDefaultCreateTableSyntax() {
        StringBuilder builder = new StringBuilder();
        builder.append("CREATE TABLE IF NOT EXISTS ").append(getName()).append("(");
        int number = 0;
        for (DatabaseElement element : elements) {
            number++;
            builder.append(element.field()).append(" ").append(element.type());
            if (!element.isNull()) builder.append(" NOT NULL");
            if (number < elements.size()) builder.append(",");
        }
        builder.append(getPrimaryKeysSyntax()).append(")");
        return builder.toString();
    }

    public String getDisplayName() {
        return ConfigurationUtil.getConfig(ConfigurationType.CONFIG).getString("MySQL-Storage.Table-Name");
    }

    private String getPrimaryKeysSyntax() {
        StringBuilder builder = new StringBuilder();
        boolean non = true;
        for (DatabaseElement element : elements) {
            if (element.primaryKey()) {
                non = false;
                break;
            }
        }
        if (non) return builder.toString();
        builder.append(", PRIMARY KEY (");
        List<String> primaryKeys = new ArrayList<>();
        elements.stream().filter(DatabaseElement::primaryKey).forEach(element -> {
            primaryKeys.add(element.field());
        });
        if (!primaryKeys.isEmpty()) {
            builder.append(primaryKeys.toString(), 1, primaryKeys.toString().length() - 1);
        }
        builder.append(")");
        return builder.toString();
    }
}
