package org.itmo.repository

import org.itmo.db.PostgresClient
import org.itmo.model.Image
import org.itmo.model.ImageMetadata
import java.sql.Timestamp
import java.time.Instant

class ImageRepository(private val postgresClient: PostgresClient) {

    /**
     * Создает новое изображение в базе данных
     * @param data Бинарные данные изображения
     * @param contentType MIME-тип изображения (например, image/jpeg, image/png)
     * @param uploadedBy ID пользователя, загрузившего изображение
     * @return ID созданного изображения
     */
    fun createImage(data: ByteArray, contentType: String, uploadedBy: Long?): Long {
        val connection = postgresClient.getConnection()
        try {
            val sql = """
                INSERT INTO images (data, content_type, size_bytes, uploaded_by, created_at)
                VALUES (?, ?, ?, ?, ?)
                RETURNING id
            """.trimIndent()

            connection.prepareStatement(sql).use { statement ->
                statement.setBytes(1, data)
                statement.setString(2, contentType)
                statement.setInt(3, data.size)
                if (uploadedBy != null) {
                    statement.setLong(4, uploadedBy)
                } else {
                    statement.setNull(4, java.sql.Types.BIGINT)
                }
                statement.setTimestamp(5, Timestamp.from(Instant.now()))

                val resultSet = statement.executeQuery()
                if (resultSet.next()) {
                    return resultSet.getLong("id")
                } else {
                    throw RuntimeException("Failed to create image")
                }
            }
        } finally {
            connection.close()
        }
    }

    /**
     * Получает изображение по ID
     * @param imageId ID изображения
     * @return Объект Image или null, если изображение не найдено
     */
    fun getImageById(imageId: Long): Image? {
        val connection = postgresClient.getConnection()
        try {
            val sql = """
                SELECT id, data, content_type, size_bytes, uploaded_by, created_at
                FROM images
                WHERE id = '$imageId'
            """.trimIndent()

            connection.prepareStatement(sql).use { statement ->
                val resultSet = statement.executeQuery()

                return if (resultSet.next()) {
                    Image(
                        id = resultSet.getLong("id"),
                        data = resultSet.getBytes("data"),
                        contentType = resultSet.getString("content_type"),
                        sizeBytes = resultSet.getInt("size_bytes"),
                        uploadedBy = resultSet.getObject("uploaded_by") as Long?,
                        createdAt = resultSet.getTimestamp("created_at").toInstant()
                    )
                } else {
                    null
                }
            }
        } finally {
            connection.close()
        }
    }

    /**
     * Получает только метаданные изображения (без бинарных данных)
     */
    fun getImageMetadata(imageId: Long): ImageMetadata? {
        val connection = postgresClient.getConnection()
        try {
            val sql = """
                SELECT id, content_type, size_bytes, uploaded_by, created_at
                FROM images
                WHERE id = ?
            """.trimIndent()

            connection.prepareStatement(sql).use { statement ->
                statement.setLong(1, imageId)
                val resultSet = statement.executeQuery()

                return if (resultSet.next()) {
                    ImageMetadata(
                        id = resultSet.getLong("id"),
                        contentType = resultSet.getString("content_type"),
                        sizeBytes = resultSet.getInt("size_bytes"),
                        uploadedBy = resultSet.getObject("uploaded_by") as Long?,
                        createdAt = resultSet.getTimestamp("created_at").toInstant()
                    )
                } else {
                    null
                }
            }
        } finally {
            connection.close()
        }
    }

    /**
     * Удаляет изображение по ID
     * @param imageId ID изображения
     * @return true, если изображение было удалено
     */
    fun deleteImage(imageId: Long): Boolean {
        val connection = postgresClient.getConnection()
        try {
            val sql = "DELETE FROM images WHERE id = ?"
            connection.prepareStatement(sql).use { statement ->
                statement.setLong(1, imageId)
                val rowsAffected = statement.executeUpdate()
                return rowsAffected > 0
            }
        } finally {
            connection.close()
        }
    }

    /**
     * Получает все изображения пользователя
     */
    fun getImagesByUser(userId: Long): List<ImageMetadata> {
        val connection = postgresClient.getConnection()
        try {
            val sql = """
                SELECT id, content_type, size_bytes, uploaded_by, created_at
                FROM images
                WHERE uploaded_by = ?
                ORDER BY created_at DESC
            """.trimIndent()

            connection.prepareStatement(sql).use { statement ->
                statement.setLong(1, userId)
                val resultSet = statement.executeQuery()

                val images = mutableListOf<ImageMetadata>()
                while (resultSet.next()) {
                    images.add(
                        ImageMetadata(
                            id = resultSet.getLong("id"),
                            contentType = resultSet.getString("content_type"),
                            sizeBytes = resultSet.getInt("size_bytes"),
                            uploadedBy = resultSet.getObject("uploaded_by") as Long?,
                            createdAt = resultSet.getTimestamp("created_at").toInstant()
                        )
                    )
                }
                return images
            }
        } finally {
            connection.close()
        }
    }
}