package com.example.dwh;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

import java.sql.*;
import java.util.concurrent.CompletableFuture;

@Singleton
@Data
@Log4j2
public class DatabaseQueryExecutor {

    @Inject
    private DatabaseConnection databaseConnection;

    public DatabaseQueryExecutor(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    public ResultSet executeQuery(String sql) throws SQLException {
        Connection connection = databaseConnection.getConnection();
        Statement statement = connection.createStatement();
        return statement.executeQuery(sql);
    }

    public void executeUpdate(String sql) throws SQLException {
        Connection connection = databaseConnection.getConnection();
        Statement statement = connection.createStatement();
        statement.executeUpdate(sql);
    }

    public void executeUpdateAsync(String sql) throws SQLException {
        CompletableFuture.runAsync(() -> {
            try (Connection connection = databaseConnection.getConnection();
                 Statement statement = connection.createStatement()) {
                statement.executeUpdate(sql);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }).exceptionally(e -> {
            log.error("Error executing update", e);
            return null;
        });
    }

    public void executeProcedure(String procedureCall, Object... params) throws SQLException {
        try (Connection connection = databaseConnection.getConnection();
             CallableStatement callableStatement = connection.prepareCall(procedureCall)) {

            // Set parameters
            for (int i = 0; i < params.length; i++) {
                callableStatement.setObject(i + 1, params[i]);
            }

            // Execute procedure
            callableStatement.execute();
        } catch (SQLException e) {
            log.error("Error executing procedure", e);
            throw e;
        }
    }

    public void executeProcedure(String procedureName) throws SQLException {
        try (Connection connection = databaseConnection.getConnection();
             CallableStatement callableStatement = connection.prepareCall("call %s".formatted(procedureName))) {
            callableStatement.execute();
        } catch (SQLException e) {
            log.error("Error executing procedure", e);
        }
    }
}
