package me.vzhilin.schema;

import me.vzhilin.util.BiMap;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Table {
    private final String name;
    private Optional<PrimaryKey> primaryKey;
    private final Map<String, ForeignKey> foreignKeys = new HashMap<>();
    private final Map<String, Column> columns = new HashMap<>();

    public Table(String name) {
        this.name = name;
    }

    public Column addColumn(String columnName, String columnType) {
        Column column = new Column(this, columnName, columnType);
        columns.put(columnName, column);
        return column;
    }

    public Column getColumn(String columnName) {
        return columns.get(columnName);
    }

    public Optional<PrimaryKey> getPrimaryKey() {
        return primaryKey;
    }

    public void setPk(PrimaryKey pk) {
        primaryKey = Optional.ofNullable(pk);
    }

    public ForeignKey addForeignKey(String fkName, Table toTable, BiMap<Column, Column> cols) {
        ForeignKey foreignKey = new ForeignKey(fkName, this, toTable, cols);
        foreignKeys.put(fkName, foreignKey);
        return foreignKey;
    }

    public String getName() {
        return name;
    }

    public PrimaryKey getPk() {
        return null;
    }

    public Map<String, Column> getColumns() {
        return Collections.unmodifiableMap(columns);
    }

    @Override
    public String toString() {
        return name;
    }
}
