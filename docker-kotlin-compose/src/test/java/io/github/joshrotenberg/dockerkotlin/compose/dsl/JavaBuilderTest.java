package io.github.joshrotenberg.dockerkotlin.compose.dsl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Java builder API.
 */
class JavaBuilderTest {

    @Test
    void simpleServiceGeneratesCorrectYaml() {
        ComposeSpec compose = ComposeBuilder.create()
            .service("web", web -> web
                .image("nginx:alpine")
                .ports("8080:80"))
            .build();

        String yaml = compose.toYaml();

        assertTrue(yaml.contains("services:"));
        assertTrue(yaml.contains("web:"));
        assertTrue(yaml.contains("image: nginx:alpine"));
    }

    @Test
    void multipleServicesWithDependencies() {
        ComposeSpec compose = ComposeBuilder.create()
            .name("myapp")
            .service("web", web -> web
                .image("nginx:alpine")
                .ports("8080:80")
                .dependsOn("api", "redis"))
            .service("api", api -> api
                .image("myapp/api:latest")
                .environment("DATABASE_URL", "postgres://db:5432/app")
                .environment("REDIS_URL", "redis://redis:6379")
                .dependsOn("db", "redis"))
            .service("db", db -> db
                .image("postgres:16")
                .environment("POSTGRES_PASSWORD", "secret")
                .volume("pgdata", "/var/lib/postgresql/data"))
            .service("redis", redis -> redis
                .image("redis:7-alpine"))
            .volume("pgdata")
            .build();

        String yaml = compose.toYaml();

        assertTrue(yaml.contains("name: myapp"));
        assertTrue(yaml.contains("web:"));
        assertTrue(yaml.contains("api:"));
        assertTrue(yaml.contains("db:"));
        assertTrue(yaml.contains("redis:"));
        assertTrue(yaml.contains("volumes:"));
        assertTrue(yaml.contains("pgdata:"));
    }

    @Test
    void serviceWithHealthcheck() {
        ComposeSpec compose = ComposeBuilder.create()
            .service("web", web -> web
                .image("nginx:alpine")
                .healthcheck(hc -> hc
                    .testShell("curl -f http://localhost/ || exit 1")
                    .interval("30s")
                    .timeout("10s")
                    .retries(3)
                    .startPeriod("5s")))
            .build();

        String yaml = compose.toYaml();

        assertTrue(yaml.contains("healthcheck:"));
        assertTrue(yaml.contains("interval: 30s"));
        assertTrue(yaml.contains("timeout: 10s"));
        assertTrue(yaml.contains("retries: 3"));
    }

    @Test
    void serviceWithLogging() {
        ComposeSpec compose = ComposeBuilder.create()
            .service("app", app -> app
                .image("myapp:latest")
                .logging("json-file", log -> log
                    .maxSize("10m")
                    .maxFile(3)))
            .build();

        String yaml = compose.toYaml();

        assertTrue(yaml.contains("logging:"));
        assertTrue(yaml.contains("driver: json-file"));
    }

    @Test
    void serviceWithDeploy() {
        ComposeSpec compose = ComposeBuilder.create()
            .service("web", web -> web
                .image("nginx:alpine")
                .deploy(deploy -> deploy
                    .replicas(3)
                    .resources(res -> res
                        .cpus("0.5")
                        .memory("512M"))))
            .build();

        String yaml = compose.toYaml();

        assertTrue(yaml.contains("deploy:"));
        assertTrue(yaml.contains("replicas: 3"));
    }

    @Test
    void networksAndVolumes() {
        ComposeSpec compose = ComposeBuilder.create()
            .service("web", web -> web
                .image("nginx:alpine")
                .networks("frontend", "backend"))
            .network("frontend", net -> net
                .driver("bridge"))
            .network("backend", net -> net
                .driver("bridge")
                .internal(true))
            .volume("data", vol -> vol
                .driver("local"))
            .build();

        String yaml = compose.toYaml();

        assertTrue(yaml.contains("networks:"));
        assertTrue(yaml.contains("frontend:"));
        assertTrue(yaml.contains("backend:"));
        assertTrue(yaml.contains("volumes:"));
        assertTrue(yaml.contains("data:"));
    }

    @Test
    void fullExample() {
        ComposeSpec compose = ComposeBuilder.create()
            .name("production-stack")
            .service("nginx", nginx -> nginx
                .image("nginx:alpine")
                .containerName("prod-nginx")
                .ports("80:80", "443:443")
                .volumes("./nginx.conf:/etc/nginx/nginx.conf:ro")
                .networks("frontend")
                .dependsOn("api")
                .restart("unless-stopped")
                .healthcheck(hc -> hc
                    .testShell("curl -f http://localhost/ || exit 1")
                    .interval("30s")
                    .timeout("5s")
                    .retries(3)))
            .service("api", api -> api
                .image("myapp/api:v1.2.3")
                .environment("NODE_ENV", "production")
                .environment("PORT", "3000")
                .networks("frontend", "backend")
                .dependsOn("postgres", "redis")
                .deploy(deploy -> deploy
                    .replicas(2)
                    .resources(res -> res
                        .cpus("1")
                        .memory("1G")))
                .logging("json-file", log -> log
                    .maxSize("50m")
                    .maxFile(5)))
            .service("postgres", pg -> pg
                .image("postgres:16-alpine")
                .environment("POSTGRES_USER", "app")
                .environment("POSTGRES_PASSWORD", "secret")
                .environment("POSTGRES_DB", "appdb")
                .volume("pgdata", "/var/lib/postgresql/data")
                .networks("backend")
                .healthcheck(hc -> hc
                    .testCmd("pg_isready", "-U", "app")
                    .interval("10s")
                    .retries(5)))
            .service("redis", redis -> redis
                .image("redis:7-alpine")
                .networks("backend"))
            .network("frontend", net -> net.driver("bridge"))
            .network("backend", net -> net
                .driver("bridge")
                .internal(true))
            .volume("pgdata", vol -> vol.driver("local"))
            .build();

        String yaml = compose.toYaml();

        // Verify structure
        assertTrue(yaml.contains("name: production-stack"));
        assertTrue(yaml.contains("services:"));
        assertTrue(yaml.contains("nginx:"));
        assertTrue(yaml.contains("api:"));
        assertTrue(yaml.contains("postgres:"));
        assertTrue(yaml.contains("redis:"));
        assertTrue(yaml.contains("networks:"));
        assertTrue(yaml.contains("volumes:"));

        // Print for visual inspection
        System.out.println(yaml);
    }
}
