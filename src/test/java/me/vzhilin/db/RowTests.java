package me.vzhilin.db;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbutils.QueryRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import me.vzhilin.schema.Column;
import me.vzhilin.schema.ForeignKey;
import me.vzhilin.schema.Schema;
import me.vzhilin.schema.SchemaLoader;
import me.vzhilin.schema.Table;

public class RowTests {
    private QueryRunner runner;
    private BasicDataSource ds;

    @Before
    public void setUp() {
        ds = new BasicDataSource();
        ds.setDriverClassName("org.sqlite.JDBC");
        ds.setUrl("jdbc:sqlite::memory:");
        runner = new QueryRunner(ds);
    }

    @After
    public void tearDown() throws SQLException {
        ds.close();
    }

    @Test
    public void rowTests() throws SQLException {
        runner.update("CREATE TABLE A ( " +
                "pk_a_1 INTEGER, " +
                "pk_a_2 TEXT, " +
                "text_a TEXT, " +
                "PRIMARY KEY (pk_a_1, pk_a_2))");

        runner.update("CREATE TABLE B ( " +
                "pk_b INTEGER, " +
                "fk_b_1 INTEGER, " +
                "fk_b_2 TEXT, " +
                "PRIMARY KEY (pk_b) " +
                "FOREIGN KEY (fk_b_1, fk_b_2) REFERENCES A(pk_a_1, pk_a_2))");

        runner.update("INSERT INTO A(pk_a_1, pk_a_2, text_a) VALUES (100, 200, '100_200')");
        runner.update("INSERT INTO A(pk_a_1, pk_a_2, text_a) VALUES (101, 201, '101_201')");
        runner.update("INSERT INTO A(pk_a_1, pk_a_2, text_a) VALUES (102, 202, '102_202')");

        runner.update("INSERT INTO B(pk_b, fk_b_1, fk_b_2) VALUES (300, 100, 200)");
        runner.update("INSERT INTO B(pk_b, fk_b_1, fk_b_2) VALUES (301, 100, 200)");
        runner.update("INSERT INTO B(pk_b, fk_b_1, fk_b_2) VALUES (302, 100, 200)");

        Schema schema = new SchemaLoader().load(ds, null);
        Table tableA = schema.getTable("A");
        Table tableB = schema.getTable("B");
        Column a1 = tableA.getColumn("pk_a_1");
        Column a2 = tableA.getColumn("pk_a_2");
        Column a3 = tableA.getColumn("text_a");

        Column b1 = tableB.getColumn("fk_b_1");
        Column b2 = tableB.getColumn("fk_b_2");

        RowContext ctx = new RowContext(runner, schema);
        Map<Column, Object> values = new HashMap<>();
        values.put(a1, 100);
        values.put(a2, 200);

        Row aRow = new Row(ctx, new ObjectKey(tableA, values));
        assertThat(aRow.get(a3), equalTo("100_200"));

        ForeignKey foreignKey = Iterables.getOnlyElement(tableB.getForeignKeys().values());
        assertThat(aRow.backwardReferencesCount(), equalTo(Collections.singletonMap(foreignKey, 3)));

        List<Row> refs = Lists.newArrayList(aRow.backwardReference(foreignKey));
        assertThat(refs, hasSize(3));
        for (Row v: refs) {
            assertThat(v.get(b1), equalTo(100));
            assertThat(v.get(b2), equalTo("200"));
        }
    }
}
