package io.github.joshrotenberg.dockerkotlin.compose.dsl

/**
 * Volume configuration.
 */
class VolumeSpec {
    /** Volume driver. */
    var driver: String? = null

    /** Driver options. */
    private val driverOpts = mutableMapOf<String, String>()

    /** External volume flag. */
    var external: Boolean? = null

    /** External volume name (if different). */
    var externalName: String? = null

    /** Volume name (overrides key). */
    var name: String? = null

    /** Labels. */
    private val labelMap = mutableMapOf<String, String>()

    /**
     * Add driver option.
     */
    fun driverOpt(key: String, value: String) {
        driverOpts[key] = value
    }

    /**
     * Add label.
     */
    fun label(key: String, value: String) {
        labelMap[key] = value
    }

    fun toMap(): Map<String, Any> = buildMap {
        driver?.let { put("driver", it) }

        if (driverOpts.isNotEmpty()) {
            put("driver_opts", driverOpts.toMap())
        }

        when {
            externalName != null -> put("external", mapOf("name" to externalName))
            external == true -> put("external", true)
        }

        name?.let { put("name", it) }

        if (labelMap.isNotEmpty()) {
            put("labels", labelMap.toMap())
        }
    }
}

/**
 * Volume mount configuration (long syntax).
 */
class VolumeMount {
    /** Mount type: volume, bind, tmpfs, npipe. */
    var type: String? = null

    /** Volume name or host path. */
    var source: String? = null

    /** Mount point in container. */
    var target: String? = null

    /** Read-only mount. */
    var readOnly: Boolean? = null

    /** Bind mount options. */
    private var bindSpec: BindOptions? = null

    /** Volume options. */
    private var volumeSpec: VolumeOptions? = null

    /** Tmpfs options. */
    private var tmpfsSpec: TmpfsOptions? = null

    /** Consistency mode. */
    var consistency: String? = null

    /**
     * Configure bind mount options.
     */
    fun bind(init: BindOptions.() -> Unit) {
        type = "bind"
        bindSpec = BindOptions().apply(init)
    }

    /**
     * Configure volume options.
     */
    fun volume(init: VolumeOptions.() -> Unit) {
        type = "volume"
        volumeSpec = VolumeOptions().apply(init)
    }

    /**
     * Configure tmpfs options.
     */
    fun tmpfs(init: TmpfsOptions.() -> Unit) {
        type = "tmpfs"
        tmpfsSpec = TmpfsOptions().apply(init)
    }

    fun toMap(): Map<String, Any> = buildMap {
        type?.let { put("type", it) }
        source?.let { put("source", it) }
        target?.let { put("target", it) }
        readOnly?.let { put("read_only", it) }
        bindSpec?.let { put("bind", it.toMap()) }
        volumeSpec?.let { put("volume", it.toMap()) }
        tmpfsSpec?.let { put("tmpfs", it.toMap()) }
        consistency?.let { put("consistency", it) }
    }
}

class BindOptions {
    var propagation: String? = null
    var createHostPath: Boolean? = null
    var selinux: String? = null

    fun toMap(): Map<String, Any> = buildMap {
        propagation?.let { put("propagation", it) }
        createHostPath?.let { put("create_host_path", it) }
        selinux?.let { put("selinux", it) }
    }
}

class VolumeOptions {
    var nocopy: Boolean? = null

    fun toMap(): Map<String, Any> = buildMap {
        nocopy?.let { put("nocopy", it) }
    }
}

class TmpfsOptions {
    var size: String? = null
    var mode: Int? = null

    fun toMap(): Map<String, Any> = buildMap {
        size?.let { put("size", it) }
        mode?.let { put("mode", it) }
    }
}
