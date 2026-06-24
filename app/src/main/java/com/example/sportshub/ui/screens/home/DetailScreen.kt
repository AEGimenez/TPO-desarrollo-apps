package com.example.sportshub.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.sportshub.data.local.entities.MatchEntity
import com.example.sportshub.data.local.entities.FavoriteEntity
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import com.example.sportshub.ui.theme.DarkBlueHeader
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    matchId: String?,
    onBackClick: () -> Unit,
    viewModel: DetailViewModel = koinViewModel()
) {
    val match by viewModel.match.collectAsStateWithLifecycle()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Estadísticas", "Eventos", "Alineaciones")

    LaunchedEffect(matchId) {
        matchId?.toIntOrNull()?.let { viewModel.loadMatch(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del Partido") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBlueHeader,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            match?.let { m ->
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Cabecera del partido (Marcador premium)
                    MatchHeaderCard(
                        match = m,
                        favorites = favorites,
                        onToggleTeam = { id, name, logo ->
                            viewModel.toggleFavoriteTeam(id, name, logo, null)
                        },
                        onToggleLeague = { id, name, logo ->
                            viewModel.toggleFavoriteLeague(id, name, logo)
                        }
                    )

                    // Pestañas (TabRow)
                    TabRow(selectedTabIndex = selectedTab) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = { Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Contenido según la pestaña
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        when (selectedTab) {
                            0 -> StatisticsTab(match = m)
                            1 -> EventsTab(match = m)
                            2 -> LineupsTab(match = m)
                        }
                    }
                }
            } ?: CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
fun MatchHeaderCard(
    match: MatchEntity,
    favorites: List<FavoriteEntity>,
    onToggleTeam: (id: Int, name: String, logo: String) -> Unit,
    onToggleLeague: (id: Int, name: String, logo: String) -> Unit
) {
    val dateFormatted = try {
        val sdf = SimpleDateFormat("EEEE dd MMMM, HH:mm", Locale.forLanguageTag("es"))
        sdf.format(Date(match.date)).replaceFirstChar { it.uppercase() }
    } catch (e: Exception) {
        ""
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkBlueHeader),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = match.leagueName,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(6.dp))
                val isLeagueFav = favorites.any { it.id == match.leagueId && it.type == "league" }
                IconButton(
                    onClick = { onToggleLeague(match.leagueId, match.leagueName, match.leagueLogo) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (isLeagueFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorito Liga",
                        tint = if (isLeagueFav) Color(0xFFFFD700) else Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Home
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    if (match.homeTeamLogo.isNotBlank()) {
                        AsyncImage(
                            model = match.homeTeamLogo,
                            contentDescription = match.homeTeamName,
                            modifier = Modifier.size(50.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = match.homeTeamName,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        val isHomeFav = favorites.any { it.id == match.homeTeamId && it.type == "team" }
                        IconButton(
                            onClick = { onToggleTeam(match.homeTeamId, match.homeTeamName, match.homeTeamLogo) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = if (isHomeFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorito Local",
                                tint = if (isHomeFav) Color(0xFFFFD700) else Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Marcador
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    if (match.status.uppercase() in listOf("FT", "1H", "2H", "HT")) {
                        Text(
                            text = "${match.homeGoals ?: 0}",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = " - ",
                            style = MaterialTheme.typography.displayMedium,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "${match.awayGoals ?: 0}",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    } else {
                        Text(
                            text = "VS",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }

                // Away
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    if (match.awayTeamLogo.isNotBlank()) {
                        AsyncImage(
                            model = match.awayTeamLogo,
                            contentDescription = match.awayTeamName,
                            modifier = Modifier.size(50.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = match.awayTeamName,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        val isAwayFav = favorites.any { it.id == match.awayTeamId && it.type == "team" }
                        IconButton(
                            onClick = { onToggleTeam(match.awayTeamId, match.awayTeamName, match.awayTeamLogo) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = if (isAwayFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorito Visitante",
                                tint = if (isAwayFav) Color(0xFFFFD700) else Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = dateFormatted,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f)
            )

            match.venue?.let {
                Text(
                    text = "Estadio: $it",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }
    }
}

// 1. Pestaña de Estadísticas (Barras comparativas de progreso)
@Composable
fun StatisticsTab(match: MatchEntity) {
    // Generamos estadísticas realistas basadas en el resultado
    val isPlayed = match.status.uppercase() == "FT"
    val homeGoals = match.homeGoals ?: 0
    val awayGoals = match.awayGoals ?: 0

    val stats = remember(match.id) {
        if (!isPlayed) {
            listOf(
                MatchStat("Posesión de balón", 50, 50),
                MatchStat("Tiros al arco", 0, 0),
                MatchStat("Corners", 0, 0),
                MatchStat("Faltas", 0, 0),
                MatchStat("Tarjetas amarillas", 0, 0)
            )
        } else {
            val totalShots = homeGoals + awayGoals + 6
            val homeShots = homeGoals + 3
            val awayShots = totalShots - homeShots
            val homePossession = 50 + (homeGoals - awayGoals) * 3
            val awayPossession = 100 - homePossession

            listOf(
                MatchStat("Posesión de balón", homePossession, awayPossession, isPercentage = true),
                MatchStat("Tiros al arco", homeShots, awayShots),
                MatchStat("Corners", 5 + (homeGoals % 2), 4 + (awayGoals % 2)),
                MatchStat("Faltas", 12 + homeGoals, 14 + awayGoals),
                MatchStat("Tarjetas amarillas", homeGoals + 1, awayGoals + 2)
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        stats.forEach { stat ->
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${stat.homeValue}${if (stat.isPercentage) "%" else ""}",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = stat.label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${stat.awayValue}${if (stat.isPercentage) "%" else ""}",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Barra comparativa
                val total = (stat.homeValue + stat.awayValue).coerceAtLeast(1).toFloat()
                val homeWeight = stat.homeValue / total
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(homeWeight.coerceAtLeast(0.01f))
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight((1f - homeWeight).coerceAtLeast(0.01f))
                            .background(MaterialTheme.colorScheme.error)
                    )
                }
            }
        }
    }
}

// 2. Pestaña de Eventos (Timeline cronológico de goles, tarjetas, etc.)
@Composable
fun EventsTab(match: MatchEntity) {
    val isPlayed = match.status.uppercase() == "FT"
    val homeGoals = match.homeGoals ?: 0
    val awayGoals = match.awayGoals ?: 0

    val events = remember(match.id) {
        if (!isPlayed) {
            emptyList()
        } else {
            val list = mutableListOf<MatchEvent>()
            // Goles local
            for (i in 0 until homeGoals) {
                list.add(MatchEvent(minute = 18 + i * 25, isHome = true, type = "Gol", player = "Delantero L. (${i+1})"))
            }
            // Goles visitante
            for (i in 0 until awayGoals) {
                list.add(MatchEvent(minute = 24 + i * 30, isHome = false, type = "Gol", player = "Delantero V. (${i+1})"))
            }
            // Tarjetas
            list.add(MatchEvent(minute = 32, isHome = true, type = "Amarilla", player = "Defensa L."))
            list.add(MatchEvent(minute = 55, isHome = false, type = "Amarilla", player = "Mediocampista V."))
            // Cambios
            list.add(MatchEvent(minute = 70, isHome = true, type = "Cambio", player = "Entra: Suplente L. / Sale: Titular L."))
            
            list.sortedBy { it.minute }
        }
    }

    if (events.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No hay eventos registrados para este partido.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(events) { event ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Minuto
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.width(42.dp)
                    ) {
                        Text(
                            text = "${event.minute}'",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Ícono del evento
                    val icon = when (event.type) {
                        "Gol" -> "⚽"
                        "Amarilla" -> "🟨"
                        "Roja" -> "🟥"
                        else -> "🔄"
                    }
                    Text(text = icon, fontSize = 20.sp)

                    Spacer(modifier = Modifier.width(16.dp))

                    // Información del equipo e implicación
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = event.player,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (event.isHome) match.homeTeamName else match.awayTeamName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// 3. Pestaña de Alineaciones (Formaciones tácticas)
@Composable
fun LineupsTab(match: MatchEntity) {
    val positions = listOf(
        TacticalPosition("POR", "Arquero Local", "Arquero Visitante"),
        TacticalPosition("DEF", "Defensa Local 1", "Defensa Visitante 1"),
        TacticalPosition("DEF", "Defensa Local 2", "Defensa Visitante 2"),
        TacticalPosition("MED", "Mediocampista Local 1", "Mediocampista Visitante 1"),
        TacticalPosition("MED", "Mediocampista Local 2", "Mediocampista Visitante 2"),
        TacticalPosition("DEL", "Delantero Local 1", "Delantero Visitante 1")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Formación Inicial (4-4-2)",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                .padding(8.dp)
        ) {
            Text(text = match.homeTeamName, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text(text = "Pos", modifier = Modifier.width(40.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.Center)
            Text(text = match.awayTeamName, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.End)
        }

        positions.forEach { pos ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = pos.homePlayer, modifier = Modifier.weight(1f), fontSize = 12.sp)
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    modifier = Modifier.width(40.dp)
                ) {
                    Text(
                        text = pos.pos,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
                Text(text = pos.awayPlayer, modifier = Modifier.weight(1f), fontSize = 12.sp, textAlign = TextAlign.End)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
        }
    }
}

data class MatchStat(val label: String, val homeValue: Int, val awayValue: Int, val isPercentage: Boolean = false)
data class MatchEvent(val minute: Int, val isHome: Boolean, val type: String, val player: String)
data class TacticalPosition(val pos: String, val homePlayer: String, val awayPlayer: String)