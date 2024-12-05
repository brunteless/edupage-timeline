package edu.brunteless.timeline

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import edu.brunteless.timeline.models.RenderLesson
import edu.brunteless.timeline.network.createHttpClient
import edu.brunteless.timeline.repositories.EdupageRepository
import edu.brunteless.timeline.ui.theme.TimelineTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            val scope = rememberCoroutineScope()
            val repository = remember {
                EdupageRepository(
                    httpClient = createHttpClient()
                )
            }
            var lessons by remember { mutableStateOf(emptyList<RenderLesson?>()) }

            TimelineTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        Column {
                            Button(
                                onClick = {
                                    scope.launch(Dispatchers.IO) {
                                        val login = repository.getCredentials()
                                        val timeline = repository.getTimeline(
                                            login, LocalDateTime.now()
                                        )
                                        lessons = timeline.lessons
                                    }
                                }
                            ) {
                                Text("Login")
                            }

                            LazyColumn {

                                items(lessons) {
                                    LessonTile(
                                        lesson = it,
                                        modifier = Modifier
                                            .padding(10.dp)
                                            .fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }


}

@Composable
fun LessonTile(
    lesson: RenderLesson?,
    modifier: Modifier = Modifier
) {
    when (lesson) {
        null -> Spacer(modifier = modifier.height(50.dp))
        else -> {
            Row(
                modifier = modifier
            ) {

                Text(
                    text = lesson.shortName,
                    style = MaterialTheme.typography.headlineMedium,
                    softWrap = false,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.width(80.dp)
                )

                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Teacher: " + lesson.teacherName,
                        style = MaterialTheme.typography.labelMedium
                    )
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = "Room: " + lesson.classroomShortName,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = lesson.startTime + " - " + lesson.endTime,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}