package io.github.joshrotenberg.dockerkotlin.testing

/**
 * Marks a [ContainerGuard] field for automatic lifecycle management by [DockerExtension].
 *
 * When used with the [DockerExtension], annotated fields will be automatically
 * started before tests and stopped after tests.
 *
 * ## Scoping
 *
 * - **Static fields**: Container is started once before all tests in the class
 *   and stopped after all tests complete. Use for expensive containers that can
 *   be safely shared between tests.
 *
 * - **Instance fields**: Container is started fresh before each test and stopped
 *   after each test completes. Use when tests need isolated container state.
 *
 * ## Kotlin Usage
 *
 * ```kotlin
 * @ExtendWith(DockerExtension::class)
 * class MyTest {
 *
 *     companion object {
 *         @Container
 *         @JvmStatic
 *         val sharedRedis = ContainerGuard(RedisTemplate("shared") { port(6379) })
 *     }
 *
 *     @Container
 *     val isolatedRedis = ContainerGuard(RedisTemplate("isolated") { port(6380) })
 *
 *     @Test
 *     fun `test with containers`() {
 *         // sharedRedis is shared across all tests
 *         // isolatedRedis is fresh for this test
 *     }
 * }
 * ```
 *
 * ## Java Usage
 *
 * ```java
 * @ExtendWith(DockerExtension.class)
 * class MyTest {
 *
 *     @Container
 *     static ContainerGuard<RedisTemplate> sharedRedis =
 *         ContainerGuard.of(new RedisTemplate.Builder("shared").port(6379).build());
 *
 *     @Container
 *     ContainerGuard<RedisTemplate> isolatedRedis =
 *         ContainerGuard.of(new RedisTemplate.Builder("isolated").port(6380).build());
 *
 *     @Test
 *     void testWithContainers() {
 *         // Use containers...
 *     }
 * }
 * ```
 *
 * @see DockerExtension
 * @see ContainerGuard
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Container
