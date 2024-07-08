package app.revanced.api.configuration.repository

import app.revanced.api.configuration.schema.APIAnnouncement
import app.revanced.api.configuration.schema.APIResponseAnnouncement
import app.revanced.api.configuration.schema.APIResponseAnnouncementId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.*
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentDateTime
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.experimental.suspendedTransactionAsync

internal class AnnouncementRepository {
    // This is better than doing a maxByOrNull { it.id }.
    private var latestAnnouncement: Announcement? = null
    private val latestAnnouncementByChannel = mutableMapOf<String, Announcement>()

    private fun updateLatestAnnouncement(new: Announcement) {
        if (latestAnnouncement?.id?.value == new.id.value) {
            latestAnnouncement = new
            latestAnnouncementByChannel[new.channel ?: return] = new
        }
    }

    init {
        runBlocking {
            transaction {
                SchemaUtils.create(Announcements, Attachments)

                // Initialize the latest announcement.
                latestAnnouncement = Announcement.all().onEach {
                    latestAnnouncementByChannel[it.channel ?: return@onEach] = it
                }.maxByOrNull { it.id } ?: return@transaction
            }
        }
    }

    suspend fun all() = transaction {
        Announcement.all().map { it.toApi() }
    }

    suspend fun all(channel: String) = transaction {
        Announcement.find { Announcements.channel eq channel }.map { it.toApi() }
    }

    suspend fun delete(id: Int) = transaction {
        val announcement = Announcement.findById(id) ?: return@transaction

        announcement.delete()

        // In case the latest announcement was deleted, query the new latest announcement again.
        if (latestAnnouncement?.id?.value == id) {
            latestAnnouncement = Announcement.all().maxByOrNull { it.id }

            // If no latest announcement was found, remove it from the channel map.
            if (latestAnnouncement == null) {
                latestAnnouncementByChannel.remove(announcement.channel)
            } else {
                latestAnnouncementByChannel[latestAnnouncement!!.channel ?: return@transaction] = latestAnnouncement!!
            }
        }
    }

    fun latest() = latestAnnouncement?.toApi()

    fun latest(channel: String) = latestAnnouncementByChannel[channel]?.toApi()

    fun latestId() = latest()?.id?.toApi()

    fun latestId(channel: String) = latest(channel)?.id?.toApi()

    suspend fun archive(
        id: Int,
        archivedAt: LocalDateTime?,
    ) = transaction {
        Announcement.findByIdAndUpdate(id) {
            it.archivedAt = archivedAt ?: java.time.LocalDateTime.now().toKotlinLocalDateTime()
        }?.also(::updateLatestAnnouncement)
    }

    suspend fun unarchive(id: Int) = transaction {
        Announcement.findByIdAndUpdate(id) {
            it.archivedAt = null
        }?.also(::updateLatestAnnouncement)
    }

    suspend fun new(new: APIAnnouncement) = transaction {
        Announcement.new {
            author = new.author
            title = new.title
            content = new.content
            channel = new.channel
            archivedAt = new.archivedAt
            level = new.level
        }.also { newAnnouncement ->
            new.attachmentUrls.map { newUrl ->
                suspendedTransactionAsync {
                    Attachment.new {
                        url = newUrl
                        announcement = newAnnouncement
                    }
                }
            }.awaitAll()
        }.also(::updateLatestAnnouncement)
    }

    suspend fun update(id: Int, new: APIAnnouncement) = transaction {
        Announcement.findByIdAndUpdate(id) {
            it.author = new.author
            it.title = new.title
            it.content = new.content
            it.channel = new.channel
            it.archivedAt = new.archivedAt
            it.level = new.level
        }?.also { newAnnouncement ->
            newAnnouncement.attachments.map {
                suspendedTransactionAsync {
                    it.delete()
                }
            }.awaitAll()

            new.attachmentUrls.map { newUrl ->
                suspendedTransactionAsync {
                    Attachment.new {
                        url = newUrl
                        announcement = newAnnouncement
                    }
                }
            }.awaitAll()
        }?.also(::updateLatestAnnouncement)
    }

    private suspend fun <T> transaction(statement: suspend Transaction.() -> T) =
        newSuspendedTransaction(Dispatchers.IO, statement = statement)

    private object Announcements : IntIdTable() {
        val author = varchar("author", 32).nullable()
        val title = varchar("title", 64)
        val content = text("content").nullable()
        val channel = varchar("channel", 16).nullable()
        val createdAt = datetime("createdAt").defaultExpression(CurrentDateTime)
        val archivedAt = datetime("archivedAt").nullable()
        val level = integer("level")
    }

    private object Attachments : IntIdTable() {
        val url = varchar("url", 256)
        val announcement = reference("announcement", Announcements, onDelete = ReferenceOption.CASCADE)
    }

    class Announcement(id: EntityID<Int>) : IntEntity(id) {
        companion object : IntEntityClass<Announcement>(Announcements)

        var author by Announcements.author
        var title by Announcements.title
        var content by Announcements.content
        val attachments by Attachment referrersOn Attachments.announcement
        var channel by Announcements.channel
        var createdAt by Announcements.createdAt
        var archivedAt by Announcements.archivedAt
        var level by Announcements.level
    }

    class Attachment(id: EntityID<Int>) : IntEntity(id) {
        companion object : IntEntityClass<Attachment>(Attachments)

        var url by Attachments.url
        var announcement by Announcement referencedOn Attachments.announcement
    }

    private fun Announcement.toApi() = APIResponseAnnouncement(
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
