package com.example.sportshub.ui.screens.container

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.sportshub.ui.theme.DarkBlueHeader
import com.google.firebase.auth.FirebaseAuth
import com.example.sportshub.ui.screens.home.HomeViewModel
import com.example.sportshub.ui.screens.home.tabs.InicioTab
import com.example.sportshub.ui.screens.home.tabs.PartidosTab
import com.example.sportshub.ui.screens.home.tabs.NoticiasTab
import com.example.sportshub.ui.screens.home.tabs.FavoritosTab
import com.example.sportshub.ui.screens.home.tabs.AsistenteTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContainerScreen(
    homeViewModel: HomeViewModel,
    onMatchClick: (String) -> Unit,
    onNewsClick: (String) -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }
    var showProfileSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "SportsHUB",
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBlueHeader,
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { showProfileSheet = true }) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Mi Perfil",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    label = { Text("Inicio") },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    label = { Text("Partidos") },
                    icon = { Icon(Icons.Default.List, contentDescription = "Partidos") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    label = { Text("Noticias") },
                    icon = { Icon(Icons.Default.Info, contentDescription = "Noticias") }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    label = { Text("Favoritos") },
                    icon = { Icon(Icons.Default.Favorite, contentDescription = "Favoritos") }
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    label = { Text("Asistente") },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Asistente") }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                0 -> InicioTab(onMatchClick = onMatchClick, onNewsClick = onNewsClick)
                1 -> PartidosTab(viewModel = homeViewModel, onMatchClick = onMatchClick)
                2 -> NoticiasTab(onNewsClick = onNewsClick)
                3 -> FavoritosTab()
                4 -> AsistenteTab()
            }
        }

        if (showProfileSheet) {
            ProfileBottomSheet(
                onDismissRequest = { showProfileSheet = false },
                onLogout = {
                    showProfileSheet = false
                    // 1. Cerrar sesión de Firebase
                    FirebaseAuth.getInstance().signOut()
                    
                    // 2. Cerrar sesión de Google
                    try {
                        val gso = com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(
                            com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN
                        ).build()
                        val googleSignInClient = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(context, gso)
                        googleSignInClient.signOut()
                    } catch (e: Exception) {
                        android.util.Log.e("MainContainer", "Error al cerrar sesión de Google: ${e.message}", e)
                    }
                    
                    onLogout()
                }
            )
        }
    }
}
