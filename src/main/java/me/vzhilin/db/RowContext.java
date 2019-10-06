package me.vzhilin.db;

import com.google.common.base.Joiner;
import me.vzhilin.schema.Column;
import me.vzhilin.schema.ForeignKey;
import me.vzhilin.schema.Schema;
import me.vzhilin.schema.Table;
import me.vzhilin.util.BiMap;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class RowContext {
    private final QueryRunner runner;
    private final Schema schema;

    public RowContext(QueryRunner runner, Schema schema) {
        this.runner = runner;
        this.schema = schema;
    }

    public Map<Column, Object> fetchValues(ObjectKey key) {
        Table table = key.getTable();
        String columns = Joiner.on(',').join(table.getColumns().values());
        StringBuilder sb = new StringBuilder("SELECT ");
        sb.append(columns).append(" FROM ").append(table.getName()).append(" WHERE ");
        Map<Column, Object> vs = key.getValues();
        List<String> parts = new ArrayList<>(vs.size());
        List<Object> params = new ArrayList<>(vs.size());
        vs.forEach((column, value) -> {
            parts.add(column.getName() + " = ? ");
            params.add(value);
        });
        sb.append(Joiner.on("AND").join(parts));
        Map<String, Object> rawResult;
        try {
            rawResult = runner.query(sb.toString(), new MapHandler(), params.toArray());
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }

        Map<Column, Object> result = new HashMap<>(rawResult.size());
        rawResult.forEach((name, o) -> result.put(table.getColumn(name), o));
        return result;
    }

    public Long backReferencesCount(Row row, ForeignKey foreignKey) {
        Table table = foreignKey.getFromTable();
        String tableName = table.getName();

        StringBuilder sb = new StringBuilder("SELECT COUNT(1) FROM ");
        sb.append(tableName);
        sb.append(" WHERE ");

        Map<Column, Object> vs = row.getKey().getValues();
        // pk -> fk
        BiMap<Column, Column> mapping = foreignKey.getColumnMapping();

        List<String> parts = new ArrayList<>(vs.size());
        List<Object> params = new ArrayList<>(vs.size());
        vs.forEach((pkColumn, value) -> {
            Column fkColumn = mapping.get(pkColumn);
            parts.add(fkColumn.getName() + " = ? ");
            params.add(value);
        });

        try {
            return runner.query(sb.toString(), new ScalarHandler<>(), params.toArray());
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }
}
