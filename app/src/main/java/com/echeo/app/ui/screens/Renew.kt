package com.echeo.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.echeo.app.domain.SubView
import com.echeo.app.domain.addMonths
import com.echeo.app.domain.formatDay
import com.echeo.app.domain.todayEpoch
import com.echeo.app.ui.components.*
import com.echeo.app.ui.theme.EcheoColors

/** Renouvellement : on prolonge la date d'expiration, montant facultatif. */
@Composable
fun RenewScreen(
    view: SubView,
    onBack: () -> Unit,
    onConfirm: (Long, Double?) -> Unit,
) {
    val today = todayEpoch()
    // Un abonnement encore actif se prolonge depuis sa date ; un expiré repart d'aujourd'hui.
    val base = maxOf(view.expiryDay, today)
    val options = (listOf(1, 3, 6, 12) + view.durationMonths).distinct().sorted()
    var months by remember { mutableStateOf(view.durationMonths) }
    var amountText by remember { mutableStateOf("") }
    val newExpiry = addMonths(base, months)

    Column(Modifier.fillMaxSize()) {
        TopBar("Renouveler", onBack)
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            EcheoCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(view.clientName, style = MaterialTheme.typography.titleMedium)
                    Text(
                        view.label,
                        style = MaterialTheme.typography.bodyLarge.copy(color = EcheoColors.TexteSecondaire),
                    )
                    Text(
                        view.relative,
                        style = MaterialTheme.typography.bodyMedium.copy(color = EcheoColors.TexteSecondaire),
                    )
                }
            }
            SectionTitle("Durée ajoutée")
            ChipRow {
                options.forEach { m ->
                    Chip("$m mois", m == months, { months = m })
                }
            }
            EcheoCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "Nouvelle date d'expiration",
                        style = MaterialTheme.typography.bodyMedium.copy(color = EcheoColors.TexteSecondaire),
                    )
                    Text(formatDay(newExpiry), style = MaterialTheme.typography.titleLarge.copy(color = EcheoColors.Turquoise))
                    Text(
                        "Prolongé à partir du ${formatDay(base)}",
                        style = MaterialTheme.typography.bodySmall.copy(color = EcheoColors.TexteDiscret),
                    )
                }
            }
            Field(
                label = "Montant payé (facultatif)",
                value = amountText,
                onChange = { amountText = it.replace(',', '.') },
                modifier = Modifier.fillMaxWidth(),
                placeholder = "0",
                keyboard = KeyboardType.Decimal,
            )
        }
        Box(Modifier.fillMaxWidth().padding(16.dp)) {
            PrimaryButton(
                "Confirmer le renouvellement",
                { onConfirm(newExpiry, amountText.toDoubleOrNull()) },
                Modifier.fillMaxWidth(),
            )
        }
    }
}
