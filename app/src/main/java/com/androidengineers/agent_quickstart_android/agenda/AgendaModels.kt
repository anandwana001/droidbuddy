package com.androidengineers.agent_quickstart_android.agenda

import java.time.Duration
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class AgendaSpeaker(
    val id: String,
    val name: String,
    val headline: String,
    val bio: String,
    val photoUrl: String,
)

data class ConferenceSession(
    val id: String,
    val title: String,
    val abstract: String,
    val startTime: String,
    val endTime: String,
    val rooms: List<String>,
    val speakers: List<AgendaSpeaker>,
    val tags: List<String>,
    val level: String,
    val format: String,
    val service: Boolean,
    val plenum: Boolean,
) {
    fun startLabel() = agendaTime(startTime)

    fun timeLabel() = "${agendaTime(startTime)} – ${agendaTime(endTime)} IST"

    fun durationMinutes(): Long =
        runCatching {
                Duration.between(OffsetDateTime.parse(startTime), OffsetDateTime.parse(endTime))
                    .toMinutes()
            }
            .getOrDefault(0)

    fun matches(query: String, room: String = "", talksOnly: Boolean = false): Boolean =
        (room.isEmpty() || room in rooms) &&
            (!talksOnly || !service) &&
            listOf(
                    title,
                    abstract,
                    speakers.joinToString { it.name },
                    tags.joinToString(),
                    rooms.joinToString(),
                )
                .joinToString(" ")
                .contains(query.trim(), ignoreCase = true)

    fun overlaps(other: ConferenceSession): Boolean =
        id != other.id && startTime < other.endTime && other.startTime < endTime
}

data class ConferenceAgenda(
    val event: String,
    val date: String,
    val timezone: String,
    val sourceUrl: String,
    val feedUrl: String,
    val importedAt: String,
    val sourceRevision: String,
    val sessionCount: Int,
    val speakerCount: Int,
    val sessions: List<ConferenceSession>,
)

fun agendaTime(value: String): String =
    runCatching {
            OffsetDateTime.parse(value)
                .atZoneSameInstant(ZoneId.of("Asia/Kolkata"))
                .format(DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH))
        }
        .getOrDefault("Time unavailable")
