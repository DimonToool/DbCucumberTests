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
public class ExecuteProcedure {

    private final DatabaseQueryExecutor databaseQueryExecutor;

    private final DbOperations dbOperations;

    public ExecuteProcedure() {
        ApplicationContext applicationContext = SharedContext.getContext();
        this.databaseQueryExecutor = applicationContext.getBean(DatabaseQueryExecutor.class);
        this.dbOperations = new DbOperations();
    }

    public void executeProcedure(String procedureName, DataTable parameters) {
        log.info("Executing db procedure %s with parameters: %s".formatted(procedureName, parameters));
        try {
            databaseQueryExecutor.executeProcedure(procedureName, parameters.asMap());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void executeProcedure(String procedureName) {
        log.info("Executing db procedure %s".formatted(procedureName));
        try {
            databaseQueryExecutor.executeProcedure(procedureName);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @When("Execute {string} procedure with parameters")
    public void executeDbProcedure(String procedureName, DataTable parameters) throws SQLException {
        dbOperations.executeDbUpdate(executor -> executeProcedure(procedureName, parameters), databaseQueryExecutor);
    }

    @When("Execute {string} procedure")
    public void executeDbProcedure(String procedureName) throws SQLException {
        dbOperations.executeDbUpdate(executor -> executeProcedure(procedureName), databaseQueryExecutor);
    }
}
