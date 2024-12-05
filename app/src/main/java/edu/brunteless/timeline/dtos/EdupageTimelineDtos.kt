package edu.brunteless.timeline.dtos

import edu.brunteless.timeline.models.*
import kotlinx.serialization.Serializable

@Serializable
data class EdupageTimelineDto(
    val status: String,
    val versionStatus: String,
    val actions: List<String>,
    val lastsync: String,
    val fromEdupage: String,
    val fromUser: String,
    val schoolYear: Int,
    val tables: Tables,
    val invalidateTables: InvalidateTables,
    val requestReview: RequestReview
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
    val status: String,
    val lastsync: String,
    val data: Map<String, TtDbiData>,
    val keyinfo: Map<String, TtDbiKeyInfo>,
    val validity: Int
)

@Serializable
data class TtDbiData(
    val teachers: Map<String, Teacher>,
    val classes: Map<String, SchoolClass>,
    val subjects: Map<String, Subject>,
    val classrooms: Map<String, Classroom>,
    val students: Map<String, Student>,
    val terms: List<Term>,
    val weeks: List<Week>,
    val days: List<Day>,
    val periods: List<Period>,
    val breaks: List<Break>
)

@Serializable
data class Teacher(
    val id: String,
    val firstname: String,
    val lastname: String,
    val short: String,
    val color: String
) {
    val fullName: String get() = firstname.trim() + " " + lastname.trim()
}

@Serializable
data class SchoolClass(
    val id: String,
    val short: String,
    val name: String,
    val color: String
)

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
data class Student(
    val id: String,
    val classid: String,
    val firstname: String,
    val lastname: String
)

@Serializable
data class Term(
    val name: String,
    val short: String,
    val id: String
)

@Serializable
data class Week(
    val name: String,
    val short: String,
    val id: String
)

@Serializable
data class Day(
    val name: String,
    val short: String,
    val id: String
)

@Serializable
data class Period(
    val name: String,
    val short: String,
    val starttime: String,
    val endtime: String,
    val id: String
)

@Serializable
data class Break(
    val name: String,
    val short: String,
    val starttime: String,
    val endtime: String,
    val id: String
)

@Serializable
data class TtDbiKeyInfo(
    val validity: Long,
    val lastsync: String
)

@Serializable
data class DailyPlan(
    val status: String,
    val lastsync: String,
    val data: Map<String, DailyPlanData>,
    val keyinfo: Map<String, DailyPlanKeyInfo>,
    val validity: Int
)

@Serializable
data class DailyPlanData(
    val plan: List<Lesson>,
    val student_absents: List<String>,
    val tt_num: Int,
    val tt_day: Int,
    val tt_week: Int,
    val tt_term: Int
)

@Serializable
data class Lesson(
    val type: String,
    val uniperiod: String,
    val header: List<HeaderItem>,
    val infos: List<Info>? = null,
    val subjectid: String? = null,
    val classids: List<String>,
    val teacherids: List<String>,
    val classroomids: List<String>,
    val groupnames: List<String>,
    val lid: String? = null,
    val groupsubjectids: List<String>,
    val periodorbreak: String,
    val starttime: String? = null,
    val endtime: String? = null,
    val period: String? = null
)

@Serializable
data class HeaderItem(
    val item: HeaderItemDetail
)

@Serializable
data class HeaderItemDetail(
    val subjectid: String,
    val changes: List<Change>
)

@Serializable
data class Change(
    val column: String,
    val old: String
)

@Serializable
data class Info(
    val type: String? = null,
    val texts: List<Text>
)

@Serializable
data class Text(
    val text: String,
    val item: InfoItem? = null
)

@Serializable
data class InfoItem(
    val teacherids: List<String>? = null,
    val classroomids: List<String>? = null
)


@Serializable
data class DailyPlanKeyInfo(
    val validity: Long,
    val lastsync: String
)


@Serializable
data class InvalidateTables(
    val TestmeLicenses: TestmeLicenses
)

@Serializable
data class TestmeLicenses(
    val keys: List<String>
)

@Serializable
data class RequestReview(
    val us: String?,
    val nd: String?,
    val show: Boolean
)

