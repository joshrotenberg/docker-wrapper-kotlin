package io.github.joshrotenberg.dockerkotlin.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Docker system information from `docker info --format json`.
 */
@Serializable
data class DockerInfo(
    @SerialName("ID")
    val id: String,

    @SerialName("Containers")
    val containers: Int,

    @SerialName("ContainersRunning")
    val containersRunning: Int,

    @SerialName("ContainersPaused")
    val containersPaused: Int,

    @SerialName("ContainersStopped")
    val containersStopped: Int,

    @SerialName("Images")
    val images: Int,

    @SerialName("Driver")
    val driver: String,

    @SerialName("MemoryLimit")
    val memoryLimit: Boolean = false,

    @SerialName("SwapLimit")
    val swapLimit: Boolean = false,

    @SerialName("CpuCfsPeriod")
    val cpuCfsPeriod: Boolean = false,

    @SerialName("CpuCfsQuota")
    val cpuCfsQuota: Boolean = false,

    @SerialName("CPUShares")
    val cpuShares: Boolean = false,

    @SerialName("CPUSet")
    val cpuSet: Boolean = false,

    @SerialName("PidsLimit")
    val pidsLimit: Boolean = false,

    @SerialName("IPv4Forwarding")
    val ipv4Forwarding: Boolean = false,

    @SerialName("Debug")
    val debug: Boolean = false,

    @SerialName("NFd")
    val nFd: Int = 0,

    @SerialName("OomKillDisable")
    val oomKillDisable: Boolean = false,

    @SerialName("NGoroutines")
    val nGoroutines: Int = 0,

    @SerialName("SystemTime")
    val systemTime: String = "",

    @SerialName("LoggingDriver")
    val loggingDriver: String = "",

    @SerialName("CgroupDriver")
    val cgroupDriver: String = "",

    @SerialName("CgroupVersion")
    val cgroupVersion: String = "",

    @SerialName("NEventsListener")
    val nEventsListener: Int = 0,

    @SerialName("KernelVersion")
    val kernelVersion: String = "",

    @SerialName("OperatingSystem")
    val operatingSystem: String = "",

    @SerialName("OSVersion")
    val osVersion: String = "",

    @SerialName("OSType")
    val osType: String = "",

    @SerialName("Architecture")
    val architecture: String = "",

    @SerialName("IndexServerAddress")
    val indexServerAddress: String = "",

    @SerialName("NCPU")
    val ncpu: Int = 0,

    @SerialName("MemTotal")
    val memTotal: Long = 0,

    @SerialName("DockerRootDir")
    val dockerRootDir: String = "",

    @SerialName("Name")
    val name: String = "",

    @SerialName("ServerVersion")
    val serverVersion: String = "",

    @SerialName("Plugins")
    val plugins: DockerPlugins? = null
)

/**
 * Docker plugin information.
 */
@Serializable
data class DockerPlugins(
    @SerialName("Volume")
    val volume: List<String> = emptyList(),

    @SerialName("Network")
    val network: List<String> = emptyList(),

    @SerialName("Authorization")
    val authorization: List<String>? = null,

    @SerialName("Log")
    val log: List<String> = emptyList()
)
