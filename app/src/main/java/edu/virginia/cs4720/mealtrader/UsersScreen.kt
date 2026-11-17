package edu.virginia.cs4720.mealtrader

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun UsersScreen(
    onUserClick: (AppUser) -> Unit,
    onBack: () -> Unit,
    vm: UsersViewModel = viewModel()
) {
    val state by vm.uiState.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        ScreenHeader("Browse Users", onBack)
        Spacer(Modifier.height(16.dp))

        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            state.error != null -> Text(state.error!!, color = MaterialTheme.colorScheme.error)
            state.users.isEmpty() -> Text("No other users yet")
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.users, key = { it.uid }) { user ->
                    UserRow(user = user, onClick = { onUserClick(user) })
                }
            }
        }
    }
}

@Composable
private fun UserRow(user: AppUser, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(user.computingId, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(user.email, style = MaterialTheme.typography.bodySmall)
            if (user.availableToDonate) {
                Spacer(Modifier.height(4.dp))
                Text("Available to donate", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}