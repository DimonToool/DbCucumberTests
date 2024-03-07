package com.example.acceptance;

import com.example.dwh.DatabaseConnection;
import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import io.micronaut.context.ApplicationContext;
import io.micronaut.transaction.SynchronousTransactionManager;
import io.micronaut.transaction.TransactionStatus;
import jakarta.inject.Inject;
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
        plugin = {"pretty", "junit:build/reports/Cucumber.xml"}
)
public class CucumberTest {

    private final DatabaseConnection databaseConnection;
    private static Connection connection;

    @Inject
    private SynchronousTransactionManager<Connection> transactionManager;

    private TransactionStatus<Connection> status;

    public CucumberTest() {
        ApplicationContext applicationContext = ApplicationContext.run();
        this.databaseConnection = applicationContext.getBean(DatabaseConnection.class);
        this.transactionManager = applicationContext.getBean(SynchronousTransactionManager.class);
    }

    @BeforeAll
    public void setUp() {
//        status = transactionManager.startTransaction();
//        connection = databaseConnection.getConnection();
//        connection.setAutoCommit(false);

        transactionManager.executeWrite(status -> {
            connection = databaseConnection.getConnection();
            connection.setAutoCommit(false);
            return status;
        });
    }

    @AfterAll
    public void tearDown() throws SQLException {
//        transactionManager.rollback(status);
//        connection.rollback();
//        connection.close();

        transactionManager.executeWrite(status -> {
            try {
                connection.rollback();
                connection.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return status;
        });
    }
}
