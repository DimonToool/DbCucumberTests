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
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log4j2
public class Update {

    private final DatabaseQueryExecutor databaseQueryExecutor;

    private final DbOperations dbOperations;

    private final ScenarioContext scenarioContext;

    public Update() {
        ApplicationContext applicationContext = SharedContext.getContext();
        this.databaseQueryExecutor = applicationContext.getBean(DatabaseQueryExecutor.class);
        this.dbOperations = new DbOperations();
        this.scenarioContext = applicationContext.getBean(ScenarioContext.class);
    }

    public void update(String table, DataTable values) {
        log.info("Updating table %s with values: %s".formatted(table, values));
        values.asMaps().forEach(row -> {
            var updateMap = new HashMap<String,String>(Map.of());
            var whereMap = new HashMap<String,String>(Map.of());
            row.forEach((k, v) -> {
                if (k.endsWith("=")) {
                    updateMap.put(k.replace("=", ""), v);
                } else {
                    whereMap.put(k, v);
                }
            });
            try {
                var sql = """
                        UPDATE %s
                        SET %s
                        WHERE %s
                        """
                        .formatted(table,
                                formatParams(updateMap, ","),
                                formatParams(whereMap, "AND"));
                log.info(sql);
                databaseQueryExecutor.executeUpdate(sql);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @When("Update table {string}")
    public void updateTable(String table, DataTable values) throws SQLException {
        dbOperations.executeDbUpdate(executor -> update(table.toUpperCase(), values), databaseQueryExecutor);
    }

    public String formatParams(Map<String, String> paramsMap, String delimiter) {
        var updateParams = paramsMap.entrySet().stream()
                .map(e -> "%s = %s".formatted(e.getKey(),
                        e.getValue().startsWith("<<")
                                ? scenarioContext.getContext(e.getValue().substring(2)).toString()
                                : e.getValue()))
                .toList();
        var stringJoinPattern = "%s %s %s".formatted("%s", delimiter, "%s");
        return updateParams.stream()
                .map(e -> {
                    if (e.contains("<<")) {
                        Pattern pattern = Pattern.compile("\\{(.*?)}");
                        Matcher matcher = pattern.matcher(e);
                        if (matcher.find()) {
                            return e.replace(matcher.group(0),
                                    scenarioContext.getContext(matcher.group(1)
                                            .replace("<<","")).toString());
                        }
                    } return e;
                })
                .reduce(stringJoinPattern::formatted).orElseThrow();
    }
}
