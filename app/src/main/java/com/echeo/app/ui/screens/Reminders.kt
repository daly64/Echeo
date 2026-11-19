package com.echeo.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.echeo.app.R
import com.echeo.app.domain.SubStatus
import com.echeo.app.domain.SubView
import com.echeo.app.domain.contactLabel
import com.echeo.app.ui.components.*
import com.echeo.app.ui.theme.EcheoColors

/** Liste de relance : abonnements expirés ou bientôt expirés, les plus urgents d'abord. */
@Composable
fun RemindersScreen(
    views: List<SubView>,
    today: Long,
    onCall: (SubView) -> Unit,
    onWhatsApp: (SubView) -> Unit,
    onRenew: (SubView) -> Unit,
    onMessages: () -> Unit,
) {
    val due = views.filter { it.status != SubStatus.ACTIVE }.sortedBy { it.expiryDay }
    Column(Modifier.fillMaxSize()) {
        EcheoHeader {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    HeaderTitle(
                        "Relances",
                        if (due.isEmpty()) "Rien à relancer" else "${due.size} à relancer",
                    )
                }
                HeaderIconButton(R.drawable.ic_chat, false, onMessages)
            }
        }
        LazyColumn(
            Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (due.isEmpty()) {
                item {
                    Text(
                        "Aucun abonnement n'est expiré ou bientôt expiré.",
                        style = MaterialTheme.typography.bodyLarge.copy(color = EcheoColors.TexteSecondaire),
                        modifier = Modifier.padding(top = 24.dp),
                    )
                }
            }
            items(due, key = { it.id }) { v ->
                ReminderCard(v, onCall, onWhatsApp, onRenew)
            }
        }
    }
}

@Composable
private fun ReminderCard(
    v: SubView,
    onCall: (SubView) -> Unit,
    onWhatsApp: (SubView) -> Unit,
    onRenew: (SubView) -> Unit,
) {
    EcheoCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(v.clientName, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${v.label} · ${v.relative}",
                        style = MaterialTheme.typography.bodyMedium.copy(color = EcheoColors.TexteSecondaire),
                    )
                }
                StatusBadge(v.status)
            }
            Spacer(Modifier.height(4.dp))
            Text(
                contactLabel(v.lastContactAt),
                style = MaterialTheme.typography.bodySmall.copy(color = EcheoColors.TexteDiscret),
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SecondaryButton("Appeler", { onCall(v) }, Modifier.weight(1f))
                PrimaryButton("WhatsApp", { onWhatsApp(v) }, Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
            SecondaryButton("Renouveler", { onRenew(v) }, Modifier.fillMaxWidth())
        }
    }
}
