package app.revanced.api.configuration.services

import app.revanced.api.configuration.repository.AnnouncementRepository
import app.revanced.api.configuration.schema.ApiAnnouncement
import kotlinx.datetime.LocalDateTime

internal class AnnouncementService(
    private val announcementRepository: AnnouncementRepository,
) {
    suspend fun latest(tags: Set<Int>) = announcementRepository.latest(tags)

    suspend fun latest() = announcementRepository.latest()

    fun latestId(tags: Set<Int>) = announcementRepository.latestId(tags)

    fun latestId() = announcementRepository.latestId()

    suspend fun paged(offset: Int, limit: Int, tags: Set<Int>?) = announcementRepository.paged(offset, limit, tags)

    suspend fun get(id: Int) = announcementRepository.get(id)

    suspend fun update(id: Int, new: ApiAnnouncement) = announcementRepository.update(id, new)

    suspend fun delete(id: Int) = announcementRepository.delete(id)

    suspend fun new(new: ApiAnnouncement) = announcementRepository.new(new)

    suspend fun archive(id: Int, archivedAt: LocalDateTime?) = announcementRepository.archive(id, archivedAt)

    suspend fun unarchive(id: Int) = announcementRepository.unarchive(id)

    suspend fun tags() = announcementRepository.tags()
}
