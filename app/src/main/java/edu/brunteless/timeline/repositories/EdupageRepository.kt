package edu.brunteless.timeline.repositories


import android.util.Log
import edu.brunteless.timeline.dtos.EdupageLoginDto
import edu.brunteless.timeline.dtos.EdupageTimelineDto
import edu.brunteless.timeline.models.RenderLesson
import edu.brunteless.timeline.models.RenderTimeline
import edu.brunteless.timeline.models.Tokens
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.headers
import io.ktor.http.HeadersBuilder
import io.ktor.http.HttpMessageBuilder
import io.ktor.http.Parameters
import io.ktor.http.parameters
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class EdupageRepository(
    private val httpClient: HttpClient
) {

    companion object {
        private const val LOGIN_URL = "https://login1.edupage.org/login/mauth"
        private const val USERNAME = ""
        private const val PASSWORD = ""
    }

    private val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val syncFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")


    suspend fun getCredentials() : Tokens {
        return httpClient
            .submitForm(LOGIN_URL, loginParameters) {
                loginHeaders
            }
            .body<EdupageLoginDto>()
            .toTokens()
    }

    suspend fun getTimeline(tokens: Tokens, day: LocalDateTime) : RenderTimeline {

        val timeline = httpClient
            .submitForm(tokens.timelineUrl, makeTimelineParameters(tokens, day)) {
                makeTimelineHeaders(tokens)
            }
            .body<EdupageTimelineDto>()
            .toLessonList(dateFormat.format(day))


        val (orderedTimeline, newIndex) = timeline.withProperOrder()

        return RenderTimeline(
            lessons = orderedTimeline,
            currentIndex = newIndex,
            day = day
        )
    }

    private fun List<RenderLesson>.withProperOrder(): Pair<MutableList<RenderLesson?>, Int> {
        val result = emptyList<RenderLesson?>().toMutableList()

        val newIndex = (first().period - 1).coerceAtLeast(0)

        var lastPeriod = 0

        forEach { lesson ->
            val gap = lesson.period - lastPeriod
            if (gap > 1) {
                repeat(gap - 1) {
                    result += null
                }
            }

            result += lesson
            lastPeriod = lesson.period
        }
        return Pair(result, newIndex)
    }

    private val Tokens.timelineUrl: String get() {
        return "https://${fromEdupage}.edupage.org/app/sync?mobile=2&mobileApp=2023.0.22&ESID=${esid}&lang=en&edid=${edid}&fromEdupage=login1&lang=en"
    }

    private fun HttpMessageBuilder.makeTimelineHeaders(tokens: Tokens) : HeadersBuilder {

        return headers {
            append("Accept", "application/json, text/javascript, */*; q=0.01")
            append("Accept-Encoding", "gzip, deflate, br")
            append("Accept-Language", "en-US,en;q=0.9,sk-SK;q=0.8,sk;q=0.7")
            append("Connection", "keep-alive")
            append("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
            append("Cookie", "ESID=${tokens.esid}; edid=${tokens.edid}")
            append("Host", "${tokens.fromEdupage}.edupage.org")
            append("Origin", "https://${tokens.fromEdupage}.edupage.org")
            append("Referer", "https://${tokens.fromEdupage}.edupage.org/app/Main?ESID=${tokens.esid}&mobile=2&mobileApp=2023.0.22&fromEdupage=login1&lang=en&edid=${tokens.edid}")
            append("sec-ch-ua", "Not A(Brand\";v=\"99\", \"Android WebView\";v=\"121\", \"Chromium\";v=\"121\"")
            append("sec-ch-ua-mobile", "?1")
            append("sec-ch-ua-platform", "Android")
            append("Sec-Fetch-Dest", "empty")
            append("Sec-Fetch-Mode", "cors")
            append("Sec-Fetch-Site", "same-origin")
            append("User-Agent", "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/121.0.6167.101 Mobile Safari/537.36")
            append("X-Requested-With", "XMLHttpRequest")
        }
    }

    private fun makeTimelineParameters(tokens: Tokens, day: LocalDateTime) : Parameters {

        val formattedDay = dateFormat.format(day)
        val formattedSync = syncFormat.format(LocalDateTime.now())

        Log.d("EdupageRepository", "Parsed: $formattedDay + $formattedSync")

        return parameters {
            append("tables", "{\"TtDbi\":{\"keys\":[\"\"]},\"DailyPlan\":{\"keys\":[\"${formattedDay}\"],\"lastsync\":\"${formattedSync}\"}}")
            append("actions", "{}")
            append("version", "2023.0.22")
            append("nativeVersion", "2.1.27")
            append("os", "Android")
            append("murl", "")
            append("fa2otp", "")
            append("fromEdupage", tokens.fromEdupage)
            append("lastsync0", "2024-11-28 21:29:33")
            append("lang", "en")
        }
    }

    private val loginParameters: Parameters
        get() {
            return parameters {
                append("m", USERNAME)
                append("h", PASSWORD)
                append("utyp", "")
                append("edupage", "")
                append("plgc", "")
                append("ajheslo", "1")
                append("hasujheslo", "1")
                append("ajportal", "1")
                append("ajportallogin", "1")
                append("mobileLogin", "1")
                append("version", "2023.0.22")
                append("fromEdupage", "login1")
                append("nativeVersion", "2.1.27")
                append("os", "Android")
            }
        }

    private val HttpMessageBuilder.loginHeaders: HeadersBuilder
        get() {
            return headers {
                append("Accept", "application/json, text/javascript, */*; q=0.01")
                append("Accept-Encoding", "gzip, deflate, br")
                append("Accept-Language", "en-US,en;q=0.9,sk-SK;q=0.8,sk;q=0.7")
                append("Connection", "keep-alive")
                append("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                append("Host", "login1.edupage.org")
                append("Origin", "https://login1.edupage.org")
                append("Referer", "https://login1.edupage.org/login/mauth")
                append(
                    "sec-ch-ua",
                    "Not A(Brand\";v=\"99\", \"Android WebView\";v=\"121\", \"Chromium\";v=\"121\""
                )
                append("sec-ch-ua-mobile", "?1")
                append("sec-ch-ua-platform", "Android")
                append("Sec-Fetch-Dest", "empty")
                append("Sec-Fetch-Mode", "cors")
                append("Sec-Fetch-Site", "same-origin")
                append(
                    "User-Agent",
                    "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/121.0.6167.101 Mobile Safari/537.36"
                )
                append("X-Requested-With", "XMLHttpRequest")
            }
        }
}