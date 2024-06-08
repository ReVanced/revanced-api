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

    fun all(channel: String) = announcementRepository.all(channel)
    fun all() = announcementRepository.all()

    fun new(new: APIAnnouncement) {
        announcementRepository.new(new)
    }
    fun archive(id: Int, archivedAt: LocalDateTime?) {
        announcementRepository.archive(id, archivedAt)
    }
    fun unarchive(id: Int) {
        announcementRepository.unarchive(id)
    }
    fun update(id: Int, new: APIAnnouncement) {
        announcementRepository.update(id, new)
    }
    fun delete(id: Int) {
        announcementRepository.delete(id)
    }
}
