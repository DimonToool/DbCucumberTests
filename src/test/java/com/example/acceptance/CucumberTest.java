package com.example.acceptance;

import com.example.dwh.DatabaseConnection;
import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import io.micronaut.context.ApplicationContext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.runner.RunWith;

import java.sql.Connection;
import java.sql.SQLException;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features",
        glue = "com.example.acceptance.steps",
        snippets = CucumberOptions.SnippetType.CAMELCASE,
        plugin = {"pretty", "html:build/reports/cucumber"}
)
public class CucumberTest {

    private final DatabaseConnection databaseConnection;
    private static Connection connection;

    public CucumberTest() {
        ApplicationContext applicationContext = ApplicationContext.run();
        this.databaseConnection = applicationContext.getBean(DatabaseConnection.class);
    }

    @BeforeAll
    public void setUp() throws SQLException {
        connection = databaseConnection.getConnection();
        connection.setAutoCommit(false);
    }

    @AfterAll
    public void tearDown() throws SQLException {
        connection.rollback();
        connection.close();
    }
}
