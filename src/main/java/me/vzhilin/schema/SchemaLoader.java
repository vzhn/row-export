package me.vzhilin.schema;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.sql.DataSource;

import me.vzhilin.util.BiMap;

public final class SchemaLoader {
    public Schema load(DataSource ds, String schemaName) throws SQLException {
        Schema schema = new Schema(schemaName);
        Connection conn = ds.getConnection();
        DatabaseMetaData metadata = conn.getMetaData();
        loadTables(schema, metadata);
        conn.close();
        return schema;
    }

    private void loadTables(Schema schema, DatabaseMetaData metadata) throws SQLException {
        String schemaName = schema.getName();
        ResultSet tables = metadata.getTables(null, schemaName, null, new String[]{"TABLE"});
        while (tables.next()) {
            schema.addTable(tables.getString("TABLE_NAME"));
        }
        tables.close();

        ResultSet columns = metadata.getColumns(null, schemaName, null, null);
        while (columns.next()) {
            String tableName = columns.getString("TABLE_NAME");
            String columnName = columns.getString("COLUMN_NAME");
            String columnType = columns.getString("TYPE_NAME");
            Table table = schema.getTable(tableName);
            if (table != null) {
                table.addColumn(columnName, columnType);
            }
        }
        columns.close();

        for (String tableName: schema.getTableNames()) {
            Table table = schema.getTable(tableName);
            ResultSet primaryKeys = metadata.getPrimaryKeys(null, schemaName, tableName);
            PrimaryKey pk = null;
            while (primaryKeys.next()) {
                String columnName = primaryKeys.getString("COLUMN_NAME");
                int keySeq = primaryKeys.getInt("KEY_SEQ");
                if (pk == null) {
                    String pkName = primaryKeys.getString("PK_NAME");
                    pk = new PrimaryKey(Optional.ofNullable(pkName), table);
                }
                pk.addColumn(table.getColumn(columnName), keySeq);
            }
            table.setPk(pk);
            primaryKeys.close();
        }

        for (String tableName: schema.getTableNames()) {
            Table table = schema.getTable(tableName);
            Optional<PrimaryKey> maybePk = table.getPrimaryKey();
            if (maybePk.isPresent()) {
                PrimaryKey pk = maybePk.get();
                ResultSet keys = metadata.getExportedKeys(null, schemaName, tableName);

                // fkTable, fkName, pkColumn -> fkColumn
                Map<Table, Map<String, BiMap<Column, Column>>> columnMapping = new HashMap<>();

                while (keys.next()) {
                    String pkColumnName = keys.getString("PKCOLUMN_NAME");
                    String fkTableName = keys.getString("FKTABLE_NAME");
                    String fkColumnName = keys.getString("FKCOLUMN_NAME");
                    String fkName = keys.getString("FK_NAME");
                    Column pkColumn = table.getColumn(pkColumnName);
                    Table fkTable = schema.getTable(fkTableName);
                    Column fkColumn = fkTable.getColumn(fkColumnName);
                    columnMapping.
                        computeIfAbsent(fkTable, t -> new HashMap<>()).
                        computeIfAbsent(fkName, t -> new BiMap<>()).
                        put(pkColumn, fkColumn);
                }
                keys.close();

                columnMapping.forEach((fkTable, fkNameToColumns) ->
                    fkNameToColumns.forEach((fkName, cols) -> {
                        ForeignKey foreignKey = fkTable.addForeignKey(fkName, table, cols);
                        pk.addForeignKey(foreignKey);
                    }));
            }
        }
    }
}
