package io.github.joshrotenberg.dockerkotlin.compose.dsl

/**
 * Build configuration for a service.
 */
class BuildSpec(
    /** Build context path. */
    var context: String
) {
    /** Dockerfile path relative to context. */
    var dockerfile: String? = null

    /** Build arguments. */
    private val buildArgs = mutableMapOf<String, String>()

    /** Target build stage. */
    var target: String? = null

    /** Cache from images. */
    private val cacheFromList = mutableListOf<String>()

    /** Labels for the built image. */
    private val labelMap = mutableMapOf<String, String>()

    /** Network for RUN instructions. */
    var network: String? = null

    /** Shared memory size. */
    var shmSize: String? = null

    /** Extra hosts. */
    private val extraHostsList = mutableListOf<String>()

    /** SSH agent socket or keys. */
    private val sshList = mutableListOf<String>()

    /** Secrets to expose during build. */
    private val secretsList = mutableListOf<Any>()

    /** Platform for build. */
    var platform: String? = null

    /** Isolation technology. */
    var isolation: String? = null

    /** Disable cache. */
    var noCache: Boolean? = null

    /** Always pull base images. */
    var pull: Boolean? = null

    /**
     * Add build argument.
     */
    fun arg(name: String, value: String) {
        buildArgs[name] = value
    }

    /**
     * Add build arguments.
     */
    fun args(vararg pairs: Pair<String, String>) {
        pairs.forEach { (k, v) -> buildArgs[k] = v }
    }

    /**
     * Add cache source image.
     */
    fun cacheFrom(vararg images: String) {
        cacheFromList.addAll(images)
    }

    /**
     * Add label.
     */
    fun label(key: String, value: String) {
        labelMap[key] = value
    }

    /**
     * Add extra host.
     */
    fun extraHost(host: String, ip: String) {
        extraHostsList.add("$host:$ip")
    }

    /**
     * Add SSH agent or key.
     */
    fun ssh(spec: String) {
        sshList.add(spec)
    }

    /**
     * Add secret for build.
     */
    fun secret(id: String) {
        secretsList.add(id)
    }

    /**
     * Add secret for build with source.
     */
    fun secret(id: String, source: String) {
        secretsList.add(mapOf("id" to id, "source" to source))
    }

    fun toMap(): Map<String, Any> = buildMap {
        put("context", context)
        dockerfile?.let { put("dockerfile", it) }

        if (buildArgs.isNotEmpty()) {
            put("args", buildArgs.toMap())
        }

        target?.let { put("target", it) }

        if (cacheFromList.isNotEmpty()) {
            put("cache_from", cacheFromList.toList())
        }

        if (labelMap.isNotEmpty()) {
            put("labels", labelMap.toMap())
        }

        network?.let { put("network", it) }
        shmSize?.let { put("shm_size", it) }

        if (extraHostsList.isNotEmpty()) {
            put("extra_hosts", extraHostsList.toList())
        }

        if (sshList.isNotEmpty()) {
            put("ssh", sshList.toList())
        }

        if (secretsList.isNotEmpty()) {
            put("secrets", secretsList.toList())
        }

        platform?.let { put("platform", it) }
        isolation?.let { put("isolation", it) }
        noCache?.let { put("no_cache", it) }
        pull?.let { put("pull", it) }
    }
}
