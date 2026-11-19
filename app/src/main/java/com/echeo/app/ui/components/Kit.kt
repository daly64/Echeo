package com.echeo.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.echeo.app.domain.SubStatus
import com.echeo.app.domain.formatDay
import com.echeo.app.ui.theme.EcheoColors

/** Barre de titre avec retour, pour les écrans secondaires. */
@Composable
fun TopBar(title: String, onBack: () -> Unit) {
    EcheoHeader {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(EcheoColors.SurBleu)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Icon(painterResource(com.echeo.app.R.drawable.ic_back), contentDescription = "Retour", tint = Color.White, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(12.dp))
            Text(title, style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontSize = 20.sp))
        }
    }
}

@Composable
fun SecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, EcheoColors.BleuNuit, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge.copy(color = EcheoColors.BleuNuit, fontSize = 15.sp))
    }
}

@Composable
fun DarkButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .clip(RoundedCornerShape(10.dp))
            .background(EcheoColors.BleuNuit)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge.copy(color = Color.White, fontSize = 15.sp))
    }
}

/** Champ de saisie avec libellé au-dessus, aux coins légèrement arrondis. */
@Composable
fun Field(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    keyboard: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
) {
    Column(modifier) {
        if (label.isNotEmpty()) {
            Text(label, style = MaterialTheme.typography.bodyMedium.copy(color = EcheoColors.TexteSecondaire))
            Spacer(Modifier.height(6.dp))
        }
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White)
                .border(1.dp, EcheoColors.Bordure, RoundedCornerShape(10.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            if (value.isEmpty() && placeholder.isNotEmpty()) {
                Text(placeholder, style = MaterialTheme.typography.bodyLarge.copy(color = EcheoColors.TexteDiscret))
            }
            BasicTextField(
                value = value,
                onValueChange = onChange,
                singleLine = singleLine,
                textStyle = MaterialTheme.typography.bodyLarge,
                keyboardOptions = KeyboardOptions(keyboardType = keyboard),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/** Pastille de choix : turquoise si sélectionnée. */
@Composable
fun Chip(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) EcheoColors.Turquoise else EcheoColors.NeutreFond)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge.copy(
                color = if (selected) Color.White else EcheoColors.Texte,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            ),
        )
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(text, style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp), modifier = modifier)
}

/** Étiquette colorée (état ou information). */
@Composable
fun Tag(text: String, colors: Pair<Color, Color>) {
    val (fg, bg) = colors
    Box(
        Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge.copy(color = fg, fontWeight = FontWeight.Bold))
    }
}

@Composable
fun StatusTag(text: String, status: SubStatus?) {
    Tag(text, statusColors(status))
}

/** Carré coloré avec une lettre : icône d'un type d'abonnement. */
@Composable
fun IconSquare(text: String, color: Long, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(color)),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = MaterialTheme.typography.titleSmall.copy(color = Color.White, fontWeight = FontWeight.Bold))
    }
}

/** Ligne de réglage cliquable, avec une valeur à droite. */
@Composable
fun NavRow(title: String, value: String?, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        if (value != null) {
            Text(value, style = MaterialTheme.typography.bodyLarge.copy(color = EcheoColors.TexteSecondaire))
            Spacer(Modifier.width(8.dp))
        }
        Text("›", style = MaterialTheme.typography.titleLarge.copy(color = EcheoColors.TexteDiscret))
    }
}

@Composable
fun SwitchRow(title: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(checkedTrackColor = EcheoColors.Turquoise),
        )
    }
}

/** Ligne libellé / valeur, pour les infos de licence. */
@Composable
fun InfoLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyMedium.copy(color = EcheoColors.TexteSecondaire), modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
    }
}

/** Bloc centré : icône, titre, texte et boutons. Pour les écrans de licence et de connexion. */
@Composable
fun CenteredMessage(
    @DrawableRes icon: Int,
    iconTint: Color,
    iconBg: Color,
    title: String,
    text: String,
    actions: @Composable ColumnScope.() -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            Modifier
                .size(84.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(painterResource(icon), contentDescription = null, tint = iconTint, modifier = Modifier.size(38.dp))
        }
        Spacer(Modifier.height(20.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodyLarge.copy(color = EcheoColors.TexteSecondaire),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(24.dp))
        actions()
    }
}

/** Bloc ID de l'appareil, à communiquer au fournisseur. */
@Composable
fun IdBox(id: String) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(EcheoColors.GrisClair)
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("ID de l'appareil", style = MaterialTheme.typography.bodySmall.copy(color = EcheoColors.TexteSecondaire))
        Spacer(Modifier.height(4.dp))
        Text(
            id,
            style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
        )
    }
}

/** Liste de choix dans une fenêtre. */
@Composable
fun <T> PickerDialog(title: String, options: List<Pair<T, String>>, onPick: (T) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                options.forEach { (value, label) ->
                    Text(
                        label,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPick(value) }
                            .padding(vertical = 12.dp),
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Annuler", color = EcheoColors.BleuNuit) } },
    )
}

/** Fenêtre de confirmation centrée au-dessus de l'écran assombri. */
@Composable
fun ConfirmDialog(
    title: String,
    text: String,
    confirmLabel: String,
    destructive: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmLabel, color = if (destructive) EcheoColors.ExpireTexte else EcheoColors.Turquoise, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler", color = EcheoColors.BleuNuit) } },
    )
}

/** Choix d'une date : affiche la date et ouvre le calendrier. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateField(label: String, day: Long, onChange: (Long) -> Unit, modifier: Modifier = Modifier) {
    var open by remember { mutableStateOf(false) }
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.bodyMedium.copy(color = EcheoColors.TexteSecondaire))
        Spacer(Modifier.height(6.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White)
                .border(1.dp, EcheoColors.Bordure, RoundedCornerShape(10.dp))
                .clickable { open = true }
                .padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            Text(formatDay(day), style = MaterialTheme.typography.bodyLarge)
        }
    }
    if (open) {
        val state = rememberDatePickerState(initialSelectedDateMillis = day * 86_400_000L)
        DatePickerDialog(
            onDismissRequest = { open = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { onChange(it / 86_400_000L) }
                    open = false
                }) { Text("OK", color = EcheoColors.Turquoise, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { open = false }) { Text("Annuler", color = EcheoColors.BleuNuit) } },
        ) {
            DatePicker(state = state)
        }
    }
}

/** Rangée de pastilles qui défile horizontalement. */
@Composable
fun ChipRow(content: @Composable RowScope.() -> Unit) {
    Row(
        Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        content = content,
    )
}
