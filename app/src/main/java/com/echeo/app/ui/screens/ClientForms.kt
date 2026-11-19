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
import com.echeo.app.data.SubscriptionTypeEntity
import com.echeo.app.domain.todayEpoch
import com.echeo.app.ui.components.*
import com.echeo.app.ui.theme.EcheoColors

@Composable
fun NewClientScreen(
    types: List<SubscriptionTypeEntity>,
    onBack: () -> Unit,
    onSave: (String, String, Long, Int, Long) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var typeId by remember { mutableStateOf(types.firstOrNull()?.id ?: 0L) }
    val type = types.firstOrNull { it.id == typeId }
    var months by remember(typeId) { mutableStateOf(type?.durationList?.firstOrNull() ?: 1) }
    var start by remember { mutableStateOf(todayEpoch()) }
    val canSave = name.isNotBlank() && phone.isNotBlank() && type != null && months > 0

    Column(Modifier.fillMaxSize()) {
        TopBar("Nouveau client", onBack)
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Field("Nom", name, { name = it }, Modifier.fillMaxWidth(), placeholder = "Nom du client")
            Field("Téléphone", phone, { phone = it }, Modifier.fillMaxWidth(), placeholder = "Numéro WhatsApp", keyboard = KeyboardType.Phone)
            TypeAndDuration(types, typeId, { typeId = it }, months, { months = it })
            DateField("Date de début", start, { start = it }, Modifier.fillMaxWidth())
        }
        Box(Modifier.fillMaxWidth().padding(16.dp)) {
            PrimaryButton(
                "Enregistrer",
                { if (canSave) onSave(name, phone, typeId, months, start) },
                Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
fun AddSubscriptionScreen(
    clientName: String,
    types: List<SubscriptionTypeEntity>,
    onBack: () -> Unit,
    onSave: (Long, Int, Long) -> Unit,
) {
    var typeId by remember { mutableStateOf(types.firstOrNull()?.id ?: 0L) }
    val type = types.firstOrNull { it.id == typeId }
    var months by remember(typeId) { mutableStateOf(type?.durationList?.firstOrNull() ?: 1) }
    var start by remember { mutableStateOf(todayEpoch()) }
    val canSave = type != null && months > 0

    Column(Modifier.fillMaxSize()) {
        TopBar("Nouvel abonnement", onBack)
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                "Pour $clientName",
                style = MaterialTheme.typography.titleMedium,
            )
            TypeAndDuration(types, typeId, { typeId = it }, months, { months = it })
            DateField("Date de début", start, { start = it }, Modifier.fillMaxWidth())
        }
        Box(Modifier.fillMaxWidth().padding(16.dp)) {
            PrimaryButton(
                "Ajouter l'abonnement",
                { if (canSave) onSave(typeId, months, start) },
                Modifier.fillMaxWidth(),
            )
        }
    }
}

/** Choix du type d'abonnement, puis de la durée proposée pour ce type. */
@Composable
private fun TypeAndDuration(
    types: List<SubscriptionTypeEntity>,
    typeId: Long,
    onType: (Long) -> Unit,
    months: Int,
    onMonths: (Int) -> Unit,
) {
    val type = types.firstOrNull { it.id == typeId }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Abonnement", style = MaterialTheme.typography.bodyMedium.copy(color = EcheoColors.TexteSecondaire))
        ChipRow {
            types.forEach { t ->
                Chip(t.name, t.id == typeId, { onType(t.id) })
            }
        }
        Spacer(Modifier.height(8.dp))
        Text("Durée", style = MaterialTheme.typography.bodyMedium.copy(color = EcheoColors.TexteSecondaire))
        ChipRow {
            type?.durationList.orEmpty().forEach { m ->
                Chip("$m mois", m == months, { onMonths(m) })
            }
        }
    }
}
