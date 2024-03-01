package com.example.acceptance.steps;

import com.example.acceptance.DbOperations;
import com.example.acceptance.SharedContext;
import com.example.dwh.DatabaseQueryExecutor;
import io.cucumber.java.en.When;
import io.micronaut.context.ApplicationContext;
import lombok.extern.log4j.Log4j2;

import java.sql.SQLException;

@Log4j2
public class ExecuteDdl {

    private final DatabaseQueryExecutor databaseQueryExecutor;

    private final DbOperations dbOperations;

    public ExecuteDdl () {
        ApplicationContext applicationContext = SharedContext.getContext();
        this.databaseQueryExecutor = applicationContext.getBean(DatabaseQueryExecutor.class);
        this.dbOperations = new DbOperations();
    }

    public void executeDdl(String ddl) {
        log.info("Executing DDL: {}", ddl);
        try {
            databaseQueryExecutor.executeUpdate(ddl);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @When("Execute DDL {string}")
    public void executeDbDdl(String ddl) throws SQLException {
        dbOperations.executeDbUpdate(executor -> executeDdl(ddl), databaseQueryExecutor);
        databaseQueryExecutor.getDatabaseConnection().getConnection().commit();
    }
}
