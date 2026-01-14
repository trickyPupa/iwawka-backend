package org.itmo.model

import java.time.Instant

data class Image(
    val id: Long,
    val data: ByteArray,
    val contentType: String,
    val sizeBytes: Int,
    val uploadedBy: Long?,
    val createdAt: Instant
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Image

        if (id != other.id) return false
        if (!data.contentEquals(other.data)) return false
        if (contentType != other.contentType) return false
        if (sizeBytes != other.sizeBytes) return false
        if (uploadedBy != other.uploadedBy) return false
        if (createdAt != other.createdAt) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + data.contentHashCode()
        result = 31 * result + contentType.hashCode()
        result = 31 * result + sizeBytes
        result = 31 * result + (uploadedBy?.hashCode() ?: 0)
        result = 31 * result + createdAt.hashCode()
        return result
    }
}

data class ImageMetadata(
    val id: Long,
    val contentType: String,
    val sizeBytes: Int,
    val uploadedBy: Long?,
    val createdAt: Instant
)
