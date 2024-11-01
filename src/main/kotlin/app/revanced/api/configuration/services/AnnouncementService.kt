package app.revanced.api.configuration.services

import app.revanced.api.configuration.repository.AnnouncementRepository
import app.revanced.api.configuration.schema.ApiAnnouncement

internal class AnnouncementService(
    private val announcementRepository: AnnouncementRepository,
) {
    suspend fun latest(tags: Set<String>) = announcementRepository.latest(tags)

    suspend fun latest() = announcementRepository.latest()

    fun latestId(tags: Set<String>) = announcementRepository.latestId(tags)

    fun latestId() = announcementRepository.latestId()

    suspend fun paged(cursor: Int, limit: Int, tags: Set<String>?, archived: Boolean) =
        announcementRepository.paged(cursor, limit, tags, archived)

    suspend fun get(id: Int) = announcementRepository.get(id)

    suspend fun update(id: Int, new: ApiAnnouncement) = announcementRepository.update(id, new)

    suspend fun delete(id: Int) = announcementRepository.delete(id)

    suspend fun new(new: ApiAnnouncement) = announcementRepository.new(new)

    suspend fun tags() = announcementRepository.tags()
}
