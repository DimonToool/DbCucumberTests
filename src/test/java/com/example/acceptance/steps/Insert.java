package com.example.acceptance.steps;

import com.example.acceptance.DbOperations;
import com.example.acceptance.SharedContext;
import com.example.dwh.DatabaseQueryExecutor;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.When;
import io.micronaut.context.ApplicationContext;
import lombok.extern.log4j.Log4j2;

import java.sql.SQLException;

@Log4j2
public class Insert {

    private final DatabaseQueryExecutor databaseQueryExecutor;

    private final DbOperations dbOperations;

    public Insert() {
        ApplicationContext applicationContext = SharedContext.getContext();
        this.databaseQueryExecutor = applicationContext.getBean(DatabaseQueryExecutor.class);
        this.dbOperations = new DbOperations();
    }

    public void insert(String table, DataTable insertValues) {
        log.info("Inserting in table %s values: %s".formatted(table, insertValues));
        var columns = insertValues.asLists().stream().findFirst().orElseThrow();
        insertValues.asLists().stream().skip(1).forEach(row -> {
            try {
                var sql = "INSERT INTO %s (%s) VALUES (%s)"
                        .formatted(table, String.join(",", columns), String.join(",", row));
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
