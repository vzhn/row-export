package me.vzhilin.schema;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import me.vzhilin.util.BiMap;

public final class Table {
    private final String name;
    private final Schema schema;
    private Optional<PrimaryKey> primaryKey;
    private final Map<String, ForeignKey> foreignKeys = new LinkedHashMap<>();
    private final Map<String, Column> columns = new LinkedHashMap<>();

    public Table(Schema schema, String name) {
        this.schema = schema;
        this.name = name;
    }

    public String getSchemaName() {
        return schema.getName();
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

    public Map<String, Column> getColumns() {
        return Collections.unmodifiableMap(columns);
    }

    public Map<String, ForeignKey> getForeignKeys() {
        return Collections.unmodifiableMap(foreignKeys);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Table table = (Table) o;
        return name.equals(table.name) &&
                schema.equals(table.schema);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, schema);
    }
}
