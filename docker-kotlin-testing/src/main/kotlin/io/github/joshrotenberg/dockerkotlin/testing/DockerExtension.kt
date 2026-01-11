package io.github.joshrotenberg.dockerkotlin.testing

import io.github.joshrotenberg.dockerkotlin.template.Template
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.TestWatcher
import org.slf4j.LoggerFactory
import java.util.Optional
import java.util.concurrent.ConcurrentHashMap

/**
 * JUnit 5 extension for managing Docker containers during tests.
 *
 * This extension automatically manages the lifecycle of containers registered
 * via [ContainerGuard] instances. Containers can be scoped to individual tests
 * or shared across all tests in a class.
 *
 * ## Basic Usage
 *
 * ```kotlin
 * @ExtendWith(DockerExtension::class)
 * class MyIntegrationTest {
 *
 *     companion object {
 *         @Container
 *         @JvmStatic
 *         val redis = ContainerGuard(RedisTemplate("test-redis") { port(6379) })
 *     }
 *
 *     @Test
 *     fun `test with redis`() {
 *         val url = redis.connectionString()
 *         // Use the container...
 *     }
 * }
 * ```
 *
 * ## Container Scopes
 *
 * - **Static fields**: Started once before all tests, stopped after all tests
 * - **Instance fields**: Started before each test, stopped after each test
 *
 * ## Failure Handling
 *
 * When a test fails, containers with `keepOnFailure(true)` will not be cleaned up,
 * allowing for debugging. Container logs can also be captured automatically with
 * `captureLogs(true)`.
 */
class DockerExtension : BeforeAllCallback, AfterAllCallback,
    BeforeEachCallback, AfterEachCallback, TestWatcher {

    private val logger = LoggerFactory.getLogger(DockerExtension::class.java)

    companion object {
        private val NAMESPACE = ExtensionContext.Namespace.create(DockerExtension::class.java)
        private const val CLASS_CONTAINERS_KEY = "classContainers"
        private const val INSTANCE_CONTAINERS_KEY = "instanceContainers"
    }

    override fun beforeAll(context: ExtensionContext) {
        val testClass = context.requiredTestClass
        val containers = findStaticContainers(testClass)

        if (containers.isNotEmpty()) {
            logger.debug("Starting {} class-scoped container(s) for {}", containers.size, testClass.simpleName)
            containers.forEach { guard ->
                runBlocking { guard.start() }
            }
            context.getStore(NAMESPACE).put(CLASS_CONTAINERS_KEY, containers)
        }
    }

    override fun afterAll(context: ExtensionContext) {
        @Suppress("UNCHECKED_CAST")
        val containers = context.getStore(NAMESPACE)
            .get(CLASS_CONTAINERS_KEY) as? List<ContainerGuard<*>> ?: return

        logger.debug("Cleaning up {} class-scoped container(s)", containers.size)
        containers.reversed().forEach { guard ->
            try {
                guard.close()
            } catch (e: Exception) {
                logger.warn("Error closing container: {}", e.message)
            }
        }
    }

    override fun beforeEach(context: ExtensionContext) {
        val testInstance = context.requiredTestInstance
        val containers = findInstanceContainers(testInstance)

        if (containers.isNotEmpty()) {
            logger.debug("Starting {} test-scoped container(s) for {}", containers.size, context.displayName)
            containers.forEach { guard ->
                runBlocking { guard.start() }
            }
            context.getStore(NAMESPACE).put(INSTANCE_CONTAINERS_KEY, containers)
        }
    }

    override fun afterEach(context: ExtensionContext) {
        @Suppress("UNCHECKED_CAST")
        val containers = context.getStore(NAMESPACE)
            .get(INSTANCE_CONTAINERS_KEY) as? List<ContainerGuard<*>> ?: return

        logger.debug("Cleaning up {} test-scoped container(s)", containers.size)
        containers.reversed().forEach { guard ->
            try {
                guard.close()
            } catch (e: Exception) {
                logger.warn("Error closing container: {}", e.message)
            }
        }
    }

    override fun testFailed(context: ExtensionContext, cause: Throwable?) {
        // Mark all containers as failed so they can be kept for debugging
        @Suppress("UNCHECKED_CAST")
        val instanceContainers = context.getStore(NAMESPACE)
            .get(INSTANCE_CONTAINERS_KEY) as? List<ContainerGuard<*>> ?: emptyList()

        @Suppress("UNCHECKED_CAST")
        val classContainers = context.getStore(NAMESPACE)
            .get(CLASS_CONTAINERS_KEY) as? List<ContainerGuard<*>> ?: emptyList()

        (instanceContainers + classContainers).forEach { guard ->
            guard.markFailed()
        }
    }

    override fun testSuccessful(context: ExtensionContext) {
        // Nothing special needed for successful tests
    }

    override fun testAborted(context: ExtensionContext, cause: Throwable?) {
        // Treat aborted as failed for container retention purposes
        testFailed(context, cause)
    }

    override fun testDisabled(context: ExtensionContext, reason: Optional<String>?) {
        // Nothing to do for disabled tests
    }

    private fun findStaticContainers(testClass: Class<*>): List<ContainerGuard<*>> {
        val containers = mutableListOf<ContainerGuard<*>>()

        // Find fields annotated with @Container
        testClass.declaredFields
            .filter { field ->
                java.lang.reflect.Modifier.isStatic(field.modifiers) &&
                        field.isAnnotationPresent(Container::class.java) &&
                        ContainerGuard::class.java.isAssignableFrom(field.type)
            }
            .forEach { field ->
                field.isAccessible = true
                val guard = field.get(null) as? ContainerGuard<*>
                if (guard != null) {
                    containers.add(guard)
                }
            }

        // Also check companion object for Kotlin classes
        testClass.declaredClasses
            .filter { it.simpleName == "Companion" }
            .forEach { companion ->
                companion.declaredFields
                    .filter { field ->
                        field.isAnnotationPresent(Container::class.java) &&
                                ContainerGuard::class.java.isAssignableFrom(field.type)
                    }
                    .forEach { field ->
                        field.isAccessible = true
                        val companionInstance = testClass.getDeclaredField("Companion").apply {
                            isAccessible = true
                        }.get(null)
                        val guard = field.get(companionInstance) as? ContainerGuard<*>
                        if (guard != null) {
                            containers.add(guard)
                        }
                    }
            }

        return containers
    }

    private fun findInstanceContainers(testInstance: Any): List<ContainerGuard<*>> {
        val containers = mutableListOf<ContainerGuard<*>>()

        testInstance::class.java.declaredFields
            .filter { field ->
                !java.lang.reflect.Modifier.isStatic(field.modifiers) &&
                        field.isAnnotationPresent(Container::class.java) &&
                        ContainerGuard::class.java.isAssignableFrom(field.type)
            }
            .forEach { field ->
                field.isAccessible = true
                val guard = field.get(testInstance) as? ContainerGuard<*>
                if (guard != null) {
                    containers.add(guard)
                }
            }

        return containers
    }
}
