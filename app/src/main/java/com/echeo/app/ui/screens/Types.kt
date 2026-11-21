package com.echeo.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.echeo.app.data.SubscriptionTypeEntity
import com.echeo.app.domain.abonnementsLabel
import com.echeo.app.ui.components.*
import com.echeo.app.ui.theme.EcheoColors

/** Liste des types d'abonnement, avec activation et nombre d'abonnements par type. */
@Composable
fun TypesScreen(
    types: List<SubscriptionTypeEntity>,
    usage: Map<Long, Int>,
    onBack: () -> Unit,
    onAdd: () -> Unit,
    onEdit: (SubscriptionTypeEntity) -> Unit,
    onToggle: (SubscriptionTypeEntity, Boolean) -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        TopBar("Types d'abonnement", onBack)
        LazyColumn(
            Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(types, key = { it.id }) { t ->
                EcheoCard(Modifier.fillMaxWidth().clickable { onEdit(t) }) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconSquare(t.icon, t.color)
                        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                            Text(t.name, style = MaterialTheme.typography.titleMedium)
                            Text(
                                "${abonnementsLabel(usage[t.id] ?: 0)} · ${t.durationList.joinToString(", ")} mois",
                                style = MaterialTheme.typography.bodyMedium.copy(color = EcheoColors.TexteSecondaire),
                            )
                            Text(
                                "Toucher pour modifier",
                                style = MaterialTheme.typography.bodySmall.copy(color = EcheoColors.Turquoise),
                            )
                        }
                        Switch(
                            checked = t.enabled,
                            onCheckedChange = { onToggle(t, it) },
                            colors = SwitchDefaults.colors(checkedTrackColor = EcheoColors.Turquoise),
                        )
                    }
                }
            }
            item {
                PrimaryButton("Ajouter un type", onAdd, Modifier.fillMaxWidth())
            }
        }
    }
}

private val COLOR_PALETTE = listOf(
    0xFF0F2A4AL,
    0xFF19B5A5L,
    0xFFE50914L,
    0xFF7C3AEDL,
    0xFFF59E0BL,
    0xFF16A34AL,
    0xFF2563EBL,
    0xFF6B7280L,
)

private fun priceText(price: Double?): String = when {
    price == null -> ""
    price % 1.0 == 0.0 -> price.toLong().toString()
    else -> price.toString()
}

/**
 * Création ou modification d'un type : nom, durées, prix facultatif, icône et couleur.
 * Avec [initial], le formulaire modifie ce type (même ceux de la liste par défaut).
 */
@Composable
fun TypeFormScreen(
    initial: SubscriptionTypeEntity?,
    onBack: () -> Unit,
    onSave: (SubscriptionTypeEntity) -> Unit,
) {
    var name by remember { mutableStateOf(initial?.name.orEmpty()) }
    var durations by remember { mutableStateOf(initial?.durations ?: "1,3,6,12") }
    var price by remember { mutableStateOf(priceText(initial?.price)) }
    // Lettre personnalisée seulement si elle diffère de la 1re lettre du nom.
    var iconText by remember {
        mutableStateOf(
            if (initial != null && initial.icon != initial.name.trim().take(1).uppercase()) initial.icon else "",
        )
    }
    var color by remember { mutableStateOf(initial?.color ?: COLOR_PALETTE[1]) }
    val palette = if (color in COLOR_PALETTE) COLOR_PALETTE else COLOR_PALETTE + color
    val letter = iconText.ifBlank { name.trim().take(1).uppercase() }.ifBlank { "?" }
    val cleanDurations = durations.split(",").mapNotNull { it.trim().toIntOrNull()?.takeIf { m -> m > 0 } }
    val canSave = name.isNotBlank() && cleanDurations.isNotEmpty()

    Column(Modifier.fillMaxSize()) {
        TopBar(if (initial == null) "Nouveau type" else "Modifier le type", onBack)
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconSquare(letter, color)
            }
            Field("Nom", name, { name = it }, Modifier.fillMaxWidth(), placeholder = "Ex. Canva")
            Field(
                "Durées proposées (en mois)",
                durations,
                { durations = it },
                Modifier.fillMaxWidth(),
                placeholder = "1,3,12",
            )
            Field(
                "Prix (facultatif)",
                price,
                { price = it.replace(',', '.') },
                Modifier.fillMaxWidth(),
                keyboard = KeyboardType.Decimal,
            )
            Field(
                "Lettre de l'icône (facultatif)",
                iconText,
                { iconText = it.take(2) },
                Modifier.fillMaxWidth(),
                placeholder = "Par défaut : 1re lettre du nom",
            )
            Text("Couleur", style = MaterialTheme.typography.bodyMedium.copy(color = EcheoColors.TexteSecondaire))
            ChipRow {
                palette.forEach { c ->
                    val selected = c == color
                    Box(
                        Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(c))
                            .border(if (selected) 3.dp else 0.dp, if (selected) EcheoColors.BleuNuit else Color.Transparent, CircleShape)
                            .clickable { color = c },
                    )
                }
            }
            if (initial != null) {
                Text(
                    "Les abonnements déjà créés gardent leur durée et leur date d'expiration. Le nom, l'icône et la couleur sont mis à jour partout.",
                    style = MaterialTheme.typography.bodySmall.copy(color = EcheoColors.TexteSecondaire),
                )
            }
        }
        Box(Modifier.fillMaxWidth().padding(16.dp)) {
            PrimaryButton(
                if (initial == null) "Enregistrer le type" else "Enregistrer les modifications",
                {
                    if (canSave) {
                        val edited = SubscriptionTypeEntity(
                            name = name.trim(),
                            durations = cleanDurations.joinToString(","),
                            price = price.toDoubleOrNull(),
                            icon = letter,
                            color = color,
                            isCustom = true,
                        )
                        onSave(
                            if (initial == null) edited
                            else initial.copy(
                                name = edited.name,
                                durations = edited.durations,
                                price = edited.price,
                                icon = edited.icon,
                                color = edited.color,
                            ),
                        )
                    }
                },
                Modifier.fillMaxWidth(),
            )
        }
    }
}
