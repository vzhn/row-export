package me.vzhilin.db;

import me.vzhilin.schema.Column;
import me.vzhilin.schema.ForeignKey;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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

    public Row forwardReference(ForeignKey fk) {
        Map<Column, Object> pkValues = new HashMap<>();
        // TODO check not null
        fk.getColumnMapping().forEach((pkColumn, fkColumn) -> pkValues.put(pkColumn, get(fkColumn)));
        return new Row(ctx, new ObjectKey(fk.getPkTable(), pkValues));
    }

    public Map<ForeignKey, Long> backwardReferencesCount() {
        Set<ForeignKey> foreignKeys = key.getTable().getPrimaryKey().get().getForeignKeys();
        Map<ForeignKey, Long> result = new HashMap<>(foreignKeys.size());
        foreignKeys.forEach(fk -> result.put(fk, ctx.backReferencesCount(this, fk)));
        return result;
    }

    public Iterator<Row> backwardReference(ForeignKey fk) {
        return null;
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
}
