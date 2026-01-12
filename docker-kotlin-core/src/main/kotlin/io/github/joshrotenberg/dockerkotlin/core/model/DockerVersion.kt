package io.github.joshrotenberg.dockerkotlin.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Docker version information from `docker version --format json`.
 */
@Serializable
data class DockerVersion(
    @SerialName("Client")
    val client: ClientVersion,

    @SerialName("Server")
    val server: ServerVersion? = null
)

/**
 * Docker client version information.
 */
@Serializable
data class ClientVersion(
    @SerialName("Version")
    val version: String,

    @SerialName("ApiVersion")
    val apiVersion: String,

    @SerialName("DefaultAPIVersion")
    val defaultApiVersion: String = "",

    @SerialName("GitCommit")
    val gitCommit: String = "",

    @SerialName("GoVersion")
    val goVersion: String = "",

    @SerialName("Os")
    val os: String = "",

    @SerialName("Arch")
    val arch: String = "",

    @SerialName("BuildTime")
    val buildTime: String = "",

    @SerialName("Context")
    val context: String = ""
)

/**
 * Docker server version information.
 */
@Serializable
data class ServerVersion(
    @SerialName("Platform")
    val platform: ServerPlatform? = null,

    @SerialName("Version")
    val version: String,

    @SerialName("ApiVersion")
    val apiVersion: String,

    @SerialName("MinAPIVersion")
    val minApiVersion: String = "",

    @SerialName("Os")
    val os: String = "",

    @SerialName("Arch")
    val arch: String = "",

    @SerialName("GitCommit")
    val gitCommit: String = "",

    @SerialName("GoVersion")
    val goVersion: String = "",

    @SerialName("KernelVersion")
    val kernelVersion: String = "",

    @SerialName("BuildTime")
    val buildTime: String = "",

    @SerialName("Components")
    val components: List<ServerComponent> = emptyList()
)

/**
 * Docker server platform information.
 */
@Serializable
data class ServerPlatform(
    @SerialName("Name")
    val name: String
)

/**
 * Docker server component information.
 */
@Serializable
data class ServerComponent(
    @SerialName("Name")
    val name: String,

    @SerialName("Version")
    val version: String,

    @SerialName("Details")
    val details: Map<String, String> = emptyMap()
)
