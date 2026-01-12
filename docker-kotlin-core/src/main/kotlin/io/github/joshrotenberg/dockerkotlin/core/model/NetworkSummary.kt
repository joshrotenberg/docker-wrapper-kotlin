package io.github.joshrotenberg.dockerkotlin.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Summary information about a network from `docker network ls`.
 */
@Serializable
data class NetworkSummary(
    @SerialName("ID")
    val id: String,

    @SerialName("Name")
    val name: String,

    @SerialName("Driver")
    val driver: String,

    @SerialName("Scope")
    val scope: String,

    @SerialName("CreatedAt")
    val createdAt: String = "",

    @SerialName("IPv4")
    val ipv4: String = "",

    @SerialName("IPv6")
    val ipv6: String = "",

    @SerialName("Internal")
    val internal: String = "",

    @SerialName("Labels")
    val labels: String = ""
) {
    /** Whether IPv4 is enabled. */
    val ipv4Enabled: Boolean get() = ipv4 == "true"

    /** Whether IPv6 is enabled. */
    val ipv6Enabled: Boolean get() = ipv6 == "true"

    /** Whether this is an internal network. */
    val isInternal: Boolean get() = internal == "true"
}
