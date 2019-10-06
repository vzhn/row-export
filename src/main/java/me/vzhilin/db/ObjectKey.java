package me.vzhilin.db;

import me.vzhilin.schema.Column;
import me.vzhilin.schema.Table;

import java.util.Map;
import java.util.Objects;

public final class ObjectKey {
    private final Table table;
    private final Map<Column, Object> values;

    public ObjectKey(Table table, Map<Column, Object> values) {
        this.table = table;
        this.values = values;
    }

    public Table getTable() {
        return table;
    }

    public Map<Column, Object> getValues() {
        return values;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ObjectKey objectKey = (ObjectKey) o;
        return table.equals(objectKey.table) &&
                values.equals(objectKey.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(table, values);
    }
}
