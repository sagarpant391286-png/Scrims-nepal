package com.example.ui.screens.auth

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.ScrimsRepository
import com.example.data.firebase.GoogleSignInHelper
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    repository: ScrimsRepository,
    onSignInSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val googleSignInHelper = remember { GoogleSignInHelper(context) }

    var isRegisterMode by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var displayName by remember { mutableStateOf("") }
    var ign by remember { mutableStateOf("") }
    var ffUid by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showProviderDisabledDialog by remember { mutableStateOf(false) }

    // Google Sign-In profile completion dialog state
    var showGoogleProfileDialog by remember { mutableStateOf(false) }
    var googleIgn by remember { mutableStateOf("") }
    var googleFfUid by remember { mutableStateOf("") }
    var googleUidToUpdate by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BrandBackground)
            .testTag("auth_screen")
    ) {
        // Background banner with gradient overlay
        Image(
            painter = painterResource(id = R.drawable.scrims_banner),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, BrandBackground.copy(alpha = 0.85f), BrandBackground)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with logo
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(BrandCardElevated)
                    .border(2.dp, CrimsonPrimary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.scrims_logo),
                    contentDescription = "Logo",
                    modifier = Modifier.size(68.dp),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "SCRIMS",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "NEPAL",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = CrimsonPrimary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Nepal's Competitive Free Fire & Esports Platform",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Tab Selector: Sign In vs Register
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(BrandCardDark)
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (!isRegisterMode) CrimsonPrimary else Color.Transparent)
                        .clickable {
                            isRegisterMode = false
                            errorMessage = null
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Sign In",
                        fontWeight = FontWeight.Bold,
                        color = if (!isRegisterMode) TextPrimary else TextSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isRegisterMode) CrimsonPrimary else Color.Transparent)
                        .clickable {
                            isRegisterMode = true
                            errorMessage = null
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Create Account",
                        fontWeight = FontWeight.Bold,
                        color = if (isRegisterMode) TextPrimary else TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Error Message Box
            AnimatedVisibility(visible = errorMessage != null) {
                errorMessage?.let { err ->
                    val isProviderDisabled = err.contains("FIREBASE_PROVIDER_DISABLED") ||
                            err.contains("operation is not allowed", ignoreCase = true) ||
                            err.contains("sign-in provider is disabled", ignoreCase = true)
                    val isAccountNotFound = err.contains("ACCOUNT_NOT_FOUND")
                    val isAccountExists = err.contains("ACCOUNT_EXISTS")

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isProviderDisabled) WarningOrange.copy(alpha = 0.15f) else ErrorRed.copy(alpha = 0.15f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isProviderDisabled) WarningOrange.copy(alpha = 0.6f) else ErrorRed.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isProviderDisabled) Icons.Default.Warning else Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = if (isProviderDisabled) WarningOrange else ErrorRed,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = when {
                                        isProviderDisabled -> "Firebase Sign-In Provider Disabled"
                                        isAccountNotFound -> "Account Not Found"
                                        isAccountExists -> "Account Already Exists"
                                        else -> "Authentication Notice"
                                    },
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isProviderDisabled) WarningOrange else ErrorRed
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = when {
                                    isProviderDisabled -> {
                                        "The Email/Password provider needs to be enabled in Firebase Console.\n\n" +
                                        "If you already enabled it in the Console, please tap 'Retry Now' to connect, or use 'Sign in with Google'."
                                    }
                                    isAccountNotFound -> "No account was found with this email. Tap below to create your account with this email and password."
                                    isAccountExists -> "An account with this email is already registered. Tap below to sign in."
                                    else -> err.removePrefix("INVALID_CREDENTIALS: ")
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            if (isAccountNotFound) {
                                Button(
                                    onClick = {
                                        isRegisterMode = true
                                        errorMessage = null
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Switch to Create Account")
                                }
                            } else if (isAccountExists) {
                                Button(
                                    onClick = {
                                        isRegisterMode = false
                                        errorMessage = null
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Switch to Sign In")
                                }
                            } else if (isProviderDisabled) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { errorMessage = null },
                                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Retry Now")
                                    }
                                    OutlinedButton(
                                        onClick = { showProviderDisabledDialog = true },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Setup Steps")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Input Form Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BrandCardDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = if (isRegisterMode) "REGISTER NEW PLAYER" else "SIGN IN TO YOUR ARENA",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isRegisterMode) {
                        OutlinedTextField(
                            value = displayName,
                            onValueChange = { displayName = it },
                            label = { Text("Full Name / Nickname") },
                            placeholder = { Text("e.g. Sagar Pant", color = TextMuted) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CrimsonPrimary,
                                unfocusedBorderColor = BrandBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("auth_name_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = ign,
                            onValueChange = { ign = it },
                            label = { Text("Free Fire In-Game Name (IGN)") },
                            placeholder = { Text("e.g. Gorkhali_FF", color = TextMuted) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CrimsonPrimary,
                                unfocusedBorderColor = BrandBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("auth_ign_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = ffUid,
                            onValueChange = { input ->
                                if (input.all { it.isDigit() }) ffUid = input
                            },
                            label = { Text("Free Fire Player UID (8-12 digits)") },
                            placeholder = { Text("e.g. 982314562", color = TextMuted) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CrimsonPrimary,
                                unfocusedBorderColor = BrandBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("auth_uid_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        placeholder = { Text("e.g. sagarpant391286@gmail.com", color = TextMuted) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CrimsonPrimary,
                            unfocusedBorderColor = BrandBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("auth_email_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password (Min 6 characters)") },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                    tint = TextSecondary
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CrimsonPrimary,
                            unfocusedBorderColor = BrandBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("auth_password_input")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            errorMessage = null
                            val cleanEmail = email.trim()
                            val cleanPass = password.trim()

                            if (cleanEmail.isBlank()) {
                                errorMessage = "Please enter your email address."
                                return@Button
                            }
                            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
                                errorMessage = "The email address is badly formatted. Please enter a valid email address (e.g. name@example.com)."
                                return@Button
                            }
                            if (cleanPass.length < 6) {
                                errorMessage = "Password must be at least 6 characters long."
                                return@Button
                            }
                            if (isRegisterMode) {
                                if (ign.isBlank()) {
                                    errorMessage = "Please enter your Free Fire In-Game Name (IGN)."
                                    return@Button
                                }
                                if (ffUid.isBlank() || ffUid.length < 6) {
                                    errorMessage = "Please enter a valid Free Fire Player UID (numbers only, min 6 digits)."
                                    return@Button
                                }
                            }

                            isLoading = true
                            coroutineScope.launch {
                                if (isRegisterMode) {
                                    val res = repository.registerWithEmail(
                                        email = cleanEmail,
                                        pass = cleanPass,
                                        displayName = displayName.ifBlank { ign }.trim(),
                                        ign = ign.trim(),
                                        ffUid = ffUid.trim()
                                    )
                                    isLoading = false
                                    res.onSuccess {
                                        Toast.makeText(context, "Account registered successfully!", Toast.LENGTH_SHORT).show()
                                        onSignInSuccess()
                                    }.onFailure { err ->
                                        errorMessage = err.message ?: "Registration failed."
                                        if (errorMessage?.contains("FIREBASE_PROVIDER_DISABLED") == true) {
                                            showProviderDisabledDialog = true
                                        }
                                    }
                                } else {
                                    val res = repository.signInWithEmail(cleanEmail, cleanPass)
                                    isLoading = false
                                    res.onSuccess {
                                        Toast.makeText(context, "Welcome back!", Toast.LENGTH_SHORT).show()
                                        onSignInSuccess()
                                    }.onFailure { err ->
                                        errorMessage = err.message ?: "Sign-in failed."
                                        if (errorMessage?.contains("FIREBASE_PROVIDER_DISABLED") == true) {
                                            showProviderDisabledDialog = true
                                        }
                                    }
                                }
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("submit_auth_btn"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = TextPrimary, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.SportsEsports, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isRegisterMode) "Register & Start with NPR 0" else "Sign In & Enter Arena",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                isRegisterMode = !isRegisterMode
                                errorMessage = null
                            }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isRegisterMode) "Already registered? " else "Don't have an account yet? ",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Text(
                            text = if (isRegisterMode) "Sign In" else "Create Account",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Divider with OR
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f).height(1.dp).background(BrandBorder))
                        Text(
                            text = "  OR  ",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                        Box(modifier = Modifier.weight(1f).height(1.dp).background(BrandBorder))
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Google Sign-In Button
                    OutlinedButton(
                        onClick = {
                            errorMessage = null
                            isLoading = true
                            val webClientId = context.getString(R.string.default_web_client_id)
                            coroutineScope.launch {
                                googleSignInHelper.onGoogleSignInClicked(webClientId) { success, msg ->
                                    isLoading = false
                                    if (success) {
                                        val firebaseUser = repository.firebaseService.auth.currentUser
                                        if (firebaseUser != null) {
                                            repository.onUserAuthenticated(
                                                uid = firebaseUser.uid,
                                                email = firebaseUser.email.orEmpty(),
                                                displayName = firebaseUser.displayName ?: "Player"
                                            )
                                            // Check if user has Free Fire IGN / UID set
                                            val profile = repository.currentUser.value
                                            if (profile.freeFireIgn.isBlank() || profile.freeFireUid.isBlank()) {
                                                googleUidToUpdate = firebaseUser.uid
                                                googleIgn = profile.freeFireIgn
                                                googleFfUid = profile.freeFireUid
                                                showGoogleProfileDialog = true
                                                return@onGoogleSignInClicked
                                            }
                                        }
                                        Toast.makeText(context, "Signed in with Google successfully!", Toast.LENGTH_SHORT).show()
                                        onSignInSuccess()
                                    } else {
                                        errorMessage = msg ?: "Google Sign-In failed"
                                    }
                                }
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("google_signin_btn"),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BrandBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Sign in with Google",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Trust Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("eSewa & Khalti Supported", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Anti-Cheat Monitored", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }
            }
        }
    }

    // Provider Disabled Information Dialog
    if (showProviderDisabledDialog) {
        AlertDialog(
            onDismissRequest = { showProviderDisabledDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = CrimsonPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Firebase Console Setup Required", fontWeight = FontWeight.Bold, color = TextPrimary)
                }
            },
            text = {
                Column {
                    Text(
                        text = "Why am I seeing 'operation is not allowed'?",
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "By default in new Firebase projects, the Email/Password sign-in provider is disabled until manually enabled in the Firebase Console.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Steps to enable in Firebase Console:",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "1. Open console.firebase.google.com\n" +
                               "2. Open project 'gen-lang-client-0410459307'\n" +
                               "3. Under 'Build', click 'Authentication'\n" +
                               "4. Select the 'Sign-in method' tab\n" +
                               "5. Click 'Email/Password' under Native providers\n" +
                               "6. Toggle 'Enable' to ON and click 'Save'\n\n" +
                               "Once enabled in the Console, email registration will work instantly!",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showProviderDisabledDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary)
                ) {
                    Text("Understood")
                }
            },
            containerColor = BrandCardDark
        )
    }

    // First-time Google Player Profile Setup Dialog
    if (showGoogleProfileDialog) {
        AlertDialog(
            onDismissRequest = {
                showGoogleProfileDialog = false
                onSignInSuccess()
            },
            title = {
                Text("Complete Free Fire Profile", fontWeight = FontWeight.Bold, color = TextPrimary)
            },
            text = {
                Column {
                    Text(
                        text = "To enter tournaments, please provide your Free Fire gamer details:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = googleIgn,
                        onValueChange = { googleIgn = it },
                        label = { Text("Free Fire In-Game Name (IGN)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CrimsonPrimary,
                            unfocusedBorderColor = BrandBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = googleFfUid,
                        onValueChange = { input -> if (input.all { it.isDigit() }) googleFfUid = input },
                        label = { Text("Free Fire Player UID") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CrimsonPrimary,
                            unfocusedBorderColor = BrandBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (googleIgn.isNotBlank() && googleFfUid.isNotBlank()) {
                            coroutineScope.launch {
                                repository.updatePlayerGamerInfo(googleIgn.trim(), googleFfUid.trim())
                                showGoogleProfileDialog = false
                                onSignInSuccess()
                            }
                        } else {
                            Toast.makeText(context, "Please enter both IGN and Player UID", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary)
                ) {
                    Text("Save & Enter Arena")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showGoogleProfileDialog = false
                    onSignInSuccess()
                }) {
                    Text("Skip for Now", color = TextSecondary)
                }
            },
            containerColor = BrandCardDark
        )
    }
}
