package io.github.joshrotenberg.dockerkotlin.compose.dsl

/**
 * Secret definition.
 */
class SecretSpec {
    /** File path for the secret. */
    var file: String? = null

    /** External secret flag. */
    var external: Boolean? = null

    /** External secret name. */
    var externalName: String? = null

    /** Secret name (overrides key). */
    var name: String? = null

    /** Environment variable containing the secret. */
    var environment: String? = null

    fun toMap(): Map<String, Any> = buildMap {
        file?.let { put("file", it) }

        when {
            externalName != null -> put("external", mapOf("name" to externalName))
            external == true -> put("external", true)
        }

        name?.let { put("name", it) }
        environment?.let { put("environment", it) }
    }
}

/**
 * Secret reference for a service.
 */
class SecretReference(
    /** Secret name. */
    val source: String
) {
    /** Target path in container. */
    var target: String? = null

    /** UID of the file. */
    var uid: String? = null

    /** GID of the file. */
    var gid: String? = null

    /** File mode. */
    var mode: Int? = null

    fun toMap(): Map<String, Any> = buildMap {
        put("source", source)
        target?.let { put("target", it) }
        uid?.let { put("uid", it) }
        gid?.let { put("gid", it) }
        mode?.let { put("mode", it) }
    }
}

/**
 * Config definition.
 */
class ConfigSpec {
    /** File path for the config. */
    var file: String? = null

    /** External config flag. */
    var external: Boolean? = null

    /** External config name. */
    var externalName: String? = null

    /** Config name (overrides key). */
    var name: String? = null

    /** Config content (inline). */
    var content: String? = null

    /** Environment variable containing the config. */
    var environment: String? = null

    fun toMap(): Map<String, Any> = buildMap {
        file?.let { put("file", it) }

        when {
            externalName != null -> put("external", mapOf("name" to externalName))
            external == true -> put("external", true)
        }

        name?.let { put("name", it) }
        content?.let { put("content", it) }
        environment?.let { put("environment", it) }
    }
}

/**
 * Config reference for a service.
 */
class ConfigReference(
    /** Config name. */
    val source: String
) {
    /** Target path in container. */
    var target: String? = null

    /** UID of the file. */
    var uid: String? = null

    /** GID of the file. */
    var gid: String? = null

    /** File mode. */
    var mode: Int? = null

    fun toMap(): Map<String, Any> = buildMap {
        put("source", source)
        target?.let { put("target", it) }
        uid?.let { put("uid", it) }
        gid?.let { put("gid", it) }
        mode?.let { put("mode", it) }
    }
}
