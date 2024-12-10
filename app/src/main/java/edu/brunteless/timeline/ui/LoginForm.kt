package edu.brunteless.timeline.ui


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import edu.brunteless.timeline.viewmodels.LoginViewModel
import edu.brunteless.timeline.viewmodels.ValidationState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject


@Composable
fun LoginForm(
    widgetId: Int,
    loginViewModel: LoginViewModel = koinInject<LoginViewModel>(),
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val loginState by loginViewModel.loginState.collectAsState()

    val scope = rememberCoroutineScope()

    LaunchedEffect(loginState.validation) {
        if (loginState.validation == ValidationState.Success) {
            scope.launch {
                delay(300L)
                onSuccess()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .imePadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(0.9f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Login",
                style = MaterialTheme.typography.headlineMedium
            )

            // Username TextField
            OutlinedTextField(
                value = loginState.username,
                onValueChange = loginViewModel::setUsername,
                label = { Text("Username") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                isError = loginState.validation == ValidationState.Error,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                )
            )

            // Password TextField
            OutlinedTextField(
                value = loginState.password,
                onValueChange = loginViewModel::setPassword,
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                isError = loginState.validation == ValidationState.Error,

            )

            // Login Button
            Button(
                onClick = {
                    when (loginState.validation) {
                        ValidationState.Loading -> return@Button
                        ValidationState.Success -> return@Button
                        else -> loginViewModel.login(context, widgetId)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {

                when (loginState.validation) {
                    ValidationState.Loading -> CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.background,
                        modifier = Modifier.size(24.dp)
                    )
                    ValidationState.Success -> Text("Setup successful")
                    else -> Text("Login")
                }

            }
        }
    }
}