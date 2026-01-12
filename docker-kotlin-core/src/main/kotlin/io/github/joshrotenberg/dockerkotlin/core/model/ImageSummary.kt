package io.github.joshrotenberg.dockerkotlin.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Summary information about an image from `docker images`.
 */
@Serializable
data class ImageSummary(
    @SerialName("ID")
    val id: String,

    @SerialName("Repository")
    val repository: String,

    @SerialName("Tag")
    val tag: String,

    @SerialName("CreatedAt")
    val createdAt: String,

    @SerialName("CreatedSince")
    val createdSince: String = "",

    @SerialName("Size")
    val size: String,

    @SerialName("Digest")
    val digest: String = "",

    @SerialName("Containers")
    val containers: String = "",

    @SerialName("SharedSize")
    val sharedSize: String = "",

    @SerialName("UniqueSize")
    val uniqueSize: String = ""
) {
    /** The full image reference (repository:tag). */
    val reference: String get() = if (tag.isNotBlank() && tag != "<none>") "$repository:$tag" else repository

    /** Whether this is a dangling image (no tag). */
    val isDangling: Boolean get() = tag == "<none>" || repository == "<none>"
}
