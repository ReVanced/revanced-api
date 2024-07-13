package app.revanced.api.configuration.services

import app.revanced.api.configuration.repository.AnnouncementRepository
import app.revanced.api.configuration.schema.APIAnnouncement
import app.revanced.api.configuration.schema.APIResponseAnnouncementId
import kotlinx.datetime.LocalDateTime

internal class AnnouncementService(
    private val announcementRepository: AnnouncementRepository,
) {
    fun latestId(channel: String): APIResponseAnnouncementId? = announcementRepository.latestId(channel)
    fun latestId(): APIResponseAnnouncementId? = announcementRepository.latestId()

    fun latest(channel: String) = announcementRepository.latest(channel)
    fun latest() = announcementRepository.latest()

    suspend fun all(channel: String) = announcementRepository.all(channel)
    suspend fun all() = announcementRepository.all()

    suspend fun new(new: APIAnnouncement) {
        announcementRepository.new(new)
    }
    suspend fun archive(id: Int, archivedAt: LocalDateTime?) {
        announcementRepository.archive(id, archivedAt)
    }
    suspend fun unarchive(id: Int) {
        announcementRepository.unarchive(id)
    }
    suspend fun update(id: Int, new: APIAnnouncement) {
        announcementRepository.update(id, new)
    }
    suspend fun delete(id: Int) {
        announcementRepository.delete(id)
    }
}
