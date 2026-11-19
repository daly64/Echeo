package com.echeo.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.echeo.app.R
import com.echeo.app.ui.components.*
import com.echeo.app.ui.theme.EcheoColors

@Composable
fun ActivationScreen(
    deviceId: String,
    error: String?,
    busy: Boolean,
    onCopy: () -> Unit,
    onActivate: (String) -> Unit,
) {
    var key by remember { mutableStateOf("") }
    Column(
        Modifier
            .fillMaxSize()
            .background(EcheoColors.GrisClair)
            .verticalScroll(rememberScrollState())
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(32.dp))
        Box(
            Modifier
                .size(84.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(EcheoColors.BleuNuit),
            contentAlignment = Alignment.Center,
        ) {
            Icon(painterResource(R.drawable.ic_lock), contentDescription = null, tint = Color.White, modifier = Modifier.size(38.dp))
        }
        Spacer(Modifier.height(20.dp))
        Text("Activer Echeo", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text(
            "Saisissez la clé de licence reçue. Elle est liée à ce téléphone.",
            style = MaterialTheme.typography.bodyLarge.copy(color = EcheoColors.TexteSecondaire),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(24.dp))
        IdBox(deviceId)
        Spacer(Modifier.height(10.dp))
        SecondaryButton("Copier l'ID", onCopy)
        Spacer(Modifier.height(24.dp))
        Field(
            label = "Clé de licence",
            value = key,
            onChange = { key = it.uppercase() },
            modifier = Modifier.fillMaxWidth(),
            placeholder = "ECH-XXXX-XXXX-XXXX",
        )
        if (error != null) {
            Spacer(Modifier.height(8.dp))
            Text(error, style = MaterialTheme.typography.bodyMedium.copy(color = EcheoColors.ExpireTexte), modifier = Modifier.fillMaxWidth())
        }
        Spacer(Modifier.height(20.dp))
        PrimaryButton(
            text = if (busy) "Vérification…" else "Activer",
            onClick = { if (!busy && key.isNotBlank()) onActivate(key.trim()) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun LicenceInvalidScreen(
    deviceId: String,
    message: String,
    onNewKey: () -> Unit,
    onCopy: () -> Unit,
) {
    Column(Modifier.fillMaxSize().background(EcheoColors.GrisClair)) {
        CenteredMessage(
            icon = R.drawable.ic_lock,
            iconTint = EcheoColors.ExpireTexte,
            iconBg = EcheoColors.ExpireFond,
            title = "Licence non valide",
            text = message,
        ) {
            IdBox(deviceId)
            Spacer(Modifier.height(16.dp))
            DarkButton("Saisir une nouvelle clé", onNewKey, Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            SecondaryButton("Copier l'ID", onCopy, Modifier.fillMaxWidth())
        }
    }
}
