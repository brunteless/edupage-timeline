package edu.brunteless.timeline.glance


import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.CircularProgressIndicator
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.lazy.GridCells
import androidx.glance.appwidget.lazy.LazyVerticalGrid
import androidx.glance.appwidget.lazy.itemsIndexed
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import edu.brunteless.timeline.models.RenderLesson
import edu.brunteless.timeline.models.RenderTimeline
import edu.brunteless.timeline.models.TimelineGlanceStateDefinition
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class TimelineGlanceWidget : GlanceAppWidget() {

    companion object {
        val indexParameter = ActionParameters.Key<Int>("new_index")
    }

    override val sizeMode: SizeMode = SizeMode.Exact

    override val stateDefinition: GlanceStateDefinition<*>
        get() = TimelineGlanceStateDefinition

    private val dayFormat = DateTimeFormatter.ofPattern("EEEE, dd.MM.yyyy")

    override suspend fun onDelete(context: Context, glanceId: GlanceId) {
        super.onDelete(context, glanceId)
        TimelineWorker.stopAllTimelineUpdates(context)
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val timeline = currentState<RenderTimeline>()

            val isLoaded = timeline.lessons.isNotEmpty()

            GlanceTheme {
                Box(
                    modifier = GlanceModifier
                        .padding(16.dp, 0.dp, 16.dp, 16.dp)
                        .fillMaxSize()
                        .appWidgetBackground()
                        .background(GlanceTheme.colors.background),
                    contentAlignment = if (isLoaded) Alignment.BottomEnd else Alignment.Center
                ) {
                    if (isLoaded) {

                        val lessons = timeline.lessons

                        Column(
                            modifier = GlanceModifier.fillMaxSize()
                        ) {
                            LessonHeader(
                                lesson = lessons[timeline.currentIndex]!!,
                                day = timeline.day
                            )

                            LessonBody(
                                lessons = lessons,
                                currentIndex = timeline.currentIndex
                            )
                        }
                        Box(
                            contentAlignment = Alignment.BottomEnd
                        ) {
                            Button(
                                text = "R",
                                onClick = {
                                    TimelineWorker.scheduleTimelineUpdate(context)
                                },
                                modifier = GlanceModifier.size(40.dp)
                            )

                        }
                    } else {
                        CircularProgressIndicator(
                            color = GlanceTheme.colors.onPrimaryContainer
                        )
                        SideEffect {
                            TimelineWorker.scheduleTimelineUpdate(context)
                        }
                    }

                }
            }
        }
    }


    @Composable
    fun LessonHeader(
        lesson: RenderLesson,
        day: LocalDateTime
    ) {
        Spacer(modifier = GlanceModifier.height(3.dp))

        Text(
            text = dayFormat.format(day),
            style = TextStyle(
                fontSize = 16.sp,
                color = GlanceTheme.colors.onPrimaryContainer,
                textAlign = TextAlign.Center
            ),
            modifier = GlanceModifier.fillMaxWidth()
        )

        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .height(50.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = lesson.shortName,
                style = TextStyle(
                    fontSize = 32.sp,
                    color = GlanceTheme.colors.onPrimaryContainer
                ),
                maxLines = 1,
            )

            Spacer(
                modifier = GlanceModifier.width(8.dp)
            )

            Column(
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                Text(
                    text = "Teacher: " + lesson.teacherName,
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = GlanceTheme.colors.onPrimaryContainer
                    )
                )
                Text(
                    text = "Room: " + lesson.classroomShortName,
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = GlanceTheme.colors.onPrimaryContainer
                    )
                )
            }


            Box(
                modifier = GlanceModifier.fillMaxSize(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = lesson.startTime + '\n' + lesson.endTime,
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = GlanceTheme.colors.onPrimaryContainer,
                        textAlign = TextAlign.Center,
                    ),
                    maxLines = 2
                )
            }
        }
    }

    @Composable
    fun LessonBody(
        lessons: List<RenderLesson?>,
        currentIndex: Int
    ) {
        LazyVerticalGrid(
            gridCells = GridCells.Fixed(5)
        ) {
            itemsIndexed(lessons) { index, lesson ->
                val buttonModifier = GlanceModifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(
                        if (index == currentIndex) GlanceTheme.colors.onPrimary
                        else GlanceTheme.colors.primaryContainer
                    )

                when (lesson) {
                    null -> Spacer(buttonModifier)
                    else -> {
                        LessonTile(
                            lesson = lesson,
                            index = index,
                            buttonModifier = buttonModifier
                        )
                    }
                }
            }
        }
    }



    @Composable
    fun LessonTile(
        lesson: RenderLesson,
        index: Int,
        buttonModifier: GlanceModifier
    ) {
        Box (
            contentAlignment = Alignment.Center,
            modifier = buttonModifier
                .clickable(
                    actionRunCallback<ChangeLessonAction>(
                        actionParametersOf(indexParameter to index)
                    )
                ),
        ) {
            Text(
                text = lesson.period.toString() + '\n' + lesson.shortName,
                style = TextStyle(
                    fontSize = 14.sp,
                    color = GlanceTheme.colors.onPrimaryContainer,
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}

class ChangeLessonAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {

        val newIndex = parameters[TimelineGlanceWidget.indexParameter]!!

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

class TimelineGlanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TimelineGlanceWidget()
}