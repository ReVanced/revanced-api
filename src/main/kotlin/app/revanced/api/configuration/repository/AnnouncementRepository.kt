package app.revanced.api.configuration.repository

import app.revanced.api.configuration.schema.APIAnnouncement
import app.revanced.api.configuration.schema.APIResponseAnnouncementId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.*
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.dao.load
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentDateTime
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.experimental.suspendedTransactionAsync

internal class AnnouncementRepository {
    init {
        runBlocking {
            transaction {
                SchemaUtils.create(Announcements, Attachments)
            }
        }
    }

    suspend fun all() = transaction {
        Announcement.all()
    }

    suspend fun all(channel: String) = transaction {
        Announcement.find { Announcements.channel eq channel }
    }

    suspend fun delete(id: Int) = transaction {
        val announcement = Announcement.findById(id) ?: return@transaction

        announcement.delete()
    }

    // TODO: These are inefficient, but I'm not sure how to make them more efficient.

    suspend fun latest() = transaction {
        Announcement.all().maxByOrNull { it.id }?.load(Announcement::attachments)
    }

    suspend fun latest(channel: String) = transaction {
        Announcement.find { Announcements.channel eq channel }.maxByOrNull { it.id }?.load(Announcement::attachments)
    }

    suspend fun latestId() = transaction {
        Announcement.all().maxByOrNull { it.id }?.id?.value?.let {
            APIResponseAnnouncementId(it)
        }
    }

    suspend fun latestId(channel: String) = transaction {
        Announcement.find { Announcements.channel eq channel }.maxByOrNull { it.id }?.id?.value?.let {
            APIResponseAnnouncementId(it)
        }
    }

    suspend fun archive(
        id: Int,
        archivedAt: LocalDateTime?,
    ) = transaction {
        Announcement.findByIdAndUpdate(id) {
            it.archivedAt = archivedAt ?: java.time.LocalDateTime.now().toKotlinLocalDateTime()
        }
    }

    suspend fun unarchive(id: Int) = transaction {
        Announcement.findByIdAndUpdate(id) {
            it.archivedAt = null
        }
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
        }
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
        }
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
}
