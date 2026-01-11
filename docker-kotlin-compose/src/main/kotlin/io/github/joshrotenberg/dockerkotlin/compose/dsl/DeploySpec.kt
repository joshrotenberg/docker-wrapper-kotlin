package io.github.joshrotenberg.dockerkotlin.compose.dsl

/**
 * Deploy configuration for Swarm mode.
 */
class DeploySpec {
    /** Number of replicas. */
    var replicas: Int? = null

    /** Endpoint mode. */
    var endpointMode: String? = null

    /** Deployment mode (replicated or global). */
    var mode: String? = null

    /** Labels for the service. */
    private val labelMap = mutableMapOf<String, String>()

    /** Placement constraints. */
    private var placementSpec: PlacementSpec? = null

    /** Resource limits and reservations. */
    private var resourcesSpec: ResourcesSpec? = null

    /** Restart policy. */
    private var restartPolicySpec: RestartPolicySpec? = null

    /** Rollback configuration. */
    private var rollbackConfigSpec: RollbackConfigSpec? = null

    /** Update configuration. */
    private var updateConfigSpec: UpdateConfigSpec? = null

    /**
     * Add label.
     */
    fun label(key: String, value: String) {
        labelMap[key] = value
    }

    /**
     * Configure placement.
     */
    fun placement(init: PlacementSpec.() -> Unit) {
        placementSpec = PlacementSpec().apply(init)
    }

    /**
     * Configure resources.
     */
    fun resources(init: ResourcesSpec.() -> Unit) {
        resourcesSpec = ResourcesSpec().apply(init)
    }

    /**
     * Configure restart policy.
     */
    fun restartPolicy(init: RestartPolicySpec.() -> Unit) {
        restartPolicySpec = RestartPolicySpec().apply(init)
    }

    /**
     * Configure rollback.
     */
    fun rollbackConfig(init: RollbackConfigSpec.() -> Unit) {
        rollbackConfigSpec = RollbackConfigSpec().apply(init)
    }

    /**
     * Configure update.
     */
    fun updateConfig(init: UpdateConfigSpec.() -> Unit) {
        updateConfigSpec = UpdateConfigSpec().apply(init)
    }

    fun toMap(): Map<String, Any> = buildMap {
        replicas?.let { put("replicas", it) }
        endpointMode?.let { put("endpoint_mode", it) }
        mode?.let { put("mode", it) }

        if (labelMap.isNotEmpty()) {
            put("labels", labelMap.toMap())
        }

        placementSpec?.let { put("placement", it.toMap()) }
        resourcesSpec?.let { put("resources", it.toMap()) }
        restartPolicySpec?.let { put("restart_policy", it.toMap()) }
        rollbackConfigSpec?.let { put("rollback_config", it.toMap()) }
        updateConfigSpec?.let { put("update_config", it.toMap()) }
    }
}

class PlacementSpec {
    private val constraints = mutableListOf<String>()
    private val preferences = mutableListOf<Map<String, Any>>()
    var maxReplicasPerNode: Int? = null

    fun constraint(constraint: String) {
        constraints.add(constraint)
    }

    fun preference(spread: String) {
        preferences.add(mapOf("spread" to spread))
    }

    fun toMap(): Map<String, Any> = buildMap {
        if (constraints.isNotEmpty()) {
            put("constraints", constraints.toList())
        }
        if (preferences.isNotEmpty()) {
            put("preferences", preferences.toList())
        }
        maxReplicasPerNode?.let { put("max_replicas_per_node", it) }
    }
}

class ResourcesSpec {
    private var limitsSpec: ResourceLimits? = null
    private var reservationsSpec: ResourceLimits? = null

    fun limits(init: ResourceLimits.() -> Unit) {
        limitsSpec = ResourceLimits().apply(init)
    }

    fun reservations(init: ResourceLimits.() -> Unit) {
        reservationsSpec = ResourceLimits().apply(init)
    }

    fun toMap(): Map<String, Any> = buildMap {
        limitsSpec?.let { put("limits", it.toMap()) }
        reservationsSpec?.let { put("reservations", it.toMap()) }
    }
}

class ResourceLimits {
    var cpus: String? = null
    var memory: String? = null
    var pids: Int? = null

    fun toMap(): Map<String, Any> = buildMap {
        cpus?.let { put("cpus", it) }
        memory?.let { put("memory", it) }
        pids?.let { put("pids", it) }
    }
}

class RestartPolicySpec {
    var condition: String? = null
    var delay: String? = null
    var maxAttempts: Int? = null
    var window: String? = null

    fun toMap(): Map<String, Any> = buildMap {
        condition?.let { put("condition", it) }
        delay?.let { put("delay", it) }
        maxAttempts?.let { put("max_attempts", it) }
        window?.let { put("window", it) }
    }
}

class RollbackConfigSpec {
    var parallelism: Int? = null
    var delay: String? = null
    var failureAction: String? = null
    var monitor: String? = null
    var maxFailureRatio: Double? = null
    var order: String? = null

    fun toMap(): Map<String, Any> = buildMap {
        parallelism?.let { put("parallelism", it) }
        delay?.let { put("delay", it) }
        failureAction?.let { put("failure_action", it) }
        monitor?.let { put("monitor", it) }
        maxFailureRatio?.let { put("max_failure_ratio", it) }
        order?.let { put("order", it) }
    }
}

class UpdateConfigSpec {
    var parallelism: Int? = null
    var delay: String? = null
    var failureAction: String? = null
    var monitor: String? = null
    var maxFailureRatio: Double? = null
    var order: String? = null

    fun toMap(): Map<String, Any> = buildMap {
        parallelism?.let { put("parallelism", it) }
        delay?.let { put("delay", it) }
        failureAction?.let { put("failure_action", it) }
        monitor?.let { put("monitor", it) }
        maxFailureRatio?.let { put("max_failure_ratio", it) }
        order?.let { put("order", it) }
    }
}
