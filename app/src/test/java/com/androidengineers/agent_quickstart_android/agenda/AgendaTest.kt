package com.androidengineers.agent_quickstart_android.agenda

import com.google.gson.Gson
import java.io.File
import org.junit.Assert.*
import org.junit.Test

class AgendaTest {
    private fun agenda() =
        Gson()
            .fromJson(File("src/main/assets/agenda.json").readText(), ConferenceAgenda::class.java)

    @Test
    fun officialSnapshotContainsEveryEntryAndSpeaker() {
        val data = agenda()
        assertEquals(67, data.sessions.size)
        assertEquals(67, data.sessions.map { it.id }.toSet().size)
        assertEquals(44, data.sessions.count { !it.service })
        assertEquals(7, data.sessions.flatMap { it.rooms }.toSet().size)
        assertEquals(52, data.sessions.flatMap { it.speakers }.map { it.id }.toSet().size)
        assertEquals("https://india.droidcon.com/droidcon-agenda", data.sourceUrl)
        assertTrue(
            data.sessions.all { it.startTime.endsWith("+05:30") && it.durationMinutes() > 0 }
        )
    }

    @Test
    fun filtersUseRealRoomSpeakerAndTopicFields() {
        val data = agenda()
        val talk = data.sessions.first { !it.service && it.speakers.isNotEmpty() }
        assertTrue(talk.matches(talk.speakers.first().name.lowercase()))
        assertTrue(talk.matches("", talk.rooms.first(), true))
        assertFalse(talk.matches("", "A room that does not exist"))
        assertFalse(data.sessions.first { it.service }.matches("", talksOnly = true))
    }

    @Test
    fun timingFormattingUsesISTAndDetectsConflicts() {
        val data = agenda()
        assertEquals("7:30 AM", data.sessions.first().startLabel())
        assertFalse(data.sessions.first().overlaps(data.sessions.first()))
        val pair =
            data.sessions.firstNotNullOf { a ->
                data.sessions.firstOrNull { a.overlaps(it) }?.let { a to it }
            }
        assertTrue(pair.first.overlaps(pair.second))
        assertTrue(pair.second.overlaps(pair.first))
    }
}
