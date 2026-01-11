package io.github.joshrotenberg.dockerkotlin.core.command

import io.github.joshrotenberg.dockerkotlin.core.CommandExecutor

/**
 * Command to build an image from a Dockerfile.
 *
 * Equivalent to `docker build`.
 *
 * Example usage:
 * ```kotlin
 * val imageId = BuildCommand(".")
 *     .tag("my-image:latest")
 *     .execute()
 * ```
 */
class BuildCommand(
    private val path: String = ".",
    executor: CommandExecutor = CommandExecutor()
) : AbstractDockerCommand<String>(executor) {

    private val tags = mutableListOf<String>()
    private var file: String? = null
    private val buildArgs = mutableMapOf<String, String>()
    private val labels = mutableMapOf<String, String>()
    private var target: String? = null
    private var noCache: Boolean = false
    private var pull: Boolean = false
    private var quiet: Boolean = false
    private var rm: Boolean = true
    private var forceRm: Boolean = false
    private var squash: Boolean = false
    private var platform: String? = null
    private var network: String? = null
    private val cacheFrom = mutableListOf<String>()
    private var compress: Boolean = false
    private var memory: String? = null
    private var memorySwap: String? = null
    private var cpuShares: Int? = null
    private var cpuPeriod: Long? = null
    private var cpuQuota: Long? = null
    private var cpusetCpus: String? = null
    private var cpusetMems: String? = null
    private var shmSize: String? = null

    /** Name and optionally a tag in the name:tag format. */
    fun tag(tag: String) = apply { this.tags.add(tag) }

    /** Name of the Dockerfile (default: PATH/Dockerfile). */
    fun file(file: String) = apply { this.file = file }

    /** Set build-time variable. */
    fun buildArg(name: String, value: String) = apply { this.buildArgs[name] = value }

    /** Set metadata for an image. */
    fun label(key: String, value: String) = apply { this.labels[key] = value }

    /** Set the target build stage to build. */
    fun target(target: String) = apply { this.target = target }

    /** Do not use cache when building the image. */
    fun noCache() = apply { this.noCache = true }

    /** Always attempt to pull a newer version of the image. */
    fun pull() = apply { this.pull = true }

    /** Suppress the build output and print image ID on success. */
    fun quiet() = apply { this.quiet = true }

    /** Remove intermediate containers after a successful build (default true). */
    fun rm(rm: Boolean) = apply { this.rm = rm }

    /** Always remove intermediate containers. */
    fun forceRm() = apply { this.forceRm = true }

    /** Squash newly built layers into a single new layer. */
    fun squash() = apply { this.squash = true }

    /** Set platform if server is multi-platform capable. */
    fun platform(platform: String) = apply { this.platform = platform }

    /** Set the networking mode for RUN instructions during build. */
    fun network(network: String) = apply { this.network = network }

    /** Images to consider as cache sources. */
    fun cacheFrom(image: String) = apply { this.cacheFrom.add(image) }

    /** Compress the build context using gzip. */
    fun compress() = apply { this.compress = true }

    /** Memory limit. */
    fun memory(memory: String) = apply { this.memory = memory }

    /** Swap limit equal to memory plus swap. */
    fun memorySwap(swap: String) = apply { this.memorySwap = swap }

    /** CPU shares (relative weight). */
    fun cpuShares(shares: Int) = apply { this.cpuShares = shares }

    /** Limit the CPU CFS period. */
    fun cpuPeriod(period: Long) = apply { this.cpuPeriod = period }

    /** Limit the CPU CFS quota. */
    fun cpuQuota(quota: Long) = apply { this.cpuQuota = quota }

    /** CPUs in which to allow execution. */
    fun cpusetCpus(cpus: String) = apply { this.cpusetCpus = cpus }

    /** MEMs in which to allow execution. */
    fun cpusetMems(mems: String) = apply { this.cpusetMems = mems }

    /** Size of /dev/shm. */
    fun shmSize(size: String) = apply { this.shmSize = size }

    override fun buildArgs(): List<String> = buildList {
        add("build")
        tags.forEach { add("--tag"); add(it) }
        file?.let { add("--file"); add(it) }
        buildArgs.forEach { (name, value) -> add("--build-arg"); add("$name=$value") }
        labels.forEach { (key, value) -> add("--label"); add("$key=$value") }
        target?.let { add("--target"); add(it) }
        if (noCache) add("--no-cache")
        if (pull) add("--pull")
        if (quiet) add("--quiet")
        if (!rm) add("--rm=false")
        if (forceRm) add("--force-rm")
        if (squash) add("--squash")
        platform?.let { add("--platform"); add(it) }
        network?.let { add("--network"); add(it) }
        cacheFrom.forEach { add("--cache-from"); add(it) }
        if (compress) add("--compress")
        memory?.let { add("--memory"); add(it) }
        memorySwap?.let { add("--memory-swap"); add(it) }
        cpuShares?.let { add("--cpu-shares"); add(it.toString()) }
        cpuPeriod?.let { add("--cpu-period"); add(it.toString()) }
        cpuQuota?.let { add("--cpu-quota"); add(it.toString()) }
        cpusetCpus?.let { add("--cpuset-cpus"); add(it) }
        cpusetMems?.let { add("--cpuset-mems"); add(it) }
        shmSize?.let { add("--shm-size"); add(it) }
        add(path)
    }

    override suspend fun execute(): String {
        val output = executeRaw()
        // Extract image ID from output
        return extractImageId(output.stdout)
    }

    override fun executeBlocking(): String {
        val output = executeRawBlocking()
        return extractImageId(output.stdout)
    }

    private fun extractImageId(stdout: String): String {
        // Look for "Successfully built <id>" or just return last line
        val regex = Regex("""Successfully built ([a-f0-9]+)""")
        val match = regex.find(stdout)
        return match?.groupValues?.get(1) ?: stdout.lines().lastOrNull { it.isNotBlank() } ?: ""
    }

    companion object {
        @JvmStatic
        fun builder(path: String = "."): BuildCommand = BuildCommand(path)
    }
}
