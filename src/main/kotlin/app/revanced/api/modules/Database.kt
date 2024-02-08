package app.revanced.api.modules

import app.revanced.api.modules.AnnouncementService.Attachments.announcement
import app.revanced.api.schema.APIAnnouncement
import app.revanced.api.schema.APILatestAnnouncement
import app.revanced.api.schema.APIResponseAnnouncement
import kotlinx.datetime.*
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.transactions.transaction

class AnnouncementService(private val database: Database) {
    private object Announcements : IntIdTable() {
        val author = varchar("author", 32).nullable()
        val title = varchar("title", 64)
        val content = text("content").nullable()
        val channel = varchar("channel", 16).nullable()
        val createdAt = datetime("createdAt")
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
        val attachments by Attachment referrersOn announcement
        var channel by Announcements.channel
        var createdAt by Announcements.createdAt
        var archivedAt by Announcements.archivedAt
        var level by Announcements.level

        fun api() = APIResponseAnnouncement(
            id.value,
            author,
            title,
            content,
            attachments.map(Attachment::url).toSet(),
            channel,
            createdAt,
            archivedAt,
            level,
        )
    }

    class Attachment(id: EntityID<Int>) : IntEntity(id) {
        companion object : IntEntityClass<Attachment>(Attachments)

        var url by Attachments.url
        var announcement by Announcement referencedOn Attachments.announcement
    }

    init {
        transaction {
            SchemaUtils.create(Announcements, Attachments)
        }
    }

    private fun <T> transaction(block: Transaction.() -> T) = transaction(database, block)

    fun read() = transaction {
        Announcement.all().map { it.api() }.toSet()
    }

    fun read(channel: String) = transaction {
        Announcement.find { Announcements.channel eq channel }.map { it.api() }.toSet()
    }

    fun delete(id: Int) = transaction {
        val announcement = Announcement.findById(id) ?: return@transaction

        announcement.delete()
    }

    fun latest() = transaction {
        Announcement.all().maxByOrNull { it.createdAt }?.api()
    }

    fun latest(channel: String) = transaction {
        Announcement.find { Announcements.channel eq channel }.maxByOrNull { it.createdAt }?.api()
    }

    fun latestId() = transaction {
        Announcement.all().maxByOrNull { it.createdAt }?.id?.value?.let {
            APILatestAnnouncement(it)
        }
    }

    fun latestId(channel: String) = transaction {
        Announcement.find { Announcements.channel eq channel }.maxByOrNull { it.createdAt }?.id?.value?.let {
            APILatestAnnouncement(it)
        }
    }

    fun archive(
        id: Int,
        archivedAt: LocalDateTime?,
    ) = transaction {
        Announcement.findById(id)?.apply {
            this.archivedAt = archivedAt ?: java.time.LocalDateTime.now().toKotlinLocalDateTime()
        }
    }

    fun unarchive(id: Int) = transaction {
        Announcement.findById(id)?.apply {
            archivedAt = null
        }
    }

    fun new(new: APIAnnouncement) = transaction {
        Announcement.new announcement@{
            author = new.author
            title = new.title
            content = new.content
            channel = new.channel
            createdAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            archivedAt = new.archivedAt
            level = new.level
        }.also { newAnnouncement ->
            new.attachmentUrls.map {
                Attachment.new {
                    url = it
                    announcement = newAnnouncement
                }
            }
        }
    }

    fun update(id: Int, new: APIAnnouncement) = transaction {
        Announcement.findById(id)?.apply {
            author = new.author
            title = new.title
            content = new.content
            channel = new.channel
            archivedAt = new.archivedAt
            level = new.level

            attachments.forEach(Attachment::delete)
            new.attachmentUrls.map {
                Attachment.new {
                    url = it
                    announcement = this@apply
                }
            }
        }
    }
}
