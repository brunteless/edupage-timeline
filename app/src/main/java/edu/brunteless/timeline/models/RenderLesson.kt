@file:UseSerializers(LocalDateTimeSerializer::class)

package edu.brunteless.timeline.models

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import androidx.glance.state.GlanceStateDefinition
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.time.Duration
import java.time.LocalDateTime

@Serializable
data class RenderLesson(
    val shortName: String,
    val startTime: String,
    val endTime: String,
    val period: Int,

    val classroomShortName: String,
    val teacherName: String
)

@Serializable
data class RenderTimeline(
    val lessons: List<RenderLesson?>,
    val currentIndex: Int,
    val day: LocalDateTime
) {

    fun getDelayDuration(lesson: RenderLesson) : Duration {
        return Duration.between(
            LocalDateTime.now(),
            day.lessonEndTime(lesson)
        )
    }

    val isNotLatest: Boolean
        get() = lessons.isEmpty() || getDelayDuration(lessons.last()!!).isNegative


    private fun LocalDateTime.lessonEndTime(lesson: RenderLesson) : LocalDateTime {
        val (hours, minutes) = lesson.endTime.split(':').map { it.toInt() }.toIntArray()
        return this
            .withHour(hours)
            .withMinute(minutes)
            .withSecond(0)
    }
}

@Serializable
data class Tokens (
    val esid: String,
    val edid: String,
    val fromEdupage: String
)


object LocalDateTimeSerializer : KSerializer<LocalDateTime> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalDateTime) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): LocalDateTime {
        return LocalDateTime.parse(decoder.decodeString())
    }
}

object TimelineGlanceStateDefinition : GlanceStateDefinition<RenderTimeline> {

    private const val FILE_NAME = "timeline_state.json"
    private val Context.dataStore: DataStore<RenderTimeline> by dataStore(
        fileName = FILE_NAME,
        serializer = TimelineSerializer
    )

    override suspend fun getDataStore(
        context: Context,
        fileKey: String
    ): DataStore<RenderTimeline> {
        return context.dataStore
    }

    override fun getLocation(context: Context, fileKey: String): File {
        return File(context.filesDir, FILE_NAME)
    }

    private object TimelineSerializer : Serializer<RenderTimeline> {
        override val defaultValue: RenderTimeline = RenderTimeline(
            lessons = emptyList(),
            currentIndex = 0,
            day = LocalDateTime.now()
        )

        override suspend fun readFrom(input: InputStream): RenderTimeline {
            return try {
                Json.decodeFromString(
                    RenderTimeline.serializer(),
                    input.bufferedReader().use { it.readText() }
                )
            } catch (e: Exception) {
                defaultValue
            }
        }

        override suspend fun writeTo(t: RenderTimeline, output: OutputStream) {
            output.bufferedWriter().use {
                it.write(
                    Json.encodeToString(RenderTimeline.serializer(), t)
                )
            }
        }
    }
}