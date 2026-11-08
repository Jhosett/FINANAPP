package com.ufpso.finanapp.ui.screens

import android.util.Patterns
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ufpso.finanapp.R
import com.ufpso.finanapp.ui.theme.FINANAPPTheme
import com.ufpso.finanapp.ui.theme.GreenDark
import com.ufpso.finanapp.ui.theme.GreenPrimary

@Composable
fun RegisterScreen(
    onBackToLogin: () -> Unit,
    onRegister: (fullName: String, email: String, username: String, password: String) -> Unit,
    registerError: String? = null, // error que viene de la base de datos
    modifier: Modifier = Modifier
) {
    var fullName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var showErrors by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    // Mensajes de error (null = campo válido)
    val nameError = when {
        fullName.isBlank() -> "Ingresa tu nombre completo"
        else -> null
    }
    val emailError = when {
        email.isBlank() -> "Ingresa tu correo electrónico"
        !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "El correo no es válido"
        else -> null
    }
    val usernameError = when {
        username.isBlank() -> "Ingresa un nombre de usuario"
        username.length < 4 -> "Mínimo 4 caracteres"
        else -> null
    }
    val passwordError = when {
        password.isBlank() -> "Ingresa una contraseña"
        password.length < 6 -> "Mínimo 6 caracteres"
        else -> null
    }
    val confirmError = when {
        confirmPassword.isBlank() -> "Confirma tu contraseña"
        confirmPassword != password -> "Las contraseñas no coinciden"
        else -> null
    }

    fun attemptRegister() {
        showErrors = true
        val isValid = listOf(nameError, emailError, usernameError, passwordError, confirmError)
            .all { it == null }
        if (isValid) {
            onRegister(fullName, email, username, password)
        }
    }

    // Avanza al siguiente campo
    val goNext: () -> Unit = { focusManager.moveFocus(FocusDirection.Down) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
            .verticalScroll(rememberScrollState())
    ) {
        RegisterHeader(onBack = onBackToLogin)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 28.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Crear cuenta",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Completa tus datos para empezar",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            AuthTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = "Nombre completo",
                leadingIcon = Icons.Filled.Badge,
                errorMessage = if (showErrors) nameError else null,
                onImeAction = goNext
            )
            AuthTextField(
                value = email,
                onValueChange = { email = it },
                label = "Correo electrónico",
                leadingIcon = Icons.Filled.Email,
                keyboardType = KeyboardType.Email,
                errorMessage = if (showErrors) emailError else null,
                onImeAction = goNext
            )
            AuthTextField(
                value = username,
                onValueChange = { username = it },
                label = "Nombre de usuario",
                leadingIcon = Icons.Filled.Person,
                errorMessage = if (showErrors) usernameError else null,
                onImeAction = goNext
            )
            AuthTextField(
                value = password,
                onValueChange = { password = it },
                label = "Contraseña",
                leadingIcon = Icons.Filled.Lock,
                isPassword = true,
                errorMessage = if (showErrors) passwordError else null,
                onImeAction = goNext
            )
            AuthTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "Confirmar contraseña",
                leadingIcon = Icons.Filled.Lock,
                isPassword = true,
                imeAction = ImeAction.Done,
                errorMessage = if (showErrors) confirmError else null,
                onImeAction = {
                    focusManager.clearFocus()
                    attemptRegister()
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (registerError != null) {
                Text(
                    text = registerError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Button(
                onClick = { attemptRegister() },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Text(
                    text = "Registrarme",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "¿Ya tienes cuenta?",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = onBackToLogin) {
                    Text("Inicia sesión", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun RegisterHeader(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
            .background(Brush.verticalGradient(listOf(GreenPrimary, GreenDark)))
            .statusBarsPadding()
            .padding(bottom = 28.dp)
    ) {
        // Botón de volver
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(4.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver al inicio de sesión",
                tint = Color.White
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Mismo logo del login, en tamaño más pequeño
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.finanapp),
                    contentDescription = "Logo de FinanApp",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Únete a FinanApp",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Toma el control de tu dinero",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f)
            )
        }
    }
}

// Campo reutilizable: evita repetir el mismo bloque 5 veces
@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    errorMessage: String? = null,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: () -> Unit = {}
) {
    var passwordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(leadingIcon, contentDescription = null) },
        trailingIcon = if (isPassword) {
            {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Filled.VisibilityOff
                        else Icons.Filled.Visibility,
                        contentDescription = if (passwordVisible) "Ocultar contraseña"
                        else "Mostrar contraseña"
                    )
                }
            }
        } else null,
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation()
        else VisualTransformation.None,
        singleLine = true,
        isError = errorMessage != null,
        supportingText = { if (errorMessage != null) Text(errorMessage) },
        shape = RoundedCornerShape(14.dp),
        keyboardOptions = KeyboardOptions(
            keyboardType = if (isPassword) KeyboardType.Password else keyboardType,
            imeAction = imeAction
        ),
        keyboardActions = KeyboardActions(
            onNext = { onImeAction() },
            onDone = { onImeAction() }
        ),
        modifier = modifier.fillMaxWidth()
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RegisterScreenPreview() {
    FINANAPPTheme {
        RegisterScreen(onBackToLogin = {}, onRegister = { _, _, _, _ -> })
    }
}