package edu.brunteless.timeline.dtos

import edu.brunteless.timeline.models.Tokens
import kotlinx.serialization.Serializable


@Serializable
data class EdupageLoginDto(
    val users: List<User>,
    val edid: String
) {
    fun toTokens() : Tokens {
        val user = users.first()
        return Tokens(
            edid = edid,
            esid = user.esid,
            fromEdupage = user.edupage
        )
    }
}

@Serializable
data class User(
    val userid: String,
    val typ: String,
    val edupage: String,
    val edumeno: String,
    val eduheslo: String,
    val firstname: String,
    val lastname: String,
    val esid: String,
    val appdata: AppData,
    val portal_userid: String,
    val portal_email: String?,
    val need2fa: Boolean?,
    val forceActivate2fa: Boolean
)

@Serializable
data class AppData(
    val loggedUser: String,
    val loggedChild: Int,
    val loggedUserName: String,
    val lang: String,
    val gender: String,
    val edupage: String,
    val school_type: Int,
    val timezonediff: Int,
    val school_country: String,
    val schoolyear_turnover: String,
    val firstDayOfWeek: Int,
    val sort_name_col: String,
    val selectedYear: Int,
    val autoYear: Int,
    val year_turnover: String,
    val vyucovacieDni: List<Boolean>,
    val server: String,
    val syncIntervalMultiplier: Int,
    val ascspl: String?,
    val jePro: Boolean,
    val isRestrictedEdupage: Boolean,
    val jeZUS: Boolean,
    val rtl: Boolean,
    val rtlAvailable: Boolean,
    val uidsgn: String,
    val webpageadmin: Boolean,
    val edurequestProps: EduRequestProps,
    val gsechash: String,
    val email: String?,
    val isOrbit: Boolean,
    val userrights: List<String>,
    val isAdult: Boolean
)

@Serializable
data class EduRequestProps(
    val edupage: String,
    val lang: String,
    val school_name: String,
    val school_country: String,
    val school_state: String,
    val schoolyear_turnover: String,
    val year_auto: Int,
    val year_auto_date: String,
    val custom_turnover: List<String>,
    val firstDayOfWeek: Int,
    val weekendDays: List<Int>,
    val timezone: String,
    val sort_name_col: String,
    val dtFormats: DtFormats,
    val jsmodulemode: String,
    val loggedUser: String,
    val loggedUserRights: List<String>,
    val isAsc: Boolean,
    val isAgenda: Boolean
)

@Serializable
data class DtFormats(
    val date: String,
    val time: String
)
