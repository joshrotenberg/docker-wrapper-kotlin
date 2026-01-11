package io.github.joshrotenberg.dockerkotlin.compose.dsl

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ComposeDslTest {

    @Test
    fun `simple service generates correct YAML structure`() {
        val compose = dockerCompose {
            service("web") {
                image = "nginx:alpine"
                ports("8080:80")
            }
        }

        val map = compose.toMap()
        val services = map["services"] as Map<*, *>
        val web = services["web"] as Map<*, *>

        assertEquals("nginx:alpine", web["image"])
        assertEquals(listOf("8080:80"), web["ports"])
    }

    @Test
    fun `multiple services with dependencies`() {
        val compose = dockerCompose {
            service("web") {
                image = "nginx:alpine"
                dependsOn("redis", "db")
            }

            service("redis") {
                image = "redis:7-alpine"
            }

            service("db") {
                image = "postgres:16"
                environment("POSTGRES_PASSWORD", "secret")
            }
        }

        val map = compose.toMap()
        val services = map["services"] as Map<*, *>

        assertEquals(3, services.size)
        assertTrue(services.containsKey("web"))
        assertTrue(services.containsKey("redis"))
        assertTrue(services.containsKey("db"))

        val web = services["web"] as Map<*, *>
        assertEquals(listOf("redis", "db"), web["depends_on"])
    }

    @Test
    fun `service with environment variables`() {
        val compose = dockerCompose {
            service("app") {
                image = "myapp:latest"
                environment {
                    "NODE_ENV" to "production"
                    "PORT" to 3000
                    "DEBUG" to false
                }
            }
        }

        val map = compose.toMap()
        val services = map["services"] as Map<*, *>
        val app = services["app"] as Map<*, *>
        val env = app["environment"] as Map<*, *>

        assertEquals("production", env["NODE_ENV"])
        assertEquals("3000", env["PORT"])
        assertEquals("false", env["DEBUG"])
    }

    @Test
    fun `service with volumes`() {
        val compose = dockerCompose {
            service("db") {
                image = "postgres:16"
                volume("pgdata", "/var/lib/postgresql/data")
                volume("./init", "/docker-entrypoint-initdb.d", "ro")
            }

            volume("pgdata") {
                driver = "local"
            }
        }

        val map = compose.toMap()
        val services = map["services"] as Map<*, *>
        val db = services["db"] as Map<*, *>
        val volumes = db["volumes"] as List<*>

        assertEquals(2, volumes.size)
        assertTrue(volumes.contains("pgdata:/var/lib/postgresql/data"))
        assertTrue(volumes.contains("./init:/docker-entrypoint-initdb.d:ro"))

        val topLevelVolumes = map["volumes"] as Map<*, *>
        assertTrue(topLevelVolumes.containsKey("pgdata"))
    }

    @Test
    fun `service with networks`() {
        val compose = dockerCompose {
            service("web") {
                image = "nginx:alpine"
                networks("frontend", "backend")
            }

            network("frontend") {
                driver = "bridge"
            }

            network("backend") {
                internal = true
            }
        }

        val map = compose.toMap()
        val networks = map["networks"] as Map<*, *>

        assertEquals(2, networks.size)
        assertTrue(networks.containsKey("frontend"))
        assertTrue(networks.containsKey("backend"))
    }

    @Test
    fun `service with healthcheck`() {
        val compose = dockerCompose {
            service("web") {
                image = "nginx:alpine"
                healthcheck {
                    testShell("curl -f http://localhost/ || exit 1")
                    interval = "30s"
                    timeout = "10s"
                    retries = 3
                    startPeriod = "5s"
                }
            }
        }

        val map = compose.toMap()
        val services = map["services"] as Map<*, *>
        val web = services["web"] as Map<*, *>
        val healthcheck = web["healthcheck"] as Map<*, *>

        assertEquals(listOf("CMD-SHELL", "curl -f http://localhost/ || exit 1"), healthcheck["test"])
        assertEquals("30s", healthcheck["interval"])
        assertEquals("10s", healthcheck["timeout"])
        assertEquals(3, healthcheck["retries"])
    }

    @Test
    fun `service with build configuration`() {
        val compose = dockerCompose {
            service("app") {
                build("./app") {
                    dockerfile = "Dockerfile.prod"
                    target = "production"
                    arg("VERSION", "1.0.0")
                    cacheFrom("myapp:latest")
                }
            }
        }

        val map = compose.toMap()
        val services = map["services"] as Map<*, *>
        val app = services["app"] as Map<*, *>
        val build = app["build"] as Map<*, *>

        assertEquals("./app", build["context"])
        assertEquals("Dockerfile.prod", build["dockerfile"])
        assertEquals("production", build["target"])
        assertEquals(mapOf("VERSION" to "1.0.0"), build["args"])
        assertEquals(listOf("myapp:latest"), build["cache_from"])
    }

    @Test
    fun `service with deploy configuration`() {
        val compose = dockerCompose {
            service("web") {
                image = "nginx:alpine"
                deploy {
                    replicas = 3
                    resources {
                        limits {
                            cpus = "0.5"
                            memory = "512M"
                        }
                        reservations {
                            cpus = "0.25"
                            memory = "256M"
                        }
                    }
                    restartPolicy {
                        condition = "on-failure"
                        maxAttempts = 3
                    }
                }
            }
        }

        val map = compose.toMap()
        val services = map["services"] as Map<*, *>
        val web = services["web"] as Map<*, *>
        val deploy = web["deploy"] as Map<*, *>

        assertEquals(3, deploy["replicas"])

        val resources = deploy["resources"] as Map<*, *>
        val limits = resources["limits"] as Map<*, *>
        assertEquals("0.5", limits["cpus"])
        assertEquals("512M", limits["memory"])
    }

    @Test
    fun `service with logging`() {
        val compose = dockerCompose {
            service("app") {
                image = "myapp:latest"
                logging("json-file") {
                    maxSize("10m")
                    maxFile(3)
                }
            }
        }

        val map = compose.toMap()
        val services = map["services"] as Map<*, *>
        val app = services["app"] as Map<*, *>
        val logging = app["logging"] as Map<*, *>

        assertEquals("json-file", logging["driver"])
        val options = logging["options"] as Map<*, *>
        assertEquals("10m", options["max-size"])
        assertEquals("3", options["max-file"])
    }

    @Test
    fun `depends_on with conditions`() {
        val compose = dockerCompose {
            service("web") {
                image = "nginx:alpine"
                dependsOn {
                    serviceHealthy("db")
                    serviceStarted("redis")
                }
            }

            service("db") {
                image = "postgres:16"
            }

            service("redis") {
                image = "redis:7"
            }
        }

        val map = compose.toMap()
        val services = map["services"] as Map<*, *>
        val web = services["web"] as Map<*, *>
        val dependsOn = web["depends_on"] as Map<*, *>

        assertEquals(mapOf("condition" to "service_healthy"), dependsOn["db"])
        assertEquals(mapOf("condition" to "service_started"), dependsOn["redis"])
    }

    @Test
    fun `generates valid YAML`() {
        val compose = dockerCompose {
            service("web") {
                image = "nginx:alpine"
                ports("80:80")
                environment("ENV", "prod")
            }
        }

        val yaml = compose.toYaml()

        assertTrue(yaml.contains("services:"))
        assertTrue(yaml.contains("web:"))
        assertTrue(yaml.contains("image: nginx:alpine"))
        assertTrue(yaml.contains("ports:"))
        assertTrue(yaml.contains("- 80:80") || yaml.contains("- '80:80'"))
    }

    @Test
    fun `full compose file`() {
        val compose = dockerCompose {
            name = "myapp"

            service("web") {
                image = "nginx:alpine"
                containerName = "myapp-web"
                ports("8080:80", "8443:443")
                volumes("./nginx.conf:/etc/nginx/nginx.conf:ro")
                networks("frontend")
                dependsOn("api")
                restart = "unless-stopped"
                healthcheck {
                    testShell("curl -f http://localhost/ || exit 1")
                    interval = "30s"
                    timeout = "5s"
                    retries = 3
                }
            }

            service("api") {
                image = "myapp/api:latest"
                environment {
                    "DATABASE_URL" to "postgres://db:5432/myapp"
                    "REDIS_URL" to "redis://redis:6379"
                }
                networks("frontend", "backend")
                dependsOn {
                    serviceHealthy("db")
                    serviceStarted("redis")
                }
                deploy {
                    replicas = 2
                    resources {
                        limits {
                            cpus = "1"
                            memory = "1G"
                        }
                    }
                }
            }

            service("db") {
                image = "postgres:16-alpine"
                environment("POSTGRES_PASSWORD", "secret")
                volumes("pgdata:/var/lib/postgresql/data")
                networks("backend")
                healthcheck {
                    testCmd("pg_isready", "-U", "postgres")
                    interval = "10s"
                    timeout = "5s"
                    retries = 5
                }
            }

            service("redis") {
                image = "redis:7-alpine"
                networks("backend")
            }

            network("frontend") {
                driver = "bridge"
            }

            network("backend") {
                driver = "bridge"
                internal = true
            }

            volume("pgdata") {
                driver = "local"
            }
        }

        val yaml = compose.toYaml()
        println(yaml)

        assertTrue(yaml.contains("name: myapp"))
        assertTrue(yaml.contains("services:"))
        assertTrue(yaml.contains("networks:"))
        assertTrue(yaml.contains("volumes:"))
    }
}
