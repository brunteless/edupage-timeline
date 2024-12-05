package edu.brunteless.timeline.glance

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.work.*
import edu.brunteless.timeline.models.TimelineGlanceStateDefinition
import java.time.Duration


class IndexUpdateWorker(
    private val context: Context,
    workerParameters: WorkerParameters,
) : CoroutineWorker(context, workerParameters) {

    companion object {
        private const val WORK_NAME_PREFIX = "index_update_worker"
        private const val IMMEDIATE_WORK_NAME = "${WORK_NAME_PREFIX}_immediate"
        private const val KEY_NEW_INDEX = "${WORK_NAME_PREFIX}should_be_next_day"

        fun stopAllIndexUpdates(workManager: WorkManager) {
            workManager.cancelAllWorkByTag(IMMEDIATE_WORK_NAME)
        }

        fun scheduleIndexUpdate(workManager: WorkManager, newIndex: Int, delay: Duration) {

            if (delay.isNegative) return

            val immediateRequest = OneTimeWorkRequestBuilder<IndexUpdateWorker>()
                .addTag(IMMEDIATE_WORK_NAME)
                .setInitialDelay(delay)
                .setInputData(
                    Data.Builder()
                        .putInt(KEY_NEW_INDEX, newIndex)
                        .build()
                )
                .build()

            workManager.enqueueUniqueWork(
                IMMEDIATE_WORK_NAME,
                ExistingWorkPolicy.APPEND,
                immediateRequest
            )
        }
    }

    private val glanceManager = GlanceAppWidgetManager(context)

    override suspend fun doWork(): Result  {

        val newIndex = inputData.getInt(KEY_NEW_INDEX, 0)

        updateAllWidgets(newIndex)

        return Result.success()
    }

    private suspend fun updateAllWidgets(newIndex: Int) {

        val glanceIds = glanceManager.getGlanceIds(TimelineGlanceWidget::class.java)

        glanceIds.forEach { glanceId ->
            updateAppWidgetState(
                context = context,
                glanceId = glanceId,
                definition = TimelineGlanceStateDefinition,
            ) {
                it.copy(currentIndex = newIndex)
            }
        }

        TimelineGlanceWidget().updateAll(context)
    }
}