package io.github.joshrotenberg.dockerkotlin.compose.dsl

/**
 * Network configuration.
 */
class NetworkSpec {
    /** Network driver. */
    var driver: String? = null

    /** Driver options. */
    private val driverOpts = mutableMapOf<String, String>()

    /** External network flag. */
    var external: Boolean? = null

    /** External network name (if different). */
    var externalName: String? = null

    /** Internal network (no external connectivity). */
    var internal: Boolean? = null

    /** Attachable (allows manual container attachment). */
    var attachable: Boolean? = null

    /** Network name (overrides key). */
    var name: String? = null

    /** Labels. */
    private val labelMap = mutableMapOf<String, String>()

    /** IPAM configuration. */
    private var ipamSpec: IpamSpec? = null

    /** Enable IPv6. */
    var enableIpv6: Boolean? = null

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

    /**
     * Configure IPAM.
     */
    fun ipam(init: IpamSpec.() -> Unit) {
        ipamSpec = IpamSpec().apply(init)
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

        internal?.let { put("internal", it) }
        attachable?.let { put("attachable", it) }
        name?.let { put("name", it) }

        if (labelMap.isNotEmpty()) {
            put("labels", labelMap.toMap())
        }

        ipamSpec?.let { put("ipam", it.toMap()) }
        enableIpv6?.let { put("enable_ipv6", it) }
    }
}

/**
 * IPAM configuration.
 */
class IpamSpec {
    /** IPAM driver. */
    var driver: String? = null

    /** IPAM driver options. */
    private val driverOpts = mutableMapOf<String, String>()

    /** Subnet configurations. */
    private val configs = mutableListOf<IpamConfig>()

    /**
     * Add driver option.
     */
    fun driverOpt(key: String, value: String) {
        driverOpts[key] = value
    }

    /**
     * Add subnet configuration.
     */
    fun config(init: IpamConfig.() -> Unit) {
        configs.add(IpamConfig().apply(init))
    }

    fun toMap(): Map<String, Any> = buildMap {
        driver?.let { put("driver", it) }

        if (driverOpts.isNotEmpty()) {
            put("options", driverOpts.toMap())
        }

        if (configs.isNotEmpty()) {
            put("config", configs.map { it.toMap() })
        }
    }
}

/**
 * IPAM subnet configuration.
 */
class IpamConfig {
    var subnet: String? = null
    var ipRange: String? = null
    var gateway: String? = null

    private val auxAddresses = mutableMapOf<String, String>()

    fun auxAddress(name: String, ip: String) {
        auxAddresses[name] = ip
    }

    fun toMap(): Map<String, Any> = buildMap {
        subnet?.let { put("subnet", it) }
        ipRange?.let { put("ip_range", it) }
        gateway?.let { put("gateway", it) }

        if (auxAddresses.isNotEmpty()) {
            put("aux_addresses", auxAddresses.toMap())
        }
    }
}

/**
 * Network connection options for a service.
 */
class NetworkConnection {
    /** Aliases for this service on this network. */
    private val aliasList = mutableListOf<String>()

    /** IPv4 address. */
    var ipv4Address: String? = null

    /** IPv6 address. */
    var ipv6Address: String? = null

    /** Priority for DNS resolution. */
    var priority: Int? = null

    /**
     * Add alias.
     */
    fun aliases(vararg names: String) {
        aliasList.addAll(names)
    }

    fun toMap(): Map<String, Any> = buildMap {
        if (aliasList.isNotEmpty()) {
            put("aliases", aliasList.toList())
        }
        ipv4Address?.let { put("ipv4_address", it) }
        ipv6Address?.let { put("ipv6_address", it) }
        priority?.let { put("priority", it) }
    }
}
