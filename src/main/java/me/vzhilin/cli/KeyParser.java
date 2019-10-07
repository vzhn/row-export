package me.vzhilin.cli;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Iterables;

import me.vzhilin.db.ObjectKey;
import me.vzhilin.schema.Column;
import me.vzhilin.schema.PrimaryKey;
import me.vzhilin.schema.Schema;
import me.vzhilin.schema.Table;

public class KeyParser {
    public List<ObjectKey> parse(Schema schema, String[] es) {
        List<ObjectKey> result = new ArrayList<>();
        for (String e: es) {
            String[] parts = e.trim().split(" ");
            String tableName = parts[0];
            Table table = schema.getTable(tableName);
            PrimaryKey pk = table.getPrimaryKey().get();

            for (int i = 1; i < parts.length; i++) {
                String p = parts[i];
                Map<Column, Object> values = new HashMap<>();
                if (p.indexOf(':') != -1) {
                    int pos = p.indexOf(':');
                    String keyColumn = p.substring(0, pos);
                    String keyValue = p.substring(pos + 1);

                    Column column = table.getColumn(keyColumn);
                    values.put(column, keyValue);
                } else {
                    Column column = Iterables.getOnlyElement(pk.getColumns());
                    values.put(column, p);
                }

                result.add(new ObjectKey(table, values));
            }
        }
        return result;
    }

}
