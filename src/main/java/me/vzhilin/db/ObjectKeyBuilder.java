package me.vzhilin.db;

import java.util.HashMap;
import java.util.Map;

import me.vzhilin.schema.Column;
import me.vzhilin.schema.Table;

public class ObjectKeyBuilder {
    private final Map<Column, Object> keyValues = new HashMap<>();
    private final Table table;

    public ObjectKeyBuilder(Table table) {
        this.table = table;
    }

    public ObjectKeyBuilder set(String columnName, Object value) {
        Column column = table.getColumn(columnName);
        keyValues.put(column, value);
        return this;
    }

    public ObjectKey build() {
        return new ObjectKey(table, keyValues);
    }
}
