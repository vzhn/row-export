package me.vzhilin.db;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import com.google.common.base.Joiner;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import me.vzhilin.schema.Column;
import me.vzhilin.schema.ForeignKey;
import me.vzhilin.schema.Schema;
import me.vzhilin.schema.Table;
import me.vzhilin.util.BiMap;

public final class RowContext {
    private final QueryRunner runner;
    private final Schema schema;

    public RowContext(QueryRunner runner, Schema schema) {
        this.runner = runner;
        this.schema = schema;
    }

    public Map<Column, Object> fetchValues(ObjectKey key) {
        Table table = key.getTable();
        String columns = Joiner.on(',').join(table.getColumns().keySet());
        StringBuilder sb = new StringBuilder("SELECT ");
        sb.append(columns).append(" FROM ").append(table.getSchemaName() + "." + table.getName()).append(" WHERE ");
        Map<Column, Object> vs = key.getValues();
        List<String> parts = new ArrayList<>(vs.size());
        List<Object> params = new ArrayList<>(vs.size());
        vs.forEach((column, value) -> {
            parts.add(column.getName() + " = ? ");
            params.add(value);
        });
        sb.append(Joiner.on(" AND ").join(parts));
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

    public Number backReferencesCount(Row row, ForeignKey foreignKey) {
        Table table = foreignKey.getFromTable();
        String tableName = table.getName();

        StringBuilder query = new StringBuilder("SELECT COUNT(1) FROM ");
        query.append(table.getSchemaName()).append(".").append(tableName);
        query.append(" WHERE ");

        Map<Column, Object> vs = row.getKey().getValues();
        BiMap<Column, Column> mapping = foreignKey.getColumnMapping();

        List<String> parts = new ArrayList<>(vs.size());
        List<Object> params = new ArrayList<>(vs.size());
        vs.forEach((pkColumn, value) -> {
            Column fkColumn = mapping.get(pkColumn);
            parts.add(fkColumn.getName() + " = ? ");
            params.add(value);
        });

        query.append(Joiner.on(" AND ").join(parts));
        try {
            return runner.query(query.toString(), new ScalarHandler<>(), params.toArray());
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public Iterable<Row> backReferences(Row pkRow, ForeignKey fk) {
        return new RowIterable(runner, pkRow, fk);
    }

    public List<Row> selectRows(List<ObjectKey> keys) {
        List<Row> result = new ArrayList<>(keys.size());
        for (ObjectKey k: keys) {
            result.add(new Row(this, k));
        }
        return result;
    }

    private final class RowIterable implements Iterable<Row> {
        private final List<Object> params;
        private final String query;
        private final DataSource ds;
        private final Table fkTable;

        public RowIterable(QueryRunner runner, Row pkRow, ForeignKey fk) {
            StringBuilder queryBuilder = new StringBuilder("SELECT ");

            Table pkTable = fk.getPkTable();
            Table fkTable = fk.getFromTable();

            params = new ArrayList<>();
            List<String> expressions = new ArrayList<>();
            fk.getColumnMapping().forEach((pkColumn, fkColumn) -> {
                expressions.add(fkColumn.getName() + " = ?");
                params.add(pkRow.get(pkColumn));
            });

            Set<String> pkColumns = fkTable.getPrimaryKey().get().getColumnNames();
            queryBuilder.append(Joiner.on(", ").join(pkColumns));
            queryBuilder.append(" FROM ").append(fkTable.getName());
            queryBuilder.append(" WHERE ").append(Joiner.on(" AND ").join(expressions));

            this.fkTable = fkTable;
            this.ds = runner.getDataSource();
            this.query = queryBuilder.toString();
        }

        @Override
        public Iterator<Row> iterator() {
            Connection conn = null;
            try {
                conn = ds.getConnection();
                PreparedStatement st = conn.prepareStatement(query);
                for (int i = 0; i < params.size(); i++) {
                    st.setObject(i + 1, params.get(i));
                }
                return new RowIterator(fkTable, st.executeQuery(), conn, st);
            } catch (SQLException ex) {
                if (conn != null) {
                    try {
                        conn.close();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
                throw new RuntimeException(ex);
            }
        }
    }

    private final class RowIterator implements Iterator<Row>, Closeable {
        private final ResultSet rs;
        private final Set<String> pkColumns;
        private final Table pkTable;
        private final Connection conn;
        private final Statement st;
        private boolean hasNext;

        private RowIterator(Table pkTable, ResultSet rs, Connection connection, Statement st) {
            this.conn = connection;
            this.st = st;
            this.rs = rs;
            this.pkColumns = pkTable.getPrimaryKey().get().getColumnNames();
            this.pkTable = pkTable;
            try {
                hasNext = rs.next();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public boolean hasNext() {
            return hasNext;
        }

        @Override
        public Row next() {
            Map<Column, Object> vs = new HashMap<>();
            pkColumns.forEach(name -> {
                try {
                    vs.put(pkTable.getColumn(name), rs.getString(name));
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
            ObjectKey key = new ObjectKey(pkTable, vs);
            try {
                hasNext = rs.next();
            } catch (SQLException e) {
                close();
                throw new RuntimeException(e);
            }
            if (!hasNext) {
                close();
            }
            return new Row(RowContext.this, key);
        }

        @Override
        public void close() {
            // TODO proper closing
            try {
                st.close();
                rs.close();
                conn.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
