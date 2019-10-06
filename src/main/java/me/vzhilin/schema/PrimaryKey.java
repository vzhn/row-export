package me.vzhilin.schema;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

public class PrimaryKey {
    private final Optional<String> name;
    private final Table table;
    private final Set<Column> columns = new HashSet<>();
    private final Set<ForeignKey> foreignKeys = new HashSet<>();

    public PrimaryKey(Optional<String> name, Table table) {
        this.name = name;
        this.table = table;
    }

    public void addColumn(Column column, int keySeq) {
        columns.add(column);
    }

    public void addForeignKey(ForeignKey foreignKey) {
        foreignKeys.add(foreignKey);
    }

    public Set<ForeignKey> getForeignKeys() {
        return Collections.unmodifiableSet(foreignKeys);
    }

    public Set<Column> getColumns() {
        return Collections.unmodifiableSet(columns);
    }

    public Set<String> getColumnNames() {
        return columns.stream().map(Column::getName).collect(toSet());
    }
}
