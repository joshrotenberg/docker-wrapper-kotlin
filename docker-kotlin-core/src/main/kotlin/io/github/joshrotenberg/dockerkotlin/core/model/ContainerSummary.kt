package io.github.joshrotenberg.dockerkotlin.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Summary information about a container from `docker ps`.
 */
@Serializable
data class ContainerSummary(
    @SerialName("ID")
    val id: String,

    @SerialName("Names")
    val names: String,

    @SerialName("Image")
    val image: String,

    @SerialName("Command")
    val command: String,

    @SerialName("CreatedAt")
    val createdAt: String,

    @SerialName("State")
    val state: String,

    @SerialName("Status")
    val status: String,

    @SerialName("Ports")
    val ports: String = "",

    @SerialName("Labels")
    val labels: String = "",

    @SerialName("Mounts")
    val mounts: String = "",

    @SerialName("Networks")
    val networks: String = "",

    @SerialName("Size")
    val size: String = "",

    @SerialName("RunningFor")
    val runningFor: String = "",

    @SerialName("LocalVolumes")
    val localVolumes: String = ""
) {
    /** Whether the container is running. */
    val isRunning: Boolean get() = state == "running"

    /** Whether the container is exited. */
    val isExited: Boolean get() = state == "exited"

    /** Whether the container is paused. */
    val isPaused: Boolean get() = state == "paused"

    /** Get the container name (without leading slash). */
    val name: String get() = names.removePrefix("/")

    /** Get labels as a map. */
    fun labelsMap(): Map<String, String> {
        if (labels.isBlank()) return emptyMap()
        return labels.split(",").mapNotNull { label ->
            val parts = label.split("=", limit = 2)
            if (parts.size == 2) parts[0] to parts[1] else null
        }.toMap()
    }
}
