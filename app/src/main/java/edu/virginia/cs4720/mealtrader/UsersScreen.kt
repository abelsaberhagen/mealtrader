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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun UsersScreen(
    onUserClick: (AppUser) -> Unit,
    onBack: (() -> Unit)? = null,
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
            state.users.isEmpty() -> Text(
                if (state.availableOnly) "No one is available to donate right now" else "No other users yet"
            )
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.users, key = { it.uid }) { user ->
                    UserRow(
                        user = user,
                        preview = state.previews[user.uid],
                        myUid = vm.myUid,
                        isFriend = user.uid in state.friendIds,
                        onClick = { onUserClick(user) },
                        onAddFriend = { vm.addFriend(user.uid) }
                    )
                }
            }
        }
    }
}

@Composable
private fun UserRow(
    user: AppUser,
    preview: ChatPreview?,
    myUid: String,
    isFriend: Boolean,
    onClick: () -> Unit,
    onAddFriend: () -> Unit
) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(user.displayName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    listOf(user.year, user.computingId).filter { it.isNotBlank() }.joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (user.availableToDonate) {
                    Text("Available to donate", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                }
                Spacer(Modifier.height(4.dp))
                if (preview != null && preview.lastMessage.isNotEmpty()) {
                    val prefix = if (preview.lastSenderId == myUid) "You: " else ""
                    Text(
                        prefix + preview.lastMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Text(
                        "Tap to start a chat",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            if (isFriend) {
                Text("Friend ✓", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
            } else {
                OutlinedButton(onClick = onAddFriend) { Text("Add Friend") }
            }
        }
    }
}