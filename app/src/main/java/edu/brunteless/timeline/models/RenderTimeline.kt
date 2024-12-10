@file:UseSerializers(LocalDateTimeSerializer::class)
package edu.brunteless.timeline.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.time.Duration
import java.time.LocalDateTime


@Serializable
data class RenderTimeline(
    val lessons: List<RenderLesson?>,
    val currentIndex: Int,
    val day: LocalDateTime,
    val credentials: Credentials? = null
) {

    fun getDelayDuration(lesson: RenderLesson) : Duration {
        return Duration.between(
            LocalDateTime.now(),
            day.lessonEndTime(lesson)
        )
    }


    fun isNotLatest() = lessons.isEmpty() || getDelayDuration(lessons.last()!!).isNegative


    private fun LocalDateTime.lessonEndTime(lesson: RenderLesson) : LocalDateTime {
        val (hours, minutes) = lesson.endTime.split(':').map { it.toInt() }.toIntArray()
        return this
            .withHour(hours)
            .withMinute(minutes)
            .withSecond(0)
    }
}
