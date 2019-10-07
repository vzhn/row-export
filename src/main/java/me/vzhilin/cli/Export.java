package me.vzhilin.cli;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.google.common.base.Joiner;

import me.vzhilin.db.Row;
import me.vzhilin.schema.Table;

public class Export {
    private final SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    public void export(List<Row> rows, PrintStream out) {
        StringBuilder sb = new StringBuilder();
        for (Row row: rows) {
            Table table = row.getTable();
            Set<String> columns = table.getColumns().keySet();

            sb.append("INSERT INTO ");
            sb.append(table.getSchemaName()).append(".").append(table.getName());
            List<String> cols = new ArrayList<>();
            List<Object> vs = new ArrayList<>();
            columns.forEach(columnName -> {
                Object value = row.get(table.getColumn(columnName));
                if (value != null) {
                    String s = Export.this.toString(value);
                    cols.add(columnName);
                    vs.add(s);
                }
            });

            sb.append("(").append(Joiner.on(',').join(cols)).append(") VALUES (");
            sb.append(Joiner.on(',').join(vs));
            sb.append(");");

            out.println(sb);
            sb.setLength(0);
        }
    }

    private String toString(Object value) {
        String s;
        if (value == null) {
            return null;
        } else
        if (value instanceof String) {
            s = "'" + value + "'";
        } else
        if (value instanceof Number) {
            s = String.valueOf(value);
        } else
        if (value instanceof Date) {
            s = "to_date('" + format.format(value) + "','DD.MM.YYYY HH24:MI:SS')";
        } else {
            throw new AssertionError();
        }
        return s;
    }
}
