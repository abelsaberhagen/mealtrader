package edu.virginia.cs4720.mealtrader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import edu.virginia.cs4720.mealtrader.ui.theme.MealtraderTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MealtraderTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val user by AuthRepository.currentUser.collectAsStateWithLifecycle()
                    val current = user
                    Box(Modifier.padding(innerPadding)) {
                        if (current == null) {
                            LoginScreen()
                        } else {
                            HomePlaceholder(
                                email = current.email ?: "",
                                onSignOut = { AuthRepository.signOut() }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomePlaceholder(email: String, onSignOut: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Logged in as $email")
        Spacer(Modifier.height(16.dp))
        Button(onClick = onSignOut) { Text("Sign Out") }
    }
}