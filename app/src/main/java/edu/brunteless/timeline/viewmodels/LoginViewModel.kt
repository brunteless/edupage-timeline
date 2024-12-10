package edu.brunteless.timeline.viewmodels

import android.content.Context
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.brunteless.timeline.models.Credentials
import edu.brunteless.timeline.models.RenderTimeline
import edu.brunteless.timeline.models.Tokens
import edu.brunteless.timeline.repositories.EdupageRepository
import edu.brunteless.timeline.widget.TimelineGlanceStateDefinition
import edu.brunteless.timeline.widget.TimelineGlanceWidget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import java.time.LocalDateTime


enum class ValidationState {
    None, Loading, Success, Error
}

data class LoginState(
    val username: String = "",
    val password: String = "",
    val validation: ValidationState = ValidationState.None
)


class LoginViewModel(
    private val edupageRepository: EdupageRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow(LoginState())
    val loginState = _loginState.asStateFlow()

    fun login(context: Context, widgetId: Int) {

        val state = _loginState.updateAndGet {
            it.copy(validation = ValidationState.Loading)
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = edupageRepository.getCredentials(state.username, state.password)

                _loginState.update {
                    it.copy(validation = ValidationState.Success)
                }

                val id = GlanceAppWidgetManager(context).getGlanceIdBy(widgetId)

                updateAppWidgetState(context, TimelineGlanceStateDefinition, id) {
                    createNewTimeline(state, response)
                }

                TimelineGlanceWidget().update(context, id)

            } catch (e: Exception) {
                Log.d("LoginViewModel", e.message, e.cause)
                _loginState.update {
                    it.copy(validation = ValidationState.Error)
                }
            }
        }
    }

    private fun createNewTimeline(state: LoginState, response: Tokens) = RenderTimeline(
        lessons = emptyList(),
        currentIndex = 0,
        day = LocalDateTime.now(),
        credentials = Credentials(
            username = state.username,
            password = state.password,
            firstname = response.firstname,
            lastname = response.lastname,
            userId = response.userId
        )
    )

    fun setUsername(newName: String) {
        _loginState.update {
            it.copy(username = newName)
        }
    }

    fun setPassword(newPassword: String) {
        _loginState.update {
            it.copy(password = newPassword)
        }
    }

}