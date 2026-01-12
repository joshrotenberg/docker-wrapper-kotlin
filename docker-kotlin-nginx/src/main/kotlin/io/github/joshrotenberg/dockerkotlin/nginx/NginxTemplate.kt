package io.github.joshrotenberg.dockerkotlin.nginx

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor
import io.github.joshrotenberg.dockerkotlin.core.command.RunCommand
import io.github.joshrotenberg.dockerkotlin.template.AbstractTemplate
import io.github.joshrotenberg.dockerkotlin.template.TemplateConfig
import io.github.joshrotenberg.dockerkotlin.template.WaitStrategy

/**
 * Configuration for Nginx template.
 */
class NginxConfig : TemplateConfig() {
    /** Nginx version/tag. */
    var version: String = "alpine"

    /** HTTP port (default 80). */
    var httpPort: Int = 80

    /** Host HTTP port mapping (null for dynamic allocation). */
    var hostHttpPort: Int? = null

    /** HTTPS port (default 443). */
    var httpsPort: Int = 443

    /** Host HTTPS port mapping (null for dynamic allocation). */
    var hostHttpsPort: Int? = null

    /** Enable HTTPS. */
    var enableHttps: Boolean = false

    /** Path to custom nginx.conf file on the host. */
    var configFile: String? = null

    /** Path to custom configuration directory on the host (mounted to /etc/nginx/conf.d). */
    var configDir: String? = null

    /** Path to static content directory on the host. */
    var contentDir: String? = null

    /** Path to SSL certificate file on the host. */
    var sslCertificate: String? = null

    /** Path to SSL certificate key file on the host. */
    var sslCertificateKey: String? = null

    /** Custom HTML content to serve (written to index.html). */
    var htmlContent: String? = null
}

/**
 * Nginx container template.
 *
 * Provides a pre-configured Nginx container with sensible defaults.
 *
 * Example usage (Kotlin):
 * ```kotlin
 * val nginx = NginxTemplate("my-nginx") {
 *     httpPort(8080)
 *     contentDir("/path/to/static/files")
 * }
 *
 * nginx.use {
 *     it.startAndWait()
 *     println(it.httpUrl()) // http://localhost:8080
 * }
 * ```
 *
 * Example usage (Java):
 * ```java
 * try (NginxTemplate nginx = NginxTemplate.builder("my-nginx")
 *         .httpPort(8080)
 *         .contentDir("/path/to/static/files")
 *         .build()) {
 *     nginx.startAndWaitBlocking();
 *     System.out.println(nginx.httpUrl());
 * }
 * ```
 */
class NginxTemplate(
    name: String,
    executor: CommandExecutor = CommandExecutor(),
    configure: NginxConfig.() -> Unit = {}
) : AbstractTemplate(name, executor) {

    override val config = NginxConfig().apply(configure)

    override val image: String = "nginx"
    override val tag: String get() = config.version

    private var cachedHttpPort: Int? = null
    private var cachedHttpsPort: Int? = null

    init {
        // Set default wait strategy - check if nginx is responding on HTTP
        if (config.waitStrategy == WaitStrategy.Running) {
            config.waitStrategy = WaitStrategy.ForCommand(
                "curl", "-sf", "http://localhost:${config.httpPort}/"
            )
        }
    }

    /** Set the Nginx version. */
    fun version(version: String) = apply { config.version = version }

    /** Set the HTTP port to expose. */
    fun httpPort(port: Int) = apply {
        config.httpPort = port
        config.hostHttpPort = port
    }

    /** Set the HTTPS port to expose. */
    fun httpsPort(port: Int) = apply {
        config.httpsPort = port
        config.hostHttpsPort = port
    }

    /** Use dynamic port allocation for HTTP. */
    fun dynamicHttpPort() = apply {
        config.hostHttpPort = null
    }

    /** Use dynamic port allocation for HTTPS. */
    fun dynamicHttpsPort() = apply {
        config.hostHttpsPort = null
    }

    /** Enable HTTPS with the given certificate and key. */
    fun enableHttps(certificatePath: String, keyPath: String) = apply {
        config.enableHttps = true
        config.sslCertificate = certificatePath
        config.sslCertificateKey = keyPath
    }

    /** Mount a custom nginx.conf file. */
    fun withConfigFile(path: String) = apply { config.configFile = path }

    /** Mount a custom configuration directory. */
    fun withConfigDir(path: String) = apply { config.configDir = path }

    /** Mount a static content directory to serve. */
    fun withContentDir(path: String) = apply { config.contentDir = path }

    /** Set custom HTML content to serve as index.html. */
    fun withHtmlContent(html: String) = apply { config.htmlContent = html }

    override fun configureRunCommand(cmd: RunCommand) {
        // HTTP port mapping
        if (config.hostHttpPort != null) {
            cmd.port(config.hostHttpPort!!, config.httpPort)
        } else {
            cmd.dynamicPort(config.httpPort)
        }

        // HTTPS port mapping (if enabled)
        if (config.enableHttps) {
            if (config.hostHttpsPort != null) {
                cmd.port(config.hostHttpsPort!!, config.httpsPort)
            } else {
                cmd.dynamicPort(config.httpsPort)
            }
        }

        // Mount nginx.conf
        config.configFile?.let {
            cmd.volume(it, "/etc/nginx/nginx.conf", "ro")
        }

        // Mount configuration directory
        config.configDir?.let {
            cmd.volume(it, "/etc/nginx/conf.d", "ro")
        }

        // Mount content directory
        config.contentDir?.let {
            cmd.volume(it, "/usr/share/nginx/html", "ro")
        }

        // Mount SSL certificates
        config.sslCertificate?.let {
            cmd.volume(it, "/etc/nginx/ssl/cert.pem", "ro")
        }
        config.sslCertificateKey?.let {
            cmd.volume(it, "/etc/nginx/ssl/key.pem", "ro")
        }
    }

    override suspend fun checkPortReady(port: Int): Boolean {
        return try {
            val result = exec("curl", "-sf", "http://localhost:${config.httpPort}/")
            result.success
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun checkCommandReady(command: List<String>): Boolean {
        return try {
            val result = exec(*command.toTypedArray())
            result.success
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get the mapped HTTP host port.
     */
    suspend fun discoverHttpPort(): Int {
        cachedHttpPort?.let { return it }
        config.hostHttpPort?.let { return it }

        val id = containerId ?: return config.httpPort
        val output = executor.execute(listOf("port", id.value, config.httpPort.toString()))
        val port = output.stdout.trim()
            .substringAfterLast(":")
            .toIntOrNull()
            ?: config.httpPort

        cachedHttpPort = port
        return port
    }

    /**
     * Get the mapped HTTP host port (blocking version).
     */
    fun getHttpPortBlocking(): Int {
        cachedHttpPort?.let { return it }
        config.hostHttpPort?.let { return it }

        val id = containerId ?: return config.httpPort
        val output = executor.executeBlocking(listOf("port", id.value, config.httpPort.toString()))
        val port = output.stdout.trim()
            .substringAfterLast(":")
            .toIntOrNull()
            ?: config.httpPort

        cachedHttpPort = port
        return port
    }

    /**
     * Get the mapped HTTP host port (returns cached or static value only).
     */
    fun getHttpPort(): Int {
        return cachedHttpPort ?: config.hostHttpPort ?: config.httpPort
    }

    /**
     * Get the mapped HTTPS host port.
     */
    suspend fun discoverHttpsPort(): Int {
        if (!config.enableHttps) return config.httpsPort

        cachedHttpsPort?.let { return it }
        config.hostHttpsPort?.let { return it }

        val id = containerId ?: return config.httpsPort
        val output = executor.execute(listOf("port", id.value, config.httpsPort.toString()))
        val port = output.stdout.trim()
            .substringAfterLast(":")
            .toIntOrNull()
            ?: config.httpsPort

        cachedHttpsPort = port
        return port
    }

    /**
     * Get the mapped HTTPS host port (blocking version).
     */
    fun getHttpsPortBlocking(): Int {
        if (!config.enableHttps) return config.httpsPort

        cachedHttpsPort?.let { return it }
        config.hostHttpsPort?.let { return it }

        val id = containerId ?: return config.httpsPort
        val output = executor.executeBlocking(listOf("port", id.value, config.httpsPort.toString()))
        val port = output.stdout.trim()
            .substringAfterLast(":")
            .toIntOrNull()
            ?: config.httpsPort

        cachedHttpsPort = port
        return port
    }

    /**
     * Get the mapped HTTPS host port (returns cached or static value only).
     */
    fun getHttpsPort(): Int {
        return cachedHttpsPort ?: config.hostHttpsPort ?: config.httpsPort
    }

    /**
     * Get the HTTP URL for this Nginx instance.
     */
    fun httpUrl(): String {
        val port = getHttpPort()
        return "http://localhost:$port"
    }

    /**
     * Get the HTTPS URL for this Nginx instance.
     */
    fun httpsUrl(): String {
        val port = getHttpsPort()
        return "https://localhost:$port"
    }

    /**
     * Reload the Nginx configuration.
     */
    suspend fun reloadConfig(): String {
        val result = exec("nginx", "-s", "reload")
        return result.stdout.trim()
    }

    /**
     * Test the Nginx configuration.
     */
    suspend fun testConfig(): Boolean {
        return try {
            val result = exec("nginx", "-t")
            result.success
        } catch (e: Exception) {
            false
        }
    }

    companion object {
        /**
         * Create a new NginxTemplate builder (for Java interop).
         */
        @JvmStatic
        fun builder(name: String): NginxTemplateBuilder = NginxTemplateBuilder(name)
    }
}

/**
 * Builder for NginxTemplate (Java-friendly).
 */
class NginxTemplateBuilder(private val name: String) {
    private val config = NginxConfig()

    fun version(version: String) = apply { config.version = version }
    fun httpPort(port: Int) = apply { config.httpPort = port; config.hostHttpPort = port }
    fun httpsPort(port: Int) = apply { config.httpsPort = port; config.hostHttpsPort = port }
    fun enableHttps(certificatePath: String, keyPath: String) = apply {
        config.enableHttps = true
        config.sslCertificate = certificatePath
        config.sslCertificateKey = keyPath
    }

    fun withConfigFile(path: String) = apply { config.configFile = path }
    fun withConfigDir(path: String) = apply { config.configDir = path }
    fun withContentDir(path: String) = apply { config.contentDir = path }
    fun withHtmlContent(html: String) = apply { config.htmlContent = html }

    fun build(): NginxTemplate = NginxTemplate(name) {
        version = this@NginxTemplateBuilder.config.version
        httpPort = this@NginxTemplateBuilder.config.httpPort
        hostHttpPort = this@NginxTemplateBuilder.config.hostHttpPort
        httpsPort = this@NginxTemplateBuilder.config.httpsPort
        hostHttpsPort = this@NginxTemplateBuilder.config.hostHttpsPort
        enableHttps = this@NginxTemplateBuilder.config.enableHttps
        configFile = this@NginxTemplateBuilder.config.configFile
        configDir = this@NginxTemplateBuilder.config.configDir
        contentDir = this@NginxTemplateBuilder.config.contentDir
        sslCertificate = this@NginxTemplateBuilder.config.sslCertificate
        sslCertificateKey = this@NginxTemplateBuilder.config.sslCertificateKey
        htmlContent = this@NginxTemplateBuilder.config.htmlContent
    }
}
