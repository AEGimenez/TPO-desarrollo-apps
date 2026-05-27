package com.example.sportshub.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.sportshub.data.local.SportsDao
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(matchId: String?, dao: SportsDao) {
    val viewModel = DetailViewModel(dao)
    val match by viewModel.match.collectAsState()

    //Cargamos el partido al entrar
    LaunchedEffect(matchId) {
        matchId?.toIntOrNull()?.let { viewModel.loadMatch(it) }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Detalle del Partido") }) }
    ) { padding ->
        Box(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            match?.let { m ->
                Card(
                    modifier = Modifier.padding(16.dp).fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = "${m.homeTeamName} vs ${m.awayTeamName}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Formateo de fecha legible
                        val dateFormatted = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(m.date))

                        DetailRow(label = "Fecha:", value = dateFormatted)
                        DetailRow(label = "Liga:", value = m.leagueName)
                        DetailRow(label = "Estadio:", value = m.venue ?: "No disponible")
                        DetailRow(label = "Estado:", value = m.status)
                    }
                }
            } ?: CircularProgressIndicator(modifier = Modifier.padding(32.dp))
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(text = label, fontWeight = FontWeight.Bold, modifier = Modifier.width(100.dp))
        Text(text = value)
    }
}