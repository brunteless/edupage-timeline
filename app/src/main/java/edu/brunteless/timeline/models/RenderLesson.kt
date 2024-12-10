package edu.brunteless.timeline.models

import kotlinx.serialization.Serializable


@Serializable
data class RenderLesson(
    val shortName: String,
    val startTime: String,
    val endTime: String,
    val period: Int,

    val classroomShortName: String,
    val teacherName: String
)
