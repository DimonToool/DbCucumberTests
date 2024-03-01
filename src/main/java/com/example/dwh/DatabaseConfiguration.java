package com.example.dwh;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Factory;
import lombok.Data;



@Data
@ConfigurationProperties("datasources.default")
@Factory
public class DatabaseConfiguration {
    private String url;
    private String username;
    private String password;
    private String driverClassName;

}
