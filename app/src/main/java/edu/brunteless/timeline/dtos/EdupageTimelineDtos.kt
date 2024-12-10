package edu.brunteless.timeline.dtos

import edu.brunteless.timeline.models.*
import kotlinx.serialization.Serializable

@Serializable
data class EdupageTimelineDto(
    val status: String? = null,
    val versionStatus: String? = null,
    val fromEdupage: String? = null,
    val fromUser: String? = null,
    val tables: Tables
) {
    fun toLessonList(formattedDay: String) : List<RenderLesson> =
        tables.DailyPlan.data[formattedDay]!!.plan
            .filter { it.type == "lesson" }
            .map { it.toRenderClassicLesson() }
            .sortedBy { it.period }
            .toList()

    
    private fun Lesson.toRenderClassicLesson() : RenderLesson {
        val teacher = tables.TtDbi.data[""]!!.teachers[teacherids.first()]
        val subject = tables.TtDbi.data[""]!!.subjects[subjectid]
        val classroom = tables.TtDbi.data[""]!!.classrooms[classroomids.first()]

        return RenderLesson(
            shortName = subject?.short ?: "???",
            startTime = starttime!!,
            endTime = endtime!!,
            period = uniperiod.toInt(),
            classroomShortName = classroom?.short ?: "Unknown",
            teacherName = teacher?.fullName ?: "Unknown"
        )
    }
}

@Serializable
data class Tables(
    val TtDbi: TtDbi,
    val DailyPlan: DailyPlan,
)

@Serializable
data class TtDbi(
    val data: Map<String, TtDbiData>
)

@Serializable
data class TtDbiData(
    val teachers: Map<String, Teacher>,
    val subjects: Map<String, Subject>,
    val classrooms: Map<String, Classroom>,
)

@Serializable
data class Teacher(
    val id: String,
    val firstname: String,
    val lastname: String,
    val short: String,
    val color: String
) {
    val fullName: String get() = firstname.trim() + ' ' + lastname.trim()
}

@Serializable
data class Subject(
    val id: String,
    val short: String,
    val name: String,
    val color: String
)

@Serializable
data class Classroom(
    val id: String,
    val short: String,
    val name: String,
    val color: String
)

@Serializable
data class DailyPlan(
    val data: Map<String, DailyPlanData>,
)

@Serializable
data class DailyPlanData(
    val plan: List<Lesson>,
)

@Serializable
data class Lesson(
    val type: String,
    val uniperiod: String,
    val subjectid: String? = null,
    val teacherids: List<String> = emptyList(),
    val classroomids: List<String> = emptyList(),
    val starttime: String? = null,
    val endtime: String? = null,
    val period: String? = null
)
