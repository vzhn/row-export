package me.vzhilin.schema;

import me.vzhilin.util.BiMap;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SchemaLoader {
    public Schema load(DataSource ds, String schemaName) throws SQLException {
        Schema schema = new Schema(schemaName);
        Connection conn = ds.getConnection();
        DatabaseMetaData metadata = conn.getMetaData();
        loadTables(schema, metadata);
        return schema;
    }

    private void loadTables(Schema schema, DatabaseMetaData metadata) throws SQLException {
        String schemaName = schema.getName();
        ResultSet tables = metadata.getTables(null, schemaName, null, new String[]{"TABLE"});
        while (tables.next()) {
            String tableSchema = tables.getString(2);
            String tableName = tables.getString(3);
            String tableType = tables.getString(4);
            Table table = schema.addTable(tableName);
        }

        ResultSet columns = metadata.getColumns(null, schemaName, null, null);
        while (columns.next()) {
            String tableName = columns.getString(3);
            String columnName = columns.getString(4);
            String columnType = columns.getString(6);

            Table table = schema.getTable(tableName);
            if (table != null) {
                table.addColumn(columnName, columnType);
            }
        }

        /**
         FKTABLE_NAME String => foreign key table name
         FKCOLUMN_NAME String => foreign key column nam
         */

        for (String tableName: schema.getTableNames()) {
            Table table = schema.getTable(tableName);
            ResultSet primaryKeys = metadata.getPrimaryKeys(null, schemaName, tableName);
            PrimaryKey pk = null;
            while (primaryKeys.next()) {
                String columnName = primaryKeys.getString(4);
                int keySeq = primaryKeys.getInt(5);
                if (pk == null) {
                    String pkName = primaryKeys.getString(6);
                    pk = new PrimaryKey(Optional.ofNullable(pkName), table);
                }
                pk.addColumn(table.getColumn(columnName), keySeq);
            }
            table.setPk(pk);

        }

        for (String tableName: schema.getTableNames()) {
            Table table = schema.getTable(tableName);
            Optional<PrimaryKey> maybePk = table.getPrimaryKey();
            if (maybePk.isPresent()) {
                PrimaryKey pk = maybePk.get();
                ResultSet keys = metadata.getExportedKeys(null, schemaName, tableName);

                // fkTable, fkName, columnMapping
                Map<Table, Map<String, BiMap<Column, Column>>> columnMapping = new HashMap<>();

                while (keys.next()) {
                    String pkTableSchema = keys.getString(2);
                    String pkTableName = keys.getString(3);
                    String pkColumnName = keys.getString(4);

                    String fkTableSchema = keys.getString(6);
                    String fkTableName = keys.getString(7);
                    String fkColumnName = keys.getString(8);
                    int keySeq = keys.getInt(9);
                    String fkName = keys.getString(12);
                    String pkName = keys.getString(13);

                    Column pkColumn = table.getColumn(pkColumnName);
                    Table fkTable = schema.getTable(fkTableName);

                    Column fkColumn = fkTable.getColumn(fkColumnName);
                    columnMapping.
                        computeIfAbsent(fkTable, t -> new HashMap<>()).
                        computeIfAbsent(fkName, t -> new BiMap<>()).
                        put(pkColumn, fkColumn);
                }

                columnMapping.forEach((fkTable, fkNameToColumns) ->
                    fkNameToColumns.forEach((fkName, cols) -> {
                        ForeignKey foreignKey = fkTable.addForeignKey(fkName, table, cols);
                        pk.addForeignKey(foreignKey);
                    }));
            }
        }
    }
}
