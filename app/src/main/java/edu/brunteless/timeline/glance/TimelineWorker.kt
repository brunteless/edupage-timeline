package edu.brunteless.timeline.glance

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.work.*
import edu.brunteless.timeline.models.RenderTimeline
import edu.brunteless.timeline.models.TimelineGlanceStateDefinition
import edu.brunteless.timeline.network.createHttpClient
import edu.brunteless.timeline.repositories.EdupageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime


class TimelineWorker(
    private val context: Context,
    workerParameters: WorkerParameters,
) : CoroutineWorker(context, workerParameters) {

    companion object {
        private const val LOGGING_TAG = "TimelineWorker"
        private const val WORK_NAME_PREFIX = "timeline_worker"
        private const val IMMEDIATE_WORK_NAME = "${WORK_NAME_PREFIX}_immediate"
        private const val KEY_NEXT_DAY = "${WORK_NAME_PREFIX}should_be_next_day"
        private val CONSTRAINTS = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        fun stopAllTimelineUpdates(context: Context) {
            val workManager = WorkManager.getInstance(context)
            workManager.cancelAllWork()
        }

        fun scheduleTimelineUpdate(context: Context) {
            val workManager = WorkManager.getInstance(context)

            val immediateRequest = OneTimeWorkRequestBuilder<TimelineWorker>()
                .addTag(IMMEDIATE_WORK_NAME)
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setConstraints(CONSTRAINTS)
                .build()

            workManager.enqueueUniqueWork(
                IMMEDIATE_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                immediateRequest
            )
        }
    }

    private val edupageRepository = EdupageRepository(
        httpClient = createHttpClient()
    )

    private val glanceManager = GlanceAppWidgetManager(context)

    override suspend fun doWork(): Result  {

        val workManager = WorkManager.getInstance(context)
        IndexUpdateWorker.stopAllIndexUpdates(workManager)

        val shouldBeNextDay = inputData.getBoolean(KEY_NEXT_DAY, false)
        val timeline: RenderTimeline = getTimeline(shouldBeNextDay)

        updateAllWidgets(timeline)
        scheduleNextUpdate(workManager, timeline)

        return Result.success()
    }

    private fun scheduleNextUpdate(workManager: WorkManager, timeline: RenderTimeline) {

        val lastLesson = timeline.lessons.last()!!

        val delay = timeline.getDelayDuration(lastLesson)
        val syncWorker = OneTimeWorkRequestBuilder<TimelineWorker>()
            .setInitialDelay(delay)
            .setConstraints(CONSTRAINTS)
            .setInputData(
                Data.Builder()
                    .putBoolean(KEY_NEXT_DAY, true)
                    .build()
            )
            .build()

        workManager.enqueueUniqueWork(
            IMMEDIATE_WORK_NAME,
            ExistingWorkPolicy.APPEND,
            syncWorker
        )
    }

    private suspend fun updateAllWidgets(timeline: RenderTimeline) {

        val glanceIds = glanceManager.getGlanceIds(TimelineGlanceWidget::class.java)

        glanceIds.forEach { glanceId ->
            updateAppWidgetState(
                context = context,
                glanceId = glanceId,
                definition = TimelineGlanceStateDefinition,
            ) {
                timeline
            }
        }

        TimelineGlanceWidget().updateAll(context)
    }

    private suspend fun getTimeline(shouldBeNextDay: Boolean): RenderTimeline {
        var timeline: RenderTimeline?

        withContext(Dispatchers.IO) {
            val id = glanceManager.getGlanceIds(TimelineGlanceWidget::class.java).first()
            val state = getAppWidgetState(
                context,
                TimelineGlanceStateDefinition,
                id
            )

            val tokens = edupageRepository.getCredentials()

            var currentDay = getCurrentDay(shouldBeNextDay, state)

            do {

                timeline = edupageRepository.getTimeline(tokens, currentDay)
                currentDay = currentDay.plusDays(1)

            } while (timeline?.isNotLatest != false)
        }

        return timeline!!
    }

    private fun getCurrentDay(
        shouldBeNextDay: Boolean,
        state: RenderTimeline
    ): LocalDateTime {
        return state.day.apply {
            if (shouldBeNextDay || state.isNotLatest) plusDays(1)
        }
    }
}