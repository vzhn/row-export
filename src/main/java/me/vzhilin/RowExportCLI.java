package me.vzhilin;

import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import me.vzhilin.cli.Export;
import me.vzhilin.cli.Fetch;
import me.vzhilin.cli.KeyParser;
import me.vzhilin.db.ObjectKey;
import me.vzhilin.db.Row;
import me.vzhilin.db.RowContext;
import me.vzhilin.schema.Schema;
import me.vzhilin.schema.SchemaLoader;

public class RowExportCLI {
    public static void main(String... argv) throws SQLException, ParseException {
        Options options = new Options();
        options.addOption("h", "help", false, "show help and exit");
        options.addRequiredOption("u", "url", true, "jdbc connection url");
        options.addOption("l", "login", true, "jdbc connection login");
        options.addOption("p", "password", true, "jdbc connection password");

        Option expressionOpt = new Option("e", "expression", true, "expression");
        expressionOpt.setArgs(Option.UNLIMITED_VALUES);
        expressionOpt.setValueSeparator(';');
        options.addOption(expressionOpt);

        CommandLine cmd;
        try {
            cmd = new DefaultParser().parse(options, argv);
        } catch (MissingOptionException ex) {
            HelpFormatter helpFormatter = new HelpFormatter();
            helpFormatter.printHelp("row-export [options]", options);
            return;
        }

        if (cmd.hasOption('h')) {
            HelpFormatter helpFormatter = new HelpFormatter();
            helpFormatter.printHelp("row-export [options]", options);
        } else {
            RowExportCLI instance = new RowExportCLI();
            instance.start(cmd);
        }
    }

    private BasicDataSource prepareDatasource(CommandLine cmd) {
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName("oracle.jdbc.OracleDriver");
        ds.setUrl(cmd.getOptionValue("url"));
        ds.setUsername(cmd.getOptionValue("login"));
        ds.setPassword(cmd.getOptionValue("password"));
        return ds;
    }

    private void start(CommandLine cmd) throws SQLException {
        KeyParser keyParser = new KeyParser();
        Fetch fetch = new Fetch();
        Export export = new Export();
        SchemaLoader schemaLoader = new SchemaLoader();

        Locale.setDefault(Locale.US);
        BasicDataSource ds = prepareDatasource(cmd);
        QueryRunner runner = new QueryRunner(ds);
        String currentSchema = runner.query("select sys_context('userenv', 'current_schema') from dual", new ScalarHandler<>());
        Schema schema = schemaLoader.load(ds, currentSchema);
        RowContext rowContext = new RowContext(runner, schema);
        List<ObjectKey> keys = keyParser.parse(schema, cmd.getOptionValues('e'));
        List<Row> rs = rowContext.selectRows(keys);
        export.export(fetch.fetch(rs), System.out);
    }
}
