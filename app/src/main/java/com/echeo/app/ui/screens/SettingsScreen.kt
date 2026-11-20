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
import androidx.compose.ui.unit.dp
import com.echeo.app.data.Settings
import com.echeo.app.domain.formatDateTime
import com.echeo.app.ui.components.*
import com.echeo.app.ui.theme.EcheoColors

private const val PICK_WARN = "warn"
private const val PICK_HOUR = "hour"

@Composable
fun SettingsScreen(
    warnDays: Int,
    reminderHour: Int,
    notify: Boolean,
    typesCount: Int,
    deviceId: String,
    licenceKey: String?,
    licenceExpiry: String?,
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
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Dernière sauvegarde", style = MaterialTheme.typography.titleMedium)
                    Text(
                        if (lastBackupAt == 0L) "Aucune pour le moment" else formatDateTime(lastBackupAt),
                        style = MaterialTheme.typography.bodyMedium.copy(color = EcheoColors.TexteSecondaire),
                    )
                }
            }
            Text(
                "La sauvegarde crée un fichier sur ce téléphone. Gardez-le en lieu sûr (Drive, clé USB, e-mail) : il permet de retrouver vos clients et abonnements.",
                style = MaterialTheme.typography.bodyMedium.copy(color = EcheoColors.TexteSecondaire),
            )
            PrimaryButton("Créer une sauvegarde", onExport, Modifier.fillMaxWidth())
            SecondaryButton("Restaurer une sauvegarde", onRestore, Modifier.fillMaxWidth())
        }
    }
}
