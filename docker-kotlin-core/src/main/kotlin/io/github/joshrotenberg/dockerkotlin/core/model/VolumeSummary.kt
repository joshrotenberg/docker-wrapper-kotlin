package io.github.joshrotenberg.dockerkotlin.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Summary information about a volume from `docker volume ls`.
 */
@Serializable
data class VolumeSummary(
    @SerialName("Name")
    val name: String,

    @SerialName("Driver")
    val driver: String,

    @SerialName("Scope")
    val scope: String = "",

    @SerialName("Mountpoint")
    val mountpoint: String = "",

    @SerialName("Labels")
    val labels: String = "",

    @SerialName("Size")
    val size: String = "",

    @SerialName("Links")
    val links: String = "",

    @SerialName("Availability")
    val availability: String = "",

    @SerialName("Group")
    val group: String = "",

    @SerialName("Status")
    val status: String = ""
)
