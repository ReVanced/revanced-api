package app.revanced.api.configuration.services

import app.revanced.api.configuration.repository.AnnouncementRepository
import app.revanced.api.configuration.schema.APIAnnouncement
import app.revanced.api.configuration.schema.APIResponseAnnouncement
import app.revanced.api.configuration.schema.APIResponseAnnouncementId
import kotlinx.datetime.LocalDateTime

internal class AnnouncementService(
    private val announcementRepository: AnnouncementRepository,
) {
    fun latestId(channel: String): APIResponseAnnouncementId? = announcementRepository.latestId(channel)?.toApi()
    fun latestId(): APIResponseAnnouncementId? = announcementRepository.latestId()?.toApi()

    fun latest(channel: String) = announcementRepository.latest(channel)?.toApi()
    fun latest() = announcementRepository.latest()?.toApi()

    suspend fun all(channel: String) = announcementRepository.all(channel).map { it.toApi() }
    suspend fun all() = announcementRepository.all().map { it.toApi() }

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

    private fun AnnouncementRepository.Announcement.toApi() = APIResponseAnnouncement(
        id.value,
        author,
        title,
        content,
        attachments.map { it.url },
        channel,
        createdAt,
        archivedAt,
        level,
    )

    private fun Int.toApi() = APIResponseAnnouncementId(this)
}
