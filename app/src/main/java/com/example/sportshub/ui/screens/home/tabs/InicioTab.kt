package com.example.sportshub.ui.screens.home.tabs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.sportshub.data.local.entities.MatchEntity
import com.example.sportshub.data.local.entities.NewsEntity
import com.example.sportshub.data.local.entities.StandingEntity
import com.example.sportshub.ui.screens.home.MatchItem
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InicioTab(
    viewModel: InicioViewModel = koinViewModel(),
    onMatchClick: (String) -> Unit,
    onNewsClick: (String) -> Unit
) {
    val upcomingMatches by viewModel.upcomingMatches.collectAsStateWithLifecycle()
    val recentNews by viewModel.recentNews.collectAsStateWithLifecycle()
    val favoriteLeagues by viewModel.favoriteLeagues.collectAsStateWithLifecycle()
    val selectedLeague by viewModel.selectedStandingsLeague.collectAsStateWithLifecycle()
    val standingsTable by viewModel.standingsTable.collectAsStateWithLifecycle()

    var isStandingsCollapsed by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. Sección Próximos Partidos (Horizontal)
        item {
            Text(
                text = "Próximos Partidos",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (upcomingMatches.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No hay partidos programados para tus favoritos.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(end = 16.dp)
                ) {
                    items(upcomingMatches) { match ->
                        UpcomingMatchCard(match = match, onClick = { onMatchClick(match.id.toString()) })
                    }
                }
            }
        }

        // 2. Sección Tabla de Posiciones (Card Colapsable)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isStandingsCollapsed = !isStandingsCollapsed },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tabla de Posiciones",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            imageVector = if (isStandingsCollapsed) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                            contentDescription = "Expandir/Colapsar"
                        )
                    }

                    AnimatedVisibility(visible = !isStandingsCollapsed) {
                        Column {
                            Spacer(modifier = Modifier.height(12.dp))

                            // Selector de Liga Favorita
                            if (favoriteLeagues.isEmpty()) {
                                Text(
                                    text = "Agrega ligas favoritas para ver posiciones.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            } else {
                                ScrollableTabRow(
                                    selectedTabIndex = favoriteLeagues.indexOf(selectedLeague).coerceAtLeast(0),
                                    edgePadding = 0.dp,
                                    containerColor = Color.Transparent,
                                    divider = {}
                                ) {
                                    favoriteLeagues.forEach { league ->
                                        Tab(
                                            selected = league == selectedLeague,
                                            onClick = { viewModel.selectStandingsLeague(league) },
                                            text = { Text(league.name, style = MaterialTheme.typography.labelSmall) }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Tabla
                                if (standingsTable.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(100.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                    }
                                } else {
                                    StandingsHeader()
                                    standingsTable.take(10).forEach { standing ->
                                        StandingRow(standing = standing)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Sección Noticias (LazyColumn vertical)
        item {
            Text(
                text = "Últimas Noticias",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (recentNews.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No hay noticias recientes.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        } else {
            items(recentNews) { article ->
                com.example.sportshub.ui.screens.home.tabs.NewsItemCard(
                    article = article,
                    onClick = { onNewsClick(article.id) }
                )
            }
        }
    }
}

@Composable
fun UpcomingMatchCard(match: MatchEntity, onClick: () -> Unit) {
    val dateFormatted = try {
        val sdf = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
        sdf.format(Date(match.date))
    } catch (e: Exception) {
        ""
    }

    Card(
        modifier = Modifier
            .width(180.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = match.leagueName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Home Team
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    if (match.homeTeamLogo.isNotBlank()) {
                        AsyncImage(
                            model = match.homeTeamLogo,
                            contentDescription = match.homeTeamName,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Text(
                        text = match.homeTeamName,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }

                Text(
                    text = "vs",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Away Team
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    if (match.awayTeamLogo.isNotBlank()) {
                        AsyncImage(
                            model = match.awayTeamLogo,
                            contentDescription = match.awayTeamName,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Text(
                        text = match.awayTeamName,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = dateFormatted,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun StandingsHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(vertical = 6.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "#", modifier = Modifier.width(24.dp), fontWeight = FontWeight.Bold, fontSize = 11.sp, textAlign = TextAlign.Center)
        Text(text = "Equipo", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 11.sp)
        Text(text = "PTS", modifier = Modifier.width(32.dp), fontWeight = FontWeight.Bold, fontSize = 11.sp, textAlign = TextAlign.Center)
        Text(text = "PJ", modifier = Modifier.width(28.dp), fontWeight = FontWeight.Bold, fontSize = 11.sp, textAlign = TextAlign.Center)
        Text(text = "G", modifier = Modifier.width(20.dp), fontWeight = FontWeight.Bold, fontSize = 11.sp, textAlign = TextAlign.Center)
        Text(text = "E", modifier = Modifier.width(20.dp), fontWeight = FontWeight.Bold, fontSize = 11.sp, textAlign = TextAlign.Center)
        Text(text = "P", modifier = Modifier.width(20.dp), fontWeight = FontWeight.Bold, fontSize = 11.sp, textAlign = TextAlign.Center)
        Text(text = "DG", modifier = Modifier.width(28.dp), fontWeight = FontWeight.Bold, fontSize = 11.sp, textAlign = TextAlign.Center)
    }
}

@Composable
fun StandingRow(standing: StandingEntity) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = standing.rank.toString(), modifier = Modifier.width(24.dp), fontSize = 11.sp, textAlign = TextAlign.Center)
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            if (standing.teamLogo.isNotBlank()) {
                AsyncImage(
                    model = standing.teamLogo,
                    contentDescription = standing.teamName,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = standing.teamName,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(text = standing.points.toString(), modifier = Modifier.width(32.dp), fontWeight = FontWeight.Bold, fontSize = 11.sp, textAlign = TextAlign.Center)
        Text(text = standing.played.toString(), modifier = Modifier.width(28.dp), fontSize = 11.sp, textAlign = TextAlign.Center)
        Text(text = standing.won.toString(), modifier = Modifier.width(20.dp), fontSize = 11.sp, textAlign = TextAlign.Center)
        Text(text = standing.drawn.toString(), modifier = Modifier.width(20.dp), fontSize = 11.sp, textAlign = TextAlign.Center)
        Text(text = standing.lost.toString(), modifier = Modifier.width(20.dp), fontSize = 11.sp, textAlign = TextAlign.Center)
        Text(text = (if (standing.goalDiff > 0) "+${standing.goalDiff}" else standing.goalDiff.toString()), modifier = Modifier.width(28.dp), fontSize = 11.sp, textAlign = TextAlign.Center)
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
}
