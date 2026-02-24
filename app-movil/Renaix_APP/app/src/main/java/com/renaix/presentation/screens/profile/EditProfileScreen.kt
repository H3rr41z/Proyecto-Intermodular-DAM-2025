package com.renaix.presentation.screens.profile

import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.renaix.di.AppContainer
import com.renaix.domain.model.User
import com.renaix.presentation.common.components.LoadingIndicator
import com.renaix.util.Constants
import com.renaix.presentation.common.components.RenaixButton
import com.renaix.presentation.common.components.RenaixTextField
import com.renaix.presentation.common.state.UiState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    appContainer: AppContainer,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userRepository = appContainer.userRepository
    val snackbarHostState = remember { SnackbarHostState() }

    var loadState by remember { mutableStateOf<UiState<User>>(UiState.Loading) }
    var saveState by remember { mutableStateOf<UiState<Unit>>(UiState.Idle) }

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var currentImageUrl by remember { mutableStateOf<String?>(null) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        selectedImageUri = uri
    }

    LaunchedEffect(Unit) {
        userRepository.getProfile()
            .onSuccess { user ->
                name = user.name
                phone = user.phone ?: ""
                currentImageUrl = user.imageUrl
                loadState = UiState.Success(user)
            }
            .onFailure { loadState = UiState.Error(it.message ?: "Error al cargar perfil") }
    }

    LaunchedEffect(saveState) {
        if (saveState is UiState.Success) {
            snackbarHostState.showSnackbar("Perfil actualizado correctamente")
            saveState = UiState.Idle
        }
        if (saveState is UiState.Error) {
            snackbarHostState.showSnackbar((saveState as UiState.Error).message)
            saveState = UiState.Idle
        }
    }

    fun save() {
        if (name.isBlank()) {
            nameError = "El nombre es obligatorio"
            return
        }
        scope.launch {
            saveState = UiState.Loading

            // Upload image if selected
            selectedImageUri?.let { uri ->
                try {
                    val bytes = context.contentResolver.openInputStream(uri)?.readBytes()
                    if (bytes != null) {
                        val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                        userRepository.uploadProfileImage(base64)
                    }
                } catch (_: Exception) {}
            }

            userRepository.updateProfile(
                name = name.trim(),
                phone = phone.trim().ifBlank { null }
            ).onSuccess {
                selectedImageUri = null
                saveState = UiState.Success(Unit)
            }.onFailure {
                saveState = UiState.Error(it.message ?: "Error al guardar")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar perfil") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when (loadState) {
            is UiState.Loading -> LoadingIndicator(
                modifier = Modifier.padding(padding),
                message = "Cargando perfil..."
            )
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar + change photo button
                    Surface(
                        modifier = Modifier.size(96.dp),
                        shape = MaterialTheme.shapes.extraLarge,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            val displayImage = selectedImageUri ?: Constants.imageUrl(currentImageUrl)
                            if (displayImage != null) {
                                AsyncImage(
                                    model = displayImage,
                                    contentDescription = "Foto de perfil",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(MaterialTheme.shapes.extraLarge)
                                )
                            } else {
                                Icon(
                                    Icons.Filled.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(56.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { imagePicker.launch("image/*") },
                        enabled = saveState !is UiState.Loading
                    ) {
                        Icon(Icons.Filled.PhotoCamera, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (selectedImageUri != null) "Imagen seleccionada" else "Cambiar foto"
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    RenaixTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            nameError = null
                        },
                        label = "Nombre *",
                        leadingIcon = Icons.Filled.Person,
                        imeAction = ImeAction.Next,
                        isError = nameError != null,
                        errorMessage = nameError
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    RenaixTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = "Teléfono (opcional)",
                        leadingIcon = Icons.Filled.Phone,
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Done
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    RenaixButton(
                        text = "Guardar cambios",
                        onClick = { save() },
                        isLoading = saveState is UiState.Loading
                    )
                }
            }
        }
    }
}
