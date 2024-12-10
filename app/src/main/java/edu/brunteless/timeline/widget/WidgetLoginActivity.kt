package edu.brunteless.timeline.widget


import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import edu.brunteless.timeline.ui.LoginForm
import org.koin.compose.KoinContext
import edu.brunteless.timeline.ui.theme.TimelineTheme



class WidgetLoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)

        enableEdgeToEdge()
        setContent {
            KoinContext {
                TimelineTheme {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        Box(
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            LoginForm(widgetId) {
                                onSuccessfulEdupageSetup(widgetId)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun onSuccessfulEdupageSetup(widgetId: Int) {
        val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        setResult(RESULT_OK, resultValue)
        finish()
    }
}