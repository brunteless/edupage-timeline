package edu.brunteless.timeline.workers

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.work.*
import edu.brunteless.timeline.widget.TimelineGlanceStateDefinition
import edu.brunteless.timeline.widget.TimelineGlanceWidget
import edu.brunteless.timeline.models.Credentials
import edu.brunteless.timeline.models.RenderTimeline
import edu.brunteless.timeline.repositories.EdupageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.LocalDateTime


class TimelineWorker(
    private val context: Context,
    workerParameters: WorkerParameters,
) : CoroutineWorker(context, workerParameters), KoinComponent {

    companion object {
        private const val WORK_NAME_PREFIX = "timeline_worker"
        private const val KEY_NEXT_DAY = "${WORK_NAME_PREFIX}should_be_next_day"
        private const val KEY_WIDGET_ID = "${WORK_NAME_PREFIX}widget_id"
        private val CONSTRAINTS = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        fun stopWorkersForWidget(context: Context, glanceId: GlanceId) {
            val workManager = WorkManager.getInstance(context)
            val widgetId = GlanceAppWidgetManager(context).getAppWidgetId(glanceId)

            IndexUpdateWorker.stopWorkersForWidget(workManager, widgetId)
            workManager.cancelAllWorkByTag(getTagForWork(widgetId))
        }

        private fun getTagForWork(widgetId: Int) = "${WORK_NAME_PREFIX}_timeline_work_for_${widgetId}"

        fun scheduleTimelineUpdate(context: Context, glanceId: GlanceId) {
            val workManager = WorkManager.getInstance(context)
            val widgetId = GlanceAppWidgetManager(context).getAppWidgetId(glanceId)

            val immediateRequest = OneTimeWorkRequestBuilder<TimelineWorker>()
                .addTag(getTagForWork(widgetId))
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setConstraints(CONSTRAINTS)
                .setInputData(
                    Data.Builder()
                        .putInt(KEY_WIDGET_ID, widgetId)
                        .build()
                )
                .build()

            workManager.enqueueUniqueWork(
                getTagForWork(widgetId),
                ExistingWorkPolicy.REPLACE,
                immediateRequest
            )
        }
    }

    private val edupageRepository by inject<EdupageRepository>()
    private val glanceManager = GlanceAppWidgetManager(context)

    override suspend fun doWork(): Result  {
        val workManager = WorkManager.getInstance(context)

        val shouldBeNextDay = inputData.getBoolean(KEY_NEXT_DAY, false)
        val widgetId = inputData.getInt(KEY_WIDGET_ID, -1)

        IndexUpdateWorker.stopWorkersForWidget(workManager, widgetId)

        val timeline: RenderTimeline = getTimeline(shouldBeNextDay, widgetId)

        updateWidget(widgetId, timeline)
        scheduleNextUpdate(workManager, widgetId, timeline)
        scheduleIndexUpdates(workManager, widgetId, timeline)

        return Result.success()
    }

    private fun scheduleNextUpdate(workManager: WorkManager, widgetId: Int, timeline: RenderTimeline) {

        val lastLesson = timeline.lessons.last()!!

        val delay = timeline.getDelayDuration(lastLesson)

        val worker = OneTimeWorkRequestBuilder<TimelineWorker>()
            .addTag(getTagForWork(widgetId))
            .setInitialDelay(delay)
            .setConstraints(CONSTRAINTS)
            .setInputData(
                Data.Builder()
                    .putBoolean(KEY_NEXT_DAY, true)
                    .putInt(KEY_WIDGET_ID, widgetId)
                    .build()
            )
            .build()

        workManager.enqueueUniqueWork(
            getTagForWork(widgetId),
            ExistingWorkPolicy.APPEND,
            worker
        )
    }

    private suspend fun updateWidget(widgetId: Int, timeline: RenderTimeline) {

        val glanceId = glanceManager.getGlanceIdBy(widgetId)

        updateAppWidgetState(
            context = context,
            glanceId = glanceId,
            definition = TimelineGlanceStateDefinition,
        ) {
            timeline
        }

        TimelineGlanceWidget().update(context, glanceId)
    }

    private suspend fun getTimeline(shouldBeNextDay: Boolean, widgetId: Int): RenderTimeline {
        var timeline: RenderTimeline?
        lateinit var credentials: Credentials

        withContext(Dispatchers.IO) {
            val glanceId = glanceManager.getGlanceIdBy(widgetId)
            val state = getAppWidgetState(
                context,
                TimelineGlanceStateDefinition,
                glanceId
            )

            credentials = state.credentials!!

            val tokens = edupageRepository.getCredentials(
                credentials.username,
                credentials.password
            )

            var currentDay = getCurrentDay(shouldBeNextDay, state)

            do {

                timeline = edupageRepository.getTimeline(tokens, currentDay)
                currentDay = currentDay.plusDays(1)

            } while (timeline?.isNotLatest() != false)
        }

        return timeline!!.copy(credentials = credentials)
    }

    private fun scheduleIndexUpdates(workManager: WorkManager, widgetId: Int, timeline: RenderTimeline) {

        val lessons = timeline.lessons
            .withIndex()
            .filter { it.value != null }
            .dropLast(1)
            .toList()

        for (i in 0..<lessons.size) {
            val delay = timeline.getDelayDuration(lessons[i].value!!)

            IndexUpdateWorker.scheduleIndexUpdate(
                workManager,
                widgetId,
                lessons[i+1].index,
                delay
            )
        }
    }

    private fun getCurrentDay(shouldBeNextDay: Boolean, state: RenderTimeline): LocalDateTime {
        return state.day.apply {
            if (shouldBeNextDay || state.isNotLatest()) plusDays(1)
        }
    }
}