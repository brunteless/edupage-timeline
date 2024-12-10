package edu.brunteless.timeline.widget


import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalGlanceId
import androidx.glance.action.clickable
import androidx.glance.appwidget.CircularProgressIndicator
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.components.CircleIconButton
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
import edu.brunteless.timeline.R
import edu.brunteless.timeline.models.RenderLesson
import edu.brunteless.timeline.models.RenderTimeline
import edu.brunteless.timeline.workers.TimelineWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter


class TimelineGlanceWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Exact

    override val stateDefinition: GlanceStateDefinition<*>
        get() = TimelineGlanceStateDefinition

    private val dayFormat = DateTimeFormatter.ofPattern("EEEE, dd.MM.yyyy")

    override suspend fun onDelete(context: Context, glanceId: GlanceId) {
        TimelineWorker.stopWorkersForWidget(context, glanceId)
        super.onDelete(context, glanceId)
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {

        provideContent {

            val timeline = currentState<RenderTimeline>()
            val isLoaded = timeline.lessons.isNotEmpty()

            GlanceTheme {
                Box(
                    modifier = GlanceModifier
                        .padding(12.dp)
                        .fillMaxSize()
                        .appWidgetBackground()
                        .background(GlanceTheme.colors.background),
                    contentAlignment = if (isLoaded) Alignment.BottomEnd else Alignment.Center
                ) {
                    when {
                        timeline.credentials == null -> NoCredentials()
                        timeline.lessons.isEmpty() -> NoLessons(context, id)
                        isLoaded -> TimelineContent(context, id, timeline)
                        else -> ErrorMessage()
                    }
                }
            }
        }
    }

    @Composable
    private fun ErrorMessage() {
        Text(
            text = "Unhandled illegal state!",
            style = TextStyle(
                color = GlanceTheme.colors.onErrorContainer
            )
        )
    }

    @Composable
    private fun NoLessons(context: Context, id: GlanceId) {
        CircularProgressIndicator(
            color = GlanceTheme.colors.onPrimaryContainer
        )
        SideEffect {
            TimelineWorker.scheduleTimelineUpdate(context, id)
        }
    }

    @Composable
    private fun NoCredentials() {
        Text(
            "No credentials were provided.",
            style = TextStyle(
                color = GlanceTheme.colors.onPrimaryContainer
            )
        )
    }

    @Composable
    private fun TimelineContent(
        context: Context,
        id: GlanceId,
        timeline: RenderTimeline
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize()
        ) {
            LessonHeader(timeline = timeline)
            LessonBody(
                lessons = timeline.lessons,
                currentIndex = timeline.currentIndex
            )
        }
        Box(
            contentAlignment = Alignment.BottomEnd
        ) {
            CircleIconButton(
                imageProvider = ImageProvider(resId = R.drawable.icon_refresh),
                onClick = {
                    TimelineWorker.scheduleTimelineUpdate(context, id)
                },
                contentDescription = "Refresh EduPage Timeline",
                modifier = GlanceModifier.size(32.dp)
            )
        }
    }


    @Composable
    private fun LessonHeader(
        timeline: RenderTimeline
    ) {
        val lesson = timeline.lessons[timeline.currentIndex]!!

        Text(
            text = dayFormat.format(timeline.day),
            style = TextStyle(
                fontSize = 14.sp,
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
    private fun LessonBody(
        lessons: List<RenderLesson?>,
        currentIndex: Int,
    ) {

        val context = LocalContext.current
        val id = LocalGlanceId.current
        val scope = rememberCoroutineScope()

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
                            buttonModifier = buttonModifier
                        ) {
                            scope.launch(Dispatchers.IO) {
                                changeLessonIndex(context, id, index)
                            }
                        }
                    }
                }
            }
        }
    }



    @Composable
    private fun LessonTile(
        lesson: RenderLesson,
        buttonModifier: GlanceModifier,
        onClick: () -> Unit
    ) {
        Box (
            contentAlignment = Alignment.Center,
            modifier = buttonModifier.clickable(onClick),
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

    private suspend fun changeLessonIndex(context: Context, id: GlanceId, newIndex: Int) {
        updateAppWidgetState(
            context = context,
            glanceId = id,
            definition = TimelineGlanceStateDefinition,
        ) {
            it.copy(currentIndex = newIndex)
        }

        TimelineGlanceWidget().update(context, id)
    }
}
