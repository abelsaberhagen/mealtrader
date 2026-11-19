package edu.virginia.cs4720.mealtrader

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun FriendsScreen(
    onFriendClick: (AppUser) -> Unit,
    onBack: () -> Unit,
    vm: FriendsViewModel = viewModel()
) {

    val state by vm.uiState.collectAsStateWithLifecycle()
    var showSearch by remember { mutableStateOf(false) }
    val searchState = rememberTextFieldState()
    val query = searchState.text.toString()
    val results = state.nonFriends.filter {
        query.isNotBlank() && it.computingId.contains(query, ignoreCase = true)
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        ScreenHeader("Friends", onBack)
        Spacer(Modifier.height(16.dp))

        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            state.error != null -> Text(state.error!!, color = MaterialTheme.colorScheme.error)
            state.friends.isEmpty() -> Text("No friends yet")
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.friends, key = { it.uid }) { friend ->
                    Card(onClick = { onFriendClick(friend) }, modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                friend.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                listOf(friend.year, friend.computingId)
                                    .filter { it.isNotBlank() }
                                    .joinToString(" · "),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (friend.availableToDonate) {
                                Text(
                                    "Available to donate",
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
        Box(

        ) {
            if(!showSearch) {
                Button(
                    onClick = { showSearch = true },
                    enabled = !state.isLoading
                ) {
                    Text("Add Friend")
                }
            }
            else {
                TextField(
                    state = searchState,
                    label = { Text("Enter Computing ID") },
                    lineLimits = TextFieldLineLimits.SingleLine,
                    trailingIcon = {
                        TextButton(onClick = {
                            searchState.clearText()
                            showSearch = false
                        }) { Text("Cancel") }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

