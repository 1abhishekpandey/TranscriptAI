package com.abhishek.transcriptai.presentation.versioninput

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.abhishek.transcriptai.util.Logger

/**
 * Version Input Screen
 * Allows users to test and save a newer YouTube client version
 * when the current version is rejected by YouTube
 *
 * @param onNavigateBack Callback for navigating back to the previous screen
 * @param viewModel VersionInputViewModel injected via Hilt
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VersionInputScreen(
    onNavigateBack: () -> Unit,
    viewModel: VersionInputViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val versionInput by viewModel.versionInput.collectAsState()

    val isValidVersion = remember(versionInput) {
        versionInput.matches(Regex("^\\d+\\.\\d+\\.\\d+$"))
    }

    val currentVersion = remember {
        when (val state = viewModel.uiState.value) {
            is VersionInputUiState.Idle -> state.currentVersion
            else -> ""
        }
    }

    Logger.logV("VersionInputScreen: Recomposing with state: ${uiState::class.simpleName}")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fix Client Version", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Info Card
            InfoCard()

            // Current Version Card
            CurrentVersionCard(currentVersion = currentVersion)

            // Version Input Field
            OutlinedTextField(
                value = versionInput,
                onValueChange = { viewModel.onEvent(VersionInputUiEvent.UpdateVersion(it)) },
                label = { Text("New version") },
                placeholder = { Text("e.g., 21.10.38") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = uiState !is VersionInputUiState.Testing
            )

            // Test Version Button
            Button(
                onClick = { viewModel.onEvent(VersionInputUiEvent.TestVersion(versionInput)) },
                modifier = Modifier.fillMaxWidth(),
                enabled = isValidVersion && uiState !is VersionInputUiState.Testing
            ) {
                Text("Test Version")
            }

            // Result Section
            when (val state = uiState) {
                is VersionInputUiState.Idle -> {
                    // No result to show
                }
                is VersionInputUiState.Testing -> {
                    TestingContent()
                }
                is VersionInputUiState.TestSuccess -> {
                    TestSuccessContent(
                        version = state.version,
                        onSaveClick = { viewModel.onEvent(VersionInputUiEvent.SaveVersion(state.version)) }
                    )
                }
                is VersionInputUiState.TestFailed -> {
                    TestFailedContent(error = state.error)
                }
                is VersionInputUiState.Saved -> {
                    SavedContent(onBackClick = onNavigateBack)
                }
            }
        }
    }
}

@Composable
private fun InfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Why is this needed?",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "YouTube periodically rejects older Android app versions when accessing its internal API. When this happens, you need to provide a newer version number.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "What does a version look like?",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "The version follows the format major.minor.patch, for example: 20.10.38 or 21.05.42",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "How to find the latest version",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "1. Open Google Play Store on your phone\n2. Search for 'YouTube'\n3. Tap on the YouTube app\n4. Scroll down to 'About this app'\n5. Look for the version number (e.g., 20.10.38)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CurrentVersionCard(currentVersion: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Text(
            text = "Current version: $currentVersion",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
private fun TestingContent() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Testing version...",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun TestSuccessContent(
    version: String,
    onSaveClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Version $version works!",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Button(
                onClick = onSaveClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary
                )
            ) {
                Text("Save & Use")
            }
        }
    }
}

@Composable
private fun TestFailedContent(error: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Test Failed",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
private fun SavedContent(onBackClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Version saved!",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Text(
                text = "Go back and retry your download.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Button(
                onClick = onBackClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary
                )
            ) {
                Text("Go Back")
            }
        }
    }
}
