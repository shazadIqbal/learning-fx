package com.mls.learningfx.config;

import org.flywaydb.core.Flyway;

public class DatabaseMigration {

    public static void migrate() {

        Flyway flyway = Flyway.configure()
                .dataSource(
                        "jdbc:postgresql://localhost:5432/postgres",
                        "postgres",
                        "root"
                )
                .locations("classpath:db/migration")
                .load();

        flyway.baseline();

        flyway.migrate();
    }
}
