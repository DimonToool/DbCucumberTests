package com.example.acceptance;

import com.example.dwh.DatabaseQueryExecutor;
import lombok.extern.log4j.Log4j2;

import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.function.Consumer;

@Log4j2
public class DbOperations {

    public void executeDbUpdate(Consumer<DatabaseQueryExecutor> updateFunction,
                              DatabaseQueryExecutor databaseQueryExecutor) throws SQLException {
        Connection connection = databaseQueryExecutor.getDatabaseConnection().getConnection();
        try {
            updateFunction.accept(databaseQueryExecutor);
        } catch (Exception e) {
            log.error("An error occurred, rolling back transaction", e);
            if (connection != null) {
                try {
                    connection.rollback(); // rollback transaction if any operation fails
                } catch (SQLException sqlException) {
                    log.error("Failed to rollback transaction", sqlException);
                }
            }
            throw new RuntimeException(e);
        }
    }

    public String getStringValueFromResultSet(ResultSet resultSet, String columnName) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnIndex = resultSet.findColumn(columnName);
        int columnType = metaData.getColumnType(columnIndex);

        return switch (columnType) {
            case Types.INTEGER -> String.valueOf(resultSet.getInt(columnName));
            case Types.BOOLEAN -> String.valueOf(resultSet.getBoolean(columnName));
            case Types.DOUBLE -> String.valueOf(resultSet.getDouble(columnName));
            case Types.DATE -> resultSet.getDate(columnName).toString();
            case Types.TIMESTAMP -> resultSet.getTimestamp(columnName).toString();
            case Types.TIME -> resultSet.getTime(columnName).toString();
            case Types.BIGINT -> String.valueOf(resultSet.getLong(columnName));
            case Types.NVARCHAR, Types.NCHAR, Types.LONGNVARCHAR, Types.LONGVARCHAR -> resultSet.getNString(columnName);
            case Types.NCLOB, Types.CLOB -> resultSet.getClob(columnName).toString();
            case Types.BLOB -> resultSet.getBlob(columnName).toString();
            case Types.OTHER -> resultSet.getObject(columnName).toString();
            case Types.NULL -> null;
            case Types.ARRAY, Types.DISTINCT, Types.STRUCT,
                    Types.REF, Types.JAVA_OBJECT, Types.ROWID,
                    Types.SQLXML, Types.REF_CURSOR -> throw new SQLException("Unsupported column type");
            case Types.DECIMAL, Types.NUMERIC -> resultSet.getBigDecimal(columnName).toString();
            case Types.SMALLINT -> String.valueOf(resultSet.getShort(columnName));
            case Types.TINYINT -> String.valueOf(resultSet.getByte(columnName));
            case Types.REAL -> String.valueOf(resultSet.getFloat(columnName));
            case Types.BINARY, Types.VARBINARY, Types.LONGVARBINARY -> new String(resultSet.getBytes(columnName), StandardCharsets.UTF_8);
            default -> resultSet.getString(columnName);
        };
    }
}
