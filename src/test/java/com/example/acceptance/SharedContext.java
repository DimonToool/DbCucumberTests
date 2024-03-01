package com.example.acceptance;

import io.micronaut.context.ApplicationContext;
import io.micronaut.runtime.Micronaut;

public class SharedContext {

    private static ApplicationContext context;

    public static ApplicationContext getContext() {
        if (context == null) {
            context = Micronaut.run(SharedContext.class);
        }
        return context;
    }
}