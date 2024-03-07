package com.example.acceptance.steps;

import com.example.acceptance.DbOperations;
import com.example.acceptance.ScenarioContext;
import com.example.acceptance.SharedContext;
import com.example.dwh.DatabaseQueryExecutor;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.When;
import io.micronaut.context.ApplicationContext;
import lombok.extern.log4j.Log4j2;

import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log4j2
public class Insert {

    private final DatabaseQueryExecutor databaseQueryExecutor;

    private final DbOperations dbOperations;

    private final ScenarioContext scenarioContext;

    public Insert() {
        ApplicationContext applicationContext = SharedContext.getContext();
        this.databaseQueryExecutor = applicationContext.getBean(DatabaseQueryExecutor.class);
        this.dbOperations = new DbOperations();
        this.scenarioContext = applicationContext.getBean(ScenarioContext.class);
    }

    public void insert(String table, DataTable insertValues) {
        log.info("Inserting in table %s values: %s".formatted(table, insertValues));
        var columns = insertValues.asLists().stream().findFirst().orElseThrow();
        insertValues.asLists().stream().skip(1).forEach(row -> {
            try {
                var formattedRow = row.stream()
                        .map(s -> {
                            if (s.startsWith("<<")) {
                                return "'%s'".formatted(scenarioContext.getContext(s.substring(2)).toString());
                            }
                            if (s.contains("<<")) {
                                Pattern pattern = Pattern.compile("\\{(.*?)}");
                                Matcher matcher = pattern.matcher(s);
                                if (matcher.find()) {
                                    return s.replace(matcher.group(0),
                                            scenarioContext.getContext(matcher.group(1)
                                                    .replace("<<","")).toString());
                                }
                            } return s;
                        })
                        .toList();
                var sql = "INSERT INTO %s (%s) VALUES (%s)"
                        .formatted(table, String.join(",", columns), String.join(",", formattedRow));
                log.info(sql);
                databaseQueryExecutor.executeUpdate(sql);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @When("Insert into table {string}")
    public void insertIntoDatabase(String table, DataTable insertValues) throws SQLException {
        dbOperations.executeDbUpdate(executor -> insert(table, insertValues), databaseQueryExecutor);
    }
}
