package me.vzhilin;

import me.vzhilin.schema.Schema;
import me.vzhilin.schema.SchemaLoader;
import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.SQLException;
import java.util.Locale;

public class RowExport {
    public static void main(String... argv) throws SQLException {
        new RowExport().start();
    }

    private void start() throws SQLException {
        Locale.setDefault(Locale.US);
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName("oracle.jdbc.OracleDriver");
        ds.setUrl("jdbc:oracle:thin:@localhost:1521:XE");
        ds.setUsername("voshod");
        ds.setPassword("voshod");
        Schema schema = new SchemaLoader().load(ds, null);
    }
}
