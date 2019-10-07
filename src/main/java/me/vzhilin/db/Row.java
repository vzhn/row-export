package me.vzhilin.db;

import me.vzhilin.schema.Column;
import me.vzhilin.schema.ForeignKey;
import me.vzhilin.schema.Table;

import java.util.*;

public final class Row {
    private final ObjectKey key;
    private final RowContext ctx;
    private final Map<Column, Object> values = new HashMap<>();
    private boolean loaded;

    public Row(RowContext ctx, ObjectKey key) {
        this.ctx = ctx;
        this.key = key;
    }

    public Object get(Column column) {
        ensureLoaded();

        return values.get(column);
    }

    public Table getTable() {
        return key.getTable();
    }

    public Map<Column, Object> getValues() {
        ensureLoaded();
        return Collections.unmodifiableMap(values);
    }

    public Map<ForeignKey, Row> forwardReferences() {
        Map<ForeignKey, Row> result = new HashMap<>();
        for (ForeignKey fk: key.getTable().getForeignKeys().values()) {
            Map<Column, Object> key = new HashMap<>();
            final boolean[] hasNull = {false};
            fk.getColumnMapping().forEach((pkColumn, fkColumn) -> {
                Object value = get(fkColumn);
                if (!hasNull[0] && value != null) {
                    key.put(pkColumn, value);
                } else {
                    hasNull[0] = true;
                }
            });
            if (hasNull[0]) {
                continue;
            }
            result.put(fk, new Row(ctx, new ObjectKey(fk.getPkTable(), key)));
        }
        return result;
    }

    public Map<ForeignKey, Number> backwardReferencesCount() {
        Set<ForeignKey> foreignKeys = key.getTable().getPrimaryKey().get().getForeignKeys();
        Map<ForeignKey, Number> result = new HashMap<>(foreignKeys.size());
        foreignKeys.forEach(fk -> result.put(fk, ctx.backReferencesCount(this, fk)));
        return result;
    }

    public Iterable<Row> backwardReference(ForeignKey fk) {
        return ctx.backReferences(this, fk);
    }

    private void ensureLoaded() {
        if (!loaded) {
            loaded = true;
            values.putAll(ctx.fetchValues(key));
        }
    }

    public ObjectKey getKey() {
        return key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Row row = (Row) o;
        return key.equals(row.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }

    @Override
    public String toString() {
        return "Row{" +
                "key=" + key +
                '}';
    }
}
