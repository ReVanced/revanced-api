package app.revanced.api.configuration.repository

import app.revanced.api.configuration.schema.ApiAnnouncement
import app.revanced.api.configuration.schema.ApiAnnouncementTag
import app.revanced.api.configuration.schema.ApiResponseAnnouncement
import app.revanced.api.configuration.schema.ApiResponseAnnouncementId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.*
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentDateTime
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

internal class AnnouncementRepository {
    // This is better than doing a maxByOrNull { it.id } on every request.
    private var latestAnnouncement: Announcement? = null
    private val latestAnnouncementByTag = mutableMapOf<Int, Announcement>()

    init {
        runBlocking {
            transaction {
                SchemaUtils.create(
                    Announcements,
                    Attachments,
                    Tags,
                    AnnouncementTags,
                )

                initializeLatestAnnouncements()
            }
        }
    }

    private fun initializeLatestAnnouncements() {
        latestAnnouncement = Announcement.all().orderBy(Announcements.id to SortOrder.DESC).firstOrNull()

        Tag.all().map { it.id.value }.forEach(::updateLatestAnnouncementForTag)
    }

    private fun updateLatestAnnouncement(new: Announcement) {
        if (latestAnnouncement == null || latestAnnouncement!!.id.value <= new.id.value) {
            latestAnnouncement = new
            new.tags.forEach { tag -> latestAnnouncementByTag[tag.id.value] = new }
        }
    }

    private fun updateLatestAnnouncementForTag(tag: Int) {
        val latestAnnouncementForTag = AnnouncementTags.select(AnnouncementTags.announcement)
            .where { AnnouncementTags.tag eq tag }
            .map { it[AnnouncementTags.announcement] }
            .mapNotNull { Announcement.findById(it) }
            .maxByOrNull { it.id }

        latestAnnouncementForTag?.let { latestAnnouncementByTag[tag] = it }
    }

    suspend fun latest() = transaction {
        latestAnnouncement.toApiResponseAnnouncement()
    }

    suspend fun latest(tags: Set<Int>) = transaction {
        tags.mapNotNull { tag -> latestAnnouncementByTag[tag] }.toApiAnnouncement()
    }

    fun latestId() = latestAnnouncement?.id?.value.toApiResponseAnnouncementId()

    fun latestId(tags: Set<Int>) =
        tags.map { tag -> latestAnnouncementByTag[tag]?.id?.value }.toApiResponseAnnouncementId()

    suspend fun paged(offset: Int, count: Int, tags: Set<Int>?) = transaction {
        if (tags == null) {
            Announcement.all()
        } else {
            @Suppress("NAME_SHADOWING")
            val tags = tags.mapNotNull { Tag.findById(it)?.id }

            Announcement.find {
                Announcements.id inSubQuery Announcements.innerJoin(AnnouncementTags)
                    .select(Announcements.id)
                    .where { AnnouncementTags.tag inList tags }
                    .withDistinct()
            }
        }.orderBy(Announcements.id to SortOrder.DESC).limit(count, offset.toLong()).map { it }.toApiAnnouncement()
    }

    suspend fun get(id: Int) = transaction {
        Announcement.findById(id).toApiResponseAnnouncement()
    }

    suspend fun new(new: ApiAnnouncement) = transaction {
        Announcement.new {
            author = new.author
            title = new.title
            content = new.content
            archivedAt = new.archivedAt
            level = new.level
            tags = SizedCollection(
                new.tags.map { tag -> Tag.find { Tags.name eq tag }.firstOrNull() ?: Tag.new { name = tag } },
            )
        }.apply {
            new.attachments.map { attachmentUrl ->
                Attachment.new {
                    url = attachmentUrl
                    announcement = this@apply
                }
            }
        }.let(::updateLatestAnnouncement)
    }

    suspend fun update(id: Int, new: ApiAnnouncement) = transaction {
        Announcement.findByIdAndUpdate(id) {
            it.author = new.author
            it.title = new.title
            it.content = new.content
            it.archivedAt = new.archivedAt
            it.level = new.level

            // Get the old tags, create new tags if they don't exist,
            // and delete tags that are not in the new tags, after updating the announcement.
            val oldTags = it.tags.toList()
            val updatedTags = new.tags.map { name ->
                Tag.find { Tags.name eq name }.firstOrNull() ?: Tag.new { this.name = name }
            }
            it.tags = SizedCollection(updatedTags)
            oldTags.forEach { tag ->
                if (tag in updatedTags || !tag.announcements.empty()) return@forEach
                tag.delete()
            }

            // Delete old attachments and create new attachments.
            it.attachments.forEach { attachment -> attachment.delete() }
            new.attachments.map { attachment ->
                Attachment.new {
                    url = attachment
                    announcement = it
                }
            }
        }?.let(::updateLatestAnnouncement) ?: Unit
    }

    suspend fun delete(id: Int) = transaction {
        val announcement = Announcement.findById(id) ?: return@transaction

        // Delete the tag if no other announcements are referencing it.
        // One count means that the announcement is the only one referencing the tag.
        announcement.tags.filter { tag -> tag.announcements.count() == 1L }.forEach { tag ->
            latestAnnouncementByTag -= tag.id.value
            tag.delete()
        }

        announcement.delete()

        // If the deleted announcement is the latest announcement, set the new latest announcement.
        if (latestAnnouncement?.id?.value == id) {
            latestAnnouncement = Announcement.all().orderBy(Announcements.id to SortOrder.DESC).firstOrNull()
        }

        // The new announcement may be the latest for a specific tag. Set the new latest announcement for that tag.
        latestAnnouncementByTag.keys.forEach { tag ->
            updateLatestAnnouncementForTag(tag)
        }
    }

    suspend fun tags() = transaction {
        Tag.all().toList().toApiTag()
    }

    private suspend fun <T> transaction(statement: suspend Transaction.() -> T) =
        newSuspendedTransaction(Dispatchers.IO, statement = statement)

    private object Announcements : IntIdTable() {
        val author = varchar("author", 32).nullable()
        val title = varchar("title", 64)
        val content = text("content").nullable()
        val createdAt = datetime("createdAt").defaultExpression(CurrentDateTime)
        val archivedAt = datetime("archivedAt").nullable()
        val level = integer("level")
    }

    private object Attachments : IntIdTable() {
        val url = varchar("url", 256)
        val announcement = reference("announcement", Announcements, onDelete = ReferenceOption.CASCADE)
    }

    private object Tags : IntIdTable() {
        val name = varchar("name", 16).uniqueIndex()
    }

    private object AnnouncementTags : Table() {
        val tag = reference("tag", Tags, onDelete = ReferenceOption.CASCADE)
        val announcement = reference("announcement", Announcements, onDelete = ReferenceOption.CASCADE)

        init {
            uniqueIndex(tag, announcement)
        }
    }

    class Announcement(id: EntityID<Int>) : IntEntity(id) {
        companion object : IntEntityClass<Announcement>(Announcements)

        var author by Announcements.author
        var title by Announcements.title
        var content by Announcements.content
        val attachments by Attachment referrersOn Attachments.announcement
        var tags by Tag via AnnouncementTags
        var createdAt by Announcements.createdAt
        var archivedAt by Announcements.archivedAt
        var level by Announcements.level
    }

    class Attachment(id: EntityID<Int>) : IntEntity(id) {
        companion object : IntEntityClass<Attachment>(Attachments)

        var url by Attachments.url
        var announcement by Announcement referencedOn Attachments.announcement
    }

    class Tag(id: EntityID<Int>) : IntEntity(id) {
        companion object : IntEntityClass<Tag>(Tags)

        var name by Tags.name
        var announcements by Announcement via AnnouncementTags
    }

    private fun Announcement?.toApiResponseAnnouncement() = this?.let {
        ApiResponseAnnouncement(
            id.value,
            author,
            title,
            content,
            attachments.map { it.url },
            tags.map { it.id.value },
            createdAt,
            archivedAt,
            level,
        )
    }

    private fun List<Announcement>.toApiAnnouncement() = map { it.toApiResponseAnnouncement()!! }

    private fun List<Tag>.toApiTag() = map { ApiAnnouncementTag(it.id.value, it.name) }

    private fun Int?.toApiResponseAnnouncementId() = this?.let { ApiResponseAnnouncementId(this) }

    private fun List<Int?>.toApiResponseAnnouncementId() = map { it.toApiResponseAnnouncementId() }
}
