package edu.brunteless.timeline.workers

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.work.*
import edu.brunteless.timeline.widget.TimelineGlanceStateDefinition
import edu.brunteless.timeline.widget.TimelineGlanceWidget
import java.time.Duration


class IndexUpdateWorker(
    private val context: Context,
    workerParameters: WorkerParameters,
) : CoroutineWorker(context, workerParameters) {

    companion object {
        private const val WORK_NAME_PREFIX = "index_update_worker"
        private const val KEY_NEW_INDEX = "${WORK_NAME_PREFIX}_new_index_value"
        private const val KEY_WIDGET_ID = "${WORK_NAME_PREFIX}_widget_id"

        fun stopWorkersForWidget(workManager: WorkManager, widgetId: Int) {
            workManager.cancelAllWorkByTag(getTagForWork(widgetId))
        }

        private fun getTagForWork(widgetId: Int) = "${WORK_NAME_PREFIX}_index_update_for_${widgetId}"

        fun scheduleIndexUpdate(
            workManager: WorkManager,
            widgetId: Int,
            newIndex: Int,
            delay: Duration
        ) {

            if (delay.isNegative) return

            val immediateRequest = OneTimeWorkRequestBuilder<IndexUpdateWorker>()
                .addTag(getTagForWork(widgetId))
                .setInitialDelay(delay)
                .setInputData(
                    Data.Builder()
                        .putInt(KEY_NEW_INDEX, newIndex)
                        .putInt(KEY_WIDGET_ID, widgetId)
                        .build()
                )
                .build()

            workManager.enqueueUniqueWork(
                getTagForWork(widgetId),
                ExistingWorkPolicy.APPEND,
                immediateRequest
            )
        }
    }

    private val glanceManager = GlanceAppWidgetManager(context)

    override suspend fun doWork(): Result  {

        val newIndex = inputData.getInt(KEY_NEW_INDEX, -1)
        val widgetId = inputData.getInt(KEY_WIDGET_ID, -1)

        updateAllWidgets(widgetId, newIndex)

        return Result.success()
    }

    private suspend fun updateAllWidgets(widgetId: Int, newIndex: Int) {

        val glanceId = glanceManager.getGlanceIdBy(widgetId)

        updateAppWidgetState(
            context = context,
            glanceId = glanceId,
            definition = TimelineGlanceStateDefinition,
        ) {
            it.copy(currentIndex = newIndex)
        }

        TimelineGlanceWidget().update(context, glanceId)
    }
}