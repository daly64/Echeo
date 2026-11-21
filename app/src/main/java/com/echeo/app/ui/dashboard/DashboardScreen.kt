package com.echeo.app.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.echeo.app.R
import com.echeo.app.domain.ClientSummary
import com.echeo.app.domain.DashboardCounts
import com.echeo.app.ui.components.ClientRow
import com.echeo.app.ui.components.EcheoCard
import com.echeo.app.ui.components.EcheoHeader
import com.echeo.app.ui.components.HeaderIconButton
import com.echeo.app.ui.components.HeaderTitle
import com.echeo.app.ui.components.PrimaryButton
import com.echeo.app.ui.components.RowDivider
import com.echeo.app.ui.theme.EcheoColors

/**
 * Tableau de bord : trois compteurs (actifs, bientôt expirés, expirés), puis la liste
 * des clients triée par expiration. Aucun chiffre d'argent.
 */
@Composable
fun DashboardContent(
    shopName: String,
    managerName: String,
    counts: DashboardCounts,
    clients: List<ClientSummary>,
    onOpenReminders: () -> Unit,
    onOpenClient: (Long) -> Unit,
    onAddClient: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sorted = clients.sortedBy { it.nextExpiryDay ?: Long.MAX_VALUE }

    Column(modifier.fillMaxSize()) {
        EcheoHeader {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) { HeaderTitle(shopName, managerName.ifBlank { null }) }
                HeaderIconButton(
                    icon = R.drawable.ic_bell,
                    showDot = counts.soon + counts.expired > 0,
                    onClick = onOpenReminders,
                )
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CounterCard("Actifs", counts.active, EcheoColors.ActifTexte, Modifier.weight(1f))
                    CounterCard("Bientôt expirés", counts.soon, EcheoColors.BientotTexte, Modifier.weight(1f))
                }
            }
            item {
                EcheoCard(Modifier.fillMaxWidth()) {
                    Row(
                        Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CounterText("Expirés", counts.expired, EcheoColors.ExpireTexte, Modifier.weight(1f))
                        PrimaryButton("Voir les relances", onOpenReminders)
                    }
                }
            }
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 0.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Clients", style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp), modifier = Modifier.weight(1f))
                    Text("Par expiration", style = MaterialTheme.typography.bodyMedium.copy(color = EcheoColors.TexteSecondaire))
                }
            }
            item {
                if (sorted.isEmpty()) {
                    EcheoCard(Modifier.fillMaxWidth()) {
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                "Aucun client pour l'instant.",
                                style = MaterialTheme.typography.bodyLarge.copy(color = EcheoColors.TexteSecondaire),
                                textAlign = TextAlign.Center,
                            )
                            Spacer(Modifier.height(12.dp))
                            PrimaryButton("Ajouter un client", onAddClient)
                        }
                    }
                } else {
                    EcheoCard(Modifier.fillMaxWidth()) {
                        sorted.forEachIndexed { index, c ->
                            if (index > 0) RowDivider()
                            ClientRow(
                                initials = c.initials,
                                title = c.name,
                                subtitle = if (c.nextSubscription != null) "${c.nextSubscription} · ${c.nextLabel}" else "Aucun abonnement",
                                status = c.status,
                                coloredAvatar = true,
                                onClick = { onOpenClient(c.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CounterCard(label: String, value: Int, color: Color, modifier: Modifier = Modifier) {
    EcheoCard(modifier) {
        CounterText(label, value, color, Modifier.padding(horizontal = 16.dp, vertical = 14.dp))
    }
}

@Composable
private fun CounterText(label: String, value: Int, color: Color, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.bodyMedium.copy(color = EcheoColors.TexteSecondaire))
        Text(value.toString(), style = MaterialTheme.typography.headlineMedium.copy(color = color, fontSize = 34.sp))
    }
}
