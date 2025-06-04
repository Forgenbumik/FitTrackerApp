package com.example.fittrackerapp.uielements.authorization

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fittrackerapp.AuthCondition
import com.example.fittrackerapp.ui.theme.Blue
import com.example.fittrackerapp.ui.theme.DarkTeal
import com.example.fittrackerapp.ui.theme.FirstTeal
import com.example.fittrackerapp.ui.theme.FitTrackerAppTheme
import com.example.fittrackerapp.uielements.downloaddata.DownloadDataActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.actionCodeSettings
import kotlinx.coroutines.launch

class AuthorizationActivity: ComponentActivity() {



    private val viewModel: AuthorizationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()
        val actionCodeSettings = actionCodeSettings  {
            // URL you want to redirect back to. The domain (www.example.com) for this
            // URL must be whitelisted in the Firebase Console.
            url = "https://fittrackerapp-54b70fdb.firebaseapp.com"
            // This must be true
            handleCodeInApp = true
            setAndroidPackageName(
                "com.example.android",
                true, // installIfNotAvailable
                "12", // minimumVersion
            )
        }

        setContent {
            FitTrackerAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(Modifier.padding(innerPadding),
                onSignInClick = { viewModel.signIn(this) },
                onSignUpClick = { viewModel.signUp() },
                        checkUserVerification = { viewModel.checkUserVerification(this) },
                        sendEmail = { email -> viewModel.sendEmail(email, actionCodeSettings) })
                }
            }
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.authCondition.collect { result ->
                    when (result) {
                        AuthCondition.SUCCESS -> {
                            startActivity(Intent(this@AuthorizationActivity, DownloadDataActivity::class.java))
                            finish()
                        }
                        AuthCondition.ERROR -> {
                            Toast.makeText(this@AuthorizationActivity, "Ошибка", Toast.LENGTH_SHORT).show()
                        }
                        AuthCondition.WAIT_FOR_EMAIL_VERIFICATION -> {
                            Toast.makeText(this@AuthorizationActivity, "Подтвердите email, прежде чем войти", Toast.LENGTH_LONG).show()
                        }
                        AuthCondition.SIGN_IN, AuthCondition.SIGN_UP -> Unit
                    }
                }
            }
        }
    }


}

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    onSignInClick: () -> Unit,
    onSignUpClick: () -> Unit,
    checkUserVerification: () -> Unit,
    sendEmail: (String) -> Unit,
    viewModel: AuthorizationViewModel = viewModel()
) {
    val condition = viewModel.authCondition.collectAsState().value
    val email = viewModel.email.collectAsState()
    val name = viewModel.name.collectAsState()
    val firstPassword = viewModel.firstPassword.collectAsState()
    val secondPassword = viewModel.secondPassword.collectAsState()



    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF181925))
            .padding(24.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkTeal),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .align(Alignment.Center)
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (condition == AuthCondition.SIGN_IN) "Вход" else "Регистрация",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                EmailField(email.value) { viewModel.setEmail(it) }

                if (condition == AuthCondition.SIGN_UP) {
                    NameField(name.value) { viewModel.setName(it) }
                }

                FirstPasswordField(firstPassword.value) { viewModel.setFirstPassword(it) }

                if (condition == AuthCondition.SIGN_UP) {
                    SecondPasswordField(secondPassword.value) { viewModel.setSecondPassword(it) }
                }

                ChooseButtons(
                    onSignInClick = onSignInClick,
                    onSignUpClick = onSignUpClick,
                    condition = condition,
                    setCondition = viewModel::setAuthCondition,
                    setName = viewModel::setName,
                    setSecondPassword = viewModel::setSecondPassword,
                    email = email,
                    name = name,
                    firstPassword = firstPassword,
                    secondPassword = secondPassword,
                    checkUserVerification = checkUserVerification,
                    sendEmail = sendEmail
                )
            }
        }
    }
}

@Composable
fun EmailField(value: String, onValueChange: (String) -> Unit) {
    StyledTextField(value, onValueChange, "Email")
}

@Composable
fun NameField(value: String, onValueChange: (String) -> Unit) {
    StyledTextField(value, onValueChange, "Имя")
}

@Composable
fun FirstPasswordField(value: String, onValueChange: (String) -> Unit) {
    StyledTextField(value, onValueChange, "Пароль", isPassword = true)
}

@Composable
fun SecondPasswordField(value: String, onValueChange: (String) -> Unit) {
    StyledTextField(value, onValueChange, "Подтвердите пароль", isPassword = true)
}

@Composable
fun StyledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean = false
) {
    val colors = TextFieldDefaults.colors(
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedContainerColor = Blue,
        unfocusedContainerColor = Blue,
        cursorColor = FirstTeal,
        focusedIndicatorColor = FirstTeal,
        unfocusedIndicatorColor = Color.DarkGray,
        focusedPlaceholderColor = Color.LightGray,
        unfocusedPlaceholderColor = Color.Gray
    )

    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        colors = colors,
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
    )
}

@Composable
fun ChooseButtons(
    onSignInClick: () -> Unit,
    onSignUpClick: () -> Unit,
    condition: AuthCondition,
    setCondition: (AuthCondition) -> Unit,
    setName: (String) -> Unit,
    setSecondPassword: (String) -> Unit,
    email: State<String>, name: State<String>,
    firstPassword: State<String>, secondPassword: State<String>,
    checkUserVerification: () -> Unit,
    sendEmail: (String) -> Unit
) {
    val context = LocalContext.current
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

        if (condition == AuthCondition.WAIT_FOR_EMAIL_VERIFICATION) {
            Button(
                onClick = checkUserVerification,
                colors = ButtonDefaults.buttonColors(containerColor = FirstTeal),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Проверить подтверждение")
            }
            Button(
                onClick = { sendEmail(email.value) },
                colors = ButtonDefaults.buttonColors(containerColor = FirstTeal),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Выслать письмо снова")
            }
        }

        Button(
            onClick = {
                when (condition) {
                    AuthCondition.SIGN_IN -> onSignInClick()
                    AuthCondition.SIGN_UP -> setCondition(AuthCondition.SIGN_IN)
                    else -> {}
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = FirstTeal),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Войти")
        }

        Button(
            onClick = {
                when (condition) {
                    AuthCondition.SIGN_IN -> {
                        setCondition(AuthCondition.SIGN_UP)
                        setName("")
                        setSecondPassword("")
                    }
                    AuthCondition.SIGN_UP -> {
                        if (email.value != "" && name.value != "" && firstPassword.value == secondPassword.value && firstPassword.value.length >= 6 && !firstPassword.value.contains(" ")) {
                            onSignUpClick()
                            setCondition(AuthCondition.WAIT_FOR_EMAIL_VERIFICATION)
                        } else {
                            val error = when {
                                email.value == "" -> "Email не может быть пустым"
                                name.value == "" -> "Имя не может быть пустым"
                                firstPassword.value.length < 6 -> "Длина пароля должна быть хотя бы 6 символов"
                                firstPassword.value.contains(" ") -> "Пароль не должен содержать пробелов"
                                else -> "Пароли не совпадают"
                            }
                            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                        }
                    }
                    else -> {}
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = FirstTeal),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Зарегистрироваться")
        }
    }
}
