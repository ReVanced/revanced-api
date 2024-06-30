package app.revanced.api.configuration.repository

import app.revanced.api.configuration.repository.AnnouncementRepository.AttachmentTable.announcement
import app.revanced.api.configuration.schema.APIAnnouncement
import app.revanced.api.configuration.schema.APIResponseAnnouncement
import app.revanced.api.configuration.schema.APIResponseAnnouncementId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.*
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

internal class AnnouncementRepository(private val database: Database) {
    init {
        runBlocking {
            transaction {
                SchemaUtils.create(AnnouncementTable, AttachmentTable)
            }
        }
    }

    suspend fun all() = transaction {
        buildSet {
            AnnouncementEntity.all().forEach { announcement ->
                add(announcement.toApi())
            }
        }
    }

    suspend fun all(channel: String) = transaction {
        buildSet {
            AnnouncementEntity.find { AnnouncementTable.channel eq channel }.forEach { announcement ->
                add(announcement.toApi())
            }
        }
    }

    suspend fun delete(id: Int) = transaction {
        val announcement = AnnouncementEntity.findById(id) ?: return@transaction

        announcement.delete()
    }

    // TODO: These are inefficient, but I'm not sure how to make them more efficient.

    suspend fun latest() = transaction {
        AnnouncementEntity.all().maxByOrNull { it.createdAt }?.toApi()
    }

    suspend fun latest(channel: String) = transaction {
        AnnouncementEntity.find { AnnouncementTable.channel eq channel }.maxByOrNull { it.createdAt }?.toApi()
    }

    suspend fun latestId() = transaction {
        AnnouncementEntity.all().maxByOrNull { it.createdAt }?.id?.value?.let {
            APIResponseAnnouncementId(it)
        }
    }

    suspend fun latestId(channel: String) = transaction {
        AnnouncementEntity.find { AnnouncementTable.channel eq channel }.maxByOrNull { it.createdAt }?.id?.value?.let {
            APIResponseAnnouncementId(it)
        }
    }

    suspend fun archive(
        id: Int,
        archivedAt: LocalDateTime?,
    ) = transaction {
        AnnouncementEntity.findById(id)?.apply {
            this.archivedAt = archivedAt ?: java.time.LocalDateTime.now().toKotlinLocalDateTime()
        }
    }

    suspend fun unarchive(id: Int) = transaction {
        AnnouncementEntity.findById(id)?.apply {
            archivedAt = null
        }
    }

    suspend fun new(new: APIAnnouncement) = transaction {
        AnnouncementEntity.new announcement@{
            author = new.author
            title = new.title
            content = new.content
            channel = new.channel
            createdAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            archivedAt = new.archivedAt
            level = new.level
        }.also { newAnnouncement ->
            new.attachmentUrls.map {
                AttachmentEntity.new {
                    url = it
                    announcement = newAnnouncement
                }
            }
        }
    }

    suspend fun update(id: Int, new: APIAnnouncement) = transaction {
        AnnouncementEntity.findById(id)?.apply {
            author = new.author
            title = new.title
            content = new.content
            channel = new.channel
            archivedAt = new.archivedAt
            level = new.level

            attachments.forEach(AttachmentEntity::delete)
            new.attachmentUrls.map {
                AttachmentEntity.new {
                    url = it
                    announcement = this@apply
                }
            }
        }
    }

    private suspend fun <T> transaction(statement: Transaction.() -> T) =
        newSuspendedTransaction(Dispatchers.IO, database, statement = statement)

    private object AnnouncementTable : IntIdTable() {
        val author = varchar("author", 32).nullable()
        val title = varchar("title", 64)
        val content = text("content").nullable()
        val channel = varchar("channel", 16).nullable()
        val createdAt = datetime("createdAt")
        val archivedAt = datetime("archivedAt").nullable()
        val level = integer("level")
    }

    private object AttachmentTable : IntIdTable() {
        val url = varchar("url", 256)
        val announcement = reference("announcement", AnnouncementTable, onDelete = ReferenceOption.CASCADE)
    }

    class AnnouncementEntity(id: EntityID<Int>) : IntEntity(id) {
        companion object : IntEntityClass<AnnouncementEntity>(AnnouncementTable)

        var author by AnnouncementTable.author
        var title by AnnouncementTable.title
        var content by AnnouncementTable.content
        val attachments by AttachmentEntity referrersOn announcement
        var channel by AnnouncementTable.channel
        var createdAt by AnnouncementTable.createdAt
        var archivedAt by AnnouncementTable.archivedAt
        var level by AnnouncementTable.level

        fun toApi() = APIResponseAnnouncement(
            id.value,
            author,
            title,
            content,
            attachmentUrls = buildSet {
                attachments.forEach {
                    add(it.url)
                }
            },
            channel,
            createdAt,
            archivedAt,
            level,
        )
    }

    class AttachmentEntity(id: EntityID<Int>) : IntEntity(id) {
        companion object : IntEntityClass<AttachmentEntity>(AttachmentTable)

        var url by AttachmentTable.url
        var announcement by AnnouncementEntity referencedOn AttachmentTable.announcement
    }
}
