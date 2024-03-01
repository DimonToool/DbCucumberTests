package com.example.acceptance.steps;

import com.example.acceptance.DbOperations;
import com.example.acceptance.SharedContext;
import com.example.dwh.DatabaseQueryExecutor;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.micronaut.context.ApplicationContext;
import lombok.extern.log4j.Log4j2;

import java.sql.ResultSet;
import java.sql.SQLException;

@Log4j2
public class Query {

    private final DatabaseQueryExecutor databaseQueryExecutor;

    private static ResultSet result;

    private final DbOperations dbOperations;

    public Query() {
        ApplicationContext applicationContext = SharedContext.getContext();
        this.databaseQueryExecutor = applicationContext.getBean(DatabaseQueryExecutor.class);
        this.dbOperations = new DbOperations();
    }

    @Then("Query {string}")
    public void query(String sql, DataTable checkedValues) throws SQLException {
        log.info("Query for Dwh db : %s".formatted(sql));
        result = databaseQueryExecutor.executeQuery(sql);
        var columns = checkedValues.asLists().stream().findFirst().orElseThrow();
        checkedValues.asLists().stream().skip(1).forEach(row -> {
            try {
                if (result.next()) {
                    for (int i = 0; i < row.size(); i++) {
                        var value = dbOperations.getStringValueFromResultSet(result, columns.get(i));
                        var expected = row.get(i);
                        if (!value.equals(expected)) {
                            throw new RuntimeException("Expected %s but got %s".formatted(expected, value));
                        }
                    }
                } else {
                    throw new RuntimeException("No results found");
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Then("Query returns nothing {string}")
    public void checkEmptyQueryResult(String sql) throws SQLException {
        log.info("Query for Dwh db : %s".formatted(sql));
        result = databaseQueryExecutor.executeQuery(sql);
        if (result.next()) {
            throw new RuntimeException("Expected no results but got some");
        }
    }

    @Then("the result should contain")
    public void theResultShouldContain() {
        log.info("Result: %s".formatted(result));
    }
}
