package app.revanced.api.configuration.services

import app.revanced.api.configuration.ApiAnnouncement
import app.revanced.api.configuration.repository.AnnouncementRepository
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.toKotlinLocalDateTime
import org.jetbrains.exposed.sql.Database
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

private object AnnouncementServiceTest {
    private lateinit var announcementService: AnnouncementService

    @JvmStatic
    @BeforeAll
    fun setUp() {
        val database = Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false")

        announcementService = AnnouncementService(AnnouncementRepository(database))
    }

    @BeforeEach
    fun clear() {
        runBlocking {
            while (true) {
                val latestId = announcementService.latestId() ?: break
                announcementService.delete(latestId.id)
            }
        }
    }

    @Test
    fun `can do basic crud`(): Unit = runBlocking {
        announcementService.new(ApiAnnouncement(title = "title"))

        val latestId = announcementService.latestId()!!.id

        announcementService.update(latestId, ApiAnnouncement(title = "new title"))
        assert(announcementService.get(latestId)?.title == "new title")

        announcementService.delete(latestId)
        assertNull(announcementService.get(latestId))
        assertNull(announcementService.latestId())
    }

    @Test
    fun `archiving works properly`() = runBlocking {
        announcementService.new(ApiAnnouncement(title = "title"))

        val latest = announcementService.latest()!!
        assertNull(announcementService.get(latest.id)?.archivedAt)

        val updated = ApiAnnouncement(
            title = latest.title,
            archivedAt = LocalDateTime.now().toKotlinLocalDateTime(),
        )

        announcementService.update(latest.id, updated)
        assertNotNull(announcementService.get(latest.id)?.archivedAt)

        return@runBlocking
    }

    @Test
    fun `latest works properly`() = runBlocking {
        announcementService.new(ApiAnnouncement(title = "title"))
        announcementService.new(ApiAnnouncement(title = "title2"))

        var latest = announcementService.latest()
        assert(latest?.title == "title2")

        announcementService.delete(latest!!.id)

        latest = announcementService.latest()
        assert(latest?.title == "title")

        announcementService.delete(latest!!.id)
        assertNull(announcementService.latest())

        announcementService.new(ApiAnnouncement(title = "1", tags = listOf("tag1", "tag2")))
        announcementService.new(ApiAnnouncement(title = "2", tags = listOf("tag1", "tag3")))
        announcementService.new(ApiAnnouncement(title = "3", tags = listOf("tag1", "tag4")))

        assert(announcementService.latest(setOf("tag2")).first().title == "1")
        assert(announcementService.latest(setOf("tag3")).last().title == "2")

        val announcement2and3 = announcementService.latest(setOf("tag1", "tag3"))
        assert(announcement2and3.size == 2)
        assert(announcement2and3.any { it.title == "2" })
        assert(announcement2and3.any { it.title == "3" })

        announcementService.delete(announcementService.latestId()!!.id)
        assert(announcementService.latest(setOf("tag1", "tag3")).first().title == "2")

        announcementService.delete(announcementService.latestId()!!.id)
        assert(announcementService.latest(setOf("tag1", "tag3")).first().title == "1")

        announcementService.delete(announcementService.latestId()!!.id)
        assert(announcementService.latest(setOf("tag1", "tag3")).isEmpty())
        assert(announcementService.tags().isEmpty())
    }

    @Test
    fun `tags work properly`() = runBlocking {
        announcementService.new(ApiAnnouncement(title = "title", tags = listOf("tag1", "tag2")))
        announcementService.new(ApiAnnouncement(title = "title2", tags = listOf("tag1", "tag3")))

        val tags = announcementService.tags()
        assertEquals(3, tags.size)
        assert(tags.any { it.name == "tag1" })
        assert(tags.any { it.name == "tag2" })
        assert(tags.any { it.name == "tag3" })

        announcementService.delete(announcementService.latestId()!!.id)
        assertEquals(2, announcementService.tags().size)

        announcementService.update(
            announcementService.latestId()!!.id,
            ApiAnnouncement(title = "title", tags = listOf("tag1", "tag3")),
        )

        assertEquals(2, announcementService.tags().size)
        assert(announcementService.tags().any { it.name == "tag3" })
    }

    @Test
    fun `attachments work properly`() = runBlocking {
        announcementService.new(ApiAnnouncement(title = "title", attachments = listOf("attachment1", "attachment2")))

        val latestAnnouncement = announcementService.latest()!!
        val latestId = latestAnnouncement.id

        val attachments = latestAnnouncement.attachments!!
        assertEquals(2, attachments.size)
        assert(attachments.any { it == "attachment1" })
        assert(attachments.any { it == "attachment2" })

        announcementService.update(
            latestId,
            ApiAnnouncement(title = "title", attachments = listOf("attachment1", "attachment3")),
        )
        assert(announcementService.get(latestId)!!.attachments!!.any { it == "attachment3" })
    }

    @Test
    fun `paging works correctly`() = runBlocking {
        repeat(10) {
            announcementService.new(ApiAnnouncement(title = "title$it"))
        }

        val announcements = announcementService.paged(Int.MAX_VALUE, 5, null)
        assertEquals(5, announcements.size, "Returns correct number of announcements")
        assertEquals("title9", announcements.first().title, "Starts from the latest announcement")

        val announcements2 = announcementService.paged(5, 5, null)
        assertEquals(5, announcements2.size, "Returns correct number of announcements when starting from the cursor")
        assertEquals("title4", announcements2.first().title, "Starts from the cursor")

        (0..4).forEach { id ->
            announcementService.update(
                id,
                ApiAnnouncement(
                    title = "title$id",
                    tags = (0..id).map { "tag$it" },
                    archivedAt = if (id % 2 == 0) {
                        // Only two announcements will be archived.
                        LocalDateTime.now().plusDays(2).minusDays(id.toLong()).toKotlinLocalDateTime()
                    } else {
                        null
                    },
                ),
            )
        }

        val tags = announcementService.tags()
        assertEquals(5, tags.size, "Returns correct number of newly created tags")

        val announcements3 = announcementService.paged(5, 5, setOf(tags[1].name))
        assertEquals(4, announcements3.size, "Filters announcements by tag")
    }
}
