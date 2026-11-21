package com.echeo.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.echeo.app.data.BackupFiles
import com.echeo.app.data.Settings
import com.echeo.app.domain.formatDateTime
import com.echeo.app.ui.components.*
import com.echeo.app.ui.theme.EcheoColors

private const val PICK_WARN = "warn"
private const val PICK_HOUR = "hour"

@Composable
fun SettingsScreen(
    managerName: String,
    shopName: String,
    warnDays: Int,
    reminderHour: Int,
    notify: Boolean,
    typesCount: Int,
    deviceId: String,
    licenceKey: String?,
    licenceExpiry: String?,
    onSaveProfile: (managerName: String, shopName: String) -> Unit,
    onWarnDays: (Int) -> Unit,
    onReminderHour: (Int) -> Unit,
    onNotify: (Boolean) -> Unit,
    onOpenTypes: () -> Unit,
    onOpenMessages: () -> Unit,
    onOpenBackup: () -> Unit,
    onActivate: () -> Unit,
    onCopy: () -> Unit,
) {
    var picker by remember { mutableStateOf<String?>(null) }
    var editingProfile by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize()) {
        EcheoHeader { HeaderTitle("Paramètres", null) }
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Group("Boutique") {
                NavRow("Nom du gérant", managerName.ifBlank { "Non renseigné" }) { editingProfile = true }
                RowDivider()
                NavRow("Nom de la boutique", shopName) { editingProfile = true }
            }
            Group("Licence") {
                NavRow("Clé de licence", if (licenceKey == null) "Non activée" else "Activée", onActivate)
                RowDivider()
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    InfoLine("Code du téléphone", deviceId)
                    if (licenceExpiry == "à vie") InfoLine("Validité", "À vie") else if (licenceExpiry != null) InfoLine("Valable jusqu'au", licenceExpiry)
                }
                RowDivider()
                NavRow("Copier le code du téléphone", null, onCopy)
            }
            Group("Abonnements") {
                NavRow("Types d'abonnement", "$typesCount", onOpenTypes)
                RowDivider()
                NavRow("Messages WhatsApp", null, onOpenMessages)
            }
            Group("Alertes") {
                NavRow("Avant expiration", "$warnDays jours") { picker = PICK_WARN }
                RowDivider()
                NavRow("Heure des rappels", "%02d h".format(reminderHour)) { picker = PICK_HOUR }
                RowDivider()
                SwitchRow("Notifications", notify, onNotify)
            }
            Group("Données") {
                NavRow("Sauvegarde et restauration", null, onOpenBackup)
            }
        }
    }

    if (editingProfile) {
        var manager by remember { mutableStateOf(managerName) }
        var shop by remember { mutableStateOf(shopName) }
        AlertDialog(
            onDismissRequest = { editingProfile = false },
            title = { Text("Gérant et boutique") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Field("Nom du gérant", manager, { manager = it }, Modifier.fillMaxWidth())
                    Field("Nom de la boutique", shop, { shop = it }, Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onSaveProfile(manager.trim(), shop.trim().ifBlank { Settings.DEFAULT_SHOP })
                    editingProfile = false
                }) { Text("Enregistrer") }
            },
            dismissButton = { TextButton(onClick = { editingProfile = false }) { Text("Annuler") } },
        )
    }

    when (picker) {
        PICK_WARN -> PickerDialog(
            title = "Avant expiration",
            options = listOf(3 to "3 jours", 7 to "7 jours", 14 to "14 jours", 30 to "30 jours"),
            onPick = {
                onWarnDays(it)
                picker = null
            },
            onDismiss = { picker = null },
        )
        PICK_HOUR -> PickerDialog(
            title = "Heure des rappels",
            options = listOf(8 to "8 h", 9 to "9 h", 10 to "10 h", 12 to "12 h", 18 to "18 h", 20 to "20 h"),
            onPick = {
                onReminderHour(it)
                picker = null
            },
            onDismiss = { picker = null },
        )
    }
}

@Composable
private fun Group(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionTitle(title)
        EcheoCard(Modifier.fillMaxWidth(), content = content)
    }
}

@Composable
fun MessagesScreen(
    before: String,
    after: String,
    onBack: () -> Unit,
    onSave: (String, String) -> Unit,
) {
    var beforeText by remember { mutableStateOf(before) }
    var afterText by remember { mutableStateOf(after) }
    Column(Modifier.fillMaxSize()) {
        TopBar("Messages WhatsApp", onBack)
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                "Variables : {nom}, {abonnement}, {date}, {jours}",
                style = MaterialTheme.typography.bodyMedium.copy(color = EcheoColors.TexteSecondaire),
            )
            Field("Avant expiration", beforeText, { beforeText = it }, Modifier.fillMaxWidth(), singleLine = false)
            Field("Après expiration", afterText, { afterText = it }, Modifier.fillMaxWidth(), singleLine = false)
            SecondaryButton(
                "Rétablir les textes par défaut",
                {
                    beforeText = Settings.WA_BEFORE_DEFAULT
                    afterText = Settings.WA_AFTER_DEFAULT
                },
                Modifier.fillMaxWidth(),
            )
        }
        Box(Modifier.fillMaxWidth().padding(16.dp)) {
            PrimaryButton("Enregistrer", { onSave(beforeText, afterText) }, Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun BackupScreen(
    lastBackupAt: Long,
    autoBackup: Boolean,
    lastAutoBackupAt: Long,
    autoLocation: String,
    onAutoBackup: (Boolean) -> Unit,
    onBack: () -> Unit,
    onExport: () -> Unit,
    onRestore: () -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        TopBar("Sauvegarde", onBack)
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Sauvegarde automatique", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        Switch(
                            checked = autoBackup,
                            onCheckedChange = onAutoBackup,
                            colors = SwitchDefaults.colors(checkedTrackColor = EcheoColors.Turquoise),
                        )
                    }
                    Text(
                        if (autoBackup) "Une copie est enregistrée chaque jour dans le dossier $autoLocation du téléphone. Les ${BackupFiles.KEEP} dernières copies sont gardées."
                        else "Désactivée : aucune copie automatique n'est enregistrée.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = EcheoColors.TexteSecondaire),
                    )
                    Text(
                        "Dernière copie automatique : " + if (lastAutoBackupAt == 0L) "aucune pour le moment" else formatDateTime(lastAutoBackupAt),
                        style = MaterialTheme.typography.bodyMedium.copy(color = EcheoColors.TexteSecondaire),
                    )
                }
            }
            EcheoCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Dernière sauvegarde manuelle", style = MaterialTheme.typography.titleMedium)
                    Text(
                        if (lastBackupAt == 0L) "Aucune pour le moment" else formatDateTime(lastBackupAt),
                        style = MaterialTheme.typography.bodyMedium.copy(color = EcheoColors.TexteSecondaire),
                    )
                }
            }
            Text(
                "Pour changer de téléphone, copiez le dossier de sauvegarde (ou envoyez un fichier par Drive ou e-mail), puis touchez « Restaurer une sauvegarde » sur le nouveau téléphone et choisissez ce fichier.",
                style = MaterialTheme.typography.bodyMedium.copy(color = EcheoColors.TexteSecondaire),
            )
            PrimaryButton("Créer une sauvegarde", onExport, Modifier.fillMaxWidth())
            SecondaryButton("Restaurer une sauvegarde", onRestore, Modifier.fillMaxWidth())
        }
    }
}
