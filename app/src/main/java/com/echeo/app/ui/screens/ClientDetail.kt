package com.echeo.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.echeo.app.data.ClientEntity
import com.echeo.app.data.RenewalEntity
import com.echeo.app.domain.SubView
import com.echeo.app.domain.formatDateTime
import com.echeo.app.domain.formatDay
import com.echeo.app.domain.initialsOf
import com.echeo.app.ui.components.*
import com.echeo.app.ui.theme.EcheoColors

/** Fiche client : coordonnées, abonnements et actions. */
@Composable
fun ClientDetailScreen(
    client: ClientEntity,
    views: List<SubView>,
    onBack: () -> Unit,
    onCall: () -> Unit,
    onWhatsApp: () -> Unit,
    onRenew: (SubView) -> Unit,
    onHistory: (SubView) -> Unit,
    onAddSub: () -> Unit,
    onDelete: () -> Unit,
) {
    var confirmDelete by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize()) {
        TopBar(client.name, onBack)
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            EcheoCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Avatar(initials = initialsOf(client.name), status = null, colored = false)
                        Column(Modifier.padding(start = 12.dp)) {
                            Text(client.name, style = MaterialTheme.typography.titleMedium)
                            Text(
                                client.phone,
                                style = MaterialTheme.typography.bodyMedium.copy(color = EcheoColors.TexteSecondaire),
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SecondaryButton("Appeler", onCall, Modifier.weight(1f))
                        PrimaryButton("WhatsApp", onWhatsApp, Modifier.weight(1f))
                    }
                }
            }
            SectionTitle("Abonnements (${views.size})")
            if (views.isEmpty()) {
                Text(
                    "Aucun abonnement pour ce client.",
                    style = MaterialTheme.typography.bodyLarge.copy(color = EcheoColors.TexteSecondaire),
                )
            }
            views.forEach { v -> SubscriptionCard(v, onRenew, onHistory) }
            SecondaryButton("Ajouter un abonnement", onAddSub, Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Text(
                "Supprimer le client",
                style = MaterialTheme.typography.bodyLarge.copy(color = EcheoColors.ExpireTexte, fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { confirmDelete = true }
                    .padding(vertical = 12.dp),
            )
        }
    }
    if (confirmDelete) {
        ConfirmDialog(
            title = "Supprimer ce client ?",
            text = "Ses abonnements et leur historique seront aussi supprimés.",
            confirmLabel = "Supprimer",
            destructive = true,
            onConfirm = {
                confirmDelete = false
                onDelete()
            },
            onDismiss = { confirmDelete = false },
        )
    }
}

@Composable
private fun SubscriptionCard(v: SubView, onRenew: (SubView) -> Unit, onHistory: (SubView) -> Unit) {
    EcheoCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconSquare(v.typeIcon, v.typeColor)
                Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                    Text(v.label, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Expire le ${formatDay(v.expiryDay)}",
                        style = MaterialTheme.typography.bodyMedium.copy(color = EcheoColors.TexteSecondaire),
                    )
                }
                StatusBadge(v.status)
            }
            Text(v.relative, style = MaterialTheme.typography.bodySmall.copy(color = EcheoColors.TexteSecondaire))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PrimaryButton("Renouveler", { onRenew(v) }, Modifier.weight(1f))
                SecondaryButton("Historique", { onHistory(v) }, Modifier.weight(1f))
            }
        }
    }
}

/** Historique des renouvellements d'un abonnement. */
@Composable
fun HistoryScreen(view: SubView, renewals: List<RenewalEntity>, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        TopBar("Historique", onBack)
        LazyColumn(
            Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Column(Modifier.padding(bottom = 4.dp)) {
                    Text(view.clientName, style = MaterialTheme.typography.titleLarge)
                    Text(
                        view.label,
                        style = MaterialTheme.typography.bodyLarge.copy(color = EcheoColors.TexteSecondaire),
                    )
                }
            }
            if (renewals.isEmpty()) {
                item {
                    Text(
                        "Aucun renouvellement pour le moment.",
                        style = MaterialTheme.typography.bodyLarge.copy(color = EcheoColors.TexteSecondaire),
                    )
                }
            }
            items(renewals, key = { it.id }) { r ->
                EcheoCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            "${formatDay(r.previousExpiryDay)} → ${formatDay(r.newExpiryDay)}",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            "Renouvelé le ${formatDateTime(r.renewedAt)}",
                            style = MaterialTheme.typography.bodyMedium.copy(color = EcheoColors.TexteSecondaire),
                        )
                        r.amount?.let {
                            Text("Montant : ${"%.2f".format(it)}", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}
