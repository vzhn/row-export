package me.vzhilin.schema;

import me.vzhilin.util.BiMap;

public final class ForeignKey {
    private final String fkName;
    private final Table fromTable;
    private final Table toTable;
    private final BiMap<Column, Column> columnMapping;

    public ForeignKey(String fkName, Table fromTable, Table toTable, BiMap<Column, Column> columnMapping) {
        this.fkName = fkName;
        this.fromTable = fromTable;
        this.toTable = toTable;
        this.columnMapping = columnMapping;
    }

    public String getFkName() {
        return fkName;
    }

    public Table getFromTable() {
        return fromTable;
    }

    public Table getPkTable() {
        return toTable;
    }

    /**
     * @return pkColumn -&gt; fkColumn
     */
    public BiMap<Column, Column> getColumnMapping() {
        return columnMapping;
    }

    @Override
    public String toString() {
        return "ForeignKey{" +
                "fkName='" + fkName + '\'' +
                ", fromTable=" + fromTable +
                '}';
    }
}
