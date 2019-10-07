package me.vzhilin.db;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbutils.QueryRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import me.vzhilin.schema.Column;
import me.vzhilin.schema.ForeignKey;
import me.vzhilin.schema.PrimaryKey;
import me.vzhilin.schema.Schema;
import me.vzhilin.schema.SchemaLoader;
import me.vzhilin.schema.Table;
import me.vzhilin.util.BiMap;

public final class SchemaTests {
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
    public void foreignKeys() throws SQLException {
        runner.update("CREATE TABLE A ( " +
                "pk_a_1 INTEGER, " +
                "pk_a_2 TEXT, " +
                "text_a TEXT, " +
                "PRIMARY KEY (pk_a_1, pk_a_2))");

        runner.update("CREATE TABLE B ( " +
                "fk_b_1 INTEGER, " +
                "fk_b_2 TEXT, " +
                "FOREIGN KEY (fk_b_1, fk_b_2) REFERENCES A(pk_a_1, pk_a_2))");

        Schema schema = new SchemaLoader().load(ds, null);
        Table tableA = schema.getTable("A");
        Table tableB = schema.getTable("B");
        Optional<PrimaryKey> maybeAPk = tableA.getPrimaryKey();

        assertTrue(maybeAPk.isPresent());
        PrimaryKey apk = maybeAPk.get();
        Column columnPkA1 = tableA.getColumn("pk_a_1");
        Column columnPkA2 = tableA.getColumn("pk_a_2");
        Column columnTextA = tableA.getColumn("text_a");

        Column columnB1 = tableB.getColumn("fk_b_1");
        Column columnB2 = tableB.getColumn("fk_b_2");
        assertThat(columnPkA1.getDataType(), equalTo("INTEGER"));
        assertThat(columnPkA2.getDataType(), equalTo("TEXT"));
        assertThat(columnTextA.getDataType(), equalTo("TEXT"));
        assertThat(apk.getColumns(), equalTo(Sets.newHashSet(columnPkA1, columnPkA2)));

        BiMap<Column, Column> columnMapping = new BiMap<>();
        columnMapping.put(columnPkA1, columnB1);
        columnMapping.put(columnPkA2, columnB2);

        ForeignKey bForeignKey = Iterables.getOnlyElement(apk.getForeignKeys());
        assertThat(bForeignKey.getColumnMapping(), equalTo(columnMapping));
        assertFalse(tableB.getPrimaryKey().isPresent());
    }

    @Test
    public void multipleForeignKeys() throws SQLException {
        runner.update("CREATE TABLE A (pk_a INTEGER, PRIMARY KEY(pk_a))");
        runner.update("CREATE TABLE B ( " +
                "fk_b_1 INTEGER, " +
                "fk_b_2 INTEGER, " +
                "fk_b_3 INTEGER, " +
                "CONSTRAINT fk1 FOREIGN KEY (fk_b_1) REFERENCES A(pk_a) " +
                "CONSTRAINT fk2 FOREIGN KEY (fk_b_2) REFERENCES A(pk_a) " +
                "CONSTRAINT fk3 FOREIGN KEY (fk_b_3) REFERENCES A(pk_a)) ");

        Schema schema = new SchemaLoader().load(ds, null);
        Table tableA = schema.getTable("A");
        Table tableB = schema.getTable("B");

        PrimaryKey apk = tableA.getPrimaryKey().get();
        Set<ForeignKey> fks = apk.getForeignKeys();
        assertThat(fks, hasSize(3));
    }
}
