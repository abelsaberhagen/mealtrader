package edu.virginia.cs4720.mealtrader

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavigation() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = "home") {
        composable("home") {
            HomePlaceholder(
                onBrowseUsers = { nav.navigate("users") },
                onFriends = { nav.navigate("friends") },
                onSignOut = { AuthRepository.signOut() }
            )
        }
        composable("users") {
            UsersScreen(
                onUserClick = { nav.navigate("chat/${it.uid}") },
                onBack = { nav.popBackStack() }
            )
        }
        composable("friends") {
            FriendsScreen(
                onFriendClick = { nav.navigate("chat/${it.uid}") },
                onBack = { nav.popBackStack() }
            )
        }
        composable("chat/{uid}") {
            ChatScreen(onBack = { nav.popBackStack() })
        }
    }
}

@Composable
fun ScreenHeader(title: String, onBack: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        TextButton(onClick = onBack) { Text("< Back") }
        Spacer(Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.headlineSmall)
    }
}

@Composable
fun HomePlaceholder(onBrowseUsers: () -> Unit, onFriends: () -> Unit, onSignOut: () -> Unit) {
    val user by AuthRepository.currentUser.collectAsStateWithLifecycle()
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Logged in as ${user?.email ?: ""}")
        Spacer(Modifier.height(16.dp))
        Button(onClick = onBrowseUsers, modifier = Modifier.fillMaxWidth()) { Text("Browse Users") }
        Spacer(Modifier.height(8.dp))
        Button(onClick = onFriends, modifier = Modifier.fillMaxWidth()) { Text("Friends") }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onSignOut, modifier = Modifier.fillMaxWidth()) { Text("Sign Out") }
    }
}